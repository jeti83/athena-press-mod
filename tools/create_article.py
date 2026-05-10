import argparse
import json
import re
import sys

from presslib import (
    ARTICLE_TEMPLATE_FILE,
    CATEGORIES_FILE,
    ARTICLES_DRAFT,
    ARTICLES_PUBLISHED,
    ARTICLES_ARCHIVED,
    load_json,
    save_json,
    now_iso,
    ALLOWED_IMAGE_SOURCE_TYPES,
)


ARTICLE_ID_PREFIX = "article"


def load_category_ids() -> set[str]:
    data = load_json(CATEGORIES_FILE)
    categories = data.get("categories", [])

    category_ids = set()

    for category in categories:
        if isinstance(category, dict) and category.get("enabled", True):
            category_id = category.get("id")
            if category_id:
                category_ids.add(category_id)

    return category_ids


def collect_existing_article_numbers() -> set[int]:
    numbers = set()
    article_dirs = [ARTICLES_DRAFT, ARTICLES_PUBLISHED, ARTICLES_ARCHIVED]

    pattern = re.compile(rf"^{ARTICLE_ID_PREFIX}_(\d+)\.json$")

    for folder in article_dirs:
        if not folder.exists():
            continue

        for path in folder.glob("*.json"):
            match = pattern.match(path.name)

            if match:
                numbers.add(int(match.group(1)))
                continue

            data = load_json(path)
            article_id = data.get("id", "")
            id_match = re.match(rf"^{ARTICLE_ID_PREFIX}_(\d+)$", article_id)

            if id_match:
                numbers.add(int(id_match.group(1)))

    return numbers


def get_next_article_id() -> str:
    existing_numbers = collect_existing_article_numbers()
    next_number = 1

    while next_number in existing_numbers:
        next_number += 1

    return f"{ARTICLE_ID_PREFIX}_{next_number:04d}"


def build_article(args) -> dict:
    template = load_json(ARTICLE_TEMPLATE_FILE)

    now = now_iso()
    article_id = get_next_article_id()

    template["id"] = article_id
    template["status"] = "draft"
    template["categoryId"] = args.category
    template["title"] = args.title
    template["subtitle"] = args.subtitle or ""
    template["teaser"] = args.teaser or ""

    template["author"] = {
        "playerName": args.author,
        "playerUuid": args.uuid or "unknown"
    }

    template["body"] = args.body or ""

    if "image" not in template or not isinstance(template["image"], dict):
        template["image"] = {}

    template["image"]["file"] = args.image or "placeholders/no_image.png"
    template["image"]["caption"] = args.caption or ""
    template["image"]["credit"] = args.credit or ""
    template["image"]["sourceType"] = args.source_type

    template["tags"] = args.tags or []

    template["createdAt"] = now
    template["updatedAt"] = now
    template["publishedAt"] = None

    return template


def parse_args():
    parser = argparse.ArgumentParser(
        description="Erstellt einen neuen AthenaPress-Artikelentwurf."
    )

    parser.add_argument("--title", required=True, help="Titel des Artikels")
    parser.add_argument("--category", required=True, help="Kategorie-ID")
    parser.add_argument("--author", required=True, help="Name des Autors")
    parser.add_argument("--uuid", default="unknown", help="Spieler-UUID des Autors, falls bekannt")
    parser.add_argument("--subtitle", default="", help="Untertitel des Artikels")
    parser.add_argument("--teaser", default="", help="Kurzer Anreißertext")
    parser.add_argument("--body", default="", help="Artikeltext")
    parser.add_argument("--image", default="placeholders/no_image.png", help="Bildpfad relativ zu AthenaPress/images/")
    parser.add_argument("--caption", default="", help="Bildunterschrift")
    parser.add_argument("--credit", default="", help="Bildnachweis")
    parser.add_argument("--source-type", default="placeholder", choices=sorted(ALLOWED_IMAGE_SOURCE_TYPES), help="Quelle des Bildes")
    parser.add_argument("--tags", nargs="*", default=[], help="Tags, getrennt durch Leerzeichen")

    return parser.parse_args()


def main():
    args = parse_args()

    category_ids = load_category_ids()

    if args.category not in category_ids:
        print(f"Fehler: Unbekannte oder deaktivierte Kategorie: {args.category}")
        print()
        print("Verfügbare Kategorien:")

        for category_id in sorted(category_ids):
            print(f"  - {category_id}")

        sys.exit(1)

    article = build_article(args)
    article_id = article["id"]

    output_path = ARTICLES_DRAFT / f"{article_id}.json"

    if output_path.exists():
        print(f"Fehler: Datei existiert bereits: {output_path}")
        sys.exit(1)

    save_json(output_path, article)

    print("Artikelentwurf erstellt:")
    print(f"  ID:        {article_id}")
    print(f"  Titel:     {article['title']}")
    print(f"  Kategorie: {article['categoryId']}")
    print(f"  Datei:     {output_path}")


if __name__ == "__main__":
    main()