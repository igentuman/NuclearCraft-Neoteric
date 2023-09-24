package igentuman.nc.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import igentuman.nc.client.gui.element.NCGuiElement;
import igentuman.nc.client.gui.element.bar.VerticalBar;
import igentuman.nc.client.gui.element.button.Checkbox;
import igentuman.nc.container.FusionCoreContainer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.util.TextUtils.applyFormat;

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

    public Component casingTootip = Component.empty();

    public FusionCoreScreen(FusionCoreContainer container, Inventory inv, Component name) {
        super(container, inv, name);
        imageWidth = 195;
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
        checkboxIsFormed = new Checkbox(imageWidth-19, 80, this,  isCasingValid());
        energyBar = new VerticalBar.Energy(17, 16,  this, container().getMaxEnergy());
        heatBar = new VerticalBar.Heat(8, 16,this,  (int) container().getMaxHeat());
        coolantBar = new VerticalBar.Coolant(17, 16,  this, 1000000);
        hotCoolantBar = new VerticalBar.HotCoolant(26, 16,  this, 1000000);
        widgets.add(heatBar);
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

    @Override
    protected void renderLabels(PoseStack matrixStack, int mouseX, int mouseY) {
        casingTootip = applyFormat(Component.translatable(getValidationResultKey(), getValidationResultData()), ChatFormatting.RED);

        if(isCasingValid()) {
            if(container().hasRecipe() && !container().getEfficiency().equals("NaN")) {
                drawString(matrixStack, font, Component.translatable("fission_reactor.efficiency", container().getEfficiency()), 46, 62, 0x8AFF8A);
                drawString(matrixStack, font, Component.translatable("fission_reactor.net_heat", container().getNetHeat()), 46, 72, 0x8AFF8A);
                drawString(matrixStack, font, Component.translatable("fission_reactor.heat_multiplier", container().getHeatMultiplier()), 46, 82, 0x8AFF8A);
            }
        }

        renderTooltips(matrixStack, mouseX-relX, mouseY-relY);
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
    }

    private void renderTooltips(PoseStack pPoseStack, int pMouseX, int pMouseY) {
        heatBar.clearTooltips();
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

        if(container().getMaxEnergy() > 0) {
            energyBar.clearTooltips();
            energyBar.addTooltip(Component.translatable("reactor.forge_energy_per_tick", container().energyPerTick()));
            if(energyBar.isMouseOver(pMouseX, pMouseY)) {
                renderTooltip(pPoseStack, energyBar.getTooltips(),
                        Optional.empty(), pMouseX, pMouseY);
            }
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
