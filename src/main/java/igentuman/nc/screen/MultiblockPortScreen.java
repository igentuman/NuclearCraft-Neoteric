package igentuman.nc.screen;

import igentuman.nc.container.MultiblockPortContainer;
import igentuman.nc.handler.sided.FluidCapabilityHandler;
import igentuman.nc.multiblock.MultiblockEntry;
import igentuman.nc.multiblock.MultiblockRegistry;
import igentuman.nc.registration.ModEntry;
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
        MultiblockEntry mbEntry = MultiblockRegistry.getByPort(menu.getBlockEntity().name);
        ModEntry entry = mbEntry != null ? mbEntry.controllerEntry() : null;
        SlotWidget.RELATIVE_X = leftPos;
        SlotWidget.RELATIVE_Y = topPos;
        if (layout != null && entry != null) {
            int inputItemCount = entry.itemCap() != null ? entry.itemCap().inputSlots : 0;
            int inputFluidCount = entry.fluidCap() != null ? entry.fluidCap().inputTanks.size() : 0;
            int outputItemCount = entry.itemCap() != null ? entry.itemCap().outputSlots : 0;
            for (int i = 0; i < layout.slots.size(); i++) {
                SlotDef slotDef = layout.slots.get(i);
                SlotWidget widget = new SlotWidget(slotDef.x, slotDef.y, 18, 18, Component.empty());
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
        } else if (entry != null && entry.itemCap() != null) {
            int inputItemCount = entry.itemCap().inputSlots;
            int outputItemCount = entry.itemCap().outputSlots;
            for (int i = 0; i < inputItemCount; i++) {
                SlotWidget widget = new SlotWidget(44 + (i % 3) * 18, 26 + (i / 3) * 18, 18, 18, Component.empty());
                slotWidgets.add(widget);
                this.addRenderableWidget(widget);
            }
            for (int i = 0; i < outputItemCount; i++) {
                SlotWidget widget = new SlotWidget(116 + (i % 3) * 18, 26 + (i / 3) * 18, 18, 18, Component.empty());
                widget.output();
                slotWidgets.add(widget);
                this.addRenderableWidget(widget);
            }
        } else if (entry != null && entry.fluidCap() != null) {
            int inputFluidCount = entry.fluidCap().inputTanks.size();
            int outputFluidCount = entry.fluidCap().outputTanks.size();
            for (int i = 0; i < inputFluidCount; i++) {
                SlotWidget widget = new SlotWidget(30 + i * 20, 30, 18, 18, Component.empty());
                widget.fluid();
                slotWidgets.add(widget);
                this.addRenderableWidget(widget);
            }
            for (int i = 0; i < outputFluidCount; i++) {
                SlotWidget widget = new SlotWidget(115 + i * 20, 30, 18, 18, Component.empty());
                widget.fluid();
                widget.output();
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
        MultiblockEntry mbEntry = MultiblockRegistry.getByPort(menu.getBlockEntity().name);
        ModEntry entry = mbEntry != null ? mbEntry.controllerEntry() : null;
        if (entry == null) return;
        int inputItemCount  = entry.itemCap()  != null ? entry.itemCap().inputSlots        : 0;
        int inputFluidCount = entry.fluidCap() != null ? entry.fluidCap().inputTanks.size() : 0;
        int outputItemCount = entry.itemCap()  != null ? entry.itemCap().outputSlots        : 0;
        int outputFluidCount = entry.fluidCap() != null ? entry.fluidCap().outputTanks.size() : 0;

        if (layout == null) {
            for (int i = 0; i < inputFluidCount; i++) {
                int fx = x + 30 + i * 20, fy = y + 30;
                if (tooltip) {
                    GuiFluidRenderer.renderFluidTooltip(guiGraphics, mouseX, mouseY, fx, fy, 16, 16,
                            tanks.getFluidInTank(i), tanks.getTankCapacity(i));
                } else {
                    GuiFluidRenderer.renderFluidTank(guiGraphics, fx, fy, 16, 16,
                            tanks.getFluidInTank(i), tanks.getTankCapacity(i));
                }
            }
            for (int i = 0; i < outputFluidCount; i++) {
                int fx = x + 115 + i * 20, fy = y + 30;
                int tankIndex = inputFluidCount + i;
                if (tooltip) {
                    GuiFluidRenderer.renderFluidTooltip(guiGraphics, mouseX, mouseY, fx, fy, 16, 16,
                            tanks.getFluidInTank(tankIndex), tanks.getTankCapacity(tankIndex));
                } else {
                    GuiFluidRenderer.renderFluidTank(guiGraphics, fx, fy, 16, 16,
                            tanks.getFluidInTank(tankIndex), tanks.getTankCapacity(tankIndex));
                }
            }
            return;
        }

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
