package igentuman.nc.block.accelerator.entity;

import igentuman.nc.compat.cc.RingAcceleratorPeripheral;
import igentuman.nc.compat.oc2.RingAcceleratorDevice;
import igentuman.nc.content.particles.ParticleStack;
import igentuman.nc.handler.sided.SidedContentHandler;
import igentuman.nc.handler.sided.SlotModePair;
import igentuman.nc.handler.sided.capability.ItemCapabilityHandler;
import igentuman.nc.multiblock.accelerator.AbstractAcceleratorMultiblock;
import igentuman.nc.multiblock.accelerator.ThoroidalAcceleratorMultiblock;
import igentuman.nc.util.capability.CustomEnergyStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

import static igentuman.nc.NuclearCraft.debugLog;
import static igentuman.nc.compat.gregtech.GTUtils.getGTEnergy;
import static igentuman.nc.compat.oc2.RingAcceleratorDevice.DEVICE_CAPABILITY;
import static igentuman.nc.content.particles.CapabilityParticleStackHandler.PARTICLE_HANDLER_CAPABILITY;
import static igentuman.nc.handler.config.AcceleratorConfig.ACCELERATOR_CONFIG;
import static igentuman.nc.multiblock.accelerator.AcceleratorRegistration.ACCELERATOR_BE;
import static igentuman.nc.util.Equations.*;
import static igentuman.nc.util.ModUtil.*;

public class RingAcceleratorControllerBE extends AbstractAcceleratorControllerBE {

    public static String NAME = "ring_accelerator_controller";
    private LazyOptional<RingAcceleratorPeripheral> peripheralCap;
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
            return LazyOptional.empty();
        }
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return LazyOptional.empty();
        }
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            return contentHandler().getFluidCapability(side);
        }
        if(isGtLoaded()) {
            if (cap == com.gregtechceu.gtceu.api.capability.forge.GTCapability.CAPABILITY_ENERGY_CONTAINER && energyStorage() != null) {
                if (isGTEUCapEnabled()) {
                    return getGTEnergy(this, side).cast();
                } else {
                    return LazyOptional.empty();
                }
            }
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
        return LazyOptional.empty();
    }


    public void tickClient() {
        super.tickClient();
        if(!isCasingValid || !isInternalValid) {
            stopSound();
        }
    }

    public void initThermal() {
        AbstractAcceleratorMultiblock mb = getAcceleratorMultiblock();
        if(mb == null || level == null) return;

        long baseCapacity = (long) ACCELERATOR_CONFIG.BASE_HEAT_CAPACITY.get();
        heatCapacity = baseCapacity * mb.getCapacityMultiplier();
        if(heatCapacity <= 0) heatCapacity = baseCapacity;

        float biomeTemp = level.getBiome(worldPosition).value().getBaseTemperature();
        ambientTemp = 273 + (int) (biomeTemp * 20F);

        if(!thermalInitialized) {
            heatStored = (int) Math.min(Integer.MAX_VALUE, ambientTemp * heatCapacity / getMaxTemp() / 2);
            thermalInitialized = true;
        }
    }

    @Override
    public int getMinEnergy() {
        return ACCELERATOR_CONFIG.RING_ACCELERATOR_INPUT_PARTICLE_MIN_ENERGY.get();
    }

    @Override
    protected boolean accelerateParticle() {
        hasParticle = false;
        energyIsTooLow = false;
        energyIsTooHigh = false;
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

        if(maxEnergy <= 0 || particleStack.getMeanEnergy() < getMinEnergy()) {
            energyIsTooLow = true;
            particleStorage.clearServer();
            return false;
        }
        if(particleStack.getMeanEnergy() > maxEnergy) {
            particleStorage.clearServer();
            energyIsTooHigh = true;
            return false;
        }
        if(!drainEnergy()) {
            return false;
        }
        long targetEnergy = (long)(maxEnergy * accelerationEnergy);
        long radiationLoss = synchrotronRadiationEnergy(radius, particleStack);
        particleStack.setMeanEnergy(Math.max(0, targetEnergy - radiationLoss));
        particleStack.addFocus(focusGain(quadStrength, particleStack) - focusLoss(beamLength, particleStack));
        particleStorage.setParticleStack(particleStack);
        internalHeating((long) ((heatRate*(accelerationEnergy)+heatRate)/2));
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

    @Override
    protected void handleMeltdown() {
        if(isAcceleratorTooHot()) {
            quenchMagnets();
            controllerEnabled = false;
        }
    }

    @Override
    protected AbstractAcceleratorMultiblock getAcceleratorMultiblock() {
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
