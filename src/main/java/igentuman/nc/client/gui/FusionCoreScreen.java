package igentuman.nc.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import igentuman.nc.client.gui.element.NCGuiElement;
import igentuman.nc.client.gui.element.bar.VerticalBar;
import igentuman.nc.client.gui.element.button.Button;
import igentuman.nc.client.gui.element.button.Checkbox;
import igentuman.nc.client.gui.element.button.SliderHorizontal;
import igentuman.nc.client.gui.element.fluid.FluidTankRenderer;
import igentuman.nc.client.gui.element.slot.VerticalLongSlot;
import igentuman.nc.container.FusionCoreContainer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.client.gui.element.fluid.FluidTankRenderer.TooltipMode.SHOW_AMOUNT_AND_CAPACITY;
import static igentuman.nc.util.TextUtils.*;

public class FusionCoreScreen extends AbstractContainerScreen<FusionCoreContainer> implements IVerticalBarScreen {
    protected final ResourceLocation GUI = rl("textures/gui/fusion_core.png");
    protected int relX;
    protected int relY;
    private int xCenter;
    private Checkbox checklist;
    private Button.FusionReactorRedstoneModeButton redstoneConfigBtn;
    private Button.MultiblockAnalyze analyzeBtn;

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

    public Component casingTootip = Component.empty();

    public FusionCoreScreen(FusionCoreContainer container, Inventory inv, Component name) {
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
        rfAmplifierSlider = new SliderHorizontal(64, 40, 119, this, menu.getBlockPos());
        rfAmplifierSlider.slideTo(container().getAmlificationAdjustment());
        redstoneConfigBtn = new Button.FusionReactorRedstoneModeButton(169, 83, this, menu.getBlockPos());
        analyzeBtn = new Button.MultiblockAnalyze(190, 105, this, menu.getBlockPos());
        widgets.add(new Button.ReportIssue(6, 150, this, menu.getBlockPos()));
        widgets.add(redstoneConfigBtn);
        widgets.add(analyzeBtn);
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
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        xCenter = getGuiLeft()-imageWidth/2;
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    public byte getComparatorMode() {
        return container().redstoneMode();
    }

    public byte getAnalogSignalStrength() {
        return container().analogSignal();
    }

    private void renderWidgets(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        redstoneConfigBtn.setMode(getComparatorMode());
        redstoneConfigBtn.strength = getAnalogSignalStrength();
        analyzeBtn.setEnabled(container().canAnalyze());
        for(NCGuiElement widget: widgets) {
            widget.draw(graphics, mouseX, mouseY, partialTicks);
        }
        checkboxIsFormed.setChecked(isCasingValid()).draw(graphics, mouseX, mouseY, partialTicks);
        checklist.setChecked(isReady()).draw(graphics, mouseX, mouseY, partialTicks);

        checklist.setTooltipKey("tooltip.nc.reactor.not_ready");
        if(isCasingValid()) {
            checkboxIsFormed.setTooltipKey("multiblock.casing.complete");

            if(isReady()) {
                checklist.setTooltipKey("tooltip.nc.reactor.ready");
            }

            checklist.addTooltip(__("tooltip.nc.reactor.has_magnets", container().hasMagnets() ? "Ok" : "--").withStyle(ChatFormatting.AQUA));
            checklist.addTooltip(__("tooltip.nc.reactor.has_amplifiers", container().hasAmplifiers() ? "Ok" : "--").withStyle(ChatFormatting.AQUA));
            checklist.addTooltip(__("tooltip.nc.reactor.has_coolant", container().hasCoolant() ? "Ok" : "--").withStyle(ChatFormatting.AQUA));
            checklist.addTooltip(__("tooltip.nc.reactor.has_energy", container().hasEnoughEnergy() ? "Ok" : "--").withStyle(ChatFormatting.AQUA));
            checklist.addTooltip(__("tooltip.nc.reactor.has_fuel", container().hasRecipe() ? "Ok" : "--").withStyle(ChatFormatting.AQUA));
            checklist.addTooltip(__("tooltip.nc.reactor.charge", container().getCharge() == 100 ? "Ok" : "--").withStyle(ChatFormatting.AQUA));
            checklist.addTooltip(__("tooltip.nc.reactor.running", container().isRunning() ? "Ok" : "--").withStyle(ChatFormatting.AQUA));

            if(!container().getElectromagnetsPower().equals("0")) {
                checkboxIsFormed.addTooltip(__("tooltip.nc.electromagnet.magnetic_field", container().getElectromagnetsField()).withStyle(ChatFormatting.BLUE));
                checkboxIsFormed.addTooltip(__("tooltip.nc.electromagnet.power", container().getElectromagnetsPower()).withStyle(ChatFormatting.AQUA));
                checkboxIsFormed.addTooltip(__("tooltip.nc.electromagnet.max_temp", container().getElectromagnetsMaxTemp()).withStyle(ChatFormatting.GOLD));
            } else {
                checkboxIsFormed.addTooltip(__("tooltip.nc.electromagnet.not_found").withStyle(ChatFormatting.RED));
            }
            checkboxIsFormed.addTooltip(Component.literal("----------------------"));
            if(container().hasAmplifiers()) {
                checkboxIsFormed.addTooltip(__("tooltip.nc.rf_amplifier.voltage", container().getAmplifierVoltage()).withStyle(ChatFormatting.BLUE));
                checkboxIsFormed.addTooltip(__("tooltip.nc.rf_amplifier.power", container().getAmplifierPower()).withStyle(ChatFormatting.AQUA));
                checkboxIsFormed.addTooltip(__("tooltip.nc.rf_amplifier.max_temp", container().getAmplifierMaxTemp()).withStyle(ChatFormatting.GOLD));
            } else {
                checkboxIsFormed.addTooltip(__("tooltip.nc.rf_amplifier.not_found").withStyle(ChatFormatting.RED));
            }

        } else {
            checkboxIsFormed.setTooltipKey("reactor.casing.incomplete");
        }
        checkboxIsFormed.addTooltip(casingTootip);
        energyBar.draw(graphics, mouseX, mouseY, partialTicks);
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
        widgets.add(new FluidTankRenderer(getFluidTank(3), SHOW_AMOUNT_AND_CAPACITY,6, 46, 191, 6));
        widgets.add(new FluidTankRenderer(getFluidTank(4), SHOW_AMOUNT_AND_CAPACITY,6, 46, 191, 56));
        widgets.add(new FluidTankRenderer(getFluidTank(5), SHOW_AMOUNT_AND_CAPACITY,6, 46, 201, 6));
        widgets.add(new FluidTankRenderer(getFluidTank(6), SHOW_AMOUNT_AND_CAPACITY,6, 46, 201, 56));

        widgets.add(new FluidTankRenderer(getFluidTank(2), SHOW_AMOUNT_AND_CAPACITY,6, 95, 27, 6));
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawCenteredString(font, __("nc_jei_cat.fusion_core"), 125, 10, 0xFFFFFF);
        graphics.drawCenteredString( font, __("fusion_core.rf_amplifiers.power", getAmplification()), 125, 20, 0xFFFFFF);
        graphics.drawCenteredString( font, __("fusion_core.rf_amplifiers.adjustment", getAmplificationAdjustment()), 125, 30, 0xFFFFFF);
        if(container().getCharge() < 100) {
            graphics.drawCenteredString( font, __("fusion_core.charge", container().getCharge()), 125, 50, 0xFFFFFF);
        }
        casingTootip = Component.empty();

        if(container().isRunning()) {
            graphics.drawCenteredString(font, __("fusion_core.efficiency", container().getEfficiency()), 125, 60, 0xFFFFFF);
            graphics.drawCenteredString(font, __("fusion_core.stability", container().getPlasmaStability()), 125, 50, 0xFFFFFF);
        }
        renderTooltips(graphics, mouseX-relX, mouseY-relY);
    }

    private String getAmplification() {
        return numberFormat(container().getAmplification());
    }

    private String getAmplificationAdjustment() {
        return numberFormat(container().getAmlificationAdjustment());
    }

    private Object getValidationResultData() {
        return container().getValidationResultData().toShortString();
    }

    private String getValidationResultKey() {
        return container().getValidationResultKey();
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, GUI);
        updateRelativeCords();
        graphics.blit(GUI, relX, relY, 0, 0, this.imageWidth, this.imageHeight);
        renderWidgets(graphics, partialTicks, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        super.mouseClicked(pMouseX, pMouseY, pButton);
        redstoneConfigBtn.mouseClicked(pMouseX, pMouseY, pButton);
        rfAmplifierSlider.mouseClicked(pMouseX, pMouseY, pButton);
        analyzeBtn.mouseClicked(pMouseX, pMouseY, pButton);
        return false;
    }

    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        super.mouseReleased(pMouseX, pMouseY, pButton);
        redstoneConfigBtn.mouseReleased(pMouseX, pMouseY, pButton);
        rfAmplifierSlider.mouseReleased(pMouseX, pMouseY, pButton);
        return false;
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
        rfAmplifierSlider.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
        return false;
    }

