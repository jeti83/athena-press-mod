package pro.jeti.athenapress.integration;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class NewspaperArticleTextFlowServiceTest {

    @Test
    void keepsShortParagraphsAsNaturalSegments() {
        NewspaperArticleTextFlowService service = new NewspaperArticleTextFlowService(80);

        List<String> segments = service.split("""
                Erster Absatz.

                Zweiter Absatz mit etwas mehr Inhalt.
                """);

        assertEquals(List.of(
                "Erster Absatz.",
                "Zweiter Absatz mit etwas mehr Inhalt."
        ), segments);
    }

    @Test
    void splitsLongParagraphAtSentenceBoundaryWhenPossible() {
        NewspaperArticleTextFlowService service = new NewspaperArticleTextFlowService(60);

        List<String> segments = service.split(
                "Erster langer Satz mit ausreichend Text. "
                        + "Zweiter langer Satz mit ebenfalls ausreichend Text."
        );

        assertEquals(2, segments.size());
        assertEquals("Erster langer Satz mit ausreichend Text.", segments.getFirst());
    }

    @Test
    void splitsVeryLongSentenceAtWordBoundary() {
        NewspaperArticleTextFlowService service = new NewspaperArticleTextFlowService(20);

        List<String> segments = service.split(
                "eins zwei drei vier fuenf sechs sieben acht"
        );

        assertTrue(segments.size() > 1);
        assertTrue(segments.stream().allMatch(segment -> segment.length() <= 20));
    }
}
