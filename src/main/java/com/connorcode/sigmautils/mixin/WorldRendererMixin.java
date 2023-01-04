package com.connorcode.sigmautils.mixin;

import com.connorcode.sigmautils.config.Config;
import com.connorcode.sigmautils.modules.misc.NoGlobalSounds;
import com.connorcode.sigmautils.modules.rendering.NoDarkSky;
import com.connorcode.sigmautils.modules.rendering.NoWorldBorder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.HeightLimitView;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

import static net.minecraft.sound.SoundEvents.*;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
    @Shadow
    @Nullable
    private ClientWorld world;

    @Inject(method = "processGlobalEvent", at = @At("HEAD"))
    void onProcessGlobalEvent(int eventId, BlockPos pos, int data, CallbackInfo ci) {
        if (!Config.getEnabled(NoGlobalSounds.class)) return;
        SoundEvent sound = switch (eventId) {
            case 1023 -> ENTITY_WITHER_SPAWN;
            case 1038 -> BLOCK_END_PORTAL_SPAWN;
            case 1028 -> ENTITY_ENDER_DRAGON_DEATH;
            default -> null;
        };

        if (sound == null || NoGlobalSounds.disabled(eventId)) return;
        Objects.requireNonNull(world)
                .playSound(pos, sound, SoundCategory.HOSTILE, 1.0F, 1.0F, false);
    }

    @Redirect(method = "renderSky(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/util/math/Matrix4f;FLnet/minecraft/client/render/Camera;ZLjava/lang/Runnable;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld$Properties;getSkyDarknessHeight(Lnet/minecraft/world/HeightLimitView;)D"))
    double onGetSkyDarknessHeight(ClientWorld.Properties instance, HeightLimitView world) {
        if (Config.getEnabled(NoDarkSky.class) && this.world != null) return this.world.getBottomY() - 32;
        return instance.getSkyDarknessHeight(world);
    }

    @Inject(method = "renderWorldBorder", at = @At("HEAD"), cancellable = true)
    void onRenderWorldBorder(Camera camera, CallbackInfo ci) {
        if (Config.getEnabled(NoWorldBorder.class)) ci.cancel();
    }
}