    private void renderTooltips(GuiGraphics graphics, int pMouseX, int pMouseY) {
        heatBar.clearTooltips();
        plasmaHeatBar.clearTooltips();
        coolantBar.clearTooltips();
        plasmaHeatBar.setTooltipKey("tooltip.nc.reactor.plasma_heat");
        plasmaHeatBar.addTooltip(__("tooltip.nc.reactor.plasma_optimal", scaledFormat(container().getOptimalTemp())).withStyle(ChatFormatting.GOLD));
        for(NCGuiElement widget: widgets) {
           if(widget.isMouseOver(pMouseX, pMouseY)) {
               graphics.renderTooltip(font, widget.getTooltips(),
                       Optional.empty(), pMouseX, pMouseY);
           }
        }
        if(rfAmplifierSlider.isMouseOver(pMouseX, pMouseY)) {
            graphics.renderTooltip(font,
                    List.of(
                            __("tooltip.nc.rf_amplifier.voltage", container().getAmplifierVoltage()).withStyle(ChatFormatting.AQUA),
                            __("tooltip.nc.rf_amplifier.power", container().getAmplifierPower()).withStyle(ChatFormatting.AQUA)
                    ),
                    Optional.empty(),
                    pMouseX, pMouseY);
        }
        if(checkboxIsFormed.isMouseOver(pMouseX, pMouseY)) {
            graphics.renderTooltip(font, checkboxIsFormed.getTooltips(),
                    Optional.empty(), pMouseX, pMouseY);
        }

        if(checklist.isMouseOver(pMouseX, pMouseY)) {
            graphics.renderTooltip(font, checklist.getTooltips(),
                    Optional.empty(), pMouseX, pMouseY);
        }
        energyBar.clearTooltips();
        energyBar.addTooltip(__("tooltip.nc.forge_energy_per_tick", scaledFormat(container().energyPerTick())));
        energyBar.addTooltip(__("reactor.internal_usage", scaledFormat(container().requiredEnergy())).withStyle(ChatFormatting.RED));
        if(energyBar.isMouseOver(pMouseX, pMouseY)) {
            graphics.renderTooltip(font, energyBar.getTooltips(),
                    Optional.empty(), pMouseX, pMouseY);
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
