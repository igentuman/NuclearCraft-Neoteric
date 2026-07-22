package igentuman.nc.screen;

import igentuman.nc.container.EngineersCrafterContainer;
import igentuman.nc.client.crafter.PendingCraftDenied;
import igentuman.nc.handler.crafter.AggregatedInventory;
import igentuman.nc.handler.crafter.AggregatedInventory.Entry;
import igentuman.nc.handler.crafter.AggregatedInventory.ItemKey;
import igentuman.nc.handler.crafter.CraftingPattern;
import igentuman.nc.network.PacketCrafterExtract;
import igentuman.nc.network.PacketCrafterInsert;
import igentuman.nc.network.PacketCrafterOpenEncoder;
import igentuman.nc.screen.element.EnergyBar;
import igentuman.nc.screen.element.NcPanel;
import igentuman.nc.screen.element.ScrollableItemGrid;
import igentuman.nc.screen.element.ScrollableItemGrid.Cell;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.network.PacketDistributor;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class EngineersCrafterScreen extends AbstractContainerScreen<EngineersCrafterContainer> {

    private static final int MODE_STORED = 0;
    private static final int MODE_CRAFTABLE = 1;
    private static final int MODE_BOTH = 2;

    private static final int GRID_COLS = 9;
    private static final int GRID_ROWS = 3;
    private static final int SEARCH_Y = 16;
    private static final int PROGRESS_Y = 31;
    private static final int GRID_Y = 36;

    private static final int BASE_IMAGE_HEIGHT = 238;
    private static final int MAX_EXTRA_ROWS = 9;
    private static final int VERTICAL_MARGIN = 20;
    private static final int SECTION_MARGIN = 6;

    private static final Field SLOT_Y = resolveSlotY();

    private static Field resolveSlotY() {
        try {
            Field f = Slot.class.getField("y");
            f.setAccessible(true);
            return f;
        } catch (ReflectiveOperationException | RuntimeException e) {
            return null;
        }
    }

    private ScrollableItemGrid grid;
    private EditBox searchBox;
    private Button modeButton;
    private String searchText = "";
    private int mode = MODE_BOTH;
    private int gridRows = GRID_ROWS;
    private int contentShift = 0;
    private int appliedSlotShift = 0;

    public EngineersCrafterScreen(EngineersCrafterContainer menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 194;
        this.imageHeight = BASE_IMAGE_HEIGHT;
    }

    @Override
    protected void init() {
        int extraRows = SLOT_Y == null ? 0
                : Mth.clamp((height - BASE_IMAGE_HEIGHT - SECTION_MARGIN - VERTICAL_MARGIN * 2) / 18, 0, MAX_EXTRA_ROWS);
        gridRows = GRID_ROWS + extraRows;
        contentShift = extraRows * 18 + SECTION_MARGIN;
        imageHeight = BASE_IMAGE_HEIGHT + contentShift;

        super.init();

        if (contentShift != appliedSlotShift) {
            shiftSlots(contentShift - appliedSlotShift);
            appliedSlotShift = contentShift;
        }

        grid = new ScrollableItemGrid(leftPos + 8, topPos + GRID_Y, GRID_COLS, gridRows, this::buildCells);
        addRenderableWidget(grid);

        searchBox = new EditBox(font, leftPos + 8, topPos + SEARCH_Y, 150, 14, Component.empty());
        searchBox.setMaxLength(50);
        searchBox.setHint(Component.translatable("screen.nuclearcraft.crafter.search"));
        searchBox.setResponder(t -> searchText = t.toLowerCase(Locale.ROOT));
        searchBox.setValue(searchText);
        setInitialFocus(searchBox);
        addRenderableWidget(searchBox);

        modeButton = Button.builder(modeLabelShort(), b -> {
            mode = (mode + 1) % 3;
            b.setMessage(modeLabelShort());
            b.setTooltip(Tooltip.create(modeLabelFull()));
        }).bounds(leftPos + 160, topPos + 15, 26, 16).tooltip(Tooltip.create(modeLabelFull())).build();
        addRenderableWidget(modeButton);

        WidgetSprites encoderSprites = new WidgetSprites(
                ResourceLocation.withDefaultNamespace("recipe_book/button"),
                ResourceLocation.withDefaultNamespace("recipe_book/button_highlighted"));
        ImageButton encoderButton = new ImageButton(leftPos + 55, topPos + 92 + contentShift, 20, 18, encoderSprites,
                b -> PacketDistributor.sendToServer(new PacketCrafterOpenEncoder(menu.getBlockPos())),
                Component.translatable("screen.nuclearcraft.crafter.encoder"));
        encoderButton.setTooltip(Tooltip.create(Component.translatable("screen.nuclearcraft.crafter.encoder")));
        addRenderableWidget(encoderButton);

        EnergyBar energyBar = new EnergyBar(leftPos + 80, topPos + 92 + contentShift, 12, 52,
                () -> menu.blockEntity.energy);
        addRenderableWidget(energyBar);
    }

    private void shiftSlots(int dy) {
        if (dy == 0 || SLOT_Y == null) return;
        try {
            for (Slot s : menu.slots) SLOT_Y.setInt(s, s.y + dy);
        } catch (IllegalAccessException ignored) {
        }
    }

    private List<Cell> buildCells() {
        AggregatedInventory agg = new AggregatedInventory(menu.blockEntity.containerSlots);
        List<Entry> stored = agg.entries();
        LinkedHashMap<ItemKey, ItemStack> craftable = craftableOutputs();
        List<Cell> out = new ArrayList<>();

        if (mode != MODE_CRAFTABLE) {
            for (Entry e : stored) {
                out.add(new Cell(e.stack(), e.count(), craftable.containsKey(e.key())));
            }
        }
        if (mode != MODE_STORED) {
            Set<ItemKey> occupied = new LinkedHashSet<>();
            if (mode == MODE_BOTH) {
                for (Entry e : stored) occupied.add(e.key());
            }
            for (var en : craftable.entrySet()) {
                if (occupied.contains(en.getKey())) continue;
                out.add(new Cell(en.getValue(), 0, true));
            }
        }
        if (!searchText.isEmpty()) {
            out.removeIf(c -> !c.stack().getHoverName().getString().toLowerCase(Locale.ROOT).contains(searchText));
        }
        return out;
    }

    private LinkedHashMap<ItemKey, ItemStack> craftableOutputs() {
        LinkedHashMap<ItemKey, ItemStack> map = new LinkedHashMap<>();
        ItemStackHandler patterns = menu.blockEntity.patterns;
        for (int i = 0; i < patterns.getSlots(); i++) {
            CraftingPattern p = CraftingPattern.from(patterns.getStackInSlot(i));
            if (p == null) continue;
            ItemStack output = p.output();
            if (output.isEmpty()) continue;
            map.computeIfAbsent(ItemKey.of(output), k -> {
                ItemStack icon = output.copy();
                icon.setCount(1);
                return icon;
            });
        }
        return map;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTicks) {
        if (PendingCraftDenied.pending) {
            List<ItemStack> items = new ArrayList<>(PendingCraftDenied.items);
            List<Integer> amounts = new ArrayList<>(PendingCraftDenied.amounts);
            boolean tooComplex = PendingCraftDenied.tooComplex;
            PendingCraftDenied.clear();
            minecraft.setScreen(new CraftDeniedScreen(this, items, amounts, tooComplex));
            return;
        }
        super.render(g, mouseX, mouseY, partialTicks);
        renderTooltip(g, mouseX, mouseY);
        if (grid.isInBounds(mouseX, mouseY)) {
            Cell cell = grid.hovered(mouseX, mouseY);
            if (cell != null && !cell.stack().isEmpty()) {
                List<Component> tip = new ArrayList<>(getTooltipFromItem(minecraft, cell.stack()));
                if (cell.stored() > 0) {
                    tip.add(Component.translatable("screen.nuclearcraft.crafter.stored", cell.stored()));
                }
                if (cell.craftable()) {
                    tip.add(Component.translatable("screen.nuclearcraft.crafter.craftable_tt"));
                }
                g.renderComponentTooltip(font, tip, mouseX, mouseY);
            }
        }
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTicks, int mouseX, int mouseY) {
        NcPanel.drawPanel(g, leftPos, topPos, imageWidth, imageHeight);
        for (Slot s : menu.slots) {
            if (s instanceof ResultSlot) {
                NcPanel.drawOutputSlot(g, leftPos + s.x, topPos + s.y);
            } else {
                NcPanel.drawSlot(g, leftPos + s.x, topPos + s.y);
            }
        }
        int total = menu.blockEntity.craftOpTotal;
        if (total > 0) {
            int idx = Math.min(menu.blockEntity.craftOpIndex, total);
            int x = leftPos + 8;
            int y = topPos + PROGRESS_Y;
            g.fill(x, y, x + 162, y + 3, 0xFF1A1A1A);
            g.fill(x, y, x + 162 * idx / total, y + 3, 0xFF22BB22);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(font, title, (imageWidth - font.width(title)) / 2, 5, 0x404040, false);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (grid.isInBounds(mx, my)) {
            handleGridClick(mx, my, button);
            return true;
        }
        return super.mouseClicked(mx, my, button);
    }

    private void handleGridClick(double mx, double my, int button) {
        BlockPos pos = menu.getBlockPos();
        if (!menu.getCarried().isEmpty()) {
            PacketDistributor.sendToServer(new PacketCrafterInsert(pos, button == 1));
            return;
        }
        Cell cell = grid.hovered(mx, my);
        if (cell == null || cell.stack().isEmpty()) return;
        if (cell.stored() > 0) {
            PacketDistributor.sendToServer(new PacketCrafterExtract(pos, cell.stack().copy(), button == 1, hasShiftDown()));
        } else if (cell.craftable()) {
            minecraft.setScreen(new CraftConfirmScreen(this, pos, cell.stack().copy()));
        }
    }

    @Override
    public boolean keyPressed(int key, int scan, int mods) {
        if (key == 256) return super.keyPressed(key, scan, mods);
        if (searchBox.isFocused() && searchBox.keyPressed(key, scan, mods)) return true;
        return super.keyPressed(key, scan, mods);
    }

    @Override
    public boolean charTyped(char c, int mods) {
        if (searchBox.isFocused() && searchBox.charTyped(c, mods)) return true;
        return super.charTyped(c, mods);
    }

    private Component modeLabelShort() {
        return Component.literal(switch (mode) {
            case MODE_STORED -> "S";
            case MODE_CRAFTABLE -> "C";
            default -> "B";
        });
    }

    private Component modeLabelFull() {
        String key = switch (mode) {
            case MODE_STORED -> "screen.nuclearcraft.crafter.mode.stored";
            case MODE_CRAFTABLE -> "screen.nuclearcraft.crafter.mode.craftable";
            default -> "screen.nuclearcraft.crafter.mode.both";
        };
        return Component.translatable("screen.nuclearcraft.crafter.view", Component.translatable(key));
    }
}
