package pro.jeti.athenapress.integration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AthenaPressVisualPreviewDemoTest {

    @TempDir
    Path tempDir;

    @Test
    void parseWithoutArgumentsUsesDefaultIssue() {
        AthenaPressVisualPreviewDemo.VisualPreviewCommand command =
                AthenaPressVisualPreviewDemo.parse(new String[0]);

        assertFalse(command.showHelp());
        assertEquals(AthenaPressVisualPreviewDemo.DEFAULT_ISSUE_ID, command.issueId());
    }

    @Test
    void parseIssueIdCreatesPreviewCommand() {
        AthenaPressVisualPreviewDemo.VisualPreviewCommand command =
                AthenaPressVisualPreviewDemo.parse(new String[]{"issue_custom"});

        assertFalse(command.showHelp());
        assertEquals("issue_custom", command.issueId());
    }

    @Test
    void parsePreviewAliasCreatesPreviewCommand() {
        AthenaPressVisualPreviewDemo.VisualPreviewCommand command =
                AthenaPressVisualPreviewDemo.parse(new String[]{"--vorschau", "issue_custom"});

        assertFalse(command.showHelp());
        assertEquals("issue_custom", command.issueId());
    }

    @Test
    void parseHelpCreatesHelpCommand() {
        AthenaPressVisualPreviewDemo.VisualPreviewCommand command =
                AthenaPressVisualPreviewDemo.parse(new String[]{"--hilfe"});

        assertTrue(command.showHelp());
    }

    @Test
    void helpTextMentionsVisualPreview() {
        String text = AthenaPressVisualPreviewDemo.createHelpText();

        assertTrue(text.contains("AthenaPress Visual Preview"));
        assertTrue(text.contains("--visual-preview"));
        assertTrue(text.contains("--vorschau"));
    }

    @Test
    void rendersVisualPreviewFromDataRoot() throws IOException {
        createPreviewDataSet();

        String text = AthenaPressVisualPreviewDemo.render(
                tempDir,
                new String[]{"--visual-preview", "issue_demo"}
        );

        assertTrue(text.contains("Athena Marktblatt Preview"));
        assertTrue(text.contains("Doppelseite 1"));
        assertTrue(text.contains("FRONT_COVER"));
        assertTrue(text.contains("Der Kronleuchter bekommt Hausrecht"));
    }

    private void createPreviewDataSet() throws IOException {
        createFolders();

        Files.writeString(
                tempDir.resolve("articles").resolve("published").resolve("article_demo.json"),
                """
                {
                  "id": "article_demo",
                  "status": "published",
                  "categoryId": "stadtklatsch",
                  "title": "Der Kronleuchter bekommt Hausrecht",
                  "subtitle": "Ein glanzvoller Zwischenfall",
                  "teaser": "Niemand wollte widersprechen.",
                  "summary": "Der Kronleuchter wurde einstimmig zum stillen Beobachter ernannt.",
                  "body": "Der Kronleuchter bekommt Hausrecht und wacht fortan mit angemessener Dramatik über alle Sitzgelegenheiten."
                }
                """
        );

        Files.writeString(
                tempDir.resolve("issues").resolve("published").resolve("issue_demo.json"),
                """
                {
                  "id": "issue_demo",
                  "status": "published",
                  "issueNumber": 3,
                  "title": "Athena Marktblatt",
                  "subtitle": "Serioes gedruckt, frech gedacht",
                  "articles": [
                    "article_demo"
                  ],
                  "cover": {
                    "mainArticleId": "article_demo",
                    "image": "placeholders/chandelier.png"
                  }
                }
                """
        );
    }

    private void createFolders() throws IOException {
        Files.createDirectories(tempDir.resolve("articles").resolve("draft"));
        Files.createDirectories(tempDir.resolve("articles").resolve("published"));
        Files.createDirectories(tempDir.resolve("articles").resolve("archived"));

        Files.createDirectories(tempDir.resolve("issues").resolve("draft"));
        Files.createDirectories(tempDir.resolve("issues").resolve("published"));
        Files.createDirectories(tempDir.resolve("issues").resolve("archived"));
    }
}
