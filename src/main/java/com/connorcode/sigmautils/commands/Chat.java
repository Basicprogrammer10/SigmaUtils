package com.connorcode.sigmautils.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;

public class Chat implements Command {
    static int execute(CommandContext<FabricClientCommandSource> context) {
        String msg = getString(context, "text");
        context.getSource()
                .getPlayer()
                .sendChatMessage(msg, null);
        return 0;
    }

    @Override
    public void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommandManager.literal("util")
                .then(ClientCommandManager.literal("chat")
                        .then(ClientCommandManager.argument("text", greedyString())
                                .executes(Chat::execute))));
    }
}
