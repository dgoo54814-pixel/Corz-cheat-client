package dev.corzstyle.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

public final class CorzClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Loading the module catalog eagerly ensures the config sees every feature.
        Features.get("aim_assist");
        SettingsStore.load();
        CorzCommands.register();
        var category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("corzstyle", "main"));
        KeyMapping openGui = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.corzstyle.open", InputConstants.Type.KEYSYM, InputConstants.KEY_RIGHT_SHIFT, category));
        KeyMapping pearl = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.corzstyle.pearl", InputConstants.Type.KEYSYM, InputConstants.KEY_V, category));

        KeyMapping build = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.corzstyle.build", InputConstants.Type.KEYSYM, InputConstants.KEY_B, category));
        KeyMapping ahSell = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.corzstyle.ahsell", InputConstants.Type.KEYSYM, InputConstants.KEY_H, category));
        KeyMapping rtp = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.corzstyle.rtp", InputConstants.Type.KEYSYM, InputConstants.KEY_J, category));
        KeyMapping scan = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.corzstyle.scan", InputConstants.Type.KEYSYM, InputConstants.KEY_K, category));

        ClientTickEvents.END_CLIENT_TICK.register((Minecraft mc) -> {
            while (openGui.consumeClick()) {
                if (mc.screen instanceof CorzScreen) mc.setScreen(null);
                else mc.setScreen(new CorzScreen(mc.screen));
            }
            while (pearl.consumeClick()) ModuleEngine.keyPearl(mc);
            while (build.consumeClick()) ServerUtilities.tell(mc, SchematicBuilder.toggle(mc));
            while (ahSell.consumeClick()) ServerUtilities.ahSell(mc);
            while (rtp.consumeClick()) ServerUtilities.rtpAndScan(mc);
            while (scan.consumeClick()) ServerUtilities.scan(mc);
            SchematicBuilder.tick(mc);
            ServerUtilities.tick(mc);
            ModuleEngine.tick(mc);
        });
    }
}
