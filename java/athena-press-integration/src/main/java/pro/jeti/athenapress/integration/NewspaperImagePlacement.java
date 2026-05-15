package pro.jeti.athenapress.integration;

public record NewspaperImagePlacement(
        String assetPath,
        String caption,
        NewspaperImageRole role,
        int pageNumber,
        int columnIndex,
        int rowStart,
        int rowSpan,
        int columnSpan
) {

    public NewspaperImagePlacement {
        assetPath = assetPath == null ? "" : assetPath;
        caption = caption == null ? "" : caption;
        role = role == null ? NewspaperImageRole.ARTICLE : role;
        pageNumber = Math.max(1, pageNumber);
        columnIndex = Math.max(0, columnIndex);
        rowStart = Math.max(0, rowStart);
        rowSpan = Math.max(1, rowSpan);
        columnSpan = Math.max(1, columnSpan);
    }
}
