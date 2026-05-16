import argparse
import subprocess
import sys
from pathlib import Path


PROJECT_DIR = Path(__file__).resolve().parent
TOOLS_DIR = PROJECT_DIR / "tools"


def run_tool(tool_name: str, args: list[str]):
    tool_path = TOOLS_DIR / tool_name

    if not tool_path.exists():
        print(f"Fehler: Tool nicht gefunden: {tool_path}")
        sys.exit(1)

    command = [sys.executable, str(tool_path), *args]
    result = subprocess.run(command)

    if result.returncode != 0:
        sys.exit(result.returncode)


def add_article_create_args(parser):
    parser.add_argument("--title", "--titel", dest="title", required=True)
    parser.add_argument("--category", "--kategorie", dest="category", required=True)
    parser.add_argument("--author", "--autor", dest="author", required=True)
    parser.add_argument("--uuid", default="unknown")
    parser.add_argument("--subtitle", "--untertitel", dest="subtitle", default="")
    parser.add_argument("--teaser", "--heissmacher", dest="teaser", default="")
    parser.add_argument("--summary", "--zusammenfassung", dest="summary", default="")
    parser.add_argument("--body", "--text", dest="body", default="")
    parser.add_argument("--image", "--bild", dest="image", default="placeholders/no_image.png")
    parser.add_argument("--caption", "--bildunterschrift", dest="caption", default="")
    parser.add_argument("--credit", "--nachweis", dest="credit", default="")
    parser.add_argument(
        "--source-type",
        "--bildquelle",
        dest="source_type",
        default="placeholder",
        choices=["placeholder", "uploaded", "screenshot", "external", "camera_marker"]
    )
    parser.add_argument("--tags", nargs="*", default=[])


def add_article_edit_args(parser):
    parser.add_argument("article_id")
    parser.add_argument("--title", "--titel", dest="title", default=None)
    parser.add_argument("--subtitle", "--untertitel", dest="subtitle", default=None)
    parser.add_argument("--teaser", "--heissmacher", dest="teaser", default=None)
    parser.add_argument("--summary", "--zusammenfassung", dest="summary", default=None)
    parser.add_argument("--body", "--text", dest="body", default=None)
    parser.add_argument("--category", "--kategorie", dest="category", default=None)
    parser.add_argument("--author", "--autor", dest="author", default=None)
    parser.add_argument("--uuid", default=None)
    parser.add_argument("--image", "--bild", dest="image", default=None)
    parser.add_argument("--caption", "--bildunterschrift", dest="caption", default=None)
    parser.add_argument("--credit", "--nachweis", dest="credit", default=None)
    parser.add_argument(
        "--source-type",
        "--bildquelle",
        dest="source_type",
        default=None,
        choices=["placeholder", "uploaded", "screenshot", "external", "camera_marker"]
    )
    parser.add_argument("--tags", nargs="*", default=None)
    parser.add_argument("--clear-tags", "--tags-loeschen", dest="clear_tags", action="store_true")
    parser.add_argument("--location", "--ort", dest="location", nargs=4, metavar=("WORLD", "X", "Y", "Z"))
    parser.add_argument("--clear-location", "--ort-loeschen", dest="clear_location", action="store_true")


def add_issue_create_args(parser):
    parser.add_argument("--title", "--titel", dest="title", default="Athena Botenblatt")
    parser.add_argument("--subtitle", "--untertitel", dest="subtitle", default="")
    parser.add_argument("--edition", "--ausgabe-name", dest="edition", default="")
    parser.add_argument("--articles", "--artikel", dest="articles", nargs="+", required=True)
    parser.add_argument("--main-article", "--titelartikel", dest="main_article", default=None)
    parser.add_argument("--cover-image", "--titelbild", dest="cover_image", default=None)


def add_subscriber_add_args(parser):
    parser.add_argument("--name", required=True)
    parser.add_argument("--uuid", default="unknown")
    parser.add_argument(
        "--delivery-mode",
        "--zustellung",
        dest="delivery_mode",
        default="item_and_notification",
        choices=["notification_only", "item_only", "item_and_notification", "mailbox"]
    )


