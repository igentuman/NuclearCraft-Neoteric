package igentuman.nc.client.gui.fission;

import com.mojang.blaze3d.systems.RenderSystem;
import igentuman.nc.client.gui.IVerticalBarScreen;
import igentuman.nc.client.gui.element.bar.ProgressBar;
import igentuman.nc.client.gui.element.bar.VerticalBar;
import igentuman.nc.client.gui.element.button.Button;
import igentuman.nc.client.gui.element.fluid.FluidTankRenderer;
import igentuman.nc.container.FissionControllerContainer;
import igentuman.nc.client.gui.IProgressScreen;
import igentuman.nc.client.gui.element.NCGuiElement;
import igentuman.nc.client.gui.element.button.Checkbox;
import net.minecraft.ChatFormatting;
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

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.client.gui.element.fluid.FluidTankRenderer.TooltipMode.SHOW_AMOUNT_AND_CAPACITY;
import static igentuman.nc.util.TextUtils.__;
import static igentuman.nc.util.TextUtils.applyFormat;

public class FissionControllerScreen extends AbstractContainerScreen<FissionControllerContainer> implements IProgressScreen, IVerticalBarScreen {
    protected final ResourceLocation GUI = rl("textures/gui/fission/controller.png");
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

    public Component casingTootip = Component.empty();
    public Component interiorTootip = Component.empty();

    public FissionControllerScreen(FissionControllerContainer container, Inventory inv, Component name) {
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
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        xCenter = getGuiLeft()-imageWidth/2;
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(graphics, mouseX, mouseY);
        graphics.renderItem(container().getInputStack(), relX+82, relY+20);
    }

    private void renderWidgets(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        if (modeBtn == null) {
            init();
        }
        modeBtn.setMode(getMenu().isBoilingMode());
        modeBtn.setTimer(getMenu().getModeTimer());
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
            checkboxInterior.addTooltip(__("reactor.heat_sinks_count", container().getHeatSinksCount()));
            checkboxInterior.addTooltip(__("reactor.moderators_count", container().getModeratorsCount()));
            checkboxInterior.addTooltip(__("reactor.moderation_level", container().getModerationLevel()));
            checkboxInterior.addTooltip(__("reactor.reactivity", container().getReactivity()));
            checkboxInterior.addTooltip(__("reactor.irradiators_connections", container().getIrradiatorsConnections()));
        }
        if(!getMenu().isBoilingMode()) {
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
        if(isCasingValid()) {
            casingTootip = applyFormat(__("reactor.size", getMultiblockHeight(), getMultiblockWidth(), getMultiblockDepth()), ChatFormatting.GOLD);
        } else {
            casingTootip = applyFormat(__(getValidationResultKey(), getValidationResultData()), ChatFormatting.RED);
        }

        if(isCasingValid()) {
            if (isInteriorValid()) {
                interiorTootip = applyFormat(__("reactor.fuel_cells", getFuelCellsCount()), ChatFormatting.GOLD);

                if(container().hasRecipe() && !container().getEfficiency().equals("NaN")) {
                    int color = container().getRawEfficiency() > 0 ? 0x8AFF8A : 0xCCCCCC;
                    graphics.pose().pushPose();
                    graphics.pose().scale(0.5f, 0.5f, 0.5f);
                    graphics.drawString(font, __("fission_reactor.efficiency", container().getEfficiency()), 35*2, 82*2, color);
                    graphics.drawString(font, __("fission_reactor.net_heat", container().getNetHeat()), 35*2, 72*2, color);
                    graphics.drawString(font, __("fission_reactor.heat_multiplier", container().getHeatMultiplier()), 35*2, 62*2, color);
                    graphics.pose().popPose();
                }
            } else {
                interiorTootip = applyFormat(__(getValidationResultKey(), getValidationResultData()), ChatFormatting.RED);
            }
        }

        renderTooltips(graphics, mouseX-relX, mouseY-relY);
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
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, GUI);
        updateRelativeCords();
        graphics.blit(GUI, relX, relY, 0, 0, this.imageWidth, this.imageHeight);
        renderWidgets(graphics, partialTicks, mouseX, mouseY);
    }

    private void renderTooltips(GuiGraphics graphics, int pMouseX, int pMouseY) {
        heatBar.clearTooltips();
        heatBar.addTooltip(__("reactor.cooling", container().getCooling()).withStyle(ChatFormatting.AQUA));
        heatBar.addTooltip(__("reactor.heating", container().getHeating()).withStyle(ChatFormatting.RED));
        heatBar.addTooltip(__("reactor.net_heat", container().getNetHeat()).withStyle(ChatFormatting.GOLD));
        if(container().isBoilingMode()) {
            heatBar.addTooltip(__("reactor.boiling_penalty", container().getBoilingPenalty()).withStyle(ChatFormatting.YELLOW));
        }
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
        if(!container().isBoilingMode()) {
            energyBar.clearTooltips();
            energyBar.addTooltip(__("reactor.forge_energy_per_tick", container().energyPerTick()));
            if(energyBar.isMouseOver(pMouseX, pMouseY+10)) {
                graphics.renderTooltip(font, energyBar.getTooltips(),
                        Optional.empty(), pMouseX, pMouseY);
            }
        } else {
            if(coolantTank.isMouseOver(pMouseX, pMouseY+10)) {
                graphics.renderTooltip(font, coolantTank.getTooltips(),
                        Optional.empty(), pMouseX, pMouseY);
            }
            if(steamTank.isMouseOver(pMouseX, pMouseY+10)) {
                List<Component> tooltips = steamTank.getTooltips();
                tooltips.add(__("reactor.steam_per_tick", container().getSteamPerTick()));
                tooltips.add(__("reactor.max_boiling_rate", container().getMaxBoilingRate()));
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
