package pro.jeti.athenapress.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ArticleWriteService {

    private final Path articlesRoot;
    private final ObjectMapper objectMapper;

    public ArticleWriteService(Path athenaPressRoot) {
        this.articlesRoot = athenaPressRoot.resolve("articles");
        this.objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    }

    public String createDraft(ArticleDraftRequest request) throws IOException {
        String id = generateNextId();
        Path draftFolder = articlesRoot.resolve("draft");
        Files.createDirectories(draftFolder);

        ObjectNode article = buildArticleNode(id, request);
        objectMapper.writeValue(draftFolder.resolve(id + ".json").toFile(), article);

        return id;
    }

    private ObjectNode buildArticleNode(String id, ArticleDraftRequest request) {
        String now = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        ObjectNode root = objectMapper.createObjectNode();
        root.put("id", id);
        root.put("status", "draft");
        root.put("categoryId", safe(request.categoryId(), "server_news"));
        root.put("title", safe(request.title(), ""));
        root.put("subtitle", request.subtitle() != null ? request.subtitle() : "");
        root.put("teaser", "");
        root.put("summary", "");

        ObjectNode author = objectMapper.createObjectNode();
        author.put("playerName", safe(request.playerName(), "unknown"));
        author.put("playerUuid", safe(request.playerUuid(), "unknown"));
        root.set("author", author);

        root.put("body", safe(request.body(), ""));

        if (request.imagePath() != null && !request.imagePath().isBlank()) {
            ObjectNode image = objectMapper.createObjectNode();
            image.put("file", request.imagePath());
            image.put("caption", request.imageCaption() != null ? request.imageCaption() : "");
            image.put("credit", safe(request.playerName(), "unknown"));
            image.put("sourceType", safe(request.imageSourceType(), "placeholder"));
            root.set("image", image);
        }

        ObjectNode location = objectMapper.createObjectNode();
        location.put("enabled", false);
        location.put("world", "");
        location.put("x", 0.0);
        location.put("y", 0.0);
        location.put("z", 0.0);
        root.set("location", location);

        root.set("tags", objectMapper.createArrayNode());
        root.put("createdAt", now);
        root.put("updatedAt", now);
        root.putNull("publishedAt");

        return root;
    }

    private String generateNextId() throws IOException {
        int maxNumber = 0;
        for (String subfolder : new String[]{"draft", "published", "archived"}) {
            Path folder = articlesRoot.resolve(subfolder);
            if (!Files.isDirectory(folder)) {
                continue;
            }
            try (var stream = Files.list(folder)) {
                int folderMax = stream
                        .map(p -> p.getFileName().toString())
                        .filter(name -> name.startsWith("article_") && name.endsWith(".json"))
                        .mapToInt(this::extractNumber)
                        .max()
                        .orElse(0);
                maxNumber = Math.max(maxNumber, folderMax);
            }
        }
        return String.format("article_%04d", maxNumber + 1);
    }

    private int extractNumber(String filename) {
        try {
            String digits = filename.substring("article_".length(), filename.length() - ".json".length());
            return Integer.parseInt(digits);
        } catch (NumberFormatException | IndexOutOfBoundsException ignored) {
            return 0;
        }
    }

    private String safe(String value, String fallback) {
        return (value == null || value.isBlank()) ? fallback : value;
    }
}
