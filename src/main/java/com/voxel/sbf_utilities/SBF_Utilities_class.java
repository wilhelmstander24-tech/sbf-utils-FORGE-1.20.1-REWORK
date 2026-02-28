package com.voxel.sbf_utilities;

import com.mojang.logging.LogUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.ModLoadingContext;
import com.voxel.sbf_utilities.config.SbfCommonConfig;
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

    public SBF_Utilities_class() {
        FMLJavaModLoadingContext.get().getModEventBus();

        MinecraftForge.EVENT_BUS.register(new ServerUtilityEvents());
    }
}
