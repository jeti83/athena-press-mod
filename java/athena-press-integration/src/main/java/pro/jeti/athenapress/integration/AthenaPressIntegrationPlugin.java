package pro.jeti.athenapress.integration;

import java.io.IOException;
import java.nio.file.Path;

import pro.jeti.athenapress.AthenaPressCore;

public class AthenaPressIntegrationPlugin {
    private final IntegrationPluginMetadata metadata;
    private final NewspaperIntegrationGateway gateway;

    public AthenaPressIntegrationPlugin(Path athenaPressRoot) {
        this(
                IntegrationPluginMetadata.current(),
                new NewspaperIntegrationGateway(new AthenaPressCore(athenaPressRoot))
        );
    }

    public AthenaPressIntegrationPlugin(
            IntegrationPluginMetadata metadata,
            NewspaperIntegrationGateway gateway
    ) {
        if (metadata == null) {
            throw new IllegalArgumentException("metadata must not be null");
        }

        if (gateway == null) {
            throw new IllegalArgumentException("gateway must not be null");
        }

        this.metadata = metadata;
        this.gateway = gateway;
    }

    public IntegrationPluginMetadata metadata() {
        return metadata;
    }

    public String onPlayerOpenNewspaper(String playerId, String issueId) throws IOException {
        return gateway.openIssueForPlayer(playerId, issueId);
    }

    public String onPlayerRequestOverview(String playerId) {
        return gateway.showOverviewForPlayer(playerId);
    }

    public String onPlayerSelectArticle(String playerId, int articleNumber) {
        return gateway.showArticleForPlayer(playerId, articleNumber);
    }

    public String onPlayerSelectArticle(String playerId, String articleId) {
        return gateway.showArticleForPlayer(playerId, articleId);
    }

    public void onPlayerCloseNewspaper(String playerId) {
        gateway.closeIssueForPlayer(playerId);
    }

    public void closeAllNewspapers() {
        gateway.closeAllIssues();
    }

    public boolean hasOpenNewspaper(String playerId) {
        return gateway.hasOpenIssueForPlayer(playerId);
    }

    public String getOpenIssueId(String playerId) {
        return gateway.getOpenIssueIdForPlayer(playerId);
    }

    public PlayerNewspaperVisualResponse onPlayerOpenVisualNewspaper(
            String playerId,
            String issueId
    ) throws IOException {
        return gateway.openVisualIssueForPlayer(playerId, issueId);
    }

    public PlayerNewspaperVisualResponse onPlayerRequestCurrentVisualSpread(
            String playerId
    ) throws IOException {
        return gateway.showCurrentVisualSpreadForPlayer(playerId);
    }

    public PlayerNewspaperVisualResponse onPlayerRequestNextVisualSpread(
            String playerId
    ) throws IOException {
        return gateway.showNextVisualSpreadForPlayer(playerId);
    }

    public PlayerNewspaperVisualResponse onPlayerRequestPreviousVisualSpread(
            String playerId
    ) throws IOException {
        return gateway.showPreviousVisualSpreadForPlayer(playerId);
    }

    public PlayerNewspaperVisualResponse onPlayerRequestVisualSpread(
            String playerId,
            int spreadIndex
    ) throws IOException {
        return gateway.showVisualSpreadForPlayer(playerId, spreadIndex);
    }

    public void onPlayerCloseVisualNewspaper(String playerId) {
        gateway.closeVisualIssueForPlayer(playerId);
    }

    public void closeAllVisualNewspapers() {
        gateway.closeAllVisualIssues();
    }

    public boolean hasOpenVisualNewspaper(String playerId) {
        return gateway.hasOpenVisualIssueForPlayer(playerId);
    }

    public int getOpenVisualSessionCount() {
        return gateway.getOpenVisualSessionCount();
    }

    public NewspaperPreviewIssue createPreview(String issueId) throws IOException {
        return gateway.createPreviewForIssue(issueId);
    }

    public String renderPreview(String issueId) throws IOException {
        return gateway.renderPreviewForIssue(issueId);
    }

    public void invalidatePreview(String issueId) {
        gateway.invalidatePreviewForIssue(issueId);
    }

    public void clearPreviewCache() {
        gateway.clearPreviewCache();
    }

    public int getCachedPreviewCount() {
        return gateway.getCachedPreviewCount();
    }

    public IssueEditorView startIssueEditor(String playerId, String playerName, boolean admin) throws IOException {
        return gateway.startIssueEditor(playerId, playerName, admin);
    }

    public IssueEditorView handleIssueEditorInput(String playerId, String input) throws IOException {
        return gateway.handleIssueEditorInput(playerId, input);
    }

    public boolean hasActiveIssueEditorSession(String playerId) {
        return gateway.hasActiveIssueEditorSession(playerId);
    }

    public void cancelIssueEditorSession(String playerId) {
        gateway.cancelIssueEditorSession(playerId);
    }

    public ArticleEditorView startArticleEditor(String playerId, String playerName, boolean admin) {
        return gateway.startArticleEditor(playerId, playerName, admin);
    }

    public ArticleEditorView handleEditorInput(String playerId, String input) throws IOException {
        return gateway.handleEditorInput(playerId, input);
    }

    public ArticleEditorView attachCameraImage(String playerId, String imagePath, String caption) {
        return gateway.attachCameraImage(playerId, imagePath, caption);
    }

    public boolean hasActiveEditorSession(String playerId) {
        return gateway.hasActiveEditorSession(playerId);
    }

    public void cancelEditorSession(String playerId) {
        gateway.cancelEditorSession(playerId);
    }

    public AlbumView openAlbum(String playerName) throws IOException {
        return gateway.openAlbum(playerName);
    }

    public AlbumView handleAlbumCommand(String playerName, String[] args) throws IOException {
        return gateway.handleAlbumCommand(playerName, args);
    }

    public AlbumView registerCameraCapture(String playerName, String displayName) throws IOException {
        return gateway.registerCameraCapture(playerName, displayName);
    }

    public String handleChefRedakteurCommand(String[] args) throws IOException {
        return gateway.handleChefRedakteurCommand(args);
    }

    public String chefRedakteurHelpText() {
        return gateway.chefRedakteurHelpText();
    }
}
