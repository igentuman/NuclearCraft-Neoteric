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

public class TargetChamberScreen extends MultiblockControllerScreen {

    public TargetChamberScreen(MultiblockControllerContainer menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        backgroundTexture = rl("textures/gui/accelerators/target_chamber_controller.png");
        imageWidth = 176;
        imageHeight = 200;
        inventoryLabelY = imageHeight - 94;
    }

    private static final int[][] OUTPUT_POSITIONS = {{86, 15}, {146, 46}, {86, 78}};

    public void init() {
        super.init();
        progressBar.setY(progressBar.getY() - 10);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);
        int x = progressBar.getX();
        int y = progressBar.getY();
        if (mouseX >= x && mouseX < x + progressBar.getWidth() && mouseY >= y && mouseY < y + progressBar.getHeight()) {
            List<Component> tooltip = List.of(
                    __("tooltip.nuclearcraft.target_chamber.progress", menu.getProgress()).withStyle(ChatFormatting.GRAY));
            guiGraphics.renderTooltip(font, tooltip, Optional.empty(), mouseX, mouseY);
        }

        int localMouseX = mouseX - leftPos;
        int localMouseY = mouseY - topPos;
        renderParticleChannel(guiGraphics, localMouseX, localMouseY, 18, 46, "input", 0);
        for (int i = 0; i < OUTPUT_POSITIONS.length; i++) {
            renderParticleChannel(guiGraphics, localMouseX, localMouseY, OUTPUT_POSITIONS[i][0], OUTPUT_POSITIONS[i][1], "output", i);
        }
    }
}
