package igentuman.nc.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.matrix.MatrixStack;
import igentuman.nc.client.gui.element.NCGuiElement;
import igentuman.nc.client.gui.element.bar.VerticalBar;
import igentuman.nc.client.gui.element.button.Checkbox;
import igentuman.nc.client.gui.element.button.SliderHorizontal;
import igentuman.nc.client.gui.element.fluid.FluidTankRenderer;
import igentuman.nc.client.gui.element.slot.VerticalLongSlot;
import igentuman.nc.container.FusionCoreContainer;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.client.gui.element.fluid.FluidTankRenderer.TooltipMode.SHOW_AMOUNT_AND_CAPACITY;
import static igentuman.nc.util.TextUtils.numberFormat;
import static igentuman.nc.util.TextUtils.scaledFormat;

public class FusionCoreScreen extends ContainerScreen<FusionCoreContainer> implements IVerticalBarScreen {
    protected final ResourceLocation GUI = new ResourceLocation(MODID, "textures/gui/fusion_core.png");
    protected int relX;
    protected int relY;
    private int xCenter;
    private Checkbox checklist;

    public FusionCoreContainer container()
    {
        return (FusionCoreContainer)menu;
    }

    public List<NCGuiElement> widgets = new ArrayList<>();
    public Checkbox checkboxIsFormed;
    private VerticalBar energyBar;
    private VerticalBar heatBar;
    private VerticalBar coolantBar;
    private VerticalBar hotCoolantBar;
    private VerticalBar.HeatLong plasmaHeatBar;

    private SliderHorizontal rfAmplifierSlider;

    public TextComponent casingTootip;

    public FusionCoreScreen(FusionCoreContainer container, PlayerInventory inv, ITextComponent name) {
        super(container, inv, name);
        imageWidth = 214;
        imageHeight = 186;
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
        checkboxIsFormed = new Checkbox(6, 104, this,  isCasingValid());
        checklist = new Checkbox(6, 122, this, isReady());
        heatBar = new VerticalBar.HeatLong(6, 5,this,  (int) container().getMaxHeat());
        energyBar = new VerticalBar.EnergyLong(16, 5,  this, container().getMaxEnergy());
        coolantBar = new VerticalBar.CoolantLong(26, 5,  this, 1000000);
        plasmaHeatBar = new VerticalBar.HeatLong(36, 5,  this, (long) (container().getOptimalTemp()*2), () -> container().getPlasmaHeat());
        rfAmplifierSlider = new SliderHorizontal(64, 30, 119, this, menu.getBlockPos());
        rfAmplifierSlider.slideTo(container().getRfAmplifiersPowerRatio());
        widgets.add(rfAmplifierSlider);
        widgets.add(heatBar);
        widgets.add(plasmaHeatBar);
        widgets.add(coolantBar);

        addSlots();
    }

    private boolean isReady() {
        return container().isReady();
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
    }

