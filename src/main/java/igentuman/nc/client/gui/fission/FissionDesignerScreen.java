package igentuman.nc.client.gui.fission;

import igentuman.nc.NuclearCraft;
import igentuman.nc.client.gui.element.FuelDropdown;
import igentuman.nc.client.gui.element.NCGuiElement;
import igentuman.nc.client.gui.fission.designer.ClientFissionDesignCache;
import igentuman.nc.client.gui.fission.designer.DesignGrid;
import igentuman.nc.client.gui.fission.designer.DesignSimulator;
import igentuman.nc.block.fission.HeatSinkBlock;
import igentuman.nc.container.FissionDesignerContainer;
import igentuman.nc.item.FissionReactorPlanItem;
import igentuman.nc.multiblock.fission.FissionReactorRegistration;
import igentuman.nc.network.toServer.PacketLoadFissionDesign;
import igentuman.nc.network.toServer.PacketSaveFissionDesign;
import igentuman.nc.util.TextUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;

import static igentuman.nc.handler.config.FissionConfig.FISSION_CONFIG;
import static igentuman.nc.multiblock.fission.FissionReactorRegistration.FISSION_BLOCKS;
import static igentuman.nc.util.TextUtils.__;

public class FissionDesignerScreen extends AbstractContainerScreen<FissionDesignerContainer> {

    protected DesignGrid grid = new DesignGrid(5, 5, 5);
    protected DesignSimulator simulator = new DesignSimulator(grid);

    protected final List<PaletteEntry> palette = new ArrayList<>();
    protected int selectedPaletteIndex = -1;
    protected int paletteScroll = 0;

    protected FuelDropdown fuelDropdown;

    protected boolean newMode = false;
    protected int pendingX = 5;
    protected int pendingY = 5;
    protected int pendingZ = 5;
    protected BlockPos designerPos;
    protected boolean designRestored = false;

    protected static final int MARGIN = 10;
    protected static final int BTN_W = 44;
    protected static final int BTN_H = 16;
    protected static final int PAL_SLOT = 18;
    protected static final int RIGHT_W = 162;
    protected static final int CELL_MAX = 20;
    protected static final int LABEL_H = 10;
    protected static final int LAYER_GAP_X = 10;
    protected static final int LAYER_GAP_Y = 6;
    protected int topBarY;
    protected int newBtnX, saveBtnX, loadBtnX;
    protected int contentTop;
    protected int rightX;
    protected int cell;
    protected int layerAreaX, layerAreaY, layerBlockW, layerBlockH;
    protected int layerColsFit, layerRowsVisible;
    protected int layerScroll = 0;
    protected int palX, palY, palCols, palVisibleRows;
    protected int fuelY, statsX, statsY;

    public FissionDesignerScreen(FissionDesignerContainer container, Inventory inv, Component name) {
        super(container, inv, name);
        this.imageWidth = 0;
        this.imageHeight = 0;
        this.designerPos = container.getBlockPos();
    }

    @Override
    protected void init() {
        super.init();
        if (palette.isEmpty()) {
            buildPalette();
        }
        fuelDropdown = new FuelDropdown(0, 0, RIGHT_W, 14);
        if (!designRestored) {
            designRestored = true;
            restoreFromCache();
        }
        layout();
    }

