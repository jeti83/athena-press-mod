import argparse
import sys

from presslib import (
    ARTICLES_DRAFT,
    ARTICLES_PUBLISHED,
    ISSUES_DRAFT,
    ISSUES_PUBLISHED,
    load_json,
    save_json,
    now_iso,
    ensure_id_matches,
)


def find_issue_file(issue_id: str):
    issue_path = ISSUES_DRAFT / f"{issue_id}.json"

    if not issue_path.exists():
        print(f"Fehler: Draft-Ausgabe nicht gefunden: {issue_path}")
        sys.exit(1)

    return issue_path


def find_article_file(article_id: str):
    article_path = ARTICLES_DRAFT / f"{article_id}.json"

    if not article_path.exists():
        published_path = ARTICLES_PUBLISHED / f"{article_id}.json"

        if published_path.exists():
            return published_path

        print(f"Fehler: Artikel nicht gefunden: {article_id}")
        print("Gesucht in:")
        print(f"  - {article_path}")
        print(f"  - {published_path}")
        sys.exit(1)

    return article_path


def publish_article(article_id: str, published_at: str, dry_run: bool = False):
    source_path = find_article_file(article_id)

    article = load_json(source_path)

    article["status"] = "published"
    article["updatedAt"] = published_at
    article["publishedAt"] = article.get("publishedAt") or published_at

    target_path = ARTICLES_PUBLISHED / f"{article_id}.json"

    if dry_run:
        print(f"[TEST] Artikel würde veröffentlicht werden: {article_id}")
        print(f"       Quelle: {source_path}")
        print(f"       Ziel:   {target_path}")
        return

    save_json(target_path, article)

    if source_path != target_path and source_path.exists():
        source_path.unlink()

    print(f"Artikel veröffentlicht: {article_id}")


def publish_issue(issue_id: str, dry_run: bool = False):
    issue_path = find_issue_file(issue_id)
    issue = load_json(issue_path)

    ensure_id_matches(issue, issue_id, issue_path)

    articles = issue.get("articles", [])

    if not isinstance(articles, list) or not articles:
        print(f"Fehler: Ausgabe {issue_id} enthält keine gültige Artikelliste.")
        sys.exit(1)

    published_at = now_iso()

    print(f"Veröffentliche Ausgabe: {issue_id}")
    print(f"Artikelanzahl: {len(articles)}")
    print()

    for article_id in articles:
        publish_article(article_id, published_at, dry_run=dry_run)

    issue["status"] = "published"
    issue["updatedAt"] = published_at
    issue["publishedAt"] = issue.get("publishedAt") or published_at
    issue["deliveredToSubscribers"] = False

    target_path = ISSUES_PUBLISHED / f"{issue_id}.json"

    if dry_run:
        print()
        print(f"[TEST] Ausgabe würde veröffentlicht werden: {issue_id}")
        print(f"       Quelle: {issue_path}")
        print(f"       Ziel:   {target_path}")
        return

    save_json(target_path, issue)

    if issue_path.exists():
        issue_path.unlink()

    print()
    print(f"Ausgabe veröffentlicht: {issue_id}")
    print(f"Datei: {target_path}")


def parse_args():
    parser = argparse.ArgumentParser(
        description="Veröffentlicht eine AthenaPress-Ausgabe samt enthaltener Artikel."
    )

    parser.add_argument(
        "issue_id",
        help="ID der zu veröffentlichenden Ausgabe, z.B. issue_0002"
    )

    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="Testlauf ohne Dateien zu verändern"
    )

    return parser.parse_args()


def main():
    args = parse_args()
    publish_issue(args.issue_id, dry_run=args.dry_run)


if __name__ == "__main__":
    main()