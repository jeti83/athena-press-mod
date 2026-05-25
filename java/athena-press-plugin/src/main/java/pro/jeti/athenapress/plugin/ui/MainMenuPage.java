package pro.jeti.athenapress.plugin.ui;

import java.util.function.Consumer;

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

public class MainMenuPage extends CustomUIPage {

    private static final boolean USE_CRASH_PROBE_UI = true;

    private static final String CRASH_PROBE_UI = """
            Group {
              LayoutMode: Middle;

              Label {
                Text: "AthenaPress GUI-Test";
                Anchor: (Width: 360, Height: 80);
                Style: (FontSize: 20, HorizontalAlignment: Center, VerticalAlignment: Center);
              }
            }
            """;

    private static final String INLINE_UI = """
            Group {
              Background: (Color: #000000(0.82));
              LayoutMode: Middle;

              Group {
                Background: (Color: #1a2436);
                Anchor: (Width: 420, Height: 380);
                LayoutMode: Top;
                Padding: (Horizontal: 24, Top: 16, Bottom: 8);

                Label {
                  Text: "Athena Botenblatt";
                  Anchor: (Height: 36);
                  Style: (FontSize: 20, RenderBold: true, HorizontalAlignment: Center,
                          TextColor: #bfcdd5, VerticalAlignment: Center);
                }

                Group #Content {
                  LayoutMode: Top;
                  Padding: (Top: 8);

                  TextButton #BtnReadNewspaper {
                    Text: "Zeitung lesen";
                    Anchor: (Height: 44);
                    Padding: (Full: 8);
                    FlexWeight: 1;
                  }

                  TextButton #BtnGiveCamera {
                    Text: "Kamera holen";
                    Anchor: (Height: 44);
                    Padding: (Full: 8);
                    FlexWeight: 1;
                  }

                  Group #AdminSection {
                    LayoutMode: Top;

                    Label {
                      Anchor: (Height: 24);
                      Padding: (Top: 10, Bottom: 4);
                      Style: (FontSize: 11, TextColor: #96a9be, HorizontalAlignment: Center,
                              VerticalAlignment: Center);
                      Text: "— Redaktion —";
                    }

                    TextButton #BtnNewArticle {
                      Text: "Artikel schreiben";
                      Anchor: (Height: 44);
                      Padding: (Full: 8);
                      FlexWeight: 1;
                    }

                    TextButton #BtnNewIssue {
                      Text: "Ausgabe erstellen";
                      Anchor: (Height: 44);
                      Padding: (Full: 8);
                      FlexWeight: 1;
                    }
                  }
                }

                Group #NavBar {
                  LayoutMode: Left;
                  Anchor: (Height: 52);
                  Padding: (Horizontal: 16, Top: 8);

                  TextButton #BtnClose {
                    Text: "Schließen";
                    FlexWeight: 1;
                  }
                }
              }
            }
            """;

    private final boolean isAdmin;
    private final Consumer<String> onCommand;

    public MainMenuPage(PlayerRef hytaleRef, boolean isAdmin, Consumer<String> onCommand) {
        super(hytaleRef, CustomPageLifetime.CanDismissOrCloseThroughInteraction);
        this.isAdmin = isAdmin;
        this.onCommand = onCommand;
    }

    @Override
    public void build(
            Ref<EntityStore> ref,
            UICommandBuilder ui,
            UIEventBuilder events,
            Store<EntityStore> store
    ) {
        if (USE_CRASH_PROBE_UI) {
            ui.appendInline(null, CRASH_PROBE_UI);
            return;
        }

        ui.appendInline(null, INLINE_UI);

        if (!isAdmin) {
            ui.remove("#AdminSection");
        }

        events.addEventBinding(CustomUIEventBindingType.Activating, "#BtnReadNewspaper",
                EventData.of("cmd", "open_newspaper"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#BtnGiveCamera",
                EventData.of("cmd", "give_camera"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#BtnClose",
                EventData.of("cmd", "close"));
        events.addEventBinding(CustomUIEventBindingType.Dismissing, null,
                EventData.of("cmd", "close"));

        if (isAdmin) {
            events.addEventBinding(CustomUIEventBindingType.Activating, "#BtnNewArticle",
                    EventData.of("cmd", "start_article_editor"));
            events.addEventBinding(CustomUIEventBindingType.Activating, "#BtnNewIssue",
                    EventData.of("cmd", "start_issue_editor"));
        }
    }

    @Override
    public void handleDataEvent(
            Ref<EntityStore> ref, Store<EntityStore> store, String json
    ) {
        String cmd = extractField(json, "cmd");
        if (cmd != null) onCommand.accept(cmd);
    }

    public void requestClose() { close(); }

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
