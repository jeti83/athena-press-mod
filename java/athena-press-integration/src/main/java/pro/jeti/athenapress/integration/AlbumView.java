package pro.jeti.athenapress.integration;

import java.util.List;

import pro.jeti.athenapress.model.PlayerPhoto;
import pro.jeti.athenapress.service.AlbumSortOrder;

public record AlbumView(
        String playerName,
        AlbumSortOrder sortOrder,
        List<PlayerPhoto> photos,
        String message
) {

    public boolean isEmpty() {
        return photos == null || photos.isEmpty();
    }

    public static AlbumView of(String playerName, AlbumSortOrder sortOrder, List<PlayerPhoto> photos) {
        return new AlbumView(playerName, sortOrder, photos, null);
    }

    public static AlbumView withMessage(String playerName, AlbumSortOrder sortOrder,
                                        List<PlayerPhoto> photos, String message) {
        return new AlbumView(playerName, sortOrder, photos, message);
    }

    public static AlbumView empty(String playerName) {
        return new AlbumView(playerName, AlbumSortOrder.DATE, List.of(),
                "Dein Album ist leer. Nimm mit der Athena-Kamera Fotos auf.");
    }

    public String renderText() {
        if (isEmpty()) {
            return message != null ? message : "Album ist leer.";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("\n--- Album von ").append(playerName).append(" (").append(sortOrder.name().toLowerCase()).append(") ---\n");
        for (int i = 0; i < photos.size(); i++) {
            PlayerPhoto photo = photos.get(i);
            String fav = photo.favorite() ? " ★" : "";
            sb.append(i + 1).append(". ").append(photo.name()).append(fav)
              .append(" [").append(photo.id()).append("]\n");
        }
        if (message != null) {
            sb.append("\n").append(message).append("\n");
        }
        return sb.toString();
    }
}
