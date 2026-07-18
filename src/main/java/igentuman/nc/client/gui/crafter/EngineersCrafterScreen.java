package igentuman.nc.client.gui.crafter;

import igentuman.nc.NuclearCraft;
import igentuman.nc.block.crafter.entity.EngineersCrafterBE;
import igentuman.nc.client.gui.element.NCGuiElement;
import igentuman.nc.client.gui.element.button.Checkbox;
import igentuman.nc.client.gui.element.button.NCImageButton;
import igentuman.nc.client.gui.element.slot.NormalSlot;
import igentuman.nc.container.EngineersCrafterContainer;
import igentuman.nc.handler.crafter.AggregatedInventory;
import igentuman.nc.handler.crafter.AggregatedInventory.Entry;
import igentuman.nc.handler.crafter.AggregatedInventory.ItemKey;
import igentuman.nc.handler.crafter.CraftingPattern;
import igentuman.nc.network.toServer.PacketCrafterExtract;
import igentuman.nc.network.toServer.PacketCrafterInsert;
import igentuman.nc.network.toServer.PacketGuiButtonPress;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static igentuman.nc.util.TextUtils.__;
import static igentuman.nc.util.TextUtils.numberFormat;

public class EngineersCrafterScreen extends AbstractContainerScreen<EngineersCrafterContainer> {

    private static final ResourceLocation RECIPE_BUTTON_TEXTURE = new ResourceLocation("textures/gui/recipe_button.png");

    private static final int MODE_STORED = 0;
    private static final int MODE_CRAFTABLE = 1;
    private static final int MODE_BOTH = 2;

    private static final int GRID_COLS = 9;
    private static final int BASE_GRID_ROWS = 3;
    private static final int MAX_EXTRA_ROWS = 9;
    private static final int BASE_IMAGE_HEIGHT = 238;
    private static final int VERTICAL_MARGIN = 20;

    private final List<NCGuiElement> widgets = new ArrayList<>();
    private ScrollableItemGrid grid;
    private Checkbox energyCheckbox;
    private Button modeButton;
    private EditBox searchBox;
    private String searchText = "";
    private int mode = MODE_BOTH;
    private int gridRows = BASE_GRID_ROWS;
    private int contentShift = 0;
    private int appliedSlotShift = 0;

    public EngineersCrafterScreen(EngineersCrafterContainer container, Inventory inv, Component name) {
        super(container, inv, name);
        imageWidth = 194;
        imageHeight = BASE_IMAGE_HEIGHT;
    }

    @Override
    protected void init() {
        int extraRows = Mth.clamp((height - BASE_IMAGE_HEIGHT - VERTICAL_MARGIN * 2) / 18, 0, MAX_EXTRA_ROWS);
        gridRows = BASE_GRID_ROWS + extraRows;
        contentShift = extraRows * 18;
        imageHeight = BASE_IMAGE_HEIGHT + contentShift;

        super.init();

        if (contentShift != appliedSlotShift) {
            int dy = contentShift - appliedSlotShift;
            for (Slot s : menu.slots) s.y += dy;
            appliedSlotShift = contentShift;
        }

        widgets.clear();
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 2; col++) {
                widgets.add(new NormalSlot(16 + col * 18, 90 + contentShift + row * 18, "item"));
            }
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                widgets.add(new NormalSlot(100 + col * 18, 90 + contentShift + row * 18, "item"));
            }
        }
        widgets.add(new NormalSlot(168, 108 + contentShift, "item_out"));
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                widgets.add(new NormalSlot(8 + col * 18, 156 + contentShift + row * 18, "item"));
            }
        }
        for (int col = 0; col < 9; col++) {
            widgets.add(new NormalSlot(8 + col * 18, 214 + contentShift, "item"));
        }

        grid = new ScrollableItemGrid(8, 32, GRID_COLS, gridRows, this::buildCells);
        grid.originX = leftPos;
        grid.originY = topPos;

        modeButton = addRenderableWidget(new Button(leftPos - 18, topPos + 16, 18, 18, modeLabel(), b -> {
                    mode = (mode + 1) % 3;
                    modeButton.setMessage(modeLabel());
                }));

        NCImageButton encoderBtn = addRenderableWidget(new NCImageButton(leftPos + 77, topPos + 88 + contentShift, 20, 18, 0, 0, 19, RECIPE_BUTTON_TEXTURE, 256, 256,
                b -> NuclearCraft.packetHandler().sendToServer(
                        new PacketGuiButtonPress(menu.getBlockPos(), EngineersCrafterBE.OPEN_ENCODER_BTN))));
        // tooltip not supported in 1.19.2

        searchBox = new EditBox(font, leftPos + 8, topPos + 16, 162, 14, Component.literal(""));
