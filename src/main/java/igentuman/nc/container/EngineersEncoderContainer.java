package igentuman.nc.container;

import igentuman.nc.block_entity.crafter.EngineersCrafterBE;
import igentuman.nc.handler.crafter.CraftingPattern;
import igentuman.nc.handler.crafter.RecipeGridLayout;
import igentuman.nc.setup.entries.Crafter;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
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
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EngineersEncoderContainer extends AbstractContainerMenu {

    public static final int ENCODE_BTN = 0;

    private static final int RESULT_SLOT = 0;
    private static final int CRAFT_START = 1;
    private static final int CRAFT_END = 10;
    private static final int PATTERN_START = 10;
    private static final int PATTERN_END = PATTERN_START + EngineersCrafterBE.PATTERNS_SIZE;
    private static final int PLAYER_START = PATTERN_END;
    private static final int PLAYER_END = PLAYER_START + 36;
    private static final int BLANK_SLOT = PLAYER_END;

    public final EngineersCrafterBE blockEntity;
    private final Player player;
    private final Level level;
    private final ContainerLevelAccess access;
    private final TransientCraftingContainer craftSlots = new TransientCraftingContainer(this, 3, 3);
    private final ResultContainer resultSlots = new ResultContainer();

    public EngineersEncoderContainer(int windowId, BlockPos pos, Inventory inv) {
        super(Crafter.ENGINEERS_ENCODER_MENU.get(), windowId);
        this.player = inv.player;
        this.level = player.level();
        this.blockEntity = (EngineersCrafterBE) level.getBlockEntity(pos);
        this.access = ContainerLevelAccess.create(level, pos);

        addSlot(new Slot(resultSlots, 0, 126, 36) {
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
                addSlot(new GhostSlot(craftSlots, col + row * 3, 30 + col * 18, 18 + row * 18));
            }
        }
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new SlotItemHandler(blockEntity.patterns, col + row * 9, 8 + col * 18, 90 + row * 18));
            }
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, 168 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inv, col, 8 + col * 18, 226));
        }
        addSlot(new SlotItemHandler(blockEntity.encoderBlanks, 0, 8, 36));
    }

    @Override
    public void slotsChanged(@NotNull Container container) {
        super.slotsChanged(container);
        if (!level.isClientSide && container == craftSlots) {
            updateResult();
        }
    }

    private void updateResult() {
        CraftingInput input = craftSlots.asCraftInput();
        Optional<RecipeHolder<CraftingRecipe>> recipe =
                level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level);
        ItemStack result = recipe
                .map(r -> r.value().assemble(input, level.registryAccess()))
                .orElse(ItemStack.EMPTY);
        resultSlots.setItem(0, result);
    }

    @Override
    public boolean clickMenuButton(@NotNull Player p, int id) {
        if (id == ENCODE_BTN) {
            if (!level.isClientSide) encode();
            return true;
        }
        return false;
    }

    private void encode() {
        CraftingInput input = craftSlots.asCraftInput();
        Optional<RecipeHolder<CraftingRecipe>> recipe =
                level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level);
        if (recipe.isEmpty()) return;
        ItemStack output = recipe.get().value().assemble(input, level.registryAccess());
        if (output.isEmpty()) return;

        if (!CraftingPattern.isBlank(blockEntity.encoderBlanks.getStackInSlot(0))) return;

        ItemStack encoded = CraftingPattern.encode(craftSlots, output);
        blockEntity.encoderBlanks.extractItem(0, 1, false);
        for (int i = 0; i < blockEntity.patterns.getSlots(); i++) {
            if (blockEntity.patterns.getStackInSlot(i).isEmpty()) {
                blockEntity.patterns.setStackInSlot(i, encoded);
                craftSlots.clearContent();
                broadcastChanges();
                return;
            }
        }
        if (!player.getInventory().add(encoded)) {
            player.drop(encoded, false);
        }
        craftSlots.clearContent();
        broadcastChanges();
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
        return stillValid(access, p, Crafter.ENGINEERS_CRAFTING_TABLE_BLOCK.get());
    }

    public List<Slot> emiGhostMatrixSlots() {
        List<Slot> list = new ArrayList<>(9);
        for (int i = CRAFT_START; i < CRAFT_END; i++) list.add(slots.get(i));
        return list;
    }

    public void fillGhostGrid(CraftingRecipe recipe) {
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
    }

    public Component getTitle() {
        return Component.translatable("container.nuclearcraft.engineers_encoder");
    }

    public BlockPos getBlockPos() {
        return blockEntity.getBlockPos();
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
