package igentuman.nc.screen.element;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import static igentuman.nc.NuclearCraft.rl;

public class SlotWidget extends AbstractWidget {
    public static ResourceLocation TEXTURE = rl("textures/gui/slots.png");
    public static int RELATIVE_X = 0;
    public static int RELATIVE_Y = 0;
    public int x;
    public int y;
    public int xOffset = 0;
    public int yOffset = 0;
    private Runnable onPress = null;

    public int X()
    {
        return RELATIVE_X+x;
    }

    public void fluid()
    {
        xOffset = 18;
    }

    public void item()
    {
        xOffset = 0;
    }

    public void input()
    {
        yOffset = 0;
    }

    public void output()
    {
        yOffset = 36;
    }

    public int Y()
    {
        return RELATIVE_Y+y;
    }
    public SlotWidget(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
        this.x = x;
        this.y = y;
    }

    public SlotWidget onPress(Runnable handler) {
        this.onPress = handler;
        return this;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (onPress != null && isActive() && visible
                && mouseX >= X() && mouseX < X() + width
                && mouseY >= Y() && mouseY < Y() + height) {
            onPress.run();
            return true;
        }
        return false;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.blit(TEXTURE, X()-1, Y()-1, xOffset, yOffset,  18, 18);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }
}
