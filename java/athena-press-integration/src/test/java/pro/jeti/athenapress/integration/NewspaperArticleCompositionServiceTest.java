package pro.jeti.athenapress.integration;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import pro.jeti.athenapress.model.GameArticleView;
import pro.jeti.athenapress.model.GameIssueView;
import pro.jeti.athenapress.model.ImageInfo;

class NewspaperArticleCompositionServiceTest {

    @Test
    void composesIssueIntoVisualNewspaperPages() {
        GameIssueView issueView = issueViewWithArticles(1);

        NewspaperVisualIssue visualIssue = new NewspaperArticleCompositionService()
                .compose(issueView);

        assertEquals("issue_test", visualIssue.issueId());
        assertEquals("Athena Morgenblatt", visualIssue.title());
        assertFalse(visualIssue.pages().isEmpty());
        assertTrue(visualIssue.pages().getFirst().blocks().stream()
                .anyMatch(block -> block.type() == NewspaperVisualBlockType.HEADLINE));
        assertTrue(visualIssue.pages().getFirst().blocks().stream()
                .anyMatch(block -> "placeholders/front.png".equals(block.assetPath())));
        assertTrue(visualIssue.pages().getFirst().blocks().stream()
                .anyMatch(block -> block.type() == NewspaperVisualBlockType.CAPTION
                        && "Artikel 1".equals(block.content())));
        assertTrue(visualIssue.pages().getFirst().blocks().stream()
                .anyMatch(block -> block.type() == NewspaperVisualBlockType.SUBHEADLINE
                        && "Artikel 1".equals(block.content())
                        && block.layoutIntent() == NewspaperBlockLayoutIntent.COVER));
        assertTrue(visualIssue.pages().getFirst().blocks().stream()
                .anyMatch(block -> block.type() == NewspaperVisualBlockType.NOTICE
                        && "Zusammenfassung 1".equals(block.content())
                        && block.columnSpan() == NewspaperLayoutTemplate.classicDoublePage()
                                .columnsPerPage()
                        && block.layoutIntent() == NewspaperBlockLayoutIntent.COVER));
        assertTrue(visualIssue.pages().getFirst().blocks().stream()
                .noneMatch(block -> block.type() == NewspaperVisualBlockType.QUOTE));
        assertTrue(visualIssue.pages().stream()
                .flatMap(page -> page.blocks().stream())
                .anyMatch(block -> block.type() == NewspaperVisualBlockType.QUOTE));
    }

    @Test
    void keepsStandaloneTitlePageBeforeArticlePages() {
        GameIssueView issueView = issueViewWithArticles(3);

        NewspaperVisualIssue visualIssue = new NewspaperArticleCompositionService()
                .compose(issueView);

        assertEquals(1, visualIssue.pages().getFirst().pageNumber());
        assertTrue(visualIssue.pages().getFirst().blocks().stream()
                .noneMatch(block -> block.type() == NewspaperVisualBlockType.QUOTE));
        assertTrue(visualIssue.pages().get(1).pageNumber() >= 2);
        assertTrue(visualIssue.pages().get(1).blocks().stream()
                .anyMatch(block -> "Zusammenfassung 1".equals(block.content())));
    }

    @Test
    void paginatesLongIssueAcrossMultiplePages() {
        GameIssueView issueView = issueViewWithArticles(20);

        NewspaperVisualIssue visualIssue = new NewspaperArticleCompositionService()
                .compose(issueView);

        assertTrue(visualIssue.pages().size() > 1);
        assertTrue(visualIssue.pages().getFirst().title().equals("Athena Morgenblatt"));
        assertTrue(visualIssue.pages().get(1).pageNumber() == 2);
        assertTrue(visualIssue.pages().get(2).title().contains("Seite 3"));
    }

    @Test
    void keepsReadableArticlesTogetherWhenTheyWouldOtherwiseSplit() {
        NewspaperArticleCompositionService service = new NewspaperArticleCompositionService(
                new NewspaperVisualPaginationService(),
                new NewspaperLayoutTemplate(
                        "compact_test",
                        "Kompakter Test",
                        100,
                        140,
                        6,
                        4,
                        2,
                        9
                )
        );
        GameIssueView issueView = issueViewWithoutMainArticle(2);

        NewspaperVisualIssue visualIssue = service.compose(issueView);

        assertTrue(visualIssue.pages().get(1).blocks().stream()
                .anyMatch(block -> "Artikel 1".equals(block.content())));
        assertTrue(visualIssue.pages().get(1).blocks().stream()
                .noneMatch(block -> "Artikel 2".equals(block.content())));
        assertTrue(visualIssue.pages().get(2).blocks().stream()
                .anyMatch(block -> "Artikel 2".equals(block.content())));
    }

