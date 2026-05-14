package pro.jeti.athenapress.integration;

public record NewspaperVisualDoublePage(
        NewspaperVisualPage leftPage,
        NewspaperVisualPage rightPage
) {

    public boolean hasLeftPage() {
        return leftPage != null;
    }

    public boolean hasRightPage() {
        return rightPage != null;
    }
}