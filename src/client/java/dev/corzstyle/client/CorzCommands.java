package dev.corzstyle.client;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/** Client-only commands: no op privileges or custom server packets required. */
public final class CorzCommands {
    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, access) -> dispatcher.register(
            ClientCommands.literal("corz")
              .then(ClientCommands.literal("load")
                .then(ClientCommands.argument("file", StringArgumentType.string()).executes(ctx -> {
                    feedback(SchematicBuilder.load(StringArgumentType.getString(ctx,"file")));
                    return 1;
                })))
              .then(ClientCommands.literal("build").executes(ctx -> {
                  feedback(SchematicBuilder.toggle(Minecraft.getInstance()));return 1;
              }))
              .then(ClientCommands.literal("ah")
                .then(ClientCommands.argument("price", IntegerArgumentType.integer(1,1_000_000_000)).executes(ctx -> {
                    int amount=IntegerArgumentType.getInteger(ctx,"price");
                    ServerUtilities.price(amount);
                    SettingsStore.save();
                    feedback("AH price set to " + amount + ". Hold an item, press H to list it.");
                    return 1;
                })))
              .then(ClientCommands.literal("rtp").executes(ctx -> {
                  ServerUtilities.rtpAndScan(Minecraft.getInstance());return 1;
              }))
              .then(ClientCommands.literal("scan").executes(ctx -> {
                  ServerUtilities.scan(Minecraft.getInstance());return 1;
              }))
              .then(ClientCommands.literal("status").executes(ctx -> {
                  feedback(SchematicBuilder.status() + " | " + ServerUtilities.status());return 1;
              }))
        ));
    }
    private static void feedback(String message) {
        var mc=Minecraft.getInstance();
        if (mc.player!=null) mc.player.sendSystemMessage(Component.literal("[CorzStyle] " + message));
    }
    private CorzCommands() {}
}
