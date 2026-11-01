package igentuman.nc.screen.element;

import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class FluidSlotWidget extends SlotWidget {
    private final AbstractContainerMenu menu;
    private final int tank;

    public FluidSlotWidget(int x, int y, AbstractContainerMenu menu, int tank) {
        super(x, y, 18, 18, Component.empty());
        this.menu = menu;
        this.tank = tank;
        fluid();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return isActive() && visible && mouseX >= X() && mouseX < X() + 16
                && mouseY >= Y() && mouseY < Y() + 16
                && FluidTankInteraction.click(menu, tank, button);
    }
}
