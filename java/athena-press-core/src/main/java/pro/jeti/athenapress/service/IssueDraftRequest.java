package pro.jeti.athenapress.service;

import java.util.List;

public record IssueDraftRequest(
        String editorName,
        String subtitle,
        List<String> articleIds,
        String mainArticleId
) {}
