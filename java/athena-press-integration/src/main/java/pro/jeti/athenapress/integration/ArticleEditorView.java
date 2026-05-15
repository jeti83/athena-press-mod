package pro.jeti.athenapress.integration;

import java.util.List;

public record ArticleEditorView(
        ArticleEditorStep step,
        String prompt,
        String currentTitle,
        String currentCategory,
        String currentBody,
        String currentImage,
        List<String> availableCategories,
        String message,
        String draftId
) {

    public boolean isComplete() {
        return step == ArticleEditorStep.SUBMITTED;
    }

    public boolean isCancelled() {
        return step == ArticleEditorStep.CANCELLED;
    }

    public static ArticleEditorView enterTitle() {
        return new ArticleEditorView(
                ArticleEditorStep.ENTER_TITLE,
                "Gib einen Titel für deinen Artikel ein:",
                null, null, null, null, List.of(), null, null
        );
    }

    public static ArticleEditorView enterCategory(String title, List<String> categories) {
        return new ArticleEditorView(
                ArticleEditorStep.ENTER_CATEGORY,
                "Wähle eine Kategorie: " + String.join(", ", categories),
                title, null, null, null, categories, null, null
        );
    }

    public static ArticleEditorView enterBody(String title, String categoryId) {
        return new ArticleEditorView(
                ArticleEditorStep.ENTER_BODY,
                "Schreibe den Artikeltext:",
                title, categoryId, null, null, List.of(), null, null
        );
    }

    public static ArticleEditorView attachImage(String title, String categoryId, String body, boolean admin) {
        String hint = admin
                ? "Hänge ein Bild an (camera_marker / placeholder / uploaded) oder tippe 'weiter':"
                : "Hänge ein Kamerafoto an oder tippe 'weiter' um fortzufahren:";
        return new ArticleEditorView(
                ArticleEditorStep.ATTACH_IMAGE,
                hint,
                title, categoryId, body, null, List.of(), null, null
        );
    }

    public static ArticleEditorView attachImageWithAlbum(
            String title, String categoryId, String body,
            java.util.List<pro.jeti.athenapress.model.PlayerPhoto> photos
    ) {
        StringBuilder prompt = new StringBuilder("Wähle ein Foto aus deinem Album (Nummer eingeben) oder tippe 'weiter':\n");
        if (photos == null || photos.isEmpty()) {
            prompt.append("(Album ist leer — nimm zuerst ein Foto mit der Kamera auf)");
        } else {
            for (int i = 0; i < photos.size(); i++) {
                var p = photos.get(i);
                prompt.append(i + 1).append(". ").append(p.name());
                if (p.favorite()) prompt.append(" ★");
                prompt.append("\n");
            }
        }
        return new ArticleEditorView(
                ArticleEditorStep.ATTACH_IMAGE,
                prompt.toString().trim(),
                title, categoryId, body, null, List.of(), null, null
        );
    }

    public static ArticleEditorView review(ArticleEditorSession session) {
        String imageSummary = session.imagePath() != null
                ? session.imagePath() + " (" + session.imageSourceType() + ")"
                : "kein Bild";
        return new ArticleEditorView(
                ArticleEditorStep.REVIEW,
                "Artikel prüfen. Tippe 'einreichen' zum Speichern oder 'abbrechen':",
                session.title(),
                session.categoryId(),
                session.body(),
                imageSummary,
                List.of(),
                null,
                null
        );
    }

    public static ArticleEditorView submitted(String draftId) {
        return new ArticleEditorView(
                ArticleEditorStep.SUBMITTED,
                null, null, null, null, null, List.of(),
                "Artikel gespeichert als Entwurf: " + draftId + ". Ein Admin kann ihn veröffentlichen.",
                draftId
        );
    }

    public static ArticleEditorView cancelled() {
        return new ArticleEditorView(
                ArticleEditorStep.CANCELLED,
                null, null, null, null, null, List.of(),
                "Artikel-Erstellung abgebrochen.",
                null
        );
    }

    public static ArticleEditorView error(ArticleEditorStep step, String errorMessage, ArticleEditorView previous) {
        return new ArticleEditorView(
                step,
                previous.prompt(),
                previous.currentTitle(),
                previous.currentCategory(),
                previous.currentBody(),
                previous.currentImage(),
                previous.availableCategories(),
                "Fehler: " + errorMessage,
                null
        );
    }
}
