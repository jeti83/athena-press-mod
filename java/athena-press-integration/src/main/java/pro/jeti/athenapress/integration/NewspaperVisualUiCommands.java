package pro.jeti.athenapress.integration;

public final class NewspaperVisualUiCommands {

    public static final String CURRENT_SPREAD = "visual_current_spread";
    public static final String NEXT_SPREAD = "visual_next_spread";
    public static final String PREVIOUS_SPREAD = "visual_previous_spread";
    public static final String SELECT_SPREAD = "visual_select_spread";

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

    public static PlayerNewspaperUiCommand selectSpread(int spreadIndex) {
        return PlayerNewspaperUiCommand.custom(
                SELECT_SPREAD,
                Integer.toString(Math.max(0, spreadIndex))
        );
    }

    public static PlayerNewspaperUiCommand selectSpread(String spreadIndex) {
        return PlayerNewspaperUiCommand.custom(SELECT_SPREAD, spreadIndex);
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
