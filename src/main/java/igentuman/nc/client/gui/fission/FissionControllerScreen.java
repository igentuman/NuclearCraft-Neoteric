package igentuman.nc.client.gui.fission;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.sun.java.accessibility.util.java.awt.TextComponentTranslator;
import igentuman.nc.client.gui.IVerticalBarScreen;
import igentuman.nc.client.gui.element.bar.ProgressBar;
import igentuman.nc.client.gui.element.bar.VerticalBar;
import igentuman.nc.client.gui.element.button.Button;
import igentuman.nc.client.gui.element.fluid.FluidTankRenderer;
import igentuman.nc.container.FissionControllerContainer;
import igentuman.nc.client.gui.IProgressScreen;
import igentuman.nc.client.gui.element.NCGuiElement;
import igentuman.nc.client.gui.element.button.Checkbox;
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
import org.antlr.v4.runtime.misc.NotNull;;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.client.gui.element.fluid.FluidTankRenderer.TooltipMode.SHOW_AMOUNT_AND_CAPACITY;
import static igentuman.nc.util.TextUtils.applyFormat;

public class FissionControllerScreen extends ContainerScreen<FissionControllerContainer> implements IProgressScreen, IVerticalBarScreen {
    protected final ResourceLocation GUI = new ResourceLocation(MODID, "textures/gui/fission/controller.png");
    protected int relX;
    protected int relY;
    private int xCenter;
    private FluidTankRenderer coolantTank;
    private FluidTankRenderer steamTank;

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
    private Button.ReactorMode modeBtn;

    public ITextComponent casingTootip = ITextComponent.nullToEmpty("");
    public ITextComponent interiorTootip = ITextComponent.nullToEmpty("");

