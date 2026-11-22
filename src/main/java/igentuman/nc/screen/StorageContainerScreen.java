package igentuman.nc.screen;

import net.minecraft.client.renderer.RenderPipelines;
import igentuman.nc.container.StorageContainerMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

import static igentuman.nc.NuclearCraft.rl;

public class StorageContainerScreen extends AbstractContainerScreen<StorageContainerMenu> {

    private final Identifier gui;

    public StorageContainerScreen(StorageContainerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, menu.getColumns() * 18 + 20, (menu.getRows() + 4) * 18 + 20);
        this.gui = rl("textures/gui/storage/" + menu.getTier() + ".png");
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTicks);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        super.extractBackground(graphics, mouseX, mouseY, partialTicks);
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        graphics.blit(RenderPipelines.GUI_TEXTURED, gui, x, y, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
    }
}
