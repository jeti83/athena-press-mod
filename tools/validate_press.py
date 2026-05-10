from pathlib import Path
import json
import sys

from presslib import (
    BASE_DIR,
    ARTICLES_DRAFT,
    ARTICLES_PUBLISHED,
    ARTICLES_ARCHIVED,
    ISSUES_DRAFT,
    ISSUES_PUBLISHED,
    ISSUES_ARCHIVED,
    IMAGES_DIR,
    CATEGORIES_FILE,
    SUBSCRIBERS_FILE,
    ALLOWED_STATUSES,
    ALLOWED_IMAGE_SOURCE_TYPES,
    ALLOWED_DELIVERY_MODES,
)


errors = []
warnings = []


def load_json_soft(path: Path):
    try:
        with path.open("r", encoding="utf-8") as file:
            return json.load(file)
    except json.JSONDecodeError as e:
        errors.append(f"Ungültiges JSON in {path}: Zeile {e.lineno}, Spalte {e.colno} - {e.msg}")
    except Exception as e:
        errors.append(f"Datei konnte nicht gelesen werden: {path} - {e}")
    return None


def check_required_fields(data: dict, required_fields: list[str], file_path: Path):
    for field in required_fields:
        if field not in data:
            errors.append(f"Pflichtfeld fehlt in {file_path}: {field}")


def load_categories():
    if not CATEGORIES_FILE.exists():
        errors.append(f"Kategorien-Datei fehlt: {CATEGORIES_FILE}")
        return set()

    data = load_json_soft(CATEGORIES_FILE)
    if not data:
        return set()

    categories = data.get("categories")

    if not isinstance(categories, list):
        errors.append(f"'categories' muss eine Liste sein in {CATEGORIES_FILE}")
        return set()

    category_ids = set()

    for category in categories:
        if not isinstance(category, dict):
            errors.append(f"Ungültiger Kategorie-Eintrag in {CATEGORIES_FILE}: {category}")
            continue

        category_id = category.get("id")

        if not category_id:
            errors.append(f"Kategorie ohne ID in {CATEGORIES_FILE}")
            continue

        if category_id in category_ids:
            errors.append(f"Doppelte Kategorie-ID gefunden: {category_id}")

        category_ids.add(category_id)

    return category_ids


def collect_articles(category_ids: set[str]):
    articles = {}

    for folder in [ARTICLES_DRAFT, ARTICLES_PUBLISHED, ARTICLES_ARCHIVED]:
        if not folder.exists():
            warnings.append(f"Artikelordner fehlt: {folder}")
            continue

        for path in folder.glob("*.json"):
            data = load_json_soft(path)
            if not data:
                continue

            check_required_fields(
                data,
                ["id", "status", "categoryId", "title", "author", "body", "image"],
                path
            )

            article_id = data.get("id")
            if article_id:
                if article_id in articles:
                    errors.append(f"Doppelte Artikel-ID gefunden: {article_id}")
                articles[article_id] = {
                    "path": path,
                    "data": data
                }

            status = data.get("status")
            if status and status not in ALLOWED_STATUSES:
                errors.append(f"Ungültiger Artikel-Status in {path}: {status}")

            category_id = data.get("categoryId")
            if category_id and category_id not in category_ids:
                errors.append(f"Unbekannte Kategorie in {path}: {category_id}")

            image = data.get("image", {})

            if not isinstance(image, dict):
                errors.append(f"'image' muss ein Objekt sein in {path}")
                continue

            image_file = image.get("file")
            source_type = image.get("sourceType")

            if source_type and source_type not in ALLOWED_IMAGE_SOURCE_TYPES:
                errors.append(f"Ungültiger image.sourceType in {path}: {source_type}")

            if image_file:
                image_path = IMAGES_DIR / image_file

                if not image_path.exists():
                    warnings.append(
                        f"Bilddatei fehlt für Artikel {article_id}: {image_file}"
                    )

    return articles


def collect_issues():
    issues = {}

    for folder in [ISSUES_DRAFT, ISSUES_PUBLISHED, ISSUES_ARCHIVED]:
        if not folder.exists():
            warnings.append(f"Ausgabenordner fehlt: {folder}")
            continue

        for path in folder.glob("*.json"):
            data = load_json_soft(path)
            if not data:
                continue

            check_required_fields(
                data,
                ["id", "status", "issueNumber", "title", "cover", "articles"],
                path
            )

            issue_id = data.get("id")

            if issue_id:
                if issue_id in issues:
                    errors.append(f"Doppelte Ausgaben-ID gefunden: {issue_id}")

                issues[issue_id] = {
                    "path": path,
                    "data": data
                }

            status = data.get("status")
            if status and status not in ALLOWED_STATUSES:
                errors.append(f"Ungültiger Ausgaben-Status in {path}: {status}")

    return issues


