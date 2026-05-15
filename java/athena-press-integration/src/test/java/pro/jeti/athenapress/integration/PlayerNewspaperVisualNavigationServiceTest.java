package pro.jeti.athenapress.integration;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import pro.jeti.athenapress.service.GameViewService;
import pro.jeti.athenapress.service.PressService;

class PlayerNewspaperVisualNavigationServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void rejectsMissingPipeline() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new PlayerNewspaperVisualNavigationService(null)
        );
    }

    @Test
    void opensVisualIssueAtFirstSpread() throws IOException {
        PlayerNewspaperVisualNavigationService service = createService(3);

        PlayerNewspaperVisualResponse response =
                service.openIssue("player-1", "issue_visual");

        assertTrue(response.newspaperOpen());
        assertTrue(response.hasSpread());
        assertEquals("issue_visual", response.issueId());
        assertEquals(0, response.spreadIndex());
        assertEquals(3, response.totalSpreadCount());
        assertEquals("Titelseite", response.spreadSignatures().getFirst().label());
        assertEquals("Spread 0", response.spreadSignatures().getFirst().hint());
        assertFalse(response.hasPreviousSpread());
        assertTrue(response.hasNextSpread());
        assertEquals(1, service.getOpenSessionCount());
    }

    @Test
    void navigatesBetweenSpreadsWithinBounds() throws IOException {
        PlayerNewspaperVisualNavigationService service = createService(3);

        service.openIssue("player-1", "issue_visual");
        PlayerNewspaperVisualResponse second = service.showNextSpread("player-1");
        PlayerNewspaperVisualResponse third = service.showNextSpread("player-1");
        PlayerNewspaperVisualResponse stillThird = service.showNextSpread("player-1");
        PlayerNewspaperVisualResponse secondAgain = service.showPreviousSpread("player-1");

        assertEquals(1, second.spreadIndex());
        assertEquals(2, third.spreadIndex());
        assertEquals(2, stillThird.spreadIndex());
        assertFalse(stillThird.hasNextSpread());
        assertEquals(1, secondAgain.spreadIndex());
        assertTrue(secondAgain.hasPreviousSpread());
        assertTrue(secondAgain.hasNextSpread());
    }

    @Test
    void selectsSpreadWithinBounds() throws IOException {
        PlayerNewspaperVisualNavigationService service = createService(3);

        service.openIssue("player-1", "issue_visual");
        PlayerNewspaperVisualResponse selected = service.showSpread("player-1", 2);
        PlayerNewspaperVisualResponse clamped = service.showSpread("player-1", 9);

        assertEquals(2, selected.spreadIndex());
        assertEquals(2, clamped.spreadIndex());
        assertFalse(clamped.hasNextSpread());
    }

    @Test
    void keepsPlayerVisualSessionsSeparate() throws IOException {
        PlayerNewspaperVisualNavigationService service = createService(3);

        service.openIssue("player-1", "issue_visual");
        service.openIssue("player-2", "issue_visual");
        service.showNextSpread("player-1");

        assertEquals(1, service.getCurrentSpreadIndex("player-1"));
        assertEquals(0, service.getCurrentSpreadIndex("player-2"));
        assertEquals(2, service.getOpenSessionCount());
    }

    @Test
    void returnsMissingResponseForPlayerWithoutSession() throws IOException {
        PlayerNewspaperVisualNavigationService service = createService(3);

        PlayerNewspaperVisualResponse response = service.showCurrentSpread("player-1");

        assertFalse(response.newspaperOpen());
        assertFalse(response.hasSpread());
        assertTrue(response.message().contains("Diese Ausgabe ist nicht verfügbar."));
    }

    @Test
    void closesVisualSession() throws IOException {
        PlayerNewspaperVisualNavigationService service = createService(3);

        service.openIssue("player-1", "issue_visual");
        service.closeIssue("player-1");

        assertFalse(service.hasOpenIssue("player-1"));
        assertEquals(0, service.getOpenSessionCount());
    }

    @Test
    void closesAllVisualSessions() throws IOException {
        PlayerNewspaperVisualNavigationService service = createService(3);

        service.openIssue("player-1", "issue_visual");
        service.openIssue("player-2", "issue_visual");
        service.closeAllIssues();

        assertEquals(0, service.getOpenSessionCount());
        assertFalse(service.hasOpenIssue("player-1"));
        assertFalse(service.hasOpenIssue("player-2"));
    }

    private PlayerNewspaperVisualNavigationService createService(int spreadCount) {
        NewspaperPreviewPipelineService pipelineService =
                new NewspaperPreviewPipelineService(
                        new GameViewService(new PressService(tempDir))
                ) {
                    @Override
                    public NewspaperPreviewIssue createPreview(String issueId) {
                        return previewIssue(issueId, spreadCount);
                    }
                };

        return new PlayerNewspaperVisualNavigationService(pipelineService);
    }

    private NewspaperPreviewIssue previewIssue(String issueId, int spreadCount) {
        List<NewspaperPreviewSpread> spreads = java.util.stream.IntStream.range(0, spreadCount)
                .mapToObj(index -> new NewspaperPreviewSpread(
                        index,
                        previewPage(index),
                        null,
                        List.of()
                ))
                .toList();

        return new NewspaperPreviewIssue(
                issueId,
                "Athena Visualblatt",
                NewspaperVisualTheme.defaultTheme(),
                NewspaperVisualDesignProfile.athenaReadableNewspaper(),
                spreads
        );
    }

    private NewspaperPreviewPage previewPage(int spreadIndex) {
        return new NewspaperPreviewPage(
                spreadIndex + 1,
                "Seite " + (spreadIndex + 1),
                spreadIndex == 0 ? NewspaperPageRole.FRONT_COVER : NewspaperPageRole.LEFT_INNER,
                NewspaperVisualDesignProfile.athenaReadableNewspaper(),
                List.of(new NewspaperPreviewBlock(
                        NewspaperVisualBlockType.HEADLINE,
                        "Spread " + spreadIndex,
                        null,
                        0,
                        0,
                        2,
                        2
                ))
        );
    }
}
