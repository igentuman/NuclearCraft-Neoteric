package igentuman.nc.client.gui.processor;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import igentuman.nc.client.gui.IProgressScreen;
import igentuman.nc.client.gui.element.NCGuiElement;
import igentuman.nc.client.gui.element.bar.EnergyBar;
import igentuman.nc.client.gui.element.bar.ProgressBar;
import igentuman.nc.client.gui.element.button.Button;
import igentuman.nc.client.gui.element.fluid.FluidTankRenderer;
import igentuman.nc.client.gui.element.slot.BigSlot;
import igentuman.nc.container.NCProcessorContainer;
import igentuman.nc.client.gui.element.slot.NormalSlot;
import igentuman.nc.content.processors.config.ProcessorSlots;
import igentuman.nc.handler.event.client.TickHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.util.TextUtils.applyFormat;
import static igentuman.nc.util.TextUtils.scaledFormat;

public class NCProcessorScreen<T extends NCProcessorContainer> extends AbstractContainerScreen<T> implements IProgressScreen {
    protected final ResourceLocation GUI = new ResourceLocation(MODID, "textures/gui/processor.png");
    protected int relX;
    protected int relY;

    protected int xCenter;

    protected ProcessorSlots slots;
    protected Button.SideConfig sideConfigBtn;
    protected Button.RedstoneConfig redstoneConfigBtn;

    public List<NCGuiElement> widgets = new ArrayList<>();
    protected EnergyBar energyBar;

    public NCProcessorScreen(T container, Inventory inv, Component name) {
        super(container, inv, name);
        imageWidth = 180;
        imageHeight = 180;
        TickHandler.currentScreenCode = menu.getProcessor().name;
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


    public void addSlots()
    {
        for(int i = 0; i < slots.slotsCount();i++) {
            if(slots.outputSlotsCount() == 1 && slots.getSlotType(i).contains("_out")) {
                widgets.add(new BigSlot(slots.getSlotPos(i), slots.getSlotType(i)));
                if(slots.getSlotType(i).contains("fluid")) {
                    widgets.add(new FluidTankRenderer(getFluidTank(slots.fluidSlotId(i)), 24, 24, slots.getSlotPos(i)[0]-4, slots.getSlotPos(i)[1]-4));
                }
            } else {
                if(!menu.getProcessor().isSlotHidden(i+slots.getInputFluids()) || slots.getSlotType(i).contains("fluid")) {
                    widgets.add(new NormalSlot(slots.getSlotPos(i), slots.getSlotType(i)));
                }
                if(slots.getSlotType(i).contains("fluid")) {
                    widgets.add(new FluidTankRenderer(getFluidTank(slots.fluidSlotId(i)), 16, 16, slots.getSlotPos(i)));
                }
            }
        }
    }

    protected void init() {
        super.init();
        slots = menu.getProcessor().getSlotsConfig();
        updateRelativeCords();
        widgets.clear();
        if(menu.getProcessor().getPower() > 0) {
            energyBar = new EnergyBar(9, 4, menu.getEnergy());
            widgets.add(energyBar);
        }
        int progressBarX = 71;
        if(slots.getOutputItems()+slots.getOutputFluids() > 5) {
            progressBarX -= ProcessorSlots.margin;
        }

        if(slots.getInputItems()+slots.getInputFluids() > 5) {
            progressBarX += ProcessorSlots.margin;
        }
        widgets.add(new ProgressBar(progressBarX, 40, this, menu.getProcessor().progressBar));
        addOtherSlots();
        sideConfigBtn = new Button.SideConfig(29, 74, this);
        widgets.add(sideConfigBtn);
        redstoneConfigBtn = new Button.RedstoneConfig(48, 74, this, menu.getPosition());
        widgets.add(redstoneConfigBtn);
        addSlots();
    }

    protected void addOtherSlots() {
        int ux = 154;

        if(menu.getProcessor().supportEnergyUpgrade) {
            widgets.add(new NormalSlot(ux, 77, "energy_upgrade"));
            ux -= 18;
        }
        if(menu.getProcessor().supportSpeedUpgrade) {
            widgets.add(new NormalSlot(ux, 77, "speed_upgrade"));
            ux -= 18;
        }

        if(menu.getProcessor().supportsCatalyst) {
            widgets.add(new NormalSlot(ux, 77, "catalyst"));
        }
    }

    protected FluidTank getFluidTank(int i) {
        return menu.getFluidTank(i);
    }

    public NCProcessorScreen(AbstractContainerMenu abstractContainerMenu, Inventory inventory, Component component) {
        this((T)abstractContainerMenu, inventory, component);
    }

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        xCenter = getGuiLeft()-imageWidth/2;
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.renderTooltip(matrixStack, mouseX, mouseY);
    }

    protected void renderWidgets(PoseStack matrix, float partialTicks, int mouseX, int mouseY) {
        redstoneConfigBtn.setMode(getMenu().getRedstoneMode());
        for(NCGuiElement widget: widgets) {
            widget.draw(matrix, mouseX, mouseY, partialTicks);
        }
    }

    @Override
    protected void renderLabels(PoseStack matrixStack, int mouseX, int mouseY) {
        drawCenteredString(matrixStack, font,  menu.getTitle(), imageWidth/2, titleLabelY, 0xffffff);
        renderTooltips(matrixStack, mouseX-relX, mouseY-relY);
    }

    @Override
    protected void renderBg(PoseStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, GUI);
        updateRelativeCords();
        this.blit(matrixStack, relX, relY, 0, 0, this.imageWidth, this.imageHeight);
        renderWidgets(matrixStack, partialTicks, mouseX, mouseY);
    }

    protected void renderTooltips(PoseStack pPoseStack, int pMouseX, int pMouseY) {
        for(NCGuiElement widget: widgets) {
            if(widget.isMouseOver(pMouseX, pMouseY)) {
                if(widget instanceof EnergyBar) {
                    widget.clearTooltips();
                    widget.addTooltip(applyFormat(Component.translatable("speed.multiplier", menu.speedMultiplier()), ChatFormatting.RED));
                    widget.addTooltip(applyFormat(Component.translatable("energy.multiplier", menu.energyMultiplier()), ChatFormatting.GOLD));
                    widget.addTooltip(applyFormat(Component.translatable("energy.per_tick", scaledFormat(menu.energyPerTick())), ChatFormatting.YELLOW));
                }
                renderTooltip(pPoseStack, widget.getTooltips(),
                        Optional.empty(), pMouseX, pMouseY);
            }
        }
    }

    @Override
    public double getProgress() {
        return menu.getProgress();
    }

    public String getRecipeTypeName() {
        return getMenu().getProcessor().name;
    }
}
