package pro.jeti.athenapress.model;

import java.util.List;

public record PlayerAlbum(
        String playerName,
        List<PlayerPhoto> photos
) {

    public boolean isEmpty() {
        return photos == null || photos.isEmpty();
    }

    public int size() {
        return photos == null ? 0 : photos.size();
    }
}
