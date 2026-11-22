package igentuman.nc.screen;

import igentuman.nc.client.gui.fission.designer.DesignGrid;
import igentuman.nc.client.gui.fission.designer.DesignGridRenderer;
import igentuman.nc.item.FissionReactorPlanItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;

import static igentuman.nc.util.TextUtils.__;

public class FissionReactorPlanScreen extends Screen {

    private static final int MARGIN = 10;
    private static final int LABEL_HEIGHT = 12;
    private static final int GAP = 10;
    private static final int GRID_TOP = 38;

    private final DesignGrid grid;
    private final String fuel;
    private int cell;
    private int columns;
    private int visibleRows;
    private int scroll;
    private int maxScroll;
    private Button previousButton;
    private Button nextButton;

    public FissionReactorPlanScreen(ItemStack stack) {
        super(stack.getHoverName());
        CompoundTag design = FissionReactorPlanItem.getDesign(stack);
        grid = DesignGrid.fromTag(design);
        fuel = FissionReactorPlanItem.fuelLabel(design.getStringOr(FissionReactorPlanItem.FUEL_KEY, ""),
                design.getStringOr(FissionReactorPlanItem.VARIANT_KEY, ""));
    }

    public static void open(ItemStack stack) {
        Minecraft.getInstance().setScreen(new FissionReactorPlanScreen(stack));
    }

    @Override
    protected void init() {
        int availableWidth = Math.max(1, width - 2 * MARGIN);
        int availableHeight = Math.max(1, height - GRID_TOP - 54);
        cell = Math.max(2, Math.min(20, Math.min(availableWidth / grid.sizeX,
                Math.max(1, availableHeight - LABEL_HEIGHT) / grid.sizeZ)));
        columns = Math.max(1, (availableWidth + GAP) / (cell * grid.sizeX + GAP));
        visibleRows = Math.max(1, (availableHeight + GAP) / (LABEL_HEIGHT + cell * grid.sizeZ + GAP));
        maxScroll = Math.max(0, (grid.sizeY + columns - 1) / columns - visibleRows);
        previousButton = addRenderableWidget(Button.builder(Component.literal("<"), b -> scroll(-1))
                .bounds(MARGIN, height - 26, 20, 20).build());
        nextButton = addRenderableWidget(Button.builder(Component.literal(">"), b -> scroll(1))
                .bounds(MARGIN + 24, height - 26, 20, 20).build());
        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, b -> onClose())
                .bounds(width - MARGIN - 60, height - 26, 60, 20).build());
        scroll(0);
    }

    private void scroll(int delta) {
        scroll = Mth.clamp(scroll + delta, 0, maxScroll);
        previousButton.active = scroll > 0;
        nextButton.active = scroll < maxScroll;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.centeredText(font, title, width / 2, 6, 0xFFFFFFFF);
        graphics.centeredText(font, __("screen.nuclearcraft.reactor_plan.read_only",
                grid.sizeX, grid.sizeY, grid.sizeZ), width / 2, 20, 0xFFA0A0A0);

        int firstLayer = scroll * columns;
        int lastLayer = Math.min(grid.sizeY, firstLayer + columns * visibleRows);
        for (int layer = firstLayer; layer < lastLayer; layer++) {
            int index = layer - firstLayer;
            int x = MARGIN + index % columns * (cell * grid.sizeX + GAP);
            int y = GRID_TOP + index / columns * (LABEL_HEIGHT + cell * grid.sizeZ + GAP);
            graphics.text(font, __("screen.nuclearcraft.designer.layer", layer + 1), x, y + 2, 0xFFE0E0E0);
            DesignGridRenderer.drawLayer(graphics, grid, layer, x, y + LABEL_HEIGHT, cell);
        }

        if (!fuel.isEmpty()) {
            graphics.text(font, font.plainSubstrByWidth(__("tooltip.nc.fission_reactor_plan.fuel", fuel).getString(),
                    width - 2 * MARGIN), MARGIN, height - 46, 0xFFFFCC66);
        }
        graphics.text(font, __("screen.nuclearcraft.reactor_plan.layers", firstLayer + 1, lastLayer, grid.sizeY),
                MARGIN + 50, height - 20, 0xFFC0C0C0);
        drawCellTooltip(graphics, mouseX, mouseY, firstLayer, lastLayer);
    }

    private void drawCellTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY, int firstLayer, int lastLayer) {
        for (int layer = firstLayer; layer < lastLayer; layer++) {
            int index = layer - firstLayer;
            int x = MARGIN + index % columns * (cell * grid.sizeX + GAP);
            int y = GRID_TOP + index / columns * (LABEL_HEIGHT + cell * grid.sizeZ + GAP) + LABEL_HEIGHT;
            if (mouseX < x || mouseX >= x + cell * grid.sizeX || mouseY < y || mouseY >= y + cell * grid.sizeZ) {
                continue;
            }
            int cx = (mouseX - x) / cell;
            int cz = (mouseY - y) / cell;
            Block block = grid.get(cx, layer, cz);
            List<Component> tooltip = new ArrayList<>();
            if (block != null) {
                tooltip.add(new ItemStack(block).getHoverName());
            }
            tooltip.add(__("screen.nuclearcraft.reactor_plan.position", cx + 1, layer + 1, cz + 1));
            graphics.setComponentTooltipForNextFrame(font, tooltip, mouseX, mouseY);
            return;
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        scroll(-(int) Math.signum(scrollY));
        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (minecraft.options.keyInventory.matches(event)) {
            onClose();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
