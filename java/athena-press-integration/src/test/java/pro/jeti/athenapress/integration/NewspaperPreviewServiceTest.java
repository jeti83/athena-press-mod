package pro.jeti.athenapress.integration;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class NewspaperPreviewServiceTest {

    @Test
    void createsPreviewFromVisualIssue() {
        NewspaperVisualIssue visualIssue = visualIssue();

        NewspaperPreviewIssue previewIssue = new NewspaperPreviewService()
                .createPreview(visualIssue);

        assertEquals("issue_preview", previewIssue.issueId());
        assertEquals("Athena Preview", previewIssue.title());
        assertTrue(previewIssue.hasSpreads());
        assertEquals(NewspaperPageRole.FRONT_COVER, previewIssue.spreads().getFirst().leftPage().role());
        assertTrue(previewIssue.spreads().getFirst().leftPage().hasBlocks());
    }

    @Test
    void preservesPlacementInformationInPreviewBlocks() {
        NewspaperPreviewIssue previewIssue = new NewspaperPreviewService()
                .createPreview(visualIssue());

        NewspaperPreviewBlock firstBlock = previewIssue.spreads()
                .getFirst()
                .leftPage()
                .blocks()
                .getFirst();

        assertEquals(NewspaperVisualBlockType.HEADLINE, firstBlock.type());
        assertEquals(0, firstBlock.columnIndex());
        assertEquals(2, firstBlock.columnSpan());
        assertFalse(firstBlock.content().isBlank());
    }

    @Test
    void rendersReadableTextPreview() {
        NewspaperPreviewIssue previewIssue = new NewspaperPreviewService()
                .createPreview(visualIssue());

        String text = new NewspaperPreviewTextRenderer().render(previewIssue);

        assertTrue(text.contains("Athena Preview Preview"));
        assertTrue(text.contains("Doppelseite 1"));
        assertTrue(text.contains("FRONT_COVER"));
        assertTrue(text.contains("HEADLINE"));
    }

    @Test
    void handlesMissingIssue() {
        NewspaperPreviewIssue previewIssue = new NewspaperPreviewService()
                .createPreview(null);

        assertNotNull(previewIssue);
        assertFalse(previewIssue.hasSpreads());
    }

    private NewspaperVisualIssue visualIssue() {
        return new NewspaperVisualIssue(
                "issue_preview",
                "Athena Preview",
                NewspaperVisualTheme.defaultTheme(),
                List.of(
                        NewspaperVisualPage.of(
                                1,
                                "Athena Preview",
                                List.of(
                                        NewspaperVisualBlock.headline("Athena Preview"),
                                        NewspaperVisualBlock.image("placeholders/front.png", "Titelbild", 2)
                                )
                        ),
                        NewspaperVisualPage.of(
                                2,
                                "Artikel",
                                List.of(
                                        NewspaperVisualBlock.subheadline("Die grosse Geschichte"),
                                        NewspaperVisualBlock.bodyText("Ein lesbarer Artikeltext.")
                                )
                        )
                )
        );
    }
}
