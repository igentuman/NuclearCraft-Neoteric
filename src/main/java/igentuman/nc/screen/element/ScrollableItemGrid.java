package igentuman.nc.screen.element;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ScrollableItemGrid extends AbstractWidget {

    public record Cell(ItemStack stack, int stored, boolean craftable) {}

    public static final int SCROLLBAR_WIDTH = 12;

    private final int cols;
    private final int rows;
    private final Supplier<List<Cell>> supplier;
    private List<Cell> cells = new ArrayList<>();
    private int scrollRow = 0;

    public ScrollableItemGrid(int x, int y, int cols, int rows, Supplier<List<Cell>> supplier) {
        super(x, y, cols * 18 + SCROLLBAR_WIDTH, rows * 18, Component.empty());
        this.cols = cols;
        this.rows = rows;
        this.supplier = supplier;
    }

    public void refresh() {
        cells = supplier.get();
        scrollRow = Mth.clamp(scrollRow, 0, maxScroll());
    }

    private int totalRows() {
        return (cells.size() + cols - 1) / cols;
    }

    private int maxScroll() {
        return Math.max(0, totalRows() - rows);
    }

    @Override
    protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        refresh();
        Font font = Minecraft.getInstance().font;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int ix = getX() + c * 18 + 1;
                int iy = getY() + r * 18 + 1;
                NcPanel.drawSlot(g, ix, iy);
                int idx = (scrollRow + r) * cols + c;
                if (idx >= cells.size()) continue;
                Cell cell = cells.get(idx);
                if (cell.stored() == 0 && cell.craftable()) {
                    g.fill(ix, iy, ix + 16, iy + 16, 0x4033AAFF);
                }
                if (!cell.stack().isEmpty()) {
                    g.renderItem(cell.stack(), ix, iy);
                }
                if (cell.stored() > 0) {
                    String s = fmt(cell.stored());
                    g.pose().pushPose();
                    g.pose().translate(0, 0, 200);
                    float scale = 0.75f;
                    g.pose().scale(scale, scale, 1f);
                    int tx = Math.round((ix + 17 - font.width(s) * scale) / scale);
                    int ty = Math.round((iy + 17 - font.lineHeight * scale) / scale);
                    g.drawString(font, s, tx, ty, 0xFFFFFF, true);
                    g.pose().popPose();
                }
            }
        }
        drawScrollbar(g);
        if (indexAt(mouseX, mouseY) >= 0) {
            int c = (mouseX - getX()) / 18;
            int r = (mouseY - getY()) / 18;
            int cellX = getX() + c * 18;
            int cellY = getY() + r * 18;
            g.fill(cellX + 1, cellY + 1, cellX + 17, cellY + 17, 0x80FFFFFF);
        }
    }

    private void drawScrollbar(GuiGraphics g) {
        int barX = getX() + cols * 18 + 2;
        int barY = getY();
        int barH = rows * 18;
        int barW = SCROLLBAR_WIDTH - 2;
        g.fill(barX, barY, barX + barW, barY + barH, 0xFF373737);
        int max = maxScroll();
        int thumbH = max == 0 ? barH : Math.max(12, barH - max * 6);
        int thumbY = max == 0 ? barY : barY + (barH - thumbH) * scrollRow / max;
        g.fill(barX + 1, thumbY + 1, barX + barW - 1, thumbY + thumbH - 1, 0xFFC6C6C6);
    }

    private int indexAt(double mx, double my) {
        if (!isInBounds(mx, my)) return -1;
        int c = (int) ((mx - getX()) / 18);
        int r = (int) ((my - getY()) / 18);
        if (c < 0 || c >= cols || r < 0 || r >= rows) return -1;
        int idx = (scrollRow + r) * cols + c;
        return (idx < 0 || idx >= cells.size()) ? -1 : idx;
    }

    public Cell hovered(double mx, double my) {
        int idx = indexAt(mx, my);
        return idx < 0 ? null : cells.get(idx);
    }

    public boolean isInBounds(double mx, double my) {
        return mx >= getX() && mx < getX() + cols * 18 && my >= getY() && my < getY() + rows * 18;
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double scrollX, double scrollY) {
        if (scrollY == 0) return false;
        scrollRow = Mth.clamp(scrollRow - (int) Math.signum(scrollY), 0, maxScroll());
        return true;
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        return false;
    }

    private static String fmt(int n) {
        if (n < 1000) return Integer.toString(n);
        if (n < 1_000_000) return (n / 1000) + "k";
        return (n / 1_000_000) + "m";
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narration) {
    }
}
