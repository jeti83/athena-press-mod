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

    public String createDraft(IssueDraftRequest request) throws IOException {
        String id = generateNextId();
        int issueNumber = extractNumber(id);
        Path draftFolder = issuesRoot.resolve("draft");
        Files.createDirectories(draftFolder);

        ObjectNode root = objectMapper.createObjectNode();
        root.put("id", id);
        root.put("status", "draft");
        root.put("issueNumber", issueNumber);
        root.put("title", "Athena Botenblatt");
        root.put("subtitle", request.subtitle() != null ? request.subtitle() : "");

        ObjectNode cover = objectMapper.createObjectNode();
        String mainId = request.mainArticleId() != null && !request.mainArticleId().isBlank()
                ? request.mainArticleId()
                : (request.articleIds() != null && !request.articleIds().isEmpty()
                        ? request.articleIds().get(0)
                        : null);
        if (mainId != null) cover.put("mainArticleId", mainId);
        cover.put("image", "placeholders/no_image.png");
        root.set("cover", cover);

        var articlesArray = objectMapper.createArrayNode();
        if (request.articleIds() != null) request.articleIds().forEach(articlesArray::add);
        root.set("articles", articlesArray);

        root.putNull("publishedAt");
        root.put("deliveredToSubscribers", false);
        String ts = now();
        root.put("createdAt", ts);
        root.put("updatedAt", ts);

        objectMapper.writeValue(draftFolder.resolve(id + ".json").toFile(), root);
        return id;
    }

    private String generateNextId() throws IOException {
        int maxNumber = 0;
        for (String subfolder : new String[]{"draft", "published", "archived"}) {
            Path folder = issuesRoot.resolve(subfolder);
            if (!Files.isDirectory(folder)) continue;
            try (var stream = Files.list(folder)) {
                int folderMax = stream
                        .map(p -> p.getFileName().toString())
                        .filter(name -> name.startsWith("issue_") && name.endsWith(".json"))
                        .mapToInt(this::extractNumber)
                        .max()
                        .orElse(0);
                maxNumber = Math.max(maxNumber, folderMax);
            }
        }
        return String.format("issue_%04d", maxNumber + 1);
    }

    private int extractNumber(String filename) {
        try {
            String digits = filename.substring("issue_".length(), filename.length() - ".json".length());
            return Integer.parseInt(digits);
        } catch (NumberFormatException | IndexOutOfBoundsException ignored) {
            return 0;
        }
    }

    private Path findIssueFile(String issueId, String folder) {
        Path candidate = issuesRoot.resolve(folder).resolve(issueId + ".json");
        return Files.exists(candidate) ? candidate : null;
    }

    private String now() {
        return OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }
}
