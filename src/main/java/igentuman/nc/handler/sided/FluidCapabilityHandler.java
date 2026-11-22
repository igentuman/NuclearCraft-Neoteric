package igentuman.nc.handler.sided;

import igentuman.nc.handler.SlotModePair;
import igentuman.nc.handler.SidedContentHandler.RelativeDirection;
import igentuman.nc.handler.fluid.FluidStackHandler;
import igentuman.nc.handler.fluid.FluidTransferBudget;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.RangedResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

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
    private final FluidTransferBudget fillBudget = new FluidTransferBudget();
    private final FluidTransferBudget drainBudget = new FluidTransferBudget();

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

    public ResourceHandler<FluidResource> getCapability(Direction side) {
        if (side == null) return new FluidHandlerWrapper(this, null);
        return wrappers.computeIfAbsent(side, d -> new FluidHandlerWrapper(this, d));
    }

    /** Limits external transfer through capabilities and automatic push/pull; internal tank operations are unaffected. */
    public void setTransferLimits(int fillTank, IntSupplier fillRate, IntSupplier sharedFreeVolume,
                                  int drainTank, IntSupplier drainRate) {
        if (fillBudget.isInTransaction() || drainBudget.isInTransaction()) throw new IllegalStateException("Cannot change fluid limits during transfer");
        this.limitedFillTank = fillTank;
        this.fillRate = fillRate;
        this.sharedFreeVolume = sharedFreeVolume;
        this.limitedDrainTank = drainTank;
        this.drainRate = drainRate;
    }

    private long transferTick() {
        return tile == null || tile.getLevel() == null ? Long.MIN_VALUE : tile.getLevel().getGameTime();
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

    public int insert(int tank, FluidResource resource, int amount, TransactionContext transaction) {
        java.util.Objects.checkIndex(tank, getTanks());
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        if (limitedFillTank >= 0 && tank == limitedDrainTank) return 0;
        if (tank != limitedFillTank) return internal.insert(tank, resource, amount, transaction);
        int allowed = Math.min(amount, Math.max(0, sharedFreeVolume.getAsInt()));
        return fillBudget.transfer(transferTick(), fillRate.getAsInt(), allowed, transaction,
                available -> internal.insert(tank, resource, available, transaction));
    }

    public int extract(int tank, FluidResource resource, int amount, TransactionContext transaction) {
        java.util.Objects.checkIndex(tank, getTanks());
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        if (limitedDrainTank >= 0 && tank == limitedFillTank) return 0;
        if (tank != limitedDrainTank) return internal.extract(tank, resource, amount, transaction);
        return drainBudget.transfer(transferTick(), drainRate.getAsInt(), amount, transaction,
                available -> internal.extract(tank, resource, available, transaction));
    }

    public void pushFluids(Direction absoluteDir) { moveFluids(absoluteDir, true); }
    public void pullFluids(Direction absoluteDir) { moveFluids(absoluteDir, false); }

    private void moveFluids(Direction direction, boolean push) {
        if (tile == null || tile.getLevel() == null || tile.getLevel().isClientSide()) return;
        ResourceHandler<FluidResource> neighbor = tile.getLevel().getCapability(
                Capabilities.Fluid.BLOCK, tile.getBlockPos().relative(direction), direction.getOpposite());
        if (neighbor == null) return;
        RelativeDirection relative = RelativeDirection.toRelative(direction, getFacing());
        if (relative == null) return;
        SlotModePair[] pairs = sideMap.get(relative.ordinal());
        if (pairs == null) return;
        for (SlotModePair pair : pairs) {
            if (pair.getMode() != (push ? SlotModePair.SlotMode.PUSH : SlotModePair.SlotMode.PULL)) continue;
            int tank = pair.getSlot();
            if (push ? tank < inputSlots : tank >= inputSlots) continue;
            var local = RangedResourceHandler.ofSingleIndex(getCapability(direction), tank);
            try (var transaction = Transaction.openRoot()) {
                ResourceHandlerUtil.move(push ? local : neighbor, push ? neighbor : local,
                        resource -> true, Integer.MAX_VALUE, transaction);
                transaction.commit();
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
            internal.deserializeNBT(provider, tag.getCompound("Fluids").orElseThrow());
        }
        if (tag.contains("SideMap")) {
            deserializeSideMap(tag.getCompound("SideMap").orElseThrow());
            invalidateWrappers();
        }
    }
}