def build_parser():
    parser = argparse.ArgumentParser(
        description="Zentrale AthenaPress-Kommandozeile."
    )

    subparsers = parser.add_subparsers(dest="area", required=True)

    # validate / pruefen
    validate_parser = subparsers.add_parser(
        "validate",
        aliases=["pruefen"],
        help="Prüft den gesamten AthenaPress-Datenbestand."
    )
    validate_parser.set_defaults(tool="validate_press.py")

    # article / artikel
    article_parser = subparsers.add_parser(
        "article",
        aliases=["artikel"],
        help="Artikel verwalten."
    )
    article_sub = article_parser.add_subparsers(dest="action", required=True)

    article_create = article_sub.add_parser(
        "create",
        aliases=["erstellen"],
        help="Artikelentwurf erstellen."
    )
    add_article_create_args(article_create)
    article_create.set_defaults(tool="create_article.py")

    article_edit = article_sub.add_parser(
        "edit",
        aliases=["bearbeiten"],
        help="Artikel bearbeiten."
    )
    add_article_edit_args(article_edit)
    article_edit.set_defaults(tool="edit_article.py")

    article_list = article_sub.add_parser(
        "list",
        aliases=["liste", "auflisten"],
        help="Artikel auflisten."
    )
    article_list.set_defaults(tool="list_articles.py")

    article_archive = article_sub.add_parser(
        "archive",
        aliases=["archivieren"],
        help="Artikel archivieren."
    )
    article_archive.add_argument("article_id")
    article_archive.add_argument("--reason", "--grund", dest="reason", default=None)
    article_archive.add_argument("--dry-run", "--test", dest="dry_run", action="store_true")
    article_archive.add_argument("--force", "--erzwingen", dest="force", action="store_true")
    article_archive.set_defaults(tool="archive_article.py")

    # issue / ausgabe
    issue_parser = subparsers.add_parser(
        "issue",
        aliases=["ausgabe"],
        help="Ausgaben verwalten."
    )
    issue_sub = issue_parser.add_subparsers(dest="action", required=True)

    issue_create = issue_sub.add_parser(
        "create",
        aliases=["erstellen"],
        help="Ausgabe erstellen."
    )
    add_issue_create_args(issue_create)
    issue_create.set_defaults(tool="create_issue.py")

    issue_publish = issue_sub.add_parser(
        "publish",
        aliases=["veroeffentlichen"],
        help="Ausgabe veröffentlichen."
    )
    issue_publish.add_argument("issue_id")
    issue_publish.add_argument("--dry-run", "--test", dest="dry_run", action="store_true")
    issue_publish.set_defaults(tool="publish_issue.py")

    issue_read = issue_sub.add_parser(
        "read",
        aliases=["lesen"],
        help="Ausgabe lesen."
    )
    issue_read.add_argument("issue_id")
    issue_read.add_argument("--full", "--voll", dest="full", action="store_true")
    issue_read.set_defaults(tool="read_issue.py")

    issue_list = issue_sub.add_parser(
        "list",
        aliases=["liste", "auflisten"],
        help="Ausgaben auflisten."
    )
    issue_list.set_defaults(tool="list_issues.py")

    issue_archive = issue_sub.add_parser(
        "archive",
        aliases=["archivieren"],
        help="Ausgabe archivieren."
    )
    issue_archive.add_argument("issue_id")
    issue_archive.add_argument("--reason", "--grund", dest="reason", default=None)
    issue_archive.add_argument("--dry-run", "--test", dest="dry_run", action="store_true")
    issue_archive.add_argument("--force", "--erzwingen", dest="force", action="store_true")
    issue_archive.set_defaults(tool="archive_issue.py")

    issue_deliver = issue_sub.add_parser(
        "deliver",
        aliases=["zustellen"],
        help="Ausgabe zustellen."
    )
    issue_deliver.add_argument("issue_id")
    issue_deliver.add_argument("--dry-run", "--test", dest="dry_run", action="store_true")
    issue_deliver.add_argument("--force", "--erzwingen", dest="force", action="store_true")
    issue_deliver.set_defaults(tool="deliver_issue.py")

    # subscriber / abonnent
    subscriber_parser = subparsers.add_parser(
        "subscriber",
        aliases=["abonnent", "abo"],
        help="Abonnenten verwalten."
    )
    subscriber_sub = subscriber_parser.add_subparsers(dest="action", required=True)

    subscriber_add = subscriber_sub.add_parser(
        "add",
        aliases=["hinzufuegen", "aktivieren"],
        help="Abonnent hinzufügen oder reaktivieren."
    )
    add_subscriber_add_args(subscriber_add)
    subscriber_add.set_defaults(tool="subscribe_player.py")

    subscriber_remove = subscriber_sub.add_parser(
        "remove",
        aliases=["entfernen", "deaktivieren"],
        help="Abonnement deaktivieren."
    )
    subscriber_remove.add_argument("--name", default=None)
    subscriber_remove.add_argument("--uuid", default=None)
    subscriber_remove.add_argument("--clear-unread", "--ungelesene-loeschen", dest="clear_unread", action="store_true")
    subscriber_remove.set_defaults(tool="unsubscribe_player.py")

    subscriber_list = subscriber_sub.add_parser(
        "list",
        aliases=["liste", "auflisten"],
        help="Abonnenten auflisten."
    )
    subscriber_list.set_defaults(tool="list_subscribers.py")

    subscriber_read = subscriber_sub.add_parser(
        "read",
        aliases=["gelesen"],
        help="Ausgabe als gelesen markieren."
    )
    subscriber_read.add_argument("--name", default=None)
    subscriber_read.add_argument("--uuid", default=None)
    subscriber_read.add_argument("--issue", "--ausgabe", dest="issue", required=True)
    subscriber_read.set_defaults(tool="mark_issue_read.py")

    # draft / entwurf
    draft_parser = subparsers.add_parser(
        "draft",
        aliases=["entwurf"],
        help="Entwürfe verwalten."
    )
    draft_sub = draft_parser.add_subparsers(dest="action", required=True)

    draft_delete = draft_sub.add_parser(
        "delete",
        aliases=["loeschen"],
        help="Entwurf löschen."
    )
    draft_delete.add_argument("kind", choices=["article", "artikel", "issue", "ausgabe"])
    draft_delete.add_argument("item_id")
    draft_delete.add_argument("--dry-run", "--test", dest="dry_run", action="store_true")
    draft_delete.add_argument("--yes", "--ja", dest="yes", action="store_true")
    draft_delete.set_defaults(tool="delete_draft.py")

    # docs / doku
    docs_parser = subparsers.add_parser(
        "docs",
        aliases=["doku"],
        help="Dokumentation exportieren."
    )
    docs_sub = docs_parser.add_subparsers(dest="action", required=True)

    docs_export = docs_sub.add_parser(
        "export",
        aliases=["exportieren"],
        help="Markdown-Datei als PDF exportieren."
    )
    docs_export.add_argument("input", metavar="EINGABE", help="Pfad zur Markdown-Datei")
    docs_export.add_argument("output", metavar="AUSGABE", help="Pfad zur PDF-Ausgabe")
    docs_export.set_defaults(tool="export_pdf.py")

    return parser


