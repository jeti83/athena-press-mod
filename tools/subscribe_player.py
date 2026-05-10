import argparse
import sys

from presslib import (
    SUBSCRIBERS_FILE,
    ALLOWED_DELIVERY_MODES,
    save_json,
    now_iso,
)
import json


DEFAULT_DELIVERY_MODE = "item_and_notification"


def load_subscribers_file():
    if not SUBSCRIBERS_FILE.exists():
        return {"subscribers": []}

    try:
        with SUBSCRIBERS_FILE.open("r", encoding="utf-8") as file:
            return json.load(file)
    except json.JSONDecodeError as e:
        print(f"Fehler: Ungültiges JSON in {SUBSCRIBERS_FILE}")
        print(f"Zeile {e.lineno}, Spalte {e.colno}: {e.msg}")
        sys.exit(1)


def find_subscriber(subscribers: list[dict], player_name: str, player_uuid: str | None):
    player_name_lower = player_name.lower()

    for subscriber in subscribers:
        existing_name = subscriber.get("playerName", "").lower()
        existing_uuid = subscriber.get("playerUuid", "unknown")

        if player_uuid and player_uuid != "unknown" and existing_uuid == player_uuid:
            return subscriber

        if existing_name == player_name_lower:
            return subscriber

    return None


def subscribe_player(args):
    data = load_subscribers_file()

    if "subscribers" not in data or not isinstance(data["subscribers"], list):
        print("Fehler: subscribers.json muss ein Objekt mit einer Liste 'subscribers' enthalten.")
        sys.exit(1)

    subscribers = data["subscribers"]
    now = now_iso()

    delivery_mode = args.delivery_mode

    if delivery_mode not in ALLOWED_DELIVERY_MODES:
        print(f"Fehler: Ungültiger deliveryMode: {delivery_mode}")
        print()
        print("Erlaubte Werte:")
        for mode in sorted(ALLOWED_DELIVERY_MODES):
            print(f"  - {mode}")
        sys.exit(1)

    existing = find_subscriber(subscribers, args.name, args.uuid)

    if existing:
        existing["playerName"] = args.name
        existing["playerUuid"] = args.uuid or existing.get("playerUuid", "unknown")
        existing["subscribed"] = True
        existing["deliveryMode"] = delivery_mode
        existing["updatedAt"] = now

        if "subscribedAt" not in existing or not existing["subscribedAt"]:
            existing["subscribedAt"] = now

        if "lastReceivedIssueId" not in existing:
            existing["lastReceivedIssueId"] = None

        if "unreadIssues" not in existing or not isinstance(existing["unreadIssues"], list):
            existing["unreadIssues"] = []

        print("Abonnent aktualisiert:")
    else:
        subscriber = {
            "playerName": args.name,
            "playerUuid": args.uuid or "unknown",
            "subscribed": True,
            "deliveryMode": delivery_mode,
            "subscribedAt": now,
            "updatedAt": now,
            "lastReceivedIssueId": None,
            "unreadIssues": []
        }

        subscribers.append(subscriber)
        existing = subscriber

        print("Abonnent hinzugefügt:")

    save_json(SUBSCRIBERS_FILE, data)

    print(f"  Name:          {existing['playerName']}")
    print(f"  UUID:          {existing['playerUuid']}")
    print(f"  Aktiv:         {existing['subscribed']}")
    print(f"  Zustellung:    {existing['deliveryMode']}")
    print(f"  Datei:         {SUBSCRIBERS_FILE}")


def parse_args():
    parser = argparse.ArgumentParser(
        description="Fügt einen AthenaPress-Abonnenten hinzu oder aktiviert ihn erneut."
    )

    parser.add_argument(
        "--name",
        required=True,
        help="Spielername"
    )

    parser.add_argument(
        "--uuid",
        default="unknown",
        help="Spieler-UUID, falls bekannt"
    )

    parser.add_argument(
        "--delivery-mode",
        default=DEFAULT_DELIVERY_MODE,
        choices=sorted(ALLOWED_DELIVERY_MODES),
        help="Art der Zustellung"
    )

    return parser.parse_args()


def main():
    args = parse_args()
    subscribe_player(args)


if __name__ == "__main__":
    main()