package pro.jeti.athenapress.service;

import pro.jeti.athenapress.model.GameArticleView;
import pro.jeti.athenapress.model.GameIssueView;

public class GameTextRendererService {

    public String createOverviewText(GameIssueView issueView) {
        if (issueView == null) {
            return "Diese Ausgabe ist nicht verfügbar.\n";
        }

        StringBuilder text = new StringBuilder();

        appendIssueHeader(text, issueView);

        if (issueView.articles().isEmpty()) {
            text.append("Diese Ausgabe enthält noch keine lesbaren Artikel.\n");
            return text.toString();
        }

        text.append("Artikel\n");
        text.append("----------------------------------------\n");

        for (int index = 0; index < issueView.articles().size(); index++) {
            GameArticleView article = issueView.articles().get(index);

            text.append("[")
                    .append(index + 1)
                    .append("] ")
                    .append(safeText(article.title()))
                    .append("\n");

            String summary = firstNonBlank(
                    article.summary(),
                    article.teaser(),
                    article.subtitle()
            );

            if (summary != null) {
                text.append("    ")
                        .append(summary)
                        .append("\n");
            }

            text.append("\n");
        }

        text.append("Wähle einen Artikel, um ihn zu lesen.\n");

        return text.toString();
    }

    public String createArticleText(GameIssueView issueView, int articleNumber) {
        if (issueView == null) {
            return "Diese Ausgabe ist nicht verfügbar.\n";
        }

        if (articleNumber < 1 || articleNumber > issueView.articles().size()) {
            return "Dieser Artikel ist in der Ausgabe nicht vorhanden.\n";
        }

        return createArticleText(issueView, issueView.articles().get(articleNumber - 1));
    }

    public String createArticleText(GameIssueView issueView, String articleId) {
        if (issueView == null) {
            return "Diese Ausgabe ist nicht verfügbar.\n";
        }

        GameArticleView article = issueView.findArticleById(articleId);

        if (article == null) {
            return "Dieser Artikel ist in der Ausgabe nicht vorhanden.\n";
        }

        return createArticleText(issueView, article);
    }

    private String createArticleText(GameIssueView issueView, GameArticleView article) {
        StringBuilder text = new StringBuilder();

        appendIssueHeader(text, issueView);

        text.append(safeText(article.title()))
                .append("\n");
        text.append("----------------------------------------\n");

        if (article.subtitle() != null && !article.subtitle().isBlank()) {
            text.append(article.subtitle())
                    .append("\n\n");
        }

        if (article.body() == null || article.body().isBlank()) {
            text.append("Dieser Artikel hat noch keinen lesbaren Text.\n");
            return text.toString();
        }

        text.append(article.body())
                .append("\n");

        return text.toString();
    }

    private void appendIssueHeader(StringBuilder text, GameIssueView issueView) {
        if (issueView.issueNumber() != null) {
            text.append("Ausgabe #")
                    .append(issueView.issueNumber())
                    .append("\n");
        }

        text.append(safeText(issueView.title()))
                .append("\n");

        if (issueView.subtitle() != null && !issueView.subtitle().isBlank()) {
            text.append(issueView.subtitle())
                    .append("\n");
        }

        text.append("----------------------------------------\n");
    }

    private String firstNonBlank(String first, String second, String third) {
        if (first != null && !first.isBlank()) {
            return first;
        }

        if (second != null && !second.isBlank()) {
            return second;
        }

        if (third != null && !third.isBlank()) {
            return third;
        }

        return null;
    }

    private String safeText(String value) {
        if (value == null || value.isBlank()) {
            return "(leer)";
        }

        return value;
    }
}