package pro.jeti.athenapress.integration;

public record PlayerNewspaperUiCommand(
        PlayerNewspaperAction action,
        String value
) {

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
}