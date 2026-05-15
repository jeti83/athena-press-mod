package pro.jeti.athenapress.integration;

public class PlayerNewspaperVisualInputDispatcher {

    private final PlayerNewspaperVisualUiController visualUiController;
    private final PlayerNewspaperVisualInputMapper inputMapper;

    public PlayerNewspaperVisualInputDispatcher(
            PlayerNewspaperVisualUiController visualUiController
    ) {
        this(visualUiController, new PlayerNewspaperVisualInputMapper());
    }

    public PlayerNewspaperVisualInputDispatcher(
            PlayerNewspaperVisualUiController visualUiController,
            PlayerNewspaperVisualInputMapper inputMapper
    ) {
        if (visualUiController == null) {
            throw new IllegalArgumentException("visualUiController must not be null");
        }

        if (inputMapper == null) {
            throw new IllegalArgumentException("inputMapper must not be null");
        }

        this.visualUiController = visualUiController;
        this.inputMapper = inputMapper;
    }

    public void dispatch(PlayerNewspaperInputEvent event) {
        if (event == null) {
            return;
        }

        PlayerNewspaperUiCommand command = inputMapper.toUiCommand(event);

        if (command == null) {
            visualUiController.showCurrentSpread(event.playerId());
            return;
        }

        visualUiController.handleCommand(event.playerId(), command);
    }
}
