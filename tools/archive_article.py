import argparse
import sys

from presslib import (
    ARTICLES_ARCHIVED,
    find_article,
    save_json,
    now_iso,
    ensure_id_matches,
)


def archive_article(args):
    source_path, article = find_article(args.article_id)

    ensure_id_matches(article, args.article_id, source_path)

    target_path = ARTICLES_ARCHIVED / f"{args.article_id}.json"

    if source_path == target_path:
        if not args.reason:
            print(f"Keine Änderung: {args.article_id} ist bereits archiviert.")
            return

        now = now_iso()

        article["archiveReason"] = args.reason
        article["updatedAt"] = now

        if "archivedAt" not in article or not article["archivedAt"]:
            article["archivedAt"] = now

        if args.dry_run:
            print(f"[TEST] Archivierungsgrund würde aktualisiert werden: {args.article_id}")
            print(f"       Grund: {args.reason}")
            return

        save_json(source_path, article)

        print("Archivierungsgrund aktualisiert:")
        print(f"  ID:         {args.article_id}")
        print(f"  Grund:      {args.reason}")
        print(f"  Geändert:   {now}")
        print(f"  Datei:      {source_path}")
        return

    if target_path.exists() and not args.force:
        print(f"Fehler: Archivdatei existiert bereits: {target_path}")
        print("Nutze --force, wenn sie überschrieben werden soll.")
        sys.exit(1)

    now = now_iso()

    previous_status = article.get("status", "unknown")
    article["status"] = "archived"
    article["updatedAt"] = now
    article["archivedAt"] = now
    article["previousStatus"] = previous_status

    if args.reason:
        article["archiveReason"] = args.reason

    if args.dry_run:
        print(f"[TEST] Artikel würde archiviert werden: {args.article_id}")
        print(f"       Quelle: {source_path}")
        print(f"       Ziel:   {target_path}")
        print(f"       Vorheriger Status: {previous_status}")
        if args.reason:
            print(f"       Grund: {args.reason}")
        return

    save_json(target_path, article)

    if source_path.exists() and source_path != target_path:
        source_path.unlink()

    print("Artikel archiviert:")
    print(f"  ID:         {args.article_id}")
    print(f"  Titel:      {article.get('title', '')}")
    print(f"  Vorher:     {previous_status}")
    print("  Jetzt:      archived")
    print(f"  Archiviert: {now}")
    if args.reason:
        print(f"  Grund:      {args.reason}")
    print(f"  Datei:      {target_path}")


def parse_args():
    parser = argparse.ArgumentParser(
        description="Archiviert einen AthenaPress-Artikel."
    )

    parser.add_argument(
        "article_id",
        help="ID des Artikels, z.B. article_0002"
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
    archive_article(args)


if __name__ == "__main__":
    main()