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
    void preservesEditorialLayoutIntentInPreviewBlocks() {
        NewspaperVisualIssue visualIssue = new NewspaperVisualIssue(
                "issue_editorial",
                "Athena Editorial",
                NewspaperVisualTheme.defaultTheme(),
                List.of(NewspaperVisualPage.of(
                        1,
                        "Kurzmeldungen",
                        List.of(NewspaperVisualBlock.shortNotice("Kurze Nachricht"))
                ))
        );

        NewspaperPreviewBlock block = new NewspaperPreviewService()
                .createPreview(visualIssue)
                .spreads()
                .getFirst()
                .leftPage()
                .blocks()
                .getFirst();

        assertEquals(NewspaperBlockLayoutIntent.SHORT_NOTICE, block.layoutIntent());
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
        assertTrue(text.contains("intent=STANDARD"));
    }

    @Test
    void handlesMissingIssue() {
        NewspaperPreviewIssue previewIssue = new NewspaperPreviewService()
                .createPreview(null);

        assertNotNull(previewIssue);
        assertFalse(previewIssue.hasSpreads());
    }

    @Test
    void namesBackCoverSpreadsAfterTheBackPage() {
        NewspaperPreviewSpread spread = new NewspaperPreviewSpread(
                2,
                new NewspaperPreviewPage(
                        4,
                        "Innenseite",
                        NewspaperPageRole.LEFT_INNER,
                        NewspaperVisualDesignProfile.athenaReadableNewspaper(),
                        List.of(new NewspaperPreviewBlock(
                                NewspaperVisualBlockType.SUBHEADLINE,
                                "Baumfarm",
                                null,
                                0,
                                0,
                                2,
                                1
                        ))
                ),
                new NewspaperPreviewPage(
                        5,
                        "Rückseite",
                        NewspaperPageRole.BACK_COVER,
                        NewspaperVisualDesignProfile.athenaReadableNewspaper(),
                        List.of(new NewspaperPreviewBlock(
                                NewspaperVisualBlockType.SUBHEADLINE,
                                "Kleinanzeigen",
                                null,
                                0,
                                0,
                                2,
                                2
                        ))
                ),
                List.of()
        );

        NewspaperSpreadSignature signature = spread.signature();

        assertEquals("Rückseite", signature.label());
        assertEquals("Kleinanzeigen", signature.hint());
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
