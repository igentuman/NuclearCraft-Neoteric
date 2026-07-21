package igentuman.nc.screen;

import igentuman.nc.block_entity.GlobalBlockEntity;
import igentuman.nc.container.MultiblockControllerContainer;
import igentuman.nc.handler.sided.FluidCapabilityHandler;
import igentuman.nc.util.GuiFluidRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;

import static igentuman.nc.util.TextUtils.__;
import static igentuman.nc.util.TextUtils.formatEnergy;

public class TurbineControllerScreen extends MultiblockControllerScreen {

    private static final int INPUT_TANK_X = 26;
    private static final int OUTPUT_TANK_X = 150;
    private static final int TANK_Y = 18;
    private static final int TANK_W = 16;
    private static final int TANK_H = 60;

    public TurbineControllerScreen(MultiblockControllerContainer menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);
        renderFluidTanks(guiGraphics, mouseX - leftPos, mouseY - topPos);

        if (!menu.isFormed()) return;

        int x = 48;
        int y = 20;
        line(guiGraphics, x, y, "screen.nuclearcraft.turbine.output", formatEnergy(synced("energyPerTick")) + "/t");
        line(guiGraphics, x, y += 11, "screen.nuclearcraft.turbine.real_flow", synced("realFlow") + " mB/t");
        line(guiGraphics, x, y += 11, "screen.nuclearcraft.turbine.max_flow", synced("maxFlow") + " mB/t");
        line(guiGraphics, x, y += 11, "screen.nuclearcraft.turbine.ratio", synced("flowRatio") + "%");
        line(guiGraphics, x, y += 11, "screen.nuclearcraft.turbine.efficiency", synced("coilEfficiency") + "%");

    }

    @Override
    protected void init() {
        super.init();
        infoCheckbox.setY(infoCheckbox.getY() + 10);
        infoCheckbox.setX(infoCheckbox.getX() - 3);
        progressBar.visible = false;
    }

    private void renderFluidTanks(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        GlobalBlockEntity be = menu.getBlockEntity();
        if (!be.hasFluidTanks()) return;
        FluidCapabilityHandler tanks = be.contentHandler.getFluidHandler();
        if (tanks == null) return;

        GuiFluidRenderer.renderTankBevel(guiGraphics, INPUT_TANK_X, TANK_Y, TANK_W, TANK_H);
        GuiFluidRenderer.renderTankBevel(guiGraphics, OUTPUT_TANK_X, TANK_Y, TANK_W, TANK_H);

        GuiFluidRenderer.renderFluidTank(guiGraphics, INPUT_TANK_X, TANK_Y, TANK_W, TANK_H,
                tanks.getFluidInTank(0), tanks.getTankCapacity(0));
        GuiFluidRenderer.renderFluidTank(guiGraphics, OUTPUT_TANK_X, TANK_Y, TANK_W, TANK_H,
                tanks.getFluidInTank(1), tanks.getTankCapacity(1));

        GuiFluidRenderer.renderFluidTooltip(guiGraphics, mouseX, mouseY, INPUT_TANK_X, TANK_Y, TANK_W, TANK_H,
                tanks.getFluidInTank(0), tanks.getTankCapacity(0));
        GuiFluidRenderer.renderFluidTooltip(guiGraphics, mouseX, mouseY, OUTPUT_TANK_X, TANK_Y, TANK_W, TANK_H,
                tanks.getFluidInTank(1), tanks.getTankCapacity(1));
    }

    @Override
    public List<Component> infoCheckboxTooltip() {
        List<Component> tooltip = new ArrayList<>();
        if (!menu.isFormed()) {
            tooltip.add(__("screen.nuclearcraft.multiblock.not_assembled").withStyle(ChatFormatting.RED));
            return tooltip;
        }
        tooltip.add(__("screen.nuclearcraft.multiblock.assembled").withStyle(ChatFormatting.GREEN));
        tooltip.add(stat("screen.nuclearcraft.turbine.blades", String.valueOf(synced("bladeCount"))));
        tooltip.add(stat("screen.nuclearcraft.turbine.active_coils", String.valueOf(synced("activeCoils"))));
        tooltip.add(stat("screen.nuclearcraft.turbine.efficiency", synced("coilEfficiency") + "%"));
        tooltip.add(stat("screen.nuclearcraft.turbine.max_flow", synced("maxFlow") + " mB/t"));
        tooltip.add(stat("screen.nuclearcraft.turbine.max_output", formatEnergy(synced("maxEnergyGen")) + "/t"));
        return tooltip;
    }

    private Component stat(String key, String value) {
        return Component.translatable(key).append(": " + value).withStyle(ChatFormatting.GOLD);
    }

    private void line(GuiGraphics g, int x, int y, String key, String value) {
        float scale = 0.75f;
        g.pose().pushPose();
        g.pose().scale(scale, scale, 1f);
        g.drawString(font, Component.translatable(key).append(": " + value),
                (int) (x / scale), (int) (y / scale), 0x404040, false);
        g.pose().popPose();
    }

    private int synced(String field) {
        int idx = menu.getBlockEntity().getSyncFieldIndex(field);
        return idx >= 0 ? menu.getSyncedValue(idx) : 0;
    }
}
