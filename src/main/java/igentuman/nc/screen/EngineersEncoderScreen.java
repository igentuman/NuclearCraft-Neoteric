package igentuman.nc.screen;

import igentuman.nc.container.EngineersEncoderContainer;
import igentuman.nc.container.EngineersEncoderContainer.PatternSlot;
import igentuman.nc.handler.crafter.EncoderRecipePreview;
import igentuman.nc.registration.ModEntry;
import igentuman.nc.screen.element.NcPanel;
import igentuman.nc.screen.element.ScrollableItemGrid;
import igentuman.nc.screen.element.SlotMover;
import igentuman.nc.setup.ModEntries;
import igentuman.nc.util.GuiFluidRenderer;
import igentuman.nc.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

import static igentuman.nc.container.EngineersEncoderContainer.*;

public class EngineersEncoderScreen extends AbstractContainerScreen<EngineersEncoderContainer> {

    private static final int GRID_X = 8;
    private static final int MIN_VISIBLE_ROWS = 4;
    private static final int FULL_IMAGE_HEIGHT = PLAYER_Y + 58 + 18 + 6;
    private static final int VERTICAL_MARGIN = 20;

    private static final int MODE_BTN_X = BLANK_X;
    private static final int MODE_BTN_Y = BLANK_Y + 22;
    private static final int PROC_ICON_X = 97;
    private static final int PROC_ICON_Y = 58;
    private static final int FLUID_IN_X = GHOST_X;
    private static final int FLUID_IN_Y = GHOST_Y + 3 * 18 + 4;
    private static final int MAX_FLUID_IN = 3;
    private static final int EXTRA_X = RESULT_X + 24;
    private static final int EXTRA_Y = GHOST_Y;
    private static final int EXTRA_COLS = 2;
    private static final int EXTRA_ROWS = 3;

    private static final ItemStackTemplate CRAFTING_ICON = new ItemStackTemplate(Items.CRAFTING_TABLE);
    private static final ItemStackTemplate PROCESSING_ICON = new ItemStackTemplate(Items.FURNACE);

    private int visibleRows = PATTERN_ROWS;
    private int scrollRow = 0;
    private int appliedSlotShift = 0;
    private IconButton modeButton;
    /** Window height for the visible pattern rows; {@code imageHeight} is final and stays at the full height. */
    private int dynHeight = FULL_IMAGE_HEIGHT;

    /** A pseudo-slot drawn in processing mode (fluid or secondary output), used for hover tooltips. */
    private record Preview(int x, int y, ItemStack item, FluidStack fluid) {}

    private final List<Preview> previews = new ArrayList<>();

