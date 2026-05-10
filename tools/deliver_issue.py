import argparse
import sys

from presslib import (
    SUBSCRIBERS_FILE,
    ISSUES_PUBLISHED,
    load_json,
    save_json,
    now_iso,
    ensure_id_matches,
)


def find_published_issue(issue_id: str):
    issue_path = ISSUES_PUBLISHED / f"{issue_id}.json"

    if not issue_path.exists():
        print(f"Fehler: Veröffentlichte Ausgabe nicht gefunden: {issue_path}")
        print("Hinweis: Es können nur Ausgaben aus issues/published zugestellt werden.")
        sys.exit(1)

    return issue_path, load_json(issue_path)


def load_subscribers():
    if not SUBSCRIBERS_FILE.exists():
        print(f"Fehler: subscribers.json nicht gefunden: {SUBSCRIBERS_FILE}")
        sys.exit(1)

    data = load_json(SUBSCRIBERS_FILE)

    if "subscribers" not in data or not isinstance(data["subscribers"], list):
        print("Fehler: subscribers.json muss ein Objekt mit einer Liste 'subscribers' enthalten.")
        sys.exit(1)

    return data


def deliver_issue(issue_id: str, dry_run: bool = False, force: bool = False):
    issue_path, issue = find_published_issue(issue_id)
    ensure_id_matches(issue, issue_id, issue_path)

    subscribers_data = load_subscribers()
    subscribers = subscribers_data["subscribers"]

    now = now_iso()

    active_subscribers = [
        subscriber for subscriber in subscribers
        if subscriber.get("subscribed", False) is True
    ]

    if not active_subscribers:
        print("Keine aktiven Abonnenten gefunden.")
        return

    delivered_count = 0
    skipped_count = 0

    print(f"Zustellung für Ausgabe: {issue_id}")
    print(f"Aktive Abonnenten: {len(active_subscribers)}")
    print()

    for subscriber in active_subscribers:
        player_name = subscriber.get("playerName", "unknown")
        delivery_mode = subscriber.get("deliveryMode", "item_and_notification")

        unread_issues = subscriber.get("unreadIssues")

        if not isinstance(unread_issues, list):
            unread_issues = []
            subscriber["unreadIssues"] = unread_issues

        already_received = (
            issue_id in unread_issues
            or subscriber.get("lastReceivedIssueId") == issue_id
        )

        if already_received and not force:
            print(f"Übersprungen: {player_name} hat {issue_id} bereits erhalten.")
            skipped_count += 1
            continue

        if issue_id not in unread_issues:
            unread_issues.append(issue_id)

        subscriber["lastReceivedIssueId"] = issue_id
        subscriber["lastDeliveryMode"] = delivery_mode
        subscriber["lastDeliveredAt"] = now
        subscriber["updatedAt"] = now

        delivered_count += 1

        if dry_run:
            print(f"[TEST] Würde zustellen an: {player_name} ({delivery_mode})")
        else:
            print(f"Zugestellt an: {player_name} ({delivery_mode})")

    issue["deliveredToSubscribers"] = True
    issue["lastDeliveredAt"] = now
    issue["updatedAt"] = now

    if dry_run:
        print()
        print("[TEST] Keine Dateien wurden verändert.")
        print(f"[TEST] Würde subscribers.json aktualisieren: {SUBSCRIBERS_FILE}")
        print(f"[TEST] Würde Ausgabe aktualisieren: {issue_path}")
    else:
        save_json(SUBSCRIBERS_FILE, subscribers_data)
        save_json(issue_path, issue)

    print()
    print("Zusammenfassung:")
    print(f"  Zugestellt:   {delivered_count}")
    print(f"  Übersprungen: {skipped_count}")
    print(f"  Ausgabe:      {issue_id}")


def parse_args():
    parser = argparse.ArgumentParser(
        description="Stellt eine veröffentlichte AthenaPress-Ausgabe an aktive Abonnenten zu."
    )

    parser.add_argument(
        "issue_id",
        help="ID der veröffentlichten Ausgabe, z.B. issue_0002"
    )

    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="Testlauf ohne Dateien zu verändern"
    )

    parser.add_argument(
        "--force",
        action="store_true",
        help="Zustellung erneut eintragen, auch wenn die Ausgabe bereits erhalten wurde"
    )

    return parser.parse_args()


def main():
    args = parse_args()
    deliver_issue(args.issue_id, dry_run=args.dry_run, force=args.force)


if __name__ == "__main__":
    main()