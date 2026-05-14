package pro.jeti.athenapress.integration;

import java.util.List;

public record NewspaperVisualIssue(
        String issueId,
        String title,
        NewspaperVisualTheme theme,
        List<NewspaperVisualPage> pages
) {

    public NewspaperVisualIssue {
        issueId = issueId == null ? "" : issueId;
        title = title == null || title.isBlank() ? "AthenaPress" : title;
        theme = theme == null ? NewspaperVisualTheme.defaultTheme() : theme;
        pages = pages == null ? List.of() : List.copyOf(pages);
    }

    public boolean hasPages() {
        return !pages.isEmpty();
    }

    public NewspaperVisualDoublePage doublePageAt(int pageIndex) {
        if (pages.isEmpty()) {
            return new NewspaperVisualDoublePage(null, null);
        }

        int safeIndex = Math.max(0, pageIndex);
        NewspaperVisualPage leftPage = safeIndex < pages.size() ? pages.get(safeIndex) : null;
        NewspaperVisualPage rightPage = safeIndex + 1 < pages.size() ? pages.get(safeIndex + 1) : null;

        return new NewspaperVisualDoublePage(leftPage, rightPage);
    }
}