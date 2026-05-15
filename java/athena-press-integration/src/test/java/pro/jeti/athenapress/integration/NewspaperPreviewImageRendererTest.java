package pro.jeti.athenapress.integration;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import javax.imageio.ImageIO;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class NewspaperPreviewImageRendererTest {

    @TempDir
    Path tempDir;

    @Test
    void rendersSpreadPreviewPngs() throws IOException {
        NewspaperPreviewIssue issue = new NewspaperPreviewIssue(
                "issue_render",
                "Athena Sichtblatt",
                NewspaperVisualTheme.defaultTheme(),
                NewspaperVisualDesignProfile.athenaReadableNewspaper(),
                List.of(new NewspaperPreviewSpread(
                        0,
                        page(1, NewspaperPageRole.FRONT_COVER),
                        page(2, NewspaperPageRole.RIGHT_INNER),
                        List.of()
                ))
        );

        NewspaperPreviewImageRenderResult result = new NewspaperPreviewImageRenderer().render(
                issue,
                tempDir.resolve("images"),
                tempDir.resolve("out")
        );

        assertTrue(result.hasImages());
        assertEquals(1, result.spreadImages().size());
        assertTrue(Files.isRegularFile(result.spreadImages().getFirst()));

        BufferedImage image = ImageIO.read(result.spreadImages().getFirst().toFile());
        assertTrue(image.getWidth() > image.getHeight());
    }

    private NewspaperPreviewPage page(int pageNumber, NewspaperPageRole role) {
        return new NewspaperPreviewPage(
                pageNumber,
                "Seite " + pageNumber,
                role,
                NewspaperVisualDesignProfile.athenaReadableNewspaper(),
                List.of(
                        new NewspaperPreviewBlock(
                                NewspaperVisualBlockType.HEADLINE,
                                "AthenaPress",
                                null,
                                0,
                                0,
                                4,
                                2
                        ),
                        new NewspaperPreviewBlock(
                                NewspaperVisualBlockType.BODY_TEXT,
                                "Dies ist eine sichtbare Vorschauseite.",
                                null,
                                0,
                                5,
                                5,
                                1
                        )
                )
        );
    }
}
