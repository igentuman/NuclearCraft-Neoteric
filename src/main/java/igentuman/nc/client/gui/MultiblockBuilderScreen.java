package igentuman.nc.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import igentuman.nc.client.gui.element.NCGuiElement;
import igentuman.nc.client.gui.element.button.Button;
import igentuman.nc.container.MultiblockBuilderContainer;
import igentuman.nc.handler.event.client.BlockOverlayHandler;
import igentuman.nc.util.annotation.NothingNullByDefault;
import igentuman.nc.util.builder.MultiblockRenderer;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.util.TextUtils.__;

@NothingNullByDefault
public class MultiblockBuilderScreen extends AbstractContainerScreen<MultiblockBuilderContainer> {

    protected final ResourceLocation GUI = rl("textures/gui/multiblock_builder.png");
    public List<NCGuiElement> widgets = new ArrayList<>();
    public String jsonText = "";
    private Button.InsertJson insertBtn;
    private Button.Build buildBtn;
    private Button.Link linkBtn;
    protected int relX;
    protected int relY;
    public HashMap<BlockPos, Block> blockMap = new HashMap<>();
    public MultiblockBuilderScreen(MultiblockBuilderContainer container, Inventory inv, Component name) {
        super(container, inv, name);
        imageWidth = 178;
        imageHeight = 238;
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
        blockMap = getMenu().getBlocksMap();
        insertBtn = new Button.InsertJson(128, 7, this, menu.getPosition());
        buildBtn = new Button.Build(110, 7, this, menu.getPosition());
        linkBtn = new Button.Link(150, 7, this, menu.getPosition(), "https://discord.gg/fGBZj3haga",
                List.of(
                        __("tooltip.nc.link.designs"),
                        __("tooltip.nc.link.designs.descr")
                )
        );
        widgets.clear();
        widgets.add(linkBtn);
        widgets.add(insertBtn);
        widgets.add(buildBtn);
    }
    @Override
    public void render(PoseStack graphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(PoseStack graphics, int mouseX, int mouseY) {
        Minecraft mc = Minecraft.getInstance();
        if(!blockMap.isEmpty()) {
            drawString(graphics, mc.font, MultiblockRenderer.getSize(blockMap).toShortString().replace(", ", "x"), 8, 6, 4210752);
        } else {
            drawString(graphics, mc.font, this.title, 8, 6, 4210752);
        }
        renderTooltips(graphics, mouseX-relX, mouseY-relY);
    }

    private void renderWidgets(PoseStack graphics, float partialTicks, int mouseX, int mouseY) {
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
    protected void renderBg(PoseStack graphics, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, GUI);
        int relX = (this.width - this.imageWidth) / 2;
        int relY = (this.height - this.imageHeight) / 2;
        blit(graphics, relX, relY, 0, 0, this.imageWidth, this.imageHeight);
        renderWidgets(graphics, partialTicks, mouseX, mouseY);
        if(!blockMap.isEmpty()) {
            MultiblockRenderer.render(blockMap, graphics, relX+20, relY+40, 45, 45);
        }
        getMenu().setBlocksMap(blockMap);
    }

    private void renderTooltips(PoseStack graphics, int pMouseX, int pMouseY) {
        for(NCGuiElement widget: widgets) {
            if(widget.isMouseOver(pMouseX, pMouseY)) {
                renderTooltip(graphics, widget.getTooltips(),
                        Optional.empty(), pMouseX, pMouseY);
            }
        }
    }
}