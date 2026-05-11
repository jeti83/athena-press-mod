package pro.jeti.athenapress.repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import pro.jeti.athenapress.model.Issue;

public class IssueRepository {

    private final Path issuesRoot;
    private final ObjectMapper objectMapper;

    public IssueRepository(Path athenaPressRoot) {
        this.issuesRoot = athenaPressRoot.resolve("issues");
        this.objectMapper = new ObjectMapper();
    }

    public List<Issue> findAll() throws IOException {
        List<Issue> issues = new ArrayList<>();

        issues.addAll(readIssuesFromFolder("draft"));
        issues.addAll(readIssuesFromFolder("published"));
        issues.addAll(readIssuesFromFolder("archived"));

        return issues;
    }

    public List<Issue> findDraftIssues() throws IOException {
        return readIssuesFromFolder("draft");
    }

    public List<Issue> findPublishedIssues() throws IOException {
        return readIssuesFromFolder("published");
    }

    public List<Issue> findArchivedIssues() throws IOException {
        return readIssuesFromFolder("archived");
    }

    public Issue findById(String issueId) throws IOException {
        for (Issue issue : findAll()) {
            if (issueId.equals(issue.id())) {
                return issue;
            }
        }

        return null;
    }

    private List<Issue> readIssuesFromFolder(String folderName) throws IOException {
        Path folder = issuesRoot.resolve(folderName);

        if (!Files.exists(folder)) {
            return List.of();
        }

        try (var stream = Files.list(folder)) {
            return stream
                    .filter(path -> path.toString().endsWith(".json"))
                    .map(this::readIssueUnchecked)
                    .toList();
        }
    }

    private Issue readIssueUnchecked(Path path) {
        try {
            return objectMapper.readValue(path.toFile(), Issue.class);
        } catch (IOException exception) {
            throw new RuntimeException("Could not read issue JSON: " + path, exception);
        }
    }
}