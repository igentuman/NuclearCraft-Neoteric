package igentuman.nc.screen.element;

import igentuman.nc.container.MultiblockControllerContainer;
import igentuman.nc.network.PacketBeamDiverterCycleOutput;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import static igentuman.nc.util.TextUtils.__;

public class BeamDiverterOutputButton extends AbstractWidget {

    private static final int WIDTH = 60;
    private static final int HEIGHT = 16;
    private final MultiblockControllerContainer menu;

    public BeamDiverterOutputButton(int x, int y, MultiblockControllerContainer menu) {
        super(x, y, WIDTH, HEIGHT, Component.empty());
        this.menu = menu;
    }

    private int synced(String field) {
        int idx = menu.getBlockEntity().getSyncFieldIndex(field);
        return idx >= 0 ? menu.getSyncedValue(idx) : 0;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int channel = synced("selectedOutputChannel");
        boolean incompatible = synced("incompatibleTurn") != 0;
        int x = getX();
        int y = getY();

        graphics.fill(x, y, x + WIDTH, y + HEIGHT, 0xFF444444);
        graphics.fill(x + 1, y + 1, x + WIDTH - 1, y + HEIGHT - 1, incompatible ? 0xFFAA3333 : 0xFF336633);

        Font font = Minecraft.getInstance().font;
        String label = __("screen.nuclearcraft.beam_diverter.output", channel).getString();
        graphics.drawString(font, label, x + (WIDTH - font.width(label)) / 2, y + 4, 0xFFFFFFFF, true);

        if (isHovered()) {
            Component tooltip = incompatible
                    ? __("screen.nuclearcraft.beam_diverter.incompatible_turn")
                    : __("screen.nuclearcraft.beam_diverter.output_tooltip");
            graphics.renderTooltip(font, tooltip, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isActive() || !visible) return false;
        if (mouseX >= getX() && mouseX < getX() + WIDTH && mouseY >= getY() && mouseY < getY() + HEIGHT) {
            PacketDistributor.sendToServer(new PacketBeamDiverterCycleOutput(menu.getPosition()));
            playDownSound(Minecraft.getInstance().getSoundManager());
            return true;
        }
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    }
}
