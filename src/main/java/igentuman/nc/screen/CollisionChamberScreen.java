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

public class CollisionChamberScreen extends MultiblockControllerScreen {

    private static final int CONTENT_OFFSET_Y = 20;

    public CollisionChamberScreen(MultiblockControllerContainer menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        backgroundTexture = rl("textures/gui/accelerators/collision_chamber_controller.png");
        imageWidth = 176;
        imageHeight = 200;
        inventoryLabelY = imageHeight - 94;
    }

    private static final int[][] INPUT_POSITIONS = {{46, 45}, {114, 45}};
    private static final int[][] OUTPUT_POSITIONS = {{49, 14}, {111, 14}, {111, 76}, {49, 76}};

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 4210752, false);
        int x = progressBar.getX();
        int y = progressBar.getY();
        if (mouseX >= x && mouseX < x + progressBar.getWidth() && mouseY >= y && mouseY < y + progressBar.getHeight()) {
            List<Component> tooltip = List.of(
                    __("tooltip.nuclearcraft.collision_chamber.progress", menu.getProgress()).withStyle(ChatFormatting.GRAY));
            guiGraphics.renderTooltip(font, tooltip, Optional.empty(), mouseX, mouseY);
        }

        int localMouseX = mouseX - leftPos;
        int localMouseY = mouseY - topPos;
        for (int i = 0; i < INPUT_POSITIONS.length; i++) {
            renderParticleChannel(guiGraphics, localMouseX, localMouseY, INPUT_POSITIONS[i][0], INPUT_POSITIONS[i][1] + CONTENT_OFFSET_Y, "input", i);
        }
        for (int i = 0; i < OUTPUT_POSITIONS.length; i++) {
            renderParticleChannel(guiGraphics, localMouseX, localMouseY, OUTPUT_POSITIONS[i][0], OUTPUT_POSITIONS[i][1] + CONTENT_OFFSET_Y, "output", i);
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        super.renderBg(guiGraphics, partialTick, mouseX, mouseY);
        guiGraphics.fill(leftPos + 45, topPos + 13, leftPos + 131, topPos + 93, 0xFFC6C6C6);
        guiGraphics.blit(backgroundTexture, leftPos + 45, topPos + 13 + CONTENT_OFFSET_Y,
                45, 13, 86, 80, 256, 256);
    }

    @Override
    protected void init() {
        super.init();
        progressBar.visible = false;
        infoCheckbox.setY(infoCheckbox.getY() + CONTENT_OFFSET_Y);
        if (energyBar != null) {
            energyBar.setY(energyBar.getY() + CONTENT_OFFSET_Y);
        }
    }
}
