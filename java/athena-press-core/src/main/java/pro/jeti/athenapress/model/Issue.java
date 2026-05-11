package pro.jeti.athenapress.model;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Issue(
        String id,
        String status,
        Integer issueNumber,
        String title,
        String subtitle,

        List<String> articles,
        ImageInfo coverImage,

        String createdAt,
        String updatedAt,
        String publishedAt,
        String archivedAt,

        Map<String, Object> metadata
) {
}