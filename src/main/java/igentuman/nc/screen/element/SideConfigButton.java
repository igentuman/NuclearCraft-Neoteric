package igentuman.nc.screen.element;

import net.minecraft.client.renderer.RenderPipelines;

import net.minecraft.client.input.MouseButtonEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import igentuman.nc.client.model.SideConfigFaces;
import igentuman.nc.handler.SidedContentHandler;
import igentuman.nc.handler.SlotModePair;
import igentuman.nc.network.PacketSideConfigToggle;
import igentuman.nc.screen.SideConfigScreen;
import igentuman.nc.setup.ModEntries;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.Optional;

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
    protected void extractWidgetRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        SlotModePair.SlotMode mode = screen.getSlotMode(direction, slotId);
        var entry = ModEntries.get(screen.getMenu().getBlockEntity().name);
        var face = SideConfigFaces.getFace(entry, SidedContentHandler.RelativeDirection.values()[direction]);
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, face, getX(), getY(), getWidth(), getHeight());
        guiGraphics.fill(getX(), getY(), getX() + 16, getY() + 16, mode.getColor());

        if (isHovered()) {
            String dirName = SidedContentHandler.RelativeDirection
                    .getDirectionName(direction);
            guiGraphics.setTooltipForNextFrame(
                    Minecraft.getInstance().font,
                    List.of(
                            Component.literal(dirName),
                            Component.literal(mode.name())
                    ),
                    Optional.empty(),
                    mouseX,
                    mouseY
            );
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.button();
        if (!isActive() || !visible) return false;
        if (mouseX >= getX() && mouseX < getX() + 16 && mouseY >= getY() && mouseY < getY() + 16) {
            ClientPacketDistributor.sendToServer(new PacketSideConfigToggle(
                    screen.getMenu().getPosition(), slotId, direction));
            return true;
        }
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    }
}
