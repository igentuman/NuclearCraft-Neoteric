package igentuman.nc.gui.fission;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import igentuman.nc.container.FissionControllerContainer;
import igentuman.nc.gui.IProgressScreen;
import igentuman.nc.gui.element.NCGuiElement;
import igentuman.nc.gui.element.bar.EnergyBar;
import igentuman.nc.gui.element.bar.ProgressBar;
import igentuman.nc.gui.element.bar.VerticalBar;
import igentuman.nc.gui.element.button.Button;
import igentuman.nc.gui.element.button.Checkbox;
import igentuman.nc.gui.element.slot.BigSlot;
import igentuman.nc.gui.element.slot.NormalSlot;
import igentuman.nc.setup.processors.config.ProcessorSlots;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

import java.util.ArrayList;
import java.util.List;

import static igentuman.nc.NuclearCraft.MODID;

public class FissionControllerScreen extends AbstractContainerScreen<FissionControllerContainer> implements IProgressScreen {
    protected final ResourceLocation GUI = new ResourceLocation(MODID, "textures/gui/fission/controller.png");
    protected int relX;
    protected int relY;
    private int xCenter;

    public FissionControllerContainer container()
    {
        return (FissionControllerContainer)menu;
    }

    public List<NCGuiElement> widgets = new ArrayList<>();
    public Checkbox checkboxCasing;
    public Checkbox checkboxInterior;
    private VerticalBar energyBar;
    private VerticalBar heatBar;
    private VerticalBar coolantBar;
    private VerticalBar hotCoolantBar;

    public FissionControllerScreen(FissionControllerContainer container, Inventory inv, Component name) {
        super(container, inv, name);
        imageWidth = 176;
        imageHeight = 176;
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
        checkboxCasing = new Checkbox(imageWidth-19, 70, this,  isCasingValid());
        checkboxInterior =  new Checkbox(imageWidth-32, 70, this,  isInteriorValid());
        energyBar = new VerticalBar.Energy(17, 16,  menu.getEnergy(), 1000000);
        heatBar = new VerticalBar.Heat(8, 16,  menu.getHeat(), 1000000);
        coolantBar = new VerticalBar.Coolant(17, 16,  menu.getEnergy(), 1000000);
        hotCoolantBar = new VerticalBar.HotCoolant(26, 16,  menu.getHeat(), 1000000);
        widgets.add(heatBar);
        widgets.add(new ProgressBar(74, 35, this,  6));
    }

    private boolean isInteriorValid() {
        return  container().isInteriorValid();
    }

    private boolean isCasingValid() {
        return  container().isCasingValid();
    }

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        xCenter = getGuiLeft()-imageWidth/2;
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.renderTooltip(matrixStack, mouseX, mouseY);
    }

    private void renderWidgets(PoseStack matrix, float partialTicks, int mouseX, int mouseY) {
        for(NCGuiElement widget: widgets) {
            widget.draw(matrix, mouseX, mouseY, partialTicks);
        }
        checkboxCasing.setChecked(isCasingValid()).draw(matrix, mouseX, mouseY, partialTicks);
        checkboxInterior.setChecked(isInteriorValid()).draw(matrix, mouseX, mouseY, partialTicks);
        energyBar.draw(matrix, mouseX, mouseY, partialTicks);
    }

    @Override
    protected void renderLabels(PoseStack matrixStack, int mouseX, int mouseY) {
        drawCenteredString(matrixStack, font,  menu.getTitle(), imageWidth/2, titleLabelY, 0xffffff);
        if(isCasingValid()) {
            Component pText = Component.translatable("reactor.size", getMultiblockHeight(), getMultiblockWidth(), getMultiblockDepth());
            FormattedCharSequence dimensionsFormatted = pText.getVisualOrderText();
            drawString(matrixStack, font, pText,  imageWidth-font.width(dimensionsFormatted)-8, 85, 0xffffff);
        } else {
            Component pText = Component.translatable(getValidationResultKey(), getValidationResultData());
            FormattedCharSequence dimensionsFormatted = pText.getVisualOrderText();
            drawString(matrixStack, font, pText,  imageWidth-font.width(dimensionsFormatted)-8, 85, 0xff0000);
        }
    }

    private Object getValidationResultData() {
        return container().getValidationResultData().toShortString();
    }

    private String getValidationResultKey() {
        return container().getValidationResultKey();
    }

    private int getMultiblockHeight() {
        return container().getHeight();
    }

    private int getMultiblockWidth() {
        return container().getWidth();
    }

    private int getMultiblockDepth() {
        return container().getDepth();
    }

    @Override
    protected void renderBg(PoseStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, GUI);
        updateRelativeCords();
        this.blit(matrixStack, relX, relY, 0, 0, this.imageWidth, this.imageHeight);
        renderWidgets(matrixStack, partialTicks, mouseX, mouseY);
    }

    private void renderTooltips(PoseStack pPoseStack, int pMouseX, int pMouseY, int x, int y) {
        for(NCGuiElement widget: widgets) {
            //renderTooltip(pPoseStack, widget.renderToolTip(), x, y);
        }
    }

    @Override
    public int getProgress() {
        return container().getProgress();
    }
}
