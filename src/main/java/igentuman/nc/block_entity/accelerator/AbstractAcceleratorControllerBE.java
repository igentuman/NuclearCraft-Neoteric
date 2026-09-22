package igentuman.nc.block_entity.accelerator;

import igentuman.nc.api.particle.IParticleHandler;
import igentuman.nc.api.particle.ParticleDefinition;
import igentuman.nc.api.particle.ParticleStack;
import igentuman.nc.block.accelerator.BeamPortMode;
import igentuman.nc.block_entity.MultiblockControllerBE;
import igentuman.nc.block_entity.MultiblockPortBE;
import igentuman.nc.config.Multiblocks;
import igentuman.nc.handler.sided.FluidCapabilityHandler;
import igentuman.nc.multiblock.StructureLifecycleState;
import igentuman.nc.multiblock.StructureRecord;
import igentuman.nc.multiblock.accelerator.AcceleratorThermal;
import igentuman.nc.multiblock.geometry.StructureRole;
import igentuman.nc.multiblock.accelerator.AcceleratorRuntimeState;
import igentuman.nc.particle.BeamConnection;
import igentuman.nc.particle.BeamConnectionResolver;
import igentuman.nc.particle.ParticlePortView;
import igentuman.nc.particle.ParticleStorage;
import igentuman.nc.particle.ParticleTransfer;
import igentuman.nc.recipe.particle.AcceleratorCoolantRecipe;
import igentuman.nc.recipe.particle.ParticleRecipes;
import igentuman.nc.setup.ModEntries;
import igentuman.nc.setup.Registers;
import igentuman.nc.util.NBTField;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;

public abstract class AbstractAcceleratorControllerBE extends MultiblockControllerBE {

    protected AcceleratorRuntimeState runtimeState;
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

    private BeamConnection outputConnection;
    private boolean coolantValidatorInstalled;
    private RecipeHolder<AcceleratorCoolantRecipe> coolantRecipeHolder;

    protected AbstractAcceleratorControllerBE(BlockPos pos, BlockState state, String name) {
        super(ModEntries.get(name).blockEntity().get(), pos, state, name);
        runtimeState = new AcceleratorRuntimeState(storage(), storage(), 0, 0, 0, null);
    }

    public AcceleratorRuntimeState runtimeState() {
        return runtimeState;
    }

    @Override
    public final void serverTick() {
        super.serverTick();
        if (!(level instanceof ServerLevel serverLevel)) return;
        runtimeState.tickBeam(() -> {
            if (formed) tickBeam(serverLevel);
        });
    }

    protected abstract void tickBeam(ServerLevel level);

    @Nullable
    @Override
    public IParticleHandler getParticleHandler(BlockPos portPos, BeamPortMode mode, int channel) {
        var record = scheduledStructure().filter(candidate -> candidate.state() == StructureLifecycleState.FORMED)
                .orElse(null);
        if (record == null || channel < 0) return null;
        StructureRole role = mode == BeamPortMode.INPUT ? StructureRole.BEAM_INPUT
                : mode == BeamPortMode.OUTPUT ? StructureRole.BEAM_OUTPUT : null;
        if (role == null) return null;
        List<BlockPos> positions = record.roles().getOrDefault(role, List.of());
        if (channel >= positions.size() || !positions.get(channel).equals(portPos)) return null;
        return mode == BeamPortMode.INPUT
                ? new ParticlePortView(runtimeState.input(), 0, ParticlePortView.Access.INPUT)
                : new ParticlePortView(runtimeState.pendingOutput(), 0, ParticlePortView.Access.OUTPUT);
    }

