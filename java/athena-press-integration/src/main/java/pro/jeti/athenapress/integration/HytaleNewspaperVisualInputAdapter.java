package pro.jeti.athenapress.integration;

public class HytaleNewspaperVisualInputAdapter {

    private final PlayerNewspaperVisualInputDispatcher inputDispatcher;

    public HytaleNewspaperVisualInputAdapter(
            PlayerNewspaperVisualInputDispatcher inputDispatcher
    ) {
        if (inputDispatcher == null) {
            throw new IllegalArgumentException("inputDispatcher must not be null");
        }

        this.inputDispatcher = inputDispatcher;
    }

    public void onNpcInteraction(HytalePlayerContext player, String issueId) {
        if (player == null) {
            return;
        }

        inputDispatcher.dispatch(
                PlayerNewspaperInputEvent.npcInteraction(player.playerId(), issueId)
        );
    }

    public void onItemUse(HytalePlayerContext player, String issueId) {
        if (player == null) {
            return;
        }

        inputDispatcher.dispatch(
                PlayerNewspaperInputEvent.itemUse(player.playerId(), issueId)
        );
    }

    public void onUiButton(HytalePlayerContext player, String command, String value) {
        if (player == null) {
            return;
        }

        inputDispatcher.dispatch(
                PlayerNewspaperInputEvent.uiButton(player.playerId(), command, value)
        );
    }

    public void onChatCommand(HytalePlayerContext player, String command, String value) {
        if (player == null) {
            return;
        }

        inputDispatcher.dispatch(
                PlayerNewspaperInputEvent.chatCommand(player.playerId(), command, value)
        );
    }

    public void onKeyBind(HytalePlayerContext player, String command, String value) {
        if (player == null) {
            return;
        }

        inputDispatcher.dispatch(
                PlayerNewspaperInputEvent.keyBind(player.playerId(), command, value)
        );
    }

    public void onServerTrigger(HytalePlayerContext player, String command, String value) {
        if (player == null) {
            return;
        }

        inputDispatcher.dispatch(
                PlayerNewspaperInputEvent.serverTrigger(player.playerId(), command, value)
        );
    }
}
