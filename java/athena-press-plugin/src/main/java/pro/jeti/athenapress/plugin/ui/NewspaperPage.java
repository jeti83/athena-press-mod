package pro.jeti.athenapress.plugin.ui;

import java.util.function.BiConsumer;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
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
public class NewspaperPage extends InteractiveCustomUIPage<UiEventData> {

    private static final boolean USE_SAFE_PROBE_UI = true;

    private static final String PROBE_UI = """
            Group {
              Background: (Color: #000000(0.82));
              LayoutMode: Middle;

              Group {
                Background: (Color: #1a2436);
                Anchor: (Width: 760, Height: 420);
                LayoutMode: Top;
                Padding: (Horizontal: 20, Top: 12, Bottom: 12);

                Label #ProbeTitle {
                  Anchor: (Height: 36);
                  Style: (FontSize: 18, RenderBold: true, HorizontalAlignment: Center,
                          TextColor: #ffffff, VerticalAlignment: Center);
                  Text: "AthenaPress Zeitung";
                }

                Label #ProbeMessage {
                  FlexWeight: 1;
                  Style: (FontSize: 15, TextColor: #d0d8e8, HorizontalAlignment: Center,
                          VerticalAlignment: Center);
                  Text: "Zeitungsseite geladen";
                }

                TextButton #BtnClose {
                  Anchor: (Height: 44);
                  Text: "Schliessen";
                }
              }
            }
            """;

    private static final String INLINE_UI = """
            Group {
              Background: (Color: #000000(0.82));
              LayoutMode: Middle;

              Group {
                Background: (Color: #1a2436);
                Anchor: (Width: 760, Height: 620);
                LayoutMode: Top;
                Padding: (Horizontal: 20, Top: 12, Bottom: 0);

                Label #Title {
                  Anchor: (Height: 36);
                  Style: (FontSize: 18, RenderBold: true, HorizontalAlignment: Center,
                          TextColor: #bfcdd5, VerticalAlignment: Center);
                  Text: "";
                }

                Group #Content {
                  LayoutMode: Top;
                  FlexWeight: 1;
                  Padding: (Top: 8);

                  Group #ArticleList {
                    LayoutMode: Top;
                    FlexWeight: 1;
                  }

                  Group #ArticleView {
                    LayoutMode: Top;
                    FlexWeight: 1;

                    Label #ArticleTitle {
                      Anchor: (Height: 36);
                      Padding: (Horizontal: 0, Top: 4, Bottom: 4);
                      Style: (FontSize: 22, RenderBold: true, TextColor: #ffffff,
                              VerticalAlignment: Center, Wrap: true);
                      Text: "";
                    }

                    Label #ArticleSubtitle {
                      Anchor: (Height: 22);
                      Padding: (Bottom: 8);
                      Style: (FontSize: 14, TextColor: #96a9be, VerticalAlignment: Center);
                      Text: "";
                    }

                    Group #ArticleImage {
                      Anchor: (Height: 180);
                      Padding: (Bottom: 4);
                    }

                    Label #ArticleCaption {
                      Anchor: (Height: 18);
                      Padding: (Bottom: 8);
                      Style: (FontSize: 11, TextColor: #787e8c, VerticalAlignment: Center);
                      Text: "";
                    }

                    Label #ArticleBody {
                      FlexWeight: 1;
                      Padding: (Top: 4, Bottom: 12);
                      Style: (FontSize: 14, TextColor: #d0d8e8, Wrap: true, VerticalAlignment: Top);
                      Text: "";
                    }
                  }
                }

                Group #NavBar {
                  LayoutMode: Left;
                  Anchor: (Height: 52);
                  Padding: (Horizontal: 16, Top: 8);

                  TextButton #BtnBack {
                    Text: "← Zurück";
                    FlexWeight: 1;
                  }

                  TextButton #BtnClose {
                    Text: "Schließen";
                    FlexWeight: 1;
                  }

                  TextButton #BtnNext {
                    Text: "Weiter →";
                    FlexWeight: 1;
                  }
                }
              }
            }
            """;

    private final PlayerNewspaperVisualView view;
    private final BiConsumer<String, String> onCommand;

    public NewspaperPage(
            PlayerRef hytaleRef,
            HytalePlayerContext player,
            PlayerNewspaperVisualView view,
            BiConsumer<String, String> onCommand
    ) {
        super(hytaleRef, CustomPageLifetime.CanDismissOrCloseThroughInteraction, UiEventData.CODEC);
        this.view      = view;
        this.onCommand = onCommand;
    }

    @Override
    public void build(
            Ref<EntityStore> ref,
            UICommandBuilder ui,
            UIEventBuilder events,
            Store<EntityStore> store
    ) {
        if (USE_SAFE_PROBE_UI) {
            ui.appendInline(null, PROBE_UI);
            ui.set("#ProbeTitle.Text", view.title());
            events.addEventBinding(CustomUIEventBindingType.Activating, "#BtnClose",
                    EventData.of("Cmd", "close"), false);
            return;
        }

        ui.appendInline(null, INLINE_UI);

        // Titel setzen
        ui.set("#Title.Text", view.title() + "  –  " + view.spreadStatus().label());

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
                    EventData.of("Cmd", "back"), false);
        }
        if (view.hasNextSpread()) {
            events.addEventBinding(CustomUIEventBindingType.Activating, "#BtnNext",
                    EventData.of("Cmd", "next"), false);
        }
        events.addEventBinding(CustomUIEventBindingType.Activating, "#BtnClose",
                EventData.of("Cmd", "close"), false);
    }

    @Override
    public void handleDataEvent(
            Ref<EntityStore> ref, Store<EntityStore> store, UiEventData data
    ) {
        String cmd = data != null ? data.cmd() : null;
        if (cmd == null) return;

        switch (cmd) {
            case "next"   -> onCommand.accept("next",   "");
            case "back"   -> onCommand.accept("back",   "");
            case "close"  -> onCommand.accept("close",  "");
            case "spread" -> {
                String idx = data.val();
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

        ui.set("#ArticleTitle.Text",    safeStr(title));
        ui.set("#ArticleSubtitle.Text", safeStr(subtitle));
        ui.set("#ArticleBody.Text",     safeStr(body).trim());
        ui.set("#ArticleCaption.Text",  safeStr(caption));

        if (!imgFile.isBlank()) {
            // Bild aus Mod-Asset-Pack: Common/Images/<assetPath>
            ui.set("#ArticleImage.Background",
                    "(TexturePath: \"Images/" + imgFile + "\", ScaleType: Fill)");
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

            ui.appendInline("#ArticleList",
                    "TextButton #" + btnId + " { Text: \"" + label + "\"; " +
                    "Padding: (Full: 8); FlexWeight: 1; }");

            events.addEventBinding(CustomUIEventBindingType.Activating, "#" + btnId,
                    EventData.of("Cmd", "spread")
                             .put("Val", String.valueOf(item.spreadIndex())), false);
        }
    }

    // -----------------------------------------------------------------------
    // Hilfsmethoden

    /** Öffentlicher Wrapper für das protected close() von CustomUIPage. */
    public void requestClose() {
        close();
    }

    private String safeStr(String s)  { return s == null ? "" : s; }
    private String escape(String s)   { return s == null ? "" : s.replace("\"", "'"); }
}
