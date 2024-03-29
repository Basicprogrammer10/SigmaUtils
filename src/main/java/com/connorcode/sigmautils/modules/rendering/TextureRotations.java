package com.connorcode.sigmautils.modules.rendering;

import com.connorcode.sigmautils.config.settings.BoolSetting;
import com.connorcode.sigmautils.config.settings.EnumSetting;
import com.connorcode.sigmautils.config.settings.NumberSetting;
import com.connorcode.sigmautils.module.DocumentedEnum;
import com.connorcode.sigmautils.module.Module;
import com.connorcode.sigmautils.module.ModuleInfo;

import static com.connorcode.sigmautils.SigmaUtils.client;

@ModuleInfo(description = "Allows you to change the seed of the texture rotation hash function")
public class TextureRotations extends Module {
    static EnumSetting<Mode> mode =
            new EnumSetting<>(TextureRotations.class, "Mode", Mode.class).value(Mode.Consistent)
                    .description(
                            "The mode this module will run in. Either disabled texture rotations, or uses the seed you provided.")
                    .build();
    static NumberSetting seed = new NumberSetting(TextureRotations.class, "Seed", 0, Long.MAX_VALUE).value(42317861)
            .precision(0)
            .description("The hash seed used in the 'Seeded' mode, and the value returned in the 'Consistent' mode.")
            .build();
    BoolSetting autoRefresh = new BoolSetting(TextureRotations.class, "Auto Refresh").value(true)
            .description("Automatically rerender world when module is enabled / disabled.")
            .build();

    public static long hash(int x, int y, int z) {
        return switch (mode.value()) {
            case Consistent -> seed.longValue();
            case Seeded -> {
                var l = (x * 3129871L) ^ (long) z * 116129781L ^ (long) y;
                l = l * l * seed.longValue() + l * 11L;
                yield l >> 16;
            }
        };
    }

    @Override
    public void enable() {
        super.enable();
        if (autoRefresh.value()) client.worldRenderer.reload();
    }

    @Override
    public void disable() {
        super.disable();
        if (autoRefresh.value()) client.worldRenderer.reload();
    }

    enum Mode {
        @DocumentedEnum("Uses the same rotation for every block")
        Consistent,
        @DocumentedEnum("Uses a custom seed to generate a random rotation for each block. Default: 42317861")
        Seeded
    }
}
