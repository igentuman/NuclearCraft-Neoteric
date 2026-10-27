package igentuman.nc.screen;

import igentuman.nc.container.EngineersEncoderContainer;
import igentuman.nc.container.EngineersEncoderContainer.PatternSlot;
import igentuman.nc.screen.element.NcPanel;
import igentuman.nc.screen.element.ScrollableItemGrid;
import igentuman.nc.screen.element.SlotMover;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

import static igentuman.nc.container.EngineersEncoderContainer.*;

public class EngineersEncoderScreen extends AbstractContainerScreen<EngineersEncoderContainer> {

    private static final int GRID_X = 8;
    private static final int MIN_VISIBLE_ROWS = 4;
    private static final int FULL_IMAGE_HEIGHT = PLAYER_Y + 58 + 18 + 6;
    private static final int VERTICAL_MARGIN = 20;

    private int visibleRows = PATTERN_ROWS;
    private int scrollRow = 0;
    private int appliedSlotShift = 0;

    public EngineersEncoderScreen(EngineersEncoderContainer menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = GRID_X + PATTERN_COLS * 18 + ScrollableItemGrid.SCROLLBAR_WIDTH + 8;
        this.imageHeight = FULL_IMAGE_HEIGHT;
    }

    @Override
    protected void init() {
        visibleRows = PATTERN_ROWS;
        if (SlotMover.available()) {
            int minHeight = FULL_IMAGE_HEIGHT - (PATTERN_ROWS - MIN_VISIBLE_ROWS) * 18;
            int extraRows = Mth.clamp((height - minHeight - VERTICAL_MARGIN * 2) / 18, 0, PATTERN_ROWS - MIN_VISIBLE_ROWS);
            visibleRows = MIN_VISIBLE_ROWS + extraRows;
        }
        int slotShift = (visibleRows - PATTERN_ROWS) * 18;
        imageHeight = FULL_IMAGE_HEIGHT + slotShift;

        super.init();

        if (slotShift != appliedSlotShift) {
            for (int i = PLAYER_START; i < PLAYER_END; i++) {
                Slot s = menu.slots.get(i);
                SlotMover.setY(s, s.y + slotShift - appliedSlotShift);
            }
            appliedSlotShift = slotShift;
        }
        scrollRow = Mth.clamp(scrollRow, 0, maxScroll());
        layoutPatternSlots();

        Button encodeButton = Button.builder(Component.literal("→"), b ->
                        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, EngineersEncoderContainer.ENCODE_BTN))
                .bounds(leftPos + 92, topPos + 35, 26, 18)
                .tooltip(Tooltip.create(Component.translatable("screen.nuclearcraft.crafter.encode")))
                .build();
        addRenderableWidget(encodeButton);
    }

    private int maxScroll() {
        return PATTERN_ROWS - visibleRows;
    }

    private void layoutPatternSlots() {
        for (int i = PATTERN_START; i < PATTERN_END; i++) {
            PatternSlot slot = (PatternSlot) menu.slots.get(i);
            int displayRow = (i - PATTERN_START) / PATTERN_COLS - scrollRow;
            slot.visible = displayRow >= 0 && displayRow < visibleRows;
            if (slot.visible) SlotMover.setY(slot, PATTERN_Y + displayRow * 18);
        }
    }

    private boolean isOverPatterns(double mx, double my) {
        double x = mx - leftPos - GRID_X;
        double y = my - topPos - PATTERN_Y;
        return x >= 0 && x < PATTERN_COLS * 18 + ScrollableItemGrid.SCROLLBAR_WIDTH && y >= 0 && y < visibleRows * 18;
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double scrollX, double scrollY) {
        if (scrollY != 0 && maxScroll() > 0 && isOverPatterns(mx, my)) {
            scrollRow = Mth.clamp(scrollRow - (int) Math.signum(scrollY), 0, maxScroll());
            layoutPatternSlots();
            return true;
        }
        return super.mouseScrolled(mx, my, scrollX, scrollY);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTicks) {
        super.render(g, mouseX, mouseY, partialTicks);
        renderTooltip(g, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTicks, int mouseX, int mouseY) {
        NcPanel.drawPanel(g, leftPos, topPos, imageWidth, imageHeight);
        for (int i = 0; i < menu.slots.size(); i++) {
            Slot s = menu.slots.get(i);
            if (!s.isActive()) continue;
            if (i == 0) {
                NcPanel.drawOutputSlot(g, leftPos + s.x, topPos + s.y);
            } else {
                NcPanel.drawSlot(g, leftPos + s.x, topPos + s.y);
            }
        }
        ScrollableItemGrid.drawScrollbar(g, leftPos + GRID_X + PATTERN_COLS * 18 + 1, topPos + PATTERN_Y - 1,
                visibleRows * 18, scrollRow, maxScroll());
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(font, title, 8, 6, 0x404040, false);
    }
}
