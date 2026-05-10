import argparse
import sys

from presslib import (
    ISSUES_ARCHIVED,
    find_issue,
    save_json,
    now_iso,
    ensure_id_matches,
)


def archive_issue(args):
    source_path, issue = find_issue(args.issue_id)

    ensure_id_matches(issue, args.issue_id, source_path)

    target_path = ISSUES_ARCHIVED / f"{args.issue_id}.json"

    if source_path == target_path:
        if not args.reason:
            print(f"Keine Änderung: {args.issue_id} ist bereits archiviert.")
            return

        now = now_iso()

        issue["archiveReason"] = args.reason
        issue["updatedAt"] = now

        if "archivedAt" not in issue or not issue["archivedAt"]:
            issue["archivedAt"] = now

        if args.dry_run:
            print(f"[TEST] Archivierungsgrund würde aktualisiert werden: {args.issue_id}")
            print(f"       Grund: {args.reason}")
            return

        save_json(source_path, issue)

        print("Archivierungsgrund aktualisiert:")
        print(f"  ID:         {args.issue_id}")
        print(f"  Grund:      {args.reason}")
        print(f"  Geändert:   {now}")
        print(f"  Datei:      {source_path}")
        return

    if target_path.exists() and not args.force:
        print(f"Fehler: Archivdatei existiert bereits: {target_path}")
        print("Nutze --force, wenn sie überschrieben werden soll.")
        sys.exit(1)

    now = now_iso()

    previous_status = issue.get("status", "unknown")
    issue["status"] = "archived"
    issue["updatedAt"] = now
    issue["archivedAt"] = now
    issue["previousStatus"] = previous_status

    if args.reason:
        issue["archiveReason"] = args.reason

    if args.dry_run:
        print(f"[TEST] Ausgabe würde archiviert werden: {args.issue_id}")
        print(f"       Quelle: {source_path}")
        print(f"       Ziel:   {target_path}")
        print(f"       Vorheriger Status: {previous_status}")
        if args.reason:
            print(f"       Grund: {args.reason}")
        return

    save_json(target_path, issue)

    if source_path.exists() and source_path != target_path:
        source_path.unlink()

    print("Ausgabe archiviert:")
    print(f"  ID:         {args.issue_id}")
    print(f"  Vorher:     {previous_status}")
    print("  Jetzt:      archived")
    print(f"  Archiviert: {now}")
    if args.reason:
        print(f"  Grund:      {args.reason}")
    print(f"  Datei:      {target_path}")


def parse_args():
    parser = argparse.ArgumentParser(
        description="Archiviert eine AthenaPress-Ausgabe."
    )

    parser.add_argument(
        "issue_id",
        help="ID der Ausgabe, z.B. issue_0001"
    )

    parser.add_argument(
        "--reason",
        default=None,
        help="Optionaler Archivierungsgrund"
    )

    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="Testlauf ohne Dateien zu verändern"
    )

    parser.add_argument(
        "--force",
        action="store_true",
        help="Überschreibt eine bereits vorhandene Archivdatei"
    )

    return parser.parse_args()


def main():
    args = parse_args()
    archive_issue(args)


if __name__ == "__main__":
    main()