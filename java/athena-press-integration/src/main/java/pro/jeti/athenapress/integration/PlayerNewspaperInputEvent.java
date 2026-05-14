package pro.jeti.athenapress.integration;

public record PlayerNewspaperInputEvent(
        String playerId,
        PlayerNewspaperInputType inputType,
        String command,
        String value
) {

    public static PlayerNewspaperInputEvent npcInteraction(String playerId, String issueId) {
        return new PlayerNewspaperInputEvent(
                playerId,
                PlayerNewspaperInputType.NPC_INTERACTION,
                "open_issue",
                issueId
        );
    }

    public static PlayerNewspaperInputEvent itemUse(String playerId, String issueId) {
        return new PlayerNewspaperInputEvent(
                playerId,
                PlayerNewspaperInputType.ITEM_USE,
                "open_issue",
                issueId
        );
    }

    public static PlayerNewspaperInputEvent uiButton(String playerId, String command, String value) {
        return new PlayerNewspaperInputEvent(
                playerId,
                PlayerNewspaperInputType.UI_BUTTON,
                command,
                value
        );
    }

    public static PlayerNewspaperInputEvent chatCommand(String playerId, String command, String value) {
        return new PlayerNewspaperInputEvent(
                playerId,
                PlayerNewspaperInputType.CHAT_COMMAND,
                command,
                value
        );
    }

    public static PlayerNewspaperInputEvent keyBind(String playerId, String command, String value) {
        return new PlayerNewspaperInputEvent(
                playerId,
                PlayerNewspaperInputType.KEY_BIND,
                command,
                value
        );
    }

    public static PlayerNewspaperInputEvent serverTrigger(String playerId, String command, String value) {
        return new PlayerNewspaperInputEvent(
                playerId,
                PlayerNewspaperInputType.SERVER_TRIGGER,
                command,
                value
        );
    }
}