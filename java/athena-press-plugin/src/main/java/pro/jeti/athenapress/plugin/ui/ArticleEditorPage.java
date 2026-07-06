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

import pro.jeti.athenapress.integration.ArticleEditorView;

/**
 * Ingame-GUI für den Artikel-Editor.
 *
 * Kategorien und Bilder sind als Buttons anklickbar.
 * Titel und Fließtext werden weiterhin per Chat eingegeben –
 * die Seite aktualisiert sich nach jeder Chat-Eingabe automatisch.
 */
public class ArticleEditorPage extends InteractiveCustomUIPage<UiEventData> {

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

                  Label #ChatInputHint {
                    Text: "Esc druecken, Eingabe in den Chat schreiben. Der naechste Schritt oeffnet sich automatisch.";
                    Anchor: (Height: 56);
                    Padding: (Full: 8);
                    Style: (FontSize: 13, TextColor: #d0d8e8, HorizontalAlignment: Center,
                            VerticalAlignment: Center);
                  }
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
        super(hytaleRef, CustomPageLifetime.CanDismissOrCloseThroughInteraction, UiEventData.CODEC);
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

        ui.set("#StepLabel.Text", stepLabel());
        ui.set("#PromptLabel.Text", safeStr(view.prompt()));

        if (view.message() != null && !view.message().isBlank()) {
            ui.set("#MessageLabel.Text", view.message());
        } else {
            ui.remove("#MessageLabel");
        }

        buildFields(ui);
        buildActionButtons(ui, events);

        events.addEventBinding(CustomUIEventBindingType.Activating, "#BtnCancel",
                EventData.of("Cmd", "cancel"), false);
    }

    @Override
    public void handleDataEvent(
            Ref<EntityStore> ref, Store<EntityStore> store, UiEventData data
    ) {
        String cmd = data != null ? data.cmd() : null;
        if (cmd == null) return;
        String val = data.val();
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

        if (titleText.isBlank())   { ui.remove("#FieldTitle"); }       else { ui.set("#FieldTitle.Text",       titleText);   }
        if (catText.isBlank())     { ui.remove("#FieldCategory"); }    else { ui.set("#FieldCategory.Text",    catText);     }
        if (bodyPreview.isBlank()) { ui.remove("#FieldBodyPreview"); } else { ui.set("#FieldBodyPreview.Text", bodyPreview); }
        if (imgText.isBlank())     { ui.remove("#FieldImage"); }       else { ui.set("#FieldImage.Text",       imgText);     }
    }

    private void buildActionButtons(UICommandBuilder ui, UIEventBuilder events) {
        ui.set("#ChatInputHint.Text", chatInputHint());
    }

    private String chatInputHint() {
        return switch (view.step()) {
            case ENTER_TITLE -> "Esc druecken, Artikeltitel in den Chat schreiben.";
            case ENTER_CATEGORY -> "Esc druecken, Kategorie exakt in den Chat schreiben.";
            case ENTER_BODY -> "Esc druecken, Artikeltext in den Chat schreiben.";
            case ATTACH_IMAGE -> "Esc druecken, Foto-Nummer oder 'weiter' in den Chat schreiben.";
            case REVIEW -> "Esc druecken, 'einreichen' oder 'abbrechen' in den Chat schreiben.";
            default -> "Esc druecken, Eingabe in den Chat schreiben.";
        };
    }

    // -----------------------------------------------------------------------

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max) + "…";
    }

    private String safeStr(String s) { return s == null ? "" : s; }
}
