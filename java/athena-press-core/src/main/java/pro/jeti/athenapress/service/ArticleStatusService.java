package pro.jeti.athenapress.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ArticleStatusService {

    private final Path articlesRoot;
    private final ObjectMapper objectMapper;

    public ArticleStatusService(Path athenaPressRoot) {
        this.articlesRoot = athenaPressRoot.resolve("articles");
        this.objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    }

    public String publishArticle(String articleId) throws IOException {
        Path source = findArticleFile(articleId, "draft");
        if (source == null) {
            return "Entwurf nicht gefunden: " + articleId;
        }

        Path publishedDir = articlesRoot.resolve("published");
        Files.createDirectories(publishedDir);

        ObjectNode node = (ObjectNode) objectMapper.readTree(source.toFile());
        node.put("status", "published");
        node.put("publishedAt", now());
        node.put("updatedAt", now());

        Path target = publishedDir.resolve(source.getFileName());
        objectMapper.writeValue(target.toFile(), node);
        Files.delete(source);

        return "Veroeffentlicht: " + articleId;
    }

    public String archiveArticle(String articleId) throws IOException {
        Path source = findArticleFile(articleId, "published");
        if (source == null) {
            source = findArticleFile(articleId, "draft");
        }
        if (source == null) {
            return "Artikel nicht gefunden: " + articleId;
        }

        Path archivedDir = articlesRoot.resolve("archived");
        Files.createDirectories(archivedDir);

        ObjectNode node = (ObjectNode) objectMapper.readTree(source.toFile());
        node.put("status", "archived");
        node.put("archivedAt", now());
        node.put("updatedAt", now());

        Path target = archivedDir.resolve(source.getFileName());
        objectMapper.writeValue(target.toFile(), node);
        Files.delete(source);

        return "Archiviert: " + articleId;
    }

    public String deleteDraft(String articleId) throws IOException {
        Path draft = findArticleFile(articleId, "draft");
        if (draft == null) {
            return "Entwurf nicht gefunden (nur Entwerfe koennen geloescht werden): " + articleId;
        }
        Files.delete(draft);
        return "Entwurf geloescht: " + articleId;
    }

    private Path findArticleFile(String articleId, String folder) {
        Path candidate = articlesRoot.resolve(folder).resolve(articleId + ".json");
        return Files.exists(candidate) ? candidate : null;
    }

    private String now() {
        return OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }
}
