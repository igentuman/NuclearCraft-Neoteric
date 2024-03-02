package igentuman.nc.client.gui.fission;

import com.mojang.blaze3d.systems.RenderSystem;
import igentuman.nc.client.gui.IProgressScreen;
import igentuman.nc.client.gui.IVerticalBarScreen;
import igentuman.nc.client.gui.element.NCGuiElement;
import igentuman.nc.client.gui.element.bar.ProgressBar;
import igentuman.nc.client.gui.element.bar.VerticalBar;
import igentuman.nc.client.gui.element.button.Button;
import igentuman.nc.client.gui.element.fluid.FluidTankRenderer;
import igentuman.nc.container.FissionPortContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
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

public class FissionPortScreen extends AbstractContainerScreen<FissionPortContainer> implements IProgressScreen, IVerticalBarScreen {
    protected final ResourceLocation GUI = new ResourceLocation(MODID, "textures/gui/fission/port.png");
    protected int relX;
    protected int relY;
    private int xCenter;
    private Button.ReactorPortRedstoneModeButton redstoneConfigBtn;
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


    public FissionPortScreen(FissionPortContainer container, Inventory inv, Component name) {
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
        redstoneConfigBtn = new Button.ReactorPortRedstoneModeButton(150, 74, this, menu.getPosition());
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
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        xCenter = getGuiLeft()-imageWidth/2;
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    private void renderWidgets(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        redstoneConfigBtn.setMode(getMenu().getComparatorMode());
        redstoneConfigBtn.strength = getMenu().getAnalogSignalStrength();
        for(NCGuiElement widget: widgets) {
            widget.draw(graphics, mouseX, mouseY, partialTicks);
        }
        if(!getMenu().getMode()) {
            energyBar.draw(graphics, mouseX, mouseY, partialTicks);
        } else {
            coolantBar.draw(graphics, mouseX, mouseY, partialTicks);
            hotCoolantBar.draw(graphics, mouseX, mouseY, partialTicks);
            coolantTank.draw(graphics, mouseX, mouseY, partialTicks);
            steamTank.draw(graphics, mouseX, mouseY, partialTicks);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawCenteredString(font,  menu.getTitle(), imageWidth/2, titleLabelY, 0xffffff);
        renderTooltips(graphics, mouseX-relX, mouseY-relY);
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

        if(!container().getMode()) {
            energyBar.clearTooltips();
            energyBar.addTooltip(Component.translatable("reactor.forge_energy_per_tick", container().energyPerTick()));
            if(energyBar.isMouseOver(pMouseX, pMouseY)) {
                graphics.renderTooltip(font, energyBar.getTooltips(),
                        Optional.empty(), pMouseX, pMouseY);
            }
        } else {
            if(coolantTank.isMouseOver(pMouseX, pMouseY)) {
                graphics.renderTooltip(font, coolantTank.getTooltips(),
                        Optional.empty(), pMouseX, pMouseY);
            }
            if(steamTank.isMouseOver(pMouseX, pMouseY)) {
                List<Component> tooltips = steamTank.getTooltips();
                tooltips.add(Component.translatable("reactor.steam_per_tick", container().getSteamPerTick()));
                graphics.renderTooltip(font, tooltips,
                        Optional.empty(), pMouseX, pMouseY);
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
