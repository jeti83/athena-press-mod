package pro.jeti.athenapress.integration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class AthenaPressVisualPreviewDemo {
    public static final String DEFAULT_ISSUE_ID = "issue_0002";

    private static final List<String> HELP_ARGUMENTS = List.of("--help", "--hilfe", "-h", "/?");
    private static final List<String> PREVIEW_ARGUMENTS = List.of("--visual-preview", "--layout-preview", "--vorschau");

    private AthenaPressVisualPreviewDemo() {
    }

    public static void main(String[] args) throws IOException {
        System.out.print(render(findDataRoot(), args));
    }

    public static String render(Path dataRoot, String[] args) throws IOException {
        VisualPreviewCommand command = parse(args);

        if (command.showHelp()) {
            return createHelpText();
        }

        AthenaPressIntegrationPlugin plugin = new AthenaPressIntegrationPlugin(dataRoot);
        return plugin.renderPreview(command.issueId());
    }

    static VisualPreviewCommand parse(String[] args) {
        if (args == null || args.length == 0) {
            return VisualPreviewCommand.preview(DEFAULT_ISSUE_ID);
        }

        String firstArgument = args[0];
        if (matchesArgument(firstArgument, HELP_ARGUMENTS)) {
            return VisualPreviewCommand.help();
        }

        if (matchesArgument(firstArgument, PREVIEW_ARGUMENTS)) {
            String issueId = args.length >= 2 ? args[1] : DEFAULT_ISSUE_ID;
            return VisualPreviewCommand.preview(issueId);
        }

        return VisualPreviewCommand.preview(firstArgument);
    }

    static String createHelpText() {
        StringBuilder help = new StringBuilder();

        help.append("\n");
        help.append("AthenaPress Visual Preview\n");
        help.append("\n");
        help.append("Verwendung:\n");
        help.append(" AthenaPressVisualPreviewDemo                         Zeigt die Visual-Preview für ")
                .append(DEFAULT_ISSUE_ID)
                .append("\n");
        help.append(" AthenaPressVisualPreviewDemo <issueId>               Zeigt die Visual-Preview einer Ausgabe\n");
        help.append(" AthenaPressVisualPreviewDemo --visual-preview <issueId> Zeigt die Visual-Preview einer Ausgabe\n");
        help.append(" AthenaPressVisualPreviewDemo --vorschau <issueId>    Deutsche Kurzform\n");
        help.append(" AthenaPressVisualPreviewDemo --help | --hilfe | -h | /? Zeigt diese Hilfe\n");
        help.append("\n");
        help.append("Die Ausgabe ist eine adapter-neutrale Doppelseiten-/Blockvorschau für Debugging und spätere native UI-Anbindung.\n");
        help.append("\n");

        return help.toString();
    }

    static Path findDataRoot() {
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

        throw new IllegalStateException(createMissingDataRootMessage(candidates));
    }

    private static String createMissingDataRootMessage(List<Path> candidates) {
        StringBuilder message = new StringBuilder();

        message.append("AthenaPress-Datenordner wurde nicht gefunden.\n");
        message.append("\n");
        message.append("Gesuchte Pfade:\n");

        for (Path candidate : candidates) {
            message.append("- ")
                    .append(candidate.toAbsolutePath().normalize())
                    .append("\n");
        }

        message.append("\n");
        message.append("Starte die Visual-Preview bitte aus dem Integration-Modulordner:\n");
        message.append("java/athena-press-integration\n");
        message.append("\n");
        message.append("Oder stelle sicher, dass der Ordner AthenaPress relativ erreichbar ist.");

        return message.toString();
    }

    private static boolean matchesArgument(String argument, List<String> aliases) {
        return aliases.stream().anyMatch(alias -> alias.equalsIgnoreCase(argument));
    }

    record VisualPreviewCommand(
            boolean showHelp,
            String issueId
    ) {

        static VisualPreviewCommand help() {
            return new VisualPreviewCommand(true, null);
        }

        static VisualPreviewCommand preview(String issueId) {
            String resolvedIssueId = issueId == null || issueId.isBlank()
                    ? DEFAULT_ISSUE_ID
                    : issueId;
            return new VisualPreviewCommand(false, resolvedIssueId);
        }
    }
}
