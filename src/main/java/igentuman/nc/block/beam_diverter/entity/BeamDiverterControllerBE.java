package igentuman.nc.block.beam_diverter.entity;

import igentuman.nc.block.accelerator.entity.AbstractAcceleratorControllerBE;
import igentuman.nc.block.accelerator.entity.AcceleratorBeamPortBE;
import igentuman.nc.compat.cc.BeamDiverterPeripheral;
import igentuman.nc.compat.oc2.BeamDiverterDevice;
import igentuman.nc.content.particles.Equations;
import igentuman.nc.content.particles.ParticleStack;
import igentuman.nc.handler.config.CommonConfig;
import igentuman.nc.multiblock.MultiblockHandler;
import igentuman.nc.multiblock.accelerator.BeamDiverterMultiblock;
import igentuman.nc.util.PortMode;
import igentuman.nc.util.annotation.NBTField;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static igentuman.nc.compat.oc2.TargetChamberDevice.DEVICE_CAPABILITY;
import static igentuman.nc.content.particles.CapabilityParticleStackHandler.PARTICLE_HANDLER_CAPABILITY;
import static igentuman.nc.handler.config.AcceleratorConfig.DECAY_CHAMBER_CONFIG;
import static igentuman.nc.handler.config.CommonConfig.GTCEU_CONFIG;
import static igentuman.nc.multiblock.accelerator.AcceleratorRegistration.ACCELERATOR_BE;
import static igentuman.nc.util.ModUtil.isCcLoaded;
import static igentuman.nc.util.ModUtil.isOC2Loaded;
import static igentuman.nc.util.PortMode.PORT_MODE;

public class BeamDiverterControllerBE extends AbstractAcceleratorControllerBE {

    public static final String NAME = "beam_diverter_controller";

    @NBTField
    public double efficiency = 100D;
    public int connectedPorts = 0;

    public BeamDiverterControllerBE(BlockPos pPos, BlockState pBlockState) {
        this(ACCELERATOR_BE.get(NAME).get(), pPos, pBlockState);
    }

    public BeamDiverterControllerBE(BlockEntityType<?> type, BlockPos pPos, BlockState pBlockState) {
        super(type, pPos, pBlockState);
        energyPerTick = DECAY_CHAMBER_CONFIG.BASE_POWER.get();
    }

    @Override
    protected boolean accelerateParticle() {

        if (particleStorage.getParticle() == null) return false;
        hasParticle = true;
        if (energyStorage().getEnergyStored() < energyPerTick) return false;

        ParticleStack copy = particleStorage.getParticle().copy();

        copy.addFocus(-Equations.focusLoss(3, copy));

        AcceleratorBeamPortBE inputPort = getMultiblock().getInputBeamPort();
        AcceleratorBeamPortBE outputPort = getMultiblock().getFirstOutputBeamPort();
        if(inputPort != null && outputPort != null) {
            if(inputPort.getFacing().getOpposite() != outputPort.getFacing()) {
                copy.addMeanEnergy(-Equations.cornerEnergyLoss(copy,160 * (Math.log(dipoleStrength*10)+0.2)));
            }
            outputPort.extractParticle(copy);
            particleStorage.outputParticles.add(copy);
        }


        particleStorage.clearServer();
        return true;
    }

    @Override
    protected void handleMeltdown() {

    }

    public List<Long> getSortedBeamPorts() {
        if (getMultiblock() == null) {
            return List.of();
        }
        List<Long> ports = new java.util.ArrayList<>(getMultiblock().getBeamPorts());
        ports.sort(Long::compare);
        return ports;
    }

