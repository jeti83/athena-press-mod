package pro.jeti.athenapress.plugin.ui;

import java.util.function.Consumer;

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

public class MainMenuPage extends InteractiveCustomUIPage<UiEventData> {

    public enum ProbeMode {
        MINIMAL,
        PANEL,
        BUTTONS,
        EVENTS,
        EVENTS_ACTIVATE,
        EVENTS_ACTIVATE_NO_DATA,
        EVENTS_ACTIVATE_UNLOCKED,
        EVENTS_DISMISS,
        NORMAL
    }

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

    private static final String PANEL_PROBE_UI = """
            Group {
              Background: (Color: #000000(0.82));
              LayoutMode: Middle;

              Group {
                Background: (Color: #1a2436);
                Anchor: (Width: 420, Height: 220);
                LayoutMode: Top;
                Padding: (Horizontal: 24, Top: 16, Bottom: 8);

                Label {
                  Text: "Athena Botenblatt";
                  Anchor: (Height: 36);
                  Style: (FontSize: 20, RenderBold: true, HorizontalAlignment: Center,
                          TextColor: #bfcdd5, VerticalAlignment: Center);
                }

                Label {
                  Text: "Panel-Probe ohne Buttons";
                  FlexWeight: 1;
                  Style: (FontSize: 14, HorizontalAlignment: Center,
                          TextColor: #d0d8e8, VerticalAlignment: Center);
                }
              }
            }
            """;

    private static final String BUTTON_PROBE_UI = """
            Group {
              Background: (Color: #000000(0.82));
              LayoutMode: Middle;

              Group {
                Background: (Color: #1a2436);
                Anchor: (Width: 420, Height: 300);
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
                  FlexWeight: 1;

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
    private final ProbeMode probeMode;

    public MainMenuPage(PlayerRef hytaleRef, boolean isAdmin, Consumer<String> onCommand) {
        this(hytaleRef, isAdmin, onCommand, ProbeMode.MINIMAL);
    }

    public MainMenuPage(
            PlayerRef hytaleRef,
            boolean isAdmin,
            Consumer<String> onCommand,
            ProbeMode probeMode
    ) {
        super(hytaleRef, CustomPageLifetime.CanDismissOrCloseThroughInteraction, UiEventData.CODEC);
        this.isAdmin = isAdmin;
        this.onCommand = onCommand;
        this.probeMode = probeMode;
    }

    @Override
    public void build(
            Ref<EntityStore> ref,
            UICommandBuilder ui,
            UIEventBuilder events,
            Store<EntityStore> store
    ) {
        switch (probeMode) {
            case MINIMAL -> {
                ui.appendInline(null, CRASH_PROBE_UI);
                return;
            }
            case PANEL -> {
                ui.appendInline(null, PANEL_PROBE_UI);
                return;
            }
            case BUTTONS -> {
                ui.appendInline(null, BUTTON_PROBE_UI);
                return;
            }
            case EVENTS -> {
                ui.appendInline(null, BUTTON_PROBE_UI);
                events.addEventBinding(CustomUIEventBindingType.Activating, "#BtnReadNewspaper",
                        EventData.of("Cmd", "open_newspaper"), false);
                events.addEventBinding(CustomUIEventBindingType.Activating, "#BtnGiveCamera",
                        EventData.of("Cmd", "give_camera"), false);
                return;
            }
            case EVENTS_ACTIVATE -> {
                ui.appendInline(null, BUTTON_PROBE_UI);
                events.addEventBinding(CustomUIEventBindingType.Activating, "#BtnReadNewspaper",
                        EventData.of("Cmd", "open_newspaper"), false);
                events.addEventBinding(CustomUIEventBindingType.Activating, "#BtnGiveCamera",
                        EventData.of("Cmd", "give_camera"), false);
                return;
            }
            case EVENTS_ACTIVATE_NO_DATA -> {
                ui.appendInline(null, BUTTON_PROBE_UI);
                events.addEventBinding(CustomUIEventBindingType.Activating, "#BtnReadNewspaper");
                events.addEventBinding(CustomUIEventBindingType.Activating, "#BtnGiveCamera");
                return;
            }
            case EVENTS_ACTIVATE_UNLOCKED -> {
                ui.appendInline(null, BUTTON_PROBE_UI);
                events.addEventBinding(CustomUIEventBindingType.Activating, "#BtnReadNewspaper",
                        EventData.of("Cmd", "open_newspaper"), false);
                events.addEventBinding(CustomUIEventBindingType.Activating, "#BtnGiveCamera",
                        EventData.of("Cmd", "give_camera"), false);
                return;
            }
            case EVENTS_DISMISS -> {
                ui.appendInline(null, BUTTON_PROBE_UI);
                return;
            }
            case NORMAL -> {
                // fall through below
            }
        }

        ui.appendInline(null, INLINE_UI);

        if (!isAdmin) {
            ui.remove("#AdminSection");
        }

        events.addEventBinding(CustomUIEventBindingType.Activating, "#BtnReadNewspaper",
                EventData.of("Cmd", "open_newspaper"), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#BtnGiveCamera",
                EventData.of("Cmd", "give_camera"), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#BtnClose",
                EventData.of("Cmd", "close"), false);

        if (isAdmin) {
            events.addEventBinding(CustomUIEventBindingType.Activating, "#BtnNewArticle",
                    EventData.of("Cmd", "start_article_editor"), false);
            events.addEventBinding(CustomUIEventBindingType.Activating, "#BtnNewIssue",
                    EventData.of("Cmd", "start_issue_editor"), false);
        }
    }

    @Override
    public void handleDataEvent(
            Ref<EntityStore> ref, Store<EntityStore> store, UiEventData data
    ) {
        String cmd = data != null ? data.cmd() : null;
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