    private void renderWidgets(MatrixStack matrix, float partialTicks, int mouseX, int mouseY) {
        for(NCGuiElement widget: widgets) {
            widget.draw(matrix, mouseX, mouseY, partialTicks);
        }
        checkboxIsFormed.setChecked(isCasingValid()).draw(matrix, mouseX, mouseY, partialTicks);
        checklist.setChecked(isReady()).draw(matrix, mouseX, mouseY, partialTicks);

        checklist.setTooltipKey("tooltip.nc.reactor.not_ready");
        if(isCasingValid()) {
            checkboxIsFormed.setTooltipKey("multiblock.casing.complete");

            if(isReady()) {
                checklist.setTooltipKey("tooltip.nc.reactor.ready");
            }

            checklist.addTooltip(new TranslationTextComponent("tooltip.nc.reactor.has_magnets", container().hasMagnets() ? "Ok" : "--").withStyle(TextFormatting.AQUA));
            checklist.addTooltip(new TranslationTextComponent("tooltip.nc.reactor.has_amplifiers", container().hasAmplifiers() ? "Ok" : "--").withStyle(TextFormatting.AQUA));
            checklist.addTooltip(new TranslationTextComponent("tooltip.nc.reactor.has_coolant", container().hasCoolant() ? "Ok" : "--").withStyle(TextFormatting.AQUA));
            checklist.addTooltip(new TranslationTextComponent("tooltip.nc.reactor.has_energy", container().hasEnoughEnergy() ? "Ok" : "--").withStyle(TextFormatting.AQUA));
            checklist.addTooltip(new TranslationTextComponent("tooltip.nc.reactor.has_fuel", container().hasRecipe() ? "Ok" : "--").withStyle(TextFormatting.AQUA));
            checklist.addTooltip(new TranslationTextComponent("tooltip.nc.reactor.charge", container().getCharge() == 100 ? "Ok" : "--").withStyle(TextFormatting.AQUA));

            if(!container().getElectromagnetsPower().equals("0")) {
                checkboxIsFormed.addTooltip(new TranslationTextComponent("tooltip.nc.electromagnet.magnetic_field", container().getElectromagnetsField()).withStyle(TextFormatting.BLUE));
                checkboxIsFormed.addTooltip(new TranslationTextComponent("tooltip.nc.electromagnet.power", container().getElectromagnetsPower()).withStyle(TextFormatting.AQUA));
                checkboxIsFormed.addTooltip(new TranslationTextComponent("tooltip.nc.electromagnet.max_temp", container().getElectromagnetsMaxTemp()).withStyle(TextFormatting.GOLD));
            } else {
                checkboxIsFormed.addTooltip(new TranslationTextComponent("tooltip.nc.electromagnet.not_found").withStyle(TextFormatting.RED));
            }
            checkboxIsFormed.addTooltip(new TranslationTextComponent("----------------------"));
            if(!container().getAmplifierVoltage().equals("0")) {
                checkboxIsFormed.addTooltip(new TranslationTextComponent("tooltip.nc.rf_amplifier.voltage", container().getAmplifierVoltage()).withStyle(TextFormatting.BLUE));
                checkboxIsFormed.addTooltip(new TranslationTextComponent("tooltip.nc.rf_amplifier.power", container().getAmplifierPower()).withStyle(TextFormatting.AQUA));
                checkboxIsFormed.addTooltip(new TranslationTextComponent("tooltip.nc.rf_amplifier.max_temp", container().getAmplifierMaxTemp()).withStyle(TextFormatting.GOLD));
            } else {
                checkboxIsFormed.addTooltip(new TranslationTextComponent("tooltip.nc.rf_amplifier.not_found").withStyle(TextFormatting.RED));
            }

        } else {
            checkboxIsFormed.setTooltipKey("reactor.casing.incomplete");
        }
        checkboxIsFormed.addTooltip(casingTootip);
        energyBar.draw(matrix, mouseX, mouseY, partialTicks);
    }

    protected FluidTank getFluidTank(int i) {
        return menu.getFluidTank(i);
    }

    public void addSlots()
    {
        widgets.add(new VerticalLongSlot(53, 6));
        widgets.add(new VerticalLongSlot(53, 56));
        widgets.add(new FluidTankRenderer(getFluidTank(0), SHOW_AMOUNT_AND_CAPACITY,6, 46, 53, 6));
        widgets.add(new FluidTankRenderer(getFluidTank(1), SHOW_AMOUNT_AND_CAPACITY,6, 46, 53, 56));

        widgets.add(new VerticalLongSlot(191, 6));
        widgets.add(new VerticalLongSlot(191, 56));
        widgets.add(new VerticalLongSlot(201, 6));
        widgets.add(new VerticalLongSlot(201, 56));
        widgets.add(new FluidTankRenderer(getFluidTank(3), SHOW_AMOUNT_AND_CAPACITY,6, 46, 192, 6));
        widgets.add(new FluidTankRenderer(getFluidTank(4), SHOW_AMOUNT_AND_CAPACITY,6, 46, 192, 56));
        widgets.add(new FluidTankRenderer(getFluidTank(5), SHOW_AMOUNT_AND_CAPACITY,6, 46, 202, 6));
        widgets.add(new FluidTankRenderer(getFluidTank(6), SHOW_AMOUNT_AND_CAPACITY,6, 46, 202, 56));

        widgets.add(new FluidTankRenderer(getFluidTank(2), SHOW_AMOUNT_AND_CAPACITY,6, 95, 27, 6));
    }

