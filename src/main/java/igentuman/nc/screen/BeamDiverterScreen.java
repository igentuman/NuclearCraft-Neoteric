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
        progressBar.visible = false;
        outputButton = new BeamDiverterOutputButton(leftPos + 30, topPos + 80, menu);
        addRenderableWidget(outputButton);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 4210752, false);
        outputButton.visible = menu.isFormed();
    }
}
