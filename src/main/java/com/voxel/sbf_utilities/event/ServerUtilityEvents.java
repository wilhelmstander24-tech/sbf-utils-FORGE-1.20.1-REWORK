package com.voxel.sbf_utilities.event;

import com.voxel.sbf_utilities.SBF_Utilities_class;
import com.voxel.sbf_utilities.config.SbfCommonConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

public class ServerUtilityEvents {
    private static final String SBF_TAG = "sbf_utils";
    private static final String STARTER_GIVEN = "starter_items_granted";
    private static final String LAST_LEVEL = "last_level";
    private static final String LAST_X = "last_x";
    private static final String LAST_Y = "last_y";
    private static final String LAST_Z = "last_z";

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        giveStartingItems(player);
        teleportToStoredLocation(player);
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        if (!SbfCommonConfig.ENABLE_LAST_LOCATION_SPAWN_FIX.get()) {
            return;
        }

        CompoundTag root = player.getPersistentData();
        CompoundTag tag = root.contains(SBF_TAG) ? root.getCompound(SBF_TAG) : new CompoundTag();
        tag.putString(LAST_LEVEL, player.level().dimension().location().toString());
        tag.putDouble(LAST_X, player.getX());
        tag.putDouble(LAST_Y, player.getY());
        tag.putDouble(LAST_Z, player.getZ());
        root.put(SBF_TAG, tag);
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        if (!SbfCommonConfig.ENABLE_DUPLICATE_MOD_SCAN.get()) {
            return;
        }

        Map<String, List<String>> byId = new HashMap<>();
        Map<String, List<String>> byDisplay = new HashMap<>();

        ModList.get().getMods().forEach(info -> {
            byId.computeIfAbsent(info.getModId(), ignored -> new ArrayList<>()).add(info.getDisplayName());
            byDisplay.computeIfAbsent(info.getDisplayName().toLowerCase(Locale.ROOT), ignored -> new ArrayList<>()).add(info.getModId());
        });

        byId.entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .forEach(entry -> SBF_Utilities_class.LOGGER.warn("[SBF Utils] Duplicate mod id detected: {} => {}", entry.getKey(), entry.getValue()));

        byDisplay.entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .forEach(entry -> SBF_Utilities_class.LOGGER.warn("[SBF Utils] Multiple mods share display identity '{}': {}", entry.getKey(), entry.getValue()));

        SBF_Utilities_class.LOGGER.info("[SBF Utils] Duplicate mod scan complete for {} loaded mods.", ModList.get().size());
    }

    private void giveStartingItems(ServerPlayer player) {
        if (!SbfCommonConfig.ENABLE_STARTING_ITEMS.get()) {
            return;
        }

        CompoundTag root = player.getPersistentData();
        CompoundTag tag = root.contains(SBF_TAG) ? root.getCompound(SBF_TAG) : new CompoundTag();

        if (tag.getBoolean(STARTER_GIVEN)) {
            return;
        }

        for (String id : SbfCommonConfig.STARTING_ITEMS.get()) {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(id));
            if (item == null || item == net.minecraft.world.item.Items.AIR) {
                SBF_Utilities_class.LOGGER.warn("[SBF Utils] Skipping unknown starting item '{}'.", id);
                continue;
            }

            ItemStack stack = new ItemStack(item);
            if (!player.getInventory().add(stack)) {
                player.drop(stack, false);
            }
        }

        tag.putBoolean(STARTER_GIVEN, true);
        root.put(SBF_TAG, tag);
    }

    private void teleportToStoredLocation(ServerPlayer player) {
        if (!SbfCommonConfig.ENABLE_LAST_LOCATION_SPAWN_FIX.get()) {
            return;
        }

        CompoundTag root = player.getPersistentData();
        if (!root.contains(SBF_TAG)) {
            return;
        }

        CompoundTag tag = root.getCompound(SBF_TAG);
        if (!tag.contains(LAST_LEVEL) || !tag.contains(LAST_X)) {
            return;
        }

        ResourceLocation dimensionId = new ResourceLocation(tag.getString(LAST_LEVEL));
        Level level = Objects.requireNonNull(player.getServer()).getLevel(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, dimensionId));
        if (level == null || !(level instanceof net.minecraft.server.level.ServerLevel targetLevel)) {
            return;
        }

        double x = tag.getDouble(LAST_X);
        double y = tag.getDouble(LAST_Y);
        double z = tag.getDouble(LAST_Z);
        BlockPos safePos = BlockPos.containing(x, y, z);
        double safeY = Math.max(targetLevel.getMinBuildHeight() + 1, safePos.getY());

        player.teleportTo(targetLevel, safePos.getX() + 0.5D, safeY, safePos.getZ() + 0.5D, player.getYRot(), player.getXRot());
    }
}
