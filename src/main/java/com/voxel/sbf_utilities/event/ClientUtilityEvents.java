package com.voxel.sbf_utilities.event;

import com.voxel.sbf_utilities.SBF_Utilities_class;
import com.voxel.sbf_utilities.config.SbfCommonConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
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

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = SBF_Utilities_class.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ClientUtilityEvents {
    private static int particleCounter = 0;
    private static final ResourceLocation SERENE_CALENDAR_ITEM_ID = new ResourceLocation("sereneseasons", "calendar");
    private ClientUtilityEvents() {
    }

    @SubscribeEvent
    public static void onInventoryInit(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof InventoryScreen screen)) {
            return;
        }

        List<UiRect> occupied = new ArrayList<>();
        if (SbfCommonConfig.ENABLE_GUI_OVERLAP_FIX.get()) {
            for (GuiEventListener listener : event.getListenersList()) {
                if (listener instanceof AbstractWidget widget && widget.visible) {
                    occupied.add(UiRect.of(widget.getX(), widget.getY(), widget.getWidth(), widget.getHeight()));
                }
            }
        }

        int defaultX = screen.getGuiLeft() + 4;
        int defaultY = screen.getGuiTop() + 4;
        UiRect questButtonRect = SbfCommonConfig.ENABLE_GUI_OVERLAP_FIX.get()
                ? resolveForInventory(defaultX, defaultY, 56, 20, screen, occupied)
                : UiRect.of(defaultX, defaultY, 56, 20);

        event.addListener(Button.builder(Component.literal("Quests"), button -> {
                    Minecraft mc = Minecraft.getInstance();
                    if (mc.player != null && mc.getConnection() != null) {
                        mc.player.connection.sendUnsignedCommand(SbfCommonConfig.INVENTORY_QUEST_COMMAND.get());
                    }
                }).bounds(questButtonRect.x, questButtonRect.y, questButtonRect.w, questButtonRect.h)
                .build());
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

        int width = event.getWindow().getGuiScaledWidth();
        int height = event.getWindow().getGuiScaledHeight();

        List<UiRect> occupied = new ArrayList<>();
        // Approximate high-conflict vanilla HUD zones.
        occupied.add(UiRect.of((width / 2) - 91, height - 26, 182, 22)); // hotbar
        occupied.add(UiRect.of(width / 2 - 90, 2, 180, 20)); // boss bars / overlays
        occupied.add(UiRect.of(2, height - 102, 320, 100)); // chat area

        UiRect statusRect = resolveForHud(width, height, 130, 12, occupied);
        event.getGuiGraphics().fill(statusRect.x - 2, statusRect.y - 2, statusRect.x + statusRect.w + 2, statusRect.y + statusRect.h + 2, 0x66000000);
        event.getGuiGraphics().drawString(mc.font, "SBF HUD auto-layout", statusRect.x, statusRect.y, 0xFFCC66, false);
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


    private static UiRect resolveForInventory(int x, int y, int w, int h, InventoryScreen screen, List<UiRect> occupied) {
        List<UiRect> candidates = List.of(
                UiRect.of(x, y, w, h),
                UiRect.of(screen.getGuiLeft() + 176 - w - 4, screen.getGuiTop() + 4, w, h),
                UiRect.of(screen.getGuiLeft() + 4, screen.getGuiTop() + 166 - h - 4, w, h),
                UiRect.of(screen.getGuiLeft() + 176 - w - 4, screen.getGuiTop() + 166 - h - 4, w, h)
        );

        return firstNonOverlapping(candidates, occupied, candidates.get(0));
    }

    private static UiRect resolveForHud(int width, int height, int w, int h, List<UiRect> occupied) {
        List<UiRect> candidates = List.of(
                UiRect.of(4, 4, w, h),
                UiRect.of(width - w - 4, 4, w, h),
                UiRect.of(4, height - h - 32, w, h),
                UiRect.of(width - w - 4, height - h - 32, w, h),
                UiRect.of((width - w) / 2, 24, w, h)
        );

        return firstNonOverlapping(candidates, occupied, candidates.get(0));
    }

    private static UiRect firstNonOverlapping(List<UiRect> candidates, List<UiRect> occupied, UiRect fallback) {
        for (UiRect candidate : candidates) {
            boolean overlaps = occupied.stream().anyMatch(candidate::intersects);
            if (!overlaps) {
                return candidate;
            }
        }
        return fallback;
    }

    private record UiRect(int x, int y, int w, int h) {
        static UiRect of(int x, int y, int w, int h) {
            return new UiRect(x, y, w, h);
        }

        boolean intersects(UiRect other) {
            return this.x < other.x + other.w && this.x + this.w > other.x
                    && this.y < other.y + other.h && this.y + this.h > other.y;
        }
    }
}
