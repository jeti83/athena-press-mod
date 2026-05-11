package pro.jeti.athenapress.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record DeliveryTarget(
        String issueId,
        String playerName,
        String playerUuid,
        String deliveryMode,
        boolean unread
) {
}