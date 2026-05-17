package pro.jeti.athenapress.plugin.ui;

import java.util.function.BiConsumer;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.player.pages.CustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import pro.jeti.athenapress.integration.HytalePlayerContext;
import pro.jeti.athenapress.integration.NewspaperImagePlacement;
import pro.jeti.athenapress.integration.NewspaperPreviewBlock;
import pro.jeti.athenapress.integration.NewspaperPreviewPage;
import pro.jeti.athenapress.integration.NewspaperSpreadMenuItem;
import pro.jeti.athenapress.integration.PlayerNewspaperVisualView;

/**
 * Ingame-Zeitungsseite als Hytale CustomUIPage.
 *
 * Zeigt Artikelübersicht oder Einzelartikel mit Bild, Text und
 * Navigations-Buttons. Events (Weiter, Zurück, Schließen, Seite wählen)
 * werden per onCommand-Callback an die Runtime weitergeleitet.
 */
public class NewspaperPage extends CustomUIPage {

    /** Relativer Pfad zur .ui-Layoutdatei im Mod-Asset-Pack */
    public static final String UI_LAYOUT = "UI/Custom/AthenaPress/NewspaperPage.ui";

    private final PlayerNewspaperVisualView view;
    private final HytalePlayerContext player;
    private final BiConsumer<String, String> onCommand;

    public NewspaperPage(
            PlayerRef hytaleRef,
            HytalePlayerContext player,
            PlayerNewspaperVisualView view,
            BiConsumer<String, String> onCommand
    ) {
        super(hytaleRef, CustomPageLifetime.CanDismissOrCloseThroughInteraction);
        this.view      = view;
        this.player    = player;
        this.onCommand = onCommand;
    }

    @Override
    public void build(
            Ref<EntityStore> ref,
            UICommandBuilder ui,
            UIEventBuilder events,
            Store<EntityStore> store
    ) {
        // Basis-Layout aus .ui Datei laden
        ui.append(UI_LAYOUT);

        // Titel setzen
        ui.set("#Title", view.title() + "  –  " + view.spreadStatus().label());

        boolean showingArticle = view.hasLeftPage();

        if (showingArticle) {
            // Einzelartikel-Ansicht
            ui.remove("#ArticleList");
            buildArticleContent(ui, view.leftPage());
        } else {
            // Übersichts-Ansicht: Artikelliste
            ui.remove("#ArticleView");
            buildSpreadMenu(ui, events, view);
        }

        // Navigation sichtbar/versteckt
        if (!view.hasPreviousSpread()) ui.remove("#BtnBack");
        if (!view.hasNextSpread())     ui.remove("#BtnNext");

        // Standard Button-Events
        if (view.hasPreviousSpread()) {
            events.addEventBinding(CustomUIEventBindingType.Activating, "#BtnBack",
                    EventData.of("cmd", "back"));
        }
        if (view.hasNextSpread()) {
            events.addEventBinding(CustomUIEventBindingType.Activating, "#BtnNext",
                    EventData.of("cmd", "next"));
        }
        events.addEventBinding(CustomUIEventBindingType.Activating, "#BtnClose",
                EventData.of("cmd", "close"));
        events.addEventBinding(CustomUIEventBindingType.Dismissing, null,
                EventData.of("cmd", "close"));
    }

    @Override
    public void handleDataEvent(
            Ref<EntityStore> ref, Store<EntityStore> store, String json
    ) {
        String cmd = extractField(json, "cmd");
        if (cmd == null) return;

        switch (cmd) {
            case "next"   -> onCommand.accept("next",   "");
            case "back"   -> onCommand.accept("back",   "");
            case "close"  -> onCommand.accept("close",  "");
            case "spread" -> {
                String idx = extractField(json, "idx");
                onCommand.accept("spread", idx != null ? idx : "0");
            }
            default -> { /* unbekannt, ignorieren */ }
        }
    }

    // -----------------------------------------------------------------------

    private void buildArticleContent(UICommandBuilder ui, NewspaperPreviewPage page) {
        String title    = page.title();
        String subtitle = "";
        String body     = "";
        String imgFile  = "";
        String caption  = "";

        for (NewspaperPreviewBlock block : page.blocks()) {
            switch (block.type()) {
                case HEADLINE    -> title    = block.content();
                case SUBHEADLINE -> subtitle = block.content();
                case BODY_TEXT   -> body    += block.content() + "\n";
                case IMAGE       -> { imgFile = block.assetPath(); caption = block.content(); }
                case CAPTION     -> caption  = block.content();
                default          -> { /* DIVIDER, QUOTE usw. – vorerst ignoriert */ }
            }
        }

        // Bild aus imagePlacements wenn Block-Liste kein Bild enthält
        if (imgFile.isEmpty() && !page.imagePlacements().isEmpty()) {
            NewspaperImagePlacement img = page.imagePlacements().get(0);
            imgFile  = img.assetPath();
            caption  = img.caption();
        }

        ui.set("#ArticleTitle",    safeStr(title));
        ui.set("#ArticleSubtitle", safeStr(subtitle));
        ui.set("#ArticleBody",     safeStr(body).trim());
        ui.set("#ArticleCaption",  safeStr(caption));

        if (!imgFile.isBlank()) {
            // Bild aus Mod-Asset-Pack: Common/Images/<assetPath>
            ui.set("#ArticleImage",
                    "Background: (TexturePath: \"Images/" + imgFile + "\", ScaleType: Fill)");
        } else {
            ui.remove("#ArticleImage");
            ui.remove("#ArticleCaption");
        }
    }

    private void buildSpreadMenu(
            UICommandBuilder ui,
            UIEventBuilder events,
            PlayerNewspaperVisualView view
    ) {
        for (NewspaperSpreadMenuItem item : view.spreadMenuItems()) {
            String btnId = "Spread_" + item.spreadIndex();
            String label = escape(item.label());
            if (item.current()) label = "▶ " + label;

            ui.append("#ArticleList",
                    "TextButton #" + btnId + " { Text: \"" + label + "\"; " +
                    "Padding: (Full: 8); FlexWeight: 1; }");

            events.addEventBinding(CustomUIEventBindingType.Activating, "#" + btnId,
                    EventData.of("cmd", "spread")
                             .put("idx", String.valueOf(item.spreadIndex())));
        }
    }

    // -----------------------------------------------------------------------
    // Hilfsmethoden

    /** Extrahiert einen String-Wert aus minimalem JSON: {"key":"value"} */
    private String extractField(String json, String key) {
        if (json == null || key == null) return null;
        String search = "\"" + key + "\"";
        int ki = json.indexOf(search);
        if (ki < 0) return null;
        int colon = json.indexOf(':', ki + search.length());
        if (colon < 0) return null;
        int q1 = json.indexOf('"', colon + 1);
        if (q1 < 0) return null;
        int q2 = json.indexOf('"', q1 + 1);
        if (q2 < 0) return null;
        return json.substring(q1 + 1, q2);
    }

    /** Öffentlicher Wrapper für das protected close() von CustomUIPage. */
    public void requestClose() {
        close();
    }

    private String safeStr(String s)  { return s == null ? "" : s; }
    private String escape(String s)   { return s == null ? "" : s.replace("\"", "'"); }
}