    @Test
    void respectsDisabledSectionsWhenKeepingArticlesTogether() {
        NewspaperArticleCompositionService service = new NewspaperArticleCompositionService(
                new NewspaperVisualPaginationService(),
                NewspaperLayoutTemplate.classicDoublePage(),
                NewspaperVisualDesignProfile.athenaReadableNewspaper(),
                new NewspaperPageSectionPolicy(Map.of(
                        NewspaperPageSectionType.TITLE_PAGE,
                        NewspaperSectionRequirement.REQUIRED,
                        NewspaperPageSectionType.ADVERTISEMENTS,
                        NewspaperSectionRequirement.DISABLED
                ))
        );
        GameIssueView issueView = issueViewWithAdvertisement();

        NewspaperVisualIssue visualIssue = service.compose(issueView);

        assertTrue(visualIssue.pages().stream()
                .flatMap(page -> page.blocks().stream())
                .noneMatch(block -> "Anzeige 1".equals(block.content())));
    }

    @Test
    void carriesAdvertisementImagesAsAdvertisementPlacements() {
        GameIssueView issueView = issueViewWithImageAdvertisement();

        NewspaperVisualIssue visualIssue = new NewspaperArticleCompositionService()
                .compose(issueView);
        List<NewspaperPageLayout> layouts = new NewspaperVisualRenderer()
                .render(visualIssue, NewspaperLayoutTemplate.classicDoublePage());

        assertTrue(layouts.stream()
                .flatMap(layout -> layout.imagePlacements().stream())
                .anyMatch(image -> image.role() == NewspaperImageRole.ADVERTISEMENT
                        && "placeholders/ad.png".equals(image.assetPath())));
    }

    @Test
    void splitsLongArticleBodyIntoMultipleBodyTextBlocks() {
        NewspaperArticleCompositionService service = new NewspaperArticleCompositionService(
                new NewspaperVisualPaginationService(),
                NewspaperLayoutTemplate.classicDoublePage(),
                NewspaperVisualDesignProfile.athenaReadableNewspaper(),
                NewspaperPageSectionPolicy.defaultPolicy(),
                new NewspaperArticleClassifier(),
                new NewspaperArticleTextFlowService(80)
        );
        GameIssueView issueView = issueViewWithLongBody();

        NewspaperVisualIssue visualIssue = service.compose(issueView);

        long bodyTextBlocks = visualIssue.pages().stream()
                .flatMap(page -> page.blocks().stream())
                .filter(block -> block.type() == NewspaperVisualBlockType.BODY_TEXT)
                .count();

        assertTrue(bodyTextBlocks > 1);
    }

    @Test
    void marksContinuationWhenLongArticleFlowsOntoAnotherPage() {
        NewspaperArticleCompositionService service = new NewspaperArticleCompositionService(
                new NewspaperVisualPaginationService(),
                new NewspaperLayoutTemplate(
                        "compact_test",
                        "Kompakter Test",
                        100,
                        140,
                        6,
                        4,
                        2,
                        12
                ),
                NewspaperVisualDesignProfile.athenaReadableNewspaper(),
                NewspaperPageSectionPolicy.defaultPolicy(),
                new NewspaperArticleClassifier(),
                new NewspaperArticleTextFlowService(80)
        );
        GameIssueView issueView = issueViewWithLongBody();

        NewspaperVisualIssue visualIssue = service.compose(issueView);

        assertTrue(visualIssue.pages().stream()
                .skip(2)
                .flatMap(page -> page.blocks().stream())
                .anyMatch(block -> "Fortsetzung: Artikel 1".equals(block.content())));
    }

    @Test
    void addsVisibleHeadingsForOptionalSections() {
        GameIssueView issueView = issueViewWithOptionalSections();

        NewspaperVisualIssue visualIssue = new NewspaperArticleCompositionService()
                .compose(issueView);

        List<String> visibleBlockTexts = visualIssue.pages().stream()
                .flatMap(page -> page.blocks().stream())
                .map(NewspaperVisualBlock::content)
                .filter(content -> content != null && !content.isBlank())
                .toList();

        assertTrue(visibleBlockTexts.contains("Kurzmeldungen"));
        assertTrue(visibleBlockTexts.contains("Verschollen und unvergessen"));
        assertTrue(visibleBlockTexts.contains("Rückseite"));
    }

