package pro.jeti.athenapress.service;

import java.io.IOException;

import pro.jeti.athenapress.model.GameIssueView;

public class GameNewspaperSessionService {
    private final GameViewService gameViewService;
    private final GameTextRendererService gameTextRendererService;

    private GameIssueView currentIssueView;

    public GameNewspaperSessionService(
            GameViewService gameViewService,
            GameTextRendererService gameTextRendererService
    ) {
        this.gameViewService = gameViewService;
        this.gameTextRendererService = gameTextRendererService;
    }

    public String openIssue(String issueId) throws IOException {
        currentIssueView = gameViewService.createPublishedIssueView(issueId);
        return gameTextRendererService.createOverviewText(currentIssueView);
    }

    public String showOverview() {
        return gameTextRendererService.createOverviewText(currentIssueView);
    }

    public String showArticleByNumber(int articleNumber) {
        return gameTextRendererService.createArticleText(currentIssueView, articleNumber);
    }

    public String showArticleById(String articleId) {
        return gameTextRendererService.createArticleText(currentIssueView, articleId);
    }

    public void closeIssue() {
        currentIssueView = null;
    }

    public boolean hasOpenIssue() {
        return currentIssueView != null;
    }

    public String getOpenIssueId() {
        if (currentIssueView == null) {
            return null;
        }

        return currentIssueView.id();
    }
}