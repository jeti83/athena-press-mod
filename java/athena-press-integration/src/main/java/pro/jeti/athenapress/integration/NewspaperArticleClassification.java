package pro.jeti.athenapress.integration;

public record NewspaperArticleClassification(
        NewspaperPageSectionType sectionType,
        boolean mainArticle,
        boolean shortNotice,
        boolean specialTone
) {

    public NewspaperArticleClassification {
        sectionType = sectionType == null
                ? NewspaperPageSectionType.MIXED_ARTICLES
                : sectionType;
    }

    public static NewspaperArticleClassification forMainArticle() {
        return new NewspaperArticleClassification(
                NewspaperPageSectionType.MAIN_ARTICLE,
                true,
                false,
                false
        );
    }

    public static NewspaperArticleClassification forMixedArticle() {
        return new NewspaperArticleClassification(
                NewspaperPageSectionType.MIXED_ARTICLES,
                false,
                false,
                false
        );
    }

    public static NewspaperArticleClassification forShortNotice() {
        return new NewspaperArticleClassification(
                NewspaperPageSectionType.SHORT_NOTICES,
                false,
                true,
                false
        );
    }

    public static NewspaperArticleClassification forAdvertisement() {
        return new NewspaperArticleClassification(
                NewspaperPageSectionType.ADVERTISEMENTS,
                false,
                false,
                false
        );
    }

    public static NewspaperArticleClassification forMemorial() {
        return new NewspaperArticleClassification(
                NewspaperPageSectionType.MEMORIAL,
                false,
                false,
                true
        );
    }
}
