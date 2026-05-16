package pro.jeti.athenapress.integration;

@Deprecated
public record PlayerNewspaperUiCommand(
        PlayerNewspaperAction action,
        String value,
        String uiCommand
) {

    public PlayerNewspaperUiCommand(PlayerNewspaperAction action, String value) {
        this(action, value, null);
    }

    public static PlayerNewspaperUiCommand openIssue(String issueId) {
        return new PlayerNewspaperUiCommand(PlayerNewspaperAction.OPEN_ISSUE, issueId);
    }

    public static PlayerNewspaperUiCommand showOverview() {
        return new PlayerNewspaperUiCommand(PlayerNewspaperAction.SHOW_OVERVIEW, null);
    }

    public static PlayerNewspaperUiCommand selectArticle(int articleNumber) {
        return new PlayerNewspaperUiCommand(
                PlayerNewspaperAction.SELECT_ARTICLE_BY_NUMBER,
                Integer.toString(articleNumber)
        );
    }

    public static PlayerNewspaperUiCommand selectArticle(String articleId) {
        return new PlayerNewspaperUiCommand(PlayerNewspaperAction.SELECT_ARTICLE_BY_ID, articleId);
    }

    public static PlayerNewspaperUiCommand closeIssue() {
        return new PlayerNewspaperUiCommand(PlayerNewspaperAction.CLOSE_ISSUE, null);
    }

    public static PlayerNewspaperUiCommand custom(String uiCommand, String value) {
        return new PlayerNewspaperUiCommand(null, value, uiCommand);
    }

    public boolean hasAction() {
        return action != null;
    }

    public boolean hasUiCommand() {
        return uiCommand != null && !uiCommand.isBlank();
    }
}