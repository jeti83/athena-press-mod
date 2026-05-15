package pro.jeti.athenapress.integration;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

public class NewspaperPreviewImageRenderer {
    private static final int PAGE_WIDTH = 720;
    private static final int PAGE_HEIGHT = 980;
    private static final int SPREAD_GAP = 24;
    private static final int OUTER_PADDING = 36;
    private static final int PAGE_PADDING = 42;
    private static final Color BACKGROUND = new Color(62, 55, 49);
    private static final Color PAPER = new Color(242, 235, 221);
    private static final Color PAPER_EDGE = new Color(198, 186, 164);
    private static final Color INK = new Color(39, 34, 30);
    private static final Color MUTED_INK = new Color(94, 83, 72);
    private static final Color ACCENT = new Color(124, 45, 37);

    public NewspaperPreviewImageRenderResult render(
            NewspaperPreviewIssue previewIssue,
            Path imagesRoot,
            Path outputDirectory
    ) throws IOException {
        if (previewIssue == null || !previewIssue.hasSpreads()) {
            return new NewspaperPreviewImageRenderResult(null, List.of());
        }

        Path safeOutputDirectory = outputDirectory == null
                ? Path.of("target", "visual-preview-png")
                : outputDirectory;
        Files.createDirectories(safeOutputDirectory);

        List<Path> spreadImages = new ArrayList<>();
        for (NewspaperPreviewSpread spread : previewIssue.spreads()) {
            Path output = safeOutputDirectory.resolve(fileNameFor(previewIssue, spread));
            BufferedImage image = renderSpread(spread, imagesRoot);
            ImageIO.write(image, "png", output.toFile());
            spreadImages.add(output);
        }

        return new NewspaperPreviewImageRenderResult(previewIssue.issueId(), spreadImages);
    }

    private BufferedImage renderSpread(
            NewspaperPreviewSpread spread,
            Path imagesRoot
    ) {
        boolean hasRightPage = spread.hasRightPage();
        int width = OUTER_PADDING * 2 + PAGE_WIDTH + (hasRightPage ? SPREAD_GAP + PAGE_WIDTH : 0);
        int height = OUTER_PADDING * 2 + PAGE_HEIGHT;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        applyRenderingHints(graphics);

        graphics.setColor(BACKGROUND);
        graphics.fillRect(0, 0, width, height);
        drawPage(graphics, spread.leftPage(), OUTER_PADDING, OUTER_PADDING, imagesRoot);
        if (hasRightPage) {
            drawPage(
                    graphics,
                    spread.rightPage(),
                    OUTER_PADDING + PAGE_WIDTH + SPREAD_GAP,
                    OUTER_PADDING,
                    imagesRoot
            );
        }

        graphics.dispose();
        return image;
    }

    private void drawPage(
            Graphics2D graphics,
            NewspaperPreviewPage page,
            int x,
            int y,
            Path imagesRoot
    ) {
        if (page == null) {
            return;
        }

        graphics.setColor(PAPER);
        graphics.fillRect(x, y, PAGE_WIDTH, PAGE_HEIGHT);
        graphics.setColor(PAPER_EDGE);
        graphics.setStroke(new BasicStroke(2f));
        graphics.drawRect(x, y, PAGE_WIDTH, PAGE_HEIGHT);
        drawTopFolds(graphics, x, y);

        int contentX = x + PAGE_PADDING;
        int contentY = y + PAGE_PADDING;
        int contentWidth = PAGE_WIDTH - (PAGE_PADDING * 2);
        int contentHeight = PAGE_HEIGHT - (PAGE_PADDING * 2);
        int columns = columnsFor(page);
        int gutter = 18;
        int columnWidth = (contentWidth - gutter * (columns - 1)) / columns;
        int rowHeight = contentHeight / 24;

        drawMasthead(graphics, page, contentX, contentY, contentWidth);

        for (NewspaperPreviewBlock block : page.blocks()) {
            drawBlock(
                    graphics,
                    block,
                    contentX,
                    contentY + 58,
                    columnWidth,
                    gutter,
                    rowHeight,
                    imagesRoot
            );
        }
    }

    private void drawMasthead(
            Graphics2D graphics,
            NewspaperPreviewPage page,
            int x,
            int y,
            int width
    ) {
        graphics.setColor(MUTED_INK);
        graphics.setFont(new Font("Serif", Font.PLAIN, 14));
        graphics.drawString(page.role().name(), x, y + 14);
        graphics.drawString("Seite " + page.pageNumber(), x + width - 60, y + 14);
        graphics.setStroke(new BasicStroke(1f));
        graphics.drawLine(x, y + 26, x + width, y + 26);
    }

    private int columnsFor(NewspaperPreviewPage page) {
        return page.blocks().stream()
                .mapToInt(block -> block.columnIndex() + block.columnSpan())
                .max()
                .orElse(Math.max(1, page.designProfile().preferredColumns()));
    }

