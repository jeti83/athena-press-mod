package pro.jeti.athenapress.plugin;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.CustomUIPage;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import pro.jeti.athenapress.integration.ArticleEditorView;
import pro.jeti.athenapress.integration.IssueEditorView;
import pro.jeti.athenapress.plugin.ui.ArticleEditorPage;
import pro.jeti.athenapress.plugin.ui.IssueEditorPage;
import pro.jeti.athenapress.plugin.ui.MainMenuPage;

/**
 * Öffnet und aktualisiert die AthenaPress-GUI-Seiten (Menü, Artikel-Editor,
 * Ausgaben-Editor) über den Hytale WorldThread.
 *
 * Wenn Artikel- oder Ausgaben-Editor abgeschlossen/abgebrochen wird,
 * öffnet die Bridge automatisch das Hauptmenü wieder.
 */
public class EditorUiBridge {

    private static final Logger LOG = Logger.getLogger("AthenaPress");

    @FunctionalInterface
    public interface MenuHandler {
        void handle(String playerId, String cmd, boolean isAdmin);
    }

    @FunctionalInterface
    public interface EditorHandler {
        void handle(String playerId, String cmd, String val);
    }

    private final Map<String, Object>  playerRefs      = new ConcurrentHashMap<>();
    private final Map<String, Boolean> playerAdminCache = new ConcurrentHashMap<>();

    private volatile MenuHandler   menuHandler;
    private volatile EditorHandler articleEditorHandler;
    private volatile EditorHandler issueEditorHandler;

    public void setMenuHandler(MenuHandler h)          { this.menuHandler = h; }
    public void setArticleEditorHandler(EditorHandler h) { this.articleEditorHandler = h; }
    public void setIssueEditorHandler(EditorHandler h)   { this.issueEditorHandler = h; }

    // -----------------------------------------------------------------------
    // Spieler-Registrierung
    // -----------------------------------------------------------------------

    public void registerPlayer(String playerId, Object playerRef) {
        playerRefs.put(playerId, playerRef);
    }

    public void unregisterPlayer(String playerId) {
        playerRefs.remove(playerId);
        playerAdminCache.remove(playerId);
    }

    /** Gibt den gespeicherten PlayerRef zurück – für direkte Hytale-API-Zugriffe. */
    public Object getPlayerRef(String playerId) {
        return playerRefs.get(playerId);
    }

    /** Gibt den zuletzt gecachten Admin-Status des Spielers zurück. */
    public boolean getCachedAdminStatus(String playerId) {
        return playerAdminCache.getOrDefault(playerId, false);
    }

    // -----------------------------------------------------------------------
    // Seiten öffnen / aktualisieren
    // -----------------------------------------------------------------------

    public void openMainMenu(String playerId, boolean isAdmin) {
        playerAdminCache.put(playerId, isAdmin);
        openPage(playerId, ref -> new MainMenuPage(
                ref, isAdmin,
                cmd -> { if (menuHandler != null) menuHandler.handle(playerId, cmd, isAdmin); }
        ));
    }

    /**
     * Öffnet oder aktualisiert die Artikel-Editor-Seite.
     * Bei SUBMITTED oder CANCELLED wird stattdessen das Hauptmenü geöffnet.
     */
    public void openOrUpdateArticleEditor(String playerId, ArticleEditorView view) {
        if (view.isComplete() || view.isCancelled()) {
            openMainMenu(playerId, playerAdminCache.getOrDefault(playerId, false));
            return;
        }
        openPage(playerId, ref -> new ArticleEditorPage(
                ref, view,
                (cmd, val) -> { if (articleEditorHandler != null) articleEditorHandler.handle(playerId, cmd, val); }
        ));
    }

    /**
     * Öffnet oder aktualisiert die Ausgaben-Editor-Seite.
     * Bei SUBMITTED oder CANCELLED wird stattdessen das Hauptmenü geöffnet.
     */
    public void openOrUpdateIssueEditor(String playerId, IssueEditorView view) {
        if (view.isComplete() || view.isCancelled()) {
            openMainMenu(playerId, playerAdminCache.getOrDefault(playerId, false));
            return;
        }
        openPage(playerId, ref -> new IssueEditorPage(
                ref, view,
                (cmd, val) -> { if (issueEditorHandler != null) issueEditorHandler.handle(playerId, cmd, val); }
        ));
    }

    // -----------------------------------------------------------------------
    // Intern
    // -----------------------------------------------------------------------

    @FunctionalInterface
    private interface PageFactory {
        CustomUIPage create(PlayerRef playerRef);
    }

    private void openPage(String playerId, PageFactory factory) {
        Object rawRef = playerRefs.get(playerId);
        if (rawRef == null) {
            LOG.log(Level.WARNING, "[AP] openPage: kein PlayerRef für {0} – lazy-registration hat nicht gegriffen", playerId);
            return;
        }

        PlayerRef hytaleRef = (PlayerRef) rawRef;
        var entityRef = hytaleRef.getReference();
        var store     = entityRef.getStore();
        if (store == null) {
            LOG.log(Level.WARNING, "[AP] openPage: EntityStore null für {0}", playerId);
            return;
        }
        var externalData = store.getExternalData();
        var world = externalData != null ? externalData.getWorld() : null;
        if (world == null) {
            LOG.log(Level.WARNING, "[AP] openPage: World null für {0}", playerId);
            return;
        }

        LOG.log(Level.INFO, "[AP] openPage: world.execute() für {0}", playerId);
        world.execute(() -> {
            try {
                Player hytalePlayer = store.getComponent(entityRef, Player.getComponentType());
                if (hytalePlayer == null) {
                    LOG.log(Level.WARNING, "[AP] openPage: Player-Komponente null für {0}", playerId);
                    return;
                }
                hytalePlayer.getPageManager().openCustomPage(entityRef, store, factory.create(hytaleRef));
                LOG.log(Level.INFO, "[AP] openPage: openCustomPage() aufgerufen für {0}", playerId);
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "[AP] openPage: Ausnahme in world.execute() für " + playerId, e);
            }
        });
    }
}
