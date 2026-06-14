package igentuman.nc.client.gui.accelerator;

import com.mojang.blaze3d.systems.RenderSystem;
import igentuman.nc.client.gui.IProgressScreen;
import igentuman.nc.client.gui.IVerticalBarScreen;
import igentuman.nc.client.gui.element.GuiParticle;
import igentuman.nc.client.gui.element.NCGuiElement;
import igentuman.nc.client.gui.element.bar.ProgressBar;
import igentuman.nc.client.gui.element.bar.VerticalBar;
import igentuman.nc.client.gui.element.button.Button;
import igentuman.nc.client.gui.element.button.Checkbox;
import igentuman.nc.client.gui.element.fluid.FluidTankRenderer;
import igentuman.nc.container.TargetChamberControllerContainer;
import igentuman.nc.content.particles.ParticleStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.util.TextUtils.__;
import static igentuman.nc.util.TextUtils.applyFormat;

public class TargetChamberControllerScreen extends AbstractContainerScreen<TargetChamberControllerContainer> implements IProgressScreen, IVerticalBarScreen {
    protected final ResourceLocation GUI = rl("textures/gui/accelerators/target_chamber_controller.png");
    protected int relX;
    protected int relY;
    private int xCenter;

    public TargetChamberControllerContainer container()
    {
        return menu;
    }

    public List<NCGuiElement> widgets = new ArrayList<>();
    public Checkbox checkboxCasing;
    public Checkbox checkboxInterior;
    private VerticalBar energyBar;
    private Button.MultiblockAnalyze analyzeBtn;
    private Button.Link linkBtn;
    public GuiParticle guiParticle;
    public GuiParticle outputParticle1;
    public GuiParticle outputParticle2;
    public GuiParticle outputParticle3;
    public List<GuiParticle> outputParticles = new ArrayList<>();
    public Component casingTootip = Component.empty();
    public Component interiorTootip = Component.empty();

