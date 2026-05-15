package pro.jeti.athenapress.integration;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pro.jeti.athenapress.model.PlayerPhoto;
import pro.jeti.athenapress.service.AlbumSortOrder;
import pro.jeti.athenapress.service.ArticleDraftRequest;
import pro.jeti.athenapress.service.ArticleWriteService;
import pro.jeti.athenapress.service.PlayerAlbumService;
import pro.jeti.athenapress.repository.CategoryRepository;

public class ArticleEditorService {

    private final ArticleWriteService articleWriteService;
    private final CategoryRepository categoryRepository;
    private final PlayerAlbumService albumService;
    private final ArticleContentPolicy contentPolicy;
    private final Map<String, ArticleEditorSession> sessionsByPlayerId = new HashMap<>();

    public ArticleEditorService(
            ArticleWriteService articleWriteService,
            CategoryRepository categoryRepository,
            PlayerAlbumService albumService
    ) {
        this.articleWriteService = articleWriteService;
        this.categoryRepository = categoryRepository;
        this.albumService = albumService;
        this.contentPolicy = new ArticleContentPolicy();
    }

    public ArticleEditorView startEditing(String playerId, String playerName, boolean admin) {
        ArticleEditorSession session = new ArticleEditorSession(playerName, admin);
        sessionsByPlayerId.put(playerId, session);
        return ArticleEditorView.enterTitle();
    }

    public ArticleEditorView handleInput(String playerId, String input) throws IOException {
        ArticleEditorSession session = sessionsByPlayerId.get(playerId);
        if (session == null || !session.isActive()) {
            return ArticleEditorView.cancelled();
        }

        if (isCancelCommand(input)) {
            session.cancel();
            sessionsByPlayerId.remove(playerId);
            return ArticleEditorView.cancelled();
        }

        return switch (session.step()) {
            case ENTER_TITLE -> handleTitle(session, input);
            case ENTER_CATEGORY -> handleCategory(session, input);
            case ENTER_BODY -> handleBody(session, input);
            case ATTACH_IMAGE -> handleImageOrSkip(session, input);
            case REVIEW -> handleReview(playerId, session, input);
            default -> ArticleEditorView.cancelled();
        };
    }

    public ArticleEditorView attachCameraImage(
            String playerId,
            String imagePath,
            String caption
    ) {
        ArticleEditorSession session = sessionsByPlayerId.get(playerId);
        if (session == null || session.step() != ArticleEditorStep.ATTACH_IMAGE) {
            return ArticleEditorView.cancelled();
        }

        if (!contentPolicy.isImageSourceAllowed("camera_marker", session.isAdmin())) {
            return ArticleEditorView.error(
                    ArticleEditorStep.ATTACH_IMAGE,
                    "Kamera-Bilder sind für diesen Spieler nicht erlaubt.",
                    currentView(session)
            );
        }

        session.setImage(imagePath, caption, "camera_marker");
        return ArticleEditorView.review(session);
    }

    public boolean hasActiveSession(String playerId) {
        ArticleEditorSession session = sessionsByPlayerId.get(playerId);
        return session != null && session.isActive();
    }

    public void cancelSession(String playerId) {
        ArticleEditorSession session = sessionsByPlayerId.get(playerId);
        if (session != null) {
            session.cancel();
        }
        sessionsByPlayerId.remove(playerId);
    }

    public int activeSessionCount() {
        return (int) sessionsByPlayerId.values().stream()
                .filter(ArticleEditorSession::isActive)
                .count();
    }

    private ArticleEditorView handleTitle(ArticleEditorSession session, String input) throws IOException {
        if (input.isBlank()) {
            return ArticleEditorView.error(
                    ArticleEditorStep.ENTER_TITLE,
                    "Titel darf nicht leer sein.",
                    ArticleEditorView.enterTitle()
            );
        }
        session.setTitle(input.trim());
        return ArticleEditorView.enterCategory(session.title(), enabledCategoryIds());
    }

