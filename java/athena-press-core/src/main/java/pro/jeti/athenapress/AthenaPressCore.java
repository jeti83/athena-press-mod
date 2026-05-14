package pro.jeti.athenapress;

import java.nio.file.Path;

import pro.jeti.athenapress.repository.ArticleRepository;
import pro.jeti.athenapress.repository.CategoryRepository;
import pro.jeti.athenapress.repository.IssueRepository;
import pro.jeti.athenapress.repository.SubscriberRepository;
import pro.jeti.athenapress.service.DeliveryService;
import pro.jeti.athenapress.service.GameNewspaperSessionService;
import pro.jeti.athenapress.service.GameTextRendererService;
import pro.jeti.athenapress.service.GameViewService;
import pro.jeti.athenapress.service.PressService;
import pro.jeti.athenapress.service.PreviewService;
import pro.jeti.athenapress.service.ValidationService;

public class AthenaPressCore {
    private static final String NAME = "AthenaPress Core";
    private static final String VERSION = "0.1.0-SNAPSHOT";

    private final Path athenaPressRoot;

    private final ArticleRepository articleRepository;
    private final IssueRepository issueRepository;
    private final SubscriberRepository subscriberRepository;
    private final CategoryRepository categoryRepository;

    private final PressService pressService;
    private final DeliveryService deliveryService;
    private final ValidationService validationService;
    private final PreviewService previewService;
    private final GameViewService gameViewService;
    private final GameTextRendererService gameTextRendererService;
    private final GameNewspaperSessionService gameNewspaperSessionService;

    public AthenaPressCore(Path athenaPressRoot) {
        this.athenaPressRoot = athenaPressRoot;

        this.articleRepository = new ArticleRepository(athenaPressRoot);
        this.issueRepository = new IssueRepository(athenaPressRoot);
        this.subscriberRepository = new SubscriberRepository(athenaPressRoot);
        this.categoryRepository = new CategoryRepository(athenaPressRoot);

        this.pressService = new PressService(athenaPressRoot);
        this.deliveryService = new DeliveryService(athenaPressRoot);
        this.validationService = new ValidationService(
                articleRepository,
                issueRepository,
                subscriberRepository,
                categoryRepository,
                athenaPressRoot
        );
        this.previewService = new PreviewService();
        this.gameViewService = new GameViewService(pressService);
        this.gameTextRendererService = new GameTextRendererService();
        this.gameNewspaperSessionService = new GameNewspaperSessionService(
                gameViewService,
                gameTextRendererService
        );
    }

    public String getName() {
        return NAME;
    }

    public String getVersion() {
        return VERSION;
    }

    public Path getAthenaPressRoot() {
        return athenaPressRoot;
    }

    public ArticleRepository getArticleRepository() {
        return articleRepository;
    }

    public IssueRepository getIssueRepository() {
        return issueRepository;
    }

    public SubscriberRepository getSubscriberRepository() {
        return subscriberRepository;
    }

    public CategoryRepository getCategoryRepository() {
        return categoryRepository;
    }

    public PressService getPressService() {
        return pressService;
    }

    public DeliveryService getDeliveryService() {
        return deliveryService;
    }

    public ValidationService getValidationService() {
        return validationService;
    }

    public PreviewService getPreviewService() {
        return previewService;
    }

    public GameViewService getGameViewService() {
        return gameViewService;
    }

    public GameTextRendererService getGameTextRendererService() {
        return gameTextRendererService;
    }

    public GameNewspaperSessionService getGameNewspaperSessionService() {
        return gameNewspaperSessionService;
    }
}