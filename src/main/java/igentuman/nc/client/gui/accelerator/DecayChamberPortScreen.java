package igentuman.nc.client.gui.accelerator;

import com.mojang.blaze3d.systems.RenderSystem;
import igentuman.nc.client.gui.IProgressScreen;
import igentuman.nc.client.gui.IVerticalBarScreen;
import igentuman.nc.client.gui.element.GuiParticle;
import igentuman.nc.client.gui.element.NCGuiElement;
import igentuman.nc.client.gui.element.bar.ProgressBar;
import igentuman.nc.client.gui.element.bar.VerticalBar;
import igentuman.nc.container.DecayChamberPortContainer;
import igentuman.nc.content.particles.ParticleStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.util.TextUtils.__;

public class DecayChamberPortScreen extends AbstractContainerScreen<DecayChamberPortContainer> implements IProgressScreen, IVerticalBarScreen {
    protected final ResourceLocation GUI = rl("textures/gui/accelerators/decay_chamber_controller.png");
    protected int relX;
    protected int relY;

    public DecayChamberPortContainer container() {
        return menu;
    }

    public List<NCGuiElement> widgets = new ArrayList<>();
    private VerticalBar energyBar;
    public GuiParticle guiParticle;
    public List<GuiParticle> outputParticles = new ArrayList<>();

    public DecayChamberPortScreen(DecayChamberPortContainer container, Inventory inv, Component name) {
        super(container, inv, name);
        imageWidth = 176;
        imageHeight = 200;
        guiParticle = new GuiParticle(18, 46);
        outputParticles.add(new GuiParticle(86, 15));
        outputParticles.add(new GuiParticle(146, 46));
        outputParticles.add(new GuiParticle(86, 78));
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
        energyBar = new VerticalBar.Energy(7, 16, this, container().getMaxEnergy());
        widgets.add(new ProgressBar(71, 47, this, 8));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    private void renderWidgets(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        for (NCGuiElement widget : widgets) {
            widget.draw(graphics, mouseX, mouseY, partialTicks);
        }
        energyBar.draw(graphics, mouseX, mouseY, partialTicks);
        if (hasParticle()) {
            guiParticle.drawParticleStack(graphics, getParticleStack());
        }
        int i = 0;
        for (GuiParticle particle : outputParticles) {
            particle.drawParticleStack(graphics, container().getOutputParticle(i));
            i++;
        }
    }

    private boolean hasParticle() {
        return container().hasParticle();
    }

    private ParticleStack getParticleStack() {
        return container().getParticleStack();
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawCenteredString(font, menu.getTitle(), imageWidth / 2, 5, 0xffffff);
        renderTooltips(graphics, mouseX - relX, mouseY - relY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, GUI);
        updateRelativeCords();
        graphics.blit(GUI, relX, relY, 0, 0, this.imageWidth, this.imageHeight);
        renderWidgets(graphics, partialTicks, mouseX, mouseY);
    }

    private void renderTooltips(GuiGraphics graphics, int pMouseX, int pMouseY) {
        for (NCGuiElement widget : widgets) {
            if (widget.isMouseOver(pMouseX, pMouseY)) {
                graphics.renderTooltip(font, widget.getTooltips(), Optional.empty(), pMouseX, pMouseY);
            }
        }
        if (guiParticle.isMouseOver(pMouseX, pMouseY) && hasParticle()) {
            guiParticle.renderTooltip(graphics, getParticleStack(), pMouseX, pMouseY);
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
            graphics.renderTooltip(font, energyBar.getTooltips(), Optional.empty(), pMouseX, pMouseY);
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
