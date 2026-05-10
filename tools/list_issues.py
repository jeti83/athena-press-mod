from presslib import (
    BASE_DIR,
    ISSUES_DRAFT,
    ISSUES_PUBLISHED,
    ISSUES_ARCHIVED,
    ARTICLES_DRAFT,
    ARTICLES_PUBLISHED,
    ARTICLES_ARCHIVED,
    load_json,
    format_date,
    shorten,
)


def collect_articles() -> dict:
    articles = {}

    for folder in [ARTICLES_DRAFT, ARTICLES_PUBLISHED, ARTICLES_ARCHIVED]:
        if not folder.exists():
            continue

        for path in folder.glob("*.json"):
            data = load_json(path)
            article_id = data.get("id")

            if article_id:
                articles[article_id] = {
                    "path": path,
                    "data": data
                }

    return articles


def collect_issues() -> list[dict]:
    issues = []

    folders = [
        ("draft", ISSUES_DRAFT),
        ("published", ISSUES_PUBLISHED),
        ("archived", ISSUES_ARCHIVED)
    ]

    for folder_status, folder in folders:
        if not folder.exists():
            continue

        for path in folder.glob("*.json"):
            data = load_json(path)

            issue_id = data.get("id", path.stem)
            status = data.get("status", folder_status)

            issues.append({
                "id": issue_id,
                "status": status,
                "folderStatus": folder_status,
                "issueNumber": data.get("issueNumber", 0),
                "title": data.get("title", ""),
                "subtitle": data.get("subtitle", ""),
                "editionName": data.get("editionName", ""),
                "articles": data.get("articles", []),
                "publishedAt": data.get("publishedAt"),
                "deliveredToSubscribers": data.get("deliveredToSubscribers", False),
                "path": path,
                "data": data
            })

    return issues


def print_issue_group(title: str, issues: list[dict], articles: dict):
    print(title)
    print("=" * len(title))

    if not issues:
        print("  Keine Ausgaben gefunden.")
        print()
        return

    issues = sorted(
        issues,
        key=lambda item: (item.get("issueNumber", 0), item.get("id", "")),
        reverse=True
    )

    for issue in issues:
        issue_id = issue["id"]
        issue_number = issue.get("issueNumber", 0)
        edition_name = issue.get("editionName") or "-"
        subtitle = issue.get("subtitle") or "-"
        status = issue.get("status") or "unknown"
        article_ids = issue.get("articles", [])
        published_at = format_date(issue.get("publishedAt"))
        delivered = "ja" if issue.get("deliveredToSubscribers") else "nein"

        missing_articles = [
            article_id for article_id in article_ids
            if article_id not in articles
        ]

        marker = "!" if missing_articles else " "

        print(
            f"{marker} {issue_id} | Nr. {issue_number} | "
            f"{shorten(edition_name, 24)} | "
            f"{len(article_ids)} Artikel | {status}"
        )

        print(f"    Untertitel: {shorten(subtitle, 70)}")
        print(f"    Veröffentlicht: {published_at} | Zugestellt: {delivered}")

        if article_ids:
            print("    Artikel:")
            for article_id in article_ids:
                article = articles.get(article_id)

                if not article:
                    print(f"      - {article_id} [FEHLT]")
                    continue

                article_data = article["data"]
                article_title = article_data.get("title", "(ohne Titel)")
                article_status = article_data.get("status", "unknown")
                category_id = article_data.get("categoryId", "unknown")

                print(
                    f"      - {article_id} [{article_status}] "
                    f"{shorten(article_title, 50)} ({category_id})"
                )

        if missing_articles:
            print("    Warnung: Diese Ausgabe verweist auf fehlende Artikel.")

        print()


def main():
    articles = collect_articles()
    issues = collect_issues()

    draft_issues = [issue for issue in issues if issue["folderStatus"] == "draft"]
    published_issues = [issue for issue in issues if issue["folderStatus"] == "published"]
    archived_issues = [issue for issue in issues if issue["folderStatus"] == "archived"]

    print("AthenaPress Ausgabenübersicht")
    print("=============================")
    print(f"Projektordner: {BASE_DIR}")
    print(f"Gefundene Artikel: {len(articles)}")
    print(f"Gefundene Ausgaben: {len(issues)}")
    print()

    print_issue_group("Veröffentlichte Ausgaben", published_issues, articles)
    print_issue_group("Entwürfe", draft_issues, articles)
    print_issue_group("Archivierte Ausgaben", archived_issues, articles)


if __name__ == "__main__":
    main()