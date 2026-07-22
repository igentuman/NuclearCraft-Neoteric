package igentuman.nc.screen;

import igentuman.nc.container.EngineersEncoderContainer;
import igentuman.nc.screen.element.NcPanel;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class EngineersEncoderScreen extends AbstractContainerScreen<EngineersEncoderContainer> {

    public EngineersEncoderScreen(EngineersEncoderContainer menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 250;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        Button encodeButton = Button.builder(Component.literal("→"), b ->
                        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, EngineersEncoderContainer.ENCODE_BTN))
                .bounds(leftPos + 92, topPos + 35, 26, 18)
                .tooltip(Tooltip.create(Component.translatable("screen.nuclearcraft.crafter.encode")))
                .build();
        addRenderableWidget(encodeButton);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTicks) {
        super.render(g, mouseX, mouseY, partialTicks);
        renderTooltip(g, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTicks, int mouseX, int mouseY) {
        NcPanel.drawPanel(g, leftPos, topPos, imageWidth, imageHeight);
        for (int i = 0; i < menu.slots.size(); i++) {
            Slot s = menu.slots.get(i);
            if (i == 0) {
                NcPanel.drawOutputSlot(g, leftPos + s.x, topPos + s.y);
            } else {
                NcPanel.drawSlot(g, leftPos + s.x, topPos + s.y);
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(font, title, 8, 6, 0x404040, false);
    }
}
