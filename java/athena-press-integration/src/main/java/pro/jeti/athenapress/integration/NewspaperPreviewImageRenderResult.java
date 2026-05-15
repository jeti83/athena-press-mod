package pro.jeti.athenapress.integration;

import java.nio.file.Path;
import java.util.List;

public record NewspaperPreviewImageRenderResult(
        String issueId,
        List<Path> spreadImages
) {

    public NewspaperPreviewImageRenderResult {
        issueId = issueId == null ? "" : issueId;
        spreadImages = spreadImages == null ? List.of() : List.copyOf(spreadImages);
    }

    public boolean hasImages() {
        return !spreadImages.isEmpty();
    }
}
