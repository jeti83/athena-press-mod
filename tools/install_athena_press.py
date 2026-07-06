#!/usr/bin/env python3
"""AthenaPress Installer fuer Server-Admins.

Installiert ein von 'scripts/build-release.ps1' erzeugtes Release-Paket
(Plugin-JAR + Asset-Pack + Manifest) in eine Hytale-Server-Installation und
richtet eine oder mehrere Welten dafuer ein. Benoetigt nur Python 3, kein
Java/Maven auf der Server-Seite.

Beispiele:
    python install_athena_press.py
    python install_athena_press.py --package athena-press-release-0.4.0.zip
    python install_athena_press.py --server-root /srv/hytale/UserData --world "Athena" --non-interactive
    python install_athena_press.py --all-worlds --dry-run
"""

import argparse
import json
import shutil
import sys
import tempfile
import zipfile
from pathlib import Path


WORLD_DIR_CANDIDATES = ["Saves", "saves", "Worlds", "worlds"]


class InstallError(Exception):
    pass


def log(message):
    print(message)


def find_default_package(script_dir):
    search_dirs = [Path.cwd(), script_dir, script_dir.parent, script_dir.parent / "dist"]
    for directory in search_dirs:
        if not directory.is_dir():
            continue
        unpacked = directory / "athena-press-release"
        if (unpacked / "release-info.json").is_file():
            return unpacked
        zips = sorted(directory.glob("athena-press-release*.zip"))
        if zips:
            return zips[-1]
    return None


def resolve_package(package_arg, script_dir):
    if package_arg:
        path = Path(package_arg).expanduser().resolve()
    else:
        path = find_default_package(script_dir)
        if path is None:
            raise InstallError(
                "Kein Release-Paket gefunden. Bitte --package <Pfad zu athena-press-release(.zip)> angeben."
            )
        log(f"Release-Paket gefunden: {path}")

    if not path.exists():
        raise InstallError(f"Release-Paket nicht gefunden: {path}")

    if path.is_file() and path.suffix == ".zip":
        extract_dir = Path(tempfile.mkdtemp(prefix="ap_install_"))
        with zipfile.ZipFile(path) as zf:
            zf.extractall(extract_dir)
        return extract_dir, True

    if path.is_dir():
        return path, False

    raise InstallError(f"Release-Paket unbekanntes Format: {path}")


def load_release_info(package_dir):
    info_path = package_dir / "release-info.json"
    if not info_path.is_file():
        raise InstallError(f"release-info.json fehlt im Release-Paket: {package_dir}")
    with open(info_path, "r", encoding="utf-8") as f:
        return json.load(f)


def looks_like_hytale_root(path):
    if not path.is_dir():
        return False
    names = {p.name for p in path.iterdir() if p.is_dir()}
    return "Mods" in names or bool(names & set(WORLD_DIR_CANDIDATES))


def platform_root_candidates():
    import os
    import platform

    candidates = []
    system = platform.system()
    if system == "Windows":
        appdata = os.environ.get("APPDATA")
        if appdata:
            candidates.append(Path(appdata) / "Hytale" / "UserData")
    else:
        home = Path.home()
        candidates.append(home / ".local" / "share" / "Hytale" / "UserData")
        candidates.append(home / ".config" / "Hytale" / "UserData")
        candidates.append(Path("/opt/hytale/UserData"))
        candidates.append(Path("/srv/hytale/UserData"))
    candidates.append(Path.cwd())
    return candidates


def prompt_for_root(non_interactive):
    if non_interactive:
        raise InstallError(
            "Server-Wurzelverzeichnis nicht automatisch gefunden. --server-root <Pfad> angeben."
        )
    while True:
        try:
            entered = input(
                "Pfad zum Hytale-Server-Datenverzeichnis (enthaelt 'Mods' und 'Saves'/'Worlds'): "
            ).strip()
        except EOFError:
            raise InstallError("Keine Eingabe moeglich. --server-root <Pfad> angeben.")
        if not entered:
            continue
        candidate = Path(entered).expanduser().resolve()
        if candidate.is_dir():
            return candidate
        log(f"Pfad existiert nicht: {candidate}")