    public EngineersEncoderScreen(EngineersEncoderContainer menu, Inventory inventory, Component title) {
        super(menu, inventory, title, GRID_X + PATTERN_COLS * 18 + ScrollableItemGrid.SCROLLBAR_WIDTH + 8, FULL_IMAGE_HEIGHT);
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
        dynHeight = FULL_IMAGE_HEIGHT + slotShift;

        super.init();
        topPos = (height - dynHeight) / 2;

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

        modeButton = new IconButton(leftPos + MODE_BTN_X - 1, topPos + MODE_BTN_Y - 1, 18, 18,
                b -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId, EngineersEncoderContainer.MODE_BTN));
        addRenderableWidget(modeButton);
        refreshModeButton();
    }

    private void refreshModeButton() {
        boolean processing = menu.isProcessingMode();
        modeButton.icon = (processing ? PROCESSING_ICON : CRAFTING_ICON).create();
        modeButton.setTooltip(Tooltip.create(Component.translatable(processing
                ? "screen.nuclearcraft.crafter.mode.processing_patterns"
                : "screen.nuclearcraft.crafter.mode.crafting_patterns")));
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
    public int getImageHeight() {
        return dynHeight;
    }

    @Override
    protected boolean hasClickedOutside(double mx, double my, int xo, int yo) {
        return mx < xo || my < yo || mx >= xo + imageWidth || my >= yo + dynHeight;
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
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTicks) {
        refreshModeButton();
        super.extractRenderState(g, mouseX, mouseY, partialTicks);
        renderPreviewTooltips(g, mouseX, mouseY);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTicks) {
        super.extractBackground(g, mouseX, mouseY, partialTicks);
        NcPanel.drawPanel(g, leftPos, topPos, imageWidth, dynHeight);
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
        previews.clear();
        if (menu.isProcessingMode()) renderProcessing(g);
    }

    private void renderProcessing(GuiGraphicsExtractor g) {
        EncoderRecipePreview recipe = menu.selectedPreview();
        for (int i = 0; i < MAX_FLUID_IN; i++) {
            NcPanel.drawSlot(g, leftPos + FLUID_IN_X + i * 18, topPos + FLUID_IN_Y);
        }
        for (int r = 0; r < EXTRA_ROWS; r++) {
            for (int c = 0; c < EXTRA_COLS; c++) {
                NcPanel.drawOutputSlot(g, leftPos + EXTRA_X + c * 18, topPos + EXTRA_Y + r * 18);
            }
        }
        NcPanel.drawSlot(g, leftPos + PROC_ICON_X, topPos + PROC_ICON_Y);
        if (recipe == null) return;

        ModEntry entry = ModEntries.get(recipe.processor());
        if (entry != null && entry.hasItem()) {
            ItemStack icon = new ItemStack(entry.item().get());
            g.item(icon, leftPos + PROC_ICON_X, topPos + PROC_ICON_Y);
            previews.add(new Preview(PROC_ICON_X, PROC_ICON_Y, icon, FluidStack.EMPTY));
        }

        int i = 0;
        for (FluidStack f : recipe.fluidInputs()) {
            if (i >= MAX_FLUID_IN) break;
            int x = FLUID_IN_X + i * 18;
            GuiFluidRenderer.renderFluidTank(g, leftPos + x, topPos + FLUID_IN_Y, 16, 16, f, Math.max(1, f.getAmount()));
            previews.add(new Preview(x, FLUID_IN_Y, ItemStack.EMPTY, f));
            i++;
        }

        List<Preview> extras = new ArrayList<>();
        List<ItemStack> outputs = recipe.itemOutputs();
        for (int o = 1; o < outputs.size(); o++) extras.add(new Preview(0, 0, outputs.get(o), FluidStack.EMPTY));
        for (FluidStack f : recipe.fluidOutputs()) extras.add(new Preview(0, 0, ItemStack.EMPTY, f));
        int idx = 0;
        for (Preview p : extras) {
            if (idx >= EXTRA_COLS * EXTRA_ROWS) break;
            int x = EXTRA_X + (idx % EXTRA_COLS) * 18;
            int y = EXTRA_Y + (idx / EXTRA_COLS) * 18;
            if (!p.item().isEmpty()) {
                g.item(p.item(), leftPos + x, topPos + y);
                g.itemDecorations(font, p.item(), leftPos + x, topPos + y);
            } else {
                GuiFluidRenderer.renderFluidTank(g, leftPos + x, topPos + y, 16, 16, p.fluid(), Math.max(1, p.fluid().getAmount()));
            }
            previews.add(new Preview(x, y, p.item(), p.fluid()));
            idx++;
        }
    }

    private void renderPreviewTooltips(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        if (!menu.isProcessingMode()) return;
        for (Preview p : previews) {
            int x = leftPos + p.x();
            int y = topPos + p.y();
            if (mouseX < x || mouseX >= x + 16 || mouseY < y || mouseY >= y + 16) continue;
            if (!p.item().isEmpty()) {
                List<Component> tip = new ArrayList<>(getTooltipFromItem(minecraft, p.item()));
                if (p.x() == PROC_ICON_X && p.y() == PROC_ICON_Y) {
                    tip.addFirst(Component.translatable("screen.nuclearcraft.crafter.processor", Component.empty())
                            .withStyle(ChatFormatting.GRAY));
                }
                g.setComponentTooltipForNextFrame(font, tip, mouseX, mouseY);
            } else if (!p.fluid().isEmpty()) {
                List<Component> tip = new ArrayList<>();
                tip.add(p.fluid().getHoverName());
                tip.add(Component.literal(TextUtils.formatLiquid(p.fluid().getAmount())).withStyle(ChatFormatting.GRAY));
                g.setComponentTooltipForNextFrame(font, tip, mouseX, mouseY);
            }
            return;
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        g.text(font, title, 8, 6, 0xFF404040, false);
        if (menu.isProcessingMode() && menu.selectedPreview() == null) {
            Component hint = Component.translatable("screen.nuclearcraft.crafter.no_recipe");
            g.text(font, hint, FLUID_IN_X + MAX_FLUID_IN * 18 + 6, FLUID_IN_Y + 4, 0xFF808080, false);
        }
    }

    /** Vanilla button that draws an item icon instead of a label. */
    private static class IconButton extends Button.Plain {
        ItemStack icon = ItemStack.EMPTY;

        IconButton(int x, int y, int w, int h, OnPress onPress) {
            super(x, y, w, h, Component.empty(), onPress, DEFAULT_NARRATION);
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
            super.extractContents(g, mouseX, mouseY, partialTick);
            if (!icon.isEmpty()) {
                g.item(icon, getX() + (width - 16) / 2, getY() + (height - 16) / 2);
            }
        }
    }
}