    @Test
    void marksShortNoticeAndMemorialBlocksWithEditorialLayoutIntent() {
        GameIssueView issueView = issueViewWithOptionalSections();

        NewspaperVisualIssue visualIssue = new NewspaperArticleCompositionService()
                .compose(issueView);

        assertTrue(visualIssue.pages().stream()
                .flatMap(page -> page.blocks().stream())
                .anyMatch(block -> "Kurze Nachricht".equals(block.content())
                        && block.layoutIntent() == NewspaperBlockLayoutIntent.SHORT_NOTICE));
        assertTrue(visualIssue.pages().stream()
                .flatMap(page -> page.blocks().stream())
                .anyMatch(block -> "Verschollen im Nebel".equals(block.content())
                        && block.layoutIntent() == NewspaperBlockLayoutIntent.MEMORIAL));
        assertTrue(visualIssue.pages().stream()
                .flatMap(page -> page.blocks().stream())
                .anyMatch(block -> "Verschollen und unvergessen".equals(block.content())
                        && block.layoutIntent() == NewspaperBlockLayoutIntent.MEMORIAL));
        assertTrue(visualIssue.pages().stream()
                .flatMap(page -> page.blocks().stream())
                .anyMatch(block -> "Ein stiller Moment.".equals(block.content())
                        && block.layoutIntent() == NewspaperBlockLayoutIntent.MEMORIAL));
    }

    @Test
    void movesAdvertisementsIntoBackPageSection() {
        GameIssueView issueView = issueViewWithImageAdvertisement();

        NewspaperVisualIssue visualIssue = new NewspaperArticleCompositionService()
                .compose(issueView);

        List<String> visibleBlockTexts = visualIssue.pages().stream()
                .flatMap(page -> page.blocks().stream())
                .map(NewspaperVisualBlock::content)
                .filter(content -> content != null && !content.isBlank())
                .toList();

        assertTrue(visibleBlockTexts.contains("Rückseite"));
        assertTrue(visibleBlockTexts.contains("Anzeige 1"));
        assertFalse(visibleBlockTexts.contains("Anzeigen"));
        assertTrue(visualIssue.pages().stream()
                .flatMap(page -> page.blocks().stream())
                .anyMatch(block -> "Anzeige 1".equals(block.content())
                        && block.layoutIntent() == NewspaperBlockLayoutIntent.BACK_PAGE));
        assertTrue(visualIssue.pages().getLast().blocks().stream()
                .anyMatch(block -> "Rückseite".equals(block.content())));
        assertTrue(visualIssue.pages().getLast().blocks().stream()
                .anyMatch(block -> "Anzeige 1".equals(block.content())));
        assertTrue(visualIssue.pages().stream()
                .limit(visualIssue.pages().size() - 1L)
                .flatMap(page -> page.blocks().stream())
                .noneMatch(block -> "Anzeige 1".equals(block.content())));
    }

    @Test
    void expandsSingleBackPageAdvertisementAcrossThePage() {
        GameIssueView issueView = issueViewWithImageAdvertisement();

        NewspaperVisualIssue visualIssue = new NewspaperArticleCompositionService()
                .compose(issueView);

        assertTrue(visualIssue.pages().getLast().blocks().stream()
                .anyMatch(block -> "Anzeige 1".equals(block.content())
                        && block.columnSpan() == NewspaperLayoutTemplate.classicDoublePage()
                                .columnsPerPage()));
    }

    @Test
    void keepsMultipleBackPageAdvertisementsCompact() {
        GameIssueView issueView = issueViewWithMultipleAdvertisements();

        NewspaperVisualIssue visualIssue = new NewspaperArticleCompositionService()
                .compose(issueView);

        assertTrue(visualIssue.pages().getLast().blocks().stream()
                .filter(block -> block.type() == NewspaperVisualBlockType.ADVERTISEMENT)
                .allMatch(block -> block.columnSpan() == 1));
    }

    @Test
    void addsArticleImagesToVisualComposition() {
        GameIssueView issueView = issueViewWithArticleImage();

        NewspaperVisualIssue visualIssue = new NewspaperArticleCompositionService()
                .compose(issueView);

        assertTrue(visualIssue.pages().stream()
                .flatMap(page -> page.blocks().stream())
                .anyMatch(block -> "placeholders/article.png".equals(block.assetPath())));
        assertTrue(visualIssue.pages().stream()
                .flatMap(page -> page.blocks().stream())
                .anyMatch(block -> block.imageRole() == NewspaperImageRole.ARTICLE));
        assertTrue(visualIssue.pages().stream()
                .flatMap(page -> page.blocks().stream())
                .anyMatch(block -> block.type() == NewspaperVisualBlockType.CAPTION
                        && "Bildunterschrift".equals(block.content())));
    }

