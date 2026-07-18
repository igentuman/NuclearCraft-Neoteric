package igentuman.nc.container;

import igentuman.nc.block.crafter.entity.EngineersCrafterBE;
import igentuman.nc.handler.crafter.AggregatedInventory;
import igentuman.nc.handler.crafter.RecipeGridLayout;
import igentuman.nc.handler.storage.ContainerSyncDispatcher;
import igentuman.nc.item.ContainerBlockItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.setup.registration.NCCrafter.ENGINEERS_CRAFTING_TABLE_BLOCK;
import static igentuman.nc.setup.registration.NCCrafter.ENGINEERS_CRAFTING_TABLE_CONTAINER;
import static igentuman.nc.util.TextUtils.__;

public class EngineersCrafterContainer extends AbstractContainerMenu {

    // Slot index layout
    private static final int CONTAINER_START = 0;
    private static final int CONTAINER_END = CONTAINER_START + EngineersCrafterBE.CONTAINER_SLOTS; // exclusive
    private static final int CRAFT_START = CONTAINER_END;
    private static final int CRAFT_END = CRAFT_START + 9; // exclusive
    private static final int RESULT_SLOT = CRAFT_END;
    private static final int PLAYER_START = RESULT_SLOT + 1;
    private static final int PLAYER_END = PLAYER_START + 36;

    /** Main player inventory (hotbar + storage); excludes armor and offhand. */
    private static final int MAIN_INV_SIZE = 36;

    public final EngineersCrafterBE blockEntity;
    private final Player player;
    private final Level level;
    private final ContainerLevelAccess access;
    private final CraftingContainer craftSlots = new CraftingContainer(this, 3, 3);
    private final ResultContainer resultSlots = new ResultContainer();
    /** UUIDs this viewer is currently subscribed to via the sync dispatcher (server-side only). */
    private final Set<UUID> subscribed = new HashSet<>();

