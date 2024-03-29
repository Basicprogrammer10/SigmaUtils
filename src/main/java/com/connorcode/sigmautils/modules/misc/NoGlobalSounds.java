package com.connorcode.sigmautils.modules.misc;

import com.connorcode.sigmautils.config.settings.BoolSetting;
import com.connorcode.sigmautils.module.Module;
import com.connorcode.sigmautils.module.ModuleInfo;

@ModuleInfo(description = "Disables playing all global sounds. (Wither Spawn, End Portal Open, Dragon Death)")
public class NoGlobalSounds extends Module {
    public static BoolSetting disableWither = new BoolSetting(NoGlobalSounds.class, "Disable Wither").value(true)
            .description("Disables the global wither spawn sound")
            .build();
    public static BoolSetting disableEndPortal = new BoolSetting(NoGlobalSounds.class, "Disable End Portal").value(true)
            .description("Disables the global end portal opening sound")
            .build();
    public static BoolSetting disableDragonDeath =
            new BoolSetting(NoGlobalSounds.class, "Disable Dragon Death").value(true)
                    .description("Disables the global dragon death sound")
                    .build();

    public static boolean disabled(int eventId) {
        return switch (eventId) {
            case 1023 -> disableWither.value();
            case 1038 -> disableEndPortal.value();
            case 1028 -> disableDragonDeath.value();
            default -> false;
        };
    }
}