    private ArticleEditorView handleCategory(ArticleEditorSession session, String input) throws IOException {
        List<String> categories = enabledCategoryIds();
        String trimmed = input.trim().toLowerCase();

        if (!categories.contains(trimmed)) {
            return ArticleEditorView.error(
                    ArticleEditorStep.ENTER_CATEGORY,
                    "Unbekannte Kategorie. Verfügbar: " + String.join(", ", categories),
                    ArticleEditorView.enterCategory(session.title(), categories)
            );
        }

        session.setCategory(trimmed);
        return ArticleEditorView.enterBody(session.title(), session.categoryId());
    }

    private ArticleEditorView handleBody(ArticleEditorSession session, String input) throws IOException {
        if (input.isBlank()) {
            return ArticleEditorView.error(
                    ArticleEditorStep.ENTER_BODY,
                    "Artikeltext darf nicht leer sein.",
                    ArticleEditorView.enterBody(session.title(), session.categoryId())
            );
        }
        session.setBody(input.trim());

        List<PlayerPhoto> photos = albumService.listPhotos(session.playerName(), AlbumSortOrder.DATE);
        session.setAlbumSnapshot(photos);
        return ArticleEditorView.attachImageWithAlbum(
                session.title(), session.categoryId(), session.body(), photos
        );
    }

    private ArticleEditorView handleImageOrSkip(ArticleEditorSession session, String input) throws IOException {
        if (isSkipCommand(input)) {
            session.skipImage();
            return ArticleEditorView.review(session);
        }

        List<PlayerPhoto> snapshot = session.albumSnapshot();
        if (snapshot != null && !snapshot.isEmpty()) {
            try {
                int index = Integer.parseInt(input.trim()) - 1;
                if (index >= 0 && index < snapshot.size()) {
                    PlayerPhoto selected = snapshot.get(index);
                    session.setImage(selected.imagePath(), selected.name(), "camera_marker");
                    return ArticleEditorView.review(session);
                }
            } catch (NumberFormatException ignored) {
            }
        }

        List<PlayerPhoto> photos = albumService.listPhotos(session.playerName(), AlbumSortOrder.DATE);
        session.setAlbumSnapshot(photos);
        return ArticleEditorView.error(
                ArticleEditorStep.ATTACH_IMAGE,
                "Bitte eine Nummer aus dem Album wählen oder 'weiter' tippen.",
                ArticleEditorView.attachImageWithAlbum(
                        session.title(), session.categoryId(), session.body(), photos
                )
        );
    }

    private ArticleEditorView handleReview(
            String playerId,
            ArticleEditorSession session,
            String input
    ) throws IOException {
        if (isSubmitCommand(input)) {
            String draftId = articleWriteService.createDraft(new ArticleDraftRequest(
                    session.playerName(),
                    "unknown",
                    session.title(),
                    null,
                    session.categoryId(),
                    session.body(),
                    session.imagePath(),
                    session.imageCaption(),
                    session.imageSourceType()
            ));
            session.markSubmitted();
            sessionsByPlayerId.remove(playerId);
            return ArticleEditorView.submitted(draftId);
        }

        return ArticleEditorView.error(
                ArticleEditorStep.REVIEW,
                "Tippe 'einreichen' zum Speichern oder 'abbrechen'.",
                ArticleEditorView.review(session)
        );
    }

    private ArticleEditorView currentView(ArticleEditorSession session) {
        return ArticleEditorView.attachImageWithAlbum(
                session.title(), session.categoryId(), session.body(), session.albumSnapshot()
        );
    }

    private List<String> enabledCategoryIds() throws IOException {
        return categoryRepository.findEnabledCategories().stream()
                .map(c -> c.id())
                .toList();
    }

    private boolean isCancelCommand(String input) {
        String lower = input.trim().toLowerCase();
        return lower.equals("abbrechen") || lower.equals("cancel");
    }

    private boolean isSkipCommand(String input) {
        String lower = input.trim().toLowerCase();
        return lower.equals("weiter") || lower.equals("skip") || lower.equals("überspringen");
    }

    private boolean isSubmitCommand(String input) {
        String lower = input.trim().toLowerCase();
        return lower.equals("einreichen") || lower.equals("submit") || lower.equals("speichern");
    }
}
