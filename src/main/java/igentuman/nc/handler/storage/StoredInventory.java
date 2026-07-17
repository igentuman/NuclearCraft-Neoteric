package igentuman.nc.handler.storage;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

/**
 * Backing contents for one container UUID: a fixed list of stacks plus its size. Serialization uses
 * the {@code RealCount} trick (mirrors the old item/block NBT layout) so stack counts above 127 and
 * the legacy on-disk format round-trip cleanly.
 */
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

    public NonNullList<ItemStack> stacks() {
        return stacks;
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

    /** Reads a {@code {Items:[{Slot,..,RealCount?}]}} tag into the current slots; ignores stored Size. */
    public void read(CompoundTag tag) {
        for (int i = 0; i < size; i++) stacks.set(i, ItemStack.EMPTY);
        ListTag list = tag.getList("Items", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag itemTags = list.getCompound(i);
            int slot = itemTags.getInt("Slot");
            if (slot < 0 || slot >= size) continue;
            ItemStack stack = ItemStack.of(itemTags);
            if (itemTags.contains("RealCount")) {
                stack.setCount(itemTags.getInt("RealCount"));
            }
            stacks.set(slot, stack);
        }
    }

    public CompoundTag save() {
        ListTag list = new ListTag();
        for (int i = 0; i < size; i++) {
            ItemStack stack = stacks.get(i);
            if (stack.isEmpty()) continue;
            CompoundTag itemTag = new CompoundTag();
            itemTag.putInt("Slot", i);
            stack.save(itemTag);
            itemTag.putInt("RealCount", stack.getCount());
            list.add(itemTag);
        }
        CompoundTag tag = new CompoundTag();
        tag.put("Items", list);
        tag.putInt("Size", size);
        return tag;
    }

    public static StoredInventory load(CompoundTag tag, int fallbackSize) {
        int size = tag.contains("Size", Tag.TAG_INT) ? tag.getInt("Size") : fallbackSize;
        StoredInventory inv = new StoredInventory(size);
        inv.read(tag);
        return inv;
    }
}