    public List<Map<String, Object>> getBeamPortsInfo() {
        List<Map<String, Object>> infoList = new ArrayList<>();
        if (getMultiblock() == null) return infoList;
        List<Long> sortedPorts = getSortedBeamPorts();
        for (int i = 0; i < sortedPorts.size(); i++) {
            long packedPos = sortedPorts.get(i);
            BlockPos pos = BlockPos.of(packedPos);
            Map<String, Object> portInfo = new HashMap<>();
            portInfo.put("id", i);
            portInfo.put("x", pos.getX());
            portInfo.put("y", pos.getY());
            portInfo.put("z", pos.getZ());

            BlockState state = level.getBlockState(pos);
            String modeStr = "disabled";
            if (state.hasProperty(PORT_MODE)) {
                modeStr = state.getValue(PORT_MODE).getSerializedName();
            }
            portInfo.put("mode", modeStr);

            BlockEntity be = level.getBlockEntity(pos);
            Map<String, Object> particleInfo = null;
            if (be instanceof AcceleratorBeamPortBE beamPortBe) {
                ParticleStack particleStack = beamPortBe.clientParticle;
                if (particleStack != null && !particleStack.isEmpty() && particleStack.getParticle() != null) {
                    particleInfo = new HashMap<>();
                    particleInfo.put("energy", particleStack.getMeanEnergy());
                    particleInfo.put("focus", particleStack.getFocus());
                    particleInfo.put("amount", particleStack.getAmount());
                    particleInfo.put("particle", particleStack.getParticle().getName());
                }
            }
            portInfo.put("particle", particleInfo);

            infoList.add(portInfo);
        }
        return infoList;
    }

    public boolean setBeamPortMode(int id, String mode) {
        if (getMultiblock() == null) return false;
        List<Long> sortedPorts = getSortedBeamPorts();
        if (id < 0 || id >= sortedPorts.size()) return false;
        long packedPos = sortedPorts.get(id);
        BlockPos pos = BlockPos.of(packedPos);
        BlockState state = level.getBlockState(pos);
        if (!state.hasProperty(PORT_MODE)) return false;

        PortMode.Mode enumMode;
        if (mode.equalsIgnoreCase("input")) {
            enumMode = PortMode.Mode.INPUT;
        } else if (mode.equalsIgnoreCase("output")) {
            enumMode = PortMode.Mode.OUTPUT;
        } else if (mode.equalsIgnoreCase("disabled")) {
            enumMode = PortMode.Mode.DISABLED;
        } else {
            return false;
        }

        MultiblockHandler.get(level.dimension()).addIgnoreToUpdate(pos);
        level.setBlockAndUpdate(pos, state.setValue(PORT_MODE, enumMode));
        return true;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public BeamDiverterMultiblock getMultiblock() {
        if (getLevel() == null || getLevel().isClientSide()) {
            return null;
        }
        if (multiblock == null) {
            multiblock = new BeamDiverterMultiblock(this);
            validationsCounter = 0;
        }
        return (BeamDiverterMultiblock) multiblock;
    }

    @Override
    public boolean canInvalidateCache() {
        return false;
    }

    private LazyOptional<BeamDiverterPeripheral> peripheralCap;

    public <T> LazyOptional<T> getPeripheral(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (peripheralCap == null) {
            peripheralCap = LazyOptional.of(() -> new BeamDiverterPeripheral(this));
        }
        return peripheralCap.cast();
    }

    public <T> LazyOptional<T> getOCDevice(Capability<T> cap, Direction side) {
        return LazyOptional.of(() -> BeamDiverterDevice.createDevice(this)).cast();
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == PARTICLE_HANDLER_CAPABILITY) {
            return particleHandler().cast();
        }
        if (isOC2Loaded() && cap == DEVICE_CAPABILITY) {
            return getOCDevice(cap, side);
        }
        if (isCcLoaded() && cap == dan200.computercraft.shared.Capabilities.CAPABILITY_PERIPHERAL) {
            return getPeripheral(cap, side);
        }
        return super.getCapability(cap, side);
    }

    public ParticleStack getOutputParticle(int i) {
        return particleStorage.getParticle();
    }


    public CommonConfig.GTCEUCompatibilityConfig.GTCEUTier getTier() {
        return GTCEU_CONFIG.ACCELERATORS_ENERGY_TIER.get();
    }
}
