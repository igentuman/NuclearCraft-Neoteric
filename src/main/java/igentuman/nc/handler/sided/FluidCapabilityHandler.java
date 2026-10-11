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
import java.util.function.IntSupplier;

/** Sided fluid handler backing internal tanks, enforcing per-face access and auto push/pull of fluids. */
public class FluidCapabilityHandler extends AbstractCapabilityHandler {

    private final FluidStackHandler internal;
    private final Map<Direction, FluidHandlerWrapper> wrappers = new EnumMap<>(Direction.class);
    private IntSupplier fillRate;
    private IntSupplier drainRate;
    private IntSupplier sharedFreeVolume;
    private int limitedFillTank = -1;
    private int limitedDrainTank = -1;
    private long transferTick = Long.MIN_VALUE;
    private int filledThisTick;
    private int drainedThisTick;

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
        if (side == null) return new FluidHandlerWrapper(this, null);
        return wrappers.computeIfAbsent(side, d -> new FluidHandlerWrapper(this, d));
    }

    /** Limits external transfer through capabilities and automatic push/pull; internal tank operations are unaffected. */
    public void setTransferLimits(int fillTank, IntSupplier fillRate, IntSupplier sharedFreeVolume,
                                  int drainTank, IntSupplier drainRate) {
        this.limitedFillTank = fillTank;
        this.fillRate = fillRate;
        this.sharedFreeVolume = sharedFreeVolume;
        this.limitedDrainTank = drainTank;
        this.drainRate = drainRate;
    }

    private void refreshTransferTick() {
        if (tile == null || tile.getLevel() == null) return;
        long now = tile.getLevel().getGameTime();
        if (transferTick != now) {
            transferTick = now;
            filledThisTick = 0;
            drainedThisTick = 0;
        }
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
        if (limitedFillTank >= 0 && tank == limitedDrainTank) return 0;
        if (tank == limitedFillTank) {
            refreshTransferTick();
            int allowed = Math.min(resource.getAmount(), Math.max(0, fillRate.getAsInt() - filledThisTick));
            allowed = Math.min(allowed, Math.max(0, sharedFreeVolume.getAsInt()));
            if (allowed <= 0) return 0;
            int filled = internal.fillTank(tank, resource.copyWithAmount(allowed), action);
            if (action.execute()) filledThisTick += filled;
            return filled;
        }
        return internal.fillTank(tank, resource, action);
    }

    public FluidStack drainTank(int tank, int maxDrain, IFluidHandler.FluidAction action) {
        if (limitedDrainTank >= 0 && tank == limitedFillTank) return FluidStack.EMPTY;
        if (tank == limitedDrainTank) {
            refreshTransferTick();
            maxDrain = Math.min(maxDrain, Math.max(0, drainRate.getAsInt() - drainedThisTick));
            if (maxDrain <= 0) return FluidStack.EMPTY;
            FluidStack drained = internal.drainTank(tank, maxDrain, action);
            if (action.execute()) drainedThisTick += drained.getAmount();
            return drained;
        }
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

            FluidStack available = drainTank(tank, inTank.getAmount(), IFluidHandler.FluidAction.SIMULATE);
            if (available.isEmpty()) continue;
            int filled = neighbor.fill(available, IFluidHandler.FluidAction.SIMULATE);
            if (filled > 0) {
                FluidStack drained = drainTank(tank, filled, IFluidHandler.FluidAction.EXECUTE);
                if (!drained.isEmpty()) neighbor.fill(drained, IFluidHandler.FluidAction.EXECUTE);
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

            int filled = fillTank(tank, drained, IFluidHandler.FluidAction.SIMULATE);
            if (filled > 0) {
                FluidStack actualDrained = neighbor.drain(drained.copyWithAmount(filled), IFluidHandler.FluidAction.EXECUTE);
                if (!actualDrained.isEmpty()) {
                    fillTank(tank, actualDrained, IFluidHandler.FluidAction.EXECUTE);
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
