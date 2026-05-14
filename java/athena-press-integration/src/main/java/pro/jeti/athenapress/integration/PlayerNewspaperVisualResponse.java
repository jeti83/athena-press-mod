package pro.jeti.athenapress.integration;

public record PlayerNewspaperVisualResponse(
        String playerId,
        String issueId,
        String title,
        int spreadIndex,
        int totalSpreadCount,
        NewspaperPreviewSpread spread,
        boolean newspaperOpen,
        String message
) {

    public PlayerNewspaperVisualResponse {
        title = title == null || title.isBlank() ? "AthenaPress" : title;
        spreadIndex = Math.max(0, spreadIndex);
        totalSpreadCount = Math.max(0, totalSpreadCount);
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
