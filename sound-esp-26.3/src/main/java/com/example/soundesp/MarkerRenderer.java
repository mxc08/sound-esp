package com.example.soundesp;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.phys.Vec3;

/**
 * Draws markers on the HUD by projecting each world position onto the screen.
 * HUD drawing ignores the depth buffer, so markers are visible through blocks.
 */
public final class MarkerRenderer {
    private MarkerRenderer() {}

    public static void render(GuiGraphicsExtractor g) {
        SoundEspConfig cfg = SoundEspConfig.get();
        Minecraft mc = Minecraft.getInstance();
        if (!cfg.enabled || mc.player == null || mc.level == null || mc.options.hideGui) return;

        Camera cam = mc.gameRenderer.getMainCamera();
        Vec3 origin = cam.position();
        double yaw = Math.toRadians(cam.yRot());
        double pitch = Math.toRadians(cam.xRot());

        Vec3 fwd = new Vec3(-Math.sin(yaw) * Math.cos(pitch), -Math.sin(pitch), Math.cos(yaw) * Math.cos(pitch));
        Vec3 right = new Vec3(-Math.cos(yaw), 0, -Math.sin(yaw));
        Vec3 up = right.cross(fwd);

        int w = g.guiWidth();
        int h = g.guiHeight();
        double aspect = (double) w / h;
        double tanHalf = Math.tan(Math.toRadians(mc.options.fov().get()) / 2.0);
        long now = System.currentTimeMillis();

        for (MarkerManager.Marker m : MarkerManager.active()) {
            Vec3 d = m.pos().subtract(origin);
            double depth = d.dot(fwd);
            double sx = d.dot(right);
            double sy = d.dot(up);

            double nx, ny;
            boolean clamped = false;
            if (depth > 0.05) {
                nx = sx / depth / (tanHalf * aspect);
                ny = sy / depth / tanHalf;
            } else {
                double len = Math.max(1e-6, Math.hypot(sx, sy));
                nx = sx / len * 10;
                ny = sy / len * 10;
            }
            if (Math.abs(nx) > 0.92 || Math.abs(ny) > 0.88) {
                double s = Math.max(Math.abs(nx) / 0.92, Math.abs(ny) / 0.88);
                nx /= s; ny /= s;
                clamped = true;
            }

            int px = (int) (w / 2.0 + nx * w / 2.0);
            int py = (int) (h / 2.0 - ny * h / 2.0);

            double life = (double) (m.expiresMs() - now) / Math.max(1, m.expiresMs() - m.createdMs());
            int alpha = (int) (60 + 195 * Math.max(0, Math.min(1, life)));
            int color = (alpha << 24) | 0xFF3030;
            int outline = (alpha << 24);
            int r = clamped ? 3 : 4;

            g.fill(px - r - 1, py - r - 1, px + r + 1, py + r + 1, outline);
            g.fill(px - r, py - r, px + r, py + r, color);

            String name = m.id().startsWith("minecraft:") ? m.id().substring(10) : m.id();
            String label = name + " " + (int) d.length() + "m";
            int textColor = (alpha << 24) | 0xFFFFFF;
            g.text(mc.font, label, px - mc.font.width(label) / 2, py + r + 3, textColor, true);
        }
    }
}