    @Test
    void rendersVisualPagesIntoAdapterNeutralLayout() {
        GameIssueView issueView = issueViewWithArticles(1);
        NewspaperVisualIssue visualIssue = new NewspaperArticleCompositionService()
                .compose(issueView);

        List<NewspaperPageLayout> layouts = new NewspaperVisualRenderer()
                .render(visualIssue, NewspaperLayoutTemplate.classicDoublePage());

        NewspaperPageLayout firstLayout = layouts.getFirst();

        assertEquals(2, firstLayout.columns().size());
        assertFalse(firstLayout.placements().isEmpty());
        assertEquals(NewspaperVisualBlockType.HEADLINE, firstLayout.placements().getFirst().blockType());
        assertEquals(2, firstLayout.placements().getFirst().columnSpan());
        assertEquals("placeholders/front.png", firstLayout.imagePlacements().getFirst().assetPath());
        assertEquals(NewspaperImageRole.COVER, firstLayout.imagePlacements().getFirst().role());
        assertEquals(2, firstLayout.imagePlacements().getFirst().columnSpan());
    }

    @Test
    void usesLooseCompositionToBreakSymmetricColumnStarts() {
        NewspaperVisualIssue visualIssue = new NewspaperVisualIssue(
                "issue_loose",
                "Athena Lockerblatt",
                NewspaperVisualTheme.defaultTheme(),
                List.of(NewspaperVisualPage.of(
                        1,
                        "Athena Lockerblatt",
                        List.of(
                                NewspaperVisualBlock.divider(),
                                NewspaperVisualBlock.notice("Nicht streng links beginnen")
                        )
                ))
        );
        NewspaperVisualDesignProfile looseProfile = new NewspaperVisualDesignProfile(
                "always_loose",
                NewspaperLayoutMood.LOOSE_COMMUNITY_SHEET,
                NewspaperPageCornerStyle.HANGING_TOP_CORNERS,
                3,
                4,
                100,
                NewspaperCoverPolicy.STANDALONE_TITLE_PAGE,
                NewspaperArticleFlowPolicy.KEEP_ARTICLES_TOGETHER_WHEN_READABLE,
                NewspaperNavigationStyle.PAGE_TURNING_WITH_SUBTLE_MENU,
                true,
                true,
                true
        );

        NewspaperPageLayout layout = new NewspaperVisualRenderer().render(
                visualIssue,
                looseProfile.toLayoutTemplate(),
                looseProfile
        ).getFirst();

        assertEquals(2, layout.placements().get(1).columnIndex());
    }

    private GameIssueView issueViewWithArticles(int articleCount) {
        List<GameArticleView> articles = java.util.stream.IntStream.rangeClosed(1, articleCount)
                .mapToObj(index -> new GameArticleView(
                        "article_" + index,
                        "server_news",
                        "Artikel " + index,
                        "Untertitel " + index,
                        "Teaser " + index,
                        "Zusammenfassung " + index,
                        "Dies ist ein langer lesbarer Artikeltext fuer Layouttests."
                ))
                .toList();

        return new GameIssueView(
                "issue_test",
                7,
                "Athena Morgenblatt",
                "Aus der Stadt, fuer die Stadt",
                "article_1",
                "placeholders/front.png",
                articles
        );
    }

    private GameIssueView issueViewWithoutMainArticle(int articleCount) {
        List<GameArticleView> articles = java.util.stream.IntStream.rangeClosed(1, articleCount)
                .mapToObj(index -> new GameArticleView(
                        "article_" + index,
                        "server_news",
                        "Artikel " + index,
                        "Untertitel " + index,
                        "Teaser " + index,
                        "Zusammenfassung " + index,
                        "Dies ist ein langer lesbarer Artikeltext fuer Layouttests."
                ))
                .toList();

        return new GameIssueView(
                "issue_test",
                7,
                "Athena Morgenblatt",
                "Aus der Stadt, fuer die Stadt",
                "missing_article",
                "placeholders/front.png",
                articles
        );
    }

