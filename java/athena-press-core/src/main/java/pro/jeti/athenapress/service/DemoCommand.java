package pro.jeti.athenapress.service;

public record DemoCommand(
        DemoCommandType type,
        String issueId
) {

    public static DemoCommand showHelp() {
        return new DemoCommand(DemoCommandType.SHOW_HELP, null);
    }

    public static DemoCommand listPublishedIssues() {
        return new DemoCommand(DemoCommandType.LIST_PUBLISHED_ISSUES, null);
    }

    public static DemoCommand listArticles() {
        return new DemoCommand(DemoCommandType.LIST_ARTICLES, null);
    }

    public static DemoCommand statusOverview() {
        return new DemoCommand(DemoCommandType.SHOW_STATUS, null);
    }

    public static DemoCommand previewIssue(String issueId) {
        return new DemoCommand(DemoCommandType.PREVIEW_ISSUE, issueId);
    }

    public static DemoCommand validateIssue(String issueId) {
        return new DemoCommand(DemoCommandType.VALIDATE_ISSUE, issueId);
    }
}
