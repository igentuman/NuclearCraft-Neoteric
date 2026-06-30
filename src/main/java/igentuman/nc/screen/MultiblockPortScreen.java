package igentuman.nc.screen;

import igentuman.nc.block_entity.GlobalBlockEntity;
import igentuman.nc.container.MultiblockPortContainer;
import igentuman.nc.handler.sided.FluidCapabilityHandler;
import igentuman.nc.registration.ModEntry;
import igentuman.nc.screen.element.RedstoneModeButton;
import igentuman.nc.screen.element.SlotWidget;
import igentuman.nc.setup.ModEntries;
import igentuman.nc.util.GuiFluidRenderer;
import igentuman.nc.util.SlotDef;
import igentuman.nc.util.SlotsLayout;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;

import static igentuman.nc.Main.rl;

public class MultiblockPortScreen extends AbstractContainerScreen<MultiblockPortContainer> {

    private static final ResourceLocation TEXTURE = rl("textures/gui/processor.png");
    protected final List<SlotWidget> slotWidgets = new ArrayList<>();
    protected RedstoneModeButton redstoneButton;

    public MultiblockPortScreen(MultiblockPortContainer menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        imageWidth = 180;
        imageHeight = 180;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;

        slotWidgets.clear();
        SlotsLayout layout = menu.getLayout();
        var controller = menu.getBlockEntity().controller();
        if (layout != null && controller != null) {
            SlotWidget.RELATIVE_X = leftPos;
            SlotWidget.RELATIVE_Y = topPos;
            for (int i = 0; i < layout.slots.size(); i++) {
                SlotDef slotDef = layout.slots.get(i);
                SlotWidget widget = new SlotWidget(slotDef.x, slotDef.y, 18, 18, Component.empty());

                ModEntry entry = ModEntries.get(controller.name);
                int inputItemCount = entry.itemCap() != null ? entry.itemCap().inputSlots : 0;
                int inputFluidCount = entry.fluidCap() != null ? entry.fluidCap().inputTanks.size() : 0;
                int outputItemCount = entry.itemCap() != null ? entry.itemCap().outputSlots : 0;

                boolean isInputFluid = i >= inputItemCount && i < inputItemCount + inputFluidCount;
                boolean isOutputFluid = i >= inputItemCount + inputFluidCount + outputItemCount;

                if (isInputFluid || isOutputFluid) {
                    widget.fluid();
                }
                if (slotDef.output) {
                    widget.output();
                }

                slotWidgets.add(widget);
                this.addRenderableWidget(widget);
            }
        }

        if (menu.getBlockEntity().redstoneModes().length > 0) {
            redstoneButton = new RedstoneModeButton(leftPos + imageWidth - 24, topPos + 6, menu);
            addRenderableWidget(redstoneButton);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);
        var controller = menu.getBlockEntity().controller();
        if (controller != null) {
            if (controller.hasFluidTanks()) {
                FluidCapabilityHandler tanks = controller.contentHandler.getFluidHandler();
                renderFluidTanks(guiGraphics, 0, 0, tanks, false, 0, 0);
                renderFluidTanks(guiGraphics, 0, 0, tanks, true, mouseX - leftPos, mouseY - topPos);
            }
        }
    }

    private void renderFluidTanks(GuiGraphics guiGraphics, int x, int y,
                                   FluidCapabilityHandler tanks, boolean tooltip, int mouseX, int mouseY) {
        SlotsLayout layout = menu.getLayout();
        var controller = menu.getBlockEntity().controller();
        if (layout == null || controller == null) return;

        ModEntry entry = ModEntries.get(controller.name);
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

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
