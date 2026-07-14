package igentuman.nc.handler.sided;

import igentuman.nc.handler.SlotModePair;
import igentuman.nc.handler.SidedContentHandler.RelativeDirection;
import igentuman.nc.handler.fluid.FluidStackHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import java.util.EnumMap;
import java.util.Map;

/** Sided fluid handler backing internal tanks, enforcing per-face access and auto push/pull of fluids. */
public class FluidCapabilityHandler extends AbstractCapabilityHandler {

    private final FluidStackHandler internal;
    private final Map<Direction, FluidHandlerWrapper> wrappers = new EnumMap<>(Direction.class);

    public FluidCapabilityHandler(int inputTanks, int outputTanks, int extraTanks, int[] capacities) {
        super(inputTanks, outputTanks, extraTanks);
        int totalTanks = getTotalSlots();
        this.internal = new FluidStackHandler(totalTanks, capacities) {
            @Override
            protected void onContentsChanged(int tank) {
                if (tile != null) tile.setChanged();
            }
        };
    }

    public FluidStackHandler getInternalHandler() {
        return internal;
    }

    public IFluidHandler getCapability(Direction side) {
        if (side == null) return internal;
        return wrappers.computeIfAbsent(side, d -> new FluidHandlerWrapper(this, d));
    }

    public void invalidateWrappers() {
        wrappers.clear();
    }

    public boolean canInsertFromSide(int tank, FluidStack fluid, Direction side) {
        SlotModePair.SlotMode mode = getModeForAbsoluteSide(side, tank);
        return mode == SlotModePair.SlotMode.INPUT
                || mode == SlotModePair.SlotMode.PULL
                || mode == SlotModePair.SlotMode.DEFAULT;
    }

    public boolean canExtractFromSide(int tank, Direction side) {
        SlotModePair.SlotMode mode = getModeForAbsoluteSide(side, tank);
        return mode == SlotModePair.SlotMode.OUTPUT
                || mode == SlotModePair.SlotMode.PUSH
                || mode == SlotModePair.SlotMode.DEFAULT;
    }

    public int getTanks() {
        return internal.getTanks();
    }

    public FluidStack getFluidInTank(int tank) {
        return internal.getFluidInTank(tank);
    }

    public int getTankCapacity(int tank) {
        return internal.getTankCapacity(tank);
    }

    public boolean isFluidValid(int tank, FluidStack stack) {
        return internal.isFluidValid(tank, stack);
    }

    public int fillTank(int tank, FluidStack resource, IFluidHandler.FluidAction action) {
        return internal.fillTank(tank, resource, action);
    }

    public FluidStack drainTank(int tank, int maxDrain, IFluidHandler.FluidAction action) {
        return internal.drainTank(tank, maxDrain, action);
    }

    public void pushFluids(Direction absoluteDir) {
        if (tile == null || tile.getLevel() == null || tile.getLevel().isClientSide()) return;
        BlockPos neighborPos = tile.getBlockPos().relative(absoluteDir);
        IFluidHandler neighbor = tile.getLevel().getCapability(
                Capabilities.FluidHandler.BLOCK, neighborPos, absoluteDir.getOpposite());
        if (neighbor == null) return;

        Direction facing = getFacing();
        RelativeDirection relDir = RelativeDirection.toRelative(absoluteDir, facing);
        if (relDir == null) return;
        SlotModePair[] pairs = sideMap.get(relDir.ordinal());
        if (pairs == null) return;

        for (SlotModePair pair : pairs) {
            if (pair.getMode() != SlotModePair.SlotMode.PUSH) continue;
            int tank = pair.getSlot();
            if (tank < inputSlots) continue;
            FluidStack inTank = internal.getFluidInTank(tank);
            if (inTank.isEmpty()) continue;

            int filled = neighbor.fill(inTank.copy(), IFluidHandler.FluidAction.SIMULATE);
            if (filled > 0) {
                neighbor.fill(inTank.copyWithAmount(filled), IFluidHandler.FluidAction.EXECUTE);
                internal.drainTank(tank, filled, IFluidHandler.FluidAction.EXECUTE);
            }
        }
    }

    public void pullFluids(Direction absoluteDir) {
        if (tile == null || tile.getLevel() == null || tile.getLevel().isClientSide()) return;
        BlockPos neighborPos = tile.getBlockPos().relative(absoluteDir);
        IFluidHandler neighbor = tile.getLevel().getCapability(
                Capabilities.FluidHandler.BLOCK, neighborPos, absoluteDir.getOpposite());
        if (neighbor == null) return;

        Direction facing = getFacing();
        RelativeDirection relDir = RelativeDirection.toRelative(absoluteDir, facing);
        if (relDir == null) return;
        SlotModePair[] pairs = sideMap.get(relDir.ordinal());
        if (pairs == null) return;

        for (SlotModePair pair : pairs) {
            if (pair.getMode() != SlotModePair.SlotMode.PULL) continue;
            int tank = pair.getSlot();
            if (tank >= inputSlots) continue;

            int capacity = internal.getTankCapacity(tank);
            FluidStack inTank = internal.getFluidInTank(tank);
            int space = capacity - inTank.getAmount();
            if (space <= 0) continue;

            FluidStack drained = neighbor.drain(space, IFluidHandler.FluidAction.SIMULATE);
            if (drained.isEmpty()) continue;

            int filled = internal.fillTank(tank, drained.copy(), IFluidHandler.FluidAction.SIMULATE);
            if (filled > 0) {
                FluidStack actualDrained = neighbor.drain(drained.copyWithAmount(filled), IFluidHandler.FluidAction.EXECUTE);
                if (!actualDrained.isEmpty()) {
                    internal.fillTank(tank, actualDrained, IFluidHandler.FluidAction.EXECUTE);
                }
            }
        }
    }

    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.put("Fluids", internal.serializeNBT(provider));
        tag.put("SideMap", serializeSideMap());
        return tag;
    }

    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        if (tag.contains("Fluids")) {
            internal.deserializeNBT(provider, tag.getCompound("Fluids"));
        }
        if (tag.contains("SideMap")) {
            deserializeSideMap(tag.getCompound("SideMap"));
            invalidateWrappers();
        }
    }
}
