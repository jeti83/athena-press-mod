package pro.jeti.athenapress.integration;

import java.util.ArrayList;
import java.util.List;

public class NewspaperVisualPaginationService {

    private final NewspaperBlockLayoutRuleSet layoutRules;

    public NewspaperVisualPaginationService() {
        this(NewspaperBlockLayoutRuleSet.defaultRules());
    }

    public NewspaperVisualPaginationService(NewspaperBlockLayoutRuleSet layoutRules) {
        this.layoutRules = layoutRules == null
                ? NewspaperBlockLayoutRuleSet.defaultRules()
                : layoutRules;
    }

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
            int blockWeight = layoutRules.weightFor(block, safeTemplate);
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
}
