package com.voxel.sbf_utilities;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.ModLoadingContext;
import com.voxel.sbf_utilities.config.SbfCommonConfig;
import com.voxel.sbf_utilities.config.SbfStructureOverrideConfig;
import com.voxel.sbf_utilities.event.ServerUtilityEvents;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(SBF_Utilities_class.MOD_ID)
public class SBF_Utilities_class
{
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "sbf_utils";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final ForgeConfigSpec COMMON_SPEC = SbfCommonConfig.SPEC;
    public static final ForgeConfigSpec STRUCTURE_OVERRIDE_SPEC = SbfStructureOverrideConfig.SPEC;

    public SBF_Utilities_class() {
        FMLJavaModLoadingContext.get().getModEventBus();

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, COMMON_SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, STRUCTURE_OVERRIDE_SPEC, "sbf_utils-structure-overrides.toml");

        MinecraftForge.EVENT_BUS.register(new ServerUtilityEvents());
    }
}