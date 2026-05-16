package pro.jeti.athenapress.integration;

import java.util.List;

import pro.jeti.athenapress.model.Article;

public class IssueEditorSession {

    private final String playerName;
    private final boolean admin;
    private final List<Article> availableArticles;

    private IssueEditorStep step = IssueEditorStep.SELECT_ARTICLES;
    private List<String> selectedArticleIds;
    private String mainArticleId;
    private String subtitle;

    public IssueEditorSession(String playerName, boolean admin, List<Article> availableArticles) {
        this.playerName = playerName;
        this.admin = admin;
        this.availableArticles = availableArticles != null ? availableArticles : List.of();
    }

    public IssueEditorStep step()              { return step; }
    public String playerName()                 { return playerName; }
    public boolean isAdmin()                   { return admin; }
    public List<Article> availableArticles()   { return availableArticles; }
    public List<String> selectedArticleIds()   { return selectedArticleIds; }
    public String mainArticleId()              { return mainArticleId; }
    public String subtitle()                   { return subtitle; }

    public boolean isActive() {
        return step != IssueEditorStep.SUBMITTED && step != IssueEditorStep.CANCELLED;
    }

    public void setSelectedArticles(List<String> articleIds) {
        this.selectedArticleIds = articleIds;
        this.step = IssueEditorStep.CHOOSE_MAIN_ARTICLE;
    }

    public void setMainArticle(String id) {
        this.mainArticleId = id;
        this.step = IssueEditorStep.ENTER_SUBTITLE;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
        this.step = IssueEditorStep.REVIEW;
    }

    public void markSubmitted() {
        this.step = IssueEditorStep.SUBMITTED;
    }

    public void cancel() {
        this.step = IssueEditorStep.CANCELLED;
    }
}
