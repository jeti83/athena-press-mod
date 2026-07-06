package pro.jeti.athenapress.service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import pro.jeti.athenapress.model.PlayerPhoto;
import pro.jeti.athenapress.repository.PlayerAlbumRepository;

class PlayerAlbumServiceTest {

    @TempDir
    Path tempDir;

    private PlayerAlbumService service;

    @BeforeEach
    void setUp() {
        service = new PlayerAlbumService(new PlayerAlbumRepository(tempDir));
    }

    @Test
    void addPhotoCreatesEntry() throws IOException {
        PlayerPhoto photo = service.addPhoto("Jeti", "cam_Jeti_001.png", "Marktplatz");

        assertNotNull(photo);
        assertEquals("Marktplatz", photo.name());
        assertFalse(photo.favorite());
        assertEquals(1, service.photoCount("Jeti"));
    }

    @Test
    void listPhotosReturnsSortedByDate() throws IOException {
        service.addPhoto("Jeti", "cam_001.png", "Zweites Bild");
        service.addPhoto("Jeti", "cam_002.png", "Erstes Bild");

        List<PlayerPhoto> photos = service.listPhotos("Jeti", AlbumSortOrder.DATE);

        assertEquals(2, photos.size());
    }

    @Test
    void listPhotosSortedByName() throws IOException {
        service.addPhoto("Jeti", "cam_001.png", "Brunnen");
        service.addPhoto("Jeti", "cam_002.png", "Apfelbaum");

        List<PlayerPhoto> photos = service.listPhotos("Jeti", AlbumSortOrder.NAME);

        assertEquals("Apfelbaum", photos.get(0).name());
        assertEquals("Brunnen", photos.get(1).name());
    }

    @Test
    void toggleFavoriteFlipsFlag() throws IOException {
        PlayerPhoto photo = service.addPhoto("Jeti", "cam_001.png", "Foto");
        assertFalse(photo.favorite());

        boolean nowFav = service.toggleFavorite("Jeti", photo.id());
        assertTrue(nowFav);

        boolean nowUnfav = service.toggleFavorite("Jeti", photo.id());
        assertFalse(nowUnfav);
    }

    @Test
    void listFavoritesSortsFavoritesFirst() throws IOException {
        service.addPhoto("Jeti", "cam_001.png", "Normal");
        PlayerPhoto p2 = service.addPhoto("Jeti", "cam_002.png", "Favorit");
        service.toggleFavorite("Jeti", p2.id());

        List<PlayerPhoto> photos = service.listPhotos("Jeti", AlbumSortOrder.FAVORITE);

        assertTrue(photos.get(0).favorite());
    }

    @Test
    void renamePhotoUpdatesName() throws IOException {
        PlayerPhoto photo = service.addPhoto("Jeti", "cam_001.png", "Alt");

        boolean success = service.renamePhoto("Jeti", photo.id(), "Neu");

        assertTrue(success);
        PlayerPhoto updated = service.findPhoto("Jeti", photo.id());
        assertEquals("Neu", updated.name());
    }

    @Test
    void deletePhotoRemovesEntry() throws IOException {
        PlayerPhoto photo = service.addPhoto("Jeti", "cam_001.png", "Löschen");
        assertEquals(1, service.photoCount("Jeti"));

        boolean success = service.deletePhoto("Jeti", photo.id());

        assertTrue(success);
        assertEquals(0, service.photoCount("Jeti"));
    }

    @Test
    void albumIsolatedPerPlayer() throws IOException {
        service.addPhoto("Jeti", "cam_j.png", "Jetis Foto");
        service.addPhoto("Mira_Baut", "cam_m.png", "Miras Foto");

        assertEquals(1, service.photoCount("Jeti"));
        assertEquals(1, service.photoCount("Mira_Baut"));
    }
}