def normalize_value_for_tool(key: str, value):
    if key == "kind":
        if value == "artikel":
            return "article"
        if value == "ausgabe":
            return "issue"

    return value


def namespace_to_tool_args(namespace: argparse.Namespace) -> tuple[str, list[str]]:
    data = vars(namespace).copy()

    tool = data.pop("tool", None)
    data.pop("area", None)
    data.pop("action", None)

    args = []

    for key in ["article_id", "issue_id", "kind", "item_id", "input", "output"]:
        value = data.pop(key, None)
        if value is not None:
            value = normalize_value_for_tool(key, value)
            args.append(str(value))

    for key, value in data.items():
        if value is None:
            continue

        value = normalize_value_for_tool(key, value)

        option_name = "--" + key.replace("_", "-")

        if isinstance(value, bool):
            if value:
                args.append(option_name)
            continue

        if isinstance(value, list):
            if not value:
                continue

            args.append(option_name)
            args.extend(str(item) for item in value)
            continue

        args.append(option_name)
        args.append(str(value))

    return tool, args


def main():
    parser = build_parser()
    namespace = parser.parse_args()

    tool_name, tool_args = namespace_to_tool_args(namespace)

    if not tool_name:
        print("Fehler: Kein Tool zugeordnet.")
        sys.exit(1)

    run_tool(tool_name, tool_args)


if __name__ == "__main__":
    main()