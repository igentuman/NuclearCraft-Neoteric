package igentuman.nc.block.turbine.entity;

import igentuman.api.platform.NCLevels;
import igentuman.nc.NuclearCraft;
import igentuman.nc.block.MultiblockPortBE;
import igentuman.nc.handler.sided.capability.FluidCapabilityHandler;
import igentuman.nc.multiblock.AbstractMultiblock;
import igentuman.nc.multiblock.MultiblockHandler;
import igentuman.nc.multiblock.turbine.TurbineMultiblock;
import igentuman.nc.multiblock.turbine.TurbineRegistration;
import igentuman.nc.util.annotation.NBTField;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

import java.util.Objects;

import static igentuman.nc.NuclearCraft.currentTick;
import static igentuman.nc.block.entity.NuclearCraftBE.isGTEUCapEnabled;
import static igentuman.nc.compat.gregtech.GTUtils.isOnlyGTCEUCapEnabled;
import static igentuman.nc.compat.gregtech.GTUtils.transferEU;
import static igentuman.nc.util.ModUtil.isCcLoaded;
import static igentuman.nc.util.ModUtil.isGtLoaded;

public class TurbinePortBE extends MultiblockPortBE {
    public static String NAME = "turbine_port";
    @NBTField
    public byte analogSignal = 0;
    @NBTField
    public byte comparatorMode = SignalSource.OVERFLOW;
    protected TurbineMultiblock multiblock;
    @NBTField
    public BlockPos controllerPos;
    public TurbineControllerBE controller;
    public TurbinePortBE(BlockPos pPos, BlockState pBlockState) {
        super(TurbineRegistration.TURBINE_BE.get(NAME).get(), pPos, pBlockState);
    }

    @Override
    public void setMultiblock(AbstractMultiblock multiblock) {
        if(this.multiblock == multiblock) {
            return;
        }
        this.multiblock = (TurbineMultiblock) multiblock;
        if (this.multiblock != null) {
            controllerPos = this.multiblock.controller().controllerBE().getBlockPos();
            controller = this.multiblock.controller().controllerBE();
            setChanged();
            level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
        }
    }


