package igentuman.nc.screen;

import igentuman.nc.container.UniversalProcessorContainer;
import igentuman.nc.registration.ModEntry;
import igentuman.nc.screen.element.SlotWidget;
import igentuman.nc.setup.ModEntries;
import igentuman.nc.util.SlotsLayout;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.util.TextUtils.__;

public class SideConfigSlotSelectionScreen extends Screen {

    private static final ResourceLocation TEXTURE = rl("textures/gui/window_no_inventory.png");

    private static final int WIN_W = 180;
    private static final int WIN_H = 140;
    private static final int BTN   = 16;

    private final AbstractContainerScreen<UniversalProcessorContainer> parentScreen;

    private int winX;
    private int winY;

    private record SlotEntry(int handlerSlotId, boolean isFluid, boolean output, int x, int y) {}

    private final List<SlotEntry> slotEntries = new ArrayList<>();

    public SideConfigSlotSelectionScreen(AbstractContainerScreen<UniversalProcessorContainer> parent) {
        super(__("screen.nuclearcraft.slot_selection"));
        this.parentScreen = parent;
        width  = WIN_W;
        height = WIN_H;
    }

    @Override
    protected void init() {
        winX = (this.width - WIN_W) / 2;
        winY = (this.height - WIN_H) / 2;

        SlotWidget.RELATIVE_X = winX;
        SlotWidget.RELATIVE_Y = winY;

        slotEntries.clear();
        UniversalProcessorContainer menu = parentScreen.getMenu();
        ModEntry entry = ModEntries.get(menu.getBlockEntity().name);
        SlotsLayout layout = entry.slotsLayout();

        int inputItemCount   = entry.itemCap()  != null ? entry.itemCap().inputSlots         : 0;
        int outputItemCount  = entry.itemCap()  != null ? entry.itemCap().outputSlots         : 0;
        int inputFluidCount  = entry.fluidCap() != null ? entry.fluidCap().inputTanks.size()  : 0;
        int outputFluidCount = entry.fluidCap() != null ? entry.fluidCap().outputTanks.size() : 0;
        int totalItemSlots   = inputItemCount + outputItemCount;

        int layoutIndex = 0;
        for (int i = 0; i < inputItemCount; i++, layoutIndex++) {
            slotEntries.add(new SlotEntry(i, false, false,
                    layout.slots.get(layoutIndex).x, layout.slots.get(layoutIndex).y));
        }
        for (int i = 0; i < inputFluidCount; i++, layoutIndex++) {
            slotEntries.add(new SlotEntry(totalItemSlots + i, true, false,
                    layout.slots.get(layoutIndex).x, layout.slots.get(layoutIndex).y));
        }
        for (int i = 0; i < outputItemCount; i++, layoutIndex++) {
            slotEntries.add(new SlotEntry(inputItemCount + i, false, true,
                    layout.slots.get(layoutIndex).x, layout.slots.get(layoutIndex).y));
        }
        for (int i = 0; i < outputFluidCount; i++, layoutIndex++) {
            slotEntries.add(new SlotEntry(totalItemSlots + inputFluidCount + i, true, true,
                    layout.slots.get(layoutIndex).x, layout.slots.get(layoutIndex).y));
        }

        for (SlotEntry se : slotEntries) {
            final int slotId = se.handlerSlotId();
            SlotWidget btn = new SlotWidget(se.x(), se.y(), BTN + 2, BTN + 2, Component.empty());
            if (se.isFluid()) btn.fluid(); else btn.item();
            if (se.output()) btn.output();
            btn.onPress(() -> Minecraft.getInstance().setScreen(new SideConfigScreen(parentScreen, slotId)));
            addRenderableWidget(btn);
        }

        addRenderableWidget(net.minecraft.client.gui.components.Button.builder(Component.literal("X"), btn ->
                        Minecraft.getInstance().setScreen(parentScreen))
                .pos(winX + WIN_W - 14, winY + 2)
                .size(12, 12)
                .build());
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.blit(TEXTURE, winX, winY, 0, 0, WIN_W, WIN_H, 256, 256);
        guiGraphics.drawCenteredString(font, this.title, winX + WIN_W / 2, winY + 6, 0x404040);
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parentScreen);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