searchBox.setMaxLength(50);
        searchBox.setResponder(text -> searchText = text.toLowerCase(java.util.Locale.ROOT));
        addRenderableWidget(searchBox);
        setFocused(searchBox);

        NCGuiElement.RELATIVE_X = leftPos;
        NCGuiElement.RELATIVE_Y = topPos;
        energyCheckbox = new Checkbox(imageWidth - 15, 5, this, hasEnergy());
    }

    private boolean hasEnergy() {
        return menu.blockEntity.energy.getEnergyStored() > 0;
    }

    private List<Component> energyTooltip() {
        int stored = menu.blockEntity.energy.getEnergyStored();
        int capacity = menu.blockEntity.energy.getMaxEnergyStored();
        return List.<Component>of(
                __("tooltip.nc.energy.per_tick", numberFormat(EngineersCrafterBE.PASSIVE_FE)),
                __("tooltip.nc.energy_stored", numberFormat(stored), numberFormat(capacity)));
    }

    private Component modeLabel() {
        String key = switch (mode) {
            case MODE_CRAFTABLE -> "gui.nc.crafter.mode.craftable";
            case MODE_BOTH -> "gui.nc.crafter.mode.both";
            default -> "gui.nc.crafter.mode.stored";
        };
        return __("gui.nc.crafter.view", __(key));
    }

    private List<ScrollableItemGrid.Cell> buildCells() {
        AggregatedInventory agg = new AggregatedInventory(menu.blockEntity.containerSlots);
        List<Entry> stored = agg.entries();
        LinkedHashMap<ItemKey, ItemStack> craftable = craftableOutputs();
        List<ScrollableItemGrid.Cell> out = new ArrayList<>();

        if (mode != MODE_CRAFTABLE) {
            for (Entry e : stored) {
                out.add(new ScrollableItemGrid.Cell(e.stack(), e.count(), craftable.containsKey(e.key())));
            }
        }

        if (mode != MODE_STORED) {
            Set<ItemKey> occupied = new LinkedHashSet<>();
            if (mode == MODE_BOTH) {
                for (Entry e : stored) occupied.add(e.key());
            }
            for (var en : craftable.entrySet()) {
                if (occupied.contains(en.getKey())) continue;
                out.add(new ScrollableItemGrid.Cell(en.getValue(), 0, true));
            }
        }
        if (!searchText.isEmpty()) {
            out = out.stream()
                    .filter(c -> c.stack().getHoverName().getString().toLowerCase(java.util.Locale.ROOT).contains(searchText))
                    .collect(Collectors.toList());
        }
        return out;
    }

    private LinkedHashMap<ItemKey, ItemStack> craftableOutputs() {
        LinkedHashMap<ItemKey, ItemStack> map = new LinkedHashMap<>();
        ItemStackHandler patterns = menu.blockEntity.patterns;
        for (int i = 0; i < patterns.getSlots(); i++) {
            CraftingPattern pattern = CraftingPattern.from(patterns.getStackInSlot(i));
            if (pattern == null) continue;
            ItemStack output = pattern.output();
            if (output.isEmpty()) continue;
            ItemKey key = ItemKey.of(output);
            if (map.containsKey(key)) continue;
            ItemStack icon = output.copy();
            icon.setCount(1);
            map.put(key, icon);
        }
        return map;
    }

    @Override
    public void render(@NotNull PoseStack graphics, int mouseX, int mouseY, float partialTicks) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTicks);
        renderTooltip(graphics, mouseX, mouseY);
        if (grid != null) grid.renderTooltip(graphics, mouseX, mouseY);
        if (energyCheckbox != null && energyCheckbox.isMouseOver(mouseX - leftPos, mouseY - topPos)) {
            renderTooltip(graphics, energyTooltip(), java.util.Optional.empty(), mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (grid != null && grid.isInBounds(mouseX, mouseY)) {
            if (!menu.getCarried().isEmpty()) {
                NuclearCraft.packetHandler().sendToServer(new PacketCrafterInsert(menu.getBlockPos(), button == 1));
                return true;
            }
            ScrollableItemGrid.Cell cell = grid.hovered((int) mouseX, (int) mouseY);
            if (cell != null && !cell.stack().isEmpty()) {
                if (cell.stored() > 0) {
                    NuclearCraft.packetHandler().sendToServer(
                            new PacketCrafterExtract(menu.getBlockPos(), cell.stack(), button == 1, hasShiftDown()));
                } else if (cell.craftable()) {
                    minecraft.setScreen(new CraftConfirmScreen(this, menu.getBlockPos(), cell.stack().copy()));
                }
            }
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (searchBox != null && searchBox.keyPressed(keyCode, scanCode, modifiers)) return true;
        if (searchBox != null && searchBox.isFocused() && keyCode != 256) return true;
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char c, int modifiers) {
        if (searchBox != null && searchBox.charTyped(c, modifiers)) return true;
        return super.charTyped(c, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (grid != null && grid.onScroll(mouseX, mouseY, delta)) return true;
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    protected void renderBg(@NotNull PoseStack graphics, float partialTicks, int mouseX, int mouseY) {
        EngineersEncoderScreen.drawPanel(graphics, leftPos, topPos, imageWidth, imageHeight);
        NCGuiElement.RELATIVE_X = leftPos;
        NCGuiElement.RELATIVE_Y = topPos;
        for (NCGuiElement w : widgets) {
            w.draw(graphics, mouseX, mouseY, partialTicks);
        }
        if (energyCheckbox != null) {
            energyCheckbox.setChecked(hasEnergy()).draw(graphics, mouseX, mouseY, partialTicks);
        }
        if (grid != null) {
            grid.originX = leftPos;
            grid.originY = topPos;
            grid.refresh();
            grid.draw(graphics, mouseX, mouseY, partialTicks);
        }
        int total = menu.blockEntity.craftOpTotal;
        if (total > 0) {
            int barX = leftPos + 8;
            int barY = topPos + 31;
            int barW = 162;
            int barH = 4;
            int filled = (int) (barW * menu.blockEntity.craftOpIndex / (float) total);
            fill(graphics, barX, barY, barX + barW, barY + barH, 0xFF1A1A1A);
            if (filled > 0) {
                fill(graphics, barX, barY, barX + filled, barY + barH, 0xFF22BB22);
            }
        }
    }

    @Override
    protected void renderLabels(@NotNull PoseStack graphics, int mouseX, int mouseY) {
        drawString(graphics, font, title, titleLabelX, titleLabelY, 0x404040);
    }
}
