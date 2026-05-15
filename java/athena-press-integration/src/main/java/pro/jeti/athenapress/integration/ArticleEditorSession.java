package pro.jeti.athenapress.integration;

public class ArticleEditorSession {

    private final String playerName;
    private final boolean admin;

    private ArticleEditorStep step = ArticleEditorStep.ENTER_TITLE;
    private String title;
    private String categoryId;
    private String body;
    private String imagePath;
    private String imageCaption;
    private String imageSourceType = "placeholder";
    private java.util.List<pro.jeti.athenapress.model.PlayerPhoto> albumSnapshot;

    public ArticleEditorSession(String playerName, boolean admin) {
        this.playerName = playerName;
        this.admin = admin;
    }

    public ArticleEditorStep step() { return step; }
    public String playerName() { return playerName; }
    public boolean isAdmin() { return admin; }
    public String title() { return title; }
    public String categoryId() { return categoryId; }
    public String body() { return body; }
    public String imagePath() { return imagePath; }
    public String imageCaption() { return imageCaption; }
    public String imageSourceType() { return imageSourceType; }
    public java.util.List<pro.jeti.athenapress.model.PlayerPhoto> albumSnapshot() { return albumSnapshot; }

    public void setAlbumSnapshot(java.util.List<pro.jeti.athenapress.model.PlayerPhoto> photos) {
        this.albumSnapshot = photos;
    }

    public boolean isActive() {
        return step != ArticleEditorStep.SUBMITTED && step != ArticleEditorStep.CANCELLED;
    }

    public void setTitle(String title) {
        this.title = title;
        this.step = ArticleEditorStep.ENTER_CATEGORY;
    }

    public void setCategory(String categoryId) {
        this.categoryId = categoryId;
        this.step = ArticleEditorStep.ENTER_BODY;
    }

    public void setBody(String body) {
        this.body = body;
        this.step = ArticleEditorStep.ATTACH_IMAGE;
    }

    public void setImage(String path, String caption, String sourceType) {
        this.imagePath = path;
        this.imageCaption = caption;
        this.imageSourceType = sourceType;
        this.step = ArticleEditorStep.REVIEW;
    }

    public void skipImage() {
        this.imagePath = "placeholders/no_image.png";
        this.imageCaption = null;
        this.imageSourceType = "placeholder";
        this.step = ArticleEditorStep.REVIEW;
    }

    public void markSubmitted() {
        this.step = ArticleEditorStep.SUBMITTED;
    }

    public void cancel() {
        this.step = ArticleEditorStep.CANCELLED;
    }
}
