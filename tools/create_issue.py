import argparse
import re
import sys

from presslib import (
    ISSUE_TEMPLATE_FILE,
    ARTICLES_DRAFT,
    ARTICLES_PUBLISHED,
    ARTICLES_ARCHIVED,
    ISSUES_DRAFT,
    ISSUES_PUBLISHED,
    ISSUES_ARCHIVED,
    load_json,
    save_json,
    now_iso,
)


ISSUE_ID_PREFIX = "issue"


def collect_articles() -> dict:
    articles = {}
    article_dirs = [ARTICLES_DRAFT, ARTICLES_PUBLISHED, ARTICLES_ARCHIVED]

    for folder in article_dirs:
        if not folder.exists():
            continue

        for path in folder.glob("*.json"):
            data = load_json(path)
            article_id = data.get("id")

            if not article_id:
                print(f"Warnung: Artikel ohne ID übersprungen: {path}")
                continue

            if article_id in articles:
                print(f"Fehler: Doppelte Artikel-ID gefunden: {article_id}")
                print(f"Erste Datei: {articles[article_id]['path']}")
                print(f"Zweite Datei: {path}")
                sys.exit(1)

            articles[article_id] = {
                "path": path,
                "data": data
            }

    return articles


def collect_existing_issue_numbers() -> set[int]:
    numbers = set()
    issue_dirs = [ISSUES_DRAFT, ISSUES_PUBLISHED, ISSUES_ARCHIVED]

    file_pattern = re.compile(rf"^{ISSUE_ID_PREFIX}_(\d+)\.json$")

    for folder in issue_dirs:
        if not folder.exists():
            continue

        for path in folder.glob("*.json"):
            file_match = file_pattern.match(path.name)

            if file_match:
                numbers.add(int(file_match.group(1)))
                continue

            data = load_json(path)
            issue_id = data.get("id", "")
            id_match = re.match(rf"^{ISSUE_ID_PREFIX}_(\d+)$", issue_id)

            if id_match:
                numbers.add(int(id_match.group(1)))

            issue_number = data.get("issueNumber")
            if isinstance(issue_number, int):
                numbers.add(issue_number)

    return numbers


def get_next_issue_id_and_number() -> tuple[str, int]:
    existing_numbers = collect_existing_issue_numbers()
    next_number = 1

    while next_number in existing_numbers:
        next_number += 1

    issue_id = f"{ISSUE_ID_PREFIX}_{next_number:04d}"
    return issue_id, next_number


def get_cover_image_from_article(article: dict) -> str:
    image = article.get("image", {})

    if not isinstance(image, dict):
        return "placeholders/no_image.png"

    return image.get("file") or "placeholders/no_image.png"


def build_issue(args, articles: dict) -> dict:
    template = load_json(ISSUE_TEMPLATE_FILE)

    now = now_iso()
    issue_id, issue_number = get_next_issue_id_and_number()

    selected_article_ids = args.articles

    if not selected_article_ids:
        print("Fehler: Mindestens ein Artikel muss angegeben werden.")
        sys.exit(1)

    missing_articles = [
        article_id for article_id in selected_article_ids
        if article_id not in articles
    ]

    if missing_articles:
        print("Fehler: Folgende Artikel wurden nicht gefunden:")
        for article_id in missing_articles:
            print(f"  - {article_id}")
        print()
        print("Verfügbare Artikel:")
        for article_id in sorted(articles.keys()):
            article = articles[article_id]["data"]
            title = article.get("title", "(ohne Titel)")
            status = article.get("status", "unknown")
            print(f"  - {article_id} [{status}] {title}")
        sys.exit(1)

    main_article_id = args.main_article or selected_article_ids[0]

    if main_article_id not in selected_article_ids:
        print("Fehler: Der Titelartikel muss auch in der Artikelliste enthalten sein.")
        print(f"Titelartikel: {main_article_id}")
        sys.exit(1)

    main_article = articles[main_article_id]["data"]
    cover_image = args.cover_image or get_cover_image_from_article(main_article)

    template["id"] = issue_id
    template["status"] = "draft"
    template["issueNumber"] = issue_number

    template["title"] = args.title
    template["subtitle"] = args.subtitle or ""
    template["editionName"] = args.edition or ""

    template["cover"] = {
        "mainArticleId": main_article_id,
        "image": cover_image
    }

    template["articles"] = selected_article_ids

    template["createdAt"] = now
    template["updatedAt"] = now
    template["publishedAt"] = None
    template["deliveredToSubscribers"] = False

    return template


def parse_args():
    parser = argparse.ArgumentParser(
        description="Erstellt eine neue AthenaPress-Zeitungsausgabe."
    )

    parser.add_argument("--title", default="Athena Botenblatt", help="Titel der Zeitungsausgabe")
    parser.add_argument("--subtitle", default="", help="Untertitel der Ausgabe")
    parser.add_argument("--edition", default="", help="Name der Ausgabe")
    parser.add_argument("--articles", nargs="+", required=True, help="Artikel-IDs")
    parser.add_argument("--main-article", default=None, help="Artikel-ID des Titelartikels")
    parser.add_argument("--cover-image", default=None, help="Titelbild relativ zu AthenaPress/images/")

    return parser.parse_args()


def main():
    args = parse_args()
    articles = collect_articles()

    issue = build_issue(args, articles)
    issue_id = issue["id"]

    output_path = ISSUES_DRAFT / f"{issue_id}.json"

    if output_path.exists():
        print(f"Fehler: Datei existiert bereits: {output_path}")
        sys.exit(1)

    save_json(output_path, issue)

    print("Zeitungsausgabe erstellt:")
    print(f"  ID:           {issue_id}")
    print(f"  Nummer:       {issue['issueNumber']}")
    print(f"  Titel:        {issue['title']}")
    print(f"  Untertitel:   {issue['subtitle']}")
    print(f"  Ausgabe:      {issue['editionName']}")
    print(f"  Titelartikel: {issue['cover']['mainArticleId']}")
    print(f"  Titelbild:    {issue['cover']['image']}")
    print(f"  Artikel:      {', '.join(issue['articles'])}")
    print(f"  Datei:        {output_path}")


if __name__ == "__main__":
    main()