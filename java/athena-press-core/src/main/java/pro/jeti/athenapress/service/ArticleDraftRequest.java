package pro.jeti.athenapress.service;

public record ArticleDraftRequest(
        String playerName,
        String playerUuid,
        String title,
        String subtitle,
        String categoryId,
        String body,
        String imagePath,
        String imageCaption,
        String imageSourceType
) {

    public static ArticleDraftRequest of(
            String playerName,
            String title,
            String categoryId,
            String body
    ) {
        return new ArticleDraftRequest(
                playerName,
                "unknown",
                title,
                null,
                categoryId,
                body,
                "placeholders/no_image.png",
                null,
                "placeholder"
        );
    }
}
