package pro.jeti.athenapress.service;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import pro.jeti.athenapress.model.GameArticleView;
import pro.jeti.athenapress.model.GameIssueView;

class GameTextRendererServiceTest {

    private final GameTextRendererService service = new GameTextRendererService();

    @Test
    void createsReadableOverviewText() {
        GameIssueView issueView = createIssueView();

        String text = service.createOverviewText(issueView);

        assertTrue(text.contains("Ausgabe #2"));
        assertTrue(text.contains("Athena Botenblatt"));
        assertTrue(text.contains("Die erste echte Testausgabe"));
        assertTrue(text.contains("[1] Neue Baumfarm eröffnet"));
        assertTrue(text.contains("Vier Plots gegen die Holzknappheit"));
        assertTrue(text.contains("[2] Marktplatz bekommt neue Regeln"));
        assertTrue(text.contains("Wähle einen Artikel, um ihn zu lesen."));
    }

    @Test
    void createsReadableArticleTextByNumber() {
        GameIssueView issueView = createIssueView();

        String text = service.createArticleText(issueView, 1);

        assertTrue(text.contains("Athena Botenblatt"));
        assertTrue(text.contains("Neue Baumfarm eröffnet"));
        assertTrue(text.contains("Vier Plots gegen die Holzknappheit"));
        assertTrue(text.contains("Die neue Baumfarm ist eröffnet und kann ab sofort genutzt werden."));
    }

    @Test
    void createsReadableArticleTextById() {
        GameIssueView issueView = createIssueView();

        String text = service.createArticleText(issueView, "article_market");

        assertTrue(text.contains("Marktplatz bekommt neue Regeln"));
        assertTrue(text.contains("Neue Regeln sollen den Handel übersichtlicher machen."));
    }

    @Test
    void returnsHelpfulTextForMissingIssue() {
        String text = service.createOverviewText(null);

        assertTrue(text.contains("Diese Ausgabe ist nicht verfügbar."));
    }

    @Test
    void returnsHelpfulTextForMissingArticleNumber() {
        GameIssueView issueView = createIssueView();

        String text = service.createArticleText(issueView, 99);

        assertTrue(text.contains("Dieser Artikel ist in der Ausgabe nicht vorhanden."));
    }

    @Test
    void returnsHelpfulTextForMissingArticleId() {
        GameIssueView issueView = createIssueView();

        String text = service.createArticleText(issueView, "does_not_exist");

        assertTrue(text.contains("Dieser Artikel ist in der Ausgabe nicht vorhanden."));
    }

    private GameIssueView createIssueView() {
        return new GameIssueView(
                "issue_0002",
                2,
                "Athena Botenblatt",
                "Die erste echte Testausgabe",
                "article_tree_farm",
                "placeholders/no_image.png",
                List.of(
                        new GameArticleView(
                                "article_tree_farm",
                                "build_projects",
                                "Neue Baumfarm eröffnet",
                                "Vier Plots gegen die Holzknappheit",
                                "Kurzer Teaser zur Baumfarm",
                                "Vier Plots gegen die Holzknappheit",
                                "Die neue Baumfarm ist eröffnet und kann ab sofort genutzt werden."
                        ),
                        new GameArticleView(
                                "article_market",
                                "server_news",
                                "Marktplatz bekommt neue Regeln",
                                "Mehr Ordnung am Spawn",
                                "Kurzer Teaser zum Marktplatz",
                                "Neue Regeln sollen den Handel übersichtlicher machen.",
                                "Neue Regeln sollen den Handel übersichtlicher machen."
                        )
                )
        );
    }
}