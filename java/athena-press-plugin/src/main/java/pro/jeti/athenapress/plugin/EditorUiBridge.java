package pro.jeti.athenapress.plugin;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.CustomUIPage;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import pro.jeti.athenapress.integration.ArticleEditorView;
import pro.jeti.athenapress.integration.IssueEditorView;
import pro.jeti.athenapress.plugin.ui.ArticleEditorPage;
import pro.jeti.athenapress.plugin.ui.IssueEditorPage;
import pro.jeti.athenapress.plugin.ui.MainMenuPage;

/**
 * Öffnet und aktualisiert die AthenaPress-GUI-Seiten (Menü, Artikel-Editor,
 * Ausgaben-Editor) über den Hytale WorldThread.
 *
 * playerEntityRefs speichert Ref<EntityStore> (aus ctx.senderAsPlayerRef(), thread-safe).
 * Der PlayerRef-Lookup passiert ausschließlich innerhalb von world.execute(),
 * weil store.getComponent() eine WorldThread-Assertion hat.
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

    @SuppressWarnings("rawtypes")
    private final Map<String, Ref>   playerEntityRefs = new ConcurrentHashMap<>();
    private final Map<String, Boolean> playerAdminCache = new ConcurrentHashMap<>();

    private volatile MenuHandler   menuHandler;
    private volatile EditorHandler articleEditorHandler;
    private volatile EditorHandler issueEditorHandler;

    public void setMenuHandler(MenuHandler h)             { this.menuHandler = h; }
    public void setArticleEditorHandler(EditorHandler h)  { this.articleEditorHandler = h; }
    public void setIssueEditorHandler(EditorHandler h)    { this.issueEditorHandler = h; }

    // -----------------------------------------------------------------------
    // Spieler-Registrierung
    // -----------------------------------------------------------------------

    /**
     * Registriert den Spieler mit seinem Ref<EntityStore>.
     * Darf aus beliebigem Thread aufgerufen werden – kein store.getComponent() hier.
     */
    public void registerPlayer(String playerId, Ref<EntityStore> entityRef) {
        if (entityRef == null) return;
        playerEntityRefs.put(playerId, entityRef);
    }

    public void unregisterPlayer(String playerId) {
        playerEntityRefs.remove(playerId);
        playerAdminCache.remove(playerId);
    }

    /** true wenn ein Ref für diesen Spieler vorhanden ist. */
    public boolean isRegistered(String playerId) {
        return playerEntityRefs.containsKey(playerId);
    }

    /** Gibt den zuletzt gecachten Admin-Status zurück. */
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

    @SuppressWarnings("unchecked")
    private void openPage(String playerId, PageFactory factory) {
        Ref<EntityStore> entityRef = playerEntityRefs.get(playerId);
        if (entityRef == null) {
            LOG.log(Level.WARNING, "[AP] openPage: kein EntityRef für {0}", playerId);
            return;
        }

        var store = entityRef.getStore();
        if (store == null) {
            LOG.log(Level.WARNING, "[AP] openPage: Store null für {0}", playerId);
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
                // PlayerRef-Lookup hier im WorldThread – store.getComponent() erlaubt
                PlayerRef playerRef = store.getComponent(entityRef, PlayerRef.getComponentType());
                if (playerRef == null) {
                    LOG.log(Level.WARNING, "[AP] openPage: PlayerRef-Komponente null für {0}", playerId);
                    return;
                }

                Player player = store.getComponent(entityRef, Player.getComponentType());
                if (player == null) {
                    LOG.log(Level.WARNING, "[AP] openPage: Player-Komponente null für {0}", playerId);
                    return;
                }

                player.getPageManager().openCustomPage(entityRef, store, factory.create(playerRef));
                LOG.log(Level.INFO, "[AP] openPage: openCustomPage() aufgerufen für {0}", playerId);
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "[AP] openPage: Ausnahme in world.execute() für " + playerId, e);
            }
        });
    }
}