    public TurbineMultiblock getMultiblock() {
        return multiblock;
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
        //Disallow boosters like torcherino
        if(lastTickTime == level.getGameTime()) {
            return;
        }
        lastTickTime = level.getGameTime();
        super.tickServer();
        int wasSignal = analogSignal;
        if(getMultiblock() != null || controller() != null) {
            sendOutPower();
        }
        boolean updated = updateController();
        if(hasRedstoneSignal()) {
            controller().controllerEnabled = true;
        }

        updateAnalogSignal();

        updated = wasSignal != analogSignal || updated;

        Direction dir = getFacing();

        if(controller() != null && fluidHandler() != null) {
            updated = fluidHandler().pushFluids(dir, false, worldPosition) || updated;
            updated = fluidHandler().pullFluids(dir, false, worldPosition) || updated;
        }

        if(updated || (currentTick % 40 == 0 && controller() != null && controller().controllerEnabled)) {
            MultiblockHandler.get(level.dimension()).addIgnoreToUpdate(getBlockPos());
            setChanged();
            level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    private boolean updateController() {
        boolean result = false;
        if (controller != controller()) {
            controller = controller();
            controllerPos = BlockPos.ZERO;
            result = true;
        }
        if (controller != null) {
            controllerPos = new BlockPos(controller.getBlockPos());
            result = true;
        }
        return result;
    }

    protected void transferEnergyToSide(Direction direction) {
        if (getEnergyStored() <= 0) {
            return; // No energy to transfer
        }
        int wasEnergy = getEnergyStored();
        BlockEntity be = NCLevels.getExistingBlockEntity(level, worldPosition.relative(direction));
        if (be == null || be instanceof TurbinePortBE || be instanceof TurbineControllerBE) {
            return;
        }
        if((isGtLoaded() && isGTEUCapEnabled())) {
            transferEU(controller(), be, controller().energyStorage(), direction);
        }
        if(isGtLoaded() && isOnlyGTCEUCapEnabled()) {
            return;
        }
        if(wasEnergy - getEnergyStored() >= controller().energyStorage().getMaxExtract()) {
            return;
        }
        int extracted = wasEnergy - controller().energyStorage().getEnergyStored();
        if(extracted >= controller().energyStorage().getMaxExtract()) {
            return;
        }
        int canExtract = Math.min(controller().energyStorage().getMaxExtract() - extracted, getEnergyStored());
        IEnergyStorage handler = level.getCapability(Capabilities.EnergyStorage.BLOCK, worldPosition.relative(direction), direction.getOpposite());
        if (handler != null && handler.canReceive()) {
            int received = handler.receiveEnergy(canExtract, false);
            controller().energyStorage().consumeEnergy(received);
            controller().setChanged();
        }
    }
    private void updateAnalogSignal() {
        if(controller() == null) {
            analogSignal = 0;
            return;
        }
        switch (comparatorMode) {
            case SignalSource.ENERGY:
                analogSignal = (byte) (controller().energyStorage().getEnergyStored() * 15 / controller().energyStorage().getMaxEnergyStored());
                break;
            case SignalSource.OVERFLOW:
                if (controller().getFlow() == 0) {
                    analogSignal = 0;
                    break;
                }
                analogSignal = (byte) (controller().realFlow * 15 / controller().getFlow());
                break;
        }
    }

    public void voidFluidSlot(int slotId) {
        if(controller() != null) {
            controller().voidFluidSlot(slotId);
            setChanged();
        }
    }

    protected FluidCapabilityHandler fluidHandler()
    {
        return controller().contentHandler().fluidHandler;
    }



    @Override
    public boolean canInvalidateCache() {
        return false;
    }

    @Override
    public TurbineControllerBE controller() {
        if(NuclearCraft.instance.isNcBeStopped || (!getLevel().isClientSide() && getLevel().getServer() != null && !getLevel().getServer().isRunning())) return null;
        if(getLevel().isClientSide && controller == null && controllerPos != null) {
            BlockEntity be = NCLevels.getExistingBlockEntity(getLevel(), controllerPos);
            if(be instanceof TurbineControllerBE controllerBE) {
                controller = controllerBE;
                return controller;
            }
            return controller;
        }
        try {
            return  getMultiblock().controller().controllerBE();
        } catch (NullPointerException e) {
            if(controllerPos != null) {
                return (TurbineControllerBE) NCLevels.getExistingBlockEntity(getLevel(), controllerPos);
            }
            return null;
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        if (tag.contains("Info")) {
            CompoundTag infoTag = tag.getCompound("Info");
            readTagData(infoTag);
        }
        super.loadAdditional(tag, registries);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        CompoundTag infoTag = new CompoundTag();
        saveFullTagData(infoTag);
        tag.put("Info", infoTag);
    }

    @Override
    public void loadClientData(CompoundTag tag, HolderLookup.Provider registries) {
        if (tag.contains("Info")) {
            CompoundTag infoTag = tag.getCompound("Info");
            readTagData(infoTag);
        }
    }

    @Override
    protected void saveClientData(CompoundTag tag, HolderLookup.Provider registries) {
        CompoundTag infoTag = new CompoundTag();
        tag.put("Info", infoTag);
        saveFullTagData(infoTag);
    }

    public int getEnergyStored() {
        if(controller() == null) return 0;
        return controller().energyStorage.getEnergyStored();
    }

    public int getMaxEnergyStored() {
        if(controller() == null) return 0;
        return controller().energyStorage.getMaxEnergyStored();
    }

    public int energyPerTick() {
        if(controller() == null) return 0;
        return controller().energyPerTick;
    }

    public FluidTank getFluidTank(int i) {
        if(controller() == null) return null;
        return controller().getFluidTank(i);
    }

    public void toggleRedstoneMode() {
        if(comparatorMode == SignalSource.ENERGY) {
            comparatorMode = SignalSource.OVERFLOW;
        } else {
            comparatorMode = SignalSource.ENERGY;
        }
        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }

    public static class SignalSource {
        public static final byte ENERGY = 0;
        public static final byte OVERFLOW = 2;
    }
}
