package igentuman.nc.screen;

import igentuman.nc.container.MultiblockControllerContainer;
import igentuman.nc.screen.element.BeamDiverterOutputButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class BeamDiverterScreen extends MultiblockControllerScreen {

    private BeamDiverterOutputButton outputButton;

    public BeamDiverterScreen(MultiblockControllerContainer menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        outputButton = new BeamDiverterOutputButton(leftPos + 30, topPos + 80, menu);
        addRenderableWidget(outputButton);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);
        outputButton.visible = menu.isFormed();
    }
}
