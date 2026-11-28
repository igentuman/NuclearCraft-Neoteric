package igentuman.nc.screen;

import igentuman.nc.container.MultiblockPortContainer;
import igentuman.nc.handler.sided.FluidCapabilityHandler;
import igentuman.nc.registration.ModEntry;
import igentuman.nc.screen.element.FluidSlotWidget;
import igentuman.nc.screen.element.RedstoneModeButton;
import igentuman.nc.screen.element.SlotWidget;
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

import static igentuman.nc.NuclearCraft.rl;

/** GUI screen for a multiblock I/O port: renders the controller's slots and tanks plus a redstone-mode button. */
public class MultiblockPortScreen extends AbstractContainerScreen<MultiblockPortContainer> {

    private static final ResourceLocation TEXTURE = rl("textures/gui/processor.png");
    private final List<FluidSlotWidget> tankWidgets = new ArrayList<>();
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

        tankWidgets.clear();
        SlotsLayout layout = menu.getLayout();
        ModEntry entry = menu.getControllerEntry();
        SlotWidget.RELATIVE_X = leftPos;
        SlotWidget.RELATIVE_Y = topPos;
        if (entry != null) {
            int inputItemCount = entry.itemCap() != null ? entry.itemCap().inputSlots : 0;
            int inputFluidCount = entry.fluidCap() != null ? entry.fluidCap().inputTanks.size() : 0;
            int outputItemCount = entry.itemCap() != null ? entry.itemCap().outputSlots : 0;
            int outputFluidOffset = inputItemCount + inputFluidCount + outputItemCount;
            int slotCount = outputFluidOffset + (entry.fluidCap() == null ? 0 : entry.fluidCap().outputTanks.size());
            for (int i = 0; i < slotCount; i++) {
                SlotDef slotDef = layout.slots.get(i);
                boolean inputFluid = i >= inputItemCount && i < inputItemCount + inputFluidCount;
                boolean outputFluid = i >= outputFluidOffset;
                SlotWidget widget;
                if (inputFluid || outputFluid) {
                    var tankWidget = new FluidSlotWidget(slotDef.x, slotDef.y, menu, tankWidgets.size());
                    tankWidgets.add(tankWidget);
                    widget = tankWidget;
                } else {
                    widget = new SlotWidget(slotDef.x, slotDef.y, 18, 18, Component.empty());
                }
                if (slotDef.output) {
                    widget.output();
                }
                addRenderableWidget(widget);
            }
        }

        if (menu.getRedstoneModes().length > 0) {
            redstoneButton = new RedstoneModeButton(leftPos + imageWidth - 24, topPos + 6, menu);
            addRenderableWidget(redstoneButton);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);
        renderFluidTanks(guiGraphics, mouseX, mouseY, false);
    }

    private void renderFluidTanks(GuiGraphics guiGraphics, int mouseX, int mouseY, boolean tooltip) {
        var controller = menu.getController();
        if (tankWidgets.isEmpty() || controller == null || !controller.hasFluidTanks()) return;
        FluidCapabilityHandler tanks = controller.contentHandler.getFluidHandler();
        for (int tank = 0; tank < tankWidgets.size() && tank < tanks.getTanks(); tank++) {
            FluidSlotWidget widget = tankWidgets.get(tank);
            if (tooltip) {
                GuiFluidRenderer.renderFluidTooltip(guiGraphics, mouseX, mouseY, widget.X(), widget.Y(), 16, 16,
                        tanks.getFluidInTank(tank), tanks.getTankCapacity(tank));
            } else {
                GuiFluidRenderer.renderFluidTank(guiGraphics, widget.x, widget.y, 16, 16,
                        tanks.getFluidInTank(tank), tanks.getTankCapacity(tank));
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
        renderFluidTanks(guiGraphics, mouseX, mouseY, true);
    }
}
