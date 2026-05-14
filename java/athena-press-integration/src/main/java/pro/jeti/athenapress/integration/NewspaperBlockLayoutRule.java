package pro.jeti.athenapress.integration;

public record NewspaperBlockLayoutRule(
        NewspaperVisualBlockType blockType,
        int defaultRowSpan,
        int featuredRowSpan,
        int minimumColumnSpan,
        boolean prefersFullWidth
) {

    public NewspaperBlockLayoutRule {
        defaultRowSpan = Math.max(1, defaultRowSpan);
        featuredRowSpan = Math.max(defaultRowSpan, featuredRowSpan);
        minimumColumnSpan = Math.max(1, minimumColumnSpan);
    }

    public int rowSpanFor(
            NewspaperVisualBlock block,
            NewspaperLayoutTemplate template
    ) {
        if (isFeatured(block, template)) {
            return featuredRowSpan;
        }

        return defaultRowSpan;
    }

    public int columnSpanFor(
            NewspaperVisualBlock block,
            NewspaperLayoutTemplate template
    ) {
        int columnsPerPage = template == null ? 2 : template.columnsPerPage();

        if (prefersFullWidth) {
            return columnsPerPage;
        }

        int requestedColumnSpan = block == null ? minimumColumnSpan : block.columnSpan();
        return Math.min(columnsPerPage, Math.max(minimumColumnSpan, requestedColumnSpan));
    }

    private boolean isFeatured(
            NewspaperVisualBlock block,
            NewspaperLayoutTemplate template
    ) {
        if (block == null || template == null) {
            return false;
        }

        return block.columnSpan() >= template.columnsPerPage();
    }
}
