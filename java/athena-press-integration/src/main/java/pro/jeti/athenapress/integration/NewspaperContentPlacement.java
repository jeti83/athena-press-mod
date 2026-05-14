package pro.jeti.athenapress.integration;

public record NewspaperContentPlacement(
        NewspaperVisualBlock block,
        int pageNumber,
        int columnIndex,
        int rowStart,
        int rowSpan,
        int columnSpan
) {

    public NewspaperContentPlacement {
        pageNumber = Math.max(1, pageNumber);
        columnIndex = Math.max(0, columnIndex);
        rowStart = Math.max(0, rowStart);
        rowSpan = Math.max(1, rowSpan);
        columnSpan = Math.max(1, columnSpan);
    }

    public NewspaperVisualBlockType blockType() {
        return block == null ? null : block.type();
    }
}
