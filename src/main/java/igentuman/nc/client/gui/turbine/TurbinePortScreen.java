package igentuman.nc.client.gui.turbine;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.matrix.MatrixStack;
import igentuman.nc.client.gui.IProgressScreen;
import igentuman.nc.client.gui.IVerticalBarScreen;
import igentuman.nc.client.gui.element.NCGuiElement;
import igentuman.nc.client.gui.element.bar.ProgressBar;
import igentuman.nc.client.gui.element.bar.VerticalBar;
import igentuman.nc.client.gui.element.button.Button;
import igentuman.nc.container.TurbinePortContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.ResourceLocation;
import net.minecraft.inventory.Inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static igentuman.nc.NuclearCraft.MODID;

public class TurbinePortScreen extends ContainerScreen<TurbinePortContainer> implements IProgressScreen, IVerticalBarScreen {
    protected final ResourceLocation GUI = new ResourceLocation(MODID, "textures/gui/turbine/port.png");
    protected int relX;
    protected int relY;
    private int xCenter;
    private Button.ReactorComparatorModeButton redstoneConfigBtn;

    public TurbinePortContainer container()
    {
        return (TurbinePortContainer)menu;
    }

    public List<NCGuiElement> widgets = new ArrayList<>();

    private VerticalBar energyBar;


    public TurbinePortScreen(TurbinePortContainer container, PlayerInventory inv, ITextComponent name) {
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
        Minecraft mc = Minecraft.getInstance();
        updateRelativeCords();
        widgets.clear();
        energyBar = new VerticalBar.Energy(17, 16,  this, container().getMaxEnergy());
        widgets.add(new ProgressBar(74, 35, this,  7));
        redstoneConfigBtn = new Button.ReactorComparatorModeButton(150, 74, this, menu.getPosition());
        widgets.add(redstoneConfigBtn);
    }


    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        xCenter = getGuiLeft()-imageWidth/2;
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.renderTooltip(matrixStack, mouseX, mouseY);
    }

    private void renderWidgets(MatrixStack matrix, float partialTicks, int mouseX, int mouseY) {
        redstoneConfigBtn.setMode(getMenu().getComparatorMode());
        redstoneConfigBtn.strength = getMenu().getAnalogSignalStrength();
        for(NCGuiElement widget: widgets) {
            widget.draw(matrix, mouseX, mouseY, partialTicks);
        }
        if(energyBar != null) {
            energyBar.draw(matrix, mouseX, mouseY, partialTicks);
        }
    }

    @Override
    protected void renderLabels(MatrixStack matrixStack, int mouseX, int mouseY) {
        drawCenteredString(matrixStack, font,  menu.getTitle(), imageWidth/2, titleLabelY, 0xffffff);
        renderTooltips(matrixStack, mouseX-relX, mouseY-relY);
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
    protected void renderBg(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        Minecraft.getInstance().getTextureManager().bind(GUI);
        updateRelativeCords();
        this.blit(matrixStack, relX, relY, 0, 0, this.imageWidth, this.imageHeight);
        renderWidgets(matrixStack, partialTicks, mouseX, mouseY);
    }

    private void renderTooltips(MatrixStack pMatrixStack, int pMouseX, int pMouseY) {

        for(NCGuiElement widget: widgets) {
           if(widget.isMouseOver(pMouseX, pMouseY)) {
/*               renderTooltip(pMatrixStack, widget.getTooltips(),
                       Optional.empty(), pMouseX, pMouseY);*/
           }
        }

        if(container().getMaxEnergy() > 0) {
            energyBar.clearTooltips();
            energyBar.addTooltip(new TranslationTextComponent("reactor.forge_energy_per_tick", container().energyPerTick()));
            if(energyBar.isMouseOver(pMouseX, pMouseY)) {
/*                renderTooltip(pMatrixStack, energyBar.getTooltips(),
                        Optional.empty(), pMouseX, pMouseY);*/
            }
        }
    }

    @Override
    public double getProgress() {
        return container().getProgress();
    }

    @Override
    public double getEnergy() {
        return container().getEnergy();
    }

    @Override
    public double getHeat() {
        return 0;
    }

    @Override
    public double getCoolant() {
        return 0;
    }

    @Override
    public double getHotCoolant() {
        return 0;
    }

    public int getAnalogSignalStrength()
    {
        return container().getAnalogSignalStrength();
    }
}
