package pro.jeti.athenapress.integration;

import java.util.ArrayList;
import java.util.List;

import pro.jeti.athenapress.model.GameArticleView;
import pro.jeti.athenapress.model.GameIssueView;

public class NewspaperArticleCompositionService {

    private final NewspaperVisualPaginationService paginationService;
    private final NewspaperLayoutTemplate defaultTemplate;

    public NewspaperArticleCompositionService() {
        this(
                new NewspaperVisualPaginationService(),
                NewspaperLayoutTemplate.classicDoublePage()
        );
    }

    public NewspaperArticleCompositionService(
            NewspaperVisualPaginationService paginationService,
            NewspaperLayoutTemplate defaultTemplate
    ) {
        this.paginationService = paginationService == null
                ? new NewspaperVisualPaginationService()
                : paginationService;
        this.defaultTemplate = defaultTemplate == null
                ? NewspaperLayoutTemplate.classicDoublePage()
                : defaultTemplate;
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

        List<NewspaperVisualBlock> blocks = blocksFor(issueView);
        List<NewspaperVisualPage> pages = paginationService.paginate(
                issueTitle(issueView),
                blocks,
                defaultTemplate
        );

        return new NewspaperVisualIssue(
                issueView.id(),
                issueTitle(issueView),
                NewspaperVisualTheme.defaultTheme(),
                pages
        );
    }

    private List<NewspaperVisualBlock> blocksFor(GameIssueView issueView) {
        List<NewspaperVisualBlock> blocks = new ArrayList<>();

        blocks.add(NewspaperVisualBlock.headline(issueTitle(issueView)));

        if (hasText(issueView.subtitle())) {
            blocks.add(NewspaperVisualBlock.subheadline(issueView.subtitle()));
        }

        if (hasText(issueView.coverImage())) {
            blocks.add(NewspaperVisualBlock.image(
                    issueView.coverImage(),
                    coverCaption(issueView)
            ));
        }

        blocks.add(NewspaperVisualBlock.divider());

        for (GameArticleView article : issueView.articles()) {
            addArticleBlocks(blocks, article);
        }

        return blocks;
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
