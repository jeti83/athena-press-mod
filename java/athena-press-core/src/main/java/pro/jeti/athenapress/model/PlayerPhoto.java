package pro.jeti.athenapress.model;

import java.util.List;

public record PlayerPhoto(
        String id,
        String filename,
        String name,
        String capturedAt,
        boolean favorite,
        List<String> tags
) {

    public String imagePath() {
        return "uploaded/" + filename;
    }
}
