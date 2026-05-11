package pro.jeti.athenapress.repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import pro.jeti.athenapress.model.Subscriber;

public class SubscriberRepository {

    private final Path subscribersFile;
    private final ObjectMapper objectMapper;

    public SubscriberRepository(Path athenaPressRoot) {
        this.subscribersFile = athenaPressRoot
                .resolve("subscriptions")
                .resolve("subscribers.json");
        this.objectMapper = new ObjectMapper();
    }

    public List<Subscriber> findAll() throws IOException {
        if (!Files.exists(subscribersFile)) {
            return List.of();
        }

        SubscribersFile file = objectMapper.readValue(
                subscribersFile.toFile(),
                SubscribersFile.class
        );

        if (file.subscribers() == null) {
            return List.of();
        }

        return file.subscribers();
    }

    public List<Subscriber> findActiveSubscribers() throws IOException {
        return findAll().stream()
                .filter(Subscriber::subscribed)
                .toList();
    }

    public List<Subscriber> findInactiveSubscribers() throws IOException {
        return findAll().stream()
                .filter(subscriber -> !subscriber.subscribed())
                .toList();
    }

    public Subscriber findByPlayerName(String playerName) throws IOException {
        for (Subscriber subscriber : findAll()) {
            if (playerName.equalsIgnoreCase(subscriber.playerName())) {
                return subscriber;
            }
        }

        return null;
    }

    private record SubscribersFile(
            List<Subscriber> subscribers
    ) {
    }
}