package pro.jeti.athenapress.integration;

import java.util.List;

public class ArticleContentPolicy {

    private static final List<String> PLAYER_ALLOWED = List.of("camera_marker", "placeholder");
    private static final List<String> ADMIN_ALLOWED = List.of(
            "camera_marker", "placeholder", "uploaded", "screenshot", "external"
    );

    public boolean isImageSourceAllowed(String sourceType, boolean admin) {
        if (sourceType == null) {
            return false;
        }
        return allowedSourceTypes(admin).contains(sourceType);
    }

    public List<String> allowedSourceTypes(boolean admin) {
        return admin ? ADMIN_ALLOWED : PLAYER_ALLOWED;
    }

    public boolean requiresAdmin(String sourceType) {
        return sourceType != null && !PLAYER_ALLOWED.contains(sourceType);
    }
}