def check_issues(issues: dict, articles: dict):
    for issue_id, issue_entry in issues.items():
        path = issue_entry["path"]
        data = issue_entry["data"]

        issue_articles = data.get("articles", [])

        if not isinstance(issue_articles, list):
            errors.append(f"'articles' muss eine Liste sein in {path}")
            continue

        for article_id in issue_articles:
            if article_id not in articles:
                errors.append(
                    f"Ausgabe {issue_id} verweist auf fehlenden Artikel: {article_id}"
                )

        cover = data.get("cover", {})

        if not isinstance(cover, dict):
            errors.append(f"'cover' muss ein Objekt sein in {path}")
            continue

        main_article_id = cover.get("mainArticleId")

        if main_article_id and main_article_id not in articles:
            errors.append(
                f"Ausgabe {issue_id} hat fehlenden Titelartikel: {main_article_id}"
            )

        cover_image = cover.get("image")
        if cover_image:
            cover_image_path = IMAGES_DIR / cover_image
            if not cover_image_path.exists():
                warnings.append(
                    f"Titelbild fehlt für Ausgabe {issue_id}: {cover_image}"
                )


def check_subscribers(issues: dict):
    if not SUBSCRIBERS_FILE.exists():
        warnings.append(f"Abonnenten-Datei fehlt: {SUBSCRIBERS_FILE}")
        return

    data = load_json_soft(SUBSCRIBERS_FILE)

    if not data:
        return

    subscribers = data.get("subscribers")

    if not isinstance(subscribers, list):
        errors.append(
            f"'subscribers' muss eine Liste sein in {SUBSCRIBERS_FILE}"
        )
        return

    seen_names = set()
    seen_uuids = set()

    for index, subscriber in enumerate(subscribers, start=1):
        if not isinstance(subscriber, dict):
            errors.append(
                f"Ungültiger Abonnenten-Eintrag #{index}: muss ein Objekt sein"
            )
            continue

        player_name = subscriber.get("playerName")
        player_uuid = subscriber.get("playerUuid", "unknown")
        delivery_mode = subscriber.get("deliveryMode")
        subscribed = subscriber.get("subscribed")
        unread_issues = subscriber.get("unreadIssues")
        last_received_issue_id = subscriber.get("lastReceivedIssueId")
        last_read_issue_id = subscriber.get("lastReadIssueId")

        label = player_name or f"Eintrag #{index}"

        if not player_name:
            errors.append(f"Abonnent ohne playerName: {label}")

        if subscribed is not True and subscribed is not False:
            errors.append(
                f"Abonnent {label} hat ungültiges subscribed-Feld: {subscribed}"
            )

        if delivery_mode not in ALLOWED_DELIVERY_MODES:
            errors.append(
                f"Abonnent {label} hat ungültigen deliveryMode: {delivery_mode}"
            )

        if unread_issues is None:
            errors.append(f"Abonnent {label} hat kein unreadIssues-Feld")
        elif not isinstance(unread_issues, list):
            errors.append(f"Abonnent {label}: unreadIssues muss eine Liste sein")
        else:
            for issue_id in unread_issues:
                if issue_id not in issues:
                    errors.append(
                        f"Abonnent {label} hat unbekannte ungelesene Ausgabe: {issue_id}"
                    )

        if last_received_issue_id and last_received_issue_id not in issues:
            errors.append(
                f"Abonnent {label} hat unbekannte lastReceivedIssueId: {last_received_issue_id}"
            )

        if last_read_issue_id and last_read_issue_id not in issues:
            errors.append(
                f"Abonnent {label} hat unbekannte lastReadIssueId: {last_read_issue_id}"
            )

        if player_name:
            normalized_name = player_name.lower()

            if normalized_name in seen_names:
                warnings.append(f"Doppelter Abonnentenname gefunden: {player_name}")

            seen_names.add(normalized_name)

        if player_uuid and player_uuid != "unknown":
            if player_uuid in seen_uuids:
                warnings.append(f"Doppelte Abonnenten-UUID gefunden: {player_uuid}")

            seen_uuids.add(player_uuid)


def main():
    print("AthenaPress JSON-Prüfung")
    print("========================")
    print(f"Projektordner: {BASE_DIR}")
    print()

    category_ids = load_categories()
    articles = collect_articles(category_ids)
    issues = collect_issues()

    check_issues(issues, articles)
    check_subscribers(issues)

    print(f"Gefundene Kategorien: {len(category_ids)}")
    print(f"Gefundene Artikel: {len(articles)}")
    print(f"Gefundene Ausgaben: {len(issues)}")
    print()

    if warnings:
        print("Warnungen:")
        for warning in warnings:
            print(f"  - {warning}")
        print()

    if errors:
        print("Fehler:")
        for error in errors:
            print(f"  - {error}")
        print()
        print("Prüfung fehlgeschlagen.")
        sys.exit(1)

    print("Keine kritischen Fehler gefunden.")
    print("AthenaPress-Daten sehen brauchbar aus.")


if __name__ == "__main__":
    main()