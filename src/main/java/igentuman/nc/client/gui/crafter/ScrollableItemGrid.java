package igentuman.nc.client.gui.crafter;

import igentuman.nc.client.gui.element.NCGuiElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static igentuman.nc.util.TextUtils.__;

public class ScrollableItemGrid extends NCGuiElement {

    public record Cell(ItemStack stack, int stored, boolean craftable) {}

    public static final int SCROLLBAR_WIDTH = 12;

    private final int cols;
    private final int rows;
    private final Supplier<List<Cell>> supplier;
    private List<Cell> cells = new ArrayList<>();
    private int scrollRow = 0;
    public int originX = 0;
    public int originY = 0;

    public ScrollableItemGrid(int x, int y, int cols, int rows, Supplier<List<Cell>> supplier) {
        super(x, y, cols * 18, rows * 18, Component.empty());
        this.x = x;
        this.y = y;
        this.width = cols * 18;
        this.height = rows * 18;
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

    public int maxScroll() {
        return Math.max(0, totalRows() - rows);
    }

    private int gx() { return originX + x; }
    private int gy() { return originY + y; }

    @Override
    public void draw(GuiGraphics g, int mX, int mY, float pt) {
        Font font = Minecraft.getInstance().font;
        int start = scrollRow * cols;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int cellX = gx() + c * 18;
                int cellY = gy() + r * 18;
                g.blit(TEXTURE, cellX, cellY, 0, 0, 18, 18);
                int idx = start + r * cols + c;
                if (idx >= cells.size()) continue;
                Cell cell = cells.get(idx);
                int ix = cellX + 1;
                int iy = cellY + 1;
                if (cell.stored() == 0 && cell.craftable()) {
                    g.fill(ix, iy, ix + 16, iy + 16, 0x4033AAFF);
                }
                g.renderItem(cell.stack(), ix, iy);
                if (cell.stored() > 0) {
                    String txt = fmt(cell.stored());
                    float scale = 0.75f;
                    float tx = (ix + 16 - font.width(txt) * scale) / scale;
                    float ty = (iy + 16 - font.lineHeight * scale) / scale;
                    g.pose().pushPose();
                    g.pose().translate(0, 0, 200);
                    g.pose().scale(scale, scale, 1f);
                    g.drawString(font, txt, (int) tx, (int) ty, 0xFFFFFF, true);
                    g.pose().popPose();
                }
            }
        }
        int[] pos = hoveredCellPos(mX, mY);
        if (pos != null && hovered(mX, mY) != null) {
            g.pose().pushPose();
            g.pose().translate(0, 0, 300);
            g.fill(pos[0] + 1, pos[1] + 1, pos[0] + 17, pos[1] + 17, 0x80FFFFFF);
            g.pose().popPose();
        }
        drawScrollbar(g);
    }

    private void drawScrollbar(GuiGraphics g) {
        int barX = gx() + cols * 18 + 2;
        int barY = gy();
        int barH = rows * 18;
        g.fill(barX, barY, barX + SCROLLBAR_WIDTH - 2, barY + barH, 0xFF373737);
        int max = maxScroll();
        int thumbH = max == 0 ? barH : Math.max(12, barH - max * 6);
        int thumbY = barY + (max == 0 ? 0 : (barH - thumbH) * scrollRow / max);
        g.fill(barX + 1, thumbY, barX + SCROLLBAR_WIDTH - 3, thumbY + thumbH, 0xFFC6C6C6);
    }

    @Nullable
    private int[] hoveredCellPos(int mX, int mY) {
        if (mX < gx() || mY < gy() || mX >= gx() + cols * 18 || mY >= gy() + rows * 18) return null;
        int c = (mX - gx()) / 18;
        int r = (mY - gy()) / 18;
        return new int[]{gx() + c * 18, gy() + r * 18};
    }

    @Nullable
    public Cell hovered(int mX, int mY) {
        if (mX < gx() || mY < gy() || mX >= gx() + cols * 18 || mY >= gy() + rows * 18) return null;
        int c = (mX - gx()) / 18;
        int r = (mY - gy()) / 18;
        int idx = scrollRow * cols + r * cols + c;
        if (idx < 0 || idx >= cells.size()) return null;
        return cells.get(idx);
    }

    public void renderTooltip(GuiGraphics g, int mX, int mY) {
        Cell cell = hovered(mX, mY);
        if (cell == null || cell.stack().isEmpty()) return;
        Minecraft mc = Minecraft.getInstance();
        List<Component> tt = new ArrayList<>(Screen.getTooltipFromItem(mc, cell.stack()));
        if (cell.stored() > 0) tt.add(__("gui.nc.crafter.stored", cell.stored()));
        if (cell.craftable()) tt.add(__("gui.nc.crafter.craftable_tt"));
        g.renderComponentTooltip(mc.font, tt, mX, mY);
    }

    public boolean isInBounds(double mX, double mY) {
        return mX >= gx() && mY >= gy() && mX < gx() + cols * 18 && mY < gy() + rows * 18;
    }

    public boolean onScroll(double mX, double mY, double delta) {
        if (mX < gx() || mY < gy() || mX >= gx() + cols * 18 + SCROLLBAR_WIDTH || mY >= gy() + rows * 18) return false;
        scrollRow = Mth.clamp(scrollRow - (int) Math.signum(delta), 0, maxScroll());
        return true;
    }

    private static String fmt(int n) {
        if (n < 1000) return Integer.toString(n);
        if (n < 1_000_000) return (n / 1000) + "k";
        return (n / 1_000_000) + "m";
    }
}
