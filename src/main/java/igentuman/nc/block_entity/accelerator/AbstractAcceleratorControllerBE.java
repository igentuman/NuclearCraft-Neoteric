package igentuman.nc.block_entity.accelerator;

import igentuman.nc.api.particle.ParticleDefinition;
import igentuman.nc.api.particle.ParticleStack;
import igentuman.nc.block_entity.MultiblockControllerBE;
import igentuman.nc.setup.ModEntries;
import igentuman.nc.setup.Registers;
import igentuman.nc.util.NBTField;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public abstract class AbstractAcceleratorControllerBE extends MultiblockControllerBE {

    @NBTField(syncToClient = true)
    public int temperatureK = 0;
    @NBTField(syncToClient = true)
    public int maximumTemperatureK = 0;
    @NBTField(syncToClient = true)
    public int overheatCooldownTicks = 0;
    @NBTField(syncToClient = true)
    public int coolantHeatPerTick = 0;
    @NBTField(syncToClient = true)
    public boolean overheated = false;
    @NBTField(syncToClient = true)
    public int controlSignal = 0;
    @NBTField(syncToClient = true)
    public int voltage = 0;
    @NBTField(syncToClient = true)
    public int beamLength = 0;
    @NBTField(syncToClient = true)
    public int quadrupoleField = 0;
    @NBTField(syncToClient = true)
    public int dipoleField = 0;
    @NBTField(syncToClient = true)
    public int particleAmount = 0;
    @NBTField(syncToClient = true)
    public long particleEnergyKeV = 0;
    @NBTField(syncToClient = true)
    public int particleTypeId = -1;
    @NBTField(syncToClient = true)
    public int particleFocusScaled = 0;

    protected AbstractAcceleratorControllerBE(BlockPos pos, BlockState state, String name) {
        super(ModEntries.get(name).blockEntity().get(), pos, state, name);
    }

    public void updateControlSignalDisplay(int signal) {
        if (signal == controlSignal) return;
        controlSignal = signal;
        markDirty();
    }

    public void updateThermalDisplay(long currentTemperatureK, long structureMaximumTemperatureK, int cooldown,
                                     long coolantRemoved) {
        int clampedTemperatureK = (int) Math.min(Integer.MAX_VALUE, currentTemperatureK);
        int clampedMaximumTemperatureK = (int) Math.min(Integer.MAX_VALUE, structureMaximumTemperatureK);
        int clampedCoolantHeatPerTick = (int) Math.min(Integer.MAX_VALUE, coolantRemoved);
        if (clampedTemperatureK == temperatureK && clampedMaximumTemperatureK == maximumTemperatureK
                && cooldown == overheatCooldownTicks && clampedCoolantHeatPerTick == coolantHeatPerTick
                && (cooldown > 0) == overheated) return;
        temperatureK = clampedTemperatureK;
        maximumTemperatureK = clampedMaximumTemperatureK;
        overheatCooldownTicks = cooldown;
        coolantHeatPerTick = clampedCoolantHeatPerTick;
        overheated = cooldown > 0;
        markDirty();
    }

    public void updateOpticsDisplay(long structureVoltage, int structureBeamLength, double quadrupole,
                                    double dipole) {
        int clampedVoltage = (int) Math.min(Integer.MAX_VALUE, structureVoltage);
        int clampedQuadrupoleField = (int) Math.round(Math.min(Integer.MAX_VALUE, quadrupole));
        int clampedDipoleField = (int) Math.round(Math.min(Integer.MAX_VALUE, dipole));
        if (clampedVoltage == voltage && structureBeamLength == beamLength
                && clampedQuadrupoleField == quadrupoleField && clampedDipoleField == dipoleField) return;
        voltage = clampedVoltage;
        beamLength = structureBeamLength;
        quadrupoleField = clampedQuadrupoleField;
        dipoleField = clampedDipoleField;
        markDirty();
    }

    public void syncParticleDisplay(ParticleStack stack) {
        int clampedAmount = (int) Math.min(Integer.MAX_VALUE, stack.amount());
        long energyKeV = stack.meanEnergyKeV();
        int clampedFocus = stack.isEmpty() ? 0
                : (int) Math.min(Integer.MAX_VALUE, Math.round(stack.focus() * 10_000D));
        ParticleDefinition particleDefinition = stack.isEmpty() ? null
                : Registers.PARTICLE_DEFINITION_REGISTRY.get(stack.particleId());
        int registryId = particleDefinition == null ? -1
                : Registers.PARTICLE_DEFINITION_REGISTRY.getId(particleDefinition);
        if (clampedAmount == particleAmount && energyKeV == particleEnergyKeV
                && registryId == particleTypeId && clampedFocus == particleFocusScaled) return;
        particleAmount = clampedAmount;
        particleEnergyKeV = energyKeV;
        particleTypeId = registryId;
        particleFocusScaled = clampedFocus;
        markDirty();
    }
}
