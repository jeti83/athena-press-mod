package pro.jeti.athenapress.integration;

import java.util.List;

public record PlayerNewspaperVisualResponse(
        String playerId,
        String issueId,
        String title,
        int spreadIndex,
        int totalSpreadCount,
        NewspaperPreviewSpread spread,
        List<NewspaperSpreadSignature> spreadSignatures,
        boolean newspaperOpen,
        String message
) {

    public PlayerNewspaperVisualResponse {
        title = title == null || title.isBlank() ? "AthenaPress" : title;
        spreadIndex = Math.max(0, spreadIndex);
        totalSpreadCount = Math.max(0, totalSpreadCount);
        spreadSignatures = spreadSignatures == null ? List.of() : List.copyOf(spreadSignatures);
        message = message == null ? "" : message;
    }

    public static PlayerNewspaperVisualResponse missing(
            String playerId,
            String message
    ) {
        return new PlayerNewspaperVisualResponse(
                playerId,
                null,
                "AthenaPress",
                0,
                0,
                null,
                List.of(),
                false,
                message
        );
    }

    public boolean hasSpread() {
        return spread != null;
    }

    public boolean hasPreviousSpread() {
        return newspaperOpen && spreadIndex > 0;
    }

    public boolean hasNextSpread() {
        return newspaperOpen && spreadIndex + 1 < totalSpreadCount;
    }
}
