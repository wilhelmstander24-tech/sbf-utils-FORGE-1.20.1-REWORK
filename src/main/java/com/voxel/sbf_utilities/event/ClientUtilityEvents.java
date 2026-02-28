package com.voxel.sbf_utilities.event;

import com.voxel.sbf_utilities.SBF_Utilities_class;
import com.voxel.sbf_utilities.config.SbfCommonConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = SBF_Utilities_class.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ClientUtilityEvents {
    private static int particleCounter = 0;
    private static final ResourceLocation SERENE_CALENDAR_ITEM_ID = new ResourceLocation("sereneseasons", "calendar");

    private ClientUtilityEvents() {
    }

    private static void sendClientCommand(String command) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.getConnection() != null && command != null && !command.isBlank()) {
            mc.player.connection.sendUnsignedCommand(command);
        }
    }

    @SubscribeEvent
    public static void onInventoryInit(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof InventoryScreen screen)) {
            return;
        }
        int x = (screen.width / 2) - 86;
        int y = (screen.height / 2) - 102;

        event.addListener(Button.builder(Component.literal("Quests"), button ->
                        sendClientCommand(SbfCommonConfig.INVENTORY_QUEST_COMMAND.get()))
                .bounds(x, y, 56, 20)
                .build());
    }

    @SubscribeEvent
    public static void onPauseMenuInit(ScreenEvent.Init.Post event) {
        if (!SbfCommonConfig.ENABLE_BOSS_LIST_BUTTON.get()) {
            return;
        }
        if (!(event.getScreen() instanceof PauseScreen screen)) {
            return;
        }

        int x = screen.width / 2 + 179;
        int y = screen.height / 4 + 48;

        event.addListener(Button.builder(Component.literal("★"), button ->
                        sendClientCommand(SbfCommonConfig.BOSS_LIST_COMMAND.get()))
                .bounds(x, y, 20, 20)
                .build());
    }

    @SubscribeEvent
    public static void onRenderOverlayPre(RenderGuiOverlayEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }

        if (!ModList.get().isLoaded("sereneseasons") && !ModList.get().isLoaded("seasonhud")) {
            return;
        }

        if (isHoldingCalendar(mc.player.getMainHandItem()) || isHoldingCalendar(mc.player.getOffhandItem())) {
            return;
        }

        ResourceLocation overlayId = event.getOverlay().id();
        if (overlayId == null) {
            return;
        }

        String namespace = overlayId.getNamespace();
        String path = overlayId.getPath();
        if ("seasonhud".equals(namespace) || "sereneseasons".equals(namespace) || path.contains("season")) {
            event.setCanceled(true);
        }
    }

    private static boolean isHoldingCalendar(ItemStack stack) {
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return SERENE_CALENDAR_ITEM_ID.equals(key);
    }

    @SubscribeEvent
    public static void onComputeFov(ViewportEvent.ComputeFov event) {
        double tempo = SbfCommonConfig.STAMINA_TEMPO.get();
        double correlation = SbfCommonConfig.STAMINA_MAGIC_CORRELATION.get();
        event.setFOV(event.getFOV() * (1.0D + ((tempo - 1.0D) * 0.02D * correlation)));
    }

    @SubscribeEvent
    public static void onRenderHud(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.options == null) {
            return;
        }

        if (SbfCommonConfig.ENABLE_PARTICLE_SOFT_CAP.get()) {
            particleCounter = (particleCounter + 1) % 2000;
            if (particleCounter > SbfCommonConfig.PARTICLE_SOFT_CAP.get()) {
                mc.options.particles().set(ParticleStatus.DECREASED);
            }
        }

        if (!SbfCommonConfig.ENABLE_GUI_OVERLAP_FIX.get()) {
            return;
        }
        int x = 4;
        int y = 4;
        int w = 130;
        int h = 12;
        event.getGuiGraphics().fill(x - 2, y - 2, x + w + 2, y + h + 2, 0x66000000);
        event.getGuiGraphics().drawString(mc.font, "SBF HUD auto-layout", x, y, 0xFFCC66, false);
    }
}