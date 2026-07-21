package igentuman.nc.screen.element;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public class RadiatorToggleButton extends AbstractWidget {

    private final ItemStack icon;
    private final BooleanSupplier enabledSupplier;
    private final Runnable onPress;
    private final Supplier<List<Component>> tooltipSupplier;

    public RadiatorToggleButton(int x, int y, ItemStack icon, BooleanSupplier enabledSupplier,
                                Runnable onPress, Supplier<List<Component>> tooltipSupplier) {
        super(x, y, 18, 18, Component.empty());
        this.icon = icon;
        this.enabledSupplier = enabledSupplier;
        this.onPress = onPress;
        this.tooltipSupplier = tooltipSupplier;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.renderItem(icon, getX() + 1, getY() + 1);
        if (!enabledSupplier.getAsBoolean()) {
            graphics.fill(getX() + 1, getY() + 1, getX() + 17, getY() + 17, 0xAA101010);
        }
        if (isHovered) {
            graphics.fill(getX() + 1, getY() + 1, getX() + 17, getY() + 17, 0x60FFFFFF);
            List<Component> tips = tooltipSupplier.get();
            if (!tips.isEmpty()) {
                graphics.renderComponentTooltip(Minecraft.getInstance().font, tips, mouseX, mouseY);
            }
        }
    }

    @Override
    public void onClick(double mouseX, double mouseY, int button) {
        onPress.run();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    }
}
