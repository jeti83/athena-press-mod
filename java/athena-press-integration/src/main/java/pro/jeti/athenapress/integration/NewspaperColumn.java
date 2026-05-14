package pro.jeti.athenapress.integration;

public record NewspaperColumn(
        int pageNumber,
        int columnIndex,
        int xUnits,
        int yUnits,
        int widthUnits,
        int heightUnits
) {

    public NewspaperColumn {
        pageNumber = Math.max(1, pageNumber);
        columnIndex = Math.max(0, columnIndex);
        xUnits = Math.max(0, xUnits);
        yUnits = Math.max(0, yUnits);
        widthUnits = Math.max(1, widthUnits);
        heightUnits = Math.max(1, heightUnits);
    }
}
