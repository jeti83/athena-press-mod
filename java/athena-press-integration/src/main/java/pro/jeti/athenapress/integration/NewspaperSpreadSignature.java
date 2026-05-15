package pro.jeti.athenapress.integration;

public record NewspaperSpreadSignature(
        int spreadIndex,
        String label,
        String hint,
        int leftPageNumber,
        int rightPageNumber,
        boolean frontCover,
        boolean backCover
) {

    public NewspaperSpreadSignature {
        spreadIndex = Math.max(0, spreadIndex);
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
