package pro.jeti.athenapress.integration;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class NewspaperVisualRenderer {

    private final NewspaperBlockLayoutRuleSet layoutRules;

    public NewspaperVisualRenderer() {
        this(NewspaperBlockLayoutRuleSet.defaultRules());
    }

    public NewspaperVisualRenderer(NewspaperBlockLayoutRuleSet layoutRules) {
        this.layoutRules = layoutRules == null
                ? NewspaperBlockLayoutRuleSet.defaultRules()
                : layoutRules;
    }

    public List<NewspaperPageLayout> render(
            NewspaperVisualIssue issue,
            NewspaperLayoutTemplate template
    ) {
        if (issue == null || !issue.hasPages()) {
            return List.of();
        }

        NewspaperLayoutTemplate safeTemplate = template == null
                ? NewspaperLayoutTemplate.classicDoublePage()
                : template;

        return issue.pages().stream()
                .map(page -> renderPage(page, safeTemplate))
                .toList();
    }

    private NewspaperPageLayout renderPage(
            NewspaperVisualPage page,
            NewspaperLayoutTemplate template
    ) {
        List<NewspaperColumn> columns = columnsFor(page.pageNumber(), template);
        List<NewspaperContentPlacement> placements = new ArrayList<>();
        List<NewspaperImagePlacement> imagePlacements = new ArrayList<>();
        int[] rowCursors = new int[template.columnsPerPage()];

        for (NewspaperVisualBlock block : page.blocks()) {
            int rowSpan = layoutRules.rowSpanFor(block, template);
            int columnSpan = layoutRules.columnSpanFor(block, template);
            int columnIndex = columnIndexFor(rowCursors, columnSpan);
            int rowStart = maxCursor(rowCursors, columnIndex, columnSpan);

            NewspaperContentPlacement placement = new NewspaperContentPlacement(
                    block,
                    page.pageNumber(),
                    columnIndex,
                    rowStart,
                    rowSpan,
                    columnSpan
            );

            placements.add(placement);

            if (block.type() == NewspaperVisualBlockType.IMAGE
                    || block.type() == NewspaperVisualBlockType.ADVERTISEMENT) {
                imagePlacements.add(new NewspaperImagePlacement(
                        block.assetPath(),
                        block.content(),
                        page.pageNumber(),
                        columnIndex,
                        rowStart,
                        rowSpan,
                        columnSpan
                ));
            }

            for (int index = columnIndex; index < columnIndex + columnSpan; index++) {
                rowCursors[index] = rowStart + rowSpan;
            }
        }

        return new NewspaperPageLayout(
                page.pageNumber(),
                page.title(),
                template,
                columns,
                placements,
                imagePlacements
        );
    }

    private List<NewspaperColumn> columnsFor(
            int pageNumber,
            NewspaperLayoutTemplate template
    ) {
        List<NewspaperColumn> columns = new ArrayList<>();
        int availableWidth = template.usableWidthUnits()
                - (template.gutterUnits() * (template.columnsPerPage() - 1));
        int columnWidth = Math.max(1, availableWidth / template.columnsPerPage());

        for (int index = 0; index < template.columnsPerPage(); index++) {
            int x = template.marginUnits() + (index * (columnWidth + template.gutterUnits()));
            columns.add(new NewspaperColumn(
                    pageNumber,
                    index,
                    x,
                    template.marginUnits(),
                    columnWidth,
                    template.usableHeightUnits()
            ));
        }

        return columns;
    }

    private int columnIndexFor(int[] rowCursors, int columnSpan) {
        if (columnSpan >= rowCursors.length) {
            return 0;
        }

        return java.util.stream.IntStream.range(0, rowCursors.length - columnSpan + 1)
                .boxed()
                .min(Comparator.comparingInt(index -> maxCursor(rowCursors, index, columnSpan)))
                .orElse(0);
    }

    private int maxCursor(int[] rowCursors, int columnIndex, int columnSpan) {
        int cursor = 0;
        for (int index = columnIndex; index < columnIndex + columnSpan; index++) {
            cursor = Math.max(cursor, rowCursors[index]);
        }
        return cursor;
    }

}
