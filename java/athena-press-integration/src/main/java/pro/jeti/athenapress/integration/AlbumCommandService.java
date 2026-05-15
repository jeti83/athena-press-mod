package pro.jeti.athenapress.integration;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pro.jeti.athenapress.model.PlayerPhoto;
import pro.jeti.athenapress.service.AlbumSortOrder;
import pro.jeti.athenapress.service.PlayerAlbumService;

public class AlbumCommandService {

    private final PlayerAlbumService albumService;
    private final Map<String, AlbumSortOrder> sortOrderByPlayer = new HashMap<>();

    public AlbumCommandService(PlayerAlbumService albumService) {
        this.albumService = albumService;
    }

    public AlbumView openAlbum(String playerName) throws IOException {
        AlbumSortOrder order = sortOrderByPlayer.getOrDefault(playerName, AlbumSortOrder.DATE);
        List<PlayerPhoto> photos = albumService.listPhotos(playerName, order);
        if (photos.isEmpty()) {
            return AlbumView.empty(playerName);
        }
        return AlbumView.of(playerName, order, photos);
    }

    public AlbumView handleCommand(String playerName, String[] args) throws IOException {
        if (args == null || args.length == 0) {
            return openAlbum(playerName);
        }

        String subCommand = args[0].toLowerCase();
        return switch (subCommand) {
            case "sortieren", "sort" -> handleSort(playerName, args);
            case "favorit", "favorite" -> handleFavorite(playerName, args);
            case "umbenennen", "rename" -> handleRename(playerName, args);
            case "loeschen", "löschen", "delete" -> handleDelete(playerName, args);
            default -> openAlbum(playerName);
        };
    }

    public AlbumView getAlbumForSelection(String playerName) throws IOException {
        AlbumSortOrder order = sortOrderByPlayer.getOrDefault(playerName, AlbumSortOrder.DATE);
        List<PlayerPhoto> photos = albumService.listPhotos(playerName, order);
        return AlbumView.of(playerName, order, photos);
    }

    private AlbumView handleSort(String playerName, String[] args) throws IOException {
        AlbumSortOrder order = args.length >= 2
                ? AlbumSortOrder.fromInput(args[1])
                : AlbumSortOrder.DATE;
        sortOrderByPlayer.put(playerName, order);
        List<PlayerPhoto> photos = albumService.listPhotos(playerName, order);
        return AlbumView.withMessage(playerName, order, photos,
                "Sortierung geändert zu: " + order.name().toLowerCase());
    }

    private AlbumView handleFavorite(String playerName, String[] args) throws IOException {
        if (args.length < 2) {
            return AlbumView.withMessage(playerName,
                    sortOrderByPlayer.getOrDefault(playerName, AlbumSortOrder.DATE),
                    albumService.listPhotos(playerName, AlbumSortOrder.DATE),
                    "Fehler: /album favorit <id>");
        }
        boolean isFav = albumService.toggleFavorite(playerName, args[1]);
        AlbumSortOrder order = sortOrderByPlayer.getOrDefault(playerName, AlbumSortOrder.DATE);
        return AlbumView.withMessage(playerName, order,
                albumService.listPhotos(playerName, order),
                isFav ? "Als Favorit markiert." : "Favorit entfernt.");
    }

    private AlbumView handleRename(String playerName, String[] args) throws IOException {
        if (args.length < 3) {
            return errorView(playerName, "Fehler: /album umbenennen <id> <neuer Name>");
        }
        String newName = String.join(" ", List.of(args).subList(2, args.length));
        boolean success = albumService.renamePhoto(playerName, args[1], newName);
        AlbumSortOrder order = sortOrderByPlayer.getOrDefault(playerName, AlbumSortOrder.DATE);
        return AlbumView.withMessage(playerName, order,
                albumService.listPhotos(playerName, order),
                success ? "Foto umbenannt in: " + newName : "Foto nicht gefunden.");
    }

    private AlbumView handleDelete(String playerName, String[] args) throws IOException {
        if (args.length < 2) {
            return errorView(playerName, "Fehler: /album loeschen <id>");
        }
        boolean success = albumService.deletePhoto(playerName, args[1]);
        AlbumSortOrder order = sortOrderByPlayer.getOrDefault(playerName, AlbumSortOrder.DATE);
        return AlbumView.withMessage(playerName, order,
                albumService.listPhotos(playerName, order),
                success ? "Foto gelöscht." : "Foto nicht gefunden.");
    }

    private AlbumView errorView(String playerName, String message) throws IOException {
        AlbumSortOrder order = sortOrderByPlayer.getOrDefault(playerName, AlbumSortOrder.DATE);
        return AlbumView.withMessage(playerName, order,
                albumService.listPhotos(playerName, order), message);
    }
}
