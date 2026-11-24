package com.example.soundesp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;

/** Stored in <minecraft>/config/sound-esp.json */
public class SoundEspConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("sound-esp.json");
    private static SoundEspConfig instance = new SoundEspConfig();

    public boolean enabled = true;
    public int lifetimeSeconds = 5;   // 1..30
    public int range = 64;            // blocks, 4..256
    public Set<String> selected = new LinkedHashSet<>();

    public static SoundEspConfig get() { return instance; }

    public static void load() {
        try {
            if (Files.exists(FILE)) {
                SoundEspConfig c = GSON.fromJson(Files.readString(FILE), SoundEspConfig.class);
                if (c != null) {
                    if (c.selected == null) c.selected = new LinkedHashSet<>();
                    c.lifetimeSeconds = Math.max(1, Math.min(30, c.lifetimeSeconds));
                    c.range = Math.max(4, Math.min(256, c.range));
                    instance = c;
                }
            }
        } catch (Exception e) {
            SoundEspClient.LOGGER.warn("Could not read sound-esp.json, using defaults", e);
        }
    }

    public static void save() {
        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(instance));
        } catch (Exception e) {
            SoundEspClient.LOGGER.warn("Could not write sound-esp.json", e);
        }
    }
}
