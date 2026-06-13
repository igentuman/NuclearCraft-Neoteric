package igentuman.nc.client.gui.accelerator;

import com.mojang.blaze3d.systems.RenderSystem;
import igentuman.nc.client.gui.IProgressScreen;
import igentuman.nc.client.gui.IVerticalBarScreen;
import igentuman.nc.client.gui.element.GuiParticle;
import igentuman.nc.client.gui.element.NCGuiElement;
import igentuman.nc.client.gui.element.bar.ProgressBar;
import igentuman.nc.client.gui.element.bar.VerticalBar;
import igentuman.nc.container.CollisionChamberPortContainer;
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

public class CollisionChamberPortScreen extends AbstractContainerScreen<CollisionChamberPortContainer> implements IProgressScreen, IVerticalBarScreen {
    protected final ResourceLocation GUI = rl("textures/gui/accelerators/collision_chamber_controller.png");
    protected int relX;
    protected int relY;

    public CollisionChamberPortContainer container() {
        return menu;
    }

    public List<NCGuiElement> widgets = new ArrayList<>();
    private VerticalBar energyBar;
    public GuiParticle particleA;
    public GuiParticle particleB;
    public List<GuiParticle> outputParticles = new ArrayList<>();

    public CollisionChamberPortScreen(CollisionChamberPortContainer container, Inventory inv, Component name) {
        super(container, inv, name);
        imageWidth = 176;
        imageHeight = 200;
        particleA = new GuiParticle(28, 30);
        particleB = new GuiParticle(28, 62);
        outputParticles.add(new GuiParticle(110, 15));
        outputParticles.add(new GuiParticle(146, 46));
        outputParticles.add(new GuiParticle(110, 78));
        outputParticles.add(new GuiParticle(74, 46));
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
        widgets.add(new ProgressBar(54, 47, this, 8));
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
        return container().getParticleStack();
    }

    private ParticleStack getParticleStackB() {
        return container().getSecondaryParticleStack();
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
