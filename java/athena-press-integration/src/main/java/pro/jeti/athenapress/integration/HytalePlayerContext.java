package pro.jeti.athenapress.integration;

public record HytalePlayerContext(
        String playerId,
        String playerName
) {

    public HytalePlayerContext {
        if (playerId == null || playerId.isBlank()) {
            throw new IllegalArgumentException("playerId must not be blank");
        }

        playerName = playerName == null || playerName.isBlank()
                ? playerId
                : playerName;
    }
}