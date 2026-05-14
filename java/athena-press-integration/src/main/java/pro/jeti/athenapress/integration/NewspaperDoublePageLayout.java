package pro.jeti.athenapress.integration;

import java.util.List;

public record NewspaperDoublePageLayout(
        int spreadIndex,
        NewspaperPageLayout leftPage,
        NewspaperPageLayout rightPage,
        NewspaperPageRole leftRole,
        NewspaperPageRole rightRole,
        List<NewspaperUiButton> navigationButtons
) {

    public NewspaperDoublePageLayout {
        spreadIndex = Math.max(0, spreadIndex);
        leftRole = leftRole == null ? roleFor(leftPage, true) : leftRole;
        rightRole = rightRole == null ? roleFor(rightPage, false) : rightRole;
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

    public boolean isCoverSpread() {
        return leftRole == NewspaperPageRole.FRONT_COVER
                || rightRole == NewspaperPageRole.FRONT_COVER;
    }

    private static NewspaperPageRole roleFor(
            NewspaperPageLayout page,
            boolean left
    ) {
        if (page == null) {
            return NewspaperPageRole.SINGLE_PAGE;
        }

        if (page.pageNumber() == 1) {
            return NewspaperPageRole.FRONT_COVER;
        }

        return left ? NewspaperPageRole.LEFT_INNER : NewspaperPageRole.RIGHT_INNER;
    }
}
