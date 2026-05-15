package pro.jeti.athenapress.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class IssueWriteService {

    private final Path issuesRoot;
    private final ObjectMapper objectMapper;

    public IssueWriteService(Path athenaPressRoot) {
        this.issuesRoot = athenaPressRoot.resolve("issues");
        this.objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    }

    public String publishIssue(String issueId) throws IOException {
        Path source = findIssueFile(issueId, "draft");
        if (source == null) {
            return "Entwurf nicht gefunden: " + issueId;
        }

        Path publishedDir = issuesRoot.resolve("published");
        Files.createDirectories(publishedDir);

        ObjectNode node = (ObjectNode) objectMapper.readTree(source.toFile());
        node.put("status", "published");
        node.put("publishedAt", now());
        node.put("updatedAt", now());

        Path target = publishedDir.resolve(source.getFileName());
        objectMapper.writeValue(target.toFile(), node);
        Files.delete(source);

        return "Ausgabe veroeffentlicht: " + issueId;
    }

    public String archiveIssue(String issueId) throws IOException {
        Path source = findIssueFile(issueId, "published");
        if (source == null) {
            source = findIssueFile(issueId, "draft");
        }
        if (source == null) {
            return "Ausgabe nicht gefunden: " + issueId;
        }

        Path archivedDir = issuesRoot.resolve("archived");
        Files.createDirectories(archivedDir);

        ObjectNode node = (ObjectNode) objectMapper.readTree(source.toFile());
        node.put("status", "archived");
        node.put("archivedAt", now());
        node.put("updatedAt", now());

        Path target = archivedDir.resolve(source.getFileName());
        objectMapper.writeValue(target.toFile(), node);
        Files.delete(source);

        return "Ausgabe archiviert: " + issueId;
    }

    public String deliverIssue(String issueId) throws IOException {
        Path source = findIssueFile(issueId, "published");
        if (source == null) {
            return "Veroeffentlichte Ausgabe nicht gefunden: " + issueId;
        }

        ObjectNode node = (ObjectNode) objectMapper.readTree(source.toFile());
        if (node.path("deliveredToSubscribers").asBoolean(false)) {
            return "Ausgabe wurde bereits zugestellt: " + issueId;
        }
        node.put("deliveredToSubscribers", true);
        node.put("deliveredAt", now());
        node.put("updatedAt", now());

        objectMapper.writeValue(source.toFile(), node);

        return "Ausgabe zugestellt: " + issueId;
    }

    public String deleteDraft(String issueId) throws IOException {
        Path draft = findIssueFile(issueId, "draft");
        if (draft == null) {
            return "Entwurf nicht gefunden (nur Entwerfe koennen geloescht werden): " + issueId;
        }
        Files.delete(draft);
        return "Entwurf geloescht: " + issueId;
    }

    private Path findIssueFile(String issueId, String folder) {
        Path candidate = issuesRoot.resolve(folder).resolve(issueId + ".json");
        return Files.exists(candidate) ? candidate : null;
    }

    private String now() {
        return OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }
}
