package pro.jeti.athenapress.integration;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class NewspaperDoublePageCompositionServiceTest {

    @Test
    void composesVisualIssueIntoSpreadsWithRoles() {
        NewspaperVisualIssue issue = visualIssueWithPages(4);

        List<NewspaperDoublePageLayout> spreads =
                new NewspaperDoublePageCompositionService().compose(issue);

        assertEquals(2, spreads.size());
        assertEquals(NewspaperPageRole.FRONT_COVER, spreads.getFirst().leftRole());
        assertEquals(NewspaperPageRole.RIGHT_INNER, spreads.getFirst().rightRole());
        assertEquals(NewspaperPageRole.LEFT_INNER, spreads.get(1).leftRole());
        assertEquals(NewspaperPageRole.BACK_COVER, spreads.get(1).rightRole());
    }

    @Test
    void createsNavigationButtonsForMiddleSpread() {
        NewspaperVisualIssue issue = visualIssueWithPages(6);

        List<NewspaperDoublePageLayout> spreads =
                new NewspaperDoublePageCompositionService().compose(issue);

        NewspaperDoublePageLayout middleSpread = spreads.get(1);

        assertEquals(3, spreads.size());
        assertFalse(middleSpread.navigationButtons().isEmpty());
        assertTrue(middleSpread.navigationButtons().stream()
                .anyMatch(button -> button.command().uiCommand()
                        .equals(NewspaperVisualUiCommands.PREVIOUS_SPREAD)));
        assertTrue(middleSpread.navigationButtons().stream()
                .anyMatch(button -> button.command().uiCommand()
                        .equals(NewspaperVisualUiCommands.NEXT_SPREAD)));
    }

    @Test
    void supportsSingleLastPage() {
        NewspaperVisualIssue issue = visualIssueWithPages(3);

        List<NewspaperDoublePageLayout> spreads =
                new NewspaperDoublePageCompositionService().compose(issue);

        assertEquals(2, spreads.size());
        assertTrue(spreads.get(1).hasLeftPage());
        assertFalse(spreads.get(1).hasRightPage());
        assertEquals(NewspaperPageRole.BACK_COVER, spreads.get(1).leftRole());
    }

    private NewspaperVisualIssue visualIssueWithPages(int pageCount) {
        List<NewspaperVisualPage> pages = java.util.stream.IntStream.rangeClosed(1, pageCount)
                .mapToObj(index -> NewspaperVisualPage.of(
                        index,
                        "Seite " + index,
                        List.of(
                                NewspaperVisualBlock.headline("Titel " + index),
                                NewspaperVisualBlock.bodyText("Text " + index)
                        )
                ))
                .toList();

        return new NewspaperVisualIssue(
                "issue_spread_test",
                "Athena Doppelseite",
                NewspaperVisualTheme.defaultTheme(),
                pages
        );
    }
}
