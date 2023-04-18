package igentuman.nc.util.sided.capability;

import igentuman.nc.util.sided.SidedContentHandler;
import igentuman.nc.util.sided.SidedContentHandler.*;
import igentuman.nc.util.sided.SlotModePair;
import igentuman.nc.util.sided.SlotModePair.*;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import java.util.HashMap;
import java.util.Map;

public class FluidMultiTank implements INBTSerializable<CompoundTag> {
    private final int CAPACITY;
    private final int inputFluidSlots;
    private final int outputFluidSlots;
    public final NonNullList<FluidTank> tanks;
    public final NonNullList<LazyOptional<IFluidHandler>> fluidCapabilites;
    public HashMap<Integer, SlotModePair[]> sideMap;

    public FluidMultiTank(int inputFluidSlots, int outputFluidSlots, int amount) {
        CAPACITY = amount;
        tanks = NonNullList.withSize(inputFluidSlots + outputFluidSlots, new FluidTank(CAPACITY));

        fluidCapabilites = NonNullList.create();
        for (int i = 0; i < inputFluidSlots + outputFluidSlots; i++) {
            int finalI = i;
            fluidCapabilites.add(LazyOptional.of(() -> tanks.get(finalI)));
        }
        this.inputFluidSlots = inputFluidSlots;
        this.outputFluidSlots = outputFluidSlots;
        initDefault();
    }

    public FluidMultiTank(int inputFluidSlots, int outputFluidSlots) {
        this(inputFluidSlots, outputFluidSlots, FluidType.BUCKET_VOLUME*10);
    }

    private void initDefault() {
        sideMap = new HashMap<>();
        SlotModePair[] defaultSide = new SlotModePair[inputFluidSlots+outputFluidSlots];
        for (int i = 0; i < inputFluidSlots+outputFluidSlots; i++) {
            defaultSide[i] = new SlotModePair(SlotMode.INPUT, i);
        }
        for (Direction side : Direction.values()) {
            sideMap.put(side.ordinal(), defaultSide);
        }
    }

    public SlotType getType(int slot) {
        return slot < inputFluidSlots ? SlotType.INPUT : SlotType.OUTPUT;
    }

    public SlotMode getMode(int slot, Direction side) {
        return sideMap.get(slot)[side.ordinal()].getMode();
    }

    public <T> LazyOptional<T> getCapability(Direction side) {
        if(side == null) return getCapability();
        for (SlotModePair slotModePair : sideMap.get(side.ordinal())) {
            if (slotModePair.getMode() != SlotMode.DISABLED)
                return fluidCapabilites.get(slotModePair.getSlot()).cast();
        }
        return LazyOptional.empty();
    }

    public <T> LazyOptional<T> getCapability() {
        for(Direction side : Direction.values()) {
            for (SlotModePair slotModePair : sideMap.get(side.ordinal())) {
                if (slotModePair.getMode() != SlotMode.DISABLED)
                    return fluidCapabilites.get(slotModePair.getSlot()).cast();
            }
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        for (int i = 0; i < tanks.size(); i++) {
            tag.put("tank" + i, tanks.get(i).writeToNBT(new CompoundTag()));
        }
        tag.putInt("size", tanks.size());
        tag.put("sideMap", SidedContentHandler.serializeSideMap(sideMap));
        return tag;
    }

    public void deserializeNBT(CompoundTag nbt) {
        int size = nbt.getInt("size");
        for (int i = 0; i < size; i++) {
            tanks.get(i).readFromNBT(nbt.getCompound("tank" + i));
        }
        sideMap = SidedContentHandler.deserializeSideMap(nbt.getCompound("sideMap"));
    }

    public void toggleMode(int slot, int side) {
        SlotModePair[] sideSlots = sideMap.get(side);
        SlotModePair slotModePair = sideSlots[slot];
        SlotMode mode = slotModePair.getMode();

        if(getType(slot) == SlotType.INPUT) {
            if (mode == SlotMode.DISABLED) {
                sideSlots[slot] = new SlotModePair(SlotMode.INPUT, slot);
            } else if (mode == SlotMode.INPUT) {
                sideSlots[slot] = new SlotModePair(SlotMode.DISABLED, slot);
            }
        } else {
            if (mode == SlotMode.DISABLED) {
                sideSlots[slot] = new SlotModePair(SlotMode.OUTPUT, slot);
            }  else if (mode == SlotMode.OUTPUT) {
                sideSlots[slot] = new SlotModePair(SlotMode.DISABLED, slot);
            }
        }
    }

    public void toggleSideConfig(int i, int direction) {
        toggleMode(i, direction);
    }

    public SlotMode getSlotMode(int i, int relativeDir) {
        return sideMap.get(relativeDir)[i].getMode();
    }
}
