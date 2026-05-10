package pro.jeti.athenapress.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Subscriber(
        String name,
        String displayName,
        boolean active,
        String deliveryMode,

        String subscribedAt,
        String deactivatedAt,
        String reactivatedAt,

        List<String> deliveredIssues,
        List<String> readIssues,

        Map<String, Object> metadata
) {
}