package pro.jeti.athenapress.integration;

import java.util.List;

public record NewspaperPageLayout(
        int pageNumber,
        String title,
        NewspaperLayoutTemplate template,
        NewspaperVisualDesignProfile designProfile,
        List<NewspaperColumn> columns,
        List<NewspaperContentPlacement> placements,
        List<NewspaperImagePlacement> imagePlacements
) {

    public NewspaperPageLayout {
        pageNumber = Math.max(1, pageNumber);
        title = title == null ? "" : title;
        template = template == null ? NewspaperLayoutTemplate.classicDoublePage() : template;
        designProfile = designProfile == null
                ? NewspaperVisualDesignProfile.athenaReadableNewspaper()
                : designProfile;
        columns = columns == null ? List.of() : List.copyOf(columns);
        placements = placements == null ? List.of() : List.copyOf(placements);
        imagePlacements = imagePlacements == null ? List.of() : List.copyOf(imagePlacements);
    }
}
