package pro.jeti.athenapress.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import pro.jeti.athenapress.model.GameArticleView;

class NewspaperArticleClassifierTest {

    private final NewspaperArticleClassifier classifier = new NewspaperArticleClassifier();

    @Test
    void keepsCoverArticleAsMainArticle() {
        NewspaperArticleClassification classification = classifier.classify(
                article("server_news", "Rathaus brennt", "Langer Text"),
                true
        );

        assertEquals(NewspaperPageSectionType.MAIN_ARTICLE, classification.sectionType());
        assertTrue(classification.mainArticle());
    }

    @Test
    void detectsAdvertisementsByCategory() {
        NewspaperArticleClassification classification = classifier.classify(
                article("anzeigen", "Kaufe zehn Steine", "Sonderangebot"),
                false
        );

        assertEquals(NewspaperPageSectionType.ADVERTISEMENTS, classification.sectionType());
    }

    @Test
    void detectsClassifiedsAsAdvertisements() {
        NewspaperArticleClassification classification = classifier.classify(
                article("classifieds", "Suche Brett", "Kurzer Text"),
                false
        );

        assertEquals(NewspaperPageSectionType.ADVERTISEMENTS, classification.sectionType());
    }

    @Test
    void detectsShortNoticesByCategoryOrLength() {
        NewspaperArticleClassification byCategory = classifier.classify(
                article("kurzmeldung", "Heute Kuchen", "Kurz"),
                false
        );
        NewspaperArticleClassification byLength = classifier.classify(
                article("server_news", "Kurzer Ruf", "Knapp."),
                false
        );

        assertEquals(NewspaperPageSectionType.SHORT_NOTICES, byCategory.sectionType());
        assertEquals(NewspaperPageSectionType.SHORT_NOTICES, byLength.sectionType());
        assertTrue(byLength.shortNotice());
    }

    @Test
    void detectsMemorialArticlesByText() {
        NewspaperArticleClassification classification = classifier.classify(
                article("story", "Ein Spieler ist verschollen", "Wir gedenken der letzten Sichtung."),
                false
        );

        assertEquals(NewspaperPageSectionType.MEMORIAL, classification.sectionType());
        assertTrue(classification.specialTone());
    }

    @Test
    void fallsBackToMixedArticles() {
        NewspaperArticleClassification classification = classifier.classify(
                article(
                        "server_news",
                        "Ein grosser Bau",
                        "Dies ist ein ausfuehrlicher Artikel mit ausreichend Text, der keine Kurzmeldung ist. "
                                + "Er beschreibt mehrere Beobachtungen, zitiert Beteiligte und bietet genug Laenge, "
                                + "damit die Klassifizierung ihn als normalen Mischartikel behandeln kann."
                ),
                false
        );

        assertEquals(NewspaperPageSectionType.MIXED_ARTICLES, classification.sectionType());
        assertFalse(classification.mainArticle());
    }

    private GameArticleView article(
            String categoryId,
            String title,
            String body
    ) {
        return new GameArticleView(
                "article_test",
                categoryId,
                title,
                null,
                null,
                body,
                body
        );
    }
}
