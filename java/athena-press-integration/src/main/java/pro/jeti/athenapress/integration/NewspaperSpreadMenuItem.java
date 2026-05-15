package pro.jeti.athenapress.integration;

public record NewspaperSpreadMenuItem(
        int spreadIndex,
        String label,
        String hint,
        boolean current,
        PlayerNewspaperUiCommand command
) {

    public NewspaperSpreadMenuItem {
        spreadIndex = Math.max(0, spreadIndex);
        label = label == null || label.isBlank() ? "Doppelseite " + (spreadIndex + 1) : label;
        hint = hint == null ? "" : hint;
    }

    public boolean hasHint() {
        return !hint.isBlank();
    }
}
