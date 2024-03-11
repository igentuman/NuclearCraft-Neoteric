package igentuman.nc.client.gui.processor;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.matrix.MatrixStack;
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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.ResourceLocation;
import net.minecraft.inventory.Inventory;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.antlr.v4.runtime.misc.NotNull;;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.util.TextUtils.applyFormat;
import static igentuman.nc.util.TextUtils.scaledFormat;

public class NCProcessorScreen extends ContainerScreen<NCProcessorContainer> implements IProgressScreen {
    protected final ResourceLocation GUI = new ResourceLocation(MODID, "textures/gui/processor.png");
    protected int relX;
    protected int relY;

    protected int xCenter;

    protected ProcessorSlots slots;
    protected Button.SideConfig sideConfigBtn;
    protected Button.RedstoneConfig redstoneConfigBtn;

    public List<NCGuiElement> widgets = new ArrayList<>();
    protected EnergyBar energyBar;
    private Button.ShowRecipes showRecipesBtn;

    public NCProcessorScreen(NCProcessorContainer container, PlayerInventory inv, ITextComponent name) {
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
                addWidget(new BigSlot(slots.getSlotPos(i), slots.getSlotType(i)));
                if(slots.getSlotType(i).contains("fluid")) {
                    addWidget(FluidTankRenderer.tank(getFluidTank(slots.fluidSlotId(i))).id(slots.fluidSlotId(i)).size(24, 24).pos(slots.getSlotPos(i)[0]-4, slots.getSlotPos(i)[1]-4).canVoid());
                }
            } else {
                if(!menu.getProcessor().isSlotHidden(i+slots.getInputFluids()) || slots.getSlotType(i).contains("fluid")) {
                    addWidget(new NormalSlot(slots.getSlotPos(i), slots.getSlotType(i)));
                }
                if(slots.getSlotType(i).contains("fluid")) {
                    addWidget(FluidTankRenderer.tank(getFluidTank(slots.fluidSlotId(i))).id(slots.fluidSlotId(i)).pos(slots.getSlotPos(i)).canVoid());
                }
            }
        }
    }

    protected void addWidget(NCGuiElement widget)
    {
        widget.setScreen(this);
        widgets.add(widget);
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
        if(slots.getOutputItems()+slots.getOutputFluids() > 6) {
            progressBarX -= ProcessorSlots.margin;
        }

        if(slots.getInputItems()+slots.getInputFluids() > 5) {
            progressBarX += ProcessorSlots.margin;
        }
        addWidget(new ProgressBar(progressBarX, 40, this, menu.getProcessor().progressBar));
        addOtherSlots();
        sideConfigBtn = new Button.SideConfig(29, 74, this);
        addWidget(sideConfigBtn);
        redstoneConfigBtn = new Button.RedstoneConfig(48, 74, this, menu.getPosition());
        addWidget(redstoneConfigBtn);
        showRecipesBtn = new Button.ShowRecipes(67, 74, this, menu.getPosition());
        addWidget(showRecipesBtn);
        addSlots();
    }

    protected void addOtherSlots() {
        int ux = 154;

        if(menu.getProcessor().supportEnergyUpgrade) {
            addWidget(new NormalSlot(ux, 77, "energy_upgrade"));
            ux -= 18;
        }
        if(menu.getProcessor().supportSpeedUpgrade) {
            addWidget(new NormalSlot(ux, 77, "speed_upgrade"));
            ux -= 18;
        }

        if(menu.getProcessor().supportsCatalyst) {
            addWidget(new NormalSlot(ux, 77, "catalyst"));
        }
    }

    protected FluidTank getFluidTank(int i) {
        return menu.getFluidTank(i);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        xCenter = getGuiLeft()-imageWidth/2;
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.renderTooltip(matrixStack, mouseX, mouseY);
    }

    protected void renderWidgets(MatrixStack matrix, float partialTicks, int mouseX, int mouseY) {
        redstoneConfigBtn.setMode(getMenu().getRedstoneMode());
        for(NCGuiElement widget: widgets) {
            widget.draw(matrix, mouseX, mouseY, partialTicks);
        }
    }

    @Override
    protected void renderLabels(@NotNull MatrixStack matrixStack, int mouseX, int mouseY) {
        drawCenteredString(matrixStack, font,  menu.getTitle(), imageWidth/2, titleLabelY, 0xffffff);
        renderTooltips(matrixStack, mouseX-relX, mouseY-relY);
    }

    @Override
    protected void renderBg(@NotNull MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        Minecraft.getInstance().getTextureManager().bind(GUI);
        updateRelativeCords();
        this.blit(matrixStack, relX, relY, 0, 0, this.imageWidth, this.imageHeight);
        renderWidgets(matrixStack, partialTicks, mouseX, mouseY);
    }

    protected void renderTooltips(MatrixStack pMatrixStack, int pMouseX, int pMouseY) {
        for(NCGuiElement widget: widgets) {
            if(widget.isMouseOver(pMouseX, pMouseY)) {
                if(widget instanceof EnergyBar) {
                    widget.clearTooltips();
                    widget.addTooltip(applyFormat(new TranslationTextComponent("speed.multiplier", menu.speedMultiplier()), TextFormatting.RED));
                    widget.addTooltip(applyFormat(new TranslationTextComponent("energy.multiplier", menu.energyMultiplier()), TextFormatting.GOLD));
                    widget.addTooltip(applyFormat(new TranslationTextComponent("energy.per_tick", scaledFormat(menu.energyPerTick())), TextFormatting.YELLOW));
                }
               /* renderTooltip(pMatrixStack, widget.getTooltips(),
                        Optional.empty(), pMouseX, pMouseY);*/
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
