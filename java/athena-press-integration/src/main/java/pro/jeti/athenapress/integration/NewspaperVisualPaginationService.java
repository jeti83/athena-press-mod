package pro.jeti.athenapress.integration;

import java.util.ArrayList;
import java.util.List;

public class NewspaperVisualPaginationService {

    public List<NewspaperVisualPage> paginate(
            String baseTitle,
            List<NewspaperVisualBlock> blocks,
            NewspaperLayoutTemplate template
    ) {
        if (blocks == null || blocks.isEmpty()) {
            return List.of();
        }

        NewspaperLayoutTemplate safeTemplate = template == null
                ? NewspaperLayoutTemplate.classicDoublePage()
                : template;
        int pageCapacity = safeTemplate.columnsPerPage() * safeTemplate.rowsPerPage();
        List<NewspaperVisualPage> pages = new ArrayList<>();
        List<NewspaperVisualBlock> currentBlocks = new ArrayList<>();
        int currentWeight = 0;

        for (NewspaperVisualBlock block : blocks) {
            int blockWeight = blockWeight(block, safeTemplate);
            if (!currentBlocks.isEmpty() && currentWeight + blockWeight > pageCapacity) {
                pages.add(pageFor(baseTitle, pages.size() + 1, currentBlocks));
                currentBlocks = new ArrayList<>();
                currentWeight = 0;
            }

            currentBlocks.add(block);
            currentWeight += blockWeight;
        }

        if (!currentBlocks.isEmpty()) {
            pages.add(pageFor(baseTitle, pages.size() + 1, currentBlocks));
        }

        return pages;
    }

    private NewspaperVisualPage pageFor(
            String baseTitle,
            int pageNumber,
            List<NewspaperVisualBlock> blocks
    ) {
        String title = baseTitle == null || baseTitle.isBlank()
                ? "AthenaPress"
                : baseTitle;

        if (pageNumber > 1) {
            title = title + " - Seite " + pageNumber;
        }

        return NewspaperVisualPage.of(pageNumber, title, blocks);
    }

    private int blockWeight(
            NewspaperVisualBlock block,
            NewspaperLayoutTemplate template
    ) {
        if (block == null || block.type() == null) {
            return 1;
        }

        int rowWeight = switch (block.type()) {
            case HEADLINE -> 4;
            case SUBHEADLINE -> 2;
            case BODY_TEXT -> 5;
            case IMAGE -> 8;
            case CAPTION -> 1;
            case QUOTE -> 4;
            case NOTICE -> 3;
            case ADVERTISEMENT -> 6;
            case DIVIDER -> 1;
        };

        int columnSpan = Math.min(block.columnSpan(), template.columnsPerPage());
        return rowWeight * columnSpan;
    }
}
