package igentuman.nc.screen;

import igentuman.nc.block_entity.GlobalBlockEntity;
import igentuman.nc.container.UniversalProcessorContainer;
import igentuman.nc.network.PacketProcessorButtonPress;
import igentuman.nc.screen.element.EnergyBar;
import igentuman.nc.screen.element.FluidTankBar;
import igentuman.nc.screen.element.ProcessorImageButton;
import igentuman.nc.screen.element.SlotWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import igentuman.nc.handler.sided.FluidCapabilityHandler;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.util.TextUtils.__;

public class ChargingStationScreen extends AbstractContainerScreen<UniversalProcessorContainer> {

    private static final ResourceLocation TEXTURE = rl("textures/gui/processor.png");
    private static final int U_SIDE_CONFIG = 220;
    private static final int V_SIDE_CONFIG = 220;
    private static final int U_REDSTONE = 184;
    private static final int V_REDSTONE_BASE = 220;

    public ChargingStationScreen(UniversalProcessorContainer menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        imageWidth = 180;
        imageHeight = 180;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;

        GlobalBlockEntity be = menu.getBlockEntity();

        SlotWidget.RELATIVE_X = leftPos;
        SlotWidget.RELATIVE_Y = topPos;
        SlotWidget itemSlot = new SlotWidget(80, 35, 18, 18, net.minecraft.network.chat.Component.empty());
        itemSlot.item();
        itemSlot.input();
        addRenderableWidget(itemSlot);

        if (be.hasEnergyStorage()) {
            addRenderableWidget(new EnergyBar(leftPos + 8, topPos + 10, () -> be.energyStorage));
        }

        FluidCapabilityHandler tanks = be.contentHandler.getFluidHandler();
        if (tanks != null) {
            addRenderableWidget(new FluidTankBar(
                    leftPos + 24, topPos + 10, 12, 70,
                    () -> {
                        FluidCapabilityHandler h = be.contentHandler.getFluidHandler();
                        return h != null ? h.getFluidInTank(0) : net.neoforged.neoforge.fluids.FluidStack.EMPTY;
                    },
                    () -> {
                        FluidCapabilityHandler h = be.contentHandler.getFluidHandler();
                        return h != null ? h.getTankCapacity(0) : 0;
                    }
            ));
        }

        addRenderableWidget(new ProcessorImageButton(
                leftPos + imageWidth - 48, topPos + 74,
                U_SIDE_CONFIG, V_SIDE_CONFIG,
                () -> Minecraft.getInstance().setScreen(new SideConfigSlotSelectionScreen(this)),
                List.of(__("screen.nuclearcraft.side_config"))
        ));

        addRenderableWidget(new ProcessorImageButton(
                leftPos + imageWidth - 28, topPos + 74,
                () -> U_REDSTONE,
                () -> V_REDSTONE_BASE - menu.getRedstoneMode() * 36,
                () -> PacketDistributor.sendToServer(new PacketProcessorButtonPress(
                        menu.getPosition(), PacketProcessorButtonPress.REDSTONE_BTN_ID)),
                () -> List.of(__("screen.nuclearcraft.redstone_config_" + menu.getRedstoneMode()))
        ));
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
