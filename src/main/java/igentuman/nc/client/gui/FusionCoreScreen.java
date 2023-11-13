package igentuman.nc.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import igentuman.nc.client.gui.element.NCGuiElement;
import igentuman.nc.client.gui.element.bar.VerticalBar;
import igentuman.nc.client.gui.element.button.Checkbox;
import igentuman.nc.client.gui.element.button.SliderHorizontal;
import igentuman.nc.client.gui.element.fluid.FluidTankRenderer;
import igentuman.nc.client.gui.element.slot.VerticalLongSlot;
import igentuman.nc.container.FusionCoreContainer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.client.gui.element.fluid.FluidTankRenderer.TooltipMode.SHOW_AMOUNT_AND_CAPACITY;
import static igentuman.nc.util.TextUtils.numberFormat;

public class FusionCoreScreen extends AbstractContainerScreen<FusionCoreContainer> implements IVerticalBarScreen {
    protected final ResourceLocation GUI = new ResourceLocation(MODID, "textures/gui/fusion_core.png");
    protected int relX;
    protected int relY;
    private int xCenter;

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
    private VerticalBar casingHeatBar;

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
        checkboxIsFormed = new Checkbox(175, 89, this,  isCasingValid());
        heatBar = new VerticalBar.HeatLong(6, 5,this,  (int) container().getMaxHeat());
        energyBar = new VerticalBar.EnergyLong(16, 5,  this, container().getMaxEnergy());
        coolantBar = new VerticalBar.CoolantLong(26, 5,  this, 1000000);
        casingHeatBar = new VerticalBar.HeatLong(36, 5,  this, 1000000);
        rfAmplifierSlider = new SliderHorizontal(64, 30, 119, this, menu.getBlockPos());
        rfAmplifierSlider.slideTo(container().getRfAmplifiersPowerRatio());
        widgets.add(rfAmplifierSlider);
        widgets.add(heatBar);
        widgets.add(casingHeatBar);
        widgets.add(coolantBar);

        addSlots();
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
        checkboxIsFormed.setChecked(isCasingValid()).draw(matrix, mouseX, mouseY, partialTicks);
        if(isCasingValid()) {
            checkboxIsFormed.setTooltipKey("reactor.casing.complete");
            checkboxIsFormed.addTooltip(Component.translatable("tooltip.nc.electromagnet.magnetic_field", container().getElectromagnetsField()).withStyle(ChatFormatting.BLUE));
            checkboxIsFormed.addTooltip(Component.translatable("tooltip.nc.electromagnet.power", container().getElectromagnetsPower()).withStyle(ChatFormatting.AQUA));
            checkboxIsFormed.addTooltip(Component.translatable("tooltip.nc.electromagnet.max_temp", container().getElectromagnetsMaxTemp()).withStyle(ChatFormatting.GOLD));
            checkboxIsFormed.addTooltip(Component.translatable("tooltip.nc.rf_amplifier.voltage", container().getAmplifierVoltage()).withStyle(ChatFormatting.BLUE));
            checkboxIsFormed.addTooltip(Component.translatable("tooltip.nc.rf_amplifier.power", container().getAmplifierPower()).withStyle(ChatFormatting.AQUA));
            checkboxIsFormed.addTooltip(Component.translatable("tooltip.nc.rf_amplifier.max_temp", container().getAmplifierMaxTemp()).withStyle(ChatFormatting.GOLD));
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
        widgets.add(new FluidTankRenderer(getFluidTank(0), SHOW_AMOUNT_AND_CAPACITY,6, 48, 54, 7));
        widgets.add(new FluidTankRenderer(getFluidTank(1), SHOW_AMOUNT_AND_CAPACITY,6, 48, 54, 57));

        widgets.add(new VerticalLongSlot(191, 6));
        widgets.add(new VerticalLongSlot(191, 56));
        widgets.add(new VerticalLongSlot(201, 6));
        widgets.add(new VerticalLongSlot(201, 56));
        widgets.add(new FluidTankRenderer(getFluidTank(2), SHOW_AMOUNT_AND_CAPACITY,6, 48, 192, 7));
        widgets.add(new FluidTankRenderer(getFluidTank(3), SHOW_AMOUNT_AND_CAPACITY,6, 48, 192, 57));
        widgets.add(new FluidTankRenderer(getFluidTank(4), SHOW_AMOUNT_AND_CAPACITY,6, 48, 202, 7));
        widgets.add(new FluidTankRenderer(getFluidTank(5), SHOW_AMOUNT_AND_CAPACITY,6, 48, 202, 57));

    }

