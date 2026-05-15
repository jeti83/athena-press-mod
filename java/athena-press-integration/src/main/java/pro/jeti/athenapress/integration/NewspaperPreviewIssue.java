package pro.jeti.athenapress.integration;

import java.util.List;

public record NewspaperPreviewIssue(
        String issueId,
        String title,
        NewspaperVisualTheme theme,
        NewspaperVisualDesignProfile designProfile,
        List<NewspaperPreviewSpread> spreads
) {

    public NewspaperPreviewIssue {
        issueId = issueId == null ? "" : issueId;
        title = title == null || title.isBlank() ? "AthenaPress" : title;
        theme = theme == null ? NewspaperVisualTheme.defaultTheme() : theme;
        designProfile = designProfile == null
                ? NewspaperVisualDesignProfile.athenaReadableNewspaper()
                : designProfile;
        spreads = spreads == null ? List.of() : List.copyOf(spreads);
    }

    public boolean hasSpreads() {
        return !spreads.isEmpty();
    }

    public List<NewspaperSpreadSignature> spreadSignatures() {
        return spreads.stream()
                .map(NewspaperPreviewSpread::signature)
                .toList();
    }
}
