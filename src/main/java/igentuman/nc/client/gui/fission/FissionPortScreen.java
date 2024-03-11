package igentuman.nc.client.gui.fission;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.matrix.MatrixStack;
import igentuman.nc.client.gui.IProgressScreen;
import igentuman.nc.client.gui.IVerticalBarScreen;
import igentuman.nc.client.gui.element.NCGuiElement;
import igentuman.nc.client.gui.element.bar.ProgressBar;
import igentuman.nc.client.gui.element.bar.VerticalBar;
import igentuman.nc.client.gui.element.button.Button;
import igentuman.nc.client.gui.element.fluid.FluidTankRenderer;
import igentuman.nc.container.FissionPortContainer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.ResourceLocation;
import net.minecraft.inventory.Inventory;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.client.gui.element.fluid.FluidTankRenderer.TooltipMode.SHOW_AMOUNT_AND_CAPACITY;
import static igentuman.nc.util.TextUtils.applyFormat;

public class FissionPortScreen extends ContainerScreen<FissionPortContainer> implements IProgressScreen, IVerticalBarScreen {
    protected final ResourceLocation GUI = new ResourceLocation(MODID, "textures/gui/fission/port.png");
    protected int relX;
    protected int relY;
    private int xCenter;
    private Button.ReactorComparatorModeButton redstoneConfigBtn;
    private VerticalBar coolantBar;
    private VerticalBar hotCoolantBar;
    private FluidTankRenderer coolantTank;
    private FluidTankRenderer steamTank;

    public FissionPortContainer container()
    {
        return (FissionPortContainer)menu;
    }

    public List<NCGuiElement> widgets = new ArrayList<>();

    private VerticalBar energyBar;


    public FissionPortScreen(FissionPortContainer container, PlayerInventory inv, ITextComponent name) {
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
        coolantBar = new VerticalBar.Coolant(17, 16,  this, 1000000);
        hotCoolantBar = new VerticalBar.HotCoolant(26, 16,  this, 1000000);
        coolantTank = new FluidTankRenderer(getFluidTank(0), SHOW_AMOUNT_AND_CAPACITY,6, 73, 18, 17);
        steamTank = new FluidTankRenderer(getFluidTank(1), SHOW_AMOUNT_AND_CAPACITY,6, 73, 27, 17);
        widgets.add(redstoneConfigBtn);
    }

    protected FluidTank getFluidTank(int i) {
        return menu.getFluidTank(i);
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
        if(!getMenu().getMode()) {
            energyBar.draw(matrix, mouseX, mouseY, partialTicks);
        } else {
            coolantBar.draw(matrix, mouseX, mouseY, partialTicks);
            hotCoolantBar.draw(matrix, mouseX, mouseY, partialTicks);
            coolantTank.draw(matrix, mouseX, mouseY, partialTicks);
            steamTank.draw(matrix, mouseX, mouseY, partialTicks);
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
           if(widget.isMouseOver(pMouseX, pMouseY)) {/*
               renderTooltip(pMatrixStack, widget.getTooltips(),
                       Optional.empty(), pMouseX, pMouseY);*/
           }
        }

        if(!container().getMode()) {
            energyBar.clearTooltips();
            energyBar.addTooltip(new TranslationTextComponent("reactor.forge_energy_per_tick", container().energyPerTick()));
            if(energyBar.isMouseOver(pMouseX, pMouseY)) {
/*                renderTooltip(pMatrixStack, energyBar.getTooltips(),
                        Optional.empty(), pMouseX, pMouseY);*/
            }
        } else {
            if(coolantTank.isMouseOver(pMouseX, pMouseY)) {
/*                renderTooltip(pMatrixStack, coolantTank.getTooltips(),
                        Optional.empty(), pMouseX, pMouseY);*/
            }
            if(steamTank.isMouseOver(pMouseX, pMouseY)) {
                List<ITextComponent> tooltips = steamTank.getTooltips();
/*                tooltips.add(new TranslationTextComponent("reactor.steam_per_tick", container().getSteamPerTick()));
                renderTooltip(pMatrixStack, tooltips,
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
