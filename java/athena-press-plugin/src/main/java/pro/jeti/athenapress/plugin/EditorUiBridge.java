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
 * Öffnet und aktualisiert die AthenaPress-GUI-Seiten über den Hytale WorldThread.
 *
 * Speichert den echten com.hypixel.hytale.server.core.universe.PlayerRef als Object
 * (selbes Muster wie NewspaperVisualBridge). getReference() wird lazy beim Öffnen
 * der Seite aufgerufen, weil es zur Connect-Zeit noch null zurückgeben kann.
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

    public void setMenuHandler(MenuHandler h)             { this.menuHandler = h; }
    public void setArticleEditorHandler(EditorHandler h)  { this.articleEditorHandler = h; }
    public void setIssueEditorHandler(EditorHandler h)    { this.issueEditorHandler = h; }

    // -----------------------------------------------------------------------
    // Spieler-Registrierung
    // -----------------------------------------------------------------------

    /**
     * Registriert den Spieler mit dem echten PlayerRef-Objekt vom Connect-Event.
     * Darf aus beliebigem Thread aufgerufen werden.
     */
    public void registerPlayer(String playerId, Object playerRef) {
        if (playerRef == null) return;
        playerRefs.put(playerId, playerRef);
    }

    public void unregisterPlayer(String playerId) {
        playerRefs.remove(playerId);
        playerAdminCache.remove(playerId);
    }

    /** true wenn ein PlayerRef für diesen Spieler vorhanden ist. */
    public boolean isRegistered(String playerId) {
        return playerRefs.containsKey(playerId);
    }

    /**
     * Gibt den Ref<EntityStore> für direkte WorldThread-Zugriffe zurück (z.B. giveCameraItem).
     * Gibt null zurück wenn kein PlayerRef bekannt oder getReference() noch null ist.
     */
    @SuppressWarnings("unchecked")
    public Ref<EntityStore> getEntityRef(String playerId) {
        Object raw = playerRefs.get(playerId);
        if (raw == null) return null;
        try {
            return ((PlayerRef) raw).getReference();
        } catch (Exception e) {
            return null;
        }
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

    private void openPage(String playerId, PageFactory factory) {
        Object raw = playerRefs.get(playerId);
        if (raw == null) {
            LOG.log(Level.WARNING, "[AP] openPage: kein PlayerRef fuer {0}", playerId);
            return;
        }

        // Selbes Muster wie NewspaperVisualBridge: ref lazy aus dem PlayerRef holen
        PlayerRef hytaleRef = (PlayerRef) raw;
        Ref<EntityStore> entityRef = hytaleRef.getReference();
        if (entityRef == null) {
            LOG.log(Level.WARNING, "[AP] openPage: getReference() null fuer {0}", playerId);
            return;
        }

        var store = entityRef.getStore();
        if (store == null) {
            LOG.log(Level.WARNING, "[AP] openPage: Store null fuer {0}", playerId);
            return;
        }

        var world = store.getExternalData() != null ? store.getExternalData().getWorld() : null;
        if (world == null) {
            LOG.log(Level.WARNING, "[AP] openPage: World null fuer {0}", playerId);
            return;
        }

        LOG.log(Level.INFO, "[AP] openPage: world.execute() fuer {0}", playerId);
        world.execute(() -> {
            try {
                Player player = store.getComponent(entityRef, Player.getComponentType());
                if (player == null) {
                    LOG.log(Level.WARNING, "[AP] openPage: Player-Komponente null fuer {0}", playerId);
                    return;
                }
                player.getPageManager().openCustomPage(entityRef, store, factory.create(hytaleRef));
                LOG.log(Level.INFO, "[AP] openPage: openCustomPage() aufgerufen fuer {0}", playerId);
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "[AP] openPage: Ausnahme in world.execute() fuer " + playerId, e);
            }
        });
    }
}
