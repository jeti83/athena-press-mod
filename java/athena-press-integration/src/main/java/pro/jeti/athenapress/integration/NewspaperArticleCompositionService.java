package pro.jeti.athenapress.integration;

import java.util.ArrayList;
import java.util.List;

import pro.jeti.athenapress.model.GameArticleView;
import pro.jeti.athenapress.model.GameIssueView;

public class NewspaperArticleCompositionService {

    private final NewspaperVisualPaginationService paginationService;
    private final NewspaperLayoutTemplate defaultTemplate;
    private final NewspaperVisualDesignProfile designProfile;
    private final NewspaperPageSectionPolicy sectionPolicy;
    private final NewspaperArticleClassifier articleClassifier;

    public NewspaperArticleCompositionService() {
        this(
                new NewspaperVisualPaginationService(),
                NewspaperLayoutTemplate.classicDoublePage(),
                NewspaperVisualDesignProfile.athenaReadableNewspaper(),
                NewspaperPageSectionPolicy.defaultPolicy(),
                new NewspaperArticleClassifier()
        );
    }

    public NewspaperArticleCompositionService(
            NewspaperVisualPaginationService paginationService,
            NewspaperLayoutTemplate defaultTemplate
    ) {
        this(
                paginationService,
                defaultTemplate,
                NewspaperVisualDesignProfile.athenaReadableNewspaper(),
                NewspaperPageSectionPolicy.defaultPolicy(),
                new NewspaperArticleClassifier()
        );
    }

    public NewspaperArticleCompositionService(
            NewspaperVisualPaginationService paginationService,
            NewspaperLayoutTemplate defaultTemplate,
            NewspaperVisualDesignProfile designProfile
    ) {
        this(
                paginationService,
                defaultTemplate,
                designProfile,
                NewspaperPageSectionPolicy.defaultPolicy(),
                new NewspaperArticleClassifier()
        );
    }

    public NewspaperArticleCompositionService(
            NewspaperVisualPaginationService paginationService,
            NewspaperLayoutTemplate defaultTemplate,
            NewspaperVisualDesignProfile designProfile,
            NewspaperPageSectionPolicy sectionPolicy
    ) {
        this(
                paginationService,
                defaultTemplate,
                designProfile,
                sectionPolicy,
                new NewspaperArticleClassifier()
        );
    }

    public NewspaperArticleCompositionService(
            NewspaperVisualPaginationService paginationService,
            NewspaperLayoutTemplate defaultTemplate,
            NewspaperVisualDesignProfile designProfile,
            NewspaperPageSectionPolicy sectionPolicy,
            NewspaperArticleClassifier articleClassifier
    ) {
        this.paginationService = paginationService == null
                ? new NewspaperVisualPaginationService()
                : paginationService;
        this.defaultTemplate = defaultTemplate == null
                ? NewspaperLayoutTemplate.classicDoublePage()
                : defaultTemplate;
        this.designProfile = designProfile == null
                ? NewspaperVisualDesignProfile.athenaReadableNewspaper()
                : designProfile;
        this.sectionPolicy = sectionPolicy == null
                ? NewspaperPageSectionPolicy.defaultPolicy()
                : sectionPolicy;
        this.articleClassifier = articleClassifier == null
                ? new NewspaperArticleClassifier()
                : articleClassifier;
    }

    public NewspaperVisualIssue compose(GameIssueView issueView) {
        if (issueView == null) {
            return new NewspaperVisualIssue(
                    null,
                    "AthenaPress",
                    NewspaperVisualTheme.defaultTheme(),
                    List.of()
            );
        }

        List<NewspaperVisualPage> pages = pagesFor(issueView);

        return new NewspaperVisualIssue(
                issueView.id(),
                issueTitle(issueView),
                NewspaperVisualTheme.defaultTheme(),
                pages
        );
    }

    private List<NewspaperVisualPage> pagesFor(GameIssueView issueView) {
        if (designProfile.coverPolicy() != NewspaperCoverPolicy.STANDALONE_TITLE_PAGE) {
            return paginationService.paginate(
                    issueTitle(issueView),
                    blocksForSections(sectionsFor(issueView)),
                    defaultTemplate
            );
        }

        List<NewspaperVisualPage> pages = new ArrayList<>();
        sectionPolicy.filter(List.of(titlePageSectionFor(issueView)))
                .stream()
                .findFirst()
                .ifPresent(section -> pages.add(NewspaperVisualPage.of(
                        1,
                        section.title(),
                        section.blocks()
                )));
        pages.addAll(paginationService.paginate(
                issueTitle(issueView),
                blocksForSections(articleSectionsFor(issueView)),
                defaultTemplate,
                pages.size() + 1
        ));

        return pages;
    }

