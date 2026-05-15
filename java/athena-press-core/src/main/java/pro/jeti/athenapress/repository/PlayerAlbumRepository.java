package pro.jeti.athenapress.repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import pro.jeti.athenapress.model.PlayerAlbum;
import pro.jeti.athenapress.model.PlayerPhoto;

public class PlayerAlbumRepository {

    private final Path playersRoot;
    private final ObjectMapper objectMapper;

    public PlayerAlbumRepository(Path athenaPressRoot) {
        this.playersRoot = athenaPressRoot.resolve("players");
        this.objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    }

    public PlayerAlbum loadAlbum(String playerName) throws IOException {
        Path albumFile = albumFilePath(playerName);
        if (!Files.exists(albumFile)) {
            return new PlayerAlbum(playerName, new ArrayList<>());
        }
        return objectMapper.readValue(albumFile.toFile(), PlayerAlbum.class);
    }

    public void saveAlbum(PlayerAlbum album) throws IOException {
        Path playerDir = playersRoot.resolve(album.playerName());
        Files.createDirectories(playerDir);

        ObjectNode root = objectMapper.createObjectNode();
        root.put("playerName", album.playerName());

        ArrayNode photosNode = objectMapper.createArrayNode();
        for (PlayerPhoto photo : album.photos()) {
            ObjectNode photoNode = objectMapper.createObjectNode();
            photoNode.put("id", photo.id());
            photoNode.put("filename", photo.filename());
            photoNode.put("name", photo.name());
            photoNode.put("capturedAt", photo.capturedAt());
            photoNode.put("favorite", photo.favorite());
            ArrayNode tags = objectMapper.createArrayNode();
            if (photo.tags() != null) {
                photo.tags().forEach(tags::add);
            }
            photoNode.set("tags", tags);
            photosNode.add(photoNode);
        }
        root.set("photos", photosNode);

        objectMapper.writeValue(albumFilePath(album.playerName()).toFile(), root);
    }

    public String generatePhotoId(String playerName) throws IOException {
        PlayerAlbum album = loadAlbum(playerName);
        int max = album.photos().stream()
                .map(PlayerPhoto::id)
                .filter(id -> id.startsWith("photo_"))
                .mapToInt(id -> {
                    try {
                        return Integer.parseInt(id.substring("photo_".length()));
                    } catch (NumberFormatException ignored) {
                        return 0;
                    }
                })
                .max()
                .orElse(0);
        return String.format("photo_%04d", max + 1);
    }

    public String generateFilename(String playerName) {
        String timestamp = OffsetDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return "cam_" + playerName + "_" + timestamp + ".png";
    }

    private Path albumFilePath(String playerName) {
        return playersRoot.resolve(playerName).resolve("album.json");
    }
}