    @Override
    protected void renderLabels(PoseStack matrixStack, int mouseX, int mouseY) {
        drawCenteredString(matrixStack, font, Component.translatable("nc_jei_cat.fusion_core"), 125, 10, 0xFFFFFF);
        drawCenteredString(matrixStack, font, Component.translatable("fusion_core.rf_amplifiers.power", getRfAmplifiersPowerRatio()), 125, 20, 0xFFFFFF);
        casingTootip = Component.empty();

        if(isCasingValid()) {
            if(container().hasRecipe() && !container().getEfficiency().equals("NaN")) {
                drawString(matrixStack, font, Component.translatable("fission_reactor.efficiency", container().getEfficiency()), 46, 62, 0x8AFF8A);
                drawString(matrixStack, font, Component.translatable("fission_reactor.net_heat", container().getNetHeat()), 46, 72, 0x8AFF8A);
                drawString(matrixStack, font, Component.translatable("fission_reactor.heat_multiplier", container().getHeatMultiplier()), 46, 82, 0x8AFF8A);
            }
        }

        renderTooltips(matrixStack, mouseX-relX, mouseY-relY);
    }

    private String getRfAmplifiersPowerRatio() {
        return numberFormat(container().getRfAmplifiersPowerRatio()*100);
    }

    private Object getValidationResultData() {
        return container().getValidationResultData().toShortString();
    }

    private String getValidationResultKey() {
        return container().getValidationResultKey();
    }

    @Override
    protected void renderBg(PoseStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, GUI);
        updateRelativeCords();
        this.blit(matrixStack, relX, relY, 0, 0, this.imageWidth, this.imageHeight);
        renderWidgets(matrixStack, partialTicks, mouseX, mouseY);
      //  rfAmplifierSlider.mouseMove(mouseX, mouseY);
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

    private void renderTooltips(PoseStack pPoseStack, int pMouseX, int pMouseY) {
        heatBar.clearTooltips();
        casingHeatBar.clearTooltips();
        coolantBar.clearTooltips();
        heatBar.addTooltip(Component.translatable("reactor.cooling", container().getCooling()).withStyle(ChatFormatting.AQUA));
        heatBar.addTooltip(Component.translatable("reactor.heating", container().getHeating()).withStyle(ChatFormatting.RED));
        heatBar.addTooltip(Component.translatable("reactor.net_heat", container().getNetHeat()).withStyle(ChatFormatting.GOLD));
        for(NCGuiElement widget: widgets) {
           if(widget.isMouseOver(pMouseX, pMouseY)) {
               renderTooltip(pPoseStack, widget.getTooltips(),
                       Optional.empty(), pMouseX, pMouseY);
           }
        }
        if(checkboxIsFormed.isMouseOver(pMouseX, pMouseY)) {
            renderTooltip(pPoseStack, checkboxIsFormed.getTooltips(),
                    Optional.empty(), pMouseX, pMouseY);
        }

        energyBar.clearTooltips();
        energyBar.addTooltip(Component.translatable("reactor.forge_energy_per_tick", container().energyPerTick()));
        if(energyBar.isMouseOver(pMouseX, pMouseY)) {
            renderTooltip(pPoseStack, energyBar.getTooltips(),
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
