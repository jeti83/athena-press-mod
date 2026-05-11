package pro.jeti.athenapress.model;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Subscriber(
        String playerName,
        String playerUuid,
        boolean subscribed,
        String deliveryMode,

        String subscribedAt,
        String updatedAt,
        String unsubscribedAt,

        String lastReceivedIssueId,
        String lastDeliveryMode,
        String lastDeliveredAt,

        String lastReadIssueId,
        String lastReadAt,

        List<String> unreadIssues,

        Map<String, Object> metadata
) {
}