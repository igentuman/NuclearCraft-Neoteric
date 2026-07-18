package igentuman.nc.client.gui.crafter;

import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static igentuman.nc.util.TextUtils.__;

public class CraftDeniedScreen extends Screen {

    private static final int PANEL_W = 176;
    private static final int MAX_ROWS = 8;
    private static final int ROW_H = 18;

    private final EngineersCrafterScreen parent;
    private final List<ItemStack> items;
    private final List<Integer> amounts;
    private final boolean tooComplex;
    private final int rows;
    private int left;
    private int top;
    private int panelH;

    public CraftDeniedScreen(EngineersCrafterScreen parent, List<ItemStack> items, List<Integer> amounts, boolean tooComplex) {
        super(__("gui.nc.crafter.denied.title"));
        this.parent = parent;
        this.items = items;
        this.amounts = amounts;
        this.tooComplex = tooComplex;
        this.rows = tooComplex ? 1 : Math.min(items.size(), MAX_ROWS);
    }

    public static void open(List<ItemStack> items, List<Integer> amounts, boolean tooComplex) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof EngineersCrafterScreen terminal) {
            mc.setScreen(new CraftDeniedScreen(terminal, items, amounts, tooComplex));
        }
    }

    @Override
    protected void init() {
        super.init();
        panelH = 36 + rows * ROW_H + 30;
        left = (width - PANEL_W) / 2;
        top = (height - panelH) / 2;

        addRenderableWidget(new Button(left + PANEL_W / 2 - 40, top + panelH - 24, 80, 20, __("gui.nc.crafter.back"), b -> onClose()));
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    @Override
    public void render(@NotNull PoseStack graphics, int mouseX, int mouseY, float partialTicks) {
        renderBackground(graphics);
        EngineersEncoderScreen.drawPanel(graphics, left, top, PANEL_W, panelH);
        centered(graphics, title, top + 8, 0x404040);

        int rowY = top + 28;
        if (tooComplex) {
            centered(graphics, __("gui.nc.crafter.denied.too_complex"), rowY + 4, 0x802020);
        } else {
            for (int i = 0; i < rows; i++) {
                ItemStack stack = items.get(i);
                net.minecraft.client.Minecraft.getInstance().getItemRenderer().renderAndDecorateItem(stack, left + 12, rowY);
                drawString(graphics, font, __("gui.nc.crafter.denied.entry", amounts.get(i), stack.getHoverName()),
                        left + 34, rowY + 4, 0x404040);
                rowY += ROW_H;
            }
            if (items.size() > MAX_ROWS) {
                centered(graphics, __("gui.nc.crafter.denied.more", items.size() - MAX_ROWS), rowY + 2, 0x606060);
            }
        }
        super.render(graphics, mouseX, mouseY, partialTicks);
    }

    private void centered(@NotNull PoseStack graphics, Component text, int y, int color) {
        drawString(graphics, font, text, left + PANEL_W / 2 - font.width(text) / 2, y, color);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