    public FissionControllerScreen(FissionControllerContainer container, PlayerInventory inv, ITextComponent name) {
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

    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        for(NCGuiElement widget : widgets) {
            if(widget.mouseClicked(pMouseX, pMouseY, pButton)) {
                return true;
            }
        }
        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    protected void init() {
        super.init();
        Minecraft mc = Minecraft.getInstance();
        updateRelativeCords();
        widgets.clear();
        checkboxCasing = new Checkbox(imageWidth-19, 80, this,  isCasingValid());
        checkboxInterior =  new Checkbox(imageWidth-32, 80, this,  isInteriorValid());
        energyBar = new VerticalBar.Energy(17, 16,  this, container().getMaxEnergy());
        heatBar = new VerticalBar.Heat(8, 16,this,  (int) container().getMaxHeat());
        coolantBar = new VerticalBar.Coolant(17, 16,  this, 1000000);
        hotCoolantBar = new VerticalBar.HotCoolant(26, 16,  this, 1000000);
        coolantTank = new FluidTankRenderer(getFluidTank(0), SHOW_AMOUNT_AND_CAPACITY,6, 73, 18, 17);
        steamTank = new FluidTankRenderer(getFluidTank(1), SHOW_AMOUNT_AND_CAPACITY,6, 73, 27, 17);
        widgets.add(heatBar);
        widgets.add(new ProgressBar(74, 35, this,  7));
        modeBtn = new Button.ReactorMode(150, 54, this, menu.getPosition());
        widgets.add(modeBtn);
    }

    protected FluidTank getFluidTank(int i) {
        return menu.getFluidTank(i);
    }

    private boolean isInteriorValid() {
        return  container().isInteriorValid();
    }

    private boolean isCasingValid() {
        return  container().isCasingValid();
    }

    @Override
    public void render(@NotNull MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        xCenter = getGuiLeft()-imageWidth/2;
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.renderTooltip(matrixStack, mouseX, mouseY);
        itemRenderer.renderAndDecorateItem(container().getInputStack(), relX+82, relY+20);
    }

    private void renderWidgets(MatrixStack matrix, float partialTicks, int mouseX, int mouseY) {
        modeBtn.setMode(getMenu().getMode());
        modeBtn.setTimer(getMenu().getModeTimer());
        for(NCGuiElement widget: widgets) {
            widget.draw(matrix, mouseX, mouseY, partialTicks);
        }
        checkboxCasing.setChecked(isCasingValid()).draw(matrix, mouseX, mouseY, partialTicks);
        if(isCasingValid()) {
            checkboxCasing.setTooltipKey("multiblock.casing.complete");
        } else {
            checkboxCasing.setTooltipKey("reactor.casing.incomplete");
        }
        checkboxCasing.addTooltip(casingTootip);

        checkboxInterior.setChecked(isInteriorValid()).draw(matrix, mouseX, mouseY, partialTicks);
        if(isInteriorValid()) {
            checkboxInterior.setTooltipKey("reactor.interior.complete");
        } else {
            checkboxInterior.setTooltipKey("reactor.interior.incomplete");
        }
        checkboxInterior.addTooltip(interiorTootip);
        if(isInteriorValid()) {
            checkboxInterior.addTooltip(new TranslationTextComponent("reactor.heat_sinks_count", container().getHeatSinksCount()));
            checkboxInterior.addTooltip(new TranslationTextComponent("reactor.moderators_count", container().getModeratorsCount()));
            checkboxInterior.addTooltip(new TranslationTextComponent("reactor.irradiators_connections", container().getIrradiatorsConnections()));
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
    protected void renderLabels(@NotNull MatrixStack matrixStack, int mouseX, int mouseY) {
        drawCenteredString(matrixStack, font,  menu.getTitle(), imageWidth/2, titleLabelY, 0xffffff);
        if(isCasingValid()) {
            casingTootip = applyFormat(new TranslationTextComponent("reactor.size", getMultiblockHeight(), getMultiblockWidth(), getMultiblockDepth()), TextFormatting.GOLD);
        } else {
            casingTootip = applyFormat(new TranslationTextComponent(getValidationResultKey(), getValidationResultData()), TextFormatting.RED);
        }

        if(isCasingValid()) {
            if (isInteriorValid()) {
                interiorTootip = applyFormat(new TranslationTextComponent("reactor.fuel_cells", getFuelCellsCount()), TextFormatting.GOLD);

                if(container().hasRecipe() && !container().getEfficiency().equals("NaN")) {
                    drawString(matrixStack, font, new TranslationTextComponent("fission_reactor.efficiency", container().getEfficiency()), 35, 82, 0x8AFF8A);
                    drawString(matrixStack, font, new TranslationTextComponent("fission_reactor.net_heat", container().getNetHeat()), 35, 72, 0x8AFF8A);
                    drawString(matrixStack, font, new TranslationTextComponent("fission_reactor.heat_multiplier", container().getHeatMultiplier()), 35, 62, 0x8AFF8A);
                }
            } else {
                interiorTootip = applyFormat(new TranslationTextComponent(getValidationResultKey(), getValidationResultData()), TextFormatting.RED);
            }
        }

        renderTooltips(matrixStack, mouseX-relX, mouseY-relY);
    }

    private int getFuelCellsCount() {
        return container().getFuelCellsCount();
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
    protected void renderBg(@NotNull MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        Minecraft.getInstance().getTextureManager().bind(GUI);
        updateRelativeCords();
        this.blit(matrixStack, relX, relY, 0, 0, this.imageWidth, this.imageHeight);
        renderWidgets(matrixStack, partialTicks, mouseX, mouseY);
    }

    private void renderTooltips(MatrixStack pMatrixStack, int pMouseX, int pMouseY) {
        heatBar.clearTooltips();
        heatBar.addTooltip(new TranslationTextComponent("reactor.cooling", container().getCooling()).withStyle(TextFormatting.AQUA));
        heatBar.addTooltip(new TranslationTextComponent("reactor.heating", container().getHeating()).withStyle(TextFormatting.RED));
        heatBar.addTooltip(new TranslationTextComponent("reactor.net_heat", container().getNetHeat()).withStyle(TextFormatting.GOLD));
        for(NCGuiElement widget: widgets) {
           if(widget.isMouseOver(pMouseX, pMouseY)) {
               renderComponentTooltip(pMatrixStack, widget.getTooltips(),
                        pMouseX, pMouseY);
           }
        }
        if(checkboxCasing.isMouseOver(pMouseX, pMouseY)) {
            renderComponentTooltip(pMatrixStack, checkboxCasing.getTooltips(),
                     pMouseX, pMouseY);
        }
        if(checkboxInterior.isMouseOver(pMouseX, pMouseY)) {
            renderComponentTooltip(pMatrixStack, checkboxInterior.getTooltips(),
                     pMouseX, pMouseY);
        }
        if(!container().getMode()) {
            energyBar.clearTooltips();
            energyBar.addTooltip(new TranslationTextComponent("reactor.forge_energy_per_tick", container().energyPerTick()));
            if(energyBar.isMouseOver(pMouseX, pMouseY)) {
                renderComponentTooltip(pMatrixStack, energyBar.getTooltips(),
                         pMouseX, pMouseY);
            }
        } else {
            if(coolantTank.isMouseOver(pMouseX, pMouseY)) {
                renderComponentTooltip(pMatrixStack, coolantTank.getTooltips(),
                        pMouseX, pMouseY);
            }
            if(steamTank.isMouseOver(pMouseX, pMouseY)) {
                List<ITextComponent> tooltips = steamTank.getTooltips();
                tooltips.add(new TranslationTextComponent("reactor.steam_per_tick", container().getSteamPerTick()));
               renderComponentTooltip(pMatrixStack, tooltips,
                        pMouseX, pMouseY);
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
        return container().getHeat();
    }

    @Override
    public double getCoolant() {
        return 0;
    }

    @Override
    public double getHotCoolant() {
        return 0;
    }
}
