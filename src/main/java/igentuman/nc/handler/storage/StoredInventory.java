package igentuman.nc.handler.storage;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

public class StoredInventory {

    private int size;
    private NonNullList<ItemStack> stacks;

    public StoredInventory(int size) {
        this.size = Math.max(0, size);
        this.stacks = NonNullList.withSize(this.size, ItemStack.EMPTY);
    }

    public int size() {
        return size;
    }

    public ItemStack get(int slot) {
        return (slot >= 0 && slot < size) ? stacks.get(slot) : ItemStack.EMPTY;
    }

    public void set(int slot, ItemStack stack) {
        if (slot >= 0 && slot < size) {
            stacks.set(slot, stack);
        }
    }

    public void ensureSize(int newSize) {
        if (newSize <= size) return;
        NonNullList<ItemStack> grown = NonNullList.withSize(newSize, ItemStack.EMPTY);
        for (int i = 0; i < size; i++) grown.set(i, stacks.get(i));
        stacks = grown;
        size = newSize;
    }

    public void read(HolderLookup.Provider provider, CompoundTag tag) {
        for (int i = 0; i < size; i++) stacks.set(i, ItemStack.EMPTY);
        ListTag list = tag.getList("Items", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag itemTags = list.getCompound(i);
            int slot = itemTags.getInt("Slot");
            if (slot < 0 || slot >= size) continue;
            ItemStack stack = ItemStack.parseOptional(provider, itemTags);
            if (itemTags.contains("RealCount")) {
                stack.setCount(itemTags.getInt("RealCount"));
            }
            stacks.set(slot, stack);
        }
    }

    public CompoundTag save(HolderLookup.Provider provider) {
        ListTag list = new ListTag();
        for (int i = 0; i < size; i++) {
            ItemStack stack = stacks.get(i);
            if (stack.isEmpty()) continue;
            CompoundTag itemTag = (CompoundTag) stack.copyWithCount(1).save(provider);
            itemTag.putInt("Slot", i);
            itemTag.putInt("RealCount", stack.getCount());
            list.add(itemTag);
        }
        CompoundTag tag = new CompoundTag();
        tag.put("Items", list);
        tag.putInt("Size", size);
        return tag;
    }

    public static StoredInventory load(HolderLookup.Provider provider, CompoundTag tag, int fallbackSize) {
        int size = tag.contains("Size", Tag.TAG_INT) ? tag.getInt("Size") : fallbackSize;
        StoredInventory inv = new StoredInventory(size);
        inv.read(provider, tag);
        return inv;
    }
}