    protected int updateControlSignal(ServerLevel level, StructureRecord record) {
        int signal = level.getBestNeighborSignal(worldPosition);
        for (BlockPos servicePort : record.roles().getOrDefault(StructureRole.SERVICE_PORT, List.of())) {
            if (level.hasChunkAt(servicePort) && level.getBlockEntity(servicePort) instanceof MultiblockPortBE port
                    && worldPosition.equals(port.getControllerPos())) signal = Math.max(signal, port.controlSignalSample());
        }
        signal = Math.clamp(signal, 0, 15);
        if (signal != runtimeState.controlSignal()) {
            runtimeState = new AcceleratorRuntimeState(runtimeState.input(), runtimeState.pendingOutput(),
                    runtimeState.storedHeat(), signal, runtimeState.overheatCooldown(),
                    runtimeState.activeCoolantRecipeId());
            setChanged();
        }
        if (signal != controlSignal) {
            controlSignal = signal;
            setChanged();
        }
        return signal;
    }

    protected void tickThermal(ServerLevel level, StructureRecord.StructureAggregates aggregates,
                               int coolantInputTank, int coolantOutputTank) {
        long heat = AcceleratorThermal.applyPassiveCooling(runtimeState.storedHeat(), aggregates.coolingPerTick());
        ResourceLocation activeId = null;
        long coolantRemoved = 0;

        FluidCapabilityHandler tanks = contentHandler.getFluidHandler();
        if (tanks != null && coolantInputTank >= 0 && coolantOutputTank >= 0
                && coolantInputTank < tanks.getTanks() && coolantOutputTank < tanks.getTanks()) {
            installCoolantValidator(level, coolantInputTank);
            FluidStack cold = tanks.getFluidInTank(coolantInputTank);
            RecipeHolder<AcceleratorCoolantRecipe> holder = cold.isEmpty() ? null : findCoolantRecipe(level, cold);
            if (holder != null) {
                AcceleratorCoolantRecipe recipe = holder.value();
                long currentTemperatureK = AcceleratorThermal.temperatureK(heat, aggregates.heatCapacity());
                Long minimumTemperatureK = recipe.minimumTemperatureK();
                if (minimumTemperatureK == null || currentTemperatureK >= minimumTemperatureK) {
                    int inAmount = recipe.input().amount();
                    FluidStack outTemplate = recipe.output().resolve();
                    int outAmount = outTemplate.getAmount();
                    if (inAmount > 0 && !outTemplate.isEmpty() && outAmount > 0) {
                        long availableOutput = Math.max(0, tanks.getTankCapacity(coolantOutputTank)
                                - tanks.getFluidInTank(coolantOutputTank).getAmount());
                        long ops = AcceleratorThermal.coolantOperations(heat, recipe.heatPerMb(), inAmount,
                                cold.getAmount(), availableOutput, outAmount);
                        if (ops > 0) {
                            FluidStack toOutput = new FluidStack(outTemplate.getFluid(), (int) (ops * outAmount));
                            if (tanks.fillTank(coolantOutputTank, toOutput, IFluidHandler.FluidAction.SIMULATE)
                                    == toOutput.getAmount()) {
                                tanks.drainTank(coolantInputTank, (int) (ops * inAmount), IFluidHandler.FluidAction.EXECUTE);
                                tanks.fillTank(coolantOutputTank, toOutput, IFluidHandler.FluidAction.EXECUTE);
                                long afterCoolant = AcceleratorThermal.heatAfterCoolant(heat, recipe.heatPerMb(),
                                        inAmount, ops);
                                coolantRemoved = heat - afterCoolant;
                                heat = afterCoolant;
                                activeId = holder.id();
                            }
                        }
                    }
                }
            }
        }

        long currentTemperatureK = AcceleratorThermal.temperatureK(heat, aggregates.heatCapacity());
        int cooldown = runtimeState.overheatCooldown();
        if (AcceleratorThermal.isOverheated(currentTemperatureK, aggregates.maximumTemperatureK())) {
            cooldown = Multiblocks.particleMachines().accelerator().overheatCooldownTicks();
        } else if (cooldown > 0) {
            cooldown--;
        }

        if (heat != runtimeState.storedHeat() || cooldown != runtimeState.overheatCooldown()
                || !Objects.equals(activeId, runtimeState.activeCoolantRecipeId())) {
            runtimeState = new AcceleratorRuntimeState(runtimeState.input(), runtimeState.pendingOutput(), heat,
                    runtimeState.controlSignal(), cooldown, activeId);
            setChanged();
        }
        int clampedTemperatureK = (int) Math.min(Integer.MAX_VALUE, currentTemperatureK);
        int clampedMaximumTemperatureK = (int) Math.min(Integer.MAX_VALUE, aggregates.maximumTemperatureK());
        int clampedCoolantHeatPerTick = (int) Math.min(Integer.MAX_VALUE, coolantRemoved);
        if (clampedTemperatureK != temperatureK || clampedMaximumTemperatureK != maximumTemperatureK
                || cooldown != overheatCooldownTicks || clampedCoolantHeatPerTick != coolantHeatPerTick
                || (cooldown > 0) != overheated) {
            temperatureK = clampedTemperatureK;
            maximumTemperatureK = clampedMaximumTemperatureK;
            overheatCooldownTicks = cooldown;
            coolantHeatPerTick = clampedCoolantHeatPerTick;
            overheated = cooldown > 0;
            setChanged();
        }

        int clampedVoltage = (int) Math.min(Integer.MAX_VALUE, aggregates.voltage());
        int clampedBeamLength = aggregates.beamLength();
        int clampedQuadrupoleField = (int) Math.round(Math.min(Integer.MAX_VALUE, aggregates.quadrupoleField()));
        int clampedDipoleField = (int) Math.round(Math.min(Integer.MAX_VALUE, aggregates.dipoleField()));
        if (clampedVoltage != voltage || clampedBeamLength != beamLength
                || clampedQuadrupoleField != quadrupoleField || clampedDipoleField != dipoleField) {
            voltage = clampedVoltage;
            beamLength = clampedBeamLength;
            quadrupoleField = clampedQuadrupoleField;
            dipoleField = clampedDipoleField;
            setChanged();
        }

        syncParticleDisplay(runtimeState.input().snapshot(0));
    }

