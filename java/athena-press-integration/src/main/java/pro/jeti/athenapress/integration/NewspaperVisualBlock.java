package pro.jeti.athenapress.integration;

public record NewspaperVisualBlock(
        NewspaperVisualBlockType type,
        String content,
        String assetPath,
        int columnSpan,
        NewspaperImageRole imageRole,
        NewspaperBlockLayoutIntent layoutIntent
) {

    public NewspaperVisualBlock {
        columnSpan = columnSpan <= 0 ? 1 : columnSpan;
        layoutIntent = layoutIntent == null
                ? NewspaperBlockLayoutIntent.STANDARD
                : layoutIntent;
    }

    public NewspaperVisualBlock(
            NewspaperVisualBlockType type,
            String content,
            String assetPath,
            int columnSpan
    ) {
        this(type, content, assetPath, columnSpan, null, NewspaperBlockLayoutIntent.STANDARD);
    }

    public NewspaperVisualBlock(
            NewspaperVisualBlockType type,
            String content,
            String assetPath,
            int columnSpan,
            NewspaperImageRole imageRole
    ) {
        this(type, content, assetPath, columnSpan, imageRole, NewspaperBlockLayoutIntent.STANDARD);
    }

    public static NewspaperVisualBlock headline(String content) {
        return new NewspaperVisualBlock(NewspaperVisualBlockType.HEADLINE, content, null, 2);
    }

    public static NewspaperVisualBlock coverHeadline(String content) {
        return coverBlock(NewspaperVisualBlockType.HEADLINE, content, null, 2, null);
    }

    public static NewspaperVisualBlock subheadline(String content) {
        return new NewspaperVisualBlock(NewspaperVisualBlockType.SUBHEADLINE, content, null, 2);
    }

    public static NewspaperVisualBlock memorialSubheadline(String content) {
        return memorialBlock(NewspaperVisualBlockType.SUBHEADLINE, content, null, 2, null);
    }

    public static NewspaperVisualBlock coverSubheadline(String content) {
        return coverBlock(NewspaperVisualBlockType.SUBHEADLINE, content, null, 2, null);
    }

    public static NewspaperVisualBlock bodyText(String content) {
        return new NewspaperVisualBlock(NewspaperVisualBlockType.BODY_TEXT, content, null, 1);
    }

    public static NewspaperVisualBlock bodyText(String content, int columnSpan) {
        return new NewspaperVisualBlock(NewspaperVisualBlockType.BODY_TEXT, content, null, columnSpan);
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
        return coverBlock(
                NewspaperVisualBlockType.IMAGE,
                caption,
                assetPath,
                columnSpan,
                NewspaperImageRole.COVER
        );
    }

    public static NewspaperVisualBlock quote(String content) {
        return new NewspaperVisualBlock(NewspaperVisualBlockType.QUOTE, content, null, 1);
    }

    public static NewspaperVisualBlock memorialQuote(String content) {
        return memorialBlock(NewspaperVisualBlockType.QUOTE, content, null, 1, null);
    }

    public static NewspaperVisualBlock caption(String content) {
        return new NewspaperVisualBlock(NewspaperVisualBlockType.CAPTION, content, null, 1);
    }

    public static NewspaperVisualBlock caption(String content, int columnSpan) {
        return new NewspaperVisualBlock(NewspaperVisualBlockType.CAPTION, content, null, columnSpan);
    }

    public static NewspaperVisualBlock coverCaption(String content, int columnSpan) {
        return coverBlock(NewspaperVisualBlockType.CAPTION, content, null, columnSpan, null);
    }

    public static NewspaperVisualBlock notice(String content) {
        return new NewspaperVisualBlock(NewspaperVisualBlockType.NOTICE, content, null, 1);
    }

    public static NewspaperVisualBlock shortNotice(String content) {
        return shortNoticeBlock(NewspaperVisualBlockType.NOTICE, content, null, 1, null);
    }

    public static NewspaperVisualBlock notice(String content, int columnSpan) {
        return new NewspaperVisualBlock(NewspaperVisualBlockType.NOTICE, content, null, columnSpan);
    }

    public static NewspaperVisualBlock coverNotice(String content, int columnSpan) {
        return coverBlock(NewspaperVisualBlockType.NOTICE, content, null, columnSpan, null);
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

    public static NewspaperVisualBlock backPageAdvertisement(String content, String assetPath) {
        return backPageAdvertisement(content, assetPath, 1);
    }

    public static NewspaperVisualBlock backPageAdvertisement(
            String content,
            String assetPath,
            int columnSpan
    ) {
        return backPageBlock(
                NewspaperVisualBlockType.ADVERTISEMENT,
                content,
                assetPath,
                columnSpan,
                NewspaperImageRole.ADVERTISEMENT
        );
    }

    public static NewspaperVisualBlock divider() {
        return new NewspaperVisualBlock(NewspaperVisualBlockType.DIVIDER, null, null, 2);
    }

    public static NewspaperVisualBlock coverDivider() {
        return coverBlock(NewspaperVisualBlockType.DIVIDER, null, null, 2, null);
    }

    public static NewspaperVisualBlock backPageNotice(String content) {
        return backPageNotice(content, 1);
    }

    public static NewspaperVisualBlock backPageNotice(String content, int columnSpan) {
        return backPageBlock(NewspaperVisualBlockType.NOTICE, content, null, columnSpan, null);
    }

    public static NewspaperVisualBlock backPageDivider() {
        return backPageBlock(NewspaperVisualBlockType.DIVIDER, null, null, 2, null);
    }

    private static NewspaperVisualBlock coverBlock(
            NewspaperVisualBlockType type,
            String content,
            String assetPath,
            int columnSpan,
            NewspaperImageRole imageRole
    ) {
        return new NewspaperVisualBlock(
                type,
                content,
                assetPath,
                columnSpan,
                imageRole,
                NewspaperBlockLayoutIntent.COVER
        );
    }

    private static NewspaperVisualBlock backPageBlock(
            NewspaperVisualBlockType type,
            String content,
            String assetPath,
            int columnSpan,
            NewspaperImageRole imageRole
    ) {
        return new NewspaperVisualBlock(
                type,
                content,
                assetPath,
                columnSpan,
                imageRole,
                NewspaperBlockLayoutIntent.BACK_PAGE
        );
    }

    private static NewspaperVisualBlock shortNoticeBlock(
            NewspaperVisualBlockType type,
            String content,
            String assetPath,
            int columnSpan,
            NewspaperImageRole imageRole
    ) {
        return new NewspaperVisualBlock(
                type,
                content,
                assetPath,
                columnSpan,
                imageRole,
                NewspaperBlockLayoutIntent.SHORT_NOTICE
        );
    }

    private static NewspaperVisualBlock memorialBlock(
            NewspaperVisualBlockType type,
            String content,
            String assetPath,
            int columnSpan,
            NewspaperImageRole imageRole
    ) {
        return new NewspaperVisualBlock(
                type,
                content,
                assetPath,
                columnSpan,
                imageRole,
                NewspaperBlockLayoutIntent.MEMORIAL
        );
    }
}
