package pro.jeti.athenapress.integration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PlayerNewspaperVisualUiControllerTest {

    @TempDir
    Path tempDir;

    @Test
    void rejectsMissingPlugin() {
        CapturingVisualUiPort uiPort = new CapturingVisualUiPort();

        assertThrows(
                IllegalArgumentException.class,
                () -> new PlayerNewspaperVisualUiController(null, uiPort)
        );
    }

    @Test
    void rejectsMissingPort() {
        AthenaPressIntegrationPlugin plugin = new AthenaPressIntegrationPlugin(tempDir);

        assertThrows(
                IllegalArgumentException.class,
                () -> new PlayerNewspaperVisualUiController(plugin, null)
        );
    }

    @Test
    void opensVisualIssueAndShowsView() throws IOException {
        createVisualDataSet();
        CapturingVisualUiPort uiPort = new CapturingVisualUiPort();
        PlayerNewspaperVisualUiController controller = createController(uiPort);

        controller.openIssue("player-1", "issue_visual");

        assertNotNull(uiPort.lastView);
        assertTrue(uiPort.lastView.newspaperOpen());
        assertEquals("issue_visual", uiPort.lastView.issueId());
        assertTrue(uiPort.lastView.hasLeftPage());
    }

    @Test
    void handlesVisualNextCommand() throws IOException {
        CapturingVisualUiPort uiPort = new CapturingVisualUiPort();
        PlayerNewspaperVisualUiController controller = createController(
                new StubVisualPlugin(),
                uiPort
        );

        controller.openIssue("player-1", "issue_visual");
        controller.handleCommand(
                "player-1",
                NewspaperVisualUiCommands.nextSpread()
        );

        assertNotNull(uiPort.lastView);
        assertEquals(1, uiPort.lastView.spreadIndex());
    }

    @Test
    void handlesVisualSelectCommand() throws IOException {
        CapturingVisualUiPort uiPort = new CapturingVisualUiPort();
        PlayerNewspaperVisualUiController controller = createController(
                new StubVisualPlugin(),
                uiPort
        );

        controller.handleCommand(
                "player-1",
                NewspaperVisualUiCommands.selectSpread(1)
        );

        assertNotNull(uiPort.lastView);
        assertEquals(1, uiPort.lastView.spreadIndex());
    }

    @Test
    void closesVisualIssueAndPort() throws IOException {
        createVisualDataSet();
        CapturingVisualUiPort uiPort = new CapturingVisualUiPort();
        PlayerNewspaperVisualUiController controller = createController(uiPort);

        controller.openIssue("player-1", "issue_visual");
        controller.closeIssue("player-1");

        assertEquals("player-1", uiPort.closedPlayerId);
    }

    @Test
    void showsMessageForMissingIssue() {
        CapturingVisualUiPort uiPort = new CapturingVisualUiPort();
        PlayerNewspaperVisualUiController controller = createController(uiPort);

        controller.openIssue("player-1", "missing");

        assertNotNull(uiPort.lastView);
        assertFalse(uiPort.lastView.newspaperOpen());
        assertTrue(uiPort.lastView.hasMessage());
    }

    private PlayerNewspaperVisualUiController createController(
            CapturingVisualUiPort uiPort
    ) {
        return new PlayerNewspaperVisualUiController(
                new AthenaPressIntegrationPlugin(tempDir),
                uiPort
        );
    }

    private PlayerNewspaperVisualUiController createController(
            AthenaPressIntegrationPlugin plugin,
            CapturingVisualUiPort uiPort
    ) {
        return new PlayerNewspaperVisualUiController(plugin, uiPort);
    }

    private void createVisualDataSet() throws IOException {
        createDataSetWithArticleBody("Kurzer Artikeltext.");
    }

    private void createDataSetWithArticleBody(String body) throws IOException {
        createFolders();

        Files.writeString(
                tempDir.resolve("articles").resolve("published").resolve("article_visual.json"),
                """
                {
                  "id": "article_visual",
                  "status": "published",
                  "categoryId": "stadtklatsch",
                  "title": "Die Laterne berichtet vom Platz",
                  "subtitle": "Ein helles Protokoll",
                  "teaser": "Die Menge war erleuchtet.",
                  "summary": "Die Laterne hat alles gesehen.",
                  "body": "%s"
                }
                """.formatted(body)
        );

        Files.writeString(
                tempDir.resolve("issues").resolve("published").resolve("issue_visual.json"),
                """
                {
                  "id": "issue_visual",
                  "status": "published",
                  "issueNumber": 5,
                  "title": "Athena Sichtblatt",
                  "subtitle": "Frisch geblättert",
                  "articles": [
                    "article_visual"
                  ],
                  "cover": {
                    "mainArticleId": "article_visual",
                    "image": "placeholders/lantern.png"
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

    private static class CapturingVisualUiPort implements PlayerNewspaperVisualUiPort {
        private PlayerNewspaperVisualView lastView;
        private String closedPlayerId;

        @Override
        public void show(PlayerNewspaperVisualView view) {
            this.lastView = view;
        }

        @Override
        public void close(String playerId) {
            this.closedPlayerId = playerId;
        }
    }

    private class StubVisualPlugin extends AthenaPressIntegrationPlugin {

        StubVisualPlugin() {
            super(tempDir);
        }

        @Override
        public PlayerNewspaperVisualResponse onPlayerOpenVisualNewspaper(
                String playerId,
                String issueId
        ) {
            return response(playerId, issueId, 0, 2);
        }

        @Override
        public PlayerNewspaperVisualResponse onPlayerRequestNextVisualSpread(
                String playerId
        ) {
            return response(playerId, "issue_visual", 1, 2);
        }

        @Override
        public PlayerNewspaperVisualResponse onPlayerRequestVisualSpread(
                String playerId,
                int spreadIndex
        ) {
            return response(playerId, "issue_visual", spreadIndex, 2);
        }

        private PlayerNewspaperVisualResponse response(
                String playerId,
                String issueId,
                int spreadIndex,
                int totalSpreadCount
        ) {
            return new PlayerNewspaperVisualResponse(
                    playerId,
                    issueId,
                    "Athena Sichtblatt",
                    spreadIndex,
                    totalSpreadCount,
                    new NewspaperPreviewSpread(
                            spreadIndex,
                            new NewspaperPreviewPage(
                                    spreadIndex + 1,
                                    "Seite " + (spreadIndex + 1),
                                    NewspaperPageRole.FRONT_COVER,
                                    NewspaperVisualDesignProfile.athenaReadableNewspaper(),
                                    java.util.List.of(new NewspaperPreviewBlock(
                                            NewspaperVisualBlockType.HEADLINE,
                                            "Testseite",
                                            null,
                                            0,
                                            0,
                                            2,
                                            2
                                    ))
                            ),
                            null,
                            java.util.List.of()
                    ),
                    java.util.List.of(),
                    true,
                    ""
            );
        }
    }
}
