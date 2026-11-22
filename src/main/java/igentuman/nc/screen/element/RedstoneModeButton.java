package igentuman.nc.screen.element;

import net.minecraft.client.input.MouseButtonEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import igentuman.nc.container.MultiblockPortContainer;
import igentuman.nc.network.PacketRedstoneModeCycle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import java.awt.Color;

/** Button that cycles a multiblock port's redstone control mode, coloring itself by the selected mode. */
public class RedstoneModeButton extends AbstractWidget {

    private static final int SIZE = 16;
    private final MultiblockPortContainer menu;

    public RedstoneModeButton(int x, int y, MultiblockPortContainer menu) {
        super(x, y, SIZE, SIZE, Component.empty());
        this.menu = menu;
    }

    private String[] modes() {
        return menu.getBlockEntity().redstoneModes();
    }

    private int currentMode(int count) {
        return count > 0 ? Math.floorMod(menu.getRedstoneMode(), count) : 0;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        String[] modes = modes();
        if (modes.length == 0) return;

        int mode = currentMode(modes.length);
        int x = getX();
        int y = getY();

        graphics.fill(x, y, x + SIZE, y + SIZE, 0xFF444444);
        graphics.fill(x + 1, y + 1, x + SIZE - 1, y + SIZE - 1, colorForMode(mode, modes.length));

        Font font = Minecraft.getInstance().font;
        String glyph = String.valueOf(mode);
        graphics.text(font, glyph, x + (SIZE - font.width(glyph)) / 2, y + 4, 0xFFFFFFFF, true);

        if (isHovered()) {
            Component modeName = Component.translatable("message.nuclearcraft.redstone_mode." + modes[mode]);
            graphics.setTooltipForNextFrame(font,
                    Component.translatable("message.nuclearcraft.redstone_mode", modeName),
                    mouseX, mouseY);
        }
    }

    /** Off/none is gray; remaining modes spread across the hue wheel so any mode count stays distinct. */
    private static int colorForMode(int mode, int count) {
        if (mode == 0) return 0xFF777777;
        float hue = count > 2 ? (mode - 1) / (float) (count - 1) * 0.83f : 0f;
        return 0xFF000000 | (Color.HSBtoRGB(hue, 0.75f, 0.85f) & 0xFFFFFF);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.button();
        if (!isActive() || !visible) return false;
        if (mouseX >= getX() && mouseX < getX() + SIZE && mouseY >= getY() && mouseY < getY() + SIZE) {
            ClientPacketDistributor.sendToServer(new PacketRedstoneModeCycle(menu.getPosition()));
            playDownSound(Minecraft.getInstance().getSoundManager());
            return true;
        }
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    }
}
