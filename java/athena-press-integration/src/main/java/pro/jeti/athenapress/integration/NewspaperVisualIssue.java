package pro.jeti.athenapress.integration;

import java.util.List;

@SuppressWarnings("MismatchedReadAndWriteOfCollection")
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

}
