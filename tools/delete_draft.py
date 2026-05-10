import argparse
import sys

from presslib import (
    ARTICLES_DRAFT,
    ISSUES_DRAFT,
    load_json,
)


def find_draft(kind: str, item_id: str):
    if kind == "article":
        path = ARTICLES_DRAFT / f"{item_id}.json"
    elif kind == "issue":
        path = ISSUES_DRAFT / f"{item_id}.json"
    else:
        print(f"Fehler: Unbekannter Typ: {kind}")
        sys.exit(1)

    if not path.exists():
        print(f"Fehler: Entwurf nicht gefunden: {path}")
        print("Hinweis: Dieses Skript löscht absichtlich nur Dateien aus draft/.")
        sys.exit(1)

    return path


def delete_draft(args):
    path = find_draft(args.kind, args.item_id)
    data = load_json(path)

    file_id = data.get("id")
    status = data.get("status")

    if file_id != args.item_id:
        print("Fehler: Datei-ID und angegebene ID stimmen nicht überein.")
        print(f"Angegeben: {args.item_id}")
        print(f"In Datei:   {file_id}")
        sys.exit(1)

    if status != "draft":
        print("Fehler: Nur Entwürfe mit status='draft' dürfen gelöscht werden.")
        print(f"Datei:  {path}")
        print(f"Status: {status}")
        sys.exit(1)

    title = data.get("title") or data.get("editionName") or "(ohne Titel)"

    if args.dry_run:
        print("[TEST] Entwurf würde gelöscht werden:")
        print(f"       Typ:    {args.kind}")
        print(f"       ID:     {args.item_id}")
        print(f"       Titel:  {title}")
        print(f"       Datei:  {path}")
        return

    if not args.yes:
        print("Sicherheitsabbruch: Zum Löschen bitte --yes angeben.")
        print()
        print("Geplanter Löschvorgang:")
        print(f"  Typ:    {args.kind}")
        print(f"  ID:     {args.item_id}")
        print(f"  Titel:  {title}")
        print(f"  Datei:  {path}")
        print()
        print("Ausführen mit:")
        print(f"  python tools\\delete_draft.py {args.kind} {args.item_id} --yes")
        sys.exit(1)

    path.unlink()

    print("Entwurf gelöscht:")
    print(f"  Typ:   {args.kind}")
    print(f"  ID:    {args.item_id}")
    print(f"  Titel: {title}")
    print(f"  Datei: {path}")


def parse_args():
    parser = argparse.ArgumentParser(
        description="Löscht ausschließlich AthenaPress-Entwürfe aus draft/."
    )

    parser.add_argument(
        "kind",
        choices=["article", "issue"],
        help="Typ des Entwurfs: article oder issue"
    )

    parser.add_argument(
        "item_id",
        help="ID des Entwurfs, z.B. article_0004 oder issue_0003"
    )

    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="Testlauf ohne Datei zu löschen"
    )

    parser.add_argument(
        "--yes",
        action="store_true",
        help="Bestätigt den Löschvorgang"
    )

    return parser.parse_args()


def main():
    args = parse_args()
    delete_draft(args)


if __name__ == "__main__":
    main()