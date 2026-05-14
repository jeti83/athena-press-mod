package pro.jeti.athenapress.integration;

import java.util.List;

public record NewspaperPreviewSpread(
        int spreadIndex,
        NewspaperPreviewPage leftPage,
        NewspaperPreviewPage rightPage,
        List<NewspaperUiButton> navigationButtons
) {

    public NewspaperPreviewSpread {
        spreadIndex = Math.max(0, spreadIndex);
        navigationButtons = navigationButtons == null
                ? List.of()
                : List.copyOf(navigationButtons);
    }

    public boolean hasLeftPage() {
        return leftPage != null;
    }

    public boolean hasRightPage() {
        return rightPage != null;
    }

    public boolean isSinglePageSpread() {
        return hasLeftPage() != hasRightPage();
    }
}
