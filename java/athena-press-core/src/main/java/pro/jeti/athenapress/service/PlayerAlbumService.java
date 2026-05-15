package pro.jeti.athenapress.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import pro.jeti.athenapress.model.PlayerAlbum;
import pro.jeti.athenapress.model.PlayerPhoto;
import pro.jeti.athenapress.repository.PlayerAlbumRepository;

public class PlayerAlbumService {

    private final PlayerAlbumRepository albumRepository;

    public PlayerAlbumService(PlayerAlbumRepository albumRepository) {
        this.albumRepository = albumRepository;
    }

    public PlayerPhoto addPhoto(
            String playerName,
            String filename,
            String displayName
    ) throws IOException {
        String id = albumRepository.generatePhotoId(playerName);
        String now = java.time.OffsetDateTime.now()
                .format(java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        PlayerPhoto photo = new PlayerPhoto(id, filename, displayName, now, false, List.of());

        PlayerAlbum album = albumRepository.loadAlbum(playerName);
        List<PlayerPhoto> updated = new ArrayList<>(album.photos());
        updated.add(photo);
        albumRepository.saveAlbum(new PlayerAlbum(playerName, updated));

        return photo;
    }

    public List<PlayerPhoto> listPhotos(String playerName, AlbumSortOrder sortOrder) throws IOException {
        PlayerAlbum album = albumRepository.loadAlbum(playerName);
        return sorted(album.photos(), sortOrder);
    }

    public PlayerPhoto findPhoto(String playerName, String photoId) throws IOException {
        return albumRepository.loadAlbum(playerName).photos().stream()
                .filter(p -> photoId.equals(p.id()))
                .findFirst()
                .orElse(null);
    }

    public boolean toggleFavorite(String playerName, String photoId) throws IOException {
        PlayerAlbum album = albumRepository.loadAlbum(playerName);
        List<PlayerPhoto> updated = album.photos().stream()
                .map(p -> p.id().equals(photoId)
                        ? new PlayerPhoto(p.id(), p.filename(), p.name(), p.capturedAt(), !p.favorite(), p.tags())
                        : p)
                .toList();
        albumRepository.saveAlbum(new PlayerAlbum(playerName, new ArrayList<>(updated)));

        return updated.stream()
                .filter(p -> p.id().equals(photoId))
                .map(PlayerPhoto::favorite)
                .findFirst()
                .orElse(false);
    }

    public boolean renamePhoto(String playerName, String photoId, String newName) throws IOException {
        if (newName == null || newName.isBlank()) {
            return false;
        }
        PlayerAlbum album = albumRepository.loadAlbum(playerName);
        boolean found = album.photos().stream().anyMatch(p -> p.id().equals(photoId));
        if (!found) return false;

        List<PlayerPhoto> updated = album.photos().stream()
                .map(p -> p.id().equals(photoId)
                        ? new PlayerPhoto(p.id(), p.filename(), newName.trim(), p.capturedAt(), p.favorite(), p.tags())
                        : p)
                .toList();
        albumRepository.saveAlbum(new PlayerAlbum(playerName, new ArrayList<>(updated)));
        return true;
    }

    public boolean deletePhoto(String playerName, String photoId) throws IOException {
        PlayerAlbum album = albumRepository.loadAlbum(playerName);
        List<PlayerPhoto> updated = album.photos().stream()
                .filter(p -> !p.id().equals(photoId))
                .toList();

        if (updated.size() == album.size()) {
            return false;
        }
        albumRepository.saveAlbum(new PlayerAlbum(playerName, new ArrayList<>(updated)));
        return true;
    }

    public String registerNewCapture(String playerName, String displayName) throws IOException {
        String filename = albumRepository.generateFilename(playerName);
        addPhoto(playerName, filename, displayName != null ? displayName : filename);
        return filename;
    }

    public int photoCount(String playerName) throws IOException {
        return albumRepository.loadAlbum(playerName).size();
    }

    private List<PlayerPhoto> sorted(List<PlayerPhoto> photos, AlbumSortOrder order) {
        if (photos == null) return List.of();
        return switch (order) {
            case NAME -> photos.stream()
                    .sorted(Comparator.comparing(p -> p.name() == null ? "" : p.name()))
                    .toList();
            case FAVORITE -> photos.stream()
                    .sorted(Comparator.comparing(PlayerPhoto::favorite).reversed()
                            .thenComparing(p -> p.capturedAt() == null ? "" : p.capturedAt(),
                                    Comparator.reverseOrder()))
                    .toList();
            default -> photos.stream()
                    .sorted(Comparator.comparing(
                            p -> p.capturedAt() == null ? "" : p.capturedAt(),
                            Comparator.reverseOrder()))
                    .toList();
        };
    }
}
