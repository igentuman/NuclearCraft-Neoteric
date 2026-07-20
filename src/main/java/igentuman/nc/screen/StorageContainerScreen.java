package igentuman.nc.screen;

import igentuman.nc.container.StorageContainerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import static igentuman.nc.NuclearCraft.rl;

public class StorageContainerScreen extends AbstractContainerScreen<StorageContainerMenu> {

    private final ResourceLocation gui;

    public StorageContainerScreen(StorageContainerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.gui = rl("textures/gui/storage/" + menu.getTier() + ".png");
        this.imageWidth = menu.getColumns() * 18 + 20;
        this.imageHeight = (menu.getRows() + 4) * 18 + 20;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        graphics.blit(gui, x, y, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
    }
}
