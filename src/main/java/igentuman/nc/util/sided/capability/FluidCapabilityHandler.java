package igentuman.nc.util.sided.capability;

import igentuman.nc.util.sided.SidedContentHandler;
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

public class FluidCapabilityHandler extends AbscractCapabilityHandler implements INBTSerializable<CompoundTag> {
    private final int CAPACITY;
    public final NonNullList<FluidTank> tanks;
    public final NonNullList<LazyOptional<IFluidHandler>> fluidCapabilites;

    public FluidCapabilityHandler(int inputSlots, int outputSlots, int amount) {
        CAPACITY = amount;
        tanks = NonNullList.withSize(inputSlots + outputSlots, new FluidTank(CAPACITY));

        fluidCapabilites = NonNullList.create();
        for (int i = 0; i < inputSlots + outputSlots; i++) {
            int finalI = i;
            fluidCapabilites.add(LazyOptional.of(() -> tanks.get(finalI)));
        }
        this.inputSlots = inputSlots;
        this.outputSlots = outputSlots;
        initDefault();
    }

    public FluidCapabilityHandler(int inputSlots, int outputSlots) {
        this(inputSlots, outputSlots, FluidType.BUCKET_VOLUME*10);
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
        if(sideMapUpdated) {
            sideMapUpdated = false;
            tag.put("sideMap", SidedContentHandler.serializeSideMap(sideMap));
        }
        return tag;
    }

    public void deserializeNBT(CompoundTag nbt) {
        int size = nbt.getInt("size");
        for (int i = 0; i < size; i++) {
            tanks.get(i).readFromNBT(nbt.getCompound("tank" + i));
        }
        if(!nbt.getCompound("sideMap").isEmpty()) {
            sideMap = SidedContentHandler.deserializeSideMap(nbt.getCompound("sideMap"));
        }
    }
}
