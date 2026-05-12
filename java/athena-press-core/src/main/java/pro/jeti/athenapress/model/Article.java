package pro.jeti.athenapress.model;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Article(
        String id,
        String status,
        String categoryId,

        String title,
        String subtitle,
        String teaser,
        String summary,

        AuthorInfo author,

        String body,
        ImageInfo image,
        LocationInfo location,

        List<String> tags,

        String createdAt,
        String updatedAt,
        String publishedAt,
        String archivedAt,

        Map<String, Object> metadata
) {
}