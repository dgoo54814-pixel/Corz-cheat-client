package dev.corzstyle.client;

import net.fabricmc.api.ClientModInitializer;

/** Legacy Minecraft 1.21.x bootstrapping. Input and ticking use the client Mixin. */
public final class CorzClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Features.get("aim_assist");
        SettingsStore.load();
    }
}
