package com.connorcode.sigmautils.module;

import com.connorcode.sigmautils.misc.Util;
import com.connorcode.sigmautils.mixin.ScreenAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;

public abstract class Module {
    public final String id;
    public final String name;
    public final String description;
    public final Category category;
    public boolean enabled;

    protected Module(String id, String name, String description, Category category) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
    }

    public void drawConfigInterface(MinecraftClient client, Screen screen, int x, int y) {
        ScreenAccessor sa = (ScreenAccessor) screen;
        Util.addDrawable(screen,
                new ButtonWidget(x, y, 150, 20, Text.of(String.format("%s█§r %s", enabled ? "§a" : "§c", name)),
                        button -> {
                            enabled ^= true;
                            if (enabled) enable(client);
                            else disable(client);
                            sa.invokeClearAndInit();
                        }, ((button, matrices, mouseX, mouseY) -> screen.renderOrderedTooltip(matrices,
                        sa.getTextRenderer()
                                .wrapLines(Text.of(description), 200), mouseX, mouseY))));
    }

    public void loadConfig(NbtCompound config) {
    }

    public NbtCompound saveConfig() {
        return new NbtCompound();
    }

    public void init() {
    }

    public void enable(MinecraftClient client) {
    }

    public void disable(MinecraftClient client) {
    }
}
