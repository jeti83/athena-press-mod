package pro.jeti.athenapress.integration;

public class PlayerNewspaperInputDispatcher {

    private final PlayerNewspaperUiController uiController;
    private final PlayerNewspaperInputMapper inputMapper;

    public PlayerNewspaperInputDispatcher(
            PlayerNewspaperUiController uiController
    ) {
        this(uiController, new PlayerNewspaperInputMapper());
    }

    public PlayerNewspaperInputDispatcher(
            PlayerNewspaperUiController uiController,
            PlayerNewspaperInputMapper inputMapper
    ) {
        if (uiController == null) {
            throw new IllegalArgumentException("uiController must not be null");
        }

        if (inputMapper == null) {
            throw new IllegalArgumentException("inputMapper must not be null");
        }

        this.uiController = uiController;
        this.inputMapper = inputMapper;
    }

    public void dispatch(PlayerNewspaperInputEvent event) {
        if (event == null) {
            return;
        }

        PlayerNewspaperUiCommand command = inputMapper.toUiCommand(event);

        if (command == null) {
            uiController.handleCommand(
                    event.playerId(),
                    PlayerNewspaperUiCommand.showOverview()
            );
            return;
        }

        uiController.handleCommand(event.playerId(), command);
    }
}