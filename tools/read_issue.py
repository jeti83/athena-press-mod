import argparse
import sys

from presslib import (
    find_issue,
    find_article,
    load_categories_by_id,
    format_date,
)


def print_separator(char="=", length=72):
    print(char * length)


def print_article(article_id: str, index: int, categories: dict, full: bool):
    article_path, article = find_article(article_id)

    if not article:
        print(f"{index}. [FEHLT] {article_id}")
        print()
        return

    title = article.get("title", "(ohne Titel)")
    subtitle = article.get("subtitle", "")
    teaser = article.get("teaser", "")
    body = article.get("body", "")
    status = article.get("status", "unknown")
    category_id = article.get("categoryId", "unknown")
    category = categories.get(category_id, {})
    category_name = category.get("name", category_id) if isinstance(category, dict) else category_id

    author = article.get("author", {})
    author_name = author.get("playerName", "unknown") if isinstance(author, dict) else "unknown"

    image = article.get("image", {})
    image_file = image.get("file", "-") if isinstance(image, dict) else "-"
    image_caption = image.get("caption", "") if isinstance(image, dict) else ""
    image_credit = image.get("credit", "") if isinstance(image, dict) else ""

    created_at = format_date(article.get("createdAt"))
    published_at = format_date(article.get("publishedAt"))

    tags = article.get("tags", [])

    print(f"{index}. {title}")
    print("-" * (len(f"{index}. {title}")))

    if subtitle:
        print(subtitle)
        print()

    print(f"Kategorie: {category_name} ({category_id})")
    print(f"Autor: {author_name}")
    print(f"Status: {status}")
    print(f"Erstellt: {created_at} | Veröffentlicht: {published_at}")
    print(f"Bild: {image_file}")

    if image_caption:
        print(f"Bildunterschrift: {image_caption}")

    if image_credit:
        print(f"Bildnachweis: {image_credit}")

    if tags:
        print(f"Tags: {', '.join(tags)}")

    print(f"Datei: {article_path}")
    print()

    if full:
        if body:
            print(body)
        elif teaser:
            print(teaser)
        else:
            print("[Kein Artikeltext vorhanden.]")
    else:
        if teaser:
            print(f"Teaser: {teaser}")
        elif body:
            preview = body[:220].strip()
            if len(body) > 220:
                preview += "..."
            print(f"Vorschau: {preview}")
        else:
            print("[Kein Teaser oder Artikeltext vorhanden.]")

    print()
    print_separator("-", 72)
    print()


def read_issue(issue_id: str, full: bool):
    issue_path, issue = find_issue(issue_id)
    categories = load_categories_by_id()

    title = issue.get("title", "Athena Botenblatt")
    subtitle = issue.get("subtitle", "")
    edition_name = issue.get("editionName", "")
    issue_number = issue.get("issueNumber", "?")
    status = issue.get("status", "unknown")
    published_at = format_date(issue.get("publishedAt"))
    delivered = "ja" if issue.get("deliveredToSubscribers") else "nein"

    cover = issue.get("cover", {})
    main_article_id = cover.get("mainArticleId", "") if isinstance(cover, dict) else ""
    cover_image = cover.get("image", "-") if isinstance(cover, dict) else "-"

    articles = issue.get("articles", [])

    print_separator("=", 72)
    print(title.upper())
    print_separator("=", 72)

    if edition_name:
        print(edition_name)

    if subtitle:
        print(subtitle)

    print()
    print(f"Ausgabe: Nr. {issue_number} | ID: {issue_id} | Status: {status}")
    print(f"Veröffentlicht: {published_at} | Zugestellt: {delivered}")
    print(f"Titelartikel: {main_article_id or '-'}")
    print(f"Titelbild: {cover_image}")
    print(f"Datei: {issue_path}")
    print()

    if not isinstance(articles, list) or not articles:
        print("Diese Ausgabe enthält keine Artikel.")
        return

    print("Inhalt")
    print("------")

    for index, article_id in enumerate(articles, start=1):
        article_path, article = find_article(article_id)

        if not article:
            print(f"{index}. [FEHLT] {article_id}")
            continue

        article_title = article.get("title", "(ohne Titel)")
        category_id = article.get("categoryId", "unknown")
        category = categories.get(category_id, {})
        category_name = category.get("name", category_id) if isinstance(category, dict) else category_id

        marker = "Titelartikel" if article_id == main_article_id else category_name

        print(f"{index}. {article_title} [{marker}]")

    print()
    print_separator("=", 72)
    print()

    for index, article_id in enumerate(articles, start=1):
        print_article(article_id, index, categories, full)


def parse_args():
    parser = argparse.ArgumentParser(
        description="Zeigt eine AthenaPress-Ausgabe im Terminal an."
    )

    parser.add_argument(
        "issue_id",
        help="ID der Ausgabe, z.B. issue_0002"
    )

    parser.add_argument(
        "--full",
        action="store_true",
        help="Zeigt den vollständigen Artikeltext statt nur Teaser/Vorschau"
    )

    return parser.parse_args()


def main():
    args = parse_args()
    read_issue(args.issue_id, full=args.full)


if __name__ == "__main__":
    main()