package igentuman.nc.client.gui.crafter;

import igentuman.nc.client.gui.element.NCGuiElement;
import igentuman.nc.client.gui.element.slot.NormalSlot;
import igentuman.nc.container.EngineersEncoderContainer;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static igentuman.nc.util.TextUtils.__;

public class EngineersEncoderScreen extends AbstractContainerScreen<EngineersEncoderContainer> {

    private final List<NCGuiElement> widgets = new ArrayList<>();

    public EngineersEncoderScreen(EngineersEncoderContainer container, Inventory inv, Component name) {
        super(container, inv, name);
        imageWidth = 176;
        imageHeight = 250;
    }

    @Override
    protected void init() {
        super.init();
        widgets.clear();
        widgets.add(new NormalSlot(126, 36, "item_out"));
        widgets.add(new NormalSlot(8, 36, "item"));
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                widgets.add(new NormalSlot(30 + col * 18, 18 + row * 18, "item"));
            }
        }
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 9; col++) {
                widgets.add(new NormalSlot(8 + col * 18, 90 + row * 18, "item"));
            }
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                widgets.add(new NormalSlot(8 + col * 18, 168 + row * 18, "item"));
            }
        }
        for (int col = 0; col < 9; col++) {
            widgets.add(new NormalSlot(8 + col * 18, 226, "item"));
        }

        addRenderableWidget(new Button(leftPos + 92, topPos + 35, 26, 18, Component.literal("→"), b ->
                        Minecraft.getInstance().gameMode.handleInventoryButtonClick(menu.containerId, EngineersEncoderContainer.ENCODE_BTN)));
    }

    @Override
    public void render(@NotNull PoseStack graphics, int mouseX, int mouseY, float partialTicks) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTicks);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(@NotNull PoseStack graphics, float partialTicks, int mouseX, int mouseY) {
        drawPanel(graphics, leftPos, topPos, imageWidth, imageHeight);
        NCGuiElement.RELATIVE_X = leftPos;
        NCGuiElement.RELATIVE_Y = topPos;
        for (NCGuiElement w : widgets) {
            w.draw(graphics, mouseX, mouseY, partialTicks);
        }
    }

    @Override
    protected void renderLabels(@NotNull PoseStack graphics, int mouseX, int mouseY) {
        drawString(graphics, font, title, titleLabelX, titleLabelY, 0x404040);
    }

    static void drawPanel(PoseStack g, int x, int y, int w, int h) {
        fill(g, x, y, x + w, y + h, 0xFF373737);
        fill(g, x + 1, y + 1, x + w - 1, y + h - 1, 0xFFC6C6C6);
        fill(g, x + 1, y + 1, x + w - 1, y + 3, 0xFFFFFFFF);
        fill(g, x + 1, y + 1, x + 3, y + h - 1, 0xFFFFFFFF);
        fill(g, x + w - 3, y + 3, x + w - 1, y + h - 1, 0xFF555555);
        fill(g, x + 3, y + h - 3, x + w - 1, y + h - 1, 0xFF555555);
    }
}
