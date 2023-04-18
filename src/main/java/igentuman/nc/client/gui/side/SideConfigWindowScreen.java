package igentuman.nc.client.gui.side;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import igentuman.nc.client.NcClient;
import igentuman.nc.client.gui.element.NCGuiElement;
import igentuman.nc.client.gui.element.button.Button;
import igentuman.nc.client.gui.element.button.SideConfig;
import igentuman.nc.container.NCProcessorContainer;
import igentuman.nc.util.sided.SlotModePair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;

import static igentuman.nc.NuclearCraft.MODID;

public class SideConfigWindowScreen<T extends NCProcessorContainer> extends AbstractContainerScreen<T> {
    protected final ResourceLocation GUI = new ResourceLocation(MODID, "textures/gui/small_window.png");
    protected int relX;
    protected int relY;

    protected AbstractContainerScreen parentScreen;


    private int slotId;

    public List<NCGuiElement> widgets = new ArrayList<>();
    public SideConfigWindowScreen(T container, Inventory inv, Component name) {
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
        updateRelativeCords();
        widgets.clear();
        int x = 30;
        int y = 10;
        widgets.add(new SideConfig(x, y, slotId, this, Direction.UP.ordinal()));
        widgets.add(new SideConfig(x-22, y+22, slotId, this, Direction.WEST.ordinal()));
        widgets.add(new SideConfig(x, y+22, slotId, this, Direction.NORTH.ordinal()));
        widgets.add(new SideConfig(x+22, y+22, slotId, this, Direction.EAST.ordinal()));
        widgets.add(new SideConfig(x-22, y+44, slotId, this, Direction.SOUTH.ordinal()));
        widgets.add(new SideConfig(x, y+44, slotId, this, Direction.DOWN.ordinal()));
        widgets.add(new Button.CloseConfig(6, 6, this));
    }

    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        for(NCGuiElement widget : widgets) {
            if(widget.mouseClicked(pMouseX, pMouseY, pButton)) {
                return true;
            }
        }
        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().forceSetScreen(parentScreen);
    }

    public SideConfigWindowScreen(AbstractContainerScreen parentScreen, int slotId) {
        this((T)parentScreen.getMenu(), NcClient.tryGetClientPlayer().getInventory(), Component.empty());
        this.parentScreen = parentScreen;
        this.slotId = slotId;
    }

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        int i = this.leftPos;
        int j = this.topPos;
        this.renderBg(matrixStack, partialTicks, mouseX, mouseY);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.ContainerScreenEvent.Render.Background(this, matrixStack, mouseX, mouseY));
        RenderSystem.disableDepthTest();
        for(Widget widget : this.renderables) {
            widget.render(matrixStack, mouseX, mouseY, partialTicks);
        }
        PoseStack posestack = RenderSystem.getModelViewStack();
        posestack.pushPose();
        posestack.translate((double)i, (double)j, 0.0D);
        RenderSystem.applyModelViewMatrix();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        this.hoveredSlot = null;
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        this.renderLabels(matrixStack, mouseX, mouseY);
        posestack.popPose();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.enableDepthTest();
        this.renderTooltip(matrixStack, mouseX, mouseY);
    }

    private void renderWidgets(PoseStack matrix, float partialTicks, int mouseX, int mouseY) {
        for(NCGuiElement widget: widgets) {
            widget.draw(matrix, mouseX, mouseY, partialTicks);
        }
    }

    @Override
    protected void renderLabels(PoseStack matrixStack, int mouseX, int mouseY) {
    }

    @Override
    protected void renderBg(PoseStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, GUI);
        updateRelativeCords();
        this.blit(matrixStack, relX, relY, 0, 0, this.imageWidth, this.imageHeight);
        renderWidgets(matrixStack, partialTicks, mouseX, mouseY);
    }


    public BlockPos getPosition() {
        return menu.getPosition();
    }

    public SlotModePair.SlotMode getSlotMode(int direction, int slotId) {
        return menu.getSlotMode(direction, slotId);
    }
}
