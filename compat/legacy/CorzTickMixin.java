package dev.corzstyle.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Legacy input adapter, avoiding binary APIs from newer Minecraft releases. */
@Mixin(Minecraft.class)
public abstract class CorzTickMixin {
    private static boolean oldMenu, oldPearl, oldBuild, oldAh, oldRtp, oldScan;

    @Inject(method = "tick", at = @At("TAIL"))
    private void corzTick(CallbackInfo ci) {
        Minecraft mc = (Minecraft) (Object) this;
        if (mc.player == null || mc.level == null) return;
        boolean menu = down(mc, InputConstants.KEY_RSHIFT);
        boolean pearl = down(mc, InputConstants.KEY_V);
        boolean build = down(mc, InputConstants.KEY_B);
        boolean ah = down(mc, InputConstants.KEY_H);
        boolean rtp = down(mc, InputConstants.KEY_J);
        boolean scan = down(mc, InputConstants.KEY_K);
        if (menu && !oldMenu) {
            Screen current = mc.screen;
            mc.setScreen(current instanceof CorzScreen ? null : new CorzScreen(current));
        }
        if (mc.screen == null) {
            if (pearl && !oldPearl) ModuleEngine.keyPearl(mc);
            if (build && !oldBuild) {
                tryAutoLoad(mc);
                ServerUtilities.tell(mc, SchematicBuilder.toggle(mc));
            }
            if (ah && !oldAh) ServerUtilities.ahSell(mc);
            if (rtp && !oldRtp) ServerUtilities.rtpAndScan(mc);
            if (scan && !oldScan) ServerUtilities.scan(mc);
        }
        oldMenu = menu;
        oldPearl = pearl;
        oldBuild = build;
        oldAh = ah;
        oldRtp = rtp;
        oldScan = scan;
        SchematicBuilder.tick(mc);
        ServerUtilities.tick(mc);
        ModuleEngine.tick(mc);
    }

    private static boolean down(Minecraft mc, int code) {
        return InputConstants.isKeyDown(mc.getWindow().getWindow(), code);
    }

    private static void tryAutoLoad(Minecraft mc) {
        if (!SchematicBuilder.status().equals("No schematic loaded")) return;
        Path dir = FabricLoader.getInstance().getConfigDir().resolve("corzstyle/schematics");
        try {
            if (!Files.isDirectory(dir)) return;
            try (var stream = Files.list(dir)) {
                stream.filter(p -> p.getFileName().toString().endsWith(".schem"))
                    .sorted().findFirst().ifPresent(p ->
                        ServerUtilities.tell(mc, SchematicBuilder.load(p.getFileName().toString())));
            }
        } catch (IOException ignored) { }
    }
}
