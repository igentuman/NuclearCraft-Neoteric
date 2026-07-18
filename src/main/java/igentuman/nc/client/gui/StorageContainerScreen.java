package igentuman.nc.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import igentuman.nc.container.StorageContainerContainer;
import igentuman.nc.util.annotation.NothingNullByDefault;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import static igentuman.nc.NuclearCraft.rl;

@NothingNullByDefault
public class StorageContainerScreen extends AbstractContainerScreen<StorageContainerContainer<?>> {

    private final ResourceLocation GUI;

    public StorageContainerScreen(StorageContainerContainer container, Inventory inv, Component name) {
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
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.renderTooltip(matrixStack, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(PoseStack matrixStack, int mouseX, int mouseY) {
    }

    @Override
    protected void renderBg(PoseStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, GUI);
        int relX = (this.width - this.imageWidth) / 2;
        int relY = (this.height - this.imageHeight) / 2;
        this.blit(matrixStack, relX, relY, 0, 0, this.imageWidth, this.imageHeight);
    }
}