package igentuman.nc.screen;

import igentuman.nc.block_entity.fusion.FusionReactorControllerBE;
import igentuman.nc.container.MultiblockControllerContainer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import static igentuman.nc.util.TextUtils.__;

public class FusionReactorScreen extends MultiblockControllerScreen {

    public FusionReactorScreen(MultiblockControllerContainer menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);
        drawCenteredString(guiGraphics, this.font, __("screen.nuclearcraft.fusion_reactor"), imageWidth / 2, this.titleLabelY, 4210752);
        if (!(menu.getBlockEntity() instanceof FusionReactorControllerBE be)) return;

        int x = 30;
        int y = 24;
        line(guiGraphics, x, y, __("screen.nuclearcraft.fusion.charge", be.functionalBlocksCharge));
        line(guiGraphics, x, y + 10, __("screen.nuclearcraft.fusion.plasma", be.plasmaTemperature / 1_000_000));
        line(guiGraphics, x, y + 20, __("screen.nuclearcraft.fusion.heat", (long) be.reactorHeat));
        line(guiGraphics, x, y + 30, __("screen.nuclearcraft.fusion.efficiency", (int) (be.efficiency * 100)));
        line(guiGraphics, x, y + 40, __("screen.nuclearcraft.fusion.output", be.energyPerTick));
        line(guiGraphics, x, y + 50, __("screen.nuclearcraft.fusion.amplification", be.amplificationAdjustment));
    }

    private void line(GuiGraphics g, int x, int y, Component text) {
        g.drawString(this.font, text, x, y, 0x404040, false);
    }
}
