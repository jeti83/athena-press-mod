import argparse
import sys

from presslib import (
    find_article,
    save_json,
    now_iso,
    load_category_ids,
    ALLOWED_IMAGE_SOURCE_TYPES,
)


def update_if_provided(article: dict, field_name: str, value):
    if value is not None:
        article[field_name] = value
        return True

    return False


def update_author(article: dict, player_name: str | None, player_uuid: str | None):
    changed = False

    if "author" not in article or not isinstance(article["author"], dict):
        article["author"] = {
            "playerName": "",
            "playerUuid": "unknown"
        }
        changed = True

    if player_name is not None:
        article["author"]["playerName"] = player_name
        changed = True

    if player_uuid is not None:
        article["author"]["playerUuid"] = player_uuid
        changed = True

    return changed


def update_image(article: dict, args):
    changed = False

    image_args_used = any([
        args.image is not None,
        args.caption is not None,
        args.credit is not None,
        args.source_type is not None
    ])

    if not image_args_used:
        return False

    if "image" not in article or not isinstance(article["image"], dict):
        article["image"] = {
            "file": "placeholders/no_image.png",
            "caption": "",
            "credit": "",
            "sourceType": "placeholder"
        }
        changed = True

    if args.image is not None:
        article["image"]["file"] = args.image
        changed = True

    if args.caption is not None:
        article["image"]["caption"] = args.caption
        changed = True

    if args.credit is not None:
        article["image"]["credit"] = args.credit
        changed = True

    if args.source_type is not None:
        if args.source_type not in ALLOWED_IMAGE_SOURCE_TYPES:
            print(f"Fehler: Ungültiger sourceType: {args.source_type}")
            print("Erlaubte Werte:")
            for source_type in sorted(ALLOWED_IMAGE_SOURCE_TYPES):
                print(f"  - {source_type}")
            sys.exit(1)

        article["image"]["sourceType"] = args.source_type
        changed = True

    return changed


def edit_article(args):
    article_path, article = find_article(args.article_id)
    category_ids = load_category_ids()

    changed = False

    if args.category is not None:
        if args.category not in category_ids:
            print(f"Fehler: Unbekannte oder deaktivierte Kategorie: {args.category}")
            print()
            print("Verfügbare Kategorien:")
            for category_id in sorted(category_ids):
                print(f"  - {category_id}")
            sys.exit(1)

        article["categoryId"] = args.category
        changed = True

    changed |= update_if_provided(article, "title", args.title)
    changed |= update_if_provided(article, "subtitle", args.subtitle)
    changed |= update_if_provided(article, "teaser", args.teaser)
    changed |= update_if_provided(article, "summary", args.summary)
    changed |= update_if_provided(article, "body", args.body)

    changed |= update_author(article, args.author, args.uuid)
    changed |= update_image(article, args)

    if args.tags is not None:
        article["tags"] = args.tags
        changed = True

    if args.clear_tags:
        article["tags"] = []
        changed = True

    if args.location:
        if len(args.location) != 4:
            print("Fehler: --location erwartet genau 4 Werte: WORLD X Y Z")
            sys.exit(1)

        world, x, y, z = args.location

        try:
            article["location"] = {
                "enabled": True,
                "world": world,
                "x": float(x),
                "y": float(y),
                "z": float(z)
            }
        except ValueError:
            print("Fehler: X, Y und Z müssen Zahlen sein.")
            sys.exit(1)

        changed = True

    if args.clear_location:
        article["location"] = {
            "enabled": False,
            "world": "",
            "x": 0,
            "y": 0,
            "z": 0
        }
        changed = True

    if not changed:
        print("Keine Änderungen angegeben.")
        print("Beispiel:")
        print('  python tools\\edit_article.py article_0002 --subtitle "Vier Plots gegen die Holzknappheit"')
        return

    now = now_iso()
    article["updatedAt"] = now

    save_json(article_path, article)

    print("Artikel aktualisiert:")
    print(f"  ID:        {article.get('id', args.article_id)}")
    print(f"  Titel:     {article.get('title', '')}")
    print(f"  Kategorie: {article.get('categoryId', '')}")
    print(f"  Status:    {article.get('status', '')}")
    print(f"  Geändert:  {now}")
    print(f"  Datei:     {article_path}")


def parse_args():
    parser = argparse.ArgumentParser(
        description="Bearbeitet Felder eines AthenaPress-Artikels."
    )

    parser.add_argument("article_id", help="Artikel-ID, z.B. article_0002")

    parser.add_argument("--title", default=None, help="Neuer Artikeltitel")
    parser.add_argument("--subtitle", default=None, help="Neuer Untertitel")
    parser.add_argument("--teaser", default=None, help="Neuer Teaser")
    parser.add_argument("--summary", default=None, help="Neue kurze Zusammenfassung")
    parser.add_argument("--body", default=None, help="Neuer Artikeltext")
    parser.add_argument("--category", default=None, help="Neue Kategorie-ID")

    parser.add_argument("--author", default=None, help="Neuer Autorenname")
    parser.add_argument("--uuid", default=None, help="Neue Autoren-UUID")

    parser.add_argument("--image", default=None, help="Bildpfad relativ zu AthenaPress/images/")
    parser.add_argument("--caption", default=None, help="Bildunterschrift")
    parser.add_argument("--credit", default=None, help="Bildnachweis")
    parser.add_argument("--source-type", default=None, choices=sorted(ALLOWED_IMAGE_SOURCE_TYPES), help="Bildquelle")

    parser.add_argument("--tags", nargs="*", default=None, help="Ersetzt die Tags des Artikels")
    parser.add_argument("--clear-tags", action="store_true", help="Entfernt alle Tags")

    parser.add_argument("--location", nargs=4, metavar=("WORLD", "X", "Y", "Z"), help="Setzt einen Ortsbezug")
    parser.add_argument("--clear-location", action="store_true", help="Entfernt den Ortsbezug")

    return parser.parse_args()


def main():
    args = parse_args()
    edit_article(args)


if __name__ == "__main__":
    main()