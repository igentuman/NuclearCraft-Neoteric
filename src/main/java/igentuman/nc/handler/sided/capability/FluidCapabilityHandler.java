package igentuman.nc.handler.sided.capability;

import igentuman.nc.handler.sided.SidedContentHandler;
import igentuman.nc.handler.sided.SlotModePair;
import igentuman.nc.handler.sided.SlotModePair.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static igentuman.nc.handler.sided.SlotModePair.SlotMode.*;

public class FluidCapabilityHandler extends AbstractCapabilityHandler implements INBTSerializable<CompoundTag> {
    private final int CAPACITY;
    public final NonNullList<FluidTank> tanks;
    public final NonNullList<LazyOptional<IFluidHandler>> fluidCapabilites;
    public BlockEntity tile;
    protected FluidStack[] sortedFluids;
    public List<FluidStack> holdedInputs = new ArrayList<>();

    public FluidCapabilityHandler(int inputSlots, int outputSlots, int amount) {
        CAPACITY = amount;
        tanks = NonNullList.create();

        fluidCapabilites = NonNullList.create();
        for (int i = 0; i < inputSlots + outputSlots; i++) {
            int finalI = i;
            tanks.add(new FluidTank(CAPACITY));
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
        SidedContentHandler.RelativeDirection relativeDirection = SidedContentHandler.RelativeDirection.toRelative(side, tile.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING));
        return LazyOptional.of(() -> new FluidHandlerWrapper(this, relativeDirection, (i) -> inputAllowed(i, side), (i) -> outputAllowed(i, side))).cast();
    }

    public boolean inputAllowed(Integer i, Direction side) {
        if(side == null) return true;
        SidedContentHandler.RelativeDirection relativeDirection = SidedContentHandler.RelativeDirection.toRelative(side, getFacing());
        SlotModePair.SlotMode mode = sideMap.get(relativeDirection.ordinal())[i].getMode();
        return mode == INPUT || mode == PULL;
    }

