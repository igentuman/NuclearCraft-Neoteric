package igentuman.nc.screen.element;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import static igentuman.nc.Main.rl;

public class ProcessorImageButton extends AbstractWidget {

    private static final ResourceLocation WIDGETS = rl("textures/gui/widgets.png");

    private final IntSupplier uSupplier;
    private final IntSupplier vSupplier;
    private final Runnable onPress;
    private final Supplier<List<Component>> tooltipSupplier;

    public ProcessorImageButton(int x, int y, int u, int v, Runnable onPress, List<Component> tooltips) {
        super(x, y, 18, 18, Component.empty());
        this.uSupplier = () -> u;
        this.vSupplier = () -> v;
        this.onPress = onPress;
        this.tooltipSupplier = () -> tooltips;
    }

    public ProcessorImageButton(int x, int y, IntSupplier uSupplier, IntSupplier vSupplier,
                                Runnable onPress, Supplier<List<Component>> tooltipSupplier) {
        super(x, y, 18, 18, Component.empty());
        this.uSupplier = uSupplier;
        this.vSupplier = vSupplier;
        this.onPress = onPress;
        this.tooltipSupplier = tooltipSupplier;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.blit(WIDGETS, getX(), getY(), uSupplier.getAsInt(), vSupplier.getAsInt(), 18, 18, 256, 256);
        if (isHovered) {
            graphics.fill(getX(), getY(), getX() + 18, getY() + 18, 0x40FFFFFF);
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
