package com.voxel.sbf_utilities.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public final class SbfCommonConfig {
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> STARTING_ITEMS;
    public static final ForgeConfigSpec.ConfigValue<String> QUEST_BOOK_ITEM;
    public static final ForgeConfigSpec.ConfigValue<String> CLASS_BOOK_ITEM;
    public static final ForgeConfigSpec.ConfigValue<String> INVENTORY_QUEST_COMMAND;

    public static final ForgeConfigSpec.BooleanValue ENABLE_STARTING_ITEMS;
    public static final ForgeConfigSpec.BooleanValue ENABLE_LAST_LOCATION_SPAWN_FIX;
    public static final ForgeConfigSpec.BooleanValue ENABLE_DUPLICATE_MOD_SCAN;
    public static final ForgeConfigSpec.BooleanValue ENABLE_GUI_OVERLAP_FIX;
    public static final ForgeConfigSpec.BooleanValue ENABLE_PARTICLE_SOFT_CAP;

    public static final ForgeConfigSpec.IntValue PARTICLE_SOFT_CAP;
    public static final ForgeConfigSpec.DoubleValue STAMINA_MAGIC_CORRELATION;
    public static final ForgeConfigSpec.DoubleValue STAMINA_TEMPO;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("utility_features");

        ENABLE_STARTING_ITEMS = builder
                .comment("Give configured starting items to first-time players.")
                .define("enableStartingItems", true);
        ENABLE_LAST_LOCATION_SPAWN_FIX = builder
                .comment("Teleport players to their stored logout location when joining.")
                .define("enableLastLocationSpawnFix", true);
        ENABLE_DUPLICATE_MOD_SCAN = builder
                .comment("Scan loaded mods and log duplicate display names/IDs during server start.")
                .define("enableDuplicateModScan", true);
        ENABLE_GUI_OVERLAP_FIX = builder
                .comment("Apply lightweight HUD offset to avoid common UI overlaps.")
                .define("enableGuiOverlapFix", true);
        ENABLE_PARTICLE_SOFT_CAP = builder
                .comment("Cancel low-priority client particles when camera is in heavy scenes.")
                .define("enableParticleSoftCap", true);

        PARTICLE_SOFT_CAP = builder
                .comment("Approximate particle cap before cancelling ambient particles.")
                .defineInRange("particleSoftCap", 1500, 200, 10000);

        STAMINA_TEMPO = builder
                .comment("Base stamina tempo used by external integrations.")
                .defineInRange("staminaTempo", 1.0D, 0.1D, 10.0D);

        STAMINA_MAGIC_CORRELATION = builder
                .comment("Correlates stamina tempo and magic regen multiplier. Exposed for KubeJS/other mods.")
                .defineInRange("staminaMagicCorrelation", 0.65D, 0.0D, 5.0D);

        STARTING_ITEMS = builder
                .comment("Registry names of starting items to grant once.")
                .defineList("startingItems", List.of("betterquesting:quest_book", "minecraft:book"), o -> o instanceof String);

        QUEST_BOOK_ITEM = builder
                .comment("Item ID for quest book integration points.")
                .define("questBookItem", "betterquesting:quest_book");

        CLASS_BOOK_ITEM = builder
                .comment("Item ID for class book integration points.")
                .define("classBookItem", "minecraft:writable_book");

        INVENTORY_QUEST_COMMAND = builder
                .comment("Client command sent when quest button is pressed in the inventory screen.")
                .define("inventoryQuestCommand", "ftbquests quests");

        builder.pop();

        SPEC = builder.build();
    }

    private SbfCommonConfig() {
    }
}