package pro.jeti.athenapress.service;

public enum AlbumSortOrder {
    DATE,
    NAME,
    FAVORITE;

    public static AlbumSortOrder fromInput(String input) {
        if (input == null) return DATE;
        return switch (input.trim().toLowerCase()) {
            case "name" -> NAME;
            case "favorit", "favorite", "favoriten" -> FAVORITE;
            default -> DATE;
        };
    }
}
