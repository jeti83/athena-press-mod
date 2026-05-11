package pro.jeti.athenapress.service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import pro.jeti.athenapress.model.DeliveryTarget;
import pro.jeti.athenapress.model.Issue;
import pro.jeti.athenapress.model.Subscriber;

public class DeliveryService {

    private final PressService pressService;

    public DeliveryService(Path athenaPressRoot) {
        this.pressService = new PressService(athenaPressRoot);
    }

    public List<Subscriber> findRecipientsForIssue(String issueId) throws IOException {
        Issue issue = pressService.findIssueById(issueId);

        if (issue == null || !"published".equals(issue.status())) {
            return List.of();
        }

        return pressService.findActiveSubscribers();
    }

    public boolean shouldDeliverIssueToPlayer(String playerName, String issueId) throws IOException {
        Issue issue = pressService.findIssueById(issueId);
        Subscriber subscriber = pressService.findSubscriberByPlayerName(playerName);

        if (issue == null || subscriber == null) {
            return false;
        }

        if (!"published".equals(issue.status())) {
            return false;
        }

        return subscriber.subscribed();
    }

    public String getDeliveryModeForPlayer(String playerName) throws IOException {
        Subscriber subscriber = pressService.findSubscriberByPlayerName(playerName);

        if (subscriber == null || !subscriber.subscribed()) {
            return null;
        }

        return subscriber.deliveryMode();
    }

    public boolean isIssueUnreadForPlayer(String playerName, String issueId) throws IOException {
        return pressService.isIssueUnreadForPlayer(playerName, issueId);
    }

    public List<DeliveryTarget> createDeliveryPlan(String issueId) throws IOException {
    List<Subscriber> recipients = findRecipientsForIssue(issueId);
    List<DeliveryTarget> deliveryTargets = new ArrayList<>();

    for (Subscriber subscriber : recipients) {
        deliveryTargets.add(new DeliveryTarget(
                issueId,
                subscriber.playerName(),
                subscriber.playerUuid(),
                subscriber.deliveryMode(),
                isIssueUnreadForPlayer(subscriber.playerName(), issueId)
        ));
    }

    return deliveryTargets;
    }
}