def resolve_server_root(server_root_arg, non_interactive):
    if server_root_arg:
        root = Path(server_root_arg).expanduser().resolve()
        if not root.is_dir():
            raise InstallError(f"--server-root existiert nicht: {root}")
        return root

    found = [c for c in platform_root_candidates() if looks_like_hytale_root(c)]
    if len(found) == 1:
        log(f"Server-Verzeichnis automatisch erkannt: {found[0]}")
        return found[0]
    if len(found) > 1:
        if non_interactive:
            raise InstallError(
                "Mehrere moegliche Server-Verzeichnisse gefunden, --server-root angeben: "
                + ", ".join(str(c) for c in found)
            )
        log("Mehrere moegliche Server-Verzeichnisse gefunden:")
        for i, c in enumerate(found, 1):
            log(f"  [{i}] {c}")
        choice = input("Nummer waehlen: ").strip()
        try:
            return found[int(choice) - 1]
        except (ValueError, IndexError):
            raise InstallError("Ungueltige Auswahl.")

    log("Kein Server-Verzeichnis automatisch gefunden.")
    return prompt_for_root(non_interactive)


def find_world_dir(server_root, worlds_dir_arg, non_interactive):
    if worlds_dir_arg:
        candidate = server_root / worlds_dir_arg
        if not candidate.is_dir():
            raise InstallError(f"--worlds-dir existiert nicht: {candidate}")
        return candidate

    for name in WORLD_DIR_CANDIDATES:
        candidate = server_root / name
        if candidate.is_dir():
            return candidate

    if non_interactive:
        raise InstallError(
            f"Kein Welten-Ordner (Saves/Worlds) unter {server_root} gefunden. --worlds-dir angeben."
        )
    entered = input(
        f"Kein 'Saves'/'Worlds'-Ordner unter {server_root} gefunden. "
        "Name des Welten-Ordners eingeben (leer = ueberspringen): "
    ).strip()
    if not entered:
        return None
    candidate = server_root / entered
    if not candidate.is_dir():
        raise InstallError(f"Ordner existiert nicht: {candidate}")
    return candidate


def discover_worlds(world_dir):
    if world_dir is None:
        return []
    return sorted([p.name for p in world_dir.iterdir() if p.is_dir()])


def select_worlds(available, requested, all_worlds, non_interactive):
    if all_worlds:
        return available
    if requested:
        missing = [w for w in requested if w not in available]
        if missing:
            raise InstallError(
                f"Welt(en) nicht gefunden: {', '.join(missing)}. Verfuegbar: {', '.join(available) or '(keine)'}"
            )
        return requested
    if not available:
        return []
    if non_interactive:
        raise InstallError("--world oder --all-worlds im nicht-interaktiven Modus erforderlich.")

    log("Gefundene Welten:")
    for i, w in enumerate(available, 1):
        log(f"  [{i}] {w}")
    entered = input(
        "Welten waehlen (Nummern kommagetrennt, 'alle', oder leer = keine): "
    ).strip().lower()
    if not entered:
        return []
    if entered in ("alle", "all"):
        return available
    selected = []
    for part in entered.split(","):
        part = part.strip()
        if not part:
            continue
        try:
            selected.append(available[int(part) - 1])
        except (ValueError, IndexError):
            raise InstallError(f"Ungueltige Auswahl: {part}")
    return selected


def copy_file(src, dest, dry_run):
    if dry_run:
        log(f"  [dry-run] Kopiere Datei: {src} -> {dest}")
        return
    dest.parent.mkdir(parents=True, exist_ok=True)
    shutil.copy2(src, dest)
    log(f"  Datei kopiert: {dest}")


