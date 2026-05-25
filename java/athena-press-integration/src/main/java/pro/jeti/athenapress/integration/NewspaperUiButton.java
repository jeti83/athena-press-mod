package pro.jeti.athenapress.integration;

public record NewspaperUiButton(
        String label,
        PlayerNewspaperUiCommand command,
        NewspaperUiButtonStyle style
) {

    public static NewspaperUiButton primary(String label, PlayerNewspaperUiCommand command) {
        return new NewspaperUiButton(label, command, NewspaperUiButtonStyle.PRIMARY);
    }

    public static NewspaperUiButton secondary(String label, PlayerNewspaperUiCommand command) {
        return new NewspaperUiButton(label, command, NewspaperUiButtonStyle.SECONDARY);
    }

    public static NewspaperUiButton danger(String label, PlayerNewspaperUiCommand command) {
        return new NewspaperUiButton(label, command, NewspaperUiButtonStyle.DANGER);
    }
}