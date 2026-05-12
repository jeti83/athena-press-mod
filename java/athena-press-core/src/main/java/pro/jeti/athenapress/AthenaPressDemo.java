package pro.jeti.athenapress;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class AthenaPressDemo {

    private static final ObjectMapper JSON = new ObjectMapper();

    private AthenaPressDemo() {
    }

    public static void main(String[] args) throws IOException {
        Path dataRoot = findDataRoot();
        String issueId = args.length > 0 ? args[0] : "issue_0002";

        JsonNode issue = loadIssue(dataRoot, issueId);

        System.out.println();
        System.out.println("========================================");
        System.out.println("        ATHENA BOTENBLATT");
        System.out.println("========================================");
        System.out.println();

        System.out.println(text(issue, "title", "(Ohne Titel)"));

        String subtitle = text(issue, "subtitle", "");
        if (!subtitle.isBlank()) {
            System.out.println(subtitle);
        }

        String issueName = text(issue, "issueName", "");
        if (!issueName.isBlank()) {
            System.out.println("Ausgabe: " + issueName);
        }

        System.out.println();
        System.out.println("Status: " + text(issue, "status", "(unbekannt)"));
        System.out.println("Validierung: OK - Demo konnte Ausgabe und Datenbestand lesen");

        System.out.println();
        System.out.println("Artikel:");
        printArticles(dataRoot, issue);

        System.out.println();
        System.out.println("Zustellplan:");
        printDeliveryPlan(dataRoot, issueId);

        System.out.println();
        System.out.println("========================================");
        System.out.println("Demo abgeschlossen.");
        System.out.println("========================================");
        System.out.println();
    }

    private static Path findDataRoot() {
        List<Path> candidates = List.of(
                Path.of("AthenaPress"),
                Path.of("..", "AthenaPress"),
                Path.of("..", "..", "AthenaPress")
        );

        for (Path candidate : candidates) {
            Path normalized = candidate.toAbsolutePath().normalize();
            if (Files.isDirectory(normalized)) {
                return normalized;
            }
        }

        throw new IllegalStateException("AthenaPress-Datenordner wurde nicht gefunden.");
    }

    private static JsonNode loadIssue(Path dataRoot, String issueId) throws IOException {
        for (String folder : List.of("draft", "published", "archived")) {
            Path file = dataRoot.resolve("issues").resolve(folder).resolve(issueId + ".json");
            if (Files.isRegularFile(file)) {
                return JSON.readTree(file.toFile());
            }
        }

        throw new IllegalArgumentException("Ausgabe nicht gefunden: " + issueId);
    }

    private static JsonNode loadArticle(Path dataRoot, String articleId) throws IOException {
        for (String folder : List.of("draft", "published", "archived")) {
            Path file = dataRoot.resolve("articles").resolve(folder).resolve(articleId + ".json");
            if (Files.isRegularFile(file)) {
                return JSON.readTree(file.toFile());
            }
        }

        throw new IllegalArgumentException("Artikel nicht gefunden: " + articleId);
    }

    private static void printArticles(Path dataRoot, JsonNode issue) throws IOException {
        JsonNode articles = issue.path("articles");

        if (!articles.isArray() || articles.isEmpty()) {
            System.out.println("- Keine Artikel eingetragen");
            return;
        }

        for (JsonNode articleRef : articles) {
            String articleId = articleRef.asText();
            JsonNode article = loadArticle(dataRoot, articleId);

            String categoryId = text(article, "categoryId", "unknown_category");
            String title = text(article, "title", "(Ohne Titel)");

            System.out.println("- [" + categoryId + "] " + title);
        }
    }

    private static void printDeliveryPlan(Path dataRoot, String issueId) throws IOException {
        Path subscribersFile = dataRoot.resolve("subscriptions").resolve("subscribers.json");

        if (!Files.isRegularFile(subscribersFile)) {
            System.out.println("- Keine subscribers.json gefunden");
            return;
        }

        JsonNode root = JSON.readTree(subscribersFile.toFile());
        JsonNode subscribers = root.isArray() ? root : root.path("subscribers");

        if (!subscribers.isArray() || subscribers.isEmpty()) {
            System.out.println("- Keine Abonnenten gefunden");
            return;
        }

        for (JsonNode subscriber : subscribers) {
            boolean subscribed = subscriber.path("subscribed").asBoolean(false);

            if (!subscribed) {
                continue;
            }

            String playerName = text(subscriber, "playerName", "(Unbekannt)");
            String deliveryMode = text(subscriber, "deliveryMode", "notification_only");
            boolean unread = !hasReadIssue(subscriber, issueId);

            System.out.println("- " + playerName + " -> " + deliveryMode + " -> unread " + unread);
        }
    }

    private static boolean hasReadIssue(JsonNode subscriber, String issueId) {
        for (String fieldName : List.of(
                "readIssues",
                "readIssueIds",
                "read",
                "readIssuesById",
                "readIssueMap",
                "seenIssues",
                "seenIssueIds"
        )) {
            JsonNode field = subscriber.path(fieldName);

            if (containsIssueId(field, issueId)) {
                return true;
            }
        }

        return false;
    }

    private static boolean containsIssueId(JsonNode node, String issueId) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return false;
        }

        if (node.isTextual()) {
            return issueId.equals(node.asText());
        }

        if (node.isArray()) {
            for (JsonNode entry : node) {
                if (containsIssueId(entry, issueId)) {
                    return true;
                }
            }

            return false;
        }

        if (node.isObject()) {
            if (node.has(issueId)) {
                return true;
            }

            JsonNode idField = node.path("issueId");
            if (idField.isTextual() && issueId.equals(idField.asText())) {
                return true;
            }

            JsonNode idFieldAlternative = node.path("id");
            if (idFieldAlternative.isTextual() && issueId.equals(idFieldAlternative.asText())) {
                return true;
            }

            for (JsonNode value : node) {
                if (containsIssueId(value, issueId)) {
                    return true;
                }
            }
        }

        return false;
    }

    private static String text(JsonNode node, String fieldName, String fallback) {
        JsonNode value = node.path(fieldName);

        if (value.isMissingNode() || value.isNull()) {
            return fallback;
        }

        String text = value.asText();

        if (text == null || text.isBlank()) {
            return fallback;
        }

        return text;
    }
}