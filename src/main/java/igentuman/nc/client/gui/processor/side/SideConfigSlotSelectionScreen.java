package igentuman.nc.client.gui.processor.side;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.matrix.MatrixStack;
import igentuman.nc.client.NcClient;
import igentuman.nc.client.gui.IProgressScreen;
import igentuman.nc.client.gui.element.NCGuiElement;
import igentuman.nc.client.gui.element.bar.EnergyBar;
import igentuman.nc.client.gui.element.bar.ProgressBar;
import igentuman.nc.client.gui.element.button.Button;
import igentuman.nc.client.gui.element.slot.BigSlot;
import igentuman.nc.client.gui.element.slot.NormalSlot;
import igentuman.nc.container.NCProcessorContainer;
import igentuman.nc.content.processors.config.ProcessorSlots;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.GuiContainerEvent;

import java.util.ArrayList;
import java.util.List;

import static igentuman.nc.NuclearCraft.MODID;

public class SideConfigSlotSelectionScreen<T extends NCProcessorContainer> extends ContainerScreen<T> implements IProgressScreen {
    protected final ResourceLocation GUI = new ResourceLocation(MODID, "textures/gui/window_no_inventory.png");
    protected int relX;
    protected int relY;

    protected ContainerScreen<T> parentScreen;

    private ProcessorSlots slots;

    public List<NCGuiElement> widgets = new ArrayList<>();
    private EnergyBar energyBar;

    public SideConfigSlotSelectionScreen(T container, PlayerInventory inv, TextComponent name) {
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
        slots = menu.getProcessor().getSlotsConfig();
        updateRelativeCords();
        widgets.clear();
        energyBar = new EnergyBar(9, 4, menu.getEnergy());
        widgets.add(energyBar);
        int progressBarX = 71;
        if(slots.getOutputItems()+slots.getOutputFluids() > 6) {
            progressBarX -= ProcessorSlots.margin;
        }
        widgets.add(new ProgressBar(progressBarX, 40, this, menu.getProcessor().progressBar));
        for(int i = 0; i < slots.slotsCount();i++) {
            if(slots.outputSlotsCount() == 1 && slots.getSlotType(i).contains("_out")) {
                widgets.add(new BigSlot(slots.getSlotPos(i), slots.getSlotType(i)).forConfig(this,  i));
            } else {
                if(!menu.getProcessor().isSlotHidden(i)) {
                    widgets.add(new NormalSlot(slots.getSlotPos(i), slots.getSlotType(i)).forConfig(this, i));
                }
            }
        }
        widgets.add(new Button.CloseConfig(29, 74, this));
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

    public SideConfigSlotSelectionScreen(ContainerScreen parentScreen) {
        this((T)parentScreen.getMenu(), NcClient.tryGetClientPlayer().inventory, new TranslationTextComponent(""));
        this.parentScreen = parentScreen;
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        int i = this.leftPos;
        int j = this.topPos;
        this.renderBg(matrixStack, partialTicks, mouseX, mouseY);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new GuiContainerEvent.DrawBackground(this, matrixStack, mouseX, mouseY));
        RenderSystem.disableDepthTest();
       /* for(Widget widget : this.renderables) {
            widget.render(matrixStack, mouseX, mouseY, partialTicks);
        }*/
        MatrixStack posestack = matrixStack;
        posestack.pushPose();
        posestack.translate((double)i, (double)j, 0.0D);
        //RenderSystem.applyModelViewMatrix();
        //RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        this.hoveredSlot = null;
       // RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        this.renderLabels(matrixStack, mouseX, mouseY);
        posestack.popPose();
      //  RenderSystem.applyModelViewMatrix();
        RenderSystem.enableDepthTest();
        this.renderTooltip(matrixStack, mouseX, mouseY);
    }

    private void renderWidgets(MatrixStack matrix, float partialTicks, int mouseX, int mouseY) {
        for(NCGuiElement widget: widgets) {
            widget.draw(matrix, mouseX, mouseY, partialTicks);
        }
    }

    @Override
    protected void renderLabels(MatrixStack matrixStack, int mouseX, int mouseY) {
        drawCenteredString(matrixStack, font,  new TranslationTextComponent("processor_side_config.title"), imageWidth/2, titleLabelY, 0xffffff);
    }

    @Override
    protected void renderBg(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        Minecraft.getInstance().getTextureManager().bind(GUI);
        updateRelativeCords();
        this.blit(matrixStack, relX, relY, 0, 0, this.imageWidth, this.imageHeight);
        renderWidgets(matrixStack, partialTicks, mouseX, mouseY);
    }

    @Override
    public double getProgress() {
        return menu.getProgress();
    }
}
