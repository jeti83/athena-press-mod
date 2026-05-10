import argparse
import sys

from presslib import (
    SUBSCRIBERS_FILE,
    load_json,
    save_json,
    now_iso,
)


def find_subscriber(subscribers: list[dict], player_name: str | None, player_uuid: str | None):
    if player_uuid and player_uuid != "unknown":
        for subscriber in subscribers:
            if subscriber.get("playerUuid") == player_uuid:
                return subscriber

    if player_name:
        player_name_lower = player_name.lower()

        for subscriber in subscribers:
            existing_name = subscriber.get("playerName", "").lower()

            if existing_name == player_name_lower:
                return subscriber

    return None


def unsubscribe_player(args):
    if not args.name and not args.uuid:
        print("Fehler: Bitte entweder --name oder --uuid angeben.")
        sys.exit(1)

    data = load_json(SUBSCRIBERS_FILE)

    if "subscribers" not in data or not isinstance(data["subscribers"], list):
        print("Fehler: subscribers.json muss ein Objekt mit einer Liste 'subscribers' enthalten.")
        sys.exit(1)

    subscribers = data["subscribers"]
    subscriber = find_subscriber(subscribers, args.name, args.uuid)

    if not subscriber:
        print("Fehler: Abonnent nicht gefunden.")
        if args.name:
            print(f"Name: {args.name}")
        if args.uuid:
            print(f"UUID: {args.uuid}")
        sys.exit(1)

    player_name = subscriber.get("playerName", "unknown")
    was_subscribed = subscriber.get("subscribed", False)

    if was_subscribed is not True:
        print(f"Keine Änderung: {player_name} war bereits nicht aktiv abonniert.")
        return

    now = now_iso()

    subscriber["subscribed"] = False
    subscriber["unsubscribedAt"] = now
    subscriber["updatedAt"] = now

    if args.clear_unread:
        subscriber["unreadIssues"] = []

    save_json(SUBSCRIBERS_FILE, data)

    print("Abonnement deaktiviert:")
    print(f"  Spieler:       {player_name}")
    print(f"  UUID:          {subscriber.get('playerUuid', 'unknown')}")
    print(f"  Deaktiviert:   {now}")
    print(f"  Ungelesene gelöscht: {args.clear_unread}")
    print(f"  Datei:         {SUBSCRIBERS_FILE}")


def parse_args():
    parser = argparse.ArgumentParser(
        description="Deaktiviert ein AthenaPress-Abonnement, ohne den Eintrag zu löschen."
    )

    parser.add_argument(
        "--name",
        default=None,
        help="Spielername"
    )

    parser.add_argument(
        "--uuid",
        default=None,
        help="Spieler-UUID, falls bekannt"
    )

    parser.add_argument(
        "--clear-unread",
        action="store_true",
        help="Entfernt zusätzlich alle ungelesenen Ausgaben dieses Spielers"
    )

    return parser.parse_args()


def main():
    args = parse_args()
    unsubscribe_player(args)


if __name__ == "__main__":
    main()