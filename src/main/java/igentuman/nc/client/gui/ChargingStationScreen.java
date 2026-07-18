package igentuman.nc.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import igentuman.nc.block.entity.ChargingStationBE;
import igentuman.nc.client.gui.element.NCGuiElement;
import igentuman.nc.client.gui.element.fluid.FluidTankRenderer;
import igentuman.nc.client.gui.element.slot.BigSlot;
import igentuman.nc.container.ChargingStationContainer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.util.TextUtils.__;

public class ChargingStationScreen extends AbstractContainerScreen<ChargingStationContainer> {

    private static final ResourceLocation GUI = rl("textures/gui/processor.png");
    private static final NumberFormat NF = NumberFormat.getIntegerInstance();

    private static final int FLUID_X = 8;
    private static final int FLUID_Y = 17;
    private static final int FLUID_W = 16;
    private static final int FLUID_H = 52;

    private static final int ENERGY_X = 30;
    private static final int ENERGY_Y = 17;
    private static final int ENERGY_W = 16;
    private static final int ENERGY_H = 52;

    private static final int SLOT_X = 80;
    private static final int SLOT_Y = 35;

    private FluidTankRenderer fluidRenderer;
    private BigSlot itemSlot;

    public ChargingStationScreen(ChargingStationContainer container, Inventory inv, Component name) {
        super(container, inv, name);
        imageWidth = 176;
        imageHeight = 176;
        inventoryLabelY = imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        NCGuiElement.RELATIVE_X = leftPos;
        NCGuiElement.RELATIVE_Y = topPos;
        fluidRenderer = new FluidTankRenderer(menu.blockEntity.fluidTank, FLUID_W, FLUID_H,
                leftPos + FLUID_X, topPos + FLUID_Y);
        itemSlot = new BigSlot(SLOT_X, SLOT_Y, "_in");
    }

    @Override
    public void render(@NotNull PoseStack graphics, int mouseX, int mouseY, float partialTicks) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTicks);
        renderTooltip(graphics, mouseX, mouseY);
        renderBarTooltips(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(@NotNull PoseStack graphics, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, GUI);
        NCGuiElement.RELATIVE_X = leftPos;
        NCGuiElement.RELATIVE_Y = topPos;
        blit(graphics, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        fill(graphics, leftPos + FLUID_X - 1, topPos + FLUID_Y - 1,
                leftPos + FLUID_X + FLUID_W + 1, topPos + FLUID_Y + FLUID_H + 1, 0xFF373737);
        fill(graphics, leftPos + FLUID_X, topPos + FLUID_Y,
                leftPos + FLUID_X + FLUID_W, topPos + FLUID_Y + FLUID_H, 0xFF000000);

        fill(graphics, leftPos + ENERGY_X - 1, topPos + ENERGY_Y - 1,
                leftPos + ENERGY_X + ENERGY_W + 1, topPos + ENERGY_Y + ENERGY_H + 1, 0xFF373737);
        fill(graphics, leftPos + ENERGY_X, topPos + ENERGY_Y,
                leftPos + ENERGY_X + ENERGY_W, topPos + ENERGY_Y + ENERGY_H, 0xFF000000);

        ChargingStationBE be = menu.blockEntity;
        if (be != null) {
            if (fluidRenderer != null) fluidRenderer.draw(graphics, mouseX, mouseY, partialTicks);

            int energy = be.energy.getEnergyStored();
            int energyMax = be.energy.getMaxEnergyStored();
            if (energyMax > 0 && energy > 0) {
                int h = (int) ((long) energy * ENERGY_H / energyMax);
                fill(graphics, leftPos + ENERGY_X, topPos + ENERGY_Y + (ENERGY_H - h),
                        leftPos + ENERGY_X + ENERGY_W, topPos + ENERGY_Y + ENERGY_H, 0xFFE52727);
            }
        }

        if (itemSlot != null) itemSlot.draw(graphics, mouseX, mouseY, partialTicks);
    }

    private void renderBarTooltips(PoseStack graphics, int mouseX, int mouseY) {
        ChargingStationBE be = menu.blockEntity;
        if (be == null) return;
        if (inBox(mouseX, mouseY, leftPos + FLUID_X, topPos + FLUID_Y, FLUID_W, FLUID_H)) {
            List<Component> lines = new ArrayList<>(fluidRenderer.getTooltips());
            if (lines.isEmpty()) {
                lines.add(__("tooltip.nc.charging_station.fluid_empty").withStyle(ChatFormatting.GRAY));
            }
            renderComponentTooltip(graphics, lines, mouseX, mouseY);
        }
        if (inBox(mouseX, mouseY, leftPos + ENERGY_X, topPos + ENERGY_Y, ENERGY_W, ENERGY_H)) {
            List<Component> lines = new ArrayList<>();
            lines.add(__("tooltip.nc.charging_station.energy",
                    NF.format(be.energy.getEnergyStored()), NF.format(be.energy.getMaxEnergyStored()))
                    .withStyle(ChatFormatting.RED));
            renderComponentTooltip(graphics, lines, mouseX, mouseY);
        }
    }

    private static boolean inBox(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    @Override
    protected void renderLabels(@NotNull PoseStack graphics, int mouseX, int mouseY) {
        drawString(graphics, font, title, titleLabelX, titleLabelY, 0x404040);
        drawString(graphics, font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040);
    }
}