    public boolean outputAllowed(Integer i, Direction side) {
        if(side == null) return true;
        SidedContentHandler.RelativeDirection relativeDirection = SidedContentHandler.RelativeDirection.toRelative(side, getFacing());
        SlotModePair.SlotMode mode = sideMap.get(relativeDirection.ordinal())[i].getMode();
        return mode == OUTPUT || mode == PUSH || mode == PUSH_EXCESS;
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

    private Direction getFacing() {
        return tile.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
    }

    public boolean pushFluids(Direction dir) {
        return pushFluids(dir, false, tile.getBlockPos());
    }
    public boolean pushFluids(Direction dir, boolean forceFlag, BlockPos pos) {
        BlockEntity be = tile.getLevel().getBlockEntity(pos.relative(dir));
        if(be == null) return false;
        LazyOptional<IFluidHandler> cap = be.getCapability(ForgeCapabilities.FLUID_HANDLER, dir.getOpposite());
        if(cap.isPresent()) {
            IFluidHandler handler = cap.orElse(null);
            SidedContentHandler.RelativeDirection relativeDirection = SidedContentHandler.RelativeDirection.toRelative(dir, getFacing());
            for(SlotModePair pair : sideMap.get(relativeDirection.ordinal())) {
                if(pair.getMode() == SlotMode.PUSH || forceFlag) {
                    FluidTank tank = tanks.get(pair.getSlot());
                    if(tank.getFluidAmount() > 0) {
                        int amount = handler.fill(tank.getFluid(), IFluidHandler.FluidAction.EXECUTE);
                        tank.drain(amount, IFluidHandler.FluidAction.EXECUTE);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean pullFluids(Direction dir) {
        return pullFluids(dir, false, tile.getBlockPos());
    }

    public boolean pullFluids(Direction dir, boolean forceFlag, BlockPos pos) {
        BlockEntity be = tile.getLevel().getBlockEntity(pos.relative(dir));
        if(be == null) return false;
        LazyOptional<IFluidHandler> cap = be.getCapability(ForgeCapabilities.FLUID_HANDLER, dir.getOpposite());
        if(cap.isPresent()) {
            IFluidHandler handler = cap.orElse(null);
            SidedContentHandler.RelativeDirection relativeDirection = SidedContentHandler.RelativeDirection.toRelative(dir, getFacing());
            for(SlotModePair pair : sideMap.get(relativeDirection.ordinal())) {
                if(pair.getMode() == SlotMode.PULL || forceFlag) {
                    FluidTank tank = tanks.get(pair.getSlot());
                    if(tank.getFluidAmount() < tank.getCapacity()) {
                        int amount = tank.fill(handler.drain(tank.getCapacity() - tank.getFluidAmount(), IFluidHandler.FluidAction.EXECUTE), IFluidHandler.FluidAction.EXECUTE);
                        return amount > 0;
                    }
                }
            }
        }
        return false;
    }

    public FluidStack getFluidInSlot(int i) {
        return tanks.get(i).getFluid();
    }

    public String getCacheKey() {
        String key = "";
        if(sortedFluids == null) {
            sortedFluids = new FluidStack[inputSlots];
            for(int i = 0; i < inputSlots; i++) {
                sortedFluids[i] = getFluidInSlot(i);
            }
            Arrays.sort(sortedFluids, Comparator.comparing(fluidStack -> fluidStack.getFluid().toString()));
        }
        for (FluidStack tank : sortedFluids) {
            key += tank.getFluid().toString();
        }
        return key;
    }

    public boolean isValidForOutputSlot(int i, FluidStack outputFluid) {
        if(outputAllowed(i, null)) {
            FluidStack stack = getFluidInSlot(i);
            if(stack.isEmpty()) return true;
            if(stack.isFluidEqual(outputFluid)) return true;
        }
        return false;
    }

    public boolean canPushExcessFluid(int i, FluidStack outputFluid) {
        for(Direction dir: Direction.values()) {
            BlockEntity be = tile.getLevel().getBlockEntity(tile.getBlockPos().relative(dir));
            if(be == null) continue;
            LazyOptional<IFluidHandler> cap = be.getCapability(ForgeCapabilities.FLUID_HANDLER, dir.getOpposite());
            if(cap.isPresent()) {
                IFluidHandler handler = cap.orElse(null);
                SidedContentHandler.RelativeDirection relativeDirection = SidedContentHandler.RelativeDirection.toRelative(dir, getFacing());
                for(SlotModePair pair : sideMap.get(relativeDirection.ordinal())) {
                    if(pair.getSlot() != i) continue;
                    if(pair.getMode() == PUSH) {
                        int amount = handler.fill(outputFluid, IFluidHandler.FluidAction.SIMULATE);
                        if(amount == outputFluid.getAmount()) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }


    public FluidStack insertFluidInternal(int i, FluidStack toOutput, boolean b) {
        FluidStack stack = getFluidInSlot(i);
        if(stack.isEmpty()) {
            if(!b) {
                tanks.get(i).fill(toOutput, IFluidHandler.FluidAction.EXECUTE);
            }
            return FluidStack.EMPTY;
        }
        if(stack.isFluidEqual(toOutput)) {
            int amount = tanks.get(i).fill(toOutput, IFluidHandler.FluidAction.SIMULATE);
            if(amount == toOutput.getAmount()) {
                if(!b) {
                    tanks.get(i).fill(toOutput, IFluidHandler.FluidAction.EXECUTE);
                }
                return FluidStack.EMPTY;
            }
            FluidStack result = toOutput.copy();
            result.shrink(amount);
            return result;
        }
        return toOutput;
    }

    public FluidStack pushExcessFluid(int i, FluidStack toOutput) {
        for(Direction dir: Direction.values()) {
            BlockEntity be = tile.getLevel().getBlockEntity(tile.getBlockPos().relative(dir));
            if(be == null) return toOutput;
            LazyOptional<IFluidHandler> cap = be.getCapability(ForgeCapabilities.FLUID_HANDLER, dir.getOpposite());
            if(cap.isPresent()) {
                IFluidHandler handler = cap.orElse(null);

                SidedContentHandler.RelativeDirection relativeDirection = SidedContentHandler.RelativeDirection.toRelative(dir, getFacing());
                for(SlotModePair pair : sideMap.get(relativeDirection.ordinal())) {
                    if(pair.getMode() == PUSH_EXCESS) {
                        FluidTank tank = tanks.get(pair.getSlot());
                        if(tank.getFluidAmount() > 0 && toOutput.getFluid().equals(tank.getFluid().getFluid())) {
                            if(handler.fill(toOutput, IFluidHandler.FluidAction.SIMULATE) == toOutput.getAmount() ) {
                                int amount = handler.fill(toOutput, IFluidHandler.FluidAction.EXECUTE);
                                //tank.drain(amount, IFluidHandler.FluidAction.EXECUTE);
                                return FluidStack.EMPTY;
                            }
                        }
                    }
                }
            }
            return toOutput;
        }
        return toOutput;
    }
}
