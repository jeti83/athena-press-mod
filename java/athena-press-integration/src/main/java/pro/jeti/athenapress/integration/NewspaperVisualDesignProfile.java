package pro.jeti.athenapress.integration;

public record NewspaperVisualDesignProfile(
        String profileId,
        NewspaperLayoutMood layoutMood,
        NewspaperPageCornerStyle cornerStyle,
        int preferredColumns,
        int maximumColumns,
        int asymmetryPercent,
        NewspaperCoverPolicy coverPolicy,
        NewspaperArticleFlowPolicy articleFlowPolicy,
        NewspaperNavigationStyle navigationStyle,
        boolean allowAdvertisementBlocks,
        boolean allowDocumentStyleBlocks,
        boolean keepReadablePageBody
) {

    public NewspaperVisualDesignProfile {
        profileId = profileId == null || profileId.isBlank()
                ? "athena_classic_readable"
                : profileId;
        layoutMood = layoutMood == null
                ? NewspaperLayoutMood.CLASSIC_NEWSPAPER
                : layoutMood;
        cornerStyle = cornerStyle == null
                ? NewspaperPageCornerStyle.SUBTLE_TOP_FOLDS
                : cornerStyle;
        preferredColumns = preferredColumns <= 0 ? 3 : preferredColumns;
        maximumColumns = Math.max(preferredColumns, maximumColumns);
        asymmetryPercent = Math.clamp(asymmetryPercent, 0, 100);
        coverPolicy = coverPolicy == null
                ? NewspaperCoverPolicy.STANDALONE_TITLE_PAGE
                : coverPolicy;
        articleFlowPolicy = articleFlowPolicy == null
                ? NewspaperArticleFlowPolicy.KEEP_ARTICLES_TOGETHER_WHEN_READABLE
                : articleFlowPolicy;
        navigationStyle = navigationStyle == null
                ? NewspaperNavigationStyle.PAGE_TURNING_WITH_SUBTLE_MENU
                : navigationStyle;
    }

    public static NewspaperVisualDesignProfile athenaReadableNewspaper() {
        return new NewspaperVisualDesignProfile(
                "athena_readable_newspaper",
                NewspaperLayoutMood.LOOSE_COMMUNITY_SHEET,
                NewspaperPageCornerStyle.SUBTLE_TOP_FOLDS,
                3,
                4,
                25,
                NewspaperCoverPolicy.STANDALONE_TITLE_PAGE,
                NewspaperArticleFlowPolicy.KEEP_ARTICLES_TOGETHER_WHEN_READABLE,
                NewspaperNavigationStyle.PAGE_TURNING_WITH_SUBTLE_MENU,
                true,
                true,
                true
        );
    }

    public NewspaperLayoutTemplate toLayoutTemplate() {
        int rowsPerPage = switch (layoutMood) {
            case CLASSIC_NEWSPAPER -> 26;
            case LOOSE_COMMUNITY_SHEET -> 24;
            case FEATURE_DOCUMENT -> 20;
        };

        return new NewspaperLayoutTemplate(
                profileId,
                displayName(),
                100,
                140,
                6,
                4,
                preferredColumns,
                rowsPerPage
        );
    }

    public boolean allowsLooseComposition() {
        return asymmetryPercent > 0
                || layoutMood == NewspaperLayoutMood.LOOSE_COMMUNITY_SHEET
                || layoutMood == NewspaperLayoutMood.FEATURE_DOCUMENT;
    }

    private String displayName() {
        return switch (layoutMood) {
            case CLASSIC_NEWSPAPER -> "Klassische Zeitung";
            case LOOSE_COMMUNITY_SHEET -> "Athena Community-Zeitung";
            case FEATURE_DOCUMENT -> "Dokumentartige Artikelseite";
        };
    }
}
