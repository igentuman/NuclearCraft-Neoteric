package igentuman.nc.screen.element;

import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Supplier;

/** Momentary icon button that voids all loaded fuel pebbles in the molten salt reactor. */
public class MsrVoidFuelButton extends AbstractWidget {

    private final ItemStack icon;
    private final Runnable onPress;
    private final Supplier<List<Component>> tooltipSupplier;

    public MsrVoidFuelButton(int x, int y, ItemStack icon, Runnable onPress, Supplier<List<Component>> tooltipSupplier) {
        super(x, y, 18, 18, Component.empty());
        this.icon = icon;
        this.onPress = onPress;
        this.tooltipSupplier = tooltipSupplier;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.item(icon, getX() + 1, getY() + 1);
        graphics.fill(getX() + 1, getY() + 1, getX() + 17, getY() + 17, 0x40FF3030);
        if (isHovered) {
            graphics.fill(getX() + 1, getY() + 1, getX() + 17, getY() + 17, 0x60FFFFFF);
            List<Component> tips = tooltipSupplier.get();
            if (!tips.isEmpty()) {
                graphics.setComponentTooltipForNextFrame(Minecraft.getInstance().font, tips, mouseX, mouseY);
            }
        }
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.button();
        onPress.run();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    }
}
