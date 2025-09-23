package igentuman.nc.block.accelerator.entity;

import igentuman.nc.NuclearCraft;
import igentuman.nc.block.accelerator.AcceleratorPortBlock;
import igentuman.nc.compat.cc.RingAcceleratorPeripheral;
import igentuman.nc.compat.oc2.RingAcceleratorDevice;
import igentuman.nc.content.particles.*;
import igentuman.nc.handler.sided.SidedContentHandler;
import igentuman.nc.handler.sided.capability.ItemCapabilityHandler;
import igentuman.nc.multiblock.accelerator.ThoroidalAcceleratorMultiblock;
import igentuman.nc.util.capability.CustomEnergyStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

import static igentuman.nc.NuclearCraft.currentTick;
import static igentuman.nc.NuclearCraft.debugLog;
import static igentuman.nc.compat.oc2.RingAcceleratorDevice.DEVICE_CAPABILITY;
import static igentuman.nc.multiblock.accelerator.AcceleratorRegistration.ACCELERATOR_BE;
import static igentuman.nc.util.Equations.*;
import static igentuman.nc.util.ModUtil.isCcLoaded;
import static igentuman.nc.util.ModUtil.isOC2Loaded;

public class RingAcceleratorControllerBE extends AbstractAcceleratorControllerBE {

    public static String NAME = "ring_accelerator_controller";
    private LazyOptional<RingAcceleratorPeripheral> peripheralCap;


    protected Direction facing;
    public LinearAcceleratorControllerBE.Recipe recipe;


    public RingAcceleratorControllerBE(BlockPos pPos, BlockState pBlockState) {
        super(ACCELERATOR_BE.get(NAME).get(), pPos, pBlockState);

    }

    public List<ItemStack> getAllowedInputItems()
    {
        return List.of();
    }

    @Override
    public String getName() {
        return NAME;
    }


    public <T> LazyOptional<T>  getPeripheral(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if(peripheralCap == null) {
            peripheralCap = LazyOptional.of(() -> new RingAcceleratorPeripheral(this));
        }
        return peripheralCap.cast();
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            return contentHandler().getFluidCapability(null);
        }
        if (cap == ForgeCapabilities.ENERGY) {
            return getEnergy().cast();
        }
        if(isCcLoaded()) {
            if(cap == dan200.computercraft.shared.Capabilities.CAPABILITY_PERIPHERAL) {
                return getPeripheral(cap, side);
            }
        }
        if(isOC2Loaded()) {
            if(cap == DEVICE_CAPABILITY) {
                return getOCDevice(cap, side);
            }
        }
        return super.getCapability(cap, side);
    }


    public void tickClient() {
        super.tickClient();
        if(!isCasingValid || !isInternalValid) {
            stopSound();
        }
    }

    protected int reValidateCounter = 0;

    public void tickServer() {
        if(NuclearCraft.instance.isNcBeStopped || isRemoved()) {
            return;
        }
        changed = false;
        super.tickServer();
        boolean wasEnabled = controllerEnabled;
        handleValidation();
        if(redstoneLevel < 1) {
            redstoneLevel = getRedstoneSignal();
        }
        controllerEnabled = getMultiblock().isFormed() && redstoneLevel > 0;

        if (controllerEnabled) {
            trackChanges(contentHandler().tick());
            handleMeltdown();
            trackChanges(accelerateParticle());
        }
        // Passive cooling
        heat -= coolingRate;
        heat = Math.max(0, heat);
        // Coolant cooling
        coolantCoolDown();
        refreshCacheFlag = !getMultiblock().isFormed();
        if(wasEnabled != controllerEnabled) {
            setChanged();
        }
        if(refreshCacheFlag || changed || currentTick % 20 == 0) {
            try {
                setChanged();
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState().setValue(AcceleratorPortBlock.POWERED, controllerEnabled), Block.UPDATE_NEIGHBORS);
                level.setBlockAndUpdate(worldPosition, getBlockState().setValue(AcceleratorPortBlock.POWERED, controllerEnabled));
            } catch (NullPointerException ignored) {}
        }
        //particleStorage.clear();
    }

    private boolean accelerateParticle() {
        hasParticle = false;
        if(energyStorage().getEnergyStored() < energyRequired) {
            return false;
        }

        if(particleStorage.getParticle() == null) {
            return false;
        }
        if(!drainEnergy()) {
            return false;
        }
        ParticleStack particleStack = particleStorage.getParticle();
        particleStack.addFocus(focusGain(focus, particleStack)-focusLoss(beamLength, particleStack));
        particleStack.setMeanEnergy((long)(linacEnergyGain(acceleratingVoltage, particleStack)*(redstoneLevel / 15d)));
        particleStorage.setParticleStack(particleStack);
        heat += heatRate;
        hasParticle = true;
        getMultiblock().extractParticle(particleStack);
        return true;
    }


    @Override
    public ThoroidalAcceleratorMultiblock getMultiblock() {
        if(getLevel().isClientSide()) {
            debugLog("Trying to access multiblock from client");
            return null;
        }
        if(multiblock == null) {
            multiblock = new ThoroidalAcceleratorMultiblock(this);
        }
        return (ThoroidalAcceleratorMultiblock) multiblock;
    }

    private void handleMeltdown() {

    }

    public Direction getFacing() {
        if (facing == null) {
            facing = getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        }
        return facing;
    }

    public int getDepth() {
        return depth;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean hasRedstoneSignal() {
        return Objects.requireNonNull(getLevel()).hasNeighborSignal(worldPosition);
    }


    @Override
    public ItemCapabilityHandler getItemInventory()
    {
        return contentHandler().itemHandler;
    }

    @Override
    public SidedContentHandler contentHandler() {
        return contentHandler;
    }

    @Override
    public CustomEnergyStorage energyStorage() {
        return energyStorage;
    }

    public <T> LazyOptional<T> getOCDevice(Capability<T> cap, Direction side) {
        return LazyOptional.of(() -> RingAcceleratorDevice.createDevice(this)).cast();
    }

    public FluidTank getFluidTank(int i) {
        return contentHandler().fluidHandler.tanks.get(i);
    }
}
