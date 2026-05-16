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
        return paginate(baseTitle, blocks, template, 1);
    }

    public List<NewspaperVisualPage> paginate(
            String baseTitle,
            List<NewspaperVisualBlock> blocks,
            NewspaperLayoutTemplate template,
            int firstPageNumber
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
                pages.add(pageFor(
                        baseTitle,
                        firstPageNumber + pages.size(),
                        firstPageNumber,
                        currentBlocks
                ));
                currentBlocks = new ArrayList<>();
                currentWeight = 0;
            }

            currentBlocks.add(block);
            currentWeight += blockWeight;
        }

        if (!currentBlocks.isEmpty()) {
            pages.add(pageFor(
                    baseTitle,
                    firstPageNumber + pages.size(),
                    firstPageNumber,
                    currentBlocks
            ));
        }

        return pages;
    }

    public List<NewspaperVisualPage> paginateGrouped(
            String baseTitle,
            List<List<NewspaperVisualBlock>> blockGroups,
            NewspaperLayoutTemplate template,
            int firstPageNumber
    ) {
        if (blockGroups == null || blockGroups.isEmpty()) {
            return List.of();
        }

        NewspaperLayoutTemplate safeTemplate = template == null
                ? NewspaperLayoutTemplate.classicDoublePage()
                : template;
        int pageCapacity = safeTemplate.columnsPerPage() * safeTemplate.rowsPerPage();
        List<NewspaperVisualPage> pages = new ArrayList<>();
        List<NewspaperVisualBlock> currentBlocks = new ArrayList<>();
        int currentWeight = 0;
        boolean currentPageHasContinuation = false;

        for (List<NewspaperVisualBlock> group : blockGroups) {
            if (group == null || group.isEmpty()) {
                continue;
            }

            String continuationTitle = continuationTitleFor(group);
            boolean groupContinues = false;
            int groupWeight = group.stream()
                    .mapToInt(block -> layoutRules.weightFor(block, safeTemplate))
                    .sum();

            // Only keep an article together on a fresh page if:
            // - the current page is not a continuation (continuation pages fill greedily), and
            // - the current page is at least 50 % full (avoids large empty gaps before articles)
            boolean currentPageTooEmpty = currentWeight < (int) (pageCapacity * 0.5);
            boolean shouldKeepTogether = !currentPageHasContinuation
                    && !currentPageTooEmpty
                    && groupWeight <= pageCapacity
                    && !currentBlocks.isEmpty()
                    && currentWeight + groupWeight > pageCapacity;

            if (shouldKeepTogether) {
                pages.add(pageFor(
                        baseTitle,
                        firstPageNumber + pages.size(),
                        firstPageNumber,
                        currentBlocks
                ));
                currentBlocks = new ArrayList<>();
                currentWeight = 0;
                currentPageHasContinuation = false;
            }

            for (NewspaperVisualBlock block : group) {
                int blockWeight = layoutRules.weightFor(block, safeTemplate);
                if (!currentBlocks.isEmpty() && currentWeight + blockWeight > pageCapacity) {
                    pages.add(pageFor(
                            baseTitle,
                            firstPageNumber + pages.size(),
                            firstPageNumber,
                            currentBlocks
                    ));
                    currentBlocks = new ArrayList<>();
                    currentWeight = 0;
                    currentPageHasContinuation = false;
                    groupContinues = true;
                }

                if (groupContinues && currentBlocks.isEmpty()) {
                    NewspaperVisualBlock continuationBlock =
                            NewspaperVisualBlock.subheadline(
                                    continuationTitle == null
                                            ? "Fortsetzung"
                                            : "Fortsetzung: " + continuationTitle
                            );
                    currentBlocks.add(continuationBlock);
                    currentWeight += layoutRules.weightFor(continuationBlock, safeTemplate);
                    currentPageHasContinuation = true;
                    groupContinues = false;
                }

                currentBlocks.add(block);
                currentWeight += blockWeight;
            }
        }

        if (!currentBlocks.isEmpty()) {
            pages.add(pageFor(
                    baseTitle,
                    firstPageNumber + pages.size(),
                    firstPageNumber,
                    currentBlocks
            ));
        }

        return pages;
    }

    private String continuationTitleFor(List<NewspaperVisualBlock> group) {
        return group.stream()
                .filter(block -> block != null)
                .filter(block -> block.type() == NewspaperVisualBlockType.SUBHEADLINE)
                .map(NewspaperVisualBlock::content)
                .filter(this::hasText)
                .findFirst()
                .orElse(null);
    }

    private NewspaperVisualPage pageFor(
            String baseTitle,
            int pageNumber,
            int firstPageNumber,
            List<NewspaperVisualBlock> blocks
    ) {
        String title = baseTitle == null || baseTitle.isBlank()
                ? "AthenaPress"
                : baseTitle;

        if (pageNumber > firstPageNumber) {
            title = title + " - Seite " + pageNumber;
        }

        return NewspaperVisualPage.of(pageNumber, title, blocks);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
