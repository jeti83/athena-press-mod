import sys

from presslib import (
    BASE_DIR,
    SUBSCRIBERS_FILE,
    ISSUES_DRAFT,
    ISSUES_PUBLISHED,
    ISSUES_ARCHIVED,
    load_json,
    try_load_json,
    format_date,
    shorten,
)


def collect_issues() -> dict:
    issues = {}

    folders = [
        ("draft", ISSUES_DRAFT),
        ("published", ISSUES_PUBLISHED),
        ("archived", ISSUES_ARCHIVED)
    ]

    for folder_status, folder in folders:
        if not folder.exists():
            continue

        for path in folder.glob("*.json"):
            data = try_load_json(path)

            if not data:
                continue

            issue_id = data.get("id", path.stem)

            issues[issue_id] = {
                "path": path,
                "folderStatus": folder_status,
                "data": data
            }

    return issues


def describe_issue(issue_id: str, issues: dict) -> str:
    issue = issues.get(issue_id)

    if not issue:
        return f"{issue_id} [FEHLT]"

    data = issue["data"]
    issue_number = data.get("issueNumber", "?")
    edition_name = data.get("editionName") or data.get("subtitle") or "-"
    status = data.get("status", issue["folderStatus"])

    return f"{issue_id} | Nr. {issue_number} | {shorten(edition_name, 34)} | {status}"


def print_subscriber(subscriber: dict, issues: dict, index: int):
    player_name = subscriber.get("playerName", "unknown")
    player_uuid = subscriber.get("playerUuid", "unknown")
    subscribed = subscriber.get("subscribed", False)
    delivery_mode = subscriber.get("deliveryMode", "unknown")

    subscribed_at = format_date(subscriber.get("subscribedAt"))
    updated_at = format_date(subscriber.get("updatedAt"))
    last_delivered_at = format_date(subscriber.get("lastDeliveredAt"))
    last_received_issue_id = subscriber.get("lastReceivedIssueId")

    unread_issues = subscriber.get("unreadIssues", [])

    if not isinstance(unread_issues, list):
        unread_issues = []

    status_text = "aktiv" if subscribed else "inaktiv"

    print(f"{index}. {player_name} [{status_text}]")
    print("-" * (len(f"{index}. {player_name} [{status_text}]")))

    print(f"UUID: {player_uuid}")
    print(f"Zustellung: {delivery_mode}")
    print(f"Abonniert seit: {subscribed_at}")
    print(f"Aktualisiert: {updated_at}")
    print(f"Zuletzt zugestellt: {last_delivered_at}")

    if last_received_issue_id:
        print(f"Letzte Ausgabe: {describe_issue(last_received_issue_id, issues)}")
    else:
        print("Letzte Ausgabe: -")

    if unread_issues:
        print("Ungelesene Ausgaben:")
        for issue_id in unread_issues:
            print(f"  - {describe_issue(issue_id, issues)}")
    else:
        print("Ungelesene Ausgaben: keine")

    print()


def main():
    data = load_json(SUBSCRIBERS_FILE)

    if "subscribers" not in data or not isinstance(data["subscribers"], list):
        print("Fehler: subscribers.json muss ein Objekt mit einer Liste 'subscribers' enthalten.")
        sys.exit(1)

    subscribers = data["subscribers"]
    issues = collect_issues()

    active_subscribers = [
        subscriber for subscriber in subscribers
        if subscriber.get("subscribed", False) is True
    ]

    inactive_subscribers = [
        subscriber for subscriber in subscribers
        if subscriber.get("subscribed", False) is not True
    ]

    print("AthenaPress Abonnentenübersicht")
    print("===============================")
    print(f"Projektordner: {BASE_DIR}")
    print(f"Gefundene Abonnenten: {len(subscribers)}")
    print(f"Aktiv: {len(active_subscribers)}")
    print(f"Inaktiv: {len(inactive_subscribers)}")
    print(f"Bekannte Ausgaben: {len(issues)}")
    print()

    if not subscribers:
        print("Keine Abonnenten gefunden.")
        return

    print("Aktive Abonnenten")
    print("=================")
    if active_subscribers:
        for index, subscriber in enumerate(active_subscribers, start=1):
            print_subscriber(subscriber, issues, index)
    else:
        print("Keine aktiven Abonnenten gefunden.")
        print()

    if inactive_subscribers:
        print("Inaktive Abonnenten")
        print("===================")
        for index, subscriber in enumerate(inactive_subscribers, start=1):
            print_subscriber(subscriber, issues, index)


if __name__ == "__main__":
    main()