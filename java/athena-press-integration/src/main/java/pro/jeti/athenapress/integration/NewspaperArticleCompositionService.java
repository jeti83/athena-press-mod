package pro.jeti.athenapress.integration;

import java.util.ArrayList;
import java.util.List;

import pro.jeti.athenapress.model.GameArticleView;
import pro.jeti.athenapress.model.GameIssueView;
import pro.jeti.athenapress.model.ImageInfo;

public class NewspaperArticleCompositionService {

    private final NewspaperVisualPaginationService paginationService;
    private final NewspaperLayoutTemplate defaultTemplate;
    private final NewspaperVisualDesignProfile designProfile;
    private final NewspaperPageSectionPolicy sectionPolicy;
    private final NewspaperArticleClassifier articleClassifier;
    private final NewspaperArticleTextFlowService articleTextFlowService;

    public NewspaperArticleCompositionService() {
        this(
                new NewspaperVisualPaginationService(),
                NewspaperLayoutTemplate.classicDoublePage(),
                NewspaperVisualDesignProfile.athenaReadableNewspaper(),
                NewspaperPageSectionPolicy.defaultPolicy(),
                new NewspaperArticleClassifier(),
                new NewspaperArticleTextFlowService()
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
                new NewspaperArticleClassifier(),
                new NewspaperArticleTextFlowService()
        );
    }

    public NewspaperArticleCompositionService(
            NewspaperVisualPaginationService paginationService,
            NewspaperLayoutTemplate defaultTemplate,
            NewspaperVisualDesignProfile designProfile,
            NewspaperPageSectionPolicy sectionPolicy,
            NewspaperArticleClassifier articleClassifier
    ) {
        this(
                paginationService,
                defaultTemplate,
                designProfile,
                sectionPolicy,
                articleClassifier,
                new NewspaperArticleTextFlowService()
        );
    }

    public NewspaperArticleCompositionService(
            NewspaperVisualPaginationService paginationService,
            NewspaperLayoutTemplate defaultTemplate,
            NewspaperVisualDesignProfile designProfile,
            NewspaperPageSectionPolicy sectionPolicy,
            NewspaperArticleClassifier articleClassifier,
            NewspaperArticleTextFlowService articleTextFlowService
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
        this.articleTextFlowService = articleTextFlowService == null
                ? new NewspaperArticleTextFlowService()
                : articleTextFlowService;
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
        if (designProfile.articleFlowPolicy()
                == NewspaperArticleFlowPolicy.KEEP_ARTICLES_TOGETHER_WHEN_READABLE) {
            pages.addAll(paginationService.paginateGrouped(
                    issueTitle(issueView),
                    articleBlockGroupsFor(issueView),
                    defaultTemplate,
                    pages.size() + 1
            ));
        } else {
            pages.addAll(paginationService.paginate(
                    issueTitle(issueView),
                    blocksForSections(articleSectionsFor(issueView)),
                    defaultTemplate,
                    pages.size() + 1
            ));
        }

        return pages;
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

    private List<List<NewspaperVisualBlock>> articleBlockGroupsFor(GameIssueView issueView) {
        List<List<NewspaperVisualBlock>> groups = new ArrayList<>();

        List<NewspaperVisualBlock> mainArticleBlocks = mainArticleBlocksFor(issueView);
        if (!mainArticleBlocks.isEmpty()
                && sectionPolicy.shouldInclude(NewspaperPageSection.of(
                        NewspaperPageSectionType.MAIN_ARTICLE,
                        issueTitle(issueView),
                        mainArticleBlocks
                ))) {
            groups.add(mainArticleBlocks);
        }

        GameArticleView mainArticle = issueView.findArticleById(issueView.coverMainArticleId());
        for (NewspaperPageSectionType sectionType : List.of(
                NewspaperPageSectionType.MIXED_ARTICLES,
                NewspaperPageSectionType.SHORT_NOTICES,
                NewspaperPageSectionType.MEMORIAL,
                NewspaperPageSectionType.ADVERTISEMENTS
        )) {
            List<List<NewspaperVisualBlock>> sectionGroups = new ArrayList<>();

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

                List<NewspaperVisualBlock> blocks = classifiedArticleBlocksFor(
                        article,
                        classification
                );
                if (!blocks.isEmpty()) {
                    sectionGroups.add(blocks);
                }
            }

            if (sectionPolicy.shouldInclude(NewspaperPageSection.of(
                    sectionType,
                    sectionTitleFor(sectionType),
                    blocksForGroups(sectionGroups)
            ))) {
                List<NewspaperVisualBlock> headingBlocks =
                        sectionHeadingBlocksFor(sectionType);
                if (!headingBlocks.isEmpty()) {
                    groups.add(headingBlocks);
                }
                groups.addAll(sectionGroups);
            }
        }

        return groups;
    }

