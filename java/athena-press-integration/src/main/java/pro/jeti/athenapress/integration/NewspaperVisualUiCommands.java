package pro.jeti.athenapress.integration;

public final class NewspaperVisualUiCommands {

    public static final String CURRENT_SPREAD = "visual_current_spread";
    public static final String NEXT_SPREAD = "visual_next_spread";
    public static final String PREVIOUS_SPREAD = "visual_previous_spread";

    private NewspaperVisualUiCommands() {
    }

    public static PlayerNewspaperUiCommand currentSpread() {
        return PlayerNewspaperUiCommand.custom(CURRENT_SPREAD, null);
    }

    public static PlayerNewspaperUiCommand nextSpread() {
        return PlayerNewspaperUiCommand.custom(NEXT_SPREAD, null);
    }

    public static PlayerNewspaperUiCommand previousSpread() {
        return PlayerNewspaperUiCommand.custom(PREVIOUS_SPREAD, null);
    }

    public static PlayerNewspaperUiCommand currentSpread(String value) {
        return PlayerNewspaperUiCommand.custom(CURRENT_SPREAD, value);
    }

    public static PlayerNewspaperUiCommand nextSpread(String value) {
        return PlayerNewspaperUiCommand.custom(NEXT_SPREAD, value);
    }

    public static PlayerNewspaperUiCommand previousSpread(String value) {
        return PlayerNewspaperUiCommand.custom(PREVIOUS_SPREAD, value);
    }
}
