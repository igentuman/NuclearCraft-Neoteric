package igentuman.nc.screen.element;

import igentuman.nc.network.PacketFluidTankClick;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.transfer.access.ItemAccess;

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
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.button();
        return active && visible && isMouseOver(mouseX, mouseY) && click(menu, tank, button);
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {}

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {}

    public static boolean click(AbstractContainerMenu menu, int tank, int button) {
        if (button != 0 && button != 1) return false;

        var carried = menu.getCarried();
        boolean flush = carried.isEmpty();
        if (flush) {
            if (button != 0 || !Minecraft.getInstance().hasShiftDown()) return false;
        } else if (ItemAccess.forStack(carried.copyWithCount(1)).getCapability(Capabilities.Fluid.ITEM) == null) {
            return false;
        }
        ClientPacketDistributor.sendToServer(new PacketFluidTankClick(menu.containerId, tank, flush));
        return true;
    }
}
