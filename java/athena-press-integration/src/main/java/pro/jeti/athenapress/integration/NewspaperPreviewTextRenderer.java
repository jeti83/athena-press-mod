package pro.jeti.athenapress.integration;

public class NewspaperPreviewTextRenderer {

    public String render(NewspaperPreviewIssue previewIssue) {
        if (previewIssue == null || !previewIssue.hasSpreads()) {
            return "AthenaPress Preview\nKeine Vorschauseiten vorhanden.";
        }

        StringBuilder text = new StringBuilder();
        text.append(previewIssue.title()).append(" Preview\n");
        text.append("Design: ")
                .append(previewIssue.designProfile().layoutMood())
                .append(", Navigation: ")
                .append(previewIssue.designProfile().navigationStyle())
                .append("\n");

        for (NewspaperPreviewSpread spread : previewIssue.spreads()) {
            text.append("\nDoppelseite ")
                    .append(spread.spreadIndex() + 1)
                    .append("\n");
            appendPage(text, "Links", spread.leftPage());
            appendPage(text, "Rechts", spread.rightPage());
        }

        return text.toString();
    }

    private void appendPage(
            StringBuilder text,
            String side,
            NewspaperPreviewPage page
    ) {
        if (page == null) {
            text.append(side).append(": [leer]\n");
            return;
        }

        text.append(side)
                .append(": Seite ")
                .append(page.pageNumber())
                .append(" (")
                .append(page.role())
                .append(") ")
                .append(page.title())
                .append("\n");

        for (NewspaperPreviewBlock block : page.blocks()) {
            text.append("  - ")
                    .append(block.type())
                    .append(" intent=")
                    .append(block.layoutIntent())
                    .append(" c")
                    .append(block.columnIndex())
                    .append(" r")
                    .append(block.rowStart())
                    .append("+")
                    .append(block.rowSpan())
                    .append(" span ")
                    .append(block.columnSpan());

            if (block.hasAsset()) {
                text.append(" asset=").append(block.assetPath());
            }

            if (!block.content().isBlank()) {
                text.append(": ").append(block.content());
            }

            text.append("\n");
        }

        for (NewspaperImagePlacement imagePlacement : page.imagePlacements()) {
            text.append("  - IMAGE_PLACEMENT c")
                    .append(imagePlacement.columnIndex())
                    .append(" r")
                    .append(imagePlacement.rowStart())
                    .append("+")
                    .append(imagePlacement.rowSpan())
                    .append(" span ")
                    .append(imagePlacement.columnSpan())
                    .append(" role=")
                    .append(imagePlacement.role())
                    .append(" asset=")
                    .append(imagePlacement.assetPath());

            if (!imagePlacement.caption().isBlank()) {
                text.append(": ").append(imagePlacement.caption());
            }

            text.append("\n");
        }
    }
}
