package pro.jeti.athenapress.integration;

public record PlayerNewspaperVisualSession(
        String issueId,
        int spreadIndex
) {

    public PlayerNewspaperVisualSession {
        issueId = issueId == null ? "" : issueId;
        spreadIndex = Math.max(0, spreadIndex);
    }

    public PlayerNewspaperVisualSession atSpread(int nextSpreadIndex) {
        return new PlayerNewspaperVisualSession(issueId, nextSpreadIndex);
    }
}
