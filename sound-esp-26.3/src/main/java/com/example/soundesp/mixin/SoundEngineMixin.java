package com.example.soundesp.mixin;

import com.example.soundesp.MarkerManager;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

@Mixin(SoundEngine.class)
public class SoundEngineMixin {
    // No CallbackInfo parameter and no return type in the descriptor, so this works
    // whether play() returns void or a PlayResult.
    @Inject(method = "play(Lnet/minecraft/client/resources/sounds/SoundInstance;)", at = @At("HEAD"))
    private void soundesp$onPlay(SoundInstance sound) {
        try {
            if (sound.isRelative() || sound.getAttenuation() == SoundInstance.Attenuation.NONE) return;
            MarkerManager.onSound(sound.getIdentifier().toString(), sound.getX(), sound.getY(), sound.getZ());
        } catch (Throwable ignored) {
            // never break sound playback
        }
    }
}
