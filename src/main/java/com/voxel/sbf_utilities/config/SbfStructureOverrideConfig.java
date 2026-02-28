package com.voxel.sbf_utilities.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public final class SbfStructureOverrideConfig {
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.BooleanValue ENABLE_STRUCTURE_OVERRIDES;
    public static final ForgeConfigSpec.BooleanValue APPLY_OVERRIDES_TO_DEPENDENCY_STRUCTURES;
    public static final ForgeConfigSpec.BooleanValue ENABLE_STRUCTURE_REPLACEMENTS;

    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> WHITELISTED_ADMIN_UUIDS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> STRUCTURE_COUNT_OVERRIDES;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> DEPENDENCY_STRUCTURE_COUNT_OVERRIDES;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> STRUCTURE_REPLACEMENTS;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("structure_overrides");

        ENABLE_STRUCTURE_OVERRIDES = builder
                .comment("If enabled, structure generation limits are enforced by chunk save rewriting.")
                .define("enableStructureOverrides", false);

        APPLY_OVERRIDES_TO_DEPENDENCY_STRUCTURES = builder
                .comment("If enabled, dependency structure limits are also enforced.")
                .define("applyOverridesToDependencyStructures", true);

        ENABLE_STRUCTURE_REPLACEMENTS = builder
                .comment("If enabled, capped structures can be replaced by custom structures configured below.")
                .define("enableStructureReplacements", false);

        WHITELISTED_ADMIN_UUIDS = builder
                .comment("UUIDs that can access /sbfstructure admin commands.")
                .defineList("whitelistedAdminUuids", List.of(), o -> o instanceof String);

        STRUCTURE_COUNT_OVERRIDES = builder
                .comment("Structure caps in format namespace:structure_id=count. Example: minecraft:village_plains=24")
                .defineList("structureCountOverrides", List.of(
                        "minecraft:village_plains=24",
                        "minecraft:desert_pyramid=12"
                ), o -> o instanceof String);

        DEPENDENCY_STRUCTURE_COUNT_OVERRIDES = builder
                .comment("Dependency structure caps in format namespace:structure_id=count")
                .defineList("dependencyStructureCountOverrides", List.of(), o -> o instanceof String);

        STRUCTURE_REPLACEMENTS = builder
                .comment("Structure replacements in format original_namespace:structure=new_namespace:structure")
                .defineList("structureReplacements", List.of(), o -> o instanceof String);

        builder.pop();

        SPEC = builder.build();
    }

    private SbfStructureOverrideConfig() {
    }
}
