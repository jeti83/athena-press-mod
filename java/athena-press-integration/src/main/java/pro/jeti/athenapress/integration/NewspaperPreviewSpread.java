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

    public NewspaperSpreadSignature signature() {
        return new NewspaperSpreadSignature(
                spreadIndex,
                labelFor(),
                hintFor(),
                pageNumber(leftPage),
                pageNumber(rightPage),
                hasRole(NewspaperPageRole.FRONT_COVER),
                hasRole(NewspaperPageRole.BACK_COVER)
        );
    }

    private String labelFor() {
        if (hasRole(NewspaperPageRole.FRONT_COVER)) {
            return "Titelseite";
        }

        if (hasLeftPage() && hasRightPage()) {
            return "Seiten " + leftPage.pageNumber() + "-" + rightPage.pageNumber();
        }

        NewspaperPreviewPage page = hasLeftPage() ? leftPage : rightPage;
        return page == null ? "Doppelseite " + (spreadIndex + 1) : "Seite " + page.pageNumber();
    }

    private String hintFor() {
        NewspaperPreviewPage page = hasLeftPage() ? leftPage : rightPage;
        if (page == null) {
            return "";
        }

        String subheadline = firstContentFor(page, NewspaperVisualBlockType.SUBHEADLINE);
        if (!subheadline.isBlank()) {
            return subheadline;
        }

        String headline = firstContentFor(page, NewspaperVisualBlockType.HEADLINE);
        return headline.isBlank() ? page.title() : headline;
    }

    private boolean hasRole(NewspaperPageRole role) {
        return (hasLeftPage() && leftPage.role() == role)
                || (hasRightPage() && rightPage.role() == role);
    }

    private int pageNumber(NewspaperPreviewPage page) {
        return page == null ? 0 : page.pageNumber();
    }

    private String firstContentFor(
            NewspaperPreviewPage page,
            NewspaperVisualBlockType blockType
    ) {
        return page.blocks().stream()
                .filter(block -> block.type() == blockType)
                .map(NewspaperPreviewBlock::content)
                .filter(content -> content != null && !content.isBlank())
                .findFirst()
                .orElse("");
    }
}