    protected final void syncParticleDisplay(ParticleStack stack) {
        int clampedAmount = (int) Math.min(Integer.MAX_VALUE, stack.amount());
        long energyKeV = stack.meanEnergyKeV();
        int clampedFocus = stack.isEmpty() ? 0
                : (int) Math.min(Integer.MAX_VALUE, Math.round(stack.focus() * 10_000D));
        ParticleDefinition particleDefinition = stack.isEmpty() ? null
                : Registers.PARTICLE_DEFINITION_REGISTRY.get(stack.particleId());
        int registryId = particleDefinition == null ? -1
                : Registers.PARTICLE_DEFINITION_REGISTRY.getId(particleDefinition);
        if (clampedAmount != particleAmount || energyKeV != particleEnergyKeV
                || registryId != particleTypeId || clampedFocus != particleFocusScaled) {
            particleAmount = clampedAmount;
            particleEnergyKeV = energyKeV;
            particleTypeId = registryId;
            particleFocusScaled = clampedFocus;
            setChanged();
        }
    }

    protected boolean transferPendingOutput(ServerLevel level, StructureRecord record, BlockPos outputPort,
                                            BiFunction<ParticleStack, Integer, ParticleStack> attenuation) {
        if (!level.hasChunkAt(outputPort)) return false;
        BlockState state = level.getBlockState(outputPort);
        if (!state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) return false;
        Direction direction = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        outputConnection = BeamConnectionResolver.resolve(level, outputConnection, outputPort, direction,
                Multiblocks.particleMachines().accelerator().beamConnectionReach());
        if (outputConnection == null) return false;
        IParticleHandler destination = BeamConnectionResolver.destination(level, outputConnection);
        if (destination == null) return false;

        ParticleTransfer.Result result;
        try {
            int corridorLength = BeamConnectionResolver.corridorLength(outputConnection);
            result = ParticleTransfer.execute(new ParticleTransfer.Request(runtimeState.pendingOutput(), 0,
                    destination, 0, List.of(level.dimension().location(), record.id()), Long.MAX_VALUE,
                    stack -> attenuation.apply(stack, corridorLength)));
        } catch (ArithmeticException | IllegalArgumentException ignored) {
            return false;
        }
        if (result.transferred().isEmpty()) return false;
        recordTransfer(level, outputPort, direction, outputConnection.destination(), result.transferred());
        return true;
    }

