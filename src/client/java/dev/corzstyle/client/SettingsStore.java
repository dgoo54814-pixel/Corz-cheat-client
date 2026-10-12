package dev.corzstyle.client;

import net.fabricmc.loader.api.FabricLoader;
import java.nio.file.Path;
import java.nio.file.Files;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.Properties;

public final class SettingsStore {
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("corzstyle.properties");

    public static void load() {
        if (!Files.exists(FILE)) return;
        Properties p = new Properties();
        try (InputStream stream = Files.newInputStream(FILE)) {
            p.load(stream);
            try { ServerUtilities.price(Math.max(1, Integer.parseInt(p.getProperty("ah.price", "1000")))); }
            catch (NumberFormatException ignored) { ServerUtilities.price(1000); }
            for (Feature f : Features.ALL) f.enabled = f.implemented && Boolean.parseBoolean(p.getProperty(f.id, "false"));
        } catch (IOException e) {
            System.err.println("[CorzStyle] Could not read settings: " + e.getMessage());
        }
    }

    public static void save() {
        Properties p = new Properties();
        p.setProperty("ah.price", Integer.toString(ServerUtilities.price()));
        for (Feature f : Features.ALL) if (f.implemented) p.setProperty(f.id, Boolean.toString(f.enabled));
        try {
            Files.createDirectories(FILE.getParent());
            try (OutputStream stream = Files.newOutputStream(FILE)) {
                p.store(stream, "Corz-inspired Client settings");
            }
        } catch (IOException e) {
            System.err.println("[CorzStyle] Could not save settings: " + e.getMessage());
        }
    }

    private SettingsStore() {}
}
