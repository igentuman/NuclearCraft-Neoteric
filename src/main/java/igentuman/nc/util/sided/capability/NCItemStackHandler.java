package igentuman.nc.util.sided.capability;

import igentuman.nc.util.sided.SidedContentHandler;
import igentuman.nc.util.sided.SlotModePair;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.HashMap;

public class NCItemStackHandler extends ItemStackHandler {

    public HashMap<Integer, SlotModePair[]> sideMap = new HashMap<>();
    private final int inputSlots;
    private final int outputSlots;
    public NCItemStackHandler(int input, int output) {
        super(input+output);
        this.inputSlots = input;
        this.outputSlots = output;
        initDefault();
    }

    private void initDefault() {
        sideMap = new HashMap<>();
        SlotModePair[] defaultSide = new SlotModePair[inputSlots+outputSlots];
        for (int i = 0; i < inputSlots; i++) {
            defaultSide[i] = new SlotModePair(SlotModePair.SlotMode.INPUT, i);
        }
        for (int i = inputSlots; i < inputSlots+outputSlots; i++) {
            defaultSide[i] = new SlotModePair(SlotModePair.SlotMode.OUTPUT, i);
        }
        for (Direction side : Direction.values()) {
            sideMap.put(side.ordinal(), defaultSide);
        }
    }

    public ItemStack extractItemInternal(int slot, int amount, boolean simulate)
    {
        return super.extractItem(slot, amount, simulate);
    }

    public ItemStack insertItemInternal(int slot, @Nonnull ItemStack stack, boolean simulate) {
        return super.insertItem(slot, stack, simulate);
    }

    @Override
    @NotNull
    public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate)
    {
        if(slot >= inputSlots) {return stack;}
        return super.insertItem(slot, stack, simulate);
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

    public SlotModePair.SlotMode getMode(int slot, Direction side) {
        return sideMap.get(slot)[side.ordinal()].getMode();
    }

    public void toggleMode(int slot, int side) {
        SlotModePair[] sideSlots = sideMap.get(side);
        SlotModePair slotModePair = sideSlots[slot];
        SlotModePair.SlotMode mode = slotModePair.getMode();

        if(getType(slot) == SidedContentHandler.SlotType.INPUT) {
            if (mode == SlotModePair.SlotMode.DISABLED) {
                sideSlots[slot] = new SlotModePair(SlotModePair.SlotMode.INPUT, slot);
            } else if (mode == SlotModePair.SlotMode.INPUT) {
                sideSlots[slot] = new SlotModePair(SlotModePair.SlotMode.DISABLED, slot);
            }
        } else {
            if (mode == SlotModePair.SlotMode.DISABLED) {
                sideSlots[slot] = new SlotModePair(SlotModePair.SlotMode.OUTPUT, slot);
            }  else if (mode == SlotModePair.SlotMode.OUTPUT) {
                sideSlots[slot] = new SlotModePair(SlotModePair.SlotMode.DISABLED, slot);
            }
        }
    }

    public void toggleSideConfig(int slotId, int direction) {
        toggleMode(slotId, direction);
    }

    @Override
    public CompoundTag serializeNBT()
    {
        CompoundTag nbt = super.serializeNBT();
        nbt.put("sideMap", SidedContentHandler.serializeSideMap(sideMap));
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt)
    {
        super.deserializeNBT(nbt);
        sideMap = SidedContentHandler.deserializeSideMap(nbt.getCompound("sideMap"));
    }

    public SlotModePair.SlotMode getSlotMode(int slotId, int relativeDir) {
        return sideMap.get(relativeDir)[slotId].getMode();
    }
}
