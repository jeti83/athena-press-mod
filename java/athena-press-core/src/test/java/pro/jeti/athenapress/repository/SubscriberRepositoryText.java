package pro.jeti.athenapress.repository;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import pro.jeti.athenapress.model.Subscriber;

class SubscriberRepositoryTest {

    @Test
    void shouldReadExistingSubscribers() throws Exception {
        Path projectRoot = Path.of(System.getProperty("user.dir"))
                .getParent()
                .getParent();

        Path athenaPressRoot = projectRoot.resolve("AthenaPress");

        SubscriberRepository repository = new SubscriberRepository(athenaPressRoot);

        List<Subscriber> subscribers = repository.findAll();

        assertFalse(subscribers.isEmpty(), "Expected at least one subscriber to be loaded.");

        Subscriber jeti = repository.findByPlayerName("Jeti");
        Subscriber hfJeti83 = repository.findByPlayerName("HF_jeti83");
        Subscriber testUser = repository.findByPlayerName("TestUser");

        assertNotNull(jeti, "Expected subscriber Jeti to exist.");
        assertNotNull(hfJeti83, "Expected subscriber HF_jeti83 to exist.");
        assertNotNull(testUser, "Expected subscriber TestUser to exist.");

        assertTrue(jeti.subscribed(), "Expected Jeti to be subscribed.");
        assertTrue(hfJeti83.subscribed(), "Expected HF_jeti83 to be subscribed.");
        assertFalse(testUser.subscribed(), "Expected TestUser to be inactive.");

        assertEquals("item_and_notification", jeti.deliveryMode());
        assertEquals("mailbox", hfJeti83.deliveryMode());

        assertTrue(jeti.unreadIssues().contains("issue_0002"));
        assertTrue(hfJeti83.unreadIssues().isEmpty());
        assertTrue(testUser.unreadIssues().isEmpty());
    }

    @Test
    void shouldListActiveAndInactiveSubscribers() throws Exception {
        Path projectRoot = Path.of(System.getProperty("user.dir"))
                .getParent()
                .getParent();

        Path athenaPressRoot = projectRoot.resolve("AthenaPress");

        SubscriberRepository repository = new SubscriberRepository(athenaPressRoot);

        List<Subscriber> activeSubscribers = repository.findActiveSubscribers();
        List<Subscriber> inactiveSubscribers = repository.findInactiveSubscribers();

        assertTrue(
                activeSubscribers.stream().anyMatch(subscriber -> "Jeti".equals(subscriber.playerName())),
                "Expected Jeti to be listed as active."
        );

        assertTrue(
                activeSubscribers.stream().anyMatch(subscriber -> "HF_jeti83".equals(subscriber.playerName())),
                "Expected HF_jeti83 to be listed as active."
        );

        assertTrue(
                inactiveSubscribers.stream().anyMatch(subscriber -> "TestUser".equals(subscriber.playerName())),
                "Expected TestUser to be listed as inactive."
        );
    }
}