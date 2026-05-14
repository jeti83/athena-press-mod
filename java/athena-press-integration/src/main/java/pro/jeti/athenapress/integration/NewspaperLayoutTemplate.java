package pro.jeti.athenapress.integration;

public record NewspaperLayoutTemplate(
        String templateId,
        String displayName,
        int pageWidthUnits,
        int pageHeightUnits,
        int marginUnits,
        int gutterUnits,
        int columnsPerPage,
        int rowsPerPage
) {

    public NewspaperLayoutTemplate {
        templateId = templateId == null || templateId.isBlank()
                ? "classic_double_page"
                : templateId;
        displayName = displayName == null || displayName.isBlank()
                ? "Klassische Zeitungsdoppelseite"
                : displayName;
        pageWidthUnits = pageWidthUnits <= 0 ? 100 : pageWidthUnits;
        pageHeightUnits = pageHeightUnits <= 0 ? 140 : pageHeightUnits;
        marginUnits = Math.max(0, marginUnits);
        gutterUnits = Math.max(0, gutterUnits);
        columnsPerPage = columnsPerPage <= 0 ? 2 : columnsPerPage;
        rowsPerPage = rowsPerPage <= 0 ? 24 : rowsPerPage;
    }

    public static NewspaperLayoutTemplate classicDoublePage() {
        return new NewspaperLayoutTemplate(
                "classic_double_page",
                "Klassische Zeitungsdoppelseite",
                100,
                140,
                6,
                4,
                2,
                24
        );
    }

    public int usableWidthUnits() {
        return Math.max(1, pageWidthUnits - (marginUnits * 2));
    }

    public int usableHeightUnits() {
        return Math.max(1, pageHeightUnits - (marginUnits * 2));
    }
}
