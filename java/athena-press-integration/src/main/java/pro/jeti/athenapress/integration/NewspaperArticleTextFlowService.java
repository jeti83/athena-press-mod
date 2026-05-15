package pro.jeti.athenapress.integration;

import java.util.ArrayList;
import java.util.List;

public class NewspaperArticleTextFlowService {

    private static final int DEFAULT_MAX_CHARACTERS_PER_SEGMENT = 360;

    private final int maxCharactersPerSegment;

    public NewspaperArticleTextFlowService() {
        this(DEFAULT_MAX_CHARACTERS_PER_SEGMENT);
    }

    public NewspaperArticleTextFlowService(int maxCharactersPerSegment) {
        this.maxCharactersPerSegment = maxCharactersPerSegment <= 0
                ? DEFAULT_MAX_CHARACTERS_PER_SEGMENT
                : maxCharactersPerSegment;
    }

    public List<String> split(String body) {
        if (!hasText(body)) {
            return List.of();
        }

        List<String> segments = new ArrayList<>();
        for (String paragraph : body.trim().split("\\R\\s*\\R+")) {
            appendParagraphSegments(segments, normalize(paragraph));
        }

        return segments;
    }

    private void appendParagraphSegments(List<String> segments, String paragraph) {
        if (!hasText(paragraph)) {
            return;
        }

        if (paragraph.length() <= maxCharactersPerSegment) {
            segments.add(paragraph);
            return;
        }

        String remaining = paragraph;
        while (remaining.length() > maxCharactersPerSegment) {
            int splitIndex = splitIndexFor(remaining);
            segments.add(remaining.substring(0, splitIndex).trim());
            remaining = remaining.substring(splitIndex).trim();
        }

        if (hasText(remaining)) {
            segments.add(remaining);
        }
    }

    private int splitIndexFor(String value) {
        int sentenceBoundary = lastBoundaryBefore(value, ". ");
        if (sentenceBoundary > 0) {
            return sentenceBoundary + 1;
        }

        int wordBoundary = value.lastIndexOf(' ', maxCharactersPerSegment);
        return wordBoundary > 0 ? wordBoundary : maxCharactersPerSegment;
    }

    private int lastBoundaryBefore(String value, String marker) {
        int maxIndex = Math.min(maxCharactersPerSegment, value.length());
        return value.lastIndexOf(marker, maxIndex);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ");
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
