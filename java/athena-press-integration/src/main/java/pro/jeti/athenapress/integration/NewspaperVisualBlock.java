package pro.jeti.athenapress.integration;

public record NewspaperVisualBlock(
        NewspaperVisualBlockType type,
        String content,
        String assetPath,
        int columnSpan,
        NewspaperImageRole imageRole
) {

    public NewspaperVisualBlock {
        columnSpan = columnSpan <= 0 ? 1 : columnSpan;
    }

    public NewspaperVisualBlock(
            NewspaperVisualBlockType type,
            String content,
            String assetPath,
            int columnSpan
    ) {
        this(type, content, assetPath, columnSpan, null);
    }

    public static NewspaperVisualBlock headline(String content) {
        return new NewspaperVisualBlock(
                NewspaperVisualBlockType.HEADLINE,
                content,
                null,
                2
        );
    }

    public static NewspaperVisualBlock subheadline(String content) {
        return new NewspaperVisualBlock(
                NewspaperVisualBlockType.SUBHEADLINE,
                content,
                null,
                2
        );
    }

    public static NewspaperVisualBlock bodyText(String content) {
        return new NewspaperVisualBlock(
                NewspaperVisualBlockType.BODY_TEXT,
                content,
                null,
                1
        );
    }

    public static NewspaperVisualBlock bodyText(String content, int columnSpan) {
        return new NewspaperVisualBlock(
                NewspaperVisualBlockType.BODY_TEXT,
                content,
                null,
                columnSpan
        );
    }

    public static NewspaperVisualBlock image(String assetPath, String caption) {
        return new NewspaperVisualBlock(
                NewspaperVisualBlockType.IMAGE,
                caption,
                assetPath,
                1,
                NewspaperImageRole.ARTICLE
        );
    }

    public static NewspaperVisualBlock image(
            String assetPath,
            String caption,
            int columnSpan
    ) {
        return new NewspaperVisualBlock(
                NewspaperVisualBlockType.IMAGE,
                caption,
                assetPath,
                columnSpan,
                NewspaperImageRole.ARTICLE
        );
    }

    public static NewspaperVisualBlock coverImage(
            String assetPath,
            String caption,
            int columnSpan
    ) {
        return new NewspaperVisualBlock(
                NewspaperVisualBlockType.IMAGE,
                caption,
                assetPath,
                columnSpan,
                NewspaperImageRole.COVER
        );
    }

    public static NewspaperVisualBlock quote(String content) {
        return new NewspaperVisualBlock(
                NewspaperVisualBlockType.QUOTE,
                content,
                null,
                1
        );
    }

    public static NewspaperVisualBlock caption(String content) {
        return new NewspaperVisualBlock(
                NewspaperVisualBlockType.CAPTION,
                content,
                null,
                1
        );
    }

    public static NewspaperVisualBlock caption(String content, int columnSpan) {
        return new NewspaperVisualBlock(
                NewspaperVisualBlockType.CAPTION,
                content,
                null,
                columnSpan
        );
    }

    public static NewspaperVisualBlock notice(String content) {
        return new NewspaperVisualBlock(
                NewspaperVisualBlockType.NOTICE,
                content,
                null,
                1
        );
    }

    public static NewspaperVisualBlock notice(String content, int columnSpan) {
        return new NewspaperVisualBlock(
                NewspaperVisualBlockType.NOTICE,
                content,
                null,
                columnSpan
        );
    }

    public static NewspaperVisualBlock advertisement(String content, String assetPath) {
        return new NewspaperVisualBlock(
                NewspaperVisualBlockType.ADVERTISEMENT,
                content,
                assetPath,
                1,
                NewspaperImageRole.ADVERTISEMENT
        );
    }

    public static NewspaperVisualBlock divider() {
        return new NewspaperVisualBlock(
                NewspaperVisualBlockType.DIVIDER,
                null,
                null,
                2
        );
    }
}
