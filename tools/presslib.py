from pathlib import Path
from datetime import datetime
import json
import sys


PROJECT_DIR = Path(__file__).resolve().parents[1]
BASE_DIR = PROJECT_DIR / "AthenaPress"

ARTICLES_DIR = BASE_DIR / "articles"
ARTICLES_DRAFT = ARTICLES_DIR / "draft"
ARTICLES_PUBLISHED = ARTICLES_DIR / "published"
ARTICLES_ARCHIVED = ARTICLES_DIR / "archived"

ISSUES_DIR = BASE_DIR / "issues"
ISSUES_DRAFT = ISSUES_DIR / "draft"
ISSUES_PUBLISHED = ISSUES_DIR / "published"
ISSUES_ARCHIVED = ISSUES_DIR / "archived"

IMAGES_DIR = BASE_DIR / "images"
IMAGES_UPLOADED = IMAGES_DIR / "uploaded"
IMAGES_THUMBNAILS = IMAGES_DIR / "thumbnails"
IMAGES_PLACEHOLDERS = IMAGES_DIR / "placeholders"

TEMPLATES_DIR = BASE_DIR / "templates"
ARTICLE_TEMPLATE_FILE = TEMPLATES_DIR / "article_template.json"
ISSUE_TEMPLATE_FILE = TEMPLATES_DIR / "issue_template.json"
CATEGORIES_FILE = TEMPLATES_DIR / "categories.json"

SUBSCRIPTIONS_DIR = BASE_DIR / "subscriptions"
SUBSCRIBERS_FILE = SUBSCRIPTIONS_DIR / "subscribers.json"

PLAYERS_DIR = BASE_DIR / "players"
LOGS_DIR = BASE_DIR / "logs"

ALLOWED_STATUSES = {
    "draft",
    "published",
    "archived"
}

ALLOWED_IMAGE_SOURCE_TYPES = {
    "placeholder",
    "uploaded",
    "screenshot",
    "external",
    "camera_marker"
}

ALLOWED_DELIVERY_MODES = {
    "notification_only",
    "item_only",
    "item_and_notification",
    "mailbox"
}


def now_iso() -> str:
    return datetime.now().astimezone().isoformat(timespec="seconds")


def load_json(path: Path):
    try:
        with path.open("r", encoding="utf-8") as file:
            return json.load(file)
    except FileNotFoundError:
        print(f"Fehler: Datei nicht gefunden: {path}")
        sys.exit(1)
    except json.JSONDecodeError as e:
        print(f"Fehler: Ungültiges JSON in {path}")
        print(f"Zeile {e.lineno}, Spalte {e.colno}: {e.msg}")
        sys.exit(1)


def try_load_json(path: Path):
    if not path.exists():
        return None

    return load_json(path)


def save_json(path: Path, data: dict):
    path.parent.mkdir(parents=True, exist_ok=True)

    with path.open("w", encoding="utf-8") as file:
        json.dump(data, file, ensure_ascii=False, indent=2)
        file.write("\n")


def find_json_by_id(item_id: str, folders: list[Path]):
    candidates = [
        folder / f"{item_id}.json"
        for folder in folders
    ]

    for path in candidates:
        if path.exists():
            return path, load_json(path)

    print(f"Fehler: Datei nicht gefunden für ID: {item_id}")
    print("Gesucht in:")
    for path in candidates:
        print(f"  - {path}")

    sys.exit(1)


def find_article(article_id: str):
    return find_json_by_id(
        article_id,
        [ARTICLES_DRAFT, ARTICLES_PUBLISHED, ARTICLES_ARCHIVED]
    )


def find_issue(issue_id: str):
    return find_json_by_id(
        issue_id,
        [ISSUES_PUBLISHED, ISSUES_DRAFT, ISSUES_ARCHIVED]
    )


def load_category_ids(enabled_only: bool = True) -> set[str]:
    data = load_json(CATEGORIES_FILE)
    categories = data.get("categories", [])

    category_ids = set()

    for category in categories:
        if not isinstance(category, dict):
            continue

        category_id = category.get("id")
        enabled = category.get("enabled", True)

        if not category_id:
            continue

        if enabled_only and not enabled:
            continue

        category_ids.add(category_id)

    return category_ids


def load_categories_by_id() -> dict:
    data = load_json(CATEGORIES_FILE)
    categories = {}

    for category in data.get("categories", []):
        if not isinstance(category, dict):
            continue

        category_id = category.get("id")

        if category_id:
            categories[category_id] = category

    return categories


def ensure_id_matches(data: dict, expected_id: str, path: Path):
    file_id = data.get("id")

    if file_id != expected_id:
        print("Fehler: Datei-ID und angegebene ID stimmen nicht überein.")
        print(f"Angegeben: {expected_id}")
        print(f"In Datei:   {file_id}")
        print(f"Datei:      {path}")
        sys.exit(1)


def shorten(text: str, max_length: int = 55) -> str:
    text = text or ""

    if len(text) <= max_length:
        return text

    return text[: max_length - 3] + "..."


def format_date(value):
    if not value:
        return "-"

    try:
        parsed = datetime.fromisoformat(value)
        return parsed.strftime("%Y-%m-%d %H:%M")
    except ValueError:
        return value