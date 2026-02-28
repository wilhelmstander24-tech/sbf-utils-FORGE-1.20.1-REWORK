package com.voxel.sbf_utilities.event;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.voxel.sbf_utilities.SBF_Utilities_class;
import com.voxel.sbf_utilities.config.SbfCommonConfig;
import com.voxel.sbf_utilities.config.SbfStructureOverrideConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.ChunkDataEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class ServerUtilityEvents {
    private static final String SBF_TAG = "sbf_utils";
    private static final String STARTER_GIVEN = "starter_items_granted";
    private static final String LAST_LEVEL = "last_level";
    private static final String LAST_X = "last_x";
    private static final String LAST_Y = "last_y";
    private static final String LAST_Z = "last_z";

    private final Map<String, Integer> structureSeenCounts = new HashMap<>();

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer)) {
            return;
        }

        ServerPlayer player = (ServerPlayer) event.getEntity();
        giveStartingItems(player);
        teleportToStoredLocation(player);
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer)) {
            return;
        }

        if (!SbfCommonConfig.ENABLE_LAST_LOCATION_SPAWN_FIX.get()) {
            return;
        }

        ServerPlayer player = (ServerPlayer) event.getEntity();

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
        if (SbfCommonConfig.ENABLE_DUPLICATE_MOD_SCAN.get()) {
            runDuplicateModScan();
        }

        structureSeenCounts.clear();

        if (SbfStructureOverrideConfig.ENABLE_STRUCTURE_OVERRIDES.get()) {
            int baseCaps = parseCountCaps(SbfStructureOverrideConfig.STRUCTURE_COUNT_OVERRIDES.get()).size();
            int depCaps = parseCountCaps(SbfStructureOverrideConfig.DEPENDENCY_STRUCTURE_COUNT_OVERRIDES.get()).size();
            SBF_Utilities_class.LOGGER.info("[SBF Utils] Structure overrides enabled (baseCaps={}, dependencyCaps={}).", baseCaps, depCaps);
        }
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("sbfstructure")
                        .requires(this::isAllowedCommandSource)
                        .then(Commands.literal("reload")
                                .executes(ctx -> {
                                    structureSeenCounts.clear();
                                    ctx.getSource().sendSuccess(() -> Component.literal("SBF structure override runtime counters reset."), true);
                                    return Command.SINGLE_SUCCESS;
                                }))
                        .then(Commands.literal("status")
                                .executes(ctx -> {
                                    int baseCaps = parseCountCaps(SbfStructureOverrideConfig.STRUCTURE_COUNT_OVERRIDES.get()).size();
                                    int depCaps = parseCountCaps(SbfStructureOverrideConfig.DEPENDENCY_STRUCTURE_COUNT_OVERRIDES.get()).size();
                                    int replacements = parseReplacementMap(SbfStructureOverrideConfig.STRUCTURE_REPLACEMENTS.get()).size();

                                    ctx.getSource().sendSuccess(() -> Component.literal(
                                            "enabled=" + SbfStructureOverrideConfig.ENABLE_STRUCTURE_OVERRIDES.get()
                                                    + ", baseCaps=" + baseCaps
                                                    + ", dependencyCaps=" + depCaps
                                                    + ", replacements=" + replacements), false);
                                    return Command.SINGLE_SUCCESS;
                                }))
                        .then(Commands.literal("setcap")
                                .then(Commands.argument("entry", StringArgumentType.greedyString())
                                        .executes(ctx -> setCapEntry(ctx.getSource(), StringArgumentType.getString(ctx, "entry")))))
        );
    }

    @SubscribeEvent
    public void onChunkSave(ChunkDataEvent.Save event) {
        if (!SbfStructureOverrideConfig.ENABLE_STRUCTURE_OVERRIDES.get()) {
            return;
        }

        CompoundTag data = event.getData();
        CompoundTag structures = resolveStructuresTag(data);
        if (structures == null || !structures.contains("starts", 10)) {
            return;
        }

        CompoundTag starts = structures.getCompound("starts");
        if (starts.getAllKeys().isEmpty()) {
            return;
        }

        Map<String, Integer> caps = parseCountCaps(SbfStructureOverrideConfig.STRUCTURE_COUNT_OVERRIDES.get());
        if (SbfStructureOverrideConfig.APPLY_OVERRIDES_TO_DEPENDENCY_STRUCTURES.get()) {
            caps.putAll(parseCountCaps(SbfStructureOverrideConfig.DEPENDENCY_STRUCTURE_COUNT_OVERRIDES.get()));
        }

        if (caps.isEmpty()) {
            return;
        }

        Map<String, String> replacements = parseReplacementMap(SbfStructureOverrideConfig.STRUCTURE_REPLACEMENTS.get());
        List<String> startKeys = new ArrayList<>(starts.getAllKeys());

        for (String structureId : startKeys) {
            Integer cap = caps.get(structureId);
            if (cap == null) {
                continue;
            }

            int seen = structureSeenCounts.getOrDefault(structureId, 0) + 1;
            structureSeenCounts.put(structureId, seen);

            if (seen <= cap) {
                continue;
            }

            if (SbfStructureOverrideConfig.ENABLE_STRUCTURE_REPLACEMENTS.get()) {
                String replacementId = replacements.get(structureId);
                if (replacementId != null) {
                    CompoundTag copiedStart = starts.getCompound(structureId).copy();
                    starts.remove(structureId);
                    starts.put(replacementId, copiedStart);
                    SBF_Utilities_class.LOGGER.debug("[SBF Utils] Replaced capped structure '{}' with '{}'.", structureId, replacementId);
                    continue;
                }
            }

            starts.remove(structureId);
            SBF_Utilities_class.LOGGER.debug("[SBF Utils] Removed capped structure '{}' (cap={}, seen={}).", structureId, cap, seen);
        }
    }

    private boolean isAllowedCommandSource(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer)) {
            return false;
        }

        ServerPlayer player = (ServerPlayer) source.getEntity();
        return isWhitelistedAdmin(player);
    }

    private int setCapEntry(CommandSourceStack source, String entry) {
        String[] split = entry.split("=", 2);
        if (split.length != 2 || !ResourceLocation.isValidResourceLocation(split[0].trim())) {
            source.sendFailure(Component.literal("Expected format namespace:structure=count"));
            return 0;
        }

        int cap;
        try {
            cap = Integer.parseInt(split[1].trim());
        } catch (NumberFormatException ex) {
            source.sendFailure(Component.literal("Count must be a whole number."));
            return 0;
        }

        if (cap < 0) {
            source.sendFailure(Component.literal("Count must be >= 0."));
            return 0;
        }

        String structureId = split[0].trim();
        String normalized = structureId + "=" + cap;

        List<String> next = new ArrayList<>();
        for (String current : SbfStructureOverrideConfig.STRUCTURE_COUNT_OVERRIDES.get()) {
            if (!current.startsWith(structureId + "=")) {
                next.add(current);
            }
        }
        next.add(normalized);

        SbfStructureOverrideConfig.STRUCTURE_COUNT_OVERRIDES.set(next);
        source.sendSuccess(() -> Component.literal("Updated structure cap: " + normalized), true);
        return Command.SINGLE_SUCCESS;
    }

    private static CompoundTag resolveStructuresTag(CompoundTag root) {
        if (root.contains("structures", 10)) {
            return root.getCompound("structures");
        }

        if (root.contains("Level", 10)) {
            CompoundTag level = root.getCompound("Level");
            if (level.contains("Structures", 10)) {
                return level.getCompound("Structures");
            }
            if (level.contains("structures", 10)) {
                return level.getCompound("structures");
            }
        }

        return null;
    }

    private static boolean isWhitelistedAdmin(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        for (String allowed : SbfStructureOverrideConfig.WHITELISTED_ADMIN_UUIDS.get()) {
            if (uuid.equalsIgnoreCase(allowed)) {
                return true;
            }
        }
        return false;
    }

    private static Map<String, Integer> parseCountCaps(List<? extends String> entries) {
        Map<String, Integer> parsed = new HashMap<>();
        for (String entry : entries) {
            String[] split = entry.split("=", 2);
            if (split.length != 2) {
                continue;
            }

            String structureId = split[0].trim();
            String countPart = split[1].trim();
            if (!ResourceLocation.isValidResourceLocation(structureId)) {
                continue;
            }

            try {
                int cap = Integer.parseInt(countPart);
                if (cap >= 0) {
                    parsed.put(structureId, cap);
                }
            } catch (NumberFormatException ignored) {
            }
        }
        return parsed;
    }

    private static Map<String, String> parseReplacementMap(List<? extends String> entries) {
        Map<String, String> parsed = new HashMap<>();
        for (String entry : entries) {
            String[] split = entry.split("=", 2);
            if (split.length != 2) {
                continue;
            }

            String from = split[0].trim();
            String to = split[1].trim();
            if (ResourceLocation.isValidResourceLocation(from) && ResourceLocation.isValidResourceLocation(to)) {
                parsed.put(from, to);
            }
        }
        return parsed;
    }

    private static void runDuplicateModScan() {
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
            ResourceLocation key = ResourceLocation.tryParse(id);
            if (key == null) {
                SBF_Utilities_class.LOGGER.warn("[SBF Utils] Skipping invalid starting item id '{}'.", id);
                continue;
            }

            Item item = ForgeRegistries.ITEMS.getValue(key);
            if (item == null || item == Items.AIR) {
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


        ResourceLocation dimensionId = ResourceLocation.tryParse(tag.getString(LAST_LEVEL));
        if (dimensionId == null) {
            return;
        }

        Level level = Objects.requireNonNull(player.getServer())
                .getLevel(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, dimensionId));
        if (!(level instanceof ServerLevel)) {
            return;
        }

        ServerLevel targetLevel = (ServerLevel) level;

        double x = tag.getDouble(LAST_X);
        double y = tag.getDouble(LAST_Y);
        double z = tag.getDouble(LAST_Z);
        BlockPos safePos = BlockPos.containing(x, y, z);
        double safeY = Math.max(targetLevel.getMinBuildHeight() + 1, safePos.getY());

        player.teleportTo(targetLevel, safePos.getX() + 0.5D, safeY, safePos.getZ() + 0.5D, player.getYRot(), player.getXRot());
    }
}