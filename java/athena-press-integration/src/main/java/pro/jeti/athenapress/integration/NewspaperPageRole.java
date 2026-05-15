package pro.jeti.athenapress.integration;

public enum NewspaperPageRole {
    FRONT_COVER("Titelseite"),
    LEFT_INNER("Innenseite"),
    RIGHT_INNER("Innenseite"),
    BACK_COVER("Rückseite"),
    SINGLE_PAGE("Einzelseite");

    private final String displayName;

    NewspaperPageRole(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    public boolean isCover() {
        return this == FRONT_COVER || this == BACK_COVER;
    }
}
