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
import igentuman.nc.container.CollisionChamberControllerContainer;
import igentuman.nc.content.particles.ParticleStack;
import net.minecraft.ChatFormatting;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.util.TextUtils.__;
import static igentuman.nc.util.TextUtils.applyFormat;

public class CollisionChamberControllerScreen extends AbstractContainerScreen<CollisionChamberControllerContainer> implements IProgressScreen, IVerticalBarScreen {
    protected final ResourceLocation GUI = rl("textures/gui/accelerators/collision_chamber_controller.png");
    protected int relX;
    protected int relY;

    public CollisionChamberControllerContainer container() {
        return menu;
    }

    public List<NCGuiElement> widgets = new ArrayList<>();
    public Checkbox checkboxCasing;
    public Checkbox checkboxInterior;
    private VerticalBar energyBar;
    private Button.MultiblockAnalyze analyzeBtn;
    public GuiParticle particleA;
    public GuiParticle particleB;
    public List<GuiParticle> outputParticles = new ArrayList<>();
    public Component casingTootip = Component.empty();
    public Component interiorTootip = Component.empty();

    public CollisionChamberControllerScreen(CollisionChamberControllerContainer container, Inventory inv, Component name) {
        super(container, inv, name);
        imageWidth = 176;
        imageHeight = 200;
        particleA = new GuiParticle(46, 45);
        particleB = new GuiParticle(114, 45);
        outputParticles.add(new GuiParticle(49, 15));
        outputParticles.add(new GuiParticle(111, 15));
        outputParticles.add(new GuiParticle(111, 76));
        outputParticles.add(new GuiParticle(49, 76));
    }

    protected void updateRelativeCords() {
        relX = (this.width - this.imageWidth) / 2;
        relY = (this.height - this.imageHeight) / 2;
        NCGuiElement.RELATIVE_X = relX;
        NCGuiElement.RELATIVE_Y = relY;
    }

    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        for (NCGuiElement widget : widgets) {
            if (widget.mouseClicked(pMouseX, pMouseY, pButton)) {
                return true;
            }
        }
        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    protected void init() {
        super.init();
        updateRelativeCords();
        widgets.clear();
        checkboxCasing = new Checkbox(imageWidth - 18, 105, this, isCasingValid());
        checkboxInterior = new Checkbox(imageWidth - 31, 105, this, isInteriorValid());
        energyBar = new VerticalBar.Energy(7, 16, this, container().getMaxEnergy());
        analyzeBtn = new Button.MultiblockAnalyze(150, 78, this, menu.getPosition());

        widgets.add(analyzeBtn);
    }

    private boolean isInteriorValid() {
        return container().isInteriorValid();
    }

    private boolean isCasingValid() {
        return container().isCasingValid();
    }

    @Override
    public void render(PoseStack graphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    private void renderWidgets(PoseStack graphics, float partialTicks, int mouseX, int mouseY) {
        analyzeBtn.setEnabled(container().canAnalyze());
        for (NCGuiElement widget : widgets) {
            widget.draw(graphics, mouseX, mouseY, partialTicks);
        }
        checkboxCasing.setChecked(isCasingValid()).draw(graphics, mouseX, mouseY, partialTicks);
        checkboxCasing.setTooltipKey(isCasingValid() ? "multiblock.casing.complete" : "multiblock.casing.incomplete");
        checkboxCasing.addTooltip(casingTootip);

        checkboxInterior.setChecked(isInteriorValid()).draw(graphics, mouseX, mouseY, partialTicks);
        checkboxInterior.setTooltipKey(isInteriorValid() ? "multiblock.interior.complete" : "multiblock.interior.incomplete");
        checkboxInterior.addTooltip(interiorTootip);
        if (isInteriorValid()) {
            checkboxInterior.addTooltip(__("tooltip.collision_chamber.connected_ports", container().getConnectedPorts()));
        }
        energyBar.draw(graphics, mouseX, mouseY, partialTicks);
        if (getParticleStackA() != null) {
            particleA.drawParticleStack(graphics, getParticleStackA());
        }
        if (getParticleStackB() != null) {
            particleB.drawParticleStack(graphics, getParticleStackB());
        }
        int i = 0;
        for (GuiParticle particle : outputParticles) {
            particle.drawParticleStack(graphics, container().getOutputParticle(i));
            i++;
        }
    }

    private ParticleStack getParticleStackA() {
        return container().getParticleStackA();
    }

    private ParticleStack getParticleStackB() {
        return container().getParticleStackB();
    }

    @Override
    protected void renderLabels(PoseStack graphics, int mouseX, int mouseY) {
        drawCenteredString(graphics, font, menu.getTitle(), imageWidth / 2, 4, 0xffffff);
        if (isCasingValid()) {
            casingTootip = applyFormat(__("tooltip.nc.structure.size", getMultiblockHeight(), getMultiblockWidth(), getMultiblockDepth()), ChatFormatting.GOLD);
        } else {
            casingTootip = applyFormat(__(getValidationResultKey(), getValidationResultData()), ChatFormatting.RED);
        }
        if (isCasingValid()) {
            if (isInteriorValid()) {
                if (!container().getEfficiency().equals("NaN")) {
                    int color = container().getRawEfficiency() > 0 ? 0x8AFF8A : 0xCCCCCC;
                    drawString(graphics, font, __("fission_reactor.efficiency", container().getEfficiency()), 8, 95, color);
                }
            } else {
                interiorTootip = applyFormat(__(getValidationResultKey(), getValidationResultData()), ChatFormatting.RED);
            }
        }
        renderTooltips(graphics, mouseX - relX, mouseY - relY);
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
    protected void renderBg(PoseStack graphics, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, GUI);
        updateRelativeCords();
        blit(graphics, relX, relY, 0, 0, this.imageWidth, this.imageHeight);
        renderWidgets(graphics, partialTicks, mouseX, mouseY);
    }

    private void renderTooltips(PoseStack graphics, int pMouseX, int pMouseY) {
        for (NCGuiElement widget : widgets) {
            if (widget.isMouseOver(pMouseX, pMouseY)) {
                renderTooltip(graphics, widget.getTooltips(), Optional.empty(), pMouseX, pMouseY);
            }
        }
        if (checkboxCasing.isMouseOver(pMouseX, pMouseY)) {
            renderTooltip(graphics, checkboxCasing.getTooltips(), Optional.empty(), pMouseX, pMouseY);
        }
        if (checkboxInterior.isMouseOver(pMouseX, pMouseY)) {
            renderTooltip(graphics, checkboxInterior.getTooltips(), Optional.empty(), pMouseX, pMouseY);
        }
        if (particleA.isMouseOver(pMouseX, pMouseY) && getParticleStackA() != null) {
            particleA.renderTooltip(graphics, getParticleStackA(), pMouseX, pMouseY);
        }
        if (particleB.isMouseOver(pMouseX, pMouseY) && getParticleStackB() != null) {
            particleB.renderTooltip(graphics, getParticleStackB(), pMouseX, pMouseY);
        }
        int i = 0;
        for (GuiParticle particle : outputParticles) {
            if (particle.isMouseOver(pMouseX, pMouseY)) {
                particle.renderTooltip(graphics, container().getOutputParticle(i), pMouseX, pMouseY);
            }
            i++;
        }
        energyBar.clearTooltips();
        energyBar.addTooltip(__("tooltip.nc.energy.per_tick", container().energyPerTick()));
        if (energyBar.isMouseOver(pMouseX, pMouseY + 10)) {
            renderTooltip(graphics, energyBar.getTooltips(), Optional.empty(), pMouseX, pMouseY);
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
}
