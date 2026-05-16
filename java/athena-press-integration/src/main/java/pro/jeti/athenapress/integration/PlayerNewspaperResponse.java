package pro.jeti.athenapress.integration;

@Deprecated
public record PlayerNewspaperResponse(
        String playerId,
        PlayerNewspaperAction action,
        String text,
        boolean newspaperOpen,
        String openIssueId,
        boolean closeRequested
) {

    public static PlayerNewspaperResponse of(
            String playerId,
            PlayerNewspaperAction action,
            String text,
            boolean newspaperOpen,
            String openIssueId
    ) {
        return new PlayerNewspaperResponse(
                playerId,
                action,
                text,
                newspaperOpen,
                openIssueId,
                false
        );
    }

    public static PlayerNewspaperResponse closed(
            String playerId,
            PlayerNewspaperAction action,
            String text
    ) {
        return new PlayerNewspaperResponse(
                playerId,
                action,
                text,
                false,
                null,
                true
        );
    }
}