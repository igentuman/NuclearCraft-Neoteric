package igentuman.nc.screen.element;

import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import static igentuman.nc.NuclearCraft.rl;

public class SlotWidget extends AbstractWidget {
    public static Identifier TEXTURE = rl("textures/gui/slots.png");
    public static int RELATIVE_X = 0;
    public static int RELATIVE_Y = 0;
    public int x;
    public int y;
    public int xOffset = 0;
    public int yOffset = 0;
    private Runnable onPress = null;
    private ItemStack ghostIcon = ItemStack.EMPTY;

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

    public SlotWidget ghost(ItemStack stack) {
        this.ghostIcon = stack == null ? ItemStack.EMPTY : stack;
        return this;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return onPress != null && isActive() && visible
                && mouseX >= X() && mouseX < X() + width
                && mouseY >= Y() && mouseY < Y() + height;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (isMouseOver(event.x(), event.y())) {
            onPress.run();
            return true;
        }
        return false;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, X()-1, Y()-1, xOffset, yOffset,  18, 18, 256, 256);
        if (!ghostIcon.isEmpty()) {
            graphics.fakeItem(ghostIcon, X(), Y());
            graphics.fill(X(), Y(), X() + 16, Y() + 16, 0x998B8B8B);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }
}
