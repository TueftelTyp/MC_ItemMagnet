# ItemMagnet v1.2

Minecraft 26.3 Fabric mod by TueftelTyp. Hold a magnet in either hand to pull in dropped items and, optionally, XP orbs. Three craftable upgrades are available.

| Magnet | Default radius | Crafting recipe |
| --- | --- | --- |
| Weak | 6 blocks | 4 iron ingots, 2 copper blocks, 2 redstone dust and 1 redstone block |
| Mid | 12 blocks | 4 gold blocks, 2 lapis blocks, 2 amethyst blocks and 1 weak magnet |
| Strong | 20 blocks | 4 diamond blocks, 2 emerald blocks, 2 ancient debris and 1 mid magnet |

Configuration file: `config/itemmagnet.json`. The setting `attractExperience` enables/disables XP attraction. Mod Menu + Cloth Config provide the in-game GUI. Both are optional client-side dependencies; Fabric API is required. On a multiplayer server, server configuration controls behavior.

Build using **JDK 25**, **Gradle 9.6**: `gradle build`. The build produces `build/libs/itemmagnet-1.2.jar`. If desired create Gradle wrapper first: `gradle wrapper --gradle-version 9.6`. This archive does not include a wrapper and no compilation was performed in the authoring environment.

The three textures are temporary hand-drawn Minecraft-style pixel art. The requested 'Ausbaustufen F' was not present among A-E from the prior selection; the classic A-style three-tier progression was therefore used provisionally. Mod icon uses concept A.

## Configuration menu

Install Mod Menu and Cloth Config on the client to edit settings in-game. Without them, `config/itemmagnet.json` still works. For multiplayer, edit the server configuration to control attraction behavior.

## Crafting recipes (v1.2)

Three workbench recipes are included and unlock in the recipe book as you acquire their primary ingredients:

- Weak: 4 iron ingots, 2 copper blocks, 2 redstone dust and 1 redstone block in the middle
- Mid: 4 gold blocks, 2 lapis blocks, 2 amethyst blocks and 1 Weak Magnet in the middle
- Strong: 4 diamond blocks, 2 emerald blocks, 2 ancient debris and 1 Mid Magnet in the middle

Source recipes (for changes before building) are at `src/main/resources/data/itemmagnet/recipe/`. They use the Minecraft 26.3 crafting JSON format.

**Edit recipes without rebuilding the mod:** Put the included `recipe-overrides-datapack` folder inside the save's `datapacks/` directory, edit the three files at `recipe-overrides-datapack/data/itemmagnet/recipe/*.json`, then run `/reload`. The datapack uses the same recipe IDs so it overrides the mod's default recipes. On a server, install/edit it on the server world. Recipes are editable as 3x3 item-ID grids in `config/itemmagnet.json` and in Mod Menu (three comma-separated item IDs per row). Each config save also exports `config/itemmagnet-recipes.zip`. Place that ZIP in your world `datapacks/` folder, enable it if necessary, and run `/reload` (or restart the world). When you edit the recipe in Mod Menu, copy the updated ZIP into the world datapacks directory again and reload. On servers edit the server config and deploy its generated ZIP to the server world. Editing the config does not instantly update an already loaded recipe registry.

## Build fix (v1.1, unchanged version)

Minecraft 26.3 uses Identifier instead of ResourceLocation, Fabric CreativeModeTabEvents instead of ItemGroupEvents, and no longer exposes Entity.hurtMarked. These source compatibility changes do not alter magnet behavior.

## Recipe layout

```
Weak      Mid       Strong
ICI       GLG       DED
RBR       AWA       AMA
ICI       GLG       DED
```

Ingredient letters are defined in the corresponding recipe JSON files. The `category` is `equipment` (Tools & Utilities in the crafting recipe book); the magnet items are registered in the Creative Tools & Utilities tab.

To change recipes before compiling edit `src/main/resources/data/itemmagnet/recipe/*.json`. To change a running world without rebuilding, use the included override datapack and `/reload`. Mod Menu edits magnet behavior AND recipe grids; recipe changes require installation/replacement of the generated datapack and a world reload.

## Version 1.3: toggle, effects, tooltips

- Right-click any held magnet to toggle that individual magnet on or off; new magnets default to off (activate via right-click).
- When an enabled magnet is held, the top-right vanilla status HUD shows a magnet icon. No actionbar text and no potion particles.
- Configurable END_ROD / XP particle trails and optional throttled sound effects in Mod Menu and `config/itemmagnet.json`.
- Detailed bilingual hover tooltips display on/off, current tier range, pull strength, top speed, and XP attraction.
- Existing recipes and recipe exporter remain unchanged.

Note: In multiplayer the authoritative config is on the server; tooltips read the locally available config and may not mirror server overrides.

## v1.4 changes

- Redesigned status-effect icon (18x18 pixel art).
- `rememberSwitchState` (default `false`) in `config/itemmagnet.json` and Mod Menu.
  When off, each magnet resets to OFF on re-equipping or joining a world.
  When on, its last toggle state is retained in the item data.
