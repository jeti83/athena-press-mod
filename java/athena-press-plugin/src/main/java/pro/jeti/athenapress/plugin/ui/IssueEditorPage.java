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

import pro.jeti.athenapress.integration.IssueEditorView;

/**
 * Ingame-GUI für den Ausgaben-Editor.
 *
 * Artikelauswahl (SELECT_ARTICLES) erfolgt per Chat (Nummern eingeben).
 * Für Schritte mit eindeutiger Aktion (Weiter/Überspringen/Einreichen)
 * werden anklickbare Buttons angezeigt.
 */
public class IssueEditorPage extends CustomUIPage {

    private static final String INLINE_UI = """
            Group {
              Background: (Color: #000000(0.82));
              LayoutMode: Middle;

              Group {
                Background: (Color: #1a2436);
                Anchor: (Width: 580, Height: 560);
                LayoutMode: Top;
                Padding: (Horizontal: 20, Top: 12, Bottom: 8);

                Label #StepLabel {
                  Anchor: (Height: 20);
                  Padding: (Bottom: 4);
                  Style: (FontSize: 11, TextColor: #96a9be, VerticalAlignment: Center);
                  Text: "";
                }

                Label #MessageLabel {
                  Anchor: (Height: 22);
                  Padding: (Bottom: 6);
                  Style: (FontSize: 12, TextColor: #e07070, VerticalAlignment: Center);
                  Text: "";
                }

                Label #PromptLabel {
                  Anchor: (Height: 200);
                  Padding: (Bottom: 10);
                  Style: (FontSize: 13, TextColor: #d0d8e8, Wrap: true, VerticalAlignment: Top);
                  Text: "";
                }

                Group #ActionButtons {
                  LayoutMode: Top;
                  FlexWeight: 1;
                }

                Group #NavBar {
                  LayoutMode: Left;
                  Anchor: (Height: 52);
                  Padding: (Horizontal: 16, Top: 8);

                  TextButton #BtnCancel {
                    Text: "Abbrechen";
                    FlexWeight: 1;
                  }
                }
              }
            }
            """;

    private final IssueEditorView view;
    private final BiConsumer<String, String> onCommand; // (cmd, val)

    public IssueEditorPage(
            PlayerRef hytaleRef,
            IssueEditorView view,
            BiConsumer<String, String> onCommand
    ) {
        super(hytaleRef, CustomPageLifetime.CanDismissOrCloseThroughInteraction);
        this.view = view;
        this.onCommand = onCommand;
    }

    @Override
    public void build(
            Ref<EntityStore> ref,
            UICommandBuilder ui,
            UIEventBuilder events,
            Store<EntityStore> store
    ) {
        ui.appendInline(null, INLINE_UI);

        ui.set("#StepLabel", stepLabel());
        ui.set("#PromptLabel", safeStr(view.prompt()));

        if (view.message() != null && !view.message().isBlank()) {
            ui.set("#MessageLabel", view.message());
        } else {
            ui.remove("#MessageLabel");
        }

        buildActionButtons(ui, events);

        events.addEventBinding(CustomUIEventBindingType.Activating, "#BtnCancel",
                EventData.of("cmd", "cancel"));
        events.addEventBinding(CustomUIEventBindingType.Dismissing, null,
                EventData.of("cmd", "cancel"));
    }

    @Override
    public void handleDataEvent(
            Ref<EntityStore> ref, Store<EntityStore> store, String json
    ) {
        String cmd = extractField(json, "cmd");
        if (cmd == null) return;
        String val = extractField(json, "val");
        onCommand.accept(cmd, val != null ? val : "");
    }

    public void requestClose() { close(); }

    // -----------------------------------------------------------------------

    private String stepLabel() {
        return switch (view.step()) {
            case SELECT_ARTICLES     -> "Schritt 1/4: Artikel auswählen (Nummern in Chat eingeben)";
            case CHOOSE_MAIN_ARTICLE -> "Schritt 2/4: Titelartikel wählen";
            case ENTER_SUBTITLE      -> "Schritt 3/4: Untertitel eingeben (optional)";
            case REVIEW              -> "Schritt 4/4: Überprüfen & Einreichen";
            default                  -> "";
        };
    }

    private void buildActionButtons(UICommandBuilder ui, UIEventBuilder events) {
        switch (view.step()) {
            case CHOOSE_MAIN_ARTICLE -> {
                ui.append("#ActionButtons",
                        "TextButton #BtnUseFirst { Text: \"Ersten Artikel als Titelartikel →\"; "
                        + "Padding: (Full: 8); FlexWeight: 1; }");
                events.addEventBinding(CustomUIEventBindingType.Activating, "#BtnUseFirst",
                        EventData.of("cmd", "select").put("val", "weiter"));
            }
            case ENTER_SUBTITLE -> {
                ui.append("#ActionButtons",
                        "TextButton #BtnSkipSubtitle { Text: \"Ohne Untertitel weiter →\"; "
                        + "Padding: (Full: 8); FlexWeight: 1; }");
                events.addEventBinding(CustomUIEventBindingType.Activating, "#BtnSkipSubtitle",
                        EventData.of("cmd", "select").put("val", "weiter"));
            }
            case REVIEW -> {
                ui.append("#ActionButtons",
                        "TextButton #BtnSubmit { Text: \"Ausgabe einreichen ✓\"; "
                        + "Padding: (Full: 10); FlexWeight: 1; }");
                events.addEventBinding(CustomUIEventBindingType.Activating, "#BtnSubmit",
                        EventData.of("cmd", "select").put("val", "einreichen"));
            }
            default -> { /* SELECT_ARTICLES: Spieler gibt Nummern im Chat ein */ }
        }
    }

    // -----------------------------------------------------------------------

    private String safeStr(String s) { return s == null ? "" : s; }

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
}
