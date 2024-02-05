package igentuman.nc.block.entity.fission;

import igentuman.nc.NuclearCraft;
import igentuman.nc.handler.sided.capability.FluidCapabilityHandler;
import igentuman.nc.handler.sided.capability.ItemCapabilityHandler;
import igentuman.nc.util.annotation.NBTField;
import mekanism.common.capabilities.Capabilities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static igentuman.nc.util.ModUtil.isCcLoaded;
import static igentuman.nc.util.ModUtil.isMekanismLoadeed;
import static net.minecraftforge.energy.CapabilityEnergy.ENERGY;
import static net.minecraftforge.fluids.capability.CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY;
import static net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;

public class FissionPortBE extends FissionBE {
    public static String NAME = "fission_reactor_port";
    @NBTField
    public byte analogSignal = 0;
    @NBTField
    public byte comparatorMode = SignalSource.HEAT;

    @NBTField
    public BlockPos controllerPos;

    @NBTField
    public boolean isSteamMode = false;

    public FissionPortBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, NAME);
    }
    public Direction getFacing() {
        return getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
    }

    public boolean hasRedstoneSignal() {
        return Objects.requireNonNull(getLevel()).hasNeighborSignal(worldPosition);
    }


    @Override
    public void tickServer() {
        if(NuclearCraft.instance.isNcBeStopped) return;
        super.tickServer();
        if(multiblock() == null || controller() == null) return;
        int wasSignal = analogSignal;
        boolean updated = sendOutPower();
        if(controllerPos == null) {
            controllerPos = controller().getBlockPos();
            updated = true;
            setChanged();
        }
        if(isSteamMode != controller().isSteamMode) {
            isSteamMode = controller().isSteamMode;
            updated = true;
        }

        if(hasRedstoneSignal()) {
            controller().controllerEnabled = true;
        }

        updateAnalogSignal();

        updated = wasSignal != analogSignal || updated;

        Direction dir = getFacing();

        if(itemHandler() != null) {
            updated = itemHandler().pushItems(dir, true, worldPosition) || updated;
            updated = itemHandler().pullItems(dir, true, worldPosition) || updated;
        }

        if(updated) {
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    private void updateAnalogSignal() {
        switch (comparatorMode) {
            case SignalSource.ENERGY:
                analogSignal = (byte) (controller().energyStorage.getEnergyStored() * 15 / controller().energyStorage.getMaxEnergyStored());
                break;
            case SignalSource.HEAT:
                analogSignal = (byte) (controller().heat * 15 / controller().getMaxHeat());
                break;
            case SignalSource.PROGRESS:
                analogSignal = (byte) (controller().recipeInfo.ticksProcessed * 15 / controller().recipeInfo.ticks);
                break;
            case SignalSource.ITEMS:
                analogSignal = (byte) (itemHandler().getStackInSlot(0).getCount() * 15 / itemHandler().getStackInSlot(0).getMaxStackSize());
                break;
        }
    }

    protected ItemCapabilityHandler itemHandler()
    {
        return controller().contentHandler.itemHandler;
    }

    protected FluidCapabilityHandler fluidHandler()
    {
        return controller().contentHandler.fluidCapability;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if(controller() == null) return super.getCapability(cap, side);
        if (cap == ITEM_HANDLER_CAPABILITY) {
            return controller().contentHandler.itemCapability.cast();
        }
        if (cap == FLUID_HANDLER_CAPABILITY && controller().isSteamMode) {
            return controller().getCapability(cap, side);
        }
        if (cap == ENERGY && !controller().isSteamMode) {
            return controller().getEnergy().cast();
        }

        if(isMekanismLoadeed() && isSteamMode) {
            if(cap == Capabilities.GAS_HANDLER_CAPABILITY) {
                if(controller().contentHandler.hasFluidCapability(side)) {
                    return LazyOptional.of(() -> controller().contentHandler.gasConverter(side));
                }
                return LazyOptional.empty();
            }
            if(cap == Capabilities.SLURRY_HANDLER_CAPABILITY) {
                if(controller().contentHandler.hasFluidCapability(side)) {
                    return LazyOptional.of(() -> controller().contentHandler.getSlurryConverter(side));
                }
                return LazyOptional.empty();
            }
        }

        if(isCcLoaded()) {
            if(cap == dan200.computercraft.shared.Capabilities.CAPABILITY_PERIPHERAL) {
                return controller().getPeripheral(cap, side);
            }
        }
        return super.getCapability(cap, side);
    }

    protected boolean sendOutPower() {
        if(multiblock() == null) return false;
        AtomicInteger capacity = new AtomicInteger(controller().energyStorage.getEnergyStored());
        if (capacity.get() > 0) {
            for (Direction direction : Direction.values()) {
                BlockEntity be = getLevel().getBlockEntity(worldPosition.relative(direction));
                if (be != null) {
                    boolean doContinue = be.getCapability(ENERGY, direction.getOpposite()).map(handler -> {
                                if (handler.canReceive()) {
                                    int received = handler.receiveEnergy(Math.min(capacity.get(), controller().energyStorage.getMaxEnergyStored()), false);
                                    capacity.addAndGet(-received);
                                    controller().energyStorage.consumeEnergy(received);
                                    setChanged();
                                    return capacity.get() > 0;
                                } else {
                                    return true;
                                }
                            }
                    ).orElse(true);
                    if (!doContinue) {
                        return true;
                    }
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean canInvalidateCache() {
        return false;
    }

    @Override
    public FissionControllerBE<?> controller() {
        if(NuclearCraft.instance.isNcBeStopped || (getLevel().getServer() != null && !getLevel().getServer().isRunning())) return null;
        if(getLevel().isClientSide && controllerPos != null) {
            return (FissionControllerBE<?>) getLevel().getBlockEntity(controllerPos);
        }
        try {
            return (FissionControllerBE<?>) multiblock().controller().controllerBE();
        } catch (NullPointerException e) {
            if(controllerPos != null) {
                return (FissionControllerBE<?>) getLevel().getBlockEntity(controllerPos);
            }
            return null;
        }
    }

    @Override
    public void load(CompoundTag tag) {
        if (tag.contains("Info")) {
            CompoundTag infoTag = tag.getCompound("Info");
            readTagData(infoTag);
        }
        super.load(tag);
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        CompoundTag infoTag = new CompoundTag();
        saveTagData(infoTag);
        tag.put("Info", infoTag);
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        if (tag != null) {
            loadClientData(tag);
        }
    }

    private void loadClientData(CompoundTag tag) {
        if (tag.contains("Info")) {
            CompoundTag infoTag = tag.getCompound("Info");
            readTagData(infoTag);
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveClientData(tag);
        return tag;
    }

    private void saveClientData(CompoundTag tag) {
        CompoundTag infoTag = new CompoundTag();
        tag.put("Info", infoTag);
        saveTagData(infoTag);
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag tag = pkt.getTag();
        handleUpdateTag(tag);
    }

    public int getEnergyStored() {
        if(controller() == null) return 0;
        return controller().energyStorage.getEnergyStored();
    }

    public double getDepletionProgress() {
        if(controller() == null) return 0;
        return controller().getDepletionProgress();
    }

    public int getMaxEnergyStored() {
        if(controller() == null) return 0;
        return controller().energyStorage.getMaxEnergyStored();
    }

    public int energyPerTick() {
        if(controller() == null) return 0;
        return controller().energyPerTick;
    }

    public void toggleComparatorMode() {
        comparatorMode++;
        if(comparatorMode > SignalSource.ITEMS) {
            comparatorMode = SignalSource.ENERGY;
        }
        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }

    public FluidTank getFluidTank(int i) {
        if(controller() == null) return null;
        return controller().getFluidTank(i);
    }

    public boolean getMode() {
        if(controller() == null) return false;
        return controller().isSteamMode;
    }

    public int getSteamPerTick() {
        if(controller() == null) return 0;
        return controller().steamPerTick;
    }

    public static class SignalSource {
        public static final byte ENERGY = 1;
        public static final byte HEAT = 2;
        public static final byte PROGRESS = 3;
        public static final byte ITEMS = 4;
    }
}
