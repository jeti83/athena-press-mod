package pro.jeti.athenapress.integration;

import java.util.List;

public record NewspaperPageSection(
        NewspaperPageSectionType type,
        String title,
        List<NewspaperVisualBlock> blocks
) {

    public NewspaperPageSection {
        type = type == null ? NewspaperPageSectionType.MIXED_ARTICLES : type;
        title = title == null ? "" : title;
        blocks = blocks == null ? List.of() : List.copyOf(blocks);
    }

    public static NewspaperPageSection of(
            NewspaperPageSectionType type,
            String title,
            List<NewspaperVisualBlock> blocks
    ) {
        return new NewspaperPageSection(type, title, blocks);
    }

    public boolean hasContent() {
        return !blocks.isEmpty();
    }
}
