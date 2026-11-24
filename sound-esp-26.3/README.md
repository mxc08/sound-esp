# Sound ESP (Fabric, client-only, Minecraft 26.3)

Marks the source of selected sounds that the local client plays, drawn through blocks.

## Build
Requires JDK 25 and Gradle 9.6+ (Loom 1.17).
    gradle wrapper --gradle-version 9.6.0     # once
    ./gradlew build
Jar: build/libs/sound-esp-1.0.0.jar -> .minecraft/mods with Fabric Loader 0.19.5+ and Fabric API 0.161.0+26.3.

Minecraft 26.x is unobfuscated: the code uses Mojang names, there is no Yarn mapping.

## Use
Press O in a world (rebindable: "Open Sound ESP menu"). Search sound IDs and click to toggle.
Menu: ESP on/off, clear markers, range slider, lifetime slider.
Config: .minecraft/config/sound-esp.json

Only sounds the client actually receives and plays can be marked. Some servers prohibit
this kind of visual aid; check the rules before using it on multiplayer.
