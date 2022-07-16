package com.connorcode.sigmautils;

import com.connorcode.sigmautils.config.Config;
import com.connorcode.sigmautils.module.Module;
import com.connorcode.sigmautils.modules._interface.*;
import com.connorcode.sigmautils.modules.hud.*;
import com.connorcode.sigmautils.modules.meta.Padding;
import com.connorcode.sigmautils.modules.misc.*;
import com.connorcode.sigmautils.modules.rendering.*;
import com.mojang.logging.LogUtils;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;

import java.io.IOException;

@Environment(EnvType.CLIENT)
public class SigmaUtilsClient implements ClientModInitializer {
    public static Module[] modules = new Module[]{
            new BetterSplashes(),
            new BiomeHud(),
            new BlockDistance(),
            new CameraClip(),
            new CameraDistance(),
            new ChatMessageDing(),
            new ChatPosition(),
            new CoordinatesHud(),
            new Deadmau5Ears(),
            new DisableFrontPerspective(),
            new DisableShadows(),
            new FlippedEntities(),
            new FpsHud(),
            new FullBright(),
            new GlowingPlayers(),
            new Hud(),
            new NoBossBarValue(),
            new NoBreakParticles(),
            new NoChatFade(),
            new NoFog(),
            new NoParticles(),
            new NoScoreboardValue(),
            new Padding(),
            new PrintDeathCords(),
            new RandomBackground(),
            new ServerHud(),
            new ShowInvisibleEntities(),
            new TickSpeed(),
            new WatermarkHud(),
            new Zoom()
    };

    @Override
    public void onInitializeClient() {
        LogUtils.getLogger()
                .info("Starting Sigma Utils");
        Config.initKeybindings();
        for (Module i : modules) i.init();
        ClientLifecycleEvents.CLIENT_STARTED.register((client -> {
            try {
                Config.load();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));
    }
}
