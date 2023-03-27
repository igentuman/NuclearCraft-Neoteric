package igentuman.nc.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import igentuman.nc.container.NCProcessorContainer;
import igentuman.nc.gui.element.NCGuiElement;
import igentuman.nc.gui.element.bar.EnergyBar;
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

import static igentuman.nc.NuclearCraft.MODID;

public class NCProcessorScreen<T extends NCProcessorContainer> extends AbstractContainerScreen<T> {
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
        widgets.add(energyBar);
        for(int i = 0; i < slots.slotsCount();i++) {
            if(slots.getOutputItems()+slots.getOutputFluids() == 1 && slots.getSlotType(i).contains("_out")) {
                widgets.add(new BigSlot(slots.getSlotPos(i), slots.getSlotType(i)));
            } else {
                widgets.add(new NormalSlot(slots.getSlotPos(i), slots.getSlotType(i)));
            }
        }
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

    private void renderWidgets(PoseStack matrix) {
        for(NCGuiElement widget: widgets) {
            widget.draw(matrix);
        }
    }


    @Override
    protected void renderLabels(PoseStack matrixStack, int mouseX, int mouseY) {
        drawCenteredString(matrixStack, font,  menu.getTitle(), imageWidth/2, titleLabelY, 0xffffff);
    }

    @Override
    protected void renderBg(PoseStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, GUI);
        updateRelativeCords();
        this.blit(matrixStack, relX, relY, 0, 0, this.imageWidth, this.imageHeight);
        renderWidgets(matrixStack);
    }


}
