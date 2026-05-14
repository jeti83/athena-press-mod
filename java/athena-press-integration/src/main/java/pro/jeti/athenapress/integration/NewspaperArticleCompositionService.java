package pro.jeti.athenapress.integration;

import java.util.ArrayList;
import java.util.List;

import pro.jeti.athenapress.model.GameArticleView;
import pro.jeti.athenapress.model.GameIssueView;

public class NewspaperArticleCompositionService {

    private final NewspaperVisualPaginationService paginationService;
    private final NewspaperLayoutTemplate defaultTemplate;
    private final NewspaperVisualDesignProfile designProfile;

    public NewspaperArticleCompositionService() {
        this(
                new NewspaperVisualPaginationService(),
                NewspaperLayoutTemplate.classicDoublePage(),
                NewspaperVisualDesignProfile.athenaReadableNewspaper()
        );
    }

    public NewspaperArticleCompositionService(
            NewspaperVisualPaginationService paginationService,
            NewspaperLayoutTemplate defaultTemplate
    ) {
        this(
                paginationService,
                defaultTemplate,
                NewspaperVisualDesignProfile.athenaReadableNewspaper()
        );
    }

    public NewspaperArticleCompositionService(
            NewspaperVisualPaginationService paginationService,
            NewspaperLayoutTemplate defaultTemplate,
            NewspaperVisualDesignProfile designProfile
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
                    blocksForIssue(issueView),
                    defaultTemplate
            );
        }

        List<NewspaperVisualPage> pages = new ArrayList<>();
        pages.add(NewspaperVisualPage.of(
                1,
                issueTitle(issueView),
                coverBlocksFor(issueView)
        ));
        pages.addAll(paginationService.paginate(
                issueTitle(issueView),
                articleBlocksFor(issueView),
                defaultTemplate,
                2
        ));

        return pages;
    }

    private List<NewspaperVisualBlock> blocksForIssue(GameIssueView issueView) {
        List<NewspaperVisualBlock> blocks = new ArrayList<>();
        blocks.addAll(coverBlocksFor(issueView));
        blocks.addAll(articleBlocksFor(issueView));
        return blocks;
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

    private List<NewspaperVisualBlock> articleBlocksFor(GameIssueView issueView) {
        List<NewspaperVisualBlock> blocks = new ArrayList<>();

        GameArticleView mainArticle = issueView.findArticleById(issueView.coverMainArticleId());
        if (mainArticle != null) {
            addMainArticleBlocks(blocks, mainArticle);
        }

        for (GameArticleView article : issueView.articles()) {
            if (mainArticle != null && mainArticle.id().equals(article.id())) {
                continue;
            }

            addArticleBlocks(blocks, article);
        }

        return blocks;
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