    public TargetChamberControllerScreen(TargetChamberControllerContainer container, Inventory inv, Component name) {
        super(container, inv, name);
        imageWidth = 176;
        imageHeight = 200;
        guiParticle = new GuiParticle(18, 46);
        outputParticle1 = new GuiParticle(28, 46);
        outputParticle2 = new GuiParticle(28, 46);
        outputParticle3 = new GuiParticle(28, 46);
        outputParticles.add(new GuiParticle(86, 15));
        outputParticles.add(new GuiParticle(146, 46));
        outputParticles.add(new GuiParticle(86, 78));
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
        updateRelativeCords();
        widgets.clear();
        checkboxCasing = new Checkbox(imageWidth-18, 105, this,  isCasingValid());
        checkboxInterior =  new Checkbox(imageWidth-31, 105, this,  isInteriorValid());
        energyBar = new VerticalBar.Energy(7, 16,  this, container().getMaxEnergy());
        widgets.add(new ProgressBar(71, 47, this,  8));
        analyzeBtn = new Button.MultiblockAnalyze(150, 78, this, menu.getPosition());
        linkBtn = new Button.Link(150, 14, this, menu.getPosition(), "https://ftb.fandom.com/wiki/NuclearCraft:_Neoteric#Fission_Reactor_+_Irradiator",
                List.of(__("tooltip.nc.wiki"))
        );
        addWidget(FluidTankRenderer.tank(getFluidTank(0)).id(0).size(18, 18).pos(53, 58).canVoid());
        addWidget(FluidTankRenderer.tank(getFluidTank(0)).id(0).size(18, 18).pos(113, 58).canVoid());
        widgets.add(analyzeBtn);
        widgets.add(linkBtn);

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
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        xCenter = getGuiLeft()-imageWidth/2;
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    private void renderWidgets(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        analyzeBtn.setEnabled(container().canAnalyze());
        for(NCGuiElement widget: widgets) {
            widget.draw(graphics, mouseX, mouseY, partialTicks);
        }
        checkboxCasing.setChecked(isCasingValid()).draw(graphics, mouseX, mouseY, partialTicks);
        if(isCasingValid()) {
            checkboxCasing.setTooltipKey("multiblock.casing.complete");
        } else {
            checkboxCasing.setTooltipKey("multiblock.casing.incomplete");
        }
        checkboxCasing.addTooltip(casingTootip);

        checkboxInterior.setChecked(isInteriorValid()).draw(graphics, mouseX, mouseY, partialTicks);
        if(isInteriorValid()) {
            checkboxInterior.setTooltipKey("multiblock.interior.complete");
        } else {
            checkboxInterior.setTooltipKey("multiblock.interior.incomplete");
        }
        checkboxInterior.addTooltip(interiorTootip);
        if(isInteriorValid()) {
            checkboxInterior.addTooltip(__("tooltip.particle_chamber.detectors", container().getDetectors()));
        }
        energyBar.draw(graphics, mouseX, mouseY, partialTicks);
        if(hasParticle()) {
            guiParticle.drawParticleStack(graphics, getParticleStack());
        }
        int i = 0;
        for (GuiParticle particle : outputParticles) {
            particle.drawParticleStack(graphics, container().getOutputParticle(i));
            i++;
        }
    }

    private ParticleStack getOutputParticleStack(int i) {
        return container().getOutputParticle(i);
    }

    private boolean hasParticle() {
        return container().hasParticle();
    }

    private ParticleStack getParticleStack() {
        return container().getParticleStack();
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawCenteredString(font,  menu.getTitle(), imageWidth/2, 5, 0xffffff);
        if(isCasingValid()) {
            casingTootip = applyFormat(__("tooltip.nc.structure.size", getMultiblockHeight(), getMultiblockWidth(), getMultiblockDepth()), ChatFormatting.GOLD);
        } else {
            casingTootip = applyFormat(__(getValidationResultKey(), getValidationResultData()), ChatFormatting.RED);
        }

        if(isCasingValid()) {
            if (isInteriorValid()) {
                if(container().hasRecipe() && !container().getEfficiency().equals("NaN")) {
                    int color = container().getRawEfficiency() > 0 ? 0x8AFF8A : 0xCCCCCC;
                    graphics.pose().pushPose();
                    graphics.pose().scale(0.5f, 0.5f, 0.5f);
                    graphics.drawString(font, __("fission_reactor.efficiency", container().getEfficiency()), 35*2, 82*2, color);
                    graphics.pose().popPose();
                }
            } else {
                interiorTootip = applyFormat(__(getValidationResultKey(), getValidationResultData()), ChatFormatting.RED);
            }
        }

        renderTooltips(graphics, mouseX-relX, mouseY-relY);
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
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, GUI);
        updateRelativeCords();
        graphics.blit(GUI, relX, relY, 0, 0, this.imageWidth, this.imageHeight);
        renderWidgets(graphics, partialTicks, mouseX, mouseY);
    }

    private void renderTooltips(GuiGraphics graphics, int pMouseX, int pMouseY) {
        for(NCGuiElement widget: widgets) {
           if(widget.isMouseOver(pMouseX, pMouseY)) {
               graphics.renderTooltip(font, widget.getTooltips(),
                       Optional.empty(), pMouseX, pMouseY);
           }
        }
        if(checkboxCasing.isMouseOver(pMouseX, pMouseY)) {
            graphics.renderTooltip(font, checkboxCasing.getTooltips(),
                    Optional.empty(), pMouseX, pMouseY);
        }
        if(checkboxInterior.isMouseOver(pMouseX, pMouseY)) {
            graphics.renderTooltip(font, checkboxInterior.getTooltips(),
                    Optional.empty(), pMouseX, pMouseY);
        }
        if(guiParticle.isMouseOver(pMouseX, pMouseY)) {
            if(hasParticle()) {
                guiParticle.renderTooltip(graphics, getParticleStack(), pMouseX, pMouseY);
            }
        }
        int i = 0;
        for (GuiParticle particle : outputParticles) {
            if(particle.isMouseOver(pMouseX, pMouseY)) {
                particle.renderTooltip(graphics, getOutputParticle(i), pMouseX, pMouseY);
            }
            i++;
        }
        energyBar.clearTooltips();
        energyBar.addTooltip(__("tooltip.nc.energy.per_tick", container().energyPerTick()));
        if(energyBar.isMouseOver(pMouseX, pMouseY+10)) {
            graphics.renderTooltip(font, energyBar.getTooltips(),
                    Optional.empty(), pMouseX, pMouseY);
        }
    }

    private ParticleStack getOutputParticle(int i) {
        return container().getOutputParticle(i);
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
}
