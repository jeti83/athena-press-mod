package pro.jeti.athenapress.integration;

public interface HytaleCameraUiBridge {

    void hideHud(String playerId);

    void showHud(String playerId);

    void triggerScreenshot(String playerId);
}
