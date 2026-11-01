package igentuman.nc.screen.element;

import igentuman.nc.network.PacketFluidTankClick;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.network.PacketDistributor;

/** Click region for a tank drawn directly by a custom screen. */
public final class FluidTankInteraction extends AbstractWidget {
    private final AbstractContainerMenu menu;
    private final int tank;

    public FluidTankInteraction(int x, int y, int width, int height, AbstractContainerMenu menu, int tank) {
        super(x, y, width, height, Component.empty());
        this.menu = menu;
        this.tank = tank;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return active && visible && isMouseOver(mouseX, mouseY) && click(menu, tank, button);
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {}

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {}

    public static boolean click(AbstractContainerMenu menu, int tank, int button) {
        if (button == 0 && Screen.hasShiftDown() && menu.getCarried().isEmpty()) {
            PacketDistributor.sendToServer(new PacketFluidTankClick(menu.containerId, tank, true));
            return true;
        }
        if ((button != 0 && button != 1)
                || FluidUtil.getFluidHandler(menu.getCarried().copyWithCount(1)).isEmpty()) return false;
        PacketDistributor.sendToServer(new PacketFluidTankClick(menu.containerId, tank, false));
        return true;
    }
}
