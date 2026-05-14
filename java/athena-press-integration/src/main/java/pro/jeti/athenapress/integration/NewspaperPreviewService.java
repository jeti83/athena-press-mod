package pro.jeti.athenapress.integration;

import java.util.List;

public class NewspaperPreviewService {

    private final NewspaperDoublePageCompositionService doublePageCompositionService;

    public NewspaperPreviewService() {
        this(new NewspaperDoublePageCompositionService());
    }

    public NewspaperPreviewService(
            NewspaperDoublePageCompositionService doublePageCompositionService
    ) {
        this.doublePageCompositionService = doublePageCompositionService == null
                ? new NewspaperDoublePageCompositionService()
                : doublePageCompositionService;
    }

    public NewspaperPreviewIssue createPreview(NewspaperVisualIssue visualIssue) {
        if (visualIssue == null) {
            return new NewspaperPreviewIssue(
                    null,
                    "AthenaPress",
                    NewspaperVisualTheme.defaultTheme(),
                    NewspaperVisualDesignProfile.athenaReadableNewspaper(),
                    List.of()
            );
        }

        List<NewspaperDoublePageLayout> layouts =
                doublePageCompositionService.compose(visualIssue);
        NewspaperVisualDesignProfile designProfile = layouts.stream()
                .filter(NewspaperDoublePageLayout::hasLeftPage)
                .map(layout -> layout.leftPage().designProfile())
                .findFirst()
                .orElse(NewspaperVisualDesignProfile.athenaReadableNewspaper());

        List<NewspaperPreviewSpread> spreads = layouts.stream()
                .map(this::toPreviewSpread)
                .toList();

        return new NewspaperPreviewIssue(
                visualIssue.issueId(),
                visualIssue.title(),
                visualIssue.theme(),
                designProfile,
                spreads
        );
    }

    private NewspaperPreviewSpread toPreviewSpread(NewspaperDoublePageLayout layout) {
        return new NewspaperPreviewSpread(
                layout.spreadIndex(),
                toPreviewPage(layout.leftPage(), layout.leftRole()),
                toPreviewPage(layout.rightPage(), layout.rightRole()),
                layout.navigationButtons()
        );
    }

    private NewspaperPreviewPage toPreviewPage(
            NewspaperPageLayout pageLayout,
            NewspaperPageRole role
    ) {
        if (pageLayout == null) {
            return null;
        }

        return new NewspaperPreviewPage(
                pageLayout.pageNumber(),
                pageLayout.title(),
                role,
                pageLayout.designProfile(),
                pageLayout.placements().stream()
                        .map(this::toPreviewBlock)
                        .toList()
        );
    }

    private NewspaperPreviewBlock toPreviewBlock(NewspaperContentPlacement placement) {
        NewspaperVisualBlock block = placement.block();

        return new NewspaperPreviewBlock(
                placement.blockType(),
                block == null ? "" : block.content(),
                block == null ? "" : block.assetPath(),
                placement.columnIndex(),
                placement.rowStart(),
                placement.rowSpan(),
                placement.columnSpan()
        );
    }
}
