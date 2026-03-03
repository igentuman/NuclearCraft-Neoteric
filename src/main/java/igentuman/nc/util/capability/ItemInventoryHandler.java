package igentuman.nc.util.capability;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import igentuman.api.platform.NCItemStacks;
import igentuman.api.platform.NCSerialization;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;


public class ItemInventoryHandler implements IItemHandlerModifiable, INBTSerializable<CompoundTag> {

    protected int slots;
    protected int stackSize;
    protected NonNullList<ItemStack> stacks;

    public ItemInventoryHandler(int slots, int stackSize) {
        this.slots = slots;
        this.stackSize = stackSize;
        stacks = NonNullList.withSize(slots, ItemStack.EMPTY);
    }

    @Override
    public void setStackInSlot(int slot, @NotNull ItemStack stack) {
        validateSlotIndex(slot);
        this.stacks.set(slot, stack);
    //    onContentsChanged(slot);
    }

    @Override
    public int getSlots() {
        return stacks.size();
    }

    @Override
    @NotNull
    public ItemStack getStackInSlot(int slot) {
        validateSlotIndex(slot);
        return this.stacks.get(slot);
    }
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount == 0)
            return ItemStack.EMPTY;

        validateSlotIndex(slot);

        ItemStack existing = this.stacks.get(slot);

        if (existing.isEmpty())
            return ItemStack.EMPTY;

        int toExtract = Math.min(amount, existing.getCount());

        if (existing.getCount() <= toExtract) {
            if (!simulate) {
                this.stacks.set(slot, ItemStack.EMPTY);
              //  onContentsChanged(slot);
                return existing;
            } else {
                return existing.copy();
            }
        } else {
            if (!simulate) {
                this.stacks.set(slot, NCItemStacks.copyWithCount(existing, existing.getCount() - toExtract));
               // onContentsChanged(slot);
            }

            return NCItemStacks.copyWithCount(existing, toExtract);
        }
    }

    public ItemStack insertItemInternal(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (stack.isEmpty())
            return ItemStack.EMPTY;
        if (!isItemValid(slot, stack))
            return stack;

        validateSlotIndex(slot);

        ItemStack existing = this.stacks.get(slot);

        int limit = getSlotLimit(slot);

        if (!existing.isEmpty()) {
            if (!NCItemStacks.canStack(stack, existing))
                return stack;

            limit -= existing.getCount();
        }

        if (limit <= 0)
            return stack;

        boolean reachedLimit = stack.getCount() > limit;

        if (!simulate) {
            if (existing.isEmpty()) {
                this.stacks.set(slot, reachedLimit ? NCItemStacks.copyWithCount(stack, limit) : stack);
            } else {
                existing.grow(reachedLimit ? limit : stack.getCount());
            }
        }

        return reachedLimit ? NCItemStacks.copyWithCount(stack, stack.getCount() - limit) : ItemStack.EMPTY;
    }

    @Override
    @NotNull
    public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        return insertItemInternal(slot, stack, simulate);
    }

    @Override
    public int getSlotLimit(int slot) {
        return 64;
    }

    protected int getStackLimit(int slot, @NotNull ItemStack stack) {
        return getSlotLimit(slot);
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return true;
    }

    /**
     * Custom method to save ItemStack to NBT with support for large stack counts (up to Integer.MAX_VALUE).
     * This is needed because the default ItemStack.save() method uses putByte() for count, limiting it to 127.
     * 
     * @param stack The ItemStack to save
     * @param compoundTag The NBT tag to save to
     * @return The NBT tag with the ItemStack data
     */
    private CompoundTag saveItemStackWithLargeCount(ItemStack stack, HolderLookup.Provider provider) {
        CompoundTag saveTag = (CompoundTag) NCSerialization.saveItemStack(stack, provider);
        saveTag.putInt("RealCount", stack.getCount());
        return saveTag;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        ListTag nbtTagList = new ListTag();
        for (int i = 0; i < stacks.size(); i++) {
            if (!stacks.get(i).isEmpty()) {
                CompoundTag itemTag = saveItemStackWithLargeCount(stacks.get(i), provider);
                itemTag.putInt("Slot", i);
                nbtTagList.add(itemTag);
            }
        }
        CompoundTag nbt = new CompoundTag();
        nbt.put("Items", nbtTagList);
        nbt.putInt("Size", stacks.size());
        return nbt;
    }

    public void setSize(int size) {
        stacks = NonNullList.withSize(size, ItemStack.EMPTY);
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        // Clear existing stacks first
        for (int i = 0; i < stacks.size(); i++) {
            stacks.set(i, ItemStack.EMPTY);
        }

        // Load items from NBT
        ListTag tagList = nbt.getList("Items", Tag.TAG_COMPOUND);
        for (int i = 0; i < tagList.size(); i++) {
            CompoundTag itemTags = tagList.getCompound(i);
            int slot = itemTags.getInt("Slot");

            if (slot >= 0 && slot < stacks.size()) {
                // Load ItemStack preserving large stack counts
                ItemStack stack = NCSerialization.loadItemStack(provider, itemTags);

                // Then force the count to the original value if it was larger than the item's max stack size
                if (itemTags.contains("RealCount")) {
                    int originalCount = itemTags.getInt("RealCount");
                    stack.setCount(originalCount);
                }
                stacks.set(slot, stack);
            }
        }
    }

    protected void validateSlotIndex(int slot) {
        if (slot < 0 || slot >= stacks.size())
            throw new RuntimeException("Slot " + slot + " not in valid range - [0," + stacks.size() + ")");
    }
}
