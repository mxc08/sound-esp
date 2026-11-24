package com.example.soundesp;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SoundEspClient implements ClientModInitializer {
    public static final String MOD_ID = "sound_esp";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static KeyMapping openKey;

    @Override
    public void onInitializeClient() {
        SoundEspConfig.load();

        KeyMapping.Category category = KeyMapping.Category.register(
                Identifier.fromNamespaceAndPath(MOD_ID, "main"));
        openKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.sound_esp.open", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_O, category));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openKey.consumeClick()) {
                if (client.screen == null && client.level != null) {
                    client.setScreen(new SoundEspScreen());
                }
            }
        });

        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(MOD_ID, "markers"),
                (graphics, deltaTracker) -> MarkerRenderer.render(graphics));
    }
}
