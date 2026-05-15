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

        if (hasRole(NewspaperPageRole.BACK_COVER)) {
            return "Rückseite";
        }

        if (hasLeftPage() && hasRightPage()) {
            return "Seiten " + leftPage.pageNumber() + "-" + rightPage.pageNumber();
        }

        NewspaperPreviewPage page = hasLeftPage() ? leftPage : rightPage;
        return page == null ? "Doppelseite " + (spreadIndex + 1) : "Seite " + page.pageNumber();
    }

    private String hintFor() {
        String editorialSectionHint = editorialSectionHint();
        if (!editorialSectionHint.isBlank()) {
            return editorialSectionHint;
        }

        NewspaperPreviewPage page = pageForHint();
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

    private String editorialSectionHint() {
        for (NewspaperPreviewPage page : new NewspaperPreviewPage[] {leftPage, rightPage}) {
            if (page == null) {
                continue;
            }

            String hint = page.blocks().stream()
                    .filter(block -> block.type() == NewspaperVisualBlockType.SUBHEADLINE)
                    .filter(block -> block.layoutIntent() == NewspaperBlockLayoutIntent.SHORT_NOTICE
                            || block.layoutIntent() == NewspaperBlockLayoutIntent.MEMORIAL)
                    .map(NewspaperPreviewBlock::content)
                    .filter(content -> content != null && !content.isBlank())
                    .findFirst()
                    .orElse("");
            if (!hint.isBlank()) {
                return hint;
            }
        }

        return "";
    }

    private NewspaperPreviewPage pageForHint() {
        if (hasRightPage() && rightPage.role() == NewspaperPageRole.BACK_COVER) {
            return rightPage;
        }

        if (hasLeftPage() && leftPage.role() == NewspaperPageRole.BACK_COVER) {
            return leftPage;
        }

        return hasLeftPage() ? leftPage : rightPage;
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
