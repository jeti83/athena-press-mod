package pro.jeti.athenapress.integration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pro.jeti.athenapress.model.Article;
import pro.jeti.athenapress.repository.ArticleRepository;
import pro.jeti.athenapress.service.IssueDraftRequest;
import pro.jeti.athenapress.service.IssueWriteService;

public class IssueEditorService {

    private final IssueWriteService issueWriteService;
    private final ArticleRepository articleRepository;
    private final Map<String, IssueEditorSession> sessionsByPlayerId = new HashMap<>();

    public IssueEditorService(IssueWriteService issueWriteService, ArticleRepository articleRepository) {
        this.issueWriteService = issueWriteService;
        this.articleRepository = articleRepository;
    }

    public IssueEditorView startEditing(String playerId, String playerName, boolean admin) throws IOException {
        List<Article> published = articleRepository.findAll().stream()
                .filter(a -> "published".equals(a.status()))
                .toList();
        IssueEditorSession session = new IssueEditorSession(playerName, admin, published);
        sessionsByPlayerId.put(playerId, session);
        return IssueEditorView.listArticles(published);
    }

    public IssueEditorView handleInput(String playerId, String input) throws IOException {
        IssueEditorSession session = sessionsByPlayerId.get(playerId);
        if (session == null || !session.isActive()) {
            return IssueEditorView.cancelled();
        }
        if (isCancelCommand(input)) {
            session.cancel();
            sessionsByPlayerId.remove(playerId);
            return IssueEditorView.cancelled();
        }
        return switch (session.step()) {
            case SELECT_ARTICLES       -> handleSelectArticles(session, input);
            case CHOOSE_MAIN_ARTICLE   -> handleChooseMainArticle(session, input);
            case ENTER_SUBTITLE        -> handleEnterSubtitle(session, input);
            case REVIEW                -> handleReview(playerId, session, input);
            default                    -> IssueEditorView.cancelled();
        };
    }

    public boolean hasActiveSession(String playerId) {
        IssueEditorSession session = sessionsByPlayerId.get(playerId);
        return session != null && session.isActive();
    }

    public void cancelSession(String playerId) {
        IssueEditorSession session = sessionsByPlayerId.get(playerId);
        if (session != null) session.cancel();
        sessionsByPlayerId.remove(playerId);
    }

    public int activeSessionCount() {
        return (int) sessionsByPlayerId.values().stream()
                .filter(IssueEditorSession::isActive)
                .count();
    }

    private IssueEditorView handleSelectArticles(IssueEditorSession session, String input) {
        List<Article> available = session.availableArticles();
        if (available.isEmpty()) {
            return IssueEditorView.error(IssueEditorStep.SELECT_ARTICLES,
                    "Keine veröffentlichten Artikel verfügbar.",
                    IssueEditorView.listArticles(available));
        }

        List<String> parsedIds = new ArrayList<>();
        String trimmed = input.trim().toLowerCase();

        if (trimmed.equals("alle") || trimmed.equals("all")) {
            available.stream().map(Article::id).forEach(parsedIds::add);
        } else {
            for (String token : input.split("[,\\s]+")) {
                String t = token.trim();
                if (t.isBlank()) continue;
                try {
                    int idx = Integer.parseInt(t) - 1;
                    if (idx < 0 || idx >= available.size()) {
                        return IssueEditorView.error(IssueEditorStep.SELECT_ARTICLES,
                                "Nummer " + (idx + 1) + " ist nicht in der Liste.",
                                IssueEditorView.listArticles(available));
                    }
                    parsedIds.add(available.get(idx).id());
                } catch (NumberFormatException e) {
                    boolean found = available.stream().anyMatch(a -> a.id().equals(t));
                    if (!found) {
                        return IssueEditorView.error(IssueEditorStep.SELECT_ARTICLES,
                                "Unbekannte ID: " + t,
                                IssueEditorView.listArticles(available));
                    }
                    parsedIds.add(t);
                }
            }
        }

        if (parsedIds.isEmpty()) {
            return IssueEditorView.error(IssueEditorStep.SELECT_ARTICLES,
                    "Mindestens ein Artikel muss ausgewählt werden.",
                    IssueEditorView.listArticles(available));
        }

        session.setSelectedArticles(parsedIds);
        List<Article> selectedArticles = available.stream()
                .filter(a -> parsedIds.contains(a.id()))
                .toList();
        return IssueEditorView.chooseMainArticle(selectedArticles);
    }

    private IssueEditorView handleChooseMainArticle(IssueEditorSession session, String input) {
        List<String> ids = session.selectedArticleIds();
        List<Article> selectedArticles = session.availableArticles().stream()
                .filter(a -> ids.contains(a.id()))
                .toList();

        if (isSkipCommand(input)) {
            String firstId = ids.get(0);
            session.setMainArticle(firstId);
            return IssueEditorView.enterSubtitle(ids, firstId);
        }

        try {
            int idx = Integer.parseInt(input.trim()) - 1;
            if (idx >= 0 && idx < selectedArticles.size()) {
                String mainId = selectedArticles.get(idx).id();
                session.setMainArticle(mainId);
                return IssueEditorView.enterSubtitle(ids, mainId);
            }
        } catch (NumberFormatException ignored) {}

        return IssueEditorView.error(IssueEditorStep.CHOOSE_MAIN_ARTICLE,
                "Bitte eine Nummer aus der Liste eingeben oder 'weiter' tippen.",
                IssueEditorView.chooseMainArticle(selectedArticles));
    }

    private IssueEditorView handleEnterSubtitle(IssueEditorSession session, String input) {
        session.setSubtitle(isSkipCommand(input) ? null : input.trim());
        return IssueEditorView.review(session);
    }

    private IssueEditorView handleReview(
            String playerId,
            IssueEditorSession session,
            String input
    ) throws IOException {
        if (isSubmitCommand(input)) {
            String draftId = issueWriteService.createDraft(new IssueDraftRequest(
                    session.playerName(),
                    session.subtitle(),
                    session.selectedArticleIds(),
                    session.mainArticleId()
            ));
            session.markSubmitted();
            sessionsByPlayerId.remove(playerId);
            return IssueEditorView.submitted(draftId);
        }
        return IssueEditorView.error(IssueEditorStep.REVIEW,
                "Tippe 'einreichen' zum Speichern oder 'abbrechen'.",
                IssueEditorView.review(session));
    }

    private boolean isCancelCommand(String input) {
        String lower = input.trim().toLowerCase();
        return lower.equals("abbrechen") || lower.equals("cancel");
    }

    private boolean isSkipCommand(String input) {
        String lower = input.trim().toLowerCase();
        return lower.equals("weiter") || lower.equals("skip") || lower.equals("überspringen");
    }

    private boolean isSubmitCommand(String input) {
        String lower = input.trim().toLowerCase();
        return lower.equals("einreichen") || lower.equals("submit") || lower.equals("speichern");
    }
}