def copy_tree(src, dest, dry_run):
    if dry_run:
        log(f"  [dry-run] Kopiere Ordner: {src} -> {dest}")
        return
    dest.parent.mkdir(parents=True, exist_ok=True)
    shutil.copytree(src, dest, dirs_exist_ok=True)
    log(f"  Ordner kopiert: {dest}")


def install(package_dir, release_info, server_root, world_dir, worlds, dry_run):
    mods_dir = server_root / "Mods"

    jar_src = package_dir / "plugin" / release_info["pluginJar"]
    manifest_src = package_dir / "plugin" / "manifest.json"
    asset_src = package_dir / "assets" / release_info["assetFolderName"]
    plugin_icon_src = package_dir / "icons" / release_info["icons"]["plugin"]
    camera_icon_src = package_dir / "icons" / release_info["icons"]["camera"]

    log(f"\nInstalliere Plugin-JAR nach {mods_dir}")
    copy_file(jar_src, mods_dir / release_info["pluginJar"], dry_run)

    log(f"Installiere Asset-Pack nach {mods_dir / release_info['assetFolderName']}")
    copy_tree(asset_src, mods_dir / release_info["assetFolderName"], dry_run)
    if camera_icon_src.is_file():
        copy_file(camera_icon_src, mods_dir / release_info["assetFolderName"] / "icon-256.png", dry_run)

    if not worlds:
        log("\nKeine Welt ausgewaehlt - nur globale Mods-Dateien installiert.")
        return

    for world in worlds:
        plugin_data_dir = world_dir / world / "mods" / release_info["pluginFolderName"]
        log(f"\nRichte Welt '{world}' ein: {plugin_data_dir}")
        copy_file(manifest_src, plugin_data_dir / "manifest.json", dry_run)
        if plugin_icon_src.is_file():
            copy_file(plugin_icon_src, plugin_data_dir / "icon-256.png", dry_run)


def parse_args(argv):
    parser = argparse.ArgumentParser(description="AthenaPress Installer fuer Server-Admins")
    parser.add_argument("--package", help="Pfad zum Release-Paket (Ordner oder .zip)")
    parser.add_argument("--server-root", help="Hytale-Server-Datenverzeichnis (enthaelt Mods/, Saves oder Worlds)")
    parser.add_argument("--worlds-dir", help="Name des Welten-Ordners, falls nicht 'Saves'/'Worlds'")
    parser.add_argument("--world", action="append", dest="worlds", help="Welt(en) einrichten (mehrfach angebbar)")
    parser.add_argument("--all-worlds", action="store_true", help="Alle gefundenen Welten einrichten")
    parser.add_argument("--non-interactive", action="store_true", help="Keine Rueckfragen, erfordert --server-root und --world/--all-worlds")
    parser.add_argument("--dry-run", action="store_true", help="Nur anzeigen, was passieren wuerde")
    return parser.parse_args(argv)


def main(argv=None):
    args = parse_args(argv if argv is not None else sys.argv[1:])
    script_dir = Path(__file__).resolve().parent
    extracted = False
    package_dir = None

    try:
        package_dir, extracted = resolve_package(args.package, script_dir)
        release_info = load_release_info(package_dir)
        log(f"AthenaPress Release-Paket: Version {release_info.get('version', '?')}")

        server_root = resolve_server_root(args.server_root, args.non_interactive)
        world_dir = find_world_dir(server_root, args.worlds_dir, args.non_interactive)
        available_worlds = discover_worlds(world_dir)
        worlds = select_worlds(available_worlds, args.worlds, args.all_worlds, args.non_interactive)

        install(package_dir, release_info, server_root, world_dir, worlds, args.dry_run)

        log("\nFertig. Server/Welt neu starten und ingame '/ap' testen.")
        return 0
    except InstallError as exc:
        log(f"\nFehler: {exc}")
        return 1
    finally:
        if extracted and package_dir is not None:
            shutil.rmtree(package_dir, ignore_errors=True)


if __name__ == "__main__":
    sys.exit(main())
