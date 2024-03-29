package com.connorcode.sigmautils.mixin;

import com.connorcode.sigmautils.modules.misc.PrintDeathCords;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

import static com.connorcode.sigmautils.SigmaUtils.client;

@Mixin(DeathScreen.class)
public class DeathScreenMixin {
    @Inject(method = "init", at = @At("TAIL"))
    void onDeath(CallbackInfo ci) {
        Entity player = Objects.requireNonNull(client.player);
        PrintDeathCords.lastDeath = new double[]{
                player.getX(),
                player.getY(),
                player.getZ()
        };
    }
}
