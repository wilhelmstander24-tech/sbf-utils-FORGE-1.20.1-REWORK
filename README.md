# Sinborne Odyssey Utilities (Forge 1.20.1)

This mod now provides a first-pass utility layer for the Sinsborne Odyssey modpack:

- Boss eye recipe changes via datapack override (`minecraft:ender_eye`).
- Inventory quest button (client inventory UI button that sends a quest command).
- Duplicate mod scan logging at server start for easier dupe-mod cleanup.
- Lightweight performance hooks (soft particle cap / reduced particle mode).
- KubeJS script template for End Eye recipe parity.
- Starting items for first login (configurable item list).
- Physical quest book crafting recipe (writable book fallback by default).
- Spawn persistence fix (logout position stored + restored on login).
- Particle render soft-cap logic.
- GUI/HUD overlap detection + auto-layout for injected components.
- Stamina tempo + magic correlation config values exposed for external integrations.

## Config

File: `config/sbf_utils-common.toml`

Key settings:

- `enableStartingItems`
- `startingItems`
- `enableLastLocationSpawnFix`
- `enableDuplicateModScan`
- `enableGuiOverlapFix`
- `enableParticleSoftCap`
- `particleSoftCap`
- `staminaTempo`
- `staminaMagicCorrelation`
- `inventoryQuestCommand`

## End Remastered Integration

- `build.gradle` now includes the CurseMaven repository and consumes `endremastered_dependency` from `gradle.properties` for `compileOnly`/`runtimeOnly` Forge deobf dependency wiring.
- Dependencies are declared with dynamic `+` versions to track the latest published artifact from the configured repository; pin exact versions if you need strict reproducibility.


## Additional Optional Mod Dependencies

- Added optional dependency property keys in `gradle.properties` for: Desertification, Witherstorm, Ice and Fire, Celestisynth, Companions!, Dungeons and Combat, Cataclysm, Mowzie's Mobs, Enderman Overhaul, Aquamirae, Alex's Caves, Lifesteal, Scarlet King, Sculk Sickness, Call of Yucatan, Dungeons Now Loading, T.O Magic 'n Extras, and Iron Spells 'n Spellbooks.
- To enable each one, set its `*_dependency` property to a valid Maven coordinate (for example a CurseMaven coordinate).


## End Remastered Eye Overrides

- Added `data/sbf_utils/kubejs/endremastered_eye_overrides.js` to fully override End Remastered eye recipes for Black, Lost, Old, Cold, Rogue, Cryptic, Magical, Corrupted, and Cursed eyes using your provided ingredient lists.
- The script removes original recipes and re-adds shapeless overrides (including alternative ingredient support for Rogue Eye).
- Verify/adjust item IDs in the script to match exact mod IDs in your pack if any differ by namespace.