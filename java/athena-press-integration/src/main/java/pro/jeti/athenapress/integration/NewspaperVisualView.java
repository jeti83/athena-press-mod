package pro.jeti.athenapress.integration;

import java.util.List;

public record NewspaperVisualView(
        String playerId,
        String issueId,
        String title,
        NewspaperVisualTheme theme,
        NewspaperVisualDoublePage doublePage,
        int pageIndex,
        boolean hasPreviousPage,
        boolean hasNextPage,
        List<NewspaperUiButton> buttons
) {

    public NewspaperVisualView {
        title = title == null || title.isBlank() ? "AthenaPress" : title;
        theme = theme == null ? NewspaperVisualTheme.defaultTheme() : theme;
        buttons = buttons == null ? List.of() : List.copyOf(buttons);
    }
}