package pro.jeti.athenapress.integration;

public record ChefRedakteurCommand(
        ChefRedakteurCommandType type,
        String targetId
) {

    public boolean targetsArticle() {
        return targetId != null && targetId.startsWith("article_");
    }

    public boolean targetsIssue() {
        return targetId != null && targetId.startsWith("issue_");
    }
}
