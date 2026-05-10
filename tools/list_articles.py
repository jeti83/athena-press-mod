from presslib import (
    ARTICLES_DRAFT,
    ARTICLES_PUBLISHED,
    ARTICLES_ARCHIVED,
    BASE_DIR,
    load_json,
    load_categories_by_id,
    format_date,
    shorten,
)


def collect_articles() -> list[dict]:
    articles = []

    folders = [
        ("draft", ARTICLES_DRAFT),
        ("published", ARTICLES_PUBLISHED),
        ("archived", ARTICLES_ARCHIVED)
    ]

    for folder_status, folder in folders:
        if not folder.exists():
            continue

        for path in folder.glob("*.json"):
            data = load_json(path)

            articles.append({
                "id": data.get("id", path.stem),
                "status": data.get("status", folder_status),
                "folderStatus": folder_status,
                "categoryId": data.get("categoryId", "unknown"),
                "title": data.get("title", "(ohne Titel)"),
                "subtitle": data.get("subtitle", ""),
                "author": data.get("author", {}),
                "createdAt": data.get("createdAt"),
                "updatedAt": data.get("updatedAt"),
                "publishedAt": data.get("publishedAt"),
                "tags": data.get("tags", []),
                "image": data.get("image", {}),
                "path": path,
                "data": data
            })

    return articles


def print_article_group(title: str, articles: list[dict], categories: dict):
    print(title)
    print("=" * len(title))

    if not articles:
        print("  Keine Artikel gefunden.")
        print()
        return

    articles = sorted(
        articles,
        key=lambda item: (item.get("publishedAt") or item.get("createdAt") or "", item.get("id", "")),
        reverse=True
    )

    for article in articles:
        article_id = article["id"]
        status = article.get("status", "unknown")
        category_id = article.get("categoryId", "unknown")
        category = categories.get(category_id, {})
        category_name = category.get("name", category_id) if isinstance(category, dict) else category_id
        title_text = article.get("title", "(ohne Titel)")
        subtitle = article.get("subtitle", "")
        author = article.get("author", {})
        author_name = author.get("playerName", "unknown") if isinstance(author, dict) else "unknown"
        created_at = format_date(article.get("createdAt"))
        published_at = format_date(article.get("publishedAt"))
        tags = article.get("tags", [])
        image = article.get("image", {})
        image_file = image.get("file", "-") if isinstance(image, dict) else "-"

        print(
            f"  {article_id} | {shorten(title_text, 42)} | "
            f"{category_id} | {status}"
        )

        if subtitle:
            print(f"    Untertitel: {shorten(subtitle, 70)}")

        print(f"    Kategorie: {category_name}")
        print(f"    Autor: {author_name}")
        print(f"    Erstellt: {created_at} | Veröffentlicht: {published_at}")
        print(f"    Bild: {image_file}")

        if tags:
            print(f"    Tags: {', '.join(tags)}")

        print(f"    Datei: {article['path']}")
        print()


def main():
    categories = load_categories_by_id()
    articles = collect_articles()

    draft_articles = [article for article in articles if article["folderStatus"] == "draft"]
    published_articles = [article for article in articles if article["folderStatus"] == "published"]
    archived_articles = [article for article in articles if article["folderStatus"] == "archived"]

    print("AthenaPress Artikelübersicht")
    print("============================")
    print(f"Projektordner: {BASE_DIR}")
    print(f"Gefundene Kategorien: {len(categories)}")
    print(f"Gefundene Artikel: {len(articles)}")
    print()

    print_article_group("Veröffentlichte Artikel", published_articles, categories)
    print_article_group("Entwürfe", draft_articles, categories)
    print_article_group("Archivierte Artikel", archived_articles, categories)


if __name__ == "__main__":
    main()