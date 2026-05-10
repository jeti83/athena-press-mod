package pro.jeti.athenapress.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Article(
        String id,
        String title,
        String subtitle,
        String category,
        AuthorInfo author,
        String status,

        String summary,
        String content,

        List<ImageInfo> images,
        List<String> tags,
        List<LocationInfo> locations,

        String createdAt,
        String updatedAt,
        String publishedAt,
        String archivedAt,

        Map<String, Object> metadata
) {
}