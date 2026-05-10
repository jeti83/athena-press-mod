package pro.jeti.athenapress.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Issue(
        String id,
        String name,
        String title,
        String subtitle,
        String status,

        List<String> articleIds,
        ImageInfo coverImage,

        String createdAt,
        String updatedAt,
        String publishedAt,
        String archivedAt,

        Map<String, Object> metadata
) {
}