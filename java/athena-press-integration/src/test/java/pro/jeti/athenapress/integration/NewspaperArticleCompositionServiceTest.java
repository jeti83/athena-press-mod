package pro.jeti.athenapress.integration;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import pro.jeti.athenapress.model.GameArticleView;
import pro.jeti.athenapress.model.GameIssueView;

class NewspaperArticleCompositionServiceTest {

    @Test
    void composesIssueIntoVisualNewspaperPages() {
        GameIssueView issueView = issueViewWithArticles(1);

        NewspaperVisualIssue visualIssue = new NewspaperArticleCompositionService()
                .compose(issueView);

        assertEquals("issue_test", visualIssue.issueId());
        assertEquals("Athena Morgenblatt", visualIssue.title());
        assertFalse(visualIssue.pages().isEmpty());
        assertTrue(visualIssue.pages().getFirst().blocks().stream()
                .anyMatch(block -> block.type() == NewspaperVisualBlockType.HEADLINE));
        assertTrue(visualIssue.pages().getFirst().blocks().stream()
                .anyMatch(block -> "placeholders/front.png".equals(block.assetPath())));
        assertTrue(visualIssue.pages().getFirst().blocks().stream()
                .noneMatch(block -> block.type() == NewspaperVisualBlockType.QUOTE));
        assertTrue(visualIssue.pages().stream()
                .flatMap(page -> page.blocks().stream())
                .anyMatch(block -> block.type() == NewspaperVisualBlockType.QUOTE));
    }

    @Test
    void keepsStandaloneTitlePageBeforeArticlePages() {
        GameIssueView issueView = issueViewWithArticles(3);

        NewspaperVisualIssue visualIssue = new NewspaperArticleCompositionService()
                .compose(issueView);

        assertEquals(1, visualIssue.pages().getFirst().pageNumber());
        assertTrue(visualIssue.pages().getFirst().blocks().stream()
                .noneMatch(block -> block.type() == NewspaperVisualBlockType.QUOTE));
        assertTrue(visualIssue.pages().get(1).pageNumber() >= 2);
        assertTrue(visualIssue.pages().get(1).blocks().stream()
                .anyMatch(block -> "Zusammenfassung 1".equals(block.content())));
    }

    @Test
    void paginatesLongIssueAcrossMultiplePages() {
        GameIssueView issueView = issueViewWithArticles(20);

        NewspaperVisualIssue visualIssue = new NewspaperArticleCompositionService()
                .compose(issueView);

        assertTrue(visualIssue.pages().size() > 1);
        assertTrue(visualIssue.pages().getFirst().title().equals("Athena Morgenblatt"));
        assertTrue(visualIssue.pages().get(1).pageNumber() == 2);
        assertTrue(visualIssue.pages().get(2).title().contains("Seite 3"));
    }

    @Test
    void keepsReadableArticlesTogetherWhenTheyWouldOtherwiseSplit() {
        NewspaperArticleCompositionService service = new NewspaperArticleCompositionService(
                new NewspaperVisualPaginationService(),
                new NewspaperLayoutTemplate(
                        "compact_test",
                        "Kompakter Test",
                        100,
                        140,
                        6,
                        4,
                        2,
                        9
                )
        );
        GameIssueView issueView = issueViewWithoutMainArticle(2);

        NewspaperVisualIssue visualIssue = service.compose(issueView);

        assertTrue(visualIssue.pages().get(1).blocks().stream()
                .anyMatch(block -> "Artikel 1".equals(block.content())));
        assertTrue(visualIssue.pages().get(1).blocks().stream()
                .noneMatch(block -> "Artikel 2".equals(block.content())));
        assertTrue(visualIssue.pages().get(2).blocks().stream()
                .anyMatch(block -> "Artikel 2".equals(block.content())));
    }

    @Test
    void rendersVisualPagesIntoAdapterNeutralLayout() {
        GameIssueView issueView = issueViewWithArticles(1);
        NewspaperVisualIssue visualIssue = new NewspaperArticleCompositionService()
                .compose(issueView);

        List<NewspaperPageLayout> layouts = new NewspaperVisualRenderer()
                .render(visualIssue, NewspaperLayoutTemplate.classicDoublePage());

        NewspaperPageLayout firstLayout = layouts.getFirst();

        assertEquals(2, firstLayout.columns().size());
        assertFalse(firstLayout.placements().isEmpty());
        assertEquals(NewspaperVisualBlockType.HEADLINE, firstLayout.placements().getFirst().blockType());
        assertEquals(2, firstLayout.placements().getFirst().columnSpan());
        assertEquals("placeholders/front.png", firstLayout.imagePlacements().getFirst().assetPath());
        assertEquals(2, firstLayout.imagePlacements().getFirst().columnSpan());
    }

    private GameIssueView issueViewWithArticles(int articleCount) {
        List<GameArticleView> articles = java.util.stream.IntStream.rangeClosed(1, articleCount)
                .mapToObj(index -> new GameArticleView(
                        "article_" + index,
                        "server_news",
                        "Artikel " + index,
                        "Untertitel " + index,
                        "Teaser " + index,
                        "Zusammenfassung " + index,
                        "Dies ist ein langer lesbarer Artikeltext fuer Layouttests."
                ))
                .toList();

        return new GameIssueView(
                "issue_test",
                7,
                "Athena Morgenblatt",
                "Aus der Stadt, fuer die Stadt",
                "article_1",
                "placeholders/front.png",
                articles
        );
    }

    private GameIssueView issueViewWithoutMainArticle(int articleCount) {
        List<GameArticleView> articles = java.util.stream.IntStream.rangeClosed(1, articleCount)
                .mapToObj(index -> new GameArticleView(
                        "article_" + index,
                        "server_news",
                        "Artikel " + index,
                        "Untertitel " + index,
                        "Teaser " + index,
                        "Zusammenfassung " + index,
                        "Dies ist ein langer lesbarer Artikeltext fuer Layouttests."
                ))
                .toList();

        return new GameIssueView(
                "issue_test",
                7,
                "Athena Morgenblatt",
                "Aus der Stadt, fuer die Stadt",
                "missing_article",
                "placeholders/front.png",
                articles
        );
    }
}
