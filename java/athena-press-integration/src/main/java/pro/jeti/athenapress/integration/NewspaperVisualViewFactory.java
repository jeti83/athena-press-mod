package pro.jeti.athenapress.integration;

import java.util.ArrayList;
import java.util.List;

public class NewspaperVisualViewFactory {

    public NewspaperVisualView create(
            String playerId,
            NewspaperVisualIssue issue,
            int pageIndex
    ) {
        if (issue == null) {
            return new NewspaperVisualView(
                    playerId,
                    null,
                    "AthenaPress",
                    NewspaperVisualTheme.defaultTheme(),
                    new NewspaperVisualDoublePage(null, null),
                    0,
                    false,
                    false,
                    List.of(NewspaperUiButton.danger(
                            "Schließen",
                            PlayerNewspaperUiCommand.closeIssue()
                    ))
            );
        }

        int safePageIndex = Math.max(0, pageIndex);
        NewspaperVisualDoublePage doublePage = issue.doublePageAt(safePageIndex);

        return new NewspaperVisualView(
                playerId,
                issue.issueId(),
                issue.title(),
                issue.theme(),
                doublePage,
                safePageIndex,
                safePageIndex > 0,
                safePageIndex + 2 < issue.pages().size(),
                buttonsFor(issue, safePageIndex)
        );
    }

    private List<NewspaperUiButton> buttonsFor(
            NewspaperVisualIssue issue,
            int pageIndex
    ) {
        List<NewspaperUiButton> buttons = new ArrayList<>();

        if (pageIndex > 0) {
            buttons.add(NewspaperUiButton.secondary(
                    "Zurückblättern",
                    PlayerNewspaperUiCommand.custom("visual_previous_page", null)
            ));
        }

        if (pageIndex + 2 < issue.pages().size()) {
            buttons.add(NewspaperUiButton.primary(
                    "Weiterblättern",
                    PlayerNewspaperUiCommand.custom("visual_next_page", null)
            ));
        }

        buttons.add(NewspaperUiButton.danger(
                "Zeitung schließen",
                PlayerNewspaperUiCommand.closeIssue()
        ));

        return buttons;
    }
}