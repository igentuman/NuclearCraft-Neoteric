package igentuman.nc.screen;

import igentuman.nc.container.StorageContainerItemMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import static igentuman.nc.NuclearCraft.rl;

public class StorageContainerItemScreen extends AbstractContainerScreen<StorageContainerItemMenu> {

    private final ResourceLocation gui;
    private Button magnetButton;

    public StorageContainerItemScreen(StorageContainerItemMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.gui = rl("textures/gui/storage/" + menu.getTier() + ".png");
        this.imageWidth = menu.getColumns() * 18 + 20;
        this.imageHeight = (menu.getRows() + 4) * 18 + 20;
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
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (magnetButton != null) {
            magnetButton.setMessage(magnetLabel());
            magnetButton.setTooltip(Tooltip.create(Component.translatable(
                    magnetOn() ? "tooltip.nuclearcraft.magnet.on" : "tooltip.nuclearcraft.magnet.off")));
        }
        super.render(graphics, mouseX, mouseY, partialTicks);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        graphics.blit(gui, leftPos, topPos, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
    }
}
