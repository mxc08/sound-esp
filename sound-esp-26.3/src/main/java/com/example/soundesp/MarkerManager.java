package com.example.soundesp;

import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class MarkerManager {
    public record Marker(String id, Vec3 pos, long createdMs, long expiresMs) {}

    private static final List<Marker> MARKERS = new CopyOnWriteArrayList<>();
    private static final int MAX_MARKERS = 256;

    private MarkerManager() {}

    /** Called for every positional sound the client starts playing. */
    public static void onSound(String id, double x, double y, double z) {
        SoundEspConfig cfg = SoundEspConfig.get();
        if (!cfg.enabled || !cfg.selected.contains(id)) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        Vec3 pos = new Vec3(x, y, z);
        if (mc.player.getEyePosition().distanceTo(pos) > cfg.range) return;

        long now = System.currentTimeMillis();
        if (MARKERS.size() >= MAX_MARKERS) MARKERS.remove(0);
        MARKERS.add(new Marker(id, pos, now, now + cfg.lifetimeSeconds * 1000L));
    }

    public static List<Marker> active() {
        long now = System.currentTimeMillis();
        MARKERS.removeIf(m -> m.expiresMs() < now);
        return MARKERS;
    }

    public static void clear() { MARKERS.clear(); }
}
