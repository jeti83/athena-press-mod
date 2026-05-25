package pro.jeti.athenapress.plugin.ui;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public class UiEventData {

    public static final BuilderCodec<UiEventData> CODEC = BuilderCodec
            .builder(UiEventData.class, UiEventData::new)
            .append(new KeyedCodec<>("Cmd", Codec.STRING, false),
                    (data, value) -> data.cmd = value,
                    data -> data.cmd)
            .add()
            .append(new KeyedCodec<>("Val", Codec.STRING, false),
                    (data, value) -> data.val = value,
                    data -> data.val)
            .add()
            .build();

    private String cmd;
    private String val;

    public String cmd() {
        return cmd;
    }

    public String val() {
        return val;
    }
}