    @Override
    protected void renderLabels(@NotNull MatrixStack matrixStack, int mouseX, int mouseY) {
        drawCenteredString(matrixStack, font, new TranslationTextComponent("nc_jei_cat.fusion_core"), 125, 10, 0xFFFFFF);
        drawCenteredString(matrixStack, font, new TranslationTextComponent("fusion_core.rf_amplifiers.power", getRfAmplifiersPowerRatio()), 125, 20, 0xFFFFFF);
        if(container().getCharge() < 100) {
            drawCenteredString(matrixStack, font, new TranslationTextComponent("fusion_core.charge", container().getCharge()), 125, 40, 0xFFFFFF);
        }
        casingTootip = new TranslationTextComponent("");

        if(container().isRunning()) {
            drawCenteredString(matrixStack, font, new TranslationTextComponent("fusion_core.efficiency", container().getEfficiency()), 125, 50, 0xFFFFFF);
            drawCenteredString(matrixStack, font, new TranslationTextComponent("fusion_core.stability", container().getPlasmaStability()), 125, 40, 0xFFFFFF);
        }
        renderTooltips(matrixStack, mouseX-relX, mouseY-relY);
    }

    private String getRfAmplifiersPowerRatio() {
        return numberFormat(container().getRfAmplifiersPowerRatio());
    }

    private Object getValidationResultData() {
        return container().getValidationResultData().toShortString();
    }

    private String getValidationResultKey() {
        return container().getValidationResultKey();
    }

    @Override
    protected void renderBg(@NotNull MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        Minecraft.getInstance().getTextureManager().bind(GUI);
        updateRelativeCords();
        this.blit(matrixStack, relX, relY, 0, 0, this.imageWidth, this.imageHeight);
        renderWidgets(matrixStack, partialTicks, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        super.mouseClicked(pMouseX, pMouseY, pButton);
        rfAmplifierSlider.mouseClicked(pMouseX, pMouseY, pButton);
        return false;
    }

    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        super.mouseReleased(pMouseX, pMouseY, pButton);
        rfAmplifierSlider.mouseReleased(pMouseX, pMouseY, pButton);
        return false;
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
        rfAmplifierSlider.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
        return false;
    }

    private void renderTooltips(MatrixStack pMatrixStack, int pMouseX, int pMouseY) {
        heatBar.clearTooltips();
        plasmaHeatBar.clearTooltips();
        coolantBar.clearTooltips();
        plasmaHeatBar.setTooltipKey("tooltip.nc.reactor.plasma_heat");
        plasmaHeatBar.addTooltip(new TranslationTextComponent("tooltip.nc.reactor.plasma_optimal", scaledFormat(container().getOptimalTemp())).withStyle(TextFormatting.GOLD));
        for(NCGuiElement widget: widgets) {
           if(widget.isMouseOver(pMouseX, pMouseY)) {
            /*   renderTooltip(pMatrixStack, widget.getTooltips(),
                       Optional.empty(), pMouseX, pMouseY)*/;
           }
        }
        if(rfAmplifierSlider.isMouseOver(pMouseX, pMouseY)) {
/*            renderTooltip(pMatrixStack,
                    Arrays.asList(
                            new TranslationTextComponent("tooltip.nc.rf_amplifier.voltage", container().getAmplifierVoltage()).withStyle(TextFormatting.AQUA),
                            new TranslationTextComponent("tooltip.nc.rf_amplifier.power", container().getAmplifierPower()).withStyle(TextFormatting.AQUA)
                    ),
                    pMouseX, pMouseY);*/
        }
        if(checkboxIsFormed.isMouseOver(pMouseX, pMouseY)) {
/*            renderTooltip(pMatrixStack, checkboxIsFormed.getTooltips(),
                    Optional.empty(), pMouseX, pMouseY);*/
        }

        if(checklist.isMouseOver(pMouseX, pMouseY)) {
/*            renderTooltip(pMatrixStack, checklist.getTooltips(),
                    Optional.empty(), pMouseX, pMouseY);*/
        }
        energyBar.clearTooltips();
        energyBar.addTooltip(new TranslationTextComponent("reactor.forge_energy_per_tick", scaledFormat(container().energyPerTick())));
        energyBar.addTooltip(new TranslationTextComponent("reactor.internal_usage", scaledFormat(container().requiredEnergy())).withStyle(TextFormatting.RED));
        if(energyBar.isMouseOver(pMouseX, pMouseY)) {
/*            renderTooltip(pMatrixStack, energyBar.getTooltips(),
                    Optional.empty(), pMouseX, pMouseY);*/
        }
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
