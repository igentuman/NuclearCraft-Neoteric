package igentuman.nc.screen;

import igentuman.nc.container.MultiblockControllerContainer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;
import java.util.Optional;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.util.TextUtils.__;

public class DecayChamberScreen extends MultiblockControllerScreen {

    public DecayChamberScreen(MultiblockControllerContainer menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        backgroundTexture = rl("textures/gui/accelerators/decay_chamber_controller.png");
        imageWidth = 176;
        imageHeight = 120;
        inventoryLabelY = imageHeight - 94;
    }

    private static final int[][] OUTPUT_POSITIONS = {{101, 14}, {101, 37}, {101, 60}};

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 4210752, false);

        int localMouseX = mouseX - leftPos;
        int localMouseY = mouseY - topPos;
        renderParticleChannel(guiGraphics, localMouseX, localMouseY, 68, 37, "input", 0);
        for (int i = 0; i < OUTPUT_POSITIONS.length; i++) {
            renderParticleChannel(guiGraphics, localMouseX, localMouseY, OUTPUT_POSITIONS[i][0], OUTPUT_POSITIONS[i][1], "output", i);
        }
    }

    @Override
    protected void init() {
        super.init();
        progressBar.visible = false;
    }
}
