package igentuman.nc.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import igentuman.nc.container.NCProcessorContainer;
import igentuman.nc.gui.element.NCGuiElement;
import igentuman.nc.gui.element.bar.EnergyBar;
import igentuman.nc.gui.element.bar.ProgressBar;
import igentuman.nc.gui.element.button.Button;
import igentuman.nc.gui.element.slot.BigSlot;
import igentuman.nc.gui.element.slot.NormalSlot;
import igentuman.nc.setup.processors.config.ProcessorSlots;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static igentuman.nc.NuclearCraft.MODID;

public class NCProcessorScreen<T extends NCProcessorContainer> extends AbstractContainerScreen<T> implements IProgressScreen {
    protected final ResourceLocation GUI = new ResourceLocation(MODID, "textures/gui/processor.png");
    protected int relX;
    protected int relY;

    private int xCenter;

    private ProcessorSlots slots;

    public List<NCGuiElement> widgets = new ArrayList<>();
    private EnergyBar energyBar;

    public NCProcessorScreen(T container, Inventory inv, Component name) {
        super(container, inv, name);
        imageWidth = 180;
        imageHeight = 180;
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
        slots = menu.getProcessor().getSlotsConfig();
        updateRelativeCords();
        energyBar = new EnergyBar(9, 4,  menu.getEnergy());
        widgets.clear();
        widgets.add(energyBar);
        for(int i = 0; i < slots.slotsCount();i++) {
            if(slots.getOutputItems()+slots.getOutputFluids() == 1 && slots.getSlotType(i).contains("_out")) {
                widgets.add(new BigSlot(slots.getSlotPos(i), slots.getSlotType(i)));
            } else {
                widgets.add(new NormalSlot(slots.getSlotPos(i), slots.getSlotType(i)));
            }
        }
        int progressBarX = 71;
        if(slots.getOutputItems()+slots.getOutputFluids() > 6) {
            progressBarX -= ProcessorSlots.margin;
        }
        widgets.add(new ProgressBar(progressBarX, 40, this));
        int ux = 154;
        if(menu.getProcessor().supportSpeedUpgrade) {
            widgets.add(new NormalSlot(ux, 77, "speed_upgrade"));
            ux -= 18;
        }
        if(menu.getProcessor().supportEnergyUpgrade) {
            widgets.add(new NormalSlot(ux, 77, "energy_upgrade"));
        }
        widgets.add(new Button.SideConfig(29, 74, menu));
        widgets.add(new Button.RedstoneConfig(48, 74, menu));
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

    private void renderWidgets(PoseStack matrix, float partialTicks, int mouseX, int mouseY) {
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

    private void renderTooltips(PoseStack pPoseStack, int pMouseX, int pMouseY) {
        for(NCGuiElement widget: widgets) {
            if(widget.isMouseOver(pMouseX, pMouseY)) {
                renderTooltip(pPoseStack, widget.getTooltips(),
                        Optional.empty(), pMouseX, pMouseY);
            }
        }
    }

    @Override
    public int getProgress() {
        return menu.getProgress();
    }
}
