package pro.jeti.athenapress.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class NewspaperVisualDesignProfileTest {

    @Test
    void createsReadableAthenaNewspaperDefaults() {
        NewspaperVisualDesignProfile profile =
                NewspaperVisualDesignProfile.athenaReadableNewspaper();

        assertEquals(NewspaperLayoutMood.LOOSE_COMMUNITY_SHEET, profile.layoutMood());
        assertEquals(NewspaperPageCornerStyle.SUBTLE_TOP_FOLDS, profile.cornerStyle());
        assertEquals(NewspaperCoverPolicy.STANDALONE_TITLE_PAGE, profile.coverPolicy());
        assertEquals(
                NewspaperArticleFlowPolicy.KEEP_ARTICLES_TOGETHER_WHEN_READABLE,
                profile.articleFlowPolicy()
        );
        assertEquals(
                NewspaperNavigationStyle.PAGE_TURNING_WITH_SUBTLE_MENU,
                profile.navigationStyle()
        );
        assertEquals(3, profile.preferredColumns());
        assertEquals(4, profile.maximumColumns());
        assertTrue(profile.allowAdvertisementBlocks());
        assertTrue(profile.allowDocumentStyleBlocks());
        assertTrue(profile.keepReadablePageBody());
    }

    @Test
    void convertsDesignProfileIntoLayoutTemplate() {
        NewspaperVisualDesignProfile profile =
                NewspaperVisualDesignProfile.athenaReadableNewspaper();

        NewspaperLayoutTemplate template = profile.toLayoutTemplate();

        assertEquals("athena_readable_newspaper", template.templateId());
        assertEquals(3, template.columnsPerPage());
        assertEquals(24, template.rowsPerPage());
    }

    @Test
    void keepsAsymmetryWithinReadableBounds() {
        NewspaperVisualDesignProfile profile = new NewspaperVisualDesignProfile(
                "wild",
                NewspaperLayoutMood.LOOSE_COMMUNITY_SHEET,
                NewspaperPageCornerStyle.HANGING_TOP_CORNERS,
                4,
                3,
                250,
                NewspaperCoverPolicy.STANDALONE_TITLE_PAGE,
                NewspaperArticleFlowPolicy.KEEP_ARTICLES_TOGETHER_WHEN_READABLE,
                NewspaperNavigationStyle.PAGE_TURNING_WITH_SUBTLE_MENU,
                true,
                true,
                true
        );

        assertEquals(4, profile.maximumColumns());
        assertEquals(100, profile.asymmetryPercent());
        assertTrue(profile.allowsLooseComposition());
    }
}
