package pro.jeti.athenapress.service;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import pro.jeti.athenapress.model.DeliveryTarget;
import pro.jeti.athenapress.model.Subscriber;

class DeliveryServiceTest {

    private DeliveryService createService() {
        Path projectRoot = Path.of(System.getProperty("user.dir"))
                .getParent()
                .getParent();

        Path athenaPressRoot = projectRoot.resolve("AthenaPress");

        return new DeliveryService(athenaPressRoot);
    }

    @Test
    void shouldFindRecipientsForPublishedIssue0002() throws Exception {
        DeliveryService service = createService();

        List<Subscriber> recipients = service.findRecipientsForIssue("issue_0002");

        assertTrue(
                recipients.stream().anyMatch(subscriber -> "Jeti".equals(subscriber.playerName())),
                "Expected Jeti to receive issue_0002."
        );

        assertTrue(
                recipients.stream().anyMatch(subscriber -> "HF_jeti83".equals(subscriber.playerName())),
                "Expected HF_jeti83 to receive issue_0002."
        );

        assertFalse(
                recipients.stream().anyMatch(subscriber -> "TestUser".equals(subscriber.playerName())),
                "Expected TestUser not to receive issue_0002."
        );
    }

    @Test
    void shouldCheckWhetherIssueShouldBeDeliveredToPlayer() throws Exception {
        DeliveryService service = createService();

        assertTrue(service.shouldDeliverIssueToPlayer("Jeti", "issue_0002"));
        assertTrue(service.shouldDeliverIssueToPlayer("HF_jeti83", "issue_0002"));
        assertFalse(service.shouldDeliverIssueToPlayer("TestUser", "issue_0002"));
        assertFalse(service.shouldDeliverIssueToPlayer("UnknownPlayer", "issue_0002"));
        assertFalse(service.shouldDeliverIssueToPlayer("Jeti", "issue_unknown"));
    }

    @Test
    void shouldReturnDeliveryModeForActivePlayer() throws Exception {
        DeliveryService service = createService();

        assertEquals("item_and_notification", service.getDeliveryModeForPlayer("Jeti"));
        assertEquals("mailbox", service.getDeliveryModeForPlayer("HF_jeti83"));
        assertNull(service.getDeliveryModeForPlayer("TestUser"));
        assertNull(service.getDeliveryModeForPlayer("UnknownPlayer"));
    }

    @Test
    void shouldDetectUnreadIssueForPlayer() throws Exception {
        DeliveryService service = createService();

        assertTrue(service.isIssueUnreadForPlayer("Jeti", "issue_0002"));
        assertFalse(service.isIssueUnreadForPlayer("HF_jeti83", "issue_0002"));
        assertFalse(service.isIssueUnreadForPlayer("TestUser", "issue_0002"));
    }

    @Test
    void shouldCreateDeliveryPlanForPublishedIssue0002() throws Exception {
        DeliveryService service = createService();

        List<DeliveryTarget> deliveryPlan = service.createDeliveryPlan("issue_0002");

        assertEquals(2, deliveryPlan.size(), "Expected two delivery targets for issue_0002.");

        assertTrue(
                deliveryPlan.stream().anyMatch(target ->
                     "issue_0002".equals(target.issueId())
                                && "Jeti".equals(target.playerName())
                                && "item_and_notification".equals(target.deliveryMode())
                                && target.unread()
            ),
            "Expected delivery target for Jeti with unread issue_0002."
        );

        assertTrue(
                deliveryPlan.stream().anyMatch(target ->
                        "issue_0002".equals(target.issueId())
                                && "HF_jeti83".equals(target.playerName())
                                && "mailbox".equals(target.deliveryMode())
                                && !target.unread()
                ),
                "Expected delivery target for HF_jeti83 with read issue_0002."
        );

        assertFalse(
                deliveryPlan.stream().anyMatch(target -> "TestUser".equals(target.playerName())),
                "Expected TestUser not to be included in the delivery plan."
        );
    }
}