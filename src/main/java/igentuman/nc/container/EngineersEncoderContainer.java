package igentuman.nc.container;

import igentuman.nc.NuclearCraft;
import igentuman.nc.block.crafter.entity.EngineersCrafterBE;
import igentuman.nc.handler.crafter.CraftingPattern;
import igentuman.nc.handler.crafter.ProcessorRecipes;
import igentuman.nc.handler.crafter.RecipeGridLayout;
import igentuman.nc.network.toClient.PacketEncoderSync;
import igentuman.nc.recipes.type.NcRecipe;
import igentuman.nc.setup.registration.NCCrafter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EngineersEncoderContainer extends AbstractContainerMenu {

    public static final int ENCODE_BTN = 0;
    public static final int MODE_BTN = 1;

    public static final int MODE_CRAFTING = 0;
    public static final int MODE_PROCESSING = 1;

    public static final int PATTERN_COLS = 9;
    public static final int PATTERN_ROWS = EngineersCrafterBE.PATTERNS_SIZE / PATTERN_COLS;
    public static final int PATTERN_Y = 100;
    public static final int PLAYER_Y = PATTERN_Y + PATTERN_ROWS * 18 + 6;

    public static final int GHOST_X = 30;
    public static final int GHOST_Y = 18;
    public static final int RESULT_X = 126;
    public static final int RESULT_Y = 36;
    public static final int BLANK_X = 8;
    public static final int BLANK_Y = 36;

    private static final int RESULT_SLOT = 0;
    private static final int CRAFT_START = 1;
    private static final int CRAFT_END = 10;
    public static final int PATTERN_START = 10;
    public static final int PATTERN_END = PATTERN_START + EngineersCrafterBE.PATTERNS_SIZE;
    public static final int PLAYER_START = PATTERN_END;
    public static final int PLAYER_END = PLAYER_START + 36;
    private static final int BLANK_SLOT = PLAYER_END;

    public final EngineersCrafterBE blockEntity;
    private final Player player;
    private final Level level;
    private final ContainerLevelAccess access;
    private final TransientCraftingContainer craftSlots = new TransientCraftingContainer(this, 3, 3);
    private final ResultContainer resultSlots = new ResultContainer();

    private int mode = MODE_CRAFTING;
    @Nullable
    private NcRecipe selected;
    private boolean suppressSearch;
    private boolean synced;

    @Nullable
    private ResourceLocation clientRecipeId;

    public EngineersEncoderContainer(int windowId, BlockPos pos, Inventory inv) {
        super(NCCrafter.ENGINEERS_ENCODER_CONTAINER.get(), windowId);
        this.player = inv.player;
        this.level = player.level();
        this.blockEntity = (EngineersCrafterBE) level.getBlockEntity(pos);
        this.access = ContainerLevelAccess.create(level, pos);

        addSlot(new Slot(resultSlots, 0, RESULT_X, RESULT_Y) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;
            }

            @Override
            public boolean mayPickup(@NotNull Player p) {
                return false;
            }
        });
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                addSlot(new GhostSlot(craftSlots, col + row * 3, GHOST_X + col * 18, GHOST_Y + row * 18));
            }
        }
        for (int row = 0; row < PATTERN_ROWS; row++) {
            for (int col = 0; col < PATTERN_COLS; col++) {
                addSlot(new PatternSlot(blockEntity.patterns, col + row * PATTERN_COLS, 8 + col * 18, PATTERN_Y + row * 18));
            }
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, PLAYER_Y + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inv, col, 8 + col * 18, PLAYER_Y + 58));
        }
        addSlot(new SlotItemHandler(blockEntity.encoderBlanks, 0, BLANK_X, BLANK_Y));
    }

    public int mode() {
        return mode;
    }

    public boolean isProcessingMode() {
        return mode == MODE_PROCESSING;
    }

    @Nullable
    public ResourceLocation selectedRecipeId() {
        if (level.isClientSide) return clientRecipeId;
        return selected == null ? null : selected.getId();
    }

    @Nullable
    public NcRecipe selectedRecipe() {
        if (level.isClientSide) {
            return clientRecipeId == null ? null : ProcessorRecipes.byId(level.getRecipeManager(), clientRecipeId);
        }
        return selected == null ? null : selected;
    }

    public void applySync(int mode, @Nullable ResourceLocation recipeId) {
        this.mode = mode;
        this.clientRecipeId = recipeId;
    }

    private void sync() {
        if (player instanceof ServerPlayer sp) {
            NuclearCraft.packetHandler().sendTo(new PacketEncoderSync(containerId, mode, selected == null ? null : selected.getId()), sp);
        }
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        if (!synced && !level.isClientSide) {
            synced = true;
            sync();
        }
    }

    @Override
    public void slotsChanged(@NotNull Container container) {
        super.slotsChanged(container);
        if (!level.isClientSide && container == craftSlots) {
            if (mode == MODE_PROCESSING && !suppressSearch) {
                searchProcessingRecipe();
            }
            updateResult();
        }
    }

    private void searchProcessingRecipe() {
        List<ItemStack> ghosts = new ArrayList<>(9);
        for (int i = 0; i < 9; i++) ghosts.add(craftSlots.getItem(i));
        NcRecipe found = ProcessorRecipes.matchByItems(level.getRecipeManager(), ghosts);
        if (found != selected) {
            selected = found;
            sync();
        }
    }

    private Optional<CraftingRecipe> craftingRecipe() {
        var input = craftSlots;
        return level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level);
    }

    private void updateResult() {
        ItemStack result;
        if (mode == MODE_PROCESSING) {
            result = selected == null ? ItemStack.EMPTY : selected.getResultItem(level.registryAccess());
        } else {
            var input = craftSlots;
            result = craftingRecipe()
                    .map(r -> r.assemble(input, level.registryAccess()))
                    .orElse(ItemStack.EMPTY);
        }
        resultSlots.setItem(0, result);
    }

    @Override
    public boolean clickMenuButton(@NotNull Player p, int id) {
        if (id == ENCODE_BTN) {
            if (!level.isClientSide) encode();
            return true;
        }
        if (id == MODE_BTN) {
            if (!level.isClientSide) toggleMode();
            return true;
        }
        return false;
    }

    private void toggleMode() {
        mode = mode == MODE_CRAFTING ? MODE_PROCESSING : MODE_CRAFTING;
        selected = null;
        if (mode == MODE_PROCESSING) searchProcessingRecipe();
        updateResult();
        sync();
        broadcastChanges();
    }

    private void encode() {
        ItemStack encoded;
        if (mode == MODE_PROCESSING) {
            if (selected == null) return;
            encoded = CraftingPattern.encodeProcessing(selected, selected.getId());
        } else {
            var input = craftSlots;
            Optional<CraftingRecipe> recipe = craftingRecipe();
            if (recipe.isEmpty()) return;
            ItemStack output = recipe.get().assemble(input, level.registryAccess());
            if (output.isEmpty()) return;
            encoded = CraftingPattern.encode(craftSlots, output);
        }
        if (CraftingPattern.from(encoded) == null) return;
        if (!CraftingPattern.isBlank(blockEntity.encoderBlanks.getStackInSlot(0))) return;

        blockEntity.encoderBlanks.extractItem(0, 1, false);
        for (int i = 0; i < blockEntity.patterns.getSlots(); i++) {
            if (blockEntity.patterns.getStackInSlot(i).isEmpty()) {
                blockEntity.patterns.setStackInSlot(i, encoded);
                clearSelection();
                broadcastChanges();
                return;
            }
        }
        if (!player.getInventory().add(encoded)) {
            player.drop(encoded, false);
        }
        clearSelection();
        broadcastChanges();
    }

    private void clearSelection() {
        suppressSearch = true;
        craftSlots.clearContent();
        suppressSearch = false;
        selected = null;
        updateResult();
        sync();
    }

    @Override
    public void clicked(int slotId, int button, @NotNull ClickType clickType, @NotNull Player p) {
        if (slotId >= CRAFT_START && slotId < CRAFT_END
                && (clickType == ClickType.PICKUP || clickType == ClickType.PICKUP_ALL)) {
            ItemStack carried = getCarried();
            if (carried.isEmpty()) {
                craftSlots.setItem(slotId - CRAFT_START, ItemStack.EMPTY);
            } else {
                ItemStack ghost = carried.copy();
                ghost.setCount(1);
                craftSlots.setItem(slotId - CRAFT_START, ghost);
            }
            broadcastChanges();
            return;
        }
        super.clicked(slotId, button, clickType, p);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player p, int index) {
        if (index >= CRAFT_START && index < CRAFT_END) return ItemStack.EMPTY;

        Slot slot = slots.get(index);
        if (slot == null || !slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();

        if (index >= PATTERN_START && index < PATTERN_END) {
            if (!moveItemStackTo(stack, PLAYER_START, PLAYER_END, true)) return ItemStack.EMPTY;
        } else if (index == BLANK_SLOT) {
            if (!moveItemStackTo(stack, PLAYER_START, PLAYER_END, true)) return ItemStack.EMPTY;
        } else if (CraftingPattern.isEncoded(stack)) {
            if (!moveItemStackTo(stack, PATTERN_START, PATTERN_END, false)) return ItemStack.EMPTY;
        } else if (CraftingPattern.isBlank(stack)) {
            if (!moveItemStackTo(stack, BLANK_SLOT, BLANK_SLOT + 1, false)) return ItemStack.EMPTY;
        } else {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (stack.getCount() == copy.getCount()) return ItemStack.EMPTY;
        slot.onTake(p, stack);
        return copy;
    }

    @Override
    public void removed(@NotNull Player p) {
        super.removed(p);
        craftSlots.clearContent();
    }

    @Override
    public boolean stillValid(@NotNull Player p) {
        return stillValid(access, p, NCCrafter.ENGINEERS_CRAFTING_TABLE_BLOCK.get());
    }

    public List<Slot> emiGhostMatrixSlots() {
        List<Slot> list = new ArrayList<>(9);
        for (int i = CRAFT_START; i < CRAFT_END; i++) list.add(slots.get(i));
        return list;
    }

    public void fillGhostGrid(CraftingRecipe recipe) {
        mode = MODE_CRAFTING;
        selected = null;
        suppressSearch = true;
        Ingredient[] layout = RecipeGridLayout.layout(recipe);
        for (int i = 0; i < 9; i++) {
            Ingredient ing = layout[i];
            if (ing == null || ing.isEmpty()) {
                craftSlots.setItem(i, ItemStack.EMPTY);
                continue;
            }
            ItemStack[] items = ing.getItems();
            ItemStack rep = items.length > 0 ? items[0].copy() : ItemStack.EMPTY;
            if (!rep.isEmpty()) rep.setCount(1);
            craftSlots.setItem(i, rep);
        }
        suppressSearch = false;
        updateResult();
        sync();
    }

    public void setProcessingRecipe(NcRecipe holder) {
        if (!ProcessorRecipes.isAllowed(holder.getCodeId()) || holder.isIncomplete()) return;
        mode = MODE_PROCESSING;
        selected = holder;
        suppressSearch = true;
        craftSlots.clearContent();
        int i = 0;
        for (var si : holder.getInputItems()) {
            if (i >= 9) break;
            ItemStack[] items = si.getRepresentations().toArray(ItemStack[]::new);
            if (items.length == 0) continue;
            ItemStack rep = items[0].copy();
            rep.setCount(1);
            craftSlots.setItem(i++, rep);
        }
        suppressSearch = false;
        updateResult();
        sync();
    }

    public Component getTitle() {
        return Component.translatable("container.nuclearcraft.engineers_encoder");
    }

    public BlockPos getBlockPos() {
        return blockEntity.getBlockPos();
    }

    public static class PatternSlot extends SlotItemHandler {
        public boolean visible = true;

        PatternSlot(IItemHandler handler, int index, int x, int y) {
            super(handler, index, x, y);
        }

        @Override
        public boolean isActive() {
            return visible;
        }
    }

    private static class GhostSlot extends Slot {
        GhostSlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(@NotNull ItemStack stack) {
            return false;
        }

        @Override
        public boolean mayPickup(@NotNull Player p) {
            return false;
        }
    }
}
