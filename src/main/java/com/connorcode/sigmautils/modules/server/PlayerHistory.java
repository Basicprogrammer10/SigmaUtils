package com.connorcode.sigmautils.modules.server;

import com.connorcode.sigmautils.SigmaUtils;
import com.connorcode.sigmautils.config.settings.DummySetting;
import com.connorcode.sigmautils.event.PacketReceiveCallback;
import com.connorcode.sigmautils.module.Category;
import com.connorcode.sigmautils.module.Module;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.packet.s2c.login.LoginSuccessS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

import static com.connorcode.sigmautils.config.ConfigGui.getPadding;

public class PlayerHistory extends Module {
    private static final File playerFile =
            new File(MinecraftClient.getInstance().runDirectory, "config/SigmaUtils/players.nbt");
    private static final HashMap<String, HashMap<UUID, SeenPlayer>> seenPlayers = new HashMap<>();
    private static final boolean wasEnabled = false;

    public PlayerHistory() {
        super("player_history", "Player History", "Logs every player you see on servers. Kinda cool. [EXPERIMENTAL]",
                Category.Server);
    }

    @Override
    public void init() {
        super.init();
        PlayerHistoryDisplay.init();

        try {
            loadPlayers();
        } catch (IOException e) {
            SigmaUtils.logger.error("Failed to load player history file");
            e.printStackTrace();
        }

        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            try {
                savePlayers();
            } catch (IOException e) {
                SigmaUtils.logger.error("Failed to save player history file");
                e.printStackTrace();
            }
        });

        PacketReceiveCallback.EVENT.register(packet -> {
            var client = MinecraftClient.getInstance();
            if (packet.get() instanceof LoginSuccessS2CPacket) {
                synchronized (seenPlayers) {
                    seenPlayers.values().forEach(server -> server.values().forEach(p -> p.setOnline(false)));
                }
                return;
            }

            if (!(packet.get() instanceof PlayerListS2CPacket playerListS2CPacket) ||
                    playerListS2CPacket.getAction() != PlayerListS2CPacket.Action.ADD_PLAYER ||
                    client.getCurrentServerEntry() == null) return;

            synchronized (seenPlayers) {
                for (PlayerListS2CPacket.Entry entry : playerListS2CPacket.getEntries()) {
                    var uuid = entry.getProfile().getId();
                    seenPlayers.putIfAbsent(client.getCurrentServerEntry().address, new HashMap<>());
                    var server = seenPlayers.get(client.getCurrentServerEntry().address);

                    if (server.containsKey(uuid)) server.get(uuid).update();
                    else server.put(uuid, new SeenPlayer(entry.getProfile().getName(), uuid));
                }
            }
        });
    }

    void loadPlayers() throws IOException {
        if (!playerFile.exists()) return;
        var nbt = NbtIo.readCompressed(PlayerHistory.playerFile);
        synchronized (seenPlayers) {
            for (var server : nbt.getKeys()) {
                var players = new HashMap<UUID, SeenPlayer>();
                for (var player : nbt.getCompound(server).getKeys()) {
                    var seenPlayer = SeenPlayer.deserialize(nbt.getCompound(server).getCompound(player),
                            UUID.fromString(player));
                    players.put(seenPlayer.uuid, seenPlayer);
                }
                seenPlayers.put(server, players);
            }
        }
    }

    void savePlayers() throws IOException {
        var nbt = new NbtCompound();
        synchronized (seenPlayers) {
            for (var server : seenPlayers.keySet()) {
                var serverNbt = new NbtCompound();
                for (var player : seenPlayers.get(server).values()) {
                    serverNbt.put(player.uuid.toString(), player.serialize());
                }
                nbt.put(server, serverNbt);
            }
        }
        NbtIo.writeCompressed(nbt, playerFile);
    }

    static class SeenPlayer {
        private final String name;
        private final UUID uuid;
        private boolean online;
        private int count;

        public SeenPlayer(String name, UUID uuid, int count, boolean online) {
            this.name = name;
            this.uuid = uuid;
            this.count = count;
            this.online = online;
        }

        public SeenPlayer(String name, UUID uuid) {
            this(name, uuid, 1, true);
        }

        static SeenPlayer deserialize(NbtCompound nbt, UUID uuid) {
            return new SeenPlayer(nbt.getString("name"), uuid, nbt.getInt("count"), false);
        }

        public void update() {
            if (!online) this.count++;
            this.online = true;
        }

        public void setOnline(boolean online) {
            this.online = online;
        }

        NbtCompound serialize() {
            NbtCompound nbt = new NbtCompound();
            nbt.putString("name", this.name);
            nbt.putInt("count", this.count);
            return nbt;
        }
    }

    static class PlayerHistoryDisplay {
        static int totalSeen;
        static int thisServer;

        static void init() {
            new DummySetting(PlayerHistory.class, "Total Seen Players", 0) {
                @Override
                public int initRender(Screen screen, int x, int y, int width) {
                    synchronized (seenPlayers) {
                        totalSeen = seenPlayers.values().stream().map(HashMap::size).reduce(0, Integer::sum);
                    }
                    return MinecraftClient.getInstance().textRenderer.fontHeight + getPadding() * 2;
                }

                @Override
                public void render(RenderData data, int x, int y) {
                    MinecraftClient.getInstance().textRenderer.draw(data.matrices(),
                            "§fTotal Seen Players: " + totalSeen, x, y + getPadding(), 0);
                }
            }.category("Info").build();

            new DummySetting(PlayerHistory.class, "Seen on Current Server", 0) {
                @Override
                public int initRender(Screen screen, int x, int y, int width) {
                    var client = MinecraftClient.getInstance();
                    if (client.getCurrentServerEntry() != null) {
                        synchronized (seenPlayers) {
                            thisServer =
                                    seenPlayers.getOrDefault(client.getCurrentServerEntry().address, new HashMap<>())
                                            .size();
                        }
                    }

                    return client.textRenderer.fontHeight + getPadding() * 2;
                }

                @Override
                public void render(RenderData data, int x, int y) {
                    if (MinecraftClient.getInstance().getCurrentServerEntry() == null) return;
                    MinecraftClient.getInstance().textRenderer.draw(data.matrices(),
                            "§fSeen on Current Server: " + thisServer, x, y + getPadding(), 0);
                }
            }.category("Info").build();
        }
    }
}