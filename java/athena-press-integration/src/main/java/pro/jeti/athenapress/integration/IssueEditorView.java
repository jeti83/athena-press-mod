package pro.jeti.athenapress.integration;

import java.util.List;

import pro.jeti.athenapress.model.Article;

public record IssueEditorView(
        IssueEditorStep step,
        String prompt,
        List<String> selectedArticleIds,
        String mainArticleId,
        String subtitle,
        String message,
        String draftId
) {

    public boolean isComplete()   { return step == IssueEditorStep.SUBMITTED; }
    public boolean isCancelled()  { return step == IssueEditorStep.CANCELLED; }

    public static IssueEditorView listArticles(List<Article> articles) {
        StringBuilder prompt = new StringBuilder(
                "Wähle Artikel für die Ausgabe (Nummern kommasepariert, z.B. '1,3' oder 'alle'):\n");
        if (articles == null || articles.isEmpty()) {
            prompt.append("(Keine veröffentlichten Artikel verfügbar)");
        } else {
            for (int i = 0; i < articles.size(); i++) {
                Article a = articles.get(i);
                prompt.append(i + 1).append(". [").append(a.categoryId()).append("] ").append(a.title()).append("\n");
            }
        }
        return new IssueEditorView(
                IssueEditorStep.SELECT_ARTICLES, prompt.toString().trim(),
                List.of(), null, null, null, null
        );
    }

    public static IssueEditorView chooseMainArticle(List<Article> selected) {
        StringBuilder prompt = new StringBuilder(
                "Welcher Artikel soll der Titelartikel sein? (Nummer eingeben oder 'weiter' für den ersten):\n");
        for (int i = 0; i < selected.size(); i++) {
            prompt.append(i + 1).append(". ").append(selected.get(i).title()).append("\n");
        }
        List<String> ids = selected.stream().map(Article::id).toList();
        return new IssueEditorView(
                IssueEditorStep.CHOOSE_MAIN_ARTICLE, prompt.toString().trim(),
                ids, null, null, null, null
        );
    }

    public static IssueEditorView enterSubtitle(List<String> articleIds, String mainArticleId) {
        return new IssueEditorView(
                IssueEditorStep.ENTER_SUBTITLE,
                "Gib einen Untertitel für die Ausgabe ein (oder 'weiter' zum Überspringen):",
                articleIds, mainArticleId, null, null, null
        );
    }

    public static IssueEditorView review(IssueEditorSession session) {
        StringBuilder prompt = new StringBuilder(
                "Ausgabe prüfen. Tippe 'einreichen' zum Speichern oder 'abbrechen':\n");
        prompt.append("Artikel: ").append(String.join(", ", session.selectedArticleIds())).append("\n");
        String main = session.mainArticleId() != null ? session.mainArticleId() : "(erster Artikel)";
        prompt.append("Titelartikel: ").append(main).append("\n");
        String sub = session.subtitle() != null && !session.subtitle().isBlank()
                ? session.subtitle() : "(kein Untertitel)";
        prompt.append("Untertitel: ").append(sub);
        return new IssueEditorView(
                IssueEditorStep.REVIEW, prompt.toString(),
                session.selectedArticleIds(), session.mainArticleId(), session.subtitle(),
                null, null
        );
    }

    public static IssueEditorView submitted(String draftId) {
        return new IssueEditorView(
                IssueEditorStep.SUBMITTED, null, List.of(), null, null,
                "Ausgabe gespeichert als Entwurf: " + draftId + ". Ein Admin kann sie veröffentlichen.",
                draftId
        );
    }

    public static IssueEditorView cancelled() {
        return new IssueEditorView(
                IssueEditorStep.CANCELLED, null, List.of(), null, null,
                "Ausgabe-Erstellung abgebrochen.", null
        );
    }

    public static IssueEditorView error(IssueEditorStep step, String errorMessage, IssueEditorView previous) {
        return new IssueEditorView(
                step,
                previous.prompt(),
                previous.selectedArticleIds(),
                previous.mainArticleId(),
                previous.subtitle(),
                "Fehler: " + errorMessage,
                null
        );
    }
}