    protected void addProcessingHeat(StructureRecord.StructureAggregates aggregates) {
        long heat;
        try {
            heat = Math.addExact(runtimeState.storedHeat(), aggregates.heatPerTick());
        } catch (ArithmeticException ignored) {
            heat = Long.MAX_VALUE;
        }
        runtimeState = new AcceleratorRuntimeState(runtimeState.input(), runtimeState.pendingOutput(),
                Math.max(0, heat), runtimeState.controlSignal(), runtimeState.overheatCooldown(),
                runtimeState.activeCoolantRecipeId());
        setChanged();
    }

    private RecipeHolder<AcceleratorCoolantRecipe> findCoolantRecipe(ServerLevel level, FluidStack coolant) {
        if (coolantRecipeHolder != null && coolantRecipeHolder.value().input().test(coolant)) return coolantRecipeHolder;
        for (RecipeHolder<AcceleratorCoolantRecipe> holder
                : level.getRecipeManager().getAllRecipesFor(ParticleRecipes.ACCELERATOR_COOLANT_TYPE.get())) {
            if (holder.value().input().test(coolant)) {
                coolantRecipeHolder = holder;
                return holder;
            }
        }
        coolantRecipeHolder = null;
        return null;
    }

    private void installCoolantValidator(ServerLevel level, int coolantInputTank) {
        if (coolantValidatorInstalled) return;
        FluidCapabilityHandler internal = contentHandler.getFluidHandler();
        if (internal == null) return;
        Set<Fluid> accepted = new HashSet<>();
        for (RecipeHolder<AcceleratorCoolantRecipe> holder
                : level.getRecipeManager().getAllRecipesFor(ParticleRecipes.ACCELERATOR_COOLANT_TYPE.get())) {
            for (FluidStack candidate : holder.value().input().getFluids()) accepted.add(candidate.getFluid());
        }
        internal.getInternalHandler().setTankValidator(coolantInputTank,
                stack -> accepted.contains(stack.getFluid()));
        coolantValidatorInstalled = true;
    }

    private static void recordTransfer(ServerLevel level, BlockPos source, Direction direction,
                                       BlockPos destination, ParticleStack transferred) {
        BlockEntity sourceEntity = level.getBlockEntity(source);
        if (sourceEntity instanceof MultiblockPortBE port) {
            port.recordParticleTransfer(transferred, level.getGameTime(), direction);
        }
        BlockEntity destinationEntity = level.getBlockEntity(destination);
        if (destinationEntity instanceof MultiblockPortBE port) {
            port.recordParticleTransfer(transferred, level.getGameTime(), direction);
        }
    }

    private ParticleStorage storage() {
        return new ParticleStorage(1, Integer.MAX_VALUE) {
            @Override
            protected void onContentsChanged(int channel) {
                setChanged();
            }
        };
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        CompoundTag runtime = new CompoundTag();
        runtime.putLong("storedHeat", runtimeState.storedHeat());
        runtime.putInt("controlSignal", runtimeState.controlSignal());
        runtime.putInt("overheatCooldown", runtimeState.overheatCooldown());
        if (runtimeState.activeCoolantRecipeId() != null) {
            runtime.putString("activeCoolantRecipe", runtimeState.activeCoolantRecipeId().toString());
        }
        tag.put("particleRuntime", runtime);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        runtimeState.clearBeams();
        if (!tag.contains("particleRuntime")) return;
        CompoundTag runtime = tag.getCompound("particleRuntime");
        net.minecraft.resources.ResourceLocation coolant = runtime.contains("activeCoolantRecipe")
                ? net.minecraft.resources.ResourceLocation.tryParse(runtime.getString("activeCoolantRecipe")) : null;
        runtimeState = new AcceleratorRuntimeState(runtimeState.input(), runtimeState.pendingOutput(),
                Math.max(0, runtime.getLong("storedHeat")), Math.clamp(runtime.getInt("controlSignal"), 0, 15),
                Math.max(0, runtime.getInt("overheatCooldown")), coolant);
    }
}
