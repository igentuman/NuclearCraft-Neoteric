package igentuman.nc.block.accelerator.entity;

import igentuman.nc.NuclearCraft;
import igentuman.nc.block.accelerator.AcceleratorPortBlock;
import igentuman.nc.compat.cc.RingAcceleratorPeripheral;
import igentuman.nc.compat.oc2.RingAcceleratorDevice;
import igentuman.nc.content.particles.*;
import igentuman.nc.handler.sided.SidedContentHandler;
import igentuman.nc.handler.sided.SlotModePair;
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
import static igentuman.nc.content.particles.CapabilityParticleStackHandler.PARTICLE_HANDLER_CAPABILITY;
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
        contentHandler().setAllowedInputItems(null);
        contentHandler().itemHandler.setGlobalMode(0, SlotModePair.SlotMode.DISABLED);
        contentHandler().itemHandler.setGlobalMode(1, SlotModePair.SlotMode.DISABLED);
        contentHandler().fluidHandler.setGlobalMode(0, SlotModePair.SlotMode.DISABLED);
        contentHandler().fluidHandler.setGlobalMode(1, SlotModePair.SlotMode.DISABLED);
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
        if (cap == PARTICLE_HANDLER_CAPABILITY) {
            return particleHandler.cast();
        }
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            return contentHandler().getFluidCapability(side);
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
        boolean formed = getMultiblock().isFormed();
        if(formed && !thermalInitialized) {
            initThermal();
        } else if(!formed && thermalInitialized) {
            resetThermal();
        }
        controllerEnabled = formed && redstoneLevel > 0;

        currentHeating = 0;
        if(formed) {
            externalHeating();
        }
        if (controllerEnabled) {
            trackChanges(contentHandler().tick());
            trackChanges(accelerateParticle());
            handleMeltdown();
        }
        coolantCoolDown();
        refreshCacheFlag = !formed;
        if(wasEnabled != controllerEnabled) {
            setChanged();
        }
        if(refreshCacheFlag || changed || currentTick % 20 == 0) {
            try {
                setChanged();
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState().setValue(AcceleratorPortBlock.POWERED, controllerEnabled), Block.UPDATE_ALL);
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
        ParticleStack particleStack = particleStorage.getParticle();
        if(particleStack.isEmpty() || particleStack.getParticle() == null) {
            return false;
        }
        double radius = getBeamRadius();
        long maxEnergy = ringEnergyMaxEnergy(dipoleStrength, acceleratingVoltage, radius, particleStack);
        if(maxEnergy <= 0) {
            particleStorage.clearServer();
            return false;
        }
        if(particleStack.getMeanEnergy() > maxEnergy) {
            particleStorage.clearServer();
            return false;
        }
        if(!drainEnergy()) {
            return false;
        }
        long targetEnergy = (long)(maxEnergy * (redstoneLevel / 15d));
        long radiationLoss = synchrotronRadiationEnergy(radius, particleStack);
        particleStack.setMeanEnergy(Math.max(0, targetEnergy - radiationLoss));
        particleStack.addFocus(focusGain(quadStrength, particleStack) - focusLoss(beamLength, particleStack));
        particleStorage.setParticleStack(particleStack);
        internalHeating(heatRate);
        hasParticle = true;
        if(particleStack.getFocus() > 0) {
            getMultiblock().extractParticle(particleStack);
        }
        particleStorage.clearServer();

        return true;
    }

    public double getBeamRadius() {
        int side = getMultiblock().depth();
        return Math.max(1, (side - 4) / 2d);
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
        if(isAcceleratorTooHot()) {
            quenchMagnets();
            controllerEnabled = false;
        }
    }

    @Override
    protected igentuman.nc.multiblock.accelerator.AbstractAcceleratorMultiblock getAcceleratorMultiblock() {
        return getMultiblock();
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
