package pro.jeti.athenapress.integration;

public record NewspaperPreviewBlock(
        NewspaperVisualBlockType type,
        String content,
        String assetPath,
        int columnIndex,
        int rowStart,
        int rowSpan,
        int columnSpan
) {

    public NewspaperPreviewBlock {
        content = content == null ? "" : content;
        assetPath = assetPath == null ? "" : assetPath;
        columnIndex = Math.max(0, columnIndex);
        rowStart = Math.max(0, rowStart);
        rowSpan = Math.max(1, rowSpan);
        columnSpan = Math.max(1, columnSpan);
    }

    public boolean hasAsset() {
        return !assetPath.isBlank();
    }
}