    private List<NewspaperVisualBlock> blocksForGroups(
            List<List<NewspaperVisualBlock>> groups
    ) {
        if (groups == null || groups.isEmpty()) {
            return List.of();
        }

        return groups.stream()
                .flatMap(List::stream)
                .toList();
    }

    private String sectionTitleFor(NewspaperPageSectionType sectionType) {
        return switch (sectionType) {
            case MAIN_ARTICLE, MIXED_ARTICLES -> "AthenaPress";
            case SHORT_NOTICES -> "Kurzmeldungen";
            case MEMORIAL -> "Verschollen und unvergessen";
            case ADVERTISEMENTS -> "Anzeigen";
            case TITLE_PAGE -> "Titelseite";
            case BACK_PAGE -> "Rückseite";
        };
    }

    private List<NewspaperVisualBlock> blocksForSections(List<NewspaperPageSection> sections) {
        if (sections == null || sections.isEmpty()) {
            return List.of();
        }

        return sections.stream()
                .flatMap(section -> visibleBlocksFor(section).stream())
                .toList();
    }

    private List<NewspaperVisualBlock> visibleBlocksFor(NewspaperPageSection section) {
        List<NewspaperVisualBlock> blocks = new ArrayList<>(
                sectionHeadingBlocksFor(section.type())
        );
        blocks.addAll(section.blocks());
        return blocks;
    }

    private List<NewspaperVisualBlock> sectionHeadingBlocksFor(
            NewspaperPageSectionType sectionType
    ) {
        if (sectionType == null
                || sectionType == NewspaperPageSectionType.TITLE_PAGE
                || sectionType == NewspaperPageSectionType.MAIN_ARTICLE
                || sectionType == NewspaperPageSectionType.MIXED_ARTICLES) {
            return List.of();
        }

        return List.of(
                NewspaperVisualBlock.subheadline(sectionTitleFor(sectionType)),
                NewspaperVisualBlock.divider()
        );
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

    private List<NewspaperVisualBlock> classifiedArticleBlocksFor(
            GameArticleView article,
            NewspaperArticleClassification classification
    ) {
        List<NewspaperVisualBlock> blocks = new ArrayList<>();
        addClassifiedArticleBlocks(blocks, article, classification);
        return blocks;
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

        addArticleImageBlock(blocks, article, 1);

        if (hasText(article.body())) {
            addBodyTextBlocks(blocks, article.body(), 1);
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

        addArticleImageBlock(blocks, article, defaultTemplate.columnsPerPage());

        if (hasText(article.body())) {
            addBodyTextBlocks(blocks, article.body(), defaultTemplate.columnsPerPage());
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

        addArticleImageBlock(blocks, article, 1);

        if (hasText(article.body())) {
            addBodyTextBlocks(blocks, article.body(), 1);
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

    private void addBodyTextBlocks(
            List<NewspaperVisualBlock> blocks,
            String body,
            int columnSpan
    ) {
        for (String segment : articleTextFlowService.split(body)) {
            blocks.add(NewspaperVisualBlock.bodyText(segment, columnSpan));
        }
    }

    private void addArticleImageBlock(
            List<NewspaperVisualBlock> blocks,
            GameArticleView article,
            int columnSpan
    ) {
        ImageInfo image = article == null ? null : article.image();
        if (image == null || !hasText(image.file())) {
            return;
        }

        blocks.add(NewspaperVisualBlock.image(
                image.file(),
                hasText(image.caption()) ? image.caption() : articleTitle(article),
                columnSpan
        ));
    }
}