    protected void layout() {
        topBarY = 6;
        contentTop = 24;
        newBtnX = MARGIN;
        saveBtnX = MARGIN + BTN_W + 6;
        loadBtnX = MARGIN + (BTN_W + 6) * 2;
        rightX = width - RIGHT_W - MARGIN;

        // left: flow of per-layer grids
        layerAreaX = MARGIN;
        layerAreaY = contentTop;
        int gridAreaW = rightX - MARGIN - layerAreaX;
        int gridAreaH = height - layerAreaY - MARGIN;
        int cols = Math.max(1, grid.sizeX);
        int rows = Math.max(1, grid.sizeZ);
        cell = Math.min(Math.min(gridAreaW / cols, (gridAreaH - LABEL_H) / rows), CELL_MAX);
        cell = Math.max(cell, 8);
        layerBlockW = cell * cols;
        layerBlockH = LABEL_H + cell * rows;
        layerColsFit = Math.max(1, (gridAreaW + LAYER_GAP_X) / (layerBlockW + LAYER_GAP_X));
        layerRowsVisible = Math.max(1, (gridAreaH + LAYER_GAP_Y) / (layerBlockH + LAYER_GAP_Y));
        int totalRows = (grid.sizeY + layerColsFit - 1) / layerColsFit;
        int maxScroll = Math.max(0, totalRows - layerRowsVisible);
        layerScroll = Math.max(0, Math.min(maxScroll, layerScroll));

        palX = rightX;
        palY = contentTop;
        palCols = Math.max(1, RIGHT_W / PAL_SLOT);
        palVisibleRows = 7;
        fuelY = palY + palVisibleRows * PAL_SLOT + 10;
        statsX = rightX;
        statsY = fuelY + 22;

        if (fuelDropdown != null) {
            fuelDropdown.x = rightX;
            fuelDropdown.y = fuelY;
        }
    }

    protected int[] layerGridOrigin(int layer) {
        int visIndex = layer - layerScroll * layerColsFit;
        if (visIndex < 0 || visIndex >= layerColsFit * layerRowsVisible) {
            return null;
        }
        int col = visIndex % layerColsFit;
        int row = visIndex / layerColsFit;
        int blockX = layerAreaX + col * (layerBlockW + LAYER_GAP_X);
        int blockY = layerAreaY + row * (layerBlockH + LAYER_GAP_Y);
        return new int[]{blockX, blockY + LABEL_H};
    }

    protected void buildPalette() {
        palette.clear();
        addPaletteBlock("fission_reactor_solid_fuel_cell");
        addPaletteBlock("fission_reactor_irradiation_chamber");
        addPaletteBlock("fission_reactor_pile-driver_irradiation_chamber");
        for (Block b : FissionReactorRegistration.getHSBlocks()) {
            palette.add(entryFor(b));
        }
        for (Block b : FissionReactorRegistration.moderators()) {
            palette.add(entryFor(b));
        }
        if (!palette.isEmpty()) {
            selectedPaletteIndex = 0;
        }
    }

    protected void addPaletteBlock(String key) {
        var ro = FISSION_BLOCKS.get(key);
        if (ro != null && ro.isPresent()) {
            palette.add(entryFor(ro.get()));
        }
    }

    protected PaletteEntry entryFor(Block block) {
        ItemStack stack = new ItemStack(block);
        List<Component> tip = new ArrayList<>();
        tip.add(stack.getHoverName());
        if (block instanceof HeatSinkBlock hs && hs.def != null) {
            tip.add(TextUtils.applyFormat(__("heat_sink.heat.descr", TextUtils.numberFormat(hs.heat)), ChatFormatting.GOLD));
            tip.add(TextUtils.applyFormat(hs.getPlacementRule(), ChatFormatting.AQUA));
        }
        return new PaletteEntry(block, stack, tip);
    }

    protected Block selectedBlock() {
        if (selectedPaletteIndex < 0 || selectedPaletteIndex >= palette.size()) {
            return null;
        }
        return palette.get(selectedPaletteIndex).block;
    }

    @Override
    public void render(PoseStack g, int mouseX, int mouseY, float partial) {
        NCGuiElement.RELATIVE_X = 0;
        NCGuiElement.RELATIVE_Y = 0;
        layout();
        simulator.setFuelKey(fuelDropdown.getSelectedFuelKey());
        simulator.simulateIfDirty();
        renderBackground(g);
        drawCenteredString(g, font, menu.getTitle(), width / 2, topBarY + 2, 0xFFFFFF);
        drawButton(g, newBtnX, topBarY, "New", mouseX, mouseY);
        drawButton(g, saveBtnX, topBarY, "Save", mouseX, mouseY);
        drawButton(g, loadBtnX, topBarY, "Load", mouseX, mouseY);

        drawLayers(g, mouseX, mouseY);
        drawPalette(g, mouseX, mouseY);
        drawStats(g);

        fuelDropdown.draw(g, mouseX, mouseY, partial);
        fuelDropdown.drawOverlay(g, mouseX, mouseY, partial);

        if (newMode) {
            drawNewModal(g, mouseX, mouseY);
        } else {
            drawTooltips(g, mouseX, mouseY);
        }
    }

