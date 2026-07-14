package igentuman.nc.screen.element;

import igentuman.nc.handler.SlotModePair;
import igentuman.nc.network.PacketSideConfigToggle;
import igentuman.nc.screen.SideConfigScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

/** Clickable per-face square in the side-config screen; cycles a slot's I/O mode for one direction on click. */
public class SideConfigButton extends AbstractWidget {

    private final int slotId;
    private final int direction;
    private final SideConfigScreen screen;

    public SideConfigButton(int x, int y, int slotId, int direction,
                            SideConfigScreen screen) {
        super(x, y, 16, 16, Component.empty());
        this.slotId = slotId;
        this.direction = direction;
        this.screen = screen;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        SlotModePair.SlotMode mode = screen.getSlotMode(direction, slotId);
        guiGraphics.fill(getX(), getY(), getX() + 16, getY() + 16, mode.getColor());

        if (isHovered()) {
            String dirName = igentuman.nc.handler.SidedContentHandler.RelativeDirection
                    .getDirectionName(direction);
            guiGraphics.renderTooltip(
                    net.minecraft.client.Minecraft.getInstance().font,
                    List.of(
                            Component.literal(dirName),
                            Component.literal(mode.name())
                    ),
                    java.util.Optional.empty(),
                    mouseX,
                    mouseY
            );
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isActive() || !visible) return false;
        if (mouseX >= getX() && mouseX < getX() + 16 && mouseY >= getY() && mouseY < getY() + 16) {
            PacketDistributor.sendToServer(new PacketSideConfigToggle(
                    screen.getMenu().getPosition(), slotId, direction));
            return true;
        }
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    }
}
