package igentuman.nc.client.gui.fission;

import igentuman.nc.client.gui.fission.designer.DesignGrid;
import igentuman.nc.item.FissionReactorPlanItem;
import igentuman.nc.util.ItemDataUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;

import static igentuman.nc.util.TextUtils.__;

public class FissionReactorPlanScreen extends Screen {

    private static final int MARGIN = 10;
    private static final int CONTENT_TOP = 42;
    private static final int LABEL_HEIGHT = 12;
    private static final int GAP = 10;

    private final ItemStack plan;
    private final DesignGrid grid;
    private final List<Component> details = new ArrayList<>();
    private int cellSize;
    private int layerWidth;
    private int layerHeight;
    private int columns;
    private int visibleRows;
    private int scrollRow;
    private int maxScroll;
    private Button previous;
    private Button next;

    public FissionReactorPlanScreen(ItemStack stack) {
        super(stack.getHoverName());
        plan = stack.copy();
        CompoundTag design = ItemDataUtils.getCompound(plan, FissionReactorPlanItem.DESIGN_KEY);
        grid = design.isEmpty() ? null : DesignGrid.fromTag(design);
    }

    public static void open(ItemStack stack) {
        Minecraft.getInstance().setScreen(new FissionReactorPlanScreen(stack));
    }

    @Override
    protected void init() {
        details.clear();
        plan.getItem().appendHoverText(plan, minecraft.level, details, TooltipFlag.NORMAL);
        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> onClose())
                .bounds(width / 2 - 40, height - 28, 80, 20).build());
        if (grid == null) {
            return;
        }

        int availableWidth = Math.max(1, width - MARGIN * 2);
        int availableHeight = Math.max(1, height - CONTENT_TOP - 38);
        cellSize = Math.max(1, Math.min(20, Math.min(availableWidth / grid.sizeX,
                Math.max(1, availableHeight - LABEL_HEIGHT) / grid.sizeZ)));
        layerWidth = Math.max(cellSize * grid.sizeX, font.width(layerLabel(grid.sizeY)));
        layerHeight = LABEL_HEIGHT + cellSize * grid.sizeZ;
        columns = Math.max(1, (availableWidth + GAP) / (layerWidth + GAP));
        visibleRows = Math.max(1, (availableHeight + GAP) / (layerHeight + GAP));
        maxScroll = Math.max(0, (grid.sizeY + columns - 1) / columns - visibleRows);
        scrollRow = Math.min(scrollRow, maxScroll);

        previous = addRenderableWidget(Button.builder(Component.literal("▲"), button -> scroll(-1))
                .bounds(MARGIN, height - 28, 20, 20).build());
        next = addRenderableWidget(Button.builder(Component.literal("▼"), button -> scroll(1))
                .bounds(MARGIN + 24, height - 28, 20, 20).build());
        updateButtons();
    }

    private Component layerLabel(int layer) {
        return Component.translatableWithFallback("gui.nc.fission_reactor_plan.layer", "Layer %s", layer);
    }

    private void scroll(int direction) {
        scrollRow = Math.max(0, Math.min(maxScroll, scrollRow + direction));
        updateButtons();
    }

    private void updateButtons() {
        previous.active = scrollRow > 0;
        next.active = scrollRow < maxScroll;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (grid != null && delta != 0) {
            scroll(delta > 0 ? -1 : 1);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        renderBackground(graphics);
        graphics.drawCenteredString(font, title, width / 2, 10, 0xFFFFFF);
        if (grid == null) {
            graphics.drawWordWrap(font, __("tooltip.nc.fission_reactor_plan.blank"),
                    MARGIN, CONTENT_TOP, width - MARGIN * 2, 0xC0C0C0);
            super.render(graphics, mouseX, mouseY, partialTicks);
            return;
        }

        graphics.drawCenteredString(font, __("tooltip.nc.fission_reactor_plan.size",
                grid.sizeX, grid.sizeY, grid.sizeZ), width / 2, 25, 0x55FFFF);
        ItemStack hovered = ItemStack.EMPTY;
        int firstLayer = scrollRow * columns;
        int lastLayer = Math.min(grid.sizeY, firstLayer + columns * visibleRows);
        for (int layer = firstLayer; layer < lastLayer; layer++) {
            int index = layer - firstLayer;
            int x = MARGIN + index % columns * (layerWidth + GAP);
            int y = CONTENT_TOP + index / columns * (layerHeight + GAP);
            graphics.drawString(font, layerLabel(layer + 1), x, y, 0xE0E0E0);
            y += LABEL_HEIGHT;
            for (int cx = 0; cx < grid.sizeX; cx++) {
                for (int cz = 0; cz < grid.sizeZ; cz++) {
                    int px = x + cx * cellSize;
                    int py = y + cz * cellSize;
                    graphics.fill(px, py, px + cellSize, py + cellSize, 0xFF3A3A3A);
                    graphics.fill(px, py, px + cellSize - 1, py + cellSize - 1, 0xFF13545D);
                    Block block = grid.get(cx, layer, cz);
                    ItemStack stack = block == null ? ItemStack.EMPTY : new ItemStack(block);
                    if (!stack.isEmpty()) {
                        float scale = Math.max(1, cellSize - 2) / 16f;
                        graphics.pose().pushPose();
                        graphics.pose().translate(px + (cellSize > 2 ? 1 : 0), py + (cellSize > 2 ? 1 : 0), 0);
                        graphics.pose().scale(scale, scale, 1);
                        graphics.renderItem(stack, 0, 0);
                        graphics.pose().popPose();
                    }
                    if (mouseX >= px && mouseX < px + cellSize && mouseY >= py && mouseY < py + cellSize) {
                        graphics.fill(px, py, px + cellSize, py + cellSize, 0x40FFFFFF);
                        hovered = stack;
                    }
                }
            }
        }
        graphics.drawString(font, (firstLayer + 1) + "–" + lastLayer + " / " + grid.sizeY,
                MARGIN + 50, height - 22, 0xC0C0C0);
        super.render(graphics, mouseX, mouseY, partialTicks);
        if (!hovered.isEmpty()) {
            graphics.renderTooltip(font, hovered, mouseX, mouseY);
        } else if (mouseY >= 10 && mouseY < 36) {
            graphics.renderComponentTooltip(font, details, mouseX, mouseY);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
