package pro.jeti.athenapress.integration;

import java.io.IOException;
import java.nio.file.Path;

public final class AthenaPressVisualPngPreviewDemo {
    public static final String DEFAULT_ISSUE_ID = AthenaPressVisualPreviewDemo.DEFAULT_ISSUE_ID;

    private AthenaPressVisualPngPreviewDemo() {
    }

    public static void main(String[] args) throws IOException {
        VisualPngCommand command = parse(args);
        NewspaperPreviewImageRenderResult result = render(
                Path.of("..", "..", "AthenaPress").toAbsolutePath().normalize(),
                command.issueId(),
                command.outputDirectory()
        );

        for (Path image : result.spreadImages()) {
            System.out.println(image.toAbsolutePath().normalize());
        }
    }

    public static NewspaperPreviewImageRenderResult render(
            Path dataRoot,
            String issueId,
            Path outputDirectory
    ) throws IOException {
        AthenaPressIntegrationPlugin plugin = new AthenaPressIntegrationPlugin(dataRoot);
        NewspaperPreviewIssue previewIssue = plugin.createPreview(issueId);
        return new NewspaperPreviewImageRenderer().render(
                previewIssue,
                dataRoot.resolve("images"),
                outputDirectory
        );
    }

    static VisualPngCommand parse(String[] args) {
        String issueId = args == null || args.length == 0 || args[0] == null || args[0].isBlank()
                ? DEFAULT_ISSUE_ID
                : args[0];
        Path outputDirectory = args != null && args.length >= 2 && args[1] != null && !args[1].isBlank()
                ? Path.of(args[1])
                : Path.of("target", "visual-preview-png");
        return new VisualPngCommand(issueId, outputDirectory);
    }

    record VisualPngCommand(
            String issueId,
            Path outputDirectory
    ) {
    }
}