    private List<NewspaperVisualBlock> blocksForIssue(GameIssueView issueView) {
        return blocksForSections(sectionsFor(issueView));
    }

    private List<NewspaperPageSection> sectionsFor(GameIssueView issueView) {
        List<NewspaperPageSection> sections = new ArrayList<>();
        sections.add(titlePageSectionFor(issueView));
        sections.addAll(articleSectionsFor(issueView));
        return sectionPolicy.filter(sections);
    }

    private NewspaperPageSection titlePageSectionFor(GameIssueView issueView) {
        return NewspaperPageSection.of(
                NewspaperPageSectionType.TITLE_PAGE,
                issueTitle(issueView),
                coverBlocksFor(issueView)
        );
    }

    private List<NewspaperPageSection> articleSectionsFor(GameIssueView issueView) {
        List<NewspaperPageSection> sections = new ArrayList<>();

        List<NewspaperVisualBlock> mainArticleBlocks = mainArticleBlocksFor(issueView);
        sections.add(NewspaperPageSection.of(
                NewspaperPageSectionType.MAIN_ARTICLE,
                issueTitle(issueView),
                mainArticleBlocks
        ));

        sections.add(NewspaperPageSection.of(
                NewspaperPageSectionType.MIXED_ARTICLES,
                issueTitle(issueView),
                articleBlocksForSection(issueView, NewspaperPageSectionType.MIXED_ARTICLES)
        ));

        sections.add(NewspaperPageSection.of(
                NewspaperPageSectionType.SHORT_NOTICES,
                "Kurzmeldungen",
                articleBlocksForSection(issueView, NewspaperPageSectionType.SHORT_NOTICES)
        ));

        sections.add(NewspaperPageSection.of(
                NewspaperPageSectionType.MEMORIAL,
                "Verschollen und unvergessen",
                articleBlocksForSection(issueView, NewspaperPageSectionType.MEMORIAL)
        ));

        sections.add(NewspaperPageSection.of(
                NewspaperPageSectionType.ADVERTISEMENTS,
                "Anzeigen",
                articleBlocksForSection(issueView, NewspaperPageSectionType.ADVERTISEMENTS)
        ));

        return sectionPolicy.filter(sections);
    }

    private List<NewspaperVisualBlock> blocksForSections(List<NewspaperPageSection> sections) {
        if (sections == null || sections.isEmpty()) {
            return List.of();
        }

        return sections.stream()
                .flatMap(section -> section.blocks().stream())
                .toList();
    }

    private List<NewspaperVisualBlock> coverBlocksFor(GameIssueView issueView) {
        List<NewspaperVisualBlock> blocks = new ArrayList<>();
        blocks.add(NewspaperVisualBlock.headline(issueTitle(issueView)));

        if (hasText(issueView.subtitle())) {
            blocks.add(NewspaperVisualBlock.subheadline(issueView.subtitle()));
        }

        if (hasText(issueView.coverImage())) {
            blocks.add(NewspaperVisualBlock.image(
                    issueView.coverImage(),
                    coverCaption(issueView),
                    defaultTemplate.columnsPerPage()
            ));
        }

        blocks.add(NewspaperVisualBlock.divider());
        return blocks;
    }

    private List<NewspaperVisualBlock> mainArticleBlocksFor(GameIssueView issueView) {
        List<NewspaperVisualBlock> blocks = new ArrayList<>();

        GameArticleView mainArticle = issueView.findArticleById(issueView.coverMainArticleId());
        if (mainArticle != null) {
            addMainArticleBlocks(blocks, mainArticle);
        }

        return blocks;
    }

    private List<NewspaperVisualBlock> articleBlocksForSection(
            GameIssueView issueView,
            NewspaperPageSectionType sectionType
    ) {
        List<NewspaperVisualBlock> blocks = new ArrayList<>();

        GameArticleView mainArticle = issueView.findArticleById(issueView.coverMainArticleId());
        for (GameArticleView article : issueView.articles()) {
            if (mainArticle != null && mainArticle.id().equals(article.id())) {
                continue;
            }

            NewspaperArticleClassification classification = articleClassifier.classify(
                    article,
                    false
            );
            if (classification.sectionType() != sectionType) {
                continue;
            }

            addClassifiedArticleBlocks(blocks, article, classification);
        }

        return blocks;
    }

