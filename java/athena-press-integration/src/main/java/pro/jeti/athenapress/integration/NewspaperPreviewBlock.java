package pro.jeti.athenapress.integration;

public record NewspaperPreviewBlock(
        NewspaperVisualBlockType type,
        String content,
        String assetPath,
        int columnIndex,
        int rowStart,
        int rowSpan,
        int columnSpan,
        NewspaperBlockLayoutIntent layoutIntent
) {

    public NewspaperPreviewBlock {
        content = content == null ? "" : content;
        assetPath = assetPath == null ? "" : assetPath;
        columnIndex = Math.max(0, columnIndex);
        rowStart = Math.max(0, rowStart);
        rowSpan = Math.max(1, rowSpan);
        columnSpan = Math.max(1, columnSpan);
        layoutIntent = layoutIntent == null
                ? NewspaperBlockLayoutIntent.STANDARD
                : layoutIntent;
    }

    public NewspaperPreviewBlock(
            NewspaperVisualBlockType type,
            String content,
            String assetPath,
            int columnIndex,
            int rowStart,
            int rowSpan,
            int columnSpan
    ) {
        this(
                type,
                content,
                assetPath,
                columnIndex,
                rowStart,
                rowSpan,
                columnSpan,
                NewspaperBlockLayoutIntent.STANDARD
        );
    }

    public boolean hasAsset() {
        return !assetPath.isBlank();
    }
}
