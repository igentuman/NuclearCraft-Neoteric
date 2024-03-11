package igentuman.nc.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.matrix.MatrixStack;
import igentuman.nc.container.StorageContainerContainer;
import igentuman.nc.util.annotation.NothingNullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.inventory.Inventory;
import net.minecraft.util.text.ITextComponent;

import static igentuman.nc.NuclearCraft.rl;

@NothingNullByDefault
public class StorageContainerScreen extends ContainerScreen<StorageContainerContainer<?>> {

    private final ResourceLocation GUI;

    public StorageContainerScreen(StorageContainerContainer container, PlayerInventory inv, ITextComponent name) {
        super(container, inv, name);
        GUI = rl("textures/gui/storage/"+container.getTier()+".png");
        imageWidth = getColls()*18+20;
        imageHeight = (getRows()+4)*18+20;
    }

    private int getRows() {
        return menu.getRows();
    }

    private int getColls() {
        return menu.getColls();
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.renderTooltip(matrixStack, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(MatrixStack matrixStack, int mouseX, int mouseY) {
    }

    @Override
    protected void renderBg(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        Minecraft.getInstance().getTextureManager().bind(GUI);
        int relX = (this.width - this.imageWidth) / 2;
        int relY = (this.height - this.imageHeight) / 2;
        this.blit(matrixStack, relX, relY, 0, 0, this.imageWidth, this.imageHeight);
    }
}