    private void addClassifiedArticleBlocks(
            List<NewspaperVisualBlock> blocks,
            GameArticleView article,
            NewspaperArticleClassification classification
    ) {
        if (classification == null) {
            addArticleBlocks(blocks, article);
            return;
        }

        if (classification.sectionType() == NewspaperPageSectionType.ADVERTISEMENTS) {
            addAdvertisementBlocks(blocks, article);
            return;
        }

        if (classification.sectionType() == NewspaperPageSectionType.SHORT_NOTICES) {
            addShortNoticeBlocks(blocks, article);
            return;
        }

        if (classification.sectionType() == NewspaperPageSectionType.MEMORIAL) {
            addMemorialBlocks(blocks, article);
            return;
        }

        addArticleBlocks(blocks, article);
    }

    private void addAdvertisementBlocks(
            List<NewspaperVisualBlock> blocks,
            GameArticleView article
    ) {
        if (article == null) {
            return;
        }

        blocks.add(NewspaperVisualBlock.advertisement(
                articleTitle(article),
                null
        ));

        if (hasText(article.summary())) {
            blocks.add(NewspaperVisualBlock.notice(article.summary()));
        }

        blocks.add(NewspaperVisualBlock.divider());
    }

    private void addShortNoticeBlocks(
            List<NewspaperVisualBlock> blocks,
            GameArticleView article
    ) {
        if (article == null) {
            return;
        }

        blocks.add(NewspaperVisualBlock.notice(articleTitle(article)));

        if (hasText(article.summary())) {
            blocks.add(NewspaperVisualBlock.bodyText(article.summary()));
        } else if (hasText(article.teaser())) {
            blocks.add(NewspaperVisualBlock.bodyText(article.teaser()));
        }

        blocks.add(NewspaperVisualBlock.divider());
    }

    private void addMemorialBlocks(
            List<NewspaperVisualBlock> blocks,
            GameArticleView article
    ) {
        if (article == null) {
            return;
        }

        blocks.add(NewspaperVisualBlock.subheadline(articleTitle(article)));

        if (hasText(article.summary())) {
            blocks.add(NewspaperVisualBlock.quote(article.summary()));
        } else if (hasText(article.teaser())) {
            blocks.add(NewspaperVisualBlock.quote(article.teaser()));
        }

        if (hasText(article.body())) {
            blocks.add(NewspaperVisualBlock.bodyText(article.body()));
        }

        blocks.add(NewspaperVisualBlock.divider());
    }

    private void addMainArticleBlocks(
            List<NewspaperVisualBlock> blocks,
            GameArticleView article
    ) {
        if (article == null) {
            return;
        }

        blocks.add(NewspaperVisualBlock.subheadline(articleTitle(article)));

        if (hasText(article.summary())) {
            blocks.add(NewspaperVisualBlock.quote(article.summary()));
        } else if (hasText(article.teaser())) {
            blocks.add(NewspaperVisualBlock.quote(article.teaser()));
        }

        if (hasText(article.body())) {
            blocks.add(NewspaperVisualBlock.bodyText(
                    article.body(),
                    defaultTemplate.columnsPerPage()
            ));
        }

        blocks.add(NewspaperVisualBlock.divider());
    }

    private void addArticleBlocks(
            List<NewspaperVisualBlock> blocks,
            GameArticleView article
    ) {
        if (article == null) {
            return;
        }

        blocks.add(NewspaperVisualBlock.subheadline(articleTitle(article)));

        if (hasText(article.summary())) {
            blocks.add(NewspaperVisualBlock.notice(article.summary()));
        } else if (hasText(article.teaser())) {
            blocks.add(NewspaperVisualBlock.notice(article.teaser()));
        }

        if (hasText(article.body())) {
            blocks.add(NewspaperVisualBlock.bodyText(article.body()));
        }

        blocks.add(NewspaperVisualBlock.divider());
    }

    private String issueTitle(GameIssueView issueView) {
        if (hasText(issueView.title())) {
            return issueView.title();
        }

        if (issueView.issueNumber() != null) {
            return "AthenaPress Ausgabe " + issueView.issueNumber();
        }

        return "AthenaPress";
    }

    private String articleTitle(GameArticleView article) {
        return hasText(article.title()) ? article.title() : "Unbenannter Artikel";
    }

    private String coverCaption(GameIssueView issueView) {
        GameArticleView mainArticle = issueView.findArticleById(issueView.coverMainArticleId());
        if (mainArticle != null && hasText(mainArticle.title())) {
            return mainArticle.title();
        }

        return issueTitle(issueView);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
