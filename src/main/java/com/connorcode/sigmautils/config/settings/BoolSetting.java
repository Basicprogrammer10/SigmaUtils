package com.connorcode.sigmautils.config.settings;

import com.connorcode.sigmautils.SigmaUtils;
import com.connorcode.sigmautils.misc.util.Util;
import com.connorcode.sigmautils.mixin.ScreenAccessor;
import com.connorcode.sigmautils.module.Module;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;

import static com.connorcode.sigmautils.SigmaUtils.client;
import static com.mojang.brigadier.arguments.BoolArgumentType.bool;


public class BoolSetting extends Setting<BoolSetting> {
    boolean value;
    DisplayType displayType = DisplayType.CHECKBOX;

    public BoolSetting(Class<? extends Module> module, String name) {
        super(module, name);
    }

    @Override
    protected BoolSetting getThis() {
        return this;
    }

    public BoolSetting build() {
        ClientCommandRegistrationCallback.EVENT.register(
            (dispatcher, registryAccess) -> {
                String moduleId = SigmaUtils.modules.get(this.module).id;
                dispatcher.register(ClientCommandManager.literal("util")
                                        .then(ClientCommandManager.literal("config")
                                                  .then(ClientCommandManager.literal(moduleId)
                                                            .then(ClientCommandManager.literal(this.id)
                                                                      .executes(context -> {
                                                                          context.getSource()
                                                                              .sendFeedback(
                                                                                  Text.of(String.format("%s::%s: %s", moduleId,
                                                                                                        this.id, this.value)));
                                                                          return 1;
                                                                      })
                                                                      .then(ClientCommandManager.argument("value", bool())
                                                                                .executes(context -> {
                                                                                    this.value =
                                                                                        context.getArgument("value", Boolean.class);
                                                                                    context.getSource()
                                                                                        .sendFeedback(
                                                                                            Text.of(String.format(
                                                                                                "Set %s::%s to %s",
                                                                                                moduleId, this.id,
                                                                                                this.value)));
                                                                                    return 1;
                                                                                }))))));
            });

        return super.build();
    }

    public BoolSetting value(boolean value) {
        this.value = value;
        return this;
    }

    public BoolSetting displayType(DisplayType displayType) {
        this.displayType = displayType;
        return this;
    }

    public boolean value() {
        return this.value;
    }

    @Override
    public void serialize(NbtCompound nbt) {
        nbt.putBoolean(this.id, this.value);
    }

    @Override
    public void deserialize(NbtCompound nbt) {
        if (!nbt.contains(this.id)) return;
        this.value = nbt.getBoolean(this.id);
    }

    @Override
    public int initRender(Screen screen, int x, int y, int width) {
        switch (this.displayType) {
            case BUTTON -> ButtonWidget.builder(Text.of(String.format("%s: %s", this.name, this.value ? "On" : "Off")),
                                                button -> {
                                                    BoolSetting.this.value ^= true;
                                                    ((ScreenAccessor) screen).invokeClearAndInit();
                                                }).position(x, y).size(width, 20).tooltip(Util.nullMap(getDescription(), Tooltip::of)).build();
            case CHECKBOX -> Util.addChild(screen, CheckboxWidget.builder(Text.of(this.name), client.textRenderer)
                .pos(x, y)
                .checked(this.value)
                .callback((box, value) -> this.value = value)
                .tooltip(Tooltip.of(Text.of(this.description)))
                .build());
        }

        return 20;
    }

    @Override
    public void render(RenderData data, int x, int y) {

    }

    public enum DisplayType {
        BUTTON,
        CHECKBOX
    }
}
