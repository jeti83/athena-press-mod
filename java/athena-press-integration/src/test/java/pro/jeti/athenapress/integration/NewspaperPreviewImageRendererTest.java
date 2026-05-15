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

    @Test
    void rendersAdvertisementAssetsIntoPreviewPngs() throws IOException {
        Path imagePath = tempDir.resolve("images").resolve("ad.png");
        Files.createDirectories(imagePath.getParent());
        BufferedImage adImage = new BufferedImage(40, 40, BufferedImage.TYPE_INT_RGB);
        adImage.setRGB(20, 20, java.awt.Color.RED.getRGB());
        ImageIO.write(adImage, "png", imagePath.toFile());

        NewspaperPreviewIssue issue = new NewspaperPreviewIssue(
                "issue_ad",
                "Athena Anzeigenblatt",
                NewspaperVisualTheme.defaultTheme(),
                NewspaperVisualDesignProfile.athenaReadableNewspaper(),
                List.of(new NewspaperPreviewSpread(
                        0,
                        new NewspaperPreviewPage(
                                1,
                                "Rückseite",
                                NewspaperPageRole.BACK_COVER,
                                NewspaperVisualDesignProfile.athenaReadableNewspaper(),
                                List.of(new NewspaperPreviewBlock(
                                        NewspaperVisualBlockType.ADVERTISEMENT,
                                        "Marktstand",
                                        "ad.png",
                                        0,
                                        0,
                                        8,
                                        1
                                ))
                        ),
                        null,
                        List.of()
                ))
        );

        NewspaperPreviewImageRenderResult result = new NewspaperPreviewImageRenderer().render(
                issue,
                tempDir.resolve("images"),
                tempDir.resolve("out")
        );

        BufferedImage image = ImageIO.read(result.spreadImages().getFirst().toFile());
        boolean containsRedPixel = false;
        for (int x = 0; x < image.getWidth() && !containsRedPixel; x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                java.awt.Color color = new java.awt.Color(image.getRGB(x, y), true);
                if (color.getRed() > 180 && color.getGreen() < 90 && color.getBlue() < 90) {
                    containsRedPixel = true;
                    break;
                }
            }
        }

        assertTrue(containsRedPixel);
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
