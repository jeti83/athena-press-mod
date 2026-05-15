package pro.jeti.athenapress.integration;

public record NewspaperSpreadStatus(
        int spreadIndex,
        int totalSpreadCount,
        String label,
        String hint,
        int leftPageNumber,
        int rightPageNumber,
        boolean frontCover,
        boolean backCover,
        boolean hasSpreadMenu
) {

    public NewspaperSpreadStatus {
        spreadIndex = Math.max(0, spreadIndex);
        totalSpreadCount = Math.max(0, totalSpreadCount);
        label = label == null || label.isBlank() ? "Doppelseite " + (spreadIndex + 1) : label;
        hint = hint == null ? "" : hint;
        leftPageNumber = Math.max(0, leftPageNumber);
        rightPageNumber = Math.max(0, rightPageNumber);
    }

    public boolean hasHint() {
        return !hint.isBlank();
    }

    public boolean hasLeftPage() {
        return leftPageNumber > 0;
    }

    public boolean hasRightPage() {
        return rightPageNumber > 0;
    }
}
