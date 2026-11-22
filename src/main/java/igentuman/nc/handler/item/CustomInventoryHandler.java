package igentuman.nc.handler.item;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/** Native item inventory with journaled transfers, defensive stack access and strict current-format persistence. */
public class CustomInventoryHandler implements ResourceHandler<ItemResource>, ValueIOSerializable {
    public record SavedSlot(int slot, ItemResource resource, int amount) {
        public static final Codec<SavedSlot> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.intRange(0, Integer.MAX_VALUE).fieldOf("Slot").forGetter(SavedSlot::slot),
                ItemResource.CODEC.fieldOf("Item").forGetter(SavedSlot::resource),
                Codec.intRange(1, Integer.MAX_VALUE).fieldOf("Amount").forGetter(SavedSlot::amount)
        ).apply(instance, SavedSlot::new));
    }

    // Native ValueInput can accept partial results; malformed inventory data must never clear valid contents.
    private static final Codec<List<SavedSlot>> SLOTS_CODEC = SavedSlot.CODEC.listOf().mapResult(new Codec.ResultFunction<>() {
        private <T> DataResult<T> complete(DataResult<T> result) {
            return result.error().<DataResult<T>>map(error -> DataResult.error(error::message)).orElse(result);
        }
        @Override public <T> DataResult<Pair<List<SavedSlot>, T>> apply(DynamicOps<T> ops, T input,
                                                                      DataResult<Pair<List<SavedSlot>, T>> result) {
            return complete(result);
        }
        @Override public <T> DataResult<T> coApply(DynamicOps<T> ops, List<SavedSlot> input, DataResult<T> result) {
            return complete(result);
        }
    });

    private ItemStack[] stacks;
    private List<SlotJournal> journals;

    public CustomInventoryHandler(int size) { initialize(size); }

    private void initialize(int size) {
        if (size < 0) throw new IllegalArgumentException("Negative inventory size");
        stacks = new ItemStack[size];
        Arrays.fill(stacks, ItemStack.EMPTY);
        journals = new ArrayList<>(size);
        for (int slot = 0; slot < size; slot++) journals.add(new SlotJournal(slot));
    }

    /** Resizing clears contents; configured machine stores must retain their declared size when loading. */
    public void setSize(int size) { checkDirectMutation(); initialize(size); }
    public int getSlots() { return size(); }
    public ItemStack getStackInSlot(int slot) { validateSlotIndex(slot); return stacks[slot].copy(); }
    public int getSlotLimit(int slot) { validateSlotIndex(slot); return 64; }
    public boolean isItemValid(int slot, ItemStack stack) { validateSlotIndex(slot); return true; }

    /** Internal overwrite, including already stored oversized stacks; automation uses insert/extract instead. */
    public void setStackInSlot(int slot, ItemStack stack) {
        checkDirectMutation();
        validateSlotIndex(slot);
        ItemStack previous = stacks[slot];
        stacks[slot] = stack.copy();
        if (!sameContents(previous, stacks[slot])) onContentsChanged(slot);
    }
    /** Direct menu write compatible with native ResourceHandlerSlot. */
    public void set(int index, ItemResource resource, int amount) {
        TransferPreconditions.checkNonNegative(amount);
        if (resource.isEmpty() && amount > 0) throw new IllegalArgumentException("Empty item with positive amount");
        setStackInSlot(index, resource.toStack(amount));
    }
    public void voidSlot(int slot) { setStackInSlot(slot, ItemStack.EMPTY); }

    @Override public int size() { return stacks.length; }
    @Override public ItemResource getResource(int index) { validateSlotIndex(index); return ItemResource.of(stacks[index]); }
    @Override public long getAmountAsLong(int index) { validateSlotIndex(index); return stacks[index].getCount(); }
    @Override public long getCapacityAsLong(int index, ItemResource resource) {
        validateSlotIndex(index);
        return resource.isEmpty() ? getSlotLimit(index)
                : isValid(index, resource) ? Math.min(getSlotLimit(index), resource.getMaxStackSize()) : 0;
    }
    @Override public boolean isValid(int index, ItemResource resource) {
        validateSlotIndex(index);
        return isItemValid(index, resource.toStack(1));
    }
    @Override public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
        validateSlotIndex(index);
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        ItemStack current = stacks[index];
        if (!isValid(index, resource) || (!current.isEmpty() && !resource.matches(current))) return 0;
        int inserted = (int) Math.min(amount, Math.max(0, getCapacityAsLong(index, resource) - current.getCount()));
        if (inserted > 0) {
            journals.get(index).updateSnapshots(transaction);
            stacks[index] = resource.toStack(current.getCount() + inserted);
        }
        return inserted;
    }
    @Override public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
        validateSlotIndex(index);
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        if (!resource.matches(stacks[index])) return 0;
        int extracted = Math.min(amount, stacks[index].getCount());
        if (extracted > 0) {
            journals.get(index).updateSnapshots(transaction);
            stacks[index] = resource.toStack(stacks[index].getCount() - extracted);
        }
        return extracted;
    }

    /** Temporary local-machine convenience; external transfers must share the caller's transaction. */
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) return ItemStack.EMPTY;
        try (var transaction = Transaction.openRoot()) {
            int inserted = insert(slot, ItemResource.of(stack), stack.getCount(), transaction);
            if (!simulate) transaction.commit();
            return stack.copyWithCount(stack.getCount() - inserted);
        }
    }
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        TransferPreconditions.checkNonNegative(amount);
        ItemResource resource = getResource(slot);
        if (resource.isEmpty() || amount == 0) return ItemStack.EMPTY;
        try (var transaction = Transaction.openRoot()) {
            int extracted = extract(slot, resource, amount, transaction);
            if (!simulate) transaction.commit();
            return resource.toStack(extracted);
        }
    }

    @Override public void serialize(ValueOutput output) {
        List<SavedSlot> saved = new ArrayList<>();
        for (int slot = 0; slot < size(); slot++) {
            if (!stacks[slot].isEmpty()) saved.add(new SavedSlot(slot, getResource(slot), stacks[slot].getCount()));
        }
        output.putInt("Size", size());
        output.store("Slots", SLOTS_CODEC, saved);
    }
    @Override public void deserialize(ValueInput input) {
        checkDirectMutation();
        if (input.getIntOr("Size", -1) != size()) throw new IllegalArgumentException("Configured inventory size changed");
        List<SavedSlot> saved = input.read("Slots", SLOTS_CODEC)
                .orElseThrow(() -> new IllegalArgumentException("Invalid current-format item inventory"));
        ItemStack[] loaded = new ItemStack[size()];
        Arrays.fill(loaded, ItemStack.EMPTY);
        for (SavedSlot slot : saved) {
            if (slot.slot() >= size() || !loaded[slot.slot()].isEmpty()) {
                throw new IllegalArgumentException("Invalid or duplicate inventory Slot: " + slot.slot());
            }
            loaded[slot.slot()] = slot.resource().toStack(slot.amount());
        }
        stacks = loaded;
        onLoad();
    }
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, provider);
        serialize(output);
        return output.buildResult();
    }
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        deserialize(TagValueInput.create(ProblemReporter.DISCARDING, provider, tag));
    }

    protected void validateSlotIndex(int slot) { Objects.checkIndex(slot, size()); }
    protected void onLoad() {}
    protected void onContentsChanged(int slot) {}
    public void checkDirectMutation() {
        if (journals.stream().anyMatch(SnapshotJournal::isInTransaction)) {
            throw new IllegalStateException("Cannot overwrite item storage during a transfer");
        }
    }
    private static boolean sameContents(ItemStack first, ItemStack second) {
        return first.getCount() == second.getCount()
                && (first.isEmpty() && second.isEmpty() || ItemStack.isSameItemSameComponents(first, second));
    }
    private class SlotJournal extends SnapshotJournal<ItemStack> {
        private final int slot;
        private SlotJournal(int slot) { this.slot = slot; }
        @Override protected ItemStack createSnapshot() { return stacks[slot].copy(); }
        @Override protected void revertToSnapshot(ItemStack snapshot) { stacks[slot] = snapshot; }
        @Override protected void onRootCommit(ItemStack original) {
            if (!sameContents(original, stacks[slot])) onContentsChanged(slot);
        }
    }
    public static CustomInventoryHandler init(int size, Runnable onChanged) {
        return new CustomInventoryHandler(size) {
            @Override protected void onContentsChanged(int slot) { onChanged.run(); }
        };
    }
}
