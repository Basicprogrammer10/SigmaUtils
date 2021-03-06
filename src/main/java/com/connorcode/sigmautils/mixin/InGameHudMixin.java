package com.connorcode.sigmautils.mixin;

import com.connorcode.sigmautils.config.Config;
import com.connorcode.sigmautils.misc.Raycast;
import com.connorcode.sigmautils.modules._interface.ChatPosition;
import com.connorcode.sigmautils.modules._interface.HotbarPosition;
import com.connorcode.sigmautils.modules.hud.Hud;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Objects;

import static com.connorcode.sigmautils.config.ConfigGui.getPadding;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    @Shadow
    @Final
    private MinecraftClient client;

    @Shadow
    private int scaledWidth;

    @Shadow
    private int scaledHeight;

    @Shadow
    public abstract TextRenderer getTextRenderer();

    @Inject(method = "renderCrosshair", at = @At("TAIL"))
    void onRenderCrosshair(MatrixStack matrices, CallbackInfo ci) {
        if (!Config.getEnabled("block_distance")) return;
        Vec3d cameraDirection = Objects.requireNonNull(client.cameraEntity)
                .getRotationVec(client.getTickDelta());
        double fov = client.options.getFov()
                .getValue();
        double angleSize = fov / scaledHeight;
        Vec3f verticalRotationAxis = new Vec3f(cameraDirection);
        verticalRotationAxis.cross(Vec3f.POSITIVE_Y);
        if (!verticalRotationAxis.normalize()) return;

        Vec3f horizontalRotationAxis = new Vec3f(cameraDirection);
        horizontalRotationAxis.cross(verticalRotationAxis);
        horizontalRotationAxis.normalize();

        verticalRotationAxis = new Vec3f(cameraDirection);
        verticalRotationAxis.cross(horizontalRotationAxis);

        Vec3d direction = Raycast.map((float) angleSize, cameraDirection, horizontalRotationAxis, verticalRotationAxis,
                scaledWidth / 2, scaledHeight / 2, scaledWidth, scaledHeight);
        HitResult hitResult = Raycast.raycastInDirection(client, client.getTickDelta(), 500, direction);
        if (Objects.requireNonNull(hitResult)
                .getType() == HitResult.Type.MISS) {
            return;
        }
        double distance = Objects.requireNonNull(hitResult)
                .getPos()
                .distanceTo(Objects.requireNonNull(client.player)
                        .getPos());

        String text = String.format("??f[%d]", (int) distance);
        client.textRenderer.draw(matrices, Text.of(text), scaledWidth / 2f - client.textRenderer.getWidth(text) / 2f,
                scaledHeight / 2f + client.textRenderer.fontHeight, 0);
    }

    @Inject(method = "render", at = @At("TAIL"))
    void onRender(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        if (client.options.hudHidden || !Config.getEnabled("hud")) return;

        int padding = getPadding();
        Pair<List<String>, Pair<Integer, Integer>> hud = Hud.getHud(scaledHeight, scaledWidth);

        int y = hud.getRight()
                .getRight();
        int maxText = 0;
        for (String i : hud.getLeft()) maxText = Math.max(maxText, getTextRenderer().getWidth(i));
        for (String i : hud.getLeft()) {
            client.textRenderer.drawWithShadow(matrices, i, Hud.location == 0 || Hud.location == 3 ? hud.getRight()
                    .getLeft() : hud.getRight()
                    .getLeft() + (maxText - getTextRenderer().getWidth(i)), y, 0);
            y += client.textRenderer.fontHeight + padding;
        }
    }

    @Redirect(method = "renderScoreboardSidebar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;getWidth(Ljava/lang/String;)I"))
    int onGetWidth(TextRenderer instance, String text) {
        if (Config.getEnabled("no_scoreboard_value")) return 0;
        return instance.getWidth(text);
    }


    @Redirect(method = "renderScoreboardSidebar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;draw(Lnet/minecraft/client/util/math/MatrixStack;Ljava/lang/String;FFI)I"))
    int onDraw(TextRenderer instance, MatrixStack matrices, String text, float x, float y, int color) {
        if (Config.getEnabled("no_scoreboard_value")) return 0;
        return instance.draw(matrices, text, x, y, color);
    }

    @Redirect(method = "renderScoreboardSidebar", at = @At(value = "INVOKE", target = "Ljava/lang/Integer;toString(I)Ljava/lang/String;"))
    String onToString(int buf) {
        if (Config.getEnabled("no_scoreboard_value")) return "";
        return Integer.toString(buf);
    }

    int getHotbarPos() {
        return Config.getEnabled("hotbar_position") ? HotbarPosition.yPosition : 0;
    }

    // Modified from https://github.com/yurisuika/Raise
    @ModifyArg(method = "renderHotbar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;drawTexture(Lnet/minecraft/client/util/math/MatrixStack;IIIIII)V"), index = 2)
    private int modifyHotbar(int value) {
        return value - getHotbarPos();
    }

    @ModifyArg(method = "renderHotbar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;drawTexture(Lnet/minecraft/client/util/math/MatrixStack;IIIIII)V", ordinal = 1), index = 6)
    private int modifySelectorHeight(int value) {
        return value + 2;
    }

    @ModifyArg(method = "renderHotbar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderHotbarItem(IIFLnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/ItemStack;I)V"), index = 1)
    private int modifyItem(int value) {
        return value - getHotbarPos();
    }

    @ModifyVariable(method = "renderMountJumpBar", at = @At(value = "STORE"), ordinal = 3)
    private int modifyJumpBar(int value) {
        return value - getHotbarPos();
    }

    @ModifyArg(method = "renderExperienceBar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;drawTexture(Lnet/minecraft/client/util/math/MatrixStack;IIIIII)V"), index = 2)
    private int modifyExperienceBar(int value) {
        return value - getHotbarPos();
    }

    @ModifyArg(method = "renderExperienceBar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;draw(Lnet/minecraft/client/util/math/MatrixStack;Ljava/lang/String;FFI)I"), index = 3)
    private float modifyXpText(float value) {
        return value - getHotbarPos();
    }

    @ModifyVariable(method = "renderHeldItemTooltip", at = @At(value = "STORE"), ordinal = 2)
    private int modifyHeldItemTooltip(int value) {
        return value - getHotbarPos();
    }

    @ModifyVariable(method = "renderStatusBars", at = @At(value = "STORE"), ordinal = 5)
    private int modifyStatusBars(int value) {
        return value - getHotbarPos();
    }

    @ModifyVariable(method = "renderMountHealth", at = @At(value = "STORE"), ordinal = 2)
    private int modifyMountHealth(int value) {
        return value - getHotbarPos();
    }

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;translate(DDD)V", ordinal = 0), index = 1)
    private double modifyActionbar(double value) {
        return value - getHotbarPos();
    }

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;translate(DDD)V", ordinal = 2), index = 1)
    private double modifyChat(double value) {
        if (Config.getEnabled("chat_position")) return value - ChatPosition.yPosition * client.textRenderer.fontHeight;
        return value;
    }
    // End
}
