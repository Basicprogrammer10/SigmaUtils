package com.connorcode.sigmautils.config.settings.list;

import com.connorcode.sigmautils.config.settings.DynamicSelectorSetting;
import com.connorcode.sigmautils.misc.util.Util;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static com.connorcode.sigmautils.SigmaUtils.client;
import static com.connorcode.sigmautils.modules.meta.Padding.getPadding;


public abstract class SimpleSelector<T> implements DynamicSelectorSetting.ResourceManager<T> {
    protected Registry<T> registry;
    protected DynamicSelectorSetting<T> setting;

    public SimpleSelector(DynamicSelectorSetting<T> setting, Registry<T> registry) {
        this.setting = setting;
        this.registry = registry;
    }

    public static <T> void selector(DynamicSelectorSetting<T> setting, T resource, String display, Screen screen, int x, int y, int gap) {
        var padding = getPadding();
        Util.addChild(screen, ButtonWidget.builder(Text.of("+"), button -> {
            setting.setValue(resource);
            screen.close();
        }).position(x, y).size(20, 20).tooltip(Tooltip.of(Text.of("Select"))).build());
        Util.addDrawable(screen, (matrices, mouseX, mouseY, delta) ->
                matrices.drawText(client.textRenderer, display, x + 20 + padding * 4 + gap,
                        (int) (y + padding / 2f + 10 - client.textRenderer.fontHeight / 2f), 0xFFFFFF, false)
        );
    }

    @Override
    public List<T> getAllResources() {
        return registry.stream().filter(this::filter).toList();
    }

    protected boolean filter(T resource) {
        return true;
    }

    @Override
    public String[] getSearch(T resource) {
        return new String[]{
                Objects.requireNonNull(registry.getId(resource)).toString(),
                getDisplay(resource).toLowerCase(Locale.ROOT)
        };
    }

    @Override
    public boolean renderSelector(T resource, Screen screen, int x, int y) {
        if (setting.value() == resource)
            return false;
        selector(setting, resource, getDisplay(resource), screen, x, y, 0);
        return true;
    }

    @Override
    public NbtElement serialize(T resources) {
        if (resources == null) return null;
        return NbtString.of(Objects.requireNonNull(registry.getId(resources)).toString());
    }

    @Override
    public T deserialize(NbtElement nbt) {
        if (!(nbt instanceof NbtString nbtString) || registry == null)
            return null;
        return registry.get(Identifier.tryParse(nbtString.asString()));
    }
}
