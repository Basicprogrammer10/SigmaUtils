package com.connorcode.sigmautils.modules.hud;

import com.connorcode.sigmautils.config.ModuleInfo;
import com.connorcode.sigmautils.config.settings.StringSetting;
import com.connorcode.sigmautils.misc.TextStyle;
import com.connorcode.sigmautils.module.HudModule;

@ModuleInfo(description = "Adds a \"Sigma Utils\" watermark to the Hud")
public class WatermarkHud extends HudModule {
    public static StringSetting watermark = new StringSetting(WatermarkHud.class, "Watermark").value("Sigma Utils")
            .description("The text displayed as the watermark")
            .build();

    public WatermarkHud() {
        super();
        this.defaultTextColor = TextStyle.Color.Green;
        this.defaultOrder = 0;
    }

    public String line() {
        return String.format("§r§l%s%s", this.getTextColor(), watermark.value());
    }
}
