package igentuman.nc.util.sided.capability;

import igentuman.nc.util.sided.SidedContentHandler;
import igentuman.nc.util.sided.SlotModePair;
import igentuman.nc.util.sided.SlotModePair.*;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.HashMap;

public class NCItemStackHandler extends AbscractCapabilityHandler implements IItemHandler, IItemHandlerModifiable, INBTSerializable<CompoundTag> {

    public HashMap<Integer, SlotModePair[]> sideMap = new HashMap<>();
    protected NonNullList<ItemStack> stacks;

    public NCItemStackHandler(int input, int output) {
        this.inputSlots = input;
        this.outputSlots = output;
        initDefault();
    }

    @Override
    public void setStackInSlot(int slot, @NotNull ItemStack stack)
    {
        validateSlotIndex(slot);
        this.stacks.set(slot, stack);
        onContentsChanged(slot);
    }

    @Override
    public int getSlots()
    {
        return stacks.size();
    }

    @Override
    @NotNull
    public ItemStack getStackInSlot(int slot)
    {
        validateSlotIndex(slot);
        return this.stacks.get(slot);
    }


    public ItemStack extractItemInternal(int slot, int amount, boolean simulate)
    {
        return extractItem(slot, amount, simulate);
    }

    public ItemStack extractItem(int slot, int amount, boolean simulate)
    {
        if (amount == 0)
            return ItemStack.EMPTY;

        validateSlotIndex(slot);

        ItemStack existing = this.stacks.get(slot);

        if (existing.isEmpty())
            return ItemStack.EMPTY;

        int toExtract = Math.min(amount, existing.getMaxStackSize());

        if (existing.getCount() <= toExtract)
        {
            if (!simulate)
            {
                this.stacks.set(slot, ItemStack.EMPTY);
                onContentsChanged(slot);
                return existing;
            }
            else
            {
                return existing.copy();
            }
        }
        else
        {
            if (!simulate)
            {
                this.stacks.set(slot, ItemHandlerHelper.copyStackWithSize(existing, existing.getCount() - toExtract));
                onContentsChanged(slot);
            }

            return ItemHandlerHelper.copyStackWithSize(existing, toExtract);
        }
    }

    public ItemStack insertItemInternal(int slot, @Nonnull ItemStack stack, boolean simulate) {
        return insertItem(slot, stack, simulate);
    }

