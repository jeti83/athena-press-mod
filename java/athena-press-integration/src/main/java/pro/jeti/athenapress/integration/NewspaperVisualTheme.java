package pro.jeti.athenapress.integration;

public record NewspaperVisualTheme(
        String themeId,
        String displayName,
        int columns
) {

    public NewspaperVisualTheme {
        if (themeId == null || themeId.isBlank()) {
            themeId = "default";
        }

        if (displayName == null || displayName.isBlank()) {
            displayName = "AthenaPress";
        }

        columns = columns <= 0 ? 2 : columns;
    }

    public static NewspaperVisualTheme defaultTheme() {
        return new NewspaperVisualTheme(
                "default",
                "AthenaPress",
                2
        );
    }
}