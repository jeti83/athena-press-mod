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

import pro.jeti.athenapress.integration.ArticleEditorStep;
import pro.jeti.athenapress.integration.ArticleEditorView;

/**
 * Ingame-GUI für den Artikel-Editor.
 *
 * Kategorien und Bilder sind als Buttons anklickbar.
 * Titel und Fließtext werden weiterhin per Chat eingegeben –
 * die Seite aktualisiert sich nach jeder Chat-Eingabe automatisch.
 */
public class ArticleEditorPage extends CustomUIPage {

    private static final String INLINE_UI = """
            Group {
              Background: (Color: #000000(0.82));
              LayoutMode: Middle;

              Group {
                Background: (Color: #1a2436);
                Anchor: (Width: 580, Height: 540);
                LayoutMode: Top;
                Padding: (Horizontal: 20, Top: 12, Bottom: 8);

                Label #StepLabel {
                  Anchor: (Height: 20);
                  Style: (FontSize: 11, TextColor: #96a9be, VerticalAlignment: Center);
                  Text: "";
                }

                Label #PromptLabel {
                  Anchor: (Height: 28);
                  Padding: (Top: 4, Bottom: 6);
                  Style: (FontSize: 14, TextColor: #ffffff, VerticalAlignment: Center);
                  Text: "";
                }

                Label #MessageLabel {
                  Anchor: (Height: 22);
                  Padding: (Bottom: 6);
                  Style: (FontSize: 12, TextColor: #e07070, VerticalAlignment: Center);
                  Text: "";
                }

                Group #FieldsGroup {
                  LayoutMode: Top;
                  Padding: (Bottom: 6);

                  Label #FieldTitle {
                    Anchor: (Height: 18);
                    Padding: (Bottom: 2);
                    Style: (FontSize: 12, TextColor: #96a9be, VerticalAlignment: Center);
                    Text: "";
                  }

                  Label #FieldCategory {
                    Anchor: (Height: 18);
                    Padding: (Bottom: 2);
                    Style: (FontSize: 12, TextColor: #96a9be, VerticalAlignment: Center);
                    Text: "";
                  }

                  Label #FieldBodyPreview {
                    Anchor: (Height: 18);
                    Padding: (Bottom: 2);
                    Style: (FontSize: 12, TextColor: #96a9be, VerticalAlignment: Center);
                    Text: "";
                  }

                  Label #FieldImage {
                    Anchor: (Height: 18);
                    Padding: (Bottom: 2);
                    Style: (FontSize: 12, TextColor: #96a9be, VerticalAlignment: Center);
                    Text: "";
                  }
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

    private final ArticleEditorView view;
    private final BiConsumer<String, String> onCommand; // (cmd, val)

    public ArticleEditorPage(
            PlayerRef hytaleRef,
            ArticleEditorView view,
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

        buildFields(ui);
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
            case ENTER_TITLE    -> "Schritt 1/5: Titel eingeben";
            case ENTER_CATEGORY -> "Schritt 2/5: Kategorie wählen";
            case ENTER_BODY     -> "Schritt 3/5: Artikeltext eingeben";
            case ATTACH_IMAGE   -> "Schritt 4/5: Bild anhängen (optional)";
            case REVIEW         -> "Schritt 5/5: Überprüfen & Einreichen";
            default             -> "";
        };
    }

    private void buildFields(UICommandBuilder ui) {
        String titleText   = view.currentTitle()    != null ? "Titel: "     + view.currentTitle()              : "";
        String catText     = view.currentCategory() != null ? "Kategorie: " + view.currentCategory()            : "";
        String bodyPreview = view.currentBody()     != null ? "Text: "      + truncate(view.currentBody(), 80)  : "";
        String imgText     = view.currentImage()    != null ? "Bild: "      + view.currentImage()               : "";

        if (titleText.isBlank())   { ui.remove("#FieldTitle"); }       else { ui.set("#FieldTitle",       titleText);   }
        if (catText.isBlank())     { ui.remove("#FieldCategory"); }    else { ui.set("#FieldCategory",    catText);     }
        if (bodyPreview.isBlank()) { ui.remove("#FieldBodyPreview"); } else { ui.set("#FieldBodyPreview", bodyPreview); }
        if (imgText.isBlank())     { ui.remove("#FieldImage"); }       else { ui.set("#FieldImage",       imgText);     }
    }

    private void buildActionButtons(UICommandBuilder ui, UIEventBuilder events) {
        ArticleEditorStep step = view.step();

        if (step == ArticleEditorStep.ENTER_CATEGORY
                && view.availableCategories() != null
                && !view.availableCategories().isEmpty()) {
            int i = 0;
            for (String cat : view.availableCategories()) {
                String btnId = "Cat_" + i;
                ui.append("#ActionButtons",
                        "TextButton #" + btnId + " { Text: \"" + escape(cat) + "\"; "
                        + "Padding: (Full: 8); FlexWeight: 1; }");
                events.addEventBinding(CustomUIEventBindingType.Activating, "#" + btnId,
                        EventData.of("cmd", "select").put("val", cat));
                i++;
            }

        } else if (step == ArticleEditorStep.ATTACH_IMAGE) {
            ui.append("#ActionButtons",
                    "TextButton #BtnSkipImage { Text: \"Ohne Bild weiter →\"; "
                    + "Padding: (Full: 8); FlexWeight: 1; }");
            events.addEventBinding(CustomUIEventBindingType.Activating, "#BtnSkipImage",
                    EventData.of("cmd", "select").put("val", "weiter"));

        } else if (step == ArticleEditorStep.REVIEW) {
            ui.append("#ActionButtons",
                    "TextButton #BtnSubmit { Text: \"Artikel einreichen ✓\"; "
                    + "Padding: (Full: 10); FlexWeight: 1; }");
            events.addEventBinding(CustomUIEventBindingType.Activating, "#BtnSubmit",
                    EventData.of("cmd", "select").put("val", "einreichen"));
        }
        // ENTER_TITLE, ENTER_BODY: kein Button – Spieler tippt im Chat
    }

    // -----------------------------------------------------------------------

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max) + "…";
    }

    private String safeStr(String s) { return s == null ? "" : s; }
    private String escape(String s)  { return s == null ? "" : s.replace("\"", "'"); }

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
