package pro.jeti.athenapress.integration;

import java.util.Locale;
import java.util.Set;

import pro.jeti.athenapress.model.GameArticleView;

public class NewspaperArticleClassifier {

    private static final Set<String> ADVERTISEMENT_CATEGORY_MARKERS = Set.of(
            "ad",
            "ads",
            "advertisement",
            "anzeige",
            "anzeigen",
            "werbung"
    );
    private static final Set<String> SHORT_NOTICE_CATEGORY_MARKERS = Set.of(
            "short",
            "kurz",
            "kurzmeldung",
            "notice",
            "ticker"
    );
    private static final Set<String> MEMORIAL_TEXT_MARKERS = Set.of(
            "beileid",
            "verschollen",
            "vermisst",
            "nachruf",
            "gedenken"
    );

    public NewspaperArticleClassification classify(
            GameArticleView article,
            boolean mainArticle
    ) {
        if (mainArticle) {
            return NewspaperArticleClassification.forMainArticle();
        }

        if (article == null) {
            return NewspaperArticleClassification.forMixedArticle();
        }

        String category = normalize(article.categoryId());
        String searchableText = normalize(String.join(
                " ",
                value(article.title()),
                value(article.subtitle()),
                value(article.teaser()),
                value(article.summary()),
                value(article.body())
        ));

        if (containsAny(category, ADVERTISEMENT_CATEGORY_MARKERS)) {
            return NewspaperArticleClassification.forAdvertisement();
        }

        if (containsAny(searchableText, MEMORIAL_TEXT_MARKERS)) {
            return NewspaperArticleClassification.forMemorial();
        }

        if (containsAny(category, SHORT_NOTICE_CATEGORY_MARKERS) || isShortNotice(article)) {
            return NewspaperArticleClassification.forShortNotice();
        }

        return NewspaperArticleClassification.forMixedArticle();
    }

    private boolean isShortNotice(GameArticleView article) {
        String body = value(article.body());
        String summary = value(article.summary());

        return body.length() <= 180 && summary.length() <= 120;
    }

    private boolean containsAny(String text, Set<String> markers) {
        if (text == null || text.isBlank()) {
            return false;
        }

        return markers.stream().anyMatch(text::contains);
    }

    private String normalize(String value) {
        return value(value).toLowerCase(Locale.ROOT);
    }

    private String value(String value) {
        return value == null ? "" : value.trim();
    }
}