    protected void drawLayers(PoseStack g, int mouseX, int mouseY) {
        int cols = grid.sizeX;
        int rows = grid.sizeZ;
        for (int layer = 0; layer < grid.sizeY; layer++) {
            int[] o = layerGridOrigin(layer);
            if (o == null) {
                continue;
            }
            int gx = o[0];
            int gy = o[1];
            font.draw(g, "Layer " + (layer + 1), gx, gy - LABEL_H, 0xCCE0E0E0);
            int w = cell * cols;
            int h = cell * rows;
            fill(g, gx - 1, gy - 1, gx + w + 1, gy + h + 1, 0xCC3A3A3A);
            for (int cx = 0; cx < cols; cx++) {
                for (int cz = 0; cz < rows; cz++) {
                    int px = gx + cx * cell;
                    int py = gy + cz * cell;
                    fill(g, px, py, px + cell - 1, py + cell - 1, 0xCC13545d);
                    Block b = grid.get(cx, layer, cz);
                    if (b != null) {
                        renderScaledItem(g, new ItemStack(b), px + (cell - 16) / 2, py + (cell - 16) / 2, cell / 16f);
                    }
                    if (grid.invalidCells.contains(new BlockPos(cx, layer, cz))) {
                        drawOutline(g, px, py, cell - 1, cell - 1, 0xCCFF3030);
                    }
                }
            }
        }
        // hover highlight
        int[] hc = cellAt(mouseX, mouseY);
        if (hc != null) {
            int[] o = layerGridOrigin(hc[0]);
            if (o != null) {
                int px = o[0] + hc[1] * cell;
                int py = o[1] + hc[2] * cell;
                fill(g, px, py, px + cell - 1, py + cell - 1, 0x40FFFFFF);
            }
        }
    }

    protected void drawPalette(PoseStack g, int mouseX, int mouseY) {
        int areaW = palCols * PAL_SLOT;
        int areaH = palVisibleRows * PAL_SLOT;
        fill(g, palX - 1, palY - 1, palX + areaW + 1, palY + areaH + 1, 0xFF3A3A3A);
        for (int i = 0; i < palVisibleRows * palCols; i++) {
            int idx = paletteScroll * palCols + i;
            if (idx >= palette.size()) {
                break;
            }
            int col = i % palCols;
            int row = i / palCols;
            int px = palX + col * PAL_SLOT;
            int py = palY + row * PAL_SLOT;
            fill(g, px, py, px + PAL_SLOT - 1, py + PAL_SLOT - 1, 0xFF101010);
            itemRenderer.renderGuiItem(palette.get(idx).stack, px + 1, py + 1);
            if (idx == selectedPaletteIndex) {
                drawOutline(g, px, py, PAL_SLOT - 1, PAL_SLOT - 1, 0xFFFFD030);
            }
        }
    }

    protected void drawStats(PoseStack g) {
        int y = statsY;
        int color = 0xC0C0C0;
        font.draw(g, "Heat/t: " + fmt(simulator.heatPerTick), statsX, y, color);
        font.draw(g, "Cooling/t: " + fmt(simulator.coolingPerTick), statsX, y += 11, color);
        font.draw(g, "Net heat: " + fmt(simulator.netHeat), statsX, y += 11, color);
        font.draw(g, "FE/t: " + fmt(simulator.energyPerTick), statsX, y += 11, color);
        font.draw(g, "Steam/t: " + simulator.steamPerTick, statsX, y += 11, color);
        font.draw(g, "Irradiation: " + simulator.irradiation, statsX, y += 11, color);
        String meltdown = Double.isInfinite(simulator.meltdownTimeSeconds)
                ? "∞" : fmt(simulator.meltdownTimeSeconds) + "s";
        font.draw(g, "Meltdown: " + meltdown, statsX, y += 11, color);
    }

