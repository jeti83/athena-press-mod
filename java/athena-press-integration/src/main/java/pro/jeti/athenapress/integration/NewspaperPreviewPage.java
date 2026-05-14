package pro.jeti.athenapress.integration;

import java.util.List;

public record NewspaperPreviewPage(
        int pageNumber,
        String title,
        NewspaperPageRole role,
        NewspaperVisualDesignProfile designProfile,
        List<NewspaperPreviewBlock> blocks
) {

    public NewspaperPreviewPage {
        pageNumber = Math.max(1, pageNumber);
        title = title == null ? "" : title;
        role = role == null ? NewspaperPageRole.SINGLE_PAGE : role;
        designProfile = designProfile == null
                ? NewspaperVisualDesignProfile.athenaReadableNewspaper()
                : designProfile;
        blocks = blocks == null ? List.of() : List.copyOf(blocks);
    }

    public boolean hasBlocks() {
        return !blocks.isEmpty();
    }
}
