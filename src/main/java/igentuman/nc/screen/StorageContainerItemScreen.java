package igentuman.nc.screen;

import net.minecraft.client.renderer.RenderPipelines;
import igentuman.nc.container.StorageContainerItemMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

import static igentuman.nc.NuclearCraft.rl;

public class StorageContainerItemScreen extends AbstractContainerScreen<StorageContainerItemMenu> {

    private final Identifier gui;
    private Button magnetButton;

    public StorageContainerItemScreen(StorageContainerItemMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, menu.getColumns() * 18 + 20, (menu.getRows() + 4) * 18 + 20);
        this.gui = rl("textures/gui/storage/" + menu.getTier() + ".png");
    }

    @Override
    protected void init() {
        super.init();
        magnetButton = Button.builder(magnetLabel(), b ->
                        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, StorageContainerItemMenu.MAGNET_BUTTON))
                .bounds(leftPos + imageWidth - 34, topPos + imageHeight - 83, 18, 18)
                .build();
        addRenderableWidget(magnetButton);
    }

    private boolean magnetOn() {
        return Minecraft.getInstance().player != null && menu.isMagnetEnabled(Minecraft.getInstance().player);
    }

    private Component magnetLabel() {
        return Component.literal("M").withStyle(magnetOn() ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        if (magnetButton != null) {
            magnetButton.setMessage(magnetLabel());
            magnetButton.setTooltip(Tooltip.create(Component.translatable(
                    magnetOn() ? "tooltip.nuclearcraft.magnet.on" : "tooltip.nuclearcraft.magnet.off")));
        }
        super.extractRenderState(graphics, mouseX, mouseY, partialTicks);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        super.extractBackground(graphics, mouseX, mouseY, partialTicks);
        graphics.blit(RenderPipelines.GUI_TEXTURED, gui, leftPos, topPos, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
    }
}
