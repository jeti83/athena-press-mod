package pro.jeti.athenapress.integration;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class NewspaperPageSectionPolicyTest {

    @Test
    void includesRequiredSectionsEvenWithoutBlocks() {
        NewspaperPageSectionPolicy policy = NewspaperPageSectionPolicy.defaultPolicy();
        NewspaperPageSection section = NewspaperPageSection.of(
                NewspaperPageSectionType.TITLE_PAGE,
                "Titel",
                List.of()
        );

        assertTrue(policy.shouldInclude(section));
    }

    @Test
    void skipsOptionalSectionsWithoutContent() {
        NewspaperPageSectionPolicy policy = NewspaperPageSectionPolicy.defaultPolicy();
        NewspaperPageSection section = NewspaperPageSection.of(
                NewspaperPageSectionType.ADVERTISEMENTS,
                "Anzeigen",
                List.of()
        );

        assertFalse(policy.shouldInclude(section));
    }

    @Test
    void canDisableSectionsExplicitly() {
        NewspaperPageSectionPolicy policy = new NewspaperPageSectionPolicy(Map.of(
                NewspaperPageSectionType.BACK_PAGE,
                NewspaperSectionRequirement.DISABLED
        ));
        NewspaperPageSection section = NewspaperPageSection.of(
                NewspaperPageSectionType.BACK_PAGE,
                "Rueckseite",
                List.of(NewspaperVisualBlock.notice("Impressum"))
        );

        assertFalse(policy.shouldInclude(section));
    }

    @Test
    void filtersOnlyNeededSections() {
        NewspaperPageSectionPolicy policy = NewspaperPageSectionPolicy.defaultPolicy();
        List<NewspaperPageSection> filtered = policy.filter(List.of(
                NewspaperPageSection.of(
                        NewspaperPageSectionType.TITLE_PAGE,
                        "Titel",
                        List.of()
                ),
                NewspaperPageSection.of(
                        NewspaperPageSectionType.ADVERTISEMENTS,
                        "Anzeigen",
                        List.of()
                ),
                NewspaperPageSection.of(
                        NewspaperPageSectionType.MIXED_ARTICLES,
                        "Artikel",
                        List.of(NewspaperVisualBlock.bodyText("Lesestoff"))
                )
        ));

        assertEquals(2, filtered.size());
        assertEquals(NewspaperPageSectionType.TITLE_PAGE, filtered.get(0).type());
        assertEquals(NewspaperPageSectionType.MIXED_ARTICLES, filtered.get(1).type());
    }
}
