package pro.jeti.athenapress.integration;

import java.util.List;

public record NewspaperVisualPage(
        int pageNumber,
        String title,
        List<NewspaperVisualBlock> blocks
) {

    public NewspaperVisualPage {
        pageNumber = Math.max(pageNumber, 1);
        title = title == null ? "" : title;
        blocks = blocks == null ? List.of() : List.copyOf(blocks);
    }

    public static NewspaperVisualPage of(
            int pageNumber,
            String title,
            List<NewspaperVisualBlock> blocks
    ) {
        return new NewspaperVisualPage(pageNumber, title, blocks);
    }
}