package igentuman.nc.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import igentuman.nc.container.RedstoneDImmerContainer;
import igentuman.nc.container.StorageContainerContainer;
import igentuman.nc.util.annotation.NothingNullByDefault;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import static igentuman.nc.NuclearCraft.rl;

@NothingNullByDefault
public class RedstoneDimmerScreen extends AbstractContainerScreen<RedstoneDImmerContainer> {

    private final ResourceLocation GUI;

    public RedstoneDimmerScreen(RedstoneDImmerContainer container, Inventory inv, Component name) {
        super(container, inv, name);
        GUI = rl("textures/gui/0.png");
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics matrixStack, int mouseX, int mouseY) {
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, GUI);
        int relX = (this.width - this.imageWidth) / 2;
        int relY = (this.height - this.imageHeight) / 2;
        graphics.blit(GUI, relX, relY, 0, 0, this.imageWidth, this.imageHeight);
    }
}