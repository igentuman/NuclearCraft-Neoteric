package igentuman.nc.screen;

import igentuman.nc.block_entity.GlobalBlockEntity;
import igentuman.nc.container.UniversalProcessorContainer;
import igentuman.nc.registration.ModEntry;
import igentuman.nc.screen.element.EnergyBar;
import igentuman.nc.screen.element.ProgressBar;
import igentuman.nc.screen.element.SlotWidget;
import igentuman.nc.setup.ModEntries;
import igentuman.nc.util.GuiFluidRenderer;
import igentuman.nc.util.SlotDef;
import igentuman.nc.util.SlotsLayout;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import igentuman.nc.handler.sided.FluidCapabilityHandler;

import java.util.ArrayList;
import java.util.List;

import static igentuman.nc.Main.rl;

public class UniversalProcessorScreen extends AbstractContainerScreen<UniversalProcessorContainer> {

    private static final ResourceLocation TEXTURE = rl("textures/gui/processor.png");
    private final List<SlotWidget> slotWidgets = new ArrayList<>();
    private ProgressBar progressBar;

    public UniversalProcessorScreen(UniversalProcessorContainer menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        imageWidth = 180;
        imageHeight = 180;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;

        // Setup slot widgets from layout
        slotWidgets.clear();
        SlotsLayout layout = menu.getLayout();
        if (layout != null) {
            SlotWidget.RELATIVE_X = leftPos;
            SlotWidget.RELATIVE_Y = topPos;
            addRenderableWidget(Button.builder(Component.literal("S"),
                            btn -> Minecraft.getInstance().setScreen(new SideConfigSlotSelectionScreen(this)))
                    .pos(leftPos + 153, topPos + 75)
                    .size(17, 17)
                    .build());
            for (int i = 0; i < layout.slots.size(); i++) {
                SlotDef slotDef = layout.slots.get(i);
                SlotWidget widget = new SlotWidget(slotDef.x, slotDef.y, 18, 18, Component.empty());

                // Determine if this slot is for fluids
                ModEntry entry = ModEntries.get(menu.getBlockEntity().name);
                int inputItemCount = entry.itemCap() != null ? entry.itemCap().inputSlots : 0;
                int inputFluidCount = entry.fluidCap() != null ? entry.fluidCap().inputTanks.size() : 0;
                int outputItemCount = entry.itemCap() != null ? entry.itemCap().outputSlots : 0;

                boolean isInputFluid = i >= inputItemCount && i < inputItemCount + inputFluidCount;
                boolean isOutputFluid = i >= inputItemCount + inputFluidCount + outputItemCount;

                if (isInputFluid || isOutputFluid) {
                    widget.fluid();
                }

                slotWidgets.add(widget);
                this.addRenderableWidget(widget);
            }
        }
        ModEntry progressEntry = ModEntries.get(menu.getBlockEntity().name);
        int barIndex = progressEntry != null ? progressEntry.progressBar() : 0;
        progressBar = new ProgressBar((this.width - this.imageWidth) / 2 + 72, (this.height - this.imageHeight) / 2 + 30, barIndex);
        addRenderableWidget(progressBar);

        GlobalBlockEntity be = menu.getBlockEntity();
        if (be.hasEnergyStorage()) {
            int barX = leftPos + imageWidth - 10;
            int barY = topPos + 10;
            addRenderableWidget(new EnergyBar(barX, barY, () -> menu.getBlockEntity().energyStorage));
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);
        progressBar.setProgress(menu.getProgress());
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);
        // Render fluid contents (SlotWidgets render the backgrounds)
        // This is hack, but this way we sure fluids rendered on top of other elements
        GlobalBlockEntity be = menu.getBlockEntity();
        if (be.hasFluidTanks()) {
            int x = (this.width - this.imageWidth) / 2;
            int y = (this.height - this.imageHeight) / 2;
            FluidCapabilityHandler tanks = be.contentHandler.getFluidHandler();
            renderFluidTanks(guiGraphics, 0, 0, tanks, false, 0, 0);
            renderFluidTanks(guiGraphics, 0, 0, tanks, true, mouseX - leftPos, mouseY - topPos);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    private void renderFluidTanks(GuiGraphics guiGraphics, int x, int y,
                                   FluidCapabilityHandler tanks, boolean tooltip, int mouseX, int mouseY) {
        SlotsLayout layout = menu.getLayout();
        if (layout == null) return;

        ModEntry entry = ModEntries.get(menu.getBlockEntity().name);
        int inputItemCount  = entry.itemCap()  != null ? entry.itemCap().inputSlots        : 0;
        int inputFluidCount = entry.fluidCap() != null ? entry.fluidCap().inputTanks.size() : 0;
        int outputItemCount = entry.itemCap()  != null ? entry.itemCap().outputSlots        : 0;

        int outputFluidOffset = inputItemCount + inputFluidCount + outputItemCount;

        for (int i = 0; i < inputFluidCount && (inputItemCount + i) < layout.slots.size(); i++) {
            SlotDef def = layout.slots.get(inputItemCount + i);
            if (tooltip) {
                GuiFluidRenderer.renderFluidTooltip(guiGraphics, mouseX, mouseY,
                        x + def.x, y + def.y, 16, 16,
                        tanks.getFluidInTank(i), tanks.getTankCapacity(i));
            } else {
                GuiFluidRenderer.renderFluidTank(guiGraphics, x + def.x, y + def.y, 16, 16,
                        tanks.getFluidInTank(i), tanks.getTankCapacity(i));
            }
        }

        int outputFluidCount = entry.fluidCap() != null ? entry.fluidCap().outputTanks.size() : 0;
        for (int i = 0; i < outputFluidCount && (outputFluidOffset + i) < layout.slots.size(); i++) {
            SlotDef def = layout.slots.get(outputFluidOffset + i);
            int tankIndex = inputFluidCount + i;
            if (tooltip) {
                GuiFluidRenderer.renderFluidTooltip(guiGraphics, mouseX, mouseY,
                        x + def.x, y + def.y, 16, 16,
                        tanks.getFluidInTank(tankIndex), tanks.getTankCapacity(tankIndex));
            } else {
                GuiFluidRenderer.renderFluidTank(guiGraphics, x + def.x, y + def.y, 16, 16,
                        tanks.getFluidInTank(tankIndex), tanks.getTankCapacity(tankIndex));
            }
        }
    }
}