    private void drawBlock(
            Graphics2D graphics,
            NewspaperPreviewBlock block,
            int contentX,
            int contentY,
            int columnWidth,
            int gutter,
            int rowHeight,
            Path imagesRoot
    ) {
        int x = contentX + block.columnIndex() * (columnWidth + gutter);
        int y = contentY + block.rowStart() * rowHeight;
        int width = block.columnSpan() * columnWidth + Math.max(0, block.columnSpan() - 1) * gutter;
        int height = Math.max(rowHeight, block.rowSpan() * rowHeight - 6);

        switch (block.type()) {
            case HEADLINE -> drawTextBlock(graphics, block.content(), x, y, width, height, 30, Font.BOLD, INK);
            case SUBHEADLINE -> drawTextBlock(graphics, block.content(), x, y, width, height, 21, Font.BOLD, ACCENT);
            case BODY_TEXT -> drawTextBlock(graphics, block.content(), x, y, width, height, 16, Font.PLAIN, INK);
            case QUOTE -> {
                graphics.setColor(new Color(225, 215, 198));
                graphics.fillRect(x, y, width, height);
                drawTextBlock(graphics, block.content(), x + 12, y + 8, width - 24, height - 16, 17, Font.ITALIC, INK);
            }
            case NOTICE -> {
                graphics.setColor(new Color(235, 228, 214));
                graphics.fillRect(x, y, width, height);
                drawTextBlock(graphics, block.content(), x + 10, y + 8, width - 20, height - 16, 15, Font.BOLD, INK);
            }
            case IMAGE -> drawImageBlock(graphics, block, x, y, width, height, imagesRoot);
            case CAPTION -> drawTextBlock(graphics, block.content(), x, y, width, height, 13, Font.ITALIC, MUTED_INK);
            case ADVERTISEMENT -> {
                graphics.setColor(new Color(62, 55, 49));
                graphics.fillRect(x, y, width, height);
                drawTextBlock(graphics, block.content(), x + 12, y + 12, width - 24, height - 24, 18, Font.BOLD, PAPER);
            }
            case DIVIDER -> {
                graphics.setColor(PAPER_EDGE);
                graphics.setStroke(new BasicStroke(2f));
                graphics.drawLine(x, y + Math.max(1, height / 2), x + width, y + Math.max(1, height / 2));
            }
        }
    }

    private void drawImageBlock(
            Graphics2D graphics,
            NewspaperPreviewBlock block,
            int x,
            int y,
            int width,
            int height,
            Path imagesRoot
    ) {
        BufferedImage asset = loadAsset(imagesRoot, block.assetPath());
        graphics.setColor(new Color(218, 208, 190));
        graphics.fillRect(x, y, width, height);
        if (asset != null) {
            double scale = Math.min((double) width / asset.getWidth(), (double) height / asset.getHeight());
            int imageWidth = Math.max(1, (int) Math.round(asset.getWidth() * scale));
            int imageHeight = Math.max(1, (int) Math.round(asset.getHeight() * scale));
            int imageX = x + (width - imageWidth) / 2;
            int imageY = y + (height - imageHeight) / 2;
            graphics.drawImage(asset, imageX, imageY, imageWidth, imageHeight, null);
        } else {
            graphics.setColor(PAPER_EDGE);
            graphics.setStroke(new BasicStroke(2f));
            graphics.drawRect(x, y, width, height);
            drawTextBlock(graphics, "Bild", x + 12, y + 12, width - 24, height - 24, 18, Font.BOLD, MUTED_INK);
        }
    }

    private BufferedImage loadAsset(Path imagesRoot, String assetPath) {
        if (imagesRoot == null || assetPath == null || assetPath.isBlank()) {
            return null;
        }

        Path asset = imagesRoot.resolve(assetPath).normalize();
        if (!Files.isRegularFile(asset)) {
            return null;
        }

        try {
            return ImageIO.read(asset.toFile());
        } catch (IOException exception) {
            return null;
        }
    }

    private void drawTextBlock(
            Graphics2D graphics,
            String text,
            int x,
            int y,
            int width,
            int height,
            int fontSize,
            int fontStyle,
            Color color
    ) {
        graphics.setColor(color);
        graphics.setFont(new Font("Serif", fontStyle, fontSize));
        FontMetrics metrics = graphics.getFontMetrics();
        int lineHeight = metrics.getHeight();
        int baseline = y + metrics.getAscent();
        for (String line : wrap(text, metrics, width)) {
            if (baseline > y + height) {
                return;
            }
            graphics.drawString(line, x, baseline);
            baseline += lineHeight;
        }
    }

    private List<String> wrap(String text, FontMetrics metrics, int width) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        List<String> lines = new ArrayList<>();
        for (String paragraph : text.split("\\R")) {
            StringBuilder line = new StringBuilder();
            for (String word : paragraph.trim().split("\\s+")) {
                String candidate = line.isEmpty() ? word : line + " " + word;
                if (metrics.stringWidth(candidate) <= width) {
                    line.setLength(0);
                    line.append(candidate);
                } else {
                    if (!line.isEmpty()) {
                        lines.add(line.toString());
                    }
                    line.setLength(0);
                    line.append(word);
                }
            }
            if (!line.isEmpty()) {
                lines.add(line.toString());
            }
        }
        return lines;
    }

    private void drawTopFolds(Graphics2D graphics, int x, int y) {
        graphics.setColor(new Color(225, 215, 198));
        Shape leftFold = fold(x, y, x + 42, y, x, y + 42);
        Shape rightFold = fold(x + PAGE_WIDTH, y, x + PAGE_WIDTH - 42, y, x + PAGE_WIDTH, y + 42);
        graphics.fill(leftFold);
        graphics.fill(rightFold);
    }

    private Shape fold(int x1, int y1, int x2, int y2, int x3, int y3) {
        Path2D path = new Path2D.Double();
        path.moveTo(x1, y1);
        path.lineTo(x2, y2);
        path.lineTo(x3, y3);
        path.closePath();
        return path;
    }

    private void applyRenderingHints(Graphics2D graphics) {
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }

    private String fileNameFor(
            NewspaperPreviewIssue issue,
            NewspaperPreviewSpread spread
    ) {
        String issueId = issue.issueId().isBlank() ? "athena-press" : issue.issueId();
        return issueId + "-spread-" + (spread.spreadIndex() + 1) + ".png";
    }
}