    public EngineersCrafterContainer(int windowId, BlockPos pos, Inventory inv) {
        super(ENGINEERS_CRAFTING_TABLE_CONTAINER.get(), windowId);
        this.player = inv.player;
        this.level = player.level;
        this.blockEntity = (EngineersCrafterBE) level.getBlockEntity(pos);
        this.access = ContainerLevelAccess.create(level, pos);

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 2; col++) {
                addSlot(new SlotItemHandler(blockEntity.containerSlots, col + row * 2, 16 + col * 18, 90 + row * 18));
            }
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                addSlot(new Slot(craftSlots, col + row * 3, 100 + col * 18, 90 + row * 18));
            }
        }
        addSlot(new ResultSlot(player, craftSlots, resultSlots, 0, 168, 108));

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, 156 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inv, col, 8 + col * 18, 214));
        }
    }

    @Override
    public void slotsChanged(@NotNull Container container) {
        super.slotsChanged(container);
        if (!level.isClientSide && container == craftSlots) {
            updateResult();
        }
    }

    private void updateResult() {
        Optional<CraftingRecipe> recipe = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, craftSlots, level);
        ItemStack result = recipe
                .map(r -> r.assemble(craftSlots))
                .orElse(ItemStack.EMPTY);
        resultSlots.setItem(0, result);
        if (player instanceof ServerPlayer sp) {
            sp.connection.send(new ClientboundContainerSetSlotPacket(containerId, incrementStateId(), RESULT_SLOT, result));
        }
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player p, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();
            if (index == RESULT_SLOT) {
                access.execute((lvl, pos) -> stack.getItem().onCraftedBy(stack, lvl, p));
                if (!moveItemStackTo(stack, PLAYER_START, PLAYER_END, true)) return ItemStack.EMPTY;
                slot.onQuickCraft(stack, result);
            } else if (index >= PLAYER_START && index < PLAYER_END) {
                if (stack.getItem() instanceof ContainerBlockItem) {
                    if (!moveItemStackTo(stack, CONTAINER_START, CONTAINER_END, false)) return ItemStack.EMPTY;
                } else {
                    AggregatedInventory agg = new AggregatedInventory(blockEntity.containerSlots);
                    int moved = stack.getCount() - agg.insert(stack, false).getCount();
                    if (moved <= 0) return ItemStack.EMPTY;
                    stack.shrink(moved);
                    blockEntity.markUpdated();
                }
            } else {
                if (!moveItemStackTo(stack, PLAYER_START, PLAYER_END, false)) return ItemStack.EMPTY;
            }

            if (stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
            if (stack.getCount() == result.getCount()) return ItemStack.EMPTY;
            slot.onTake(p, stack);
            if (index == RESULT_SLOT) p.drop(stack, false);
        }
        return result;
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        if (player instanceof ServerPlayer sp && level instanceof ServerLevel serverLevel) {
            syncSubscriptions(sp, serverLevel);
        }
    }

    private void syncSubscriptions(ServerPlayer sp, ServerLevel serverLevel) {
        Set<UUID> current = new HashSet<>();
        for (int i = 0; i < blockEntity.containerSlots.getSlots(); i++) {
            ItemStack s = blockEntity.containerSlots.getStackInSlot(i);
            if (!(s.getItem() instanceof ContainerBlockItem ci)) continue;
            ci.prepareServer(s, serverLevel);
            UUID u = ContainerBlockItem.readUuid(s);
            if (u == null) continue;
            current.add(u);
            if (subscribed.add(u)) {
                ContainerSyncDispatcher.subscribe(sp, u);
                ContainerSyncDispatcher.sendSnapshot(sp, u);
            }
        }
        subscribed.removeIf(u -> {
            if (!current.contains(u)) {
                ContainerSyncDispatcher.unsubscribe(sp, u);
                return true;
            }
            return false;
        });
    }

    @Override
    public void removed(@NotNull Player p) {
        super.removed(p);
        clearContainer(p, craftSlots);
        if (p instanceof ServerPlayer sp) {
            for (UUID u : subscribed) ContainerSyncDispatcher.unsubscribe(sp, u);
            subscribed.clear();
        }
    }

    @Override
    public boolean stillValid(@NotNull Player p) {
        return stillValid(access, p, ENGINEERS_CRAFTING_TABLE_BLOCK.get());
    }

    /** The 3x3 crafting matrix slots, in row-major order — target for EMI recipe transfer. */
    public List<Slot> emiCraftMatrixSlots() {
        List<Slot> list = new ArrayList<>(9);
        for (int i = CRAFT_START; i < CRAFT_END; i++) list.add(slots.get(i));
        return list;
    }

    /** Player inventory slots — the movable item source EMI reports to the recipe tree. */
    public List<Slot> emiInputSlots() {
        List<Slot> list = new ArrayList<>(36);
        for (int i = PLAYER_START; i < PLAYER_END; i++) list.add(slots.get(i));
        return list;
    }

    public boolean canAssemble(CraftingRecipe recipe) {
        StackedContents contents = new StackedContents();
        Inventory inv = player.getInventory();
        for (int i = 0; i < MAIN_INV_SIZE; i++) {
            ItemStack s = inv.getItem(i);
            if (!s.isEmpty()) contents.accountStack(s);
        }
        new AggregatedInventory(blockEntity.containerSlots).accountInto(contents);
        return contents.canCraft(recipe, null);
    }

    public void fillCraftGrid(CraftingRecipe recipe, Player p) {
        AggregatedInventory agg = new AggregatedInventory(blockEntity.containerSlots);
        for (int i = 0; i < 9; i++) {
            ItemStack cur = craftSlots.getItem(i);
            if (cur.isEmpty()) continue;
            ItemStack leftover = agg.insert(cur, false);
            if (!leftover.isEmpty() && !p.getInventory().add(leftover)) p.drop(leftover, false);
            craftSlots.setItem(i, ItemStack.EMPTY);
        }

        Ingredient[] layout = RecipeGridLayout.layout(recipe);
        for (int i = 0; i < 9; i++) {
            Ingredient ing = layout[i];
            if (ing == null || ing.isEmpty()) continue;
            ItemStack got = takeOne(ing, p, agg);
            if (!got.isEmpty()) craftSlots.setItem(i, got);
        }
        blockEntity.markUpdated();
    }

    private ItemStack takeOne(Ingredient ingredient, Player p, AggregatedInventory agg) {
        Inventory inv = p.getInventory();
        for (int i = 0; i < MAIN_INV_SIZE; i++) {
            ItemStack s = inv.getItem(i);
            if (s.isEmpty() || !ingredient.test(s)) continue;
            ItemStack one = s.copy();
            one.setCount(1);
            s.shrink(1);
            return one;
        }
        return agg.extractIngredient(ingredient, false);
    }

    public Component getTitle() {
        return __("block." + MODID + ".engineers_crafting_table");
    }

    public BlockPos getBlockPos() {
        return blockEntity.getBlockPos();
    }
}
