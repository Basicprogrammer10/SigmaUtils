package com.connorcode.sigmautils.mixin;

import com.connorcode.sigmautils.config.Config;
import com.connorcode.sigmautils.modules._interface.ChatPosition;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ChatHud.class)
public abstract class ChatHudMixin {
    @Shadow
    @Final
    private MinecraftClient client;

    @Shadow
    protected abstract boolean isChatFocused();

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/ChatHud;isChatFocused()Z"))
    boolean isChatFocused(ChatHud instance) throws Exception {
        if (Config.getEnabled("no_chat_fade")) return true;
        return isChatFocused();
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;translate(DDD)V", ordinal = 0))
    void onChatTranslate(MatrixStack instance, double x, double y, double z) throws Exception {
        if (Config.getEnabled("chat_position"))
            instance.translate(x, y - (ChatPosition.yPosition * client.textRenderer.fontHeight), z);
        else instance.translate(x, y, z);
    }
}
