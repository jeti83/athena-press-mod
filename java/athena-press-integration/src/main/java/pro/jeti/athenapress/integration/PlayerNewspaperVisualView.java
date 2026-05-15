package pro.jeti.athenapress.integration;

import java.util.List;

public record PlayerNewspaperVisualView(
        String playerId,
        String issueId,
        String title,
        int spreadIndex,
        int totalSpreadCount,
        NewspaperPreviewPage leftPage,
        NewspaperPreviewPage rightPage,
        boolean newspaperOpen,
        boolean hasPreviousSpread,
        boolean hasNextSpread,
        String message,
        List<NewspaperSpreadSignature> spreadSignatures,
        List<NewspaperSpreadMenuItem> spreadMenuItems,
        List<NewspaperUiButton> buttons
) {

    public PlayerNewspaperVisualView {
        title = title == null || title.isBlank() ? "AthenaPress" : title;
        spreadIndex = Math.max(0, spreadIndex);
        totalSpreadCount = Math.max(0, totalSpreadCount);
        message = message == null ? "" : message;
        spreadSignatures = spreadSignatures == null ? List.of() : List.copyOf(spreadSignatures);
        spreadMenuItems = spreadMenuItems == null ? List.of() : List.copyOf(spreadMenuItems);
        buttons = buttons == null ? List.of() : List.copyOf(buttons);
    }

    public boolean hasLeftPage() {
        return leftPage != null;
    }

    public boolean hasRightPage() {
        return rightPage != null;
    }

    public boolean hasMessage() {
        return !message.isBlank();
    }
}
