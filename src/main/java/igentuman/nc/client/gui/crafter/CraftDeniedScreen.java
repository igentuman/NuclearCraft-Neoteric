package igentuman.nc.client.gui.crafter;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import static igentuman.nc.util.TextUtils.__;

public class CraftDeniedScreen extends Screen {

    private static final int PANEL_W = 176;
    private static final int MAX_ROWS = 8;
    private static final int ROW_H = 18;

    private final EngineersCrafterScreen parent;
    private final List<net.minecraftforge.fluids.FluidStack> fluids;
    private final List<Integer> fluidAmounts;
    private final List<ItemStack> items;
    private final List<Integer> amounts;
    private final boolean tooComplex;
    private final int rows;
    private int left;
    private int top;
    private int panelH;

    public CraftDeniedScreen(EngineersCrafterScreen parent, List<ItemStack> items, List<Integer> amounts, List<net.minecraftforge.fluids.FluidStack> fluids, List<Integer> fluidAmounts, boolean tooComplex) {
        super(__("gui.nc.crafter.denied.title"));
        this.parent = parent;
        this.items = items;
        this.fluids = fluids;
        this.fluidAmounts = fluidAmounts;
        this.amounts = amounts;
        this.tooComplex = tooComplex;
        this.rows = tooComplex ? 1 : Math.min(items.size() + fluids.size(), MAX_ROWS);
    }

    public static void open(List<ItemStack> items, List<Integer> amounts, List<net.minecraftforge.fluids.FluidStack> fluids, List<Integer> fluidAmounts, boolean tooComplex) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof EngineersCrafterScreen terminal) {
            mc.setScreen(new CraftDeniedScreen(terminal, items, amounts, fluids, fluidAmounts, tooComplex));
        }
    }

    @Override
    protected void init() {
        super.init();
        panelH = 36 + rows * ROW_H + 30;
        left = (width - PANEL_W) / 2;
        top = (height - panelH) / 2;

        addRenderableWidget(Button.builder(__("gui.nc.crafter.back"), b -> onClose())
                .bounds(left + PANEL_W / 2 - 40, top + panelH - 24, 80, 20)
                .build());
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        renderBackground(graphics);
        EngineersEncoderScreen.drawPanel(graphics, left, top, PANEL_W, panelH);
        centered(graphics, title, top + 8, 0x404040);

        int rowY = top + 28;
        if (tooComplex) {
            centered(graphics, __("gui.nc.crafter.denied.too_complex"), rowY + 4, 0x802020);
        } else {
            for (int i = 0; i < rows; i++) {
                if (i < items.size()) {
                    ItemStack stack = items.get(i);
                    graphics.renderItem(stack, left + 12, rowY);
                    graphics.drawString(font, __("gui.nc.crafter.denied.entry", amounts.get(i), stack.getHoverName()),
                            left + 34, rowY + 4, 0x404040, false);
                } else {
                    int f = i - items.size();
                    var fluid = fluids.get(f);
                    igentuman.nc.util.GuiFluidRenderer.renderFluidTank(graphics, left + 12, rowY, 16, 16, fluid, fluid.getAmount());
                    graphics.drawString(font, __("gui.nc.crafter.denied.fluid_entry", fluidAmounts.get(f), fluid.getDisplayName()),
                            left + 34, rowY + 4, 0x404040, false);
                }
                rowY += ROW_H;
            }
            if (items.size() + fluids.size() > MAX_ROWS) {
                centered(graphics, __("gui.nc.crafter.denied.more", items.size() + fluids.size() - MAX_ROWS), rowY + 2, 0x606060);
            }
        }
        super.render(graphics, mouseX, mouseY, partialTicks);
    }

    private void centered(@NotNull GuiGraphics graphics, Component text, int y, int color) {
        graphics.drawString(font, text, left + PANEL_W / 2 - font.width(text) / 2, y, color, false);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
