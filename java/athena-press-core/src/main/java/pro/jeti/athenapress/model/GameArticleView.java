package pro.jeti.athenapress.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record GameArticleView(
        String id,
        String categoryId,
        String title,
        String subtitle,
        String teaser,
        String summary,
        String body,
        ImageInfo image
) {
    public GameArticleView(
            String id,
            String categoryId,
            String title,
            String subtitle,
            String teaser,
            String summary,
            String body
    ) {
        this(id, categoryId, title, subtitle, teaser, summary, body, null);
    }
}
