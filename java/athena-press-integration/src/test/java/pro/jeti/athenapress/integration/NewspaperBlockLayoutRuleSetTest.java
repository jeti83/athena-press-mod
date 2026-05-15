package pro.jeti.athenapress.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class NewspaperBlockLayoutRuleSetTest {

    @Test
    void makesHeadlinesAndDividersFullWidth() {
        NewspaperBlockLayoutRuleSet rules = NewspaperBlockLayoutRuleSet.defaultRules();
        NewspaperLayoutTemplate template = NewspaperLayoutTemplate.classicDoublePage();

        assertEquals(2, rules.columnSpanFor(NewspaperVisualBlock.headline("Titel"), template));
        assertEquals(2, rules.columnSpanFor(NewspaperVisualBlock.divider(), template));
    }

    @Test
    void increasesRowSpanForFeaturedImages() {
        NewspaperBlockLayoutRuleSet rules = NewspaperBlockLayoutRuleSet.defaultRules();
        NewspaperLayoutTemplate template = NewspaperLayoutTemplate.classicDoublePage();

        NewspaperVisualBlock inlineImage = NewspaperVisualBlock.image(
                "placeholders/inline.png",
                "Inline"
        );
        NewspaperVisualBlock featuredImage = NewspaperVisualBlock.image(
                "placeholders/featured.png",
                "Featured",
                template.columnsPerPage()
        );

        assertEquals(8, rules.rowSpanFor(inlineImage, template));
        assertEquals(12, rules.rowSpanFor(featuredImage, template));
        assertEquals(24, rules.weightFor(featuredImage, template));
        assertEquals(NewspaperImageRole.ARTICLE, inlineImage.imageRole());
    }

    @Test
    void respectsRequestedBodyTextColumnSpan() {
        NewspaperBlockLayoutRuleSet rules = NewspaperBlockLayoutRuleSet.defaultRules();
        NewspaperLayoutTemplate template = NewspaperLayoutTemplate.classicDoublePage();

        NewspaperVisualBlock featuredBody = NewspaperVisualBlock.bodyText(
                "Langer Hauptartikel",
                template.columnsPerPage()
        );

        assertEquals(2, rules.columnSpanFor(featuredBody, template));
        assertEquals(8, rules.rowSpanFor(featuredBody, template));
    }

    @Test
    void keepsCaptionAlignedWithRequestedImageWidth() {
        NewspaperBlockLayoutRuleSet rules = NewspaperBlockLayoutRuleSet.defaultRules();
        NewspaperLayoutTemplate template = NewspaperLayoutTemplate.classicDoublePage();

        NewspaperVisualBlock featuredCaption = NewspaperVisualBlock.caption(
                "Bildunterschrift",
                template.columnsPerPage()
        );

        assertEquals(2, rules.columnSpanFor(featuredCaption, template));
        assertEquals(1, rules.rowSpanFor(featuredCaption, template));
    }

    @Test
    void respectsRequestedNoticeColumnSpan() {
        NewspaperBlockLayoutRuleSet rules = NewspaperBlockLayoutRuleSet.defaultRules();
        NewspaperLayoutTemplate template = NewspaperLayoutTemplate.classicDoublePage();

        NewspaperVisualBlock featuredNotice = NewspaperVisualBlock.notice(
                "Aufmacher",
                template.columnsPerPage()
        );

        assertEquals(2, rules.columnSpanFor(featuredNotice, template));
        assertEquals(4, rules.rowSpanFor(featuredNotice, template));
    }

    @Test
    void compactsCoverBlocksToFitAStandaloneTitlePage() {
        NewspaperBlockLayoutRuleSet rules = NewspaperBlockLayoutRuleSet.defaultRules();
        NewspaperLayoutTemplate template = NewspaperLayoutTemplate.classicDoublePage();

        assertEquals(4, rules.rowSpanFor(NewspaperVisualBlock.coverHeadline("Titel"), template));
        assertEquals(2, rules.rowSpanFor(NewspaperVisualBlock.coverSubheadline("Untertitel"), template));
        assertEquals(3, rules.rowSpanFor(NewspaperVisualBlock.coverNotice("Aufmacher", 2), template));
        assertEquals(10, rules.rowSpanFor(
                NewspaperVisualBlock.coverImage("cover.png", "Titel", 2),
                template
        ));
        assertEquals(1, rules.rowSpanFor(NewspaperVisualBlock.coverCaption("Bildtext", 2), template));
    }

    @Test
    void expandsBackPageAdvertisementsWithoutChangingRegularAds() {
        NewspaperBlockLayoutRuleSet rules = NewspaperBlockLayoutRuleSet.defaultRules();
        NewspaperLayoutTemplate template = NewspaperLayoutTemplate.classicDoublePage();

        assertEquals(6, rules.rowSpanFor(
                NewspaperVisualBlock.advertisement("Standard", "ad.png"),
                template
        ));
        assertEquals(8, rules.rowSpanFor(
                NewspaperVisualBlock.backPageAdvertisement("Rückseite", "ad.png"),
                template
        ));
        assertEquals(2, rules.rowSpanFor(
                NewspaperVisualBlock.backPageNotice("Hinweis"),
                template
        ));
    }
}
