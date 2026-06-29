package igentuman.nc.screen;

import igentuman.nc.container.MultiblockControllerContainer;
import igentuman.nc.screen.element.EnergyBar;
import igentuman.nc.screen.element.HeatBar;
import igentuman.nc.screen.element.ProgressBar;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * Fission reactor controller screen. Reuses the generic multiblock controller container; adds a
 * readout of the reactor's synced stats (heat, FE/t, reactivity, cooling).
 */
public class FissionReactorScreen extends MultiblockControllerScreen {

    public FissionReactorScreen(MultiblockControllerContainer menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);
        if (!menu.isFormed()) return;

        int x = 38;
        int y = 18;
        line(guiGraphics, x, y + 20, "screen.nuclearcraft.fission.reactivity", synced("reactivity") + "%");
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(new HeatBar(leftPos + 18, topPos + 10, 8, 70, () -> menu.getBlockEntity().heatBuffer()));
    }

    private void line(GuiGraphics g, int x, int y, String key, String value) {
        g.drawString(font, Component.translatable(key).append(": " + value).withStyle(ChatFormatting.DARK_GRAY),
                x, y, 0x404040, false);
    }

    private int synced(String field) {
        int idx = menu.getBlockEntity().getSyncFieldIndex(field);
        return idx >= 0 ? menu.getSyncedValue(idx) : 0;
    }
}