    @Override
    @NotNull
    public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate)
    {
        if (stack.isEmpty())
            return ItemStack.EMPTY;

        if (!isItemValid(slot, stack))
            return stack;

        validateSlotIndex(slot);

        ItemStack existing = this.stacks.get(slot);

        int limit = getStackLimit(slot, stack);

        if (!existing.isEmpty())
        {
            if (!ItemHandlerHelper.canItemStacksStack(stack, existing))
                return stack;

            limit -= existing.getCount();
        }

        if (limit <= 0)
            return stack;

        boolean reachedLimit = stack.getCount() > limit;

        if (!simulate)
        {
            if (existing.isEmpty())
            {
                this.stacks.set(slot, reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, limit) : stack);
            }
            else
            {
                existing.grow(reachedLimit ? limit : stack.getCount());
            }
            onContentsChanged(slot);
        }

        return reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, stack.getCount()- limit) : ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot)
    {
        return 64;
    }

    protected int getStackLimit(int slot, @NotNull ItemStack stack)
    {
        return Math.min(getSlotLimit(slot), stack.getMaxStackSize());
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack)
    {
        return true;
    }

    private int isValidForAnyInputSlot(ItemStack stack) {
        for (int i = 0; i < inputSlots; i++) {
            if (!isItemValid(i, stack)) continue;
            if (getStackInSlot(i).isEmpty()) return i;
            if (ItemHandlerHelper.canItemStacksStack(getStackInSlot(i), stack) && getStackInSlot(i).getCount() < getSlotLimit(i)) {
                return i;
            }
        }
        return -1;
    }

    private int isValidForAnyOutputSlot(ItemStack stack) {
        for (int i = inputSlots; i < getSlots(); i++) {
            if (!isItemValid(i, stack)) continue;
            if (getStackInSlot(i).isEmpty()) return i;
            if (ItemHandlerHelper.canItemStacksStack(getStackInSlot(i), stack) && getStackInSlot(i).getCount() < getSlotLimit(i)) {
                return i;
            }
        }
        return -1;
    }

    private int getNextSlot(int currentSlot) {
        for (int i = currentSlot; i < getSlots(); i++) {
            if (!getStackInSlot(i).isEmpty()) return i;
        }
        return 0;
    }


    public SidedContentHandler.SlotType getType(int slot) {
        return slot < inputSlots ? SidedContentHandler.SlotType.INPUT : SidedContentHandler.SlotType.OUTPUT;
    }

    public SlotModePair.SlotMode getMode(int slot, int side) {
        return sideMap.get(side)[slot].getMode();
    }

    public void toggleMode(int slot, int side) {
        SlotModePair[] sideSlots = sideMap.get(side);
        SlotModePair slotModePair = sideSlots[slot];
        SlotModePair.SlotMode mode = slotModePair.getMode();

        if(getType(slot) == SidedContentHandler.SlotType.INPUT) {
            if (mode == SlotMode.DISABLED) {
                sideSlots[slot] = new SlotModePair(SlotMode.INPUT, slot);
            } else if (mode == SlotMode.INPUT) {
                sideSlots[slot] = new SlotModePair(SlotMode.PULL, slot);
            } else if (mode == SlotMode.PULL) {
                sideSlots[slot] = new SlotModePair(SlotMode.DISABLED, slot);
            }
        } else {
            if (mode == SlotModePair.SlotMode.DISABLED) {
                sideSlots[slot] = new SlotModePair(SlotMode.OUTPUT, slot);
            } else if (mode == SlotMode.OUTPUT) {
                sideSlots[slot] = new SlotModePair(SlotMode.PUSH, slot);
            } else if (mode == SlotMode.PUSH) {
                sideSlots[slot] = new SlotModePair(SlotMode.PUSH_EXCESS, slot);
            } else if (mode == SlotMode.PUSH_EXCESS) {
                sideSlots[slot] = new SlotModePair(SlotMode.DISABLED, slot);
            }
        }
    }

    @Override
    public CompoundTag serializeNBT()
    {
        ListTag nbtTagList = new ListTag();
        for (int i = 0; i < stacks.size(); i++)
        {
            if (!stacks.get(i).isEmpty())
            {
                CompoundTag itemTag = new CompoundTag();
                itemTag.putInt("Slot", i);
                stacks.get(i).save(itemTag);
                nbtTagList.add(itemTag);
            }
        }
        CompoundTag nbt = new CompoundTag();
        nbt.put("Items", nbtTagList);
        nbt.putInt("Size", stacks.size());
        nbt.put("sideMap", SidedContentHandler.serializeSideMap(sideMap));
        return nbt;
    }

    public void setSize(int size)
    {
        stacks = NonNullList.withSize(size, ItemStack.EMPTY);
    }

    @Override
    public void deserializeNBT(CompoundTag nbt)
    {
        setSize(nbt.contains("Size", Tag.TAG_INT) ? nbt.getInt("Size") : stacks.size());
        ListTag tagList = nbt.getList("Items", Tag.TAG_COMPOUND);
        for (int i = 0; i < tagList.size(); i++)
        {
            CompoundTag itemTags = tagList.getCompound(i);
            int slot = itemTags.getInt("Slot");

            if (slot >= 0 && slot < stacks.size())
            {
                stacks.set(slot, ItemStack.of(itemTags));
            }
        }
        sideMap = SidedContentHandler.deserializeSideMap(nbt.getCompound("sideMap"));
        onLoad();

    }

    protected void validateSlotIndex(int slot)
    {
        if (slot < 0 || slot >= stacks.size())
            throw new RuntimeException("Slot " + slot + " not in valid range - [0," + stacks.size() + ")");
    }

    protected void onLoad()
    {

    }

    protected void onContentsChanged(int slot)
    {

    }
}