    private GameIssueView issueViewWithAdvertisement() {
        return new GameIssueView(
                "issue_test",
                7,
                "Athena Morgenblatt",
                "Aus der Stadt, fuer die Stadt",
                "missing_article",
                "placeholders/front.png",
                List.of(new GameArticleView(
                        "ad_1",
                        "anzeigen",
                        "Anzeige 1",
                        null,
                        "Schaut vorbei.",
                        "Heute frisch.",
                        "Die Anzeige darf verschwinden."
                ))
        );
    }

    private GameIssueView issueViewWithImageAdvertisement() {
        return new GameIssueView(
                "issue_test",
                7,
                "Athena Morgenblatt",
                "Aus der Stadt, fuer die Stadt",
                "missing_article",
                "placeholders/front.png",
                List.of(new GameArticleView(
                        "ad_1",
                        "anzeigen",
                        "Anzeige 1",
                        null,
                        "Schaut vorbei.",
                        "Heute frisch.",
                        "Die Anzeige darf ein Signet tragen.",
                        new ImageInfo(
                                "placeholders/ad.png",
                                "Signet",
                                null,
                                "local"
                        )
                ))
        );
    }

    private GameIssueView issueViewWithMultipleAdvertisements() {
        return new GameIssueView(
                "issue_test",
                7,
                "Athena Morgenblatt",
                "Aus der Stadt, fuer die Stadt",
                "missing_article",
                "placeholders/front.png",
                List.of(
                        new GameArticleView(
                                "ad_1",
                                "anzeigen",
                                "Anzeige 1",
                                null,
                                "Schaut vorbei.",
                                "Heute frisch.",
                                "Die Anzeige darf ein Signet tragen."
                        ),
                        new GameArticleView(
                                "ad_2",
                                "classifieds",
                                "Anzeige 2",
                                null,
                                "Auch heute.",
                                "Noch frisch.",
                                "Die zweite Anzeige bleibt kompakt."
                        )
                )
        );
    }

    private GameIssueView issueViewWithLongBody() {
        return new GameIssueView(
                "issue_test",
                7,
                "Athena Morgenblatt",
                "Aus der Stadt, fuer die Stadt",
                "article_1",
                "placeholders/front.png",
                List.of(new GameArticleView(
                        "article_1",
                        "server_news",
                        "Artikel 1",
                        "Untertitel 1",
                        "Teaser 1",
                        "Zusammenfassung 1",
                        "Erster ausfuehrlicher Absatz mit allerlei Beobachtungen aus Athena. "
                                + "Zweiter Satz mit noch mehr Berichtswert und ordentlicher Laenge.\n\n"
                                + "Zweiter ausfuehrlicher Absatz mit weiteren Stimmen vom Platz."
                ))
        );
    }

    private GameIssueView issueViewWithOptionalSections() {
        return new GameIssueView(
                "issue_test",
                7,
                "Athena Morgenblatt",
                "Aus der Stadt, fuer die Stadt",
                "article_main",
                "placeholders/front.png",
                List.of(
                        new GameArticleView(
                                "article_main",
                                "server_news",
                                "Hauptartikel",
                                null,
                                "Teaser",
                                "Zusammenfassung",
                                "Langer Hauptartikeltext."
                        ),
                        new GameArticleView(
                                "article_short",
                                "kurzmeldung",
                                "Kurze Nachricht",
                                null,
                                "Kurz",
                                "Kurz",
                                "Kurz"
                        ),
                        new GameArticleView(
                                "article_memorial",
                                "server_news",
                                "Verschollen im Nebel",
                                null,
                                "Hinweis",
                                "Ein stiller Moment.",
                                "Ein Spieler gilt als verschollen."
                        ),
                        new GameArticleView(
                                "article_ad",
                                "anzeigen",
                                "Marktstand sucht Kundschaft",
                                null,
                                "Heute frisch",
                                "Heute frisch",
                                "Kommt vorbei."
                        )
                )
        );
    }

    private GameIssueView issueViewWithArticleImage() {
        return new GameIssueView(
                "issue_test",
                7,
                "Athena Morgenblatt",
                "Aus der Stadt, fuer die Stadt",
                "article_1",
                "placeholders/front.png",
                List.of(new GameArticleView(
                        "article_1",
                        "server_news",
                        "Artikel 1",
                        "Untertitel 1",
                        "Teaser 1",
                        "Zusammenfassung 1",
                        "Artikeltext mit Bild.",
                        new ImageInfo(
                                "placeholders/article.png",
                                "Bildunterschrift",
                                null,
                                "local"
                        )
                ))
        );
    }
}
