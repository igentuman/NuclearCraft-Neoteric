package igentuman.nc.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import igentuman.nc.client.gui.element.NCGuiElement;
import igentuman.nc.client.gui.element.button.Button;
import igentuman.nc.container.MultiblockBuilderContainer;
import igentuman.nc.util.annotation.NothingNullByDefault;
import igentuman.nc.util.builder.MultiblockRenderer;
import igentuman.nc.util.builder.ReactorDesignParser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static igentuman.nc.NuclearCraft.rl;

@NothingNullByDefault
public class MultiblockBuilderScreen extends AbstractContainerScreen<MultiblockBuilderContainer> {

    protected final ResourceLocation GUI = rl("textures/gui/window_no_inventory.png");
    public List<NCGuiElement> widgets = new ArrayList<>();
    public String jsonText = "";
    private Button.InsertJson insertBtn;
    private Button.Build buildBtn;
    protected int relX;
    protected int relY;
    public HashMap<BlockPos, Block> blockMap = new HashMap<>();
    public MultiblockBuilderScreen(MultiblockBuilderContainer container, Inventory inv, Component name) {
        super(container, inv, name);
        imageWidth = 180;
        imageHeight = 180;
    }

    protected void updateRelativeCords()
    {
        relX = (this.width - this.imageWidth) / 2;
        relY = (this.height - this.imageHeight) / 2;
        NCGuiElement.RELATIVE_X = relX;
        NCGuiElement.RELATIVE_Y = relY;
    }

    protected void init() {
        super.init();
        Minecraft mc = Minecraft.getInstance();
        updateRelativeCords();
        insertBtn = new Button.InsertJson(150, 30, this, menu.getPosition());
        buildBtn = new Button.Build(150, 60, this, menu.getPosition());
        widgets.clear();
        widgets.add(insertBtn);
        widgets.add(buildBtn);
    }
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        Minecraft mc = Minecraft.getInstance();
        graphics.drawString(mc.font, this.title, 8, 6, 4210752);
        graphics.drawString(mc.font, this.jsonText, 8, 20, 4210752);

    }

    private void renderWidgets(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        for(NCGuiElement widget: widgets) {
            widget.draw(graphics, mouseX, mouseY, partialTicks);
        }
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        for(NCGuiElement widget : widgets) {
            if(widget.mouseClicked(pMouseX, pMouseY, pButton)) {
                return true;
            }
        }
        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, GUI);
        int relX = (this.width - this.imageWidth) / 2;
        int relY = (this.height - this.imageHeight) / 2;
        graphics.blit(GUI, relX, relY, 0, 0, this.imageWidth, this.imageHeight);
        renderWidgets(graphics, partialTicks, mouseX, mouseY);
        if(!blockMap.isEmpty()) {
            MultiblockRenderer.render(blockMap, graphics.pose(), relX+10, relY+60, 40, 40);
        }
    }
}