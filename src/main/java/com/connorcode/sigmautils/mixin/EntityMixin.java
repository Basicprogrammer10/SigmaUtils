package com.connorcode.sigmautils.mixin;

import com.connorcode.sigmautils.config.Config;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin {
    @Inject(method = "isInvisible", at = @At("HEAD"), cancellable = true)
    void isInvisible(CallbackInfoReturnable<Boolean> cir) throws Exception {
        if (!Config.getEnabled("show_invisible_entities")) return;
        cir.setReturnValue(false);
    }

    @Inject(method = "isInvisibleTo", at = @At("HEAD"), cancellable = true)
    void isInvisibleTo(PlayerEntity player, CallbackInfoReturnable<Boolean> cir) throws Exception {
        if (!Config.getEnabled("show_invisible_entities")) return;
        cir.setReturnValue(false);
    }
}