    protected void drawTooltips(PoseStack g, int mouseX, int mouseY) {
        // palette hover
        int areaW = palCols * PAL_SLOT;
        int areaH = palVisibleRows * PAL_SLOT;
        if (mouseX >= palX && mouseX < palX + areaW && mouseY >= palY && mouseY < palY + areaH) {
            int col = (mouseX - palX) / PAL_SLOT;
            int row = (mouseY - palY) / PAL_SLOT;
            int idx = paletteScroll * palCols + row * palCols + col;
            if (col < palCols && idx >= 0 && idx < palette.size()) {
                renderComponentTooltip(g, palette.get(idx).tooltip, mouseX, mouseY);
            }
        }
    }

    protected void drawNewModal(PoseStack g, int mouseX, int mouseY) {
        int mw = 180, mh = 120;
        int mx = (width - mw) / 2, my = (height - mh) / 2;
        fill(g, 0, 0, width, height, 0xA0000000);
        fill(g, mx, my, mx + mw, mh + my, 0xFF202020);
        drawOutline(g, mx, my, mw, mh, 0xFF5A5A5A);
        drawCenteredString(g, font, "New Design", mx + mw / 2, my + 8, 0xFFFFFF);
        drawStepper(g, "X", pendingX, mx + 20, my + 30);
        drawStepper(g, "Y", pendingY, mx + 20, my + 52);
        drawStepper(g, "Z", pendingZ, mx + 20, my + 74);
        drawButton(g, mx + 20, my + mh - 24, "Create", mouseX, mouseY);
        drawButton(g, mx + mw - 20 - BTN_W, my + mh - 24, "Cancel", mouseX, mouseY);
    }

    protected void drawStepper(PoseStack g, String label, int value, int x, int y) {
        font.draw(g, label + ":", x, y + 4, 0xFFFFFF);
        fill(g, x + 20, y, x + 34, y + 14, 0xFF404040);
        drawCenteredString(g, font, "-", x + 27, y + 3, 0xFFFFFF);
        drawCenteredString(g, font, String.valueOf(value), x + 55, y + 3, 0xFFFFFF);
        fill(g, x + 76, y, x + 90, y + 14, 0xFF404040);
        drawCenteredString(g, font, "+", x + 83, y + 3, 0xFFFFFF);
    }

    protected void drawButton(PoseStack g, int x, int y, String text, int mouseX, int mouseY) {
        boolean hover = mouseX >= x && mouseX < x + BTN_W && mouseY >= y && mouseY < y + BTN_H;
        fill(g, x, y, x + BTN_W, y + BTN_H, hover ? 0xFF505050 : 0xFF303030);
        drawOutline(g, x, y, BTN_W, BTN_H, 0xFF5A5A5A);
        drawCenteredString(g, font, text, x + BTN_W / 2, y + (BTN_H - 8) / 2, 0xFFFFFF);
    }

    protected void drawOutline(PoseStack g, int x, int y, int w, int h, int color) {
        fill(g, x, y, x + w, y + 1, color);
        fill(g, x, y + h - 1, x + w, y + h, color);
        fill(g, x, y, x + 1, y + h, color);
        fill(g, x + w - 1, y, x + w, y + h, color);
    }

    protected void renderScaledItem(PoseStack g, ItemStack stack, int x, int y, float scale) {
        if (scale >= 1f) {
            itemRenderer.renderGuiItem(stack, x, y);
            return;
        }
        PoseStack mvStack = RenderSystem.getModelViewStack();
        mvStack.pushPose();
        mvStack.translate(x + 8 - 8 * scale, y + 8 - 8 * scale, 0);
        mvStack.scale(scale, scale, 1f);
        itemRenderer.renderGuiItem(stack, 0, 0);
        mvStack.popPose();
    }

    protected String fmt(double v) {
        return String.format("%.1f", v);
    }

    protected int[] cellAt(double mouseX, double mouseY) {
        int cols = grid.sizeX;
        int rows = grid.sizeZ;
        int w = cell * cols;
        int h = cell * rows;
        for (int layer = 0; layer < grid.sizeY; layer++) {
            int[] o = layerGridOrigin(layer);
            if (o == null) {
                continue;
            }
            int gx = o[0];
            int gy = o[1];
            if (mouseX < gx || mouseX >= gx + w || mouseY < gy || mouseY >= gy + h) {
                continue;
            }
            int cx = (int) ((mouseX - gx) / cell);
            int cz = (int) ((mouseY - gy) / cell);
            if (cx >= 0 && cx < cols && cz >= 0 && cz < rows) {
                return new int[]{layer, cx, cz};
            }
        }
        return null;
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (newMode) {
            handleNewModalClick(mx, my);
            return true;
        }
        boolean anyOpen = fuelDropdown.isOpen();
        if (fuelDropdown.mouseClicked(mx, my, button)) {
            return true;
        }
        if (anyOpen) {
            return true;
        }
        // top buttons
        if (hit(newBtnX, topBarY, BTN_W, BTN_H, mx, my)) {
            pendingX = clampSize(grid.sizeX);
            pendingY = clampSize(grid.sizeY);
            pendingZ = clampSize(grid.sizeZ);
            newMode = true;
            return true;
        }
        if (hit(saveBtnX, topBarY, BTN_W, BTN_H, mx, my)) {
            onSave();
            return true;
        }
        if (hit(loadBtnX, topBarY, BTN_W, BTN_H, mx, my)) {
            onLoad();
            return true;
        }
        // palette selection
        int areaW = palCols * PAL_SLOT;
        int areaH = palVisibleRows * PAL_SLOT;
        if (mx >= palX && mx < palX + areaW && my >= palY && my < palY + areaH) {
            int col = (int) ((mx - palX) / PAL_SLOT);
            int row = (int) ((my - palY) / PAL_SLOT);
            int idx = paletteScroll * palCols + row * palCols + col;
            if (col < palCols && idx >= 0 && idx < palette.size()) {
                selectedPaletteIndex = idx;
            }
            return true;
        }
        // grid placement
        int[] c = cellAt(mx, my);
        if (c != null) {
            if (button == 1 || hasShiftDown()) {
                grid.clear(c[1], c[0], c[2]);
            } else {
                grid.set(c[1], c[0], c[2], selectedBlock());
            }
            simulator.markDirty();
            saveToCache();
            return true;
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (!newMode && !fuelDropdown.isOpen()) {
            int[] c = cellAt(mx, my);
            if (c != null) {
                if (button == 1 || hasShiftDown()) {
                    grid.clear(c[1], c[0], c[2]);
                } else {
                    grid.set(c[1], c[0], c[2], selectedBlock());
                }
                simulator.markDirty();
                saveToCache();
                return true;
            }
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double delta) {
        if (fuelDropdown.mouseScrolled(mx, my, delta)) {
            return true;
        }
        int areaW = palCols * PAL_SLOT;
        int areaH = palVisibleRows * PAL_SLOT;
        if (mx >= palX && mx < palX + areaW && my >= palY && my < palY + areaH) {
            int maxRows = (palette.size() + palCols - 1) / palCols;
            int maxScroll = Math.max(0, maxRows - palVisibleRows);
            paletteScroll = Math.max(0, Math.min(maxScroll, paletteScroll - (int) Math.signum(delta)));
            return true;
        }
        // layer flow scroll
        if (mx >= layerAreaX && mx < rightX - MARGIN && my >= layerAreaY) {
            int totalRows = (grid.sizeY + layerColsFit - 1) / layerColsFit;
            int maxScroll = Math.max(0, totalRows - layerRowsVisible);
            layerScroll = Math.max(0, Math.min(maxScroll, layerScroll - (int) Math.signum(delta)));
            return true;
        }
        return super.mouseScrolled(mx, my, delta);
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (fuelDropdown != null && fuelDropdown.isFieldFocused() && fuelDropdown.keyPressed(key, scanCode, modifiers)) {
            return true;
        }
        return super.keyPressed(key, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char c, int modifiers) {
        if (fuelDropdown != null && fuelDropdown.isFieldFocused() && fuelDropdown.charTyped(c, modifiers)) {
            return true;
        }
        return super.charTyped(c, modifiers);
    }

    protected void handleNewModalClick(double mx, double my) {
        int mw = 180, mh = 120;
        int mx0 = (width - mw) / 2, my0 = (height - mh) / 2;
        int x = mx0 + 20;
        int[] rowY = {my0 + 30, my0 + 52, my0 + 74};
        for (int i = 0; i < 3; i++) {
            int y = rowY[i];
            if (hit(x + 20, y, 14, 14, mx, my)) {
                adjustPending(i, -1);
                return;
            }
            if (hit(x + 76, y, 14, 14, mx, my)) {
                adjustPending(i, 1);
                return;
            }
        }
        if (hit(mx0 + 20, my0 + mh - 24, BTN_W, BTN_H, mx, my)) {
            grid.resize(clampSize(pendingX), clampSize(pendingY), clampSize(pendingZ));
            simulator.markDirty();
            layerScroll = 0;
            newMode = false;
            saveToCache();
            return;
        }
        if (hit(mx0 + mw - 20 - BTN_W, my0 + mh - 24, BTN_W, BTN_H, mx, my)) {
            newMode = false;
        }
    }

    protected void adjustPending(int axis, int delta) {
        switch (axis) {
            case 0 -> pendingX = clampSize(pendingX + delta);
            case 1 -> pendingY = clampSize(pendingY + delta);
            case 2 -> pendingZ = clampSize(pendingZ + delta);
        }
    }

    protected int maxDesignSize() {
        return Math.max(1, FISSION_CONFIG.MAX_SIZE.get() - 2);
    }

    protected int clampSize(int v) {
        return Math.max(1, Math.min(maxDesignSize(), v));
    }

    protected boolean hit(int x, int y, int w, int h, double mx, double my) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    protected void onSave() {
        simulator.simulateIfDirty();
        net.minecraft.nbt.CompoundTag tag = grid.toTag();
        tag.putDouble(FissionReactorPlanItem.NET_HEAT_KEY, simulator.netHeat);
        tag.putInt(FissionReactorPlanItem.FE_GEN_KEY, (int) simulator.energyPerTick);
        NuclearCraft.packetHandler().sendToServer(new PacketSaveFissionDesign(tag, fuelDropdown.getSelectedFuelKey()));
    }

    protected void onLoad() {
        int slot = getMinecraft().player.getInventory().selected;
        NuclearCraft.packetHandler().sendToServer(new PacketLoadFissionDesign(slot));
    }

    public void applyLoadedDesign(DesignGrid loaded, List<String> fuelKey) {
        this.grid = loaded;
        this.simulator = new DesignSimulator(grid);
        this.layerScroll = 0;
        if (fuelKey != null && fuelDropdown != null) {
            fuelDropdown.setSelectedFuelKey(fuelKey);
        }
        saveToCache();
    }

    @Override
    public void removed() {
        saveToCache();
        super.removed();
    }

    protected void restoreFromCache() {
        CompoundTag cached = ClientFissionDesignCache.get(designerPos);
        if (cached == null || cached.isEmpty()) {
            return;
        }
        this.grid = DesignGrid.fromTag(cached);
        this.simulator = new DesignSimulator(grid);
        this.layerScroll = 0;
        List<String> fuelKey = FissionReactorPlanItem.fuelKeyFromTag(cached);
        if (fuelKey != null && fuelDropdown != null) {
            fuelDropdown.setSelectedFuelKey(fuelKey);
        }
    }

    protected void saveToCache() {
        if (designerPos == null) {
            return;
        }
        CompoundTag tag = grid.toTag();
        List<String> fuelKey = fuelDropdown != null ? fuelDropdown.getSelectedFuelKey() : null;
        if (fuelKey != null && !fuelKey.isEmpty()) {
            tag.put(FissionReactorPlanItem.FUEL_KEY, FissionReactorPlanItem.fuelKeyToTag(fuelKey));
        }
        ClientFissionDesignCache.put(designerPos, tag);
    }

    @Override
    protected void renderBg(PoseStack graphics, float partialTicks, int mouseX, int mouseY) {
    }

    @Override
    protected void renderLabels(PoseStack graphics, int mouseX, int mouseY) {
    }

    protected record PaletteEntry(Block block, ItemStack stack, List<Component> tooltip) {
    }
}
