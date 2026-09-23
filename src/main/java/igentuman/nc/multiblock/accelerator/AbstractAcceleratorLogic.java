package igentuman.nc.multiblock.accelerator;

import igentuman.nc.api.particle.IParticleHandler;
import igentuman.nc.api.particle.ParticleAction;
import igentuman.nc.api.particle.ParticleStack;
import igentuman.nc.block_entity.MultiblockPortBE;
import igentuman.nc.block_entity.accelerator.AbstractAcceleratorControllerBE;
import igentuman.nc.config.Multiblocks;
import igentuman.nc.handler.sided.FluidCapabilityHandler;
import igentuman.nc.api.multiblock.AbstractMultiblockLogic;
import igentuman.nc.particle.BeamConnection;
import igentuman.nc.particle.BeamConnectionResolver;
import igentuman.nc.particle.ParticleStorage;
import igentuman.nc.particle.ParticleTransfer;
import igentuman.nc.recipe.particle.AcceleratorCoolantRecipe;
import igentuman.nc.recipe.particle.ParticleRecipes;
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
import java.util.Set;
import java.util.function.BiFunction;

public abstract class AbstractAcceleratorLogic<C extends AcceleratorCache,
        B extends AbstractAcceleratorControllerBE> extends AbstractMultiblockLogic<C> {

    protected final ParticleStorage input = new ParticleStorage(1, Integer.MAX_VALUE);
    protected final ParticleStorage pendingOutput = new ParticleStorage(1, Integer.MAX_VALUE);
    private RecipeHolder<AcceleratorCoolantRecipe> cachedCoolantRecipe;
    private boolean coolantValidatorInstalled;
    protected long storedHeat;
    protected int controlSignal;
    protected int overheatCooldown;
    protected ResourceLocation activeCoolantRecipeId;

    public ParticleStorage input() {
        return input;
    }

    public ParticleStorage pendingOutput() {
        return pendingOutput;
    }

    protected abstract Class<B> controllerClass();

    protected abstract void tickBeam(ServerLevel level, BlockPos controllerPos, C cache, B controller);

    @Override
    public final void tickServer(ServerLevel level, BlockPos controllerPos, C cache) {
        BlockEntity be = level.getBlockEntity(controllerPos);
        if (!controllerClass().isInstance(be)) return;
        B controller = controllerClass().cast(be);
        pendingOutput.extract(0, Long.MAX_VALUE, ParticleAction.EXECUTE);
        try {
            tickBeam(level, controllerPos, cache, controller);
        } finally {
            clearBeams();
        }
    }

    @Override
    public void onBroken(ServerLevel level, BlockPos controllerPos, C cache) {
        super.onBroken(level, controllerPos, cache);
        if (level.getBlockEntity(controllerPos) instanceof AbstractAcceleratorControllerBE controller) {
            controller.updateControlSignalDisplay(0);
            controller.updateThermalDisplay(0, 0, 0, 0);
            controller.updateOpticsDisplay(0, 0, 0, 0);
            controller.syncParticleDisplay(ParticleStack.EMPTY);
        }
    }

    @Override
    public void resetRuntime() {
        clearBeams();
    }

    @Override
    public void saveRuntime(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putLong("storedHeat", storedHeat);
        tag.putInt("controlSignal", controlSignal);
        tag.putInt("overheatCooldown", overheatCooldown);
        if (activeCoolantRecipeId != null) tag.putString("activeCoolantRecipe", activeCoolantRecipeId.toString());
    }

    @Override
    public void loadRuntime(CompoundTag tag, HolderLookup.Provider registries) {
        clearBeams();
        storedHeat = Math.max(0, tag.getLong("storedHeat"));
        controlSignal = Math.clamp(tag.getInt("controlSignal"), 0, 15);
        overheatCooldown = Math.max(0, tag.getInt("overheatCooldown"));
        activeCoolantRecipeId = tag.contains("activeCoolantRecipe")
                ? ResourceLocation.tryParse(tag.getString("activeCoolantRecipe")) : null;
    }

    protected final void clearBeams() {
        input.extract(0, Long.MAX_VALUE, ParticleAction.EXECUTE);
        pendingOutput.extract(0, Long.MAX_VALUE, ParticleAction.EXECUTE);
    }

    protected final int updateControlSignal(ServerLevel level, BlockPos controllerPos, C cache, B controller) {
        int signal = level.getBestNeighborSignal(controllerPos);
        for (BlockPos servicePort : cache.servicePorts) {
            if (level.hasChunkAt(servicePort) && level.getBlockEntity(servicePort) instanceof MultiblockPortBE port
                    && controllerPos.equals(port.getControllerPos())) {
                signal = Math.max(signal, port.controlSignalSample());
            }
        }
        signal = Math.clamp(signal, 0, 15);
        controlSignal = signal;
        controller.updateControlSignalDisplay(signal);
        return signal;
    }

    protected final void tickThermal(ServerLevel level, B controller, AcceleratorStats stats,
                                     int coolantInputTank, int coolantOutputTank) {
        long passive = AcceleratorThermal.applyPassiveCooling(storedHeat, stats.coolingPerTick());
        long heat = runCoolant(level, controller.contentHandler.getFluidHandler(),
                coolantInputTank, coolantOutputTank, passive, stats.heatCapacity());

        long currentTemperatureK = AcceleratorThermal.temperatureK(heat, stats.heatCapacity());
        if (AcceleratorThermal.isOverheated(currentTemperatureK, stats.maximumTemperatureK())) {
            overheatCooldown = Multiblocks.particleMachines().accelerator().overheatCooldownTicks();
        } else if (overheatCooldown > 0) {
            overheatCooldown--;
        }
        storedHeat = heat;

        controller.updateThermalDisplay(currentTemperatureK, stats.maximumTemperatureK(), overheatCooldown,
                passive - heat);
        controller.updateOpticsDisplay(stats.voltage(), stats.beamLength(), stats.quadrupoleField(),
                stats.dipoleField());
        controller.syncParticleDisplay(input.snapshot(0));
    }

    private long runCoolant(ServerLevel level, @Nullable FluidCapabilityHandler tanks, int inputTank, int outputTank,
                            long heat, long heatCapacity) {
        activeCoolantRecipeId = null;
        if (tanks == null || inputTank < 0 || outputTank < 0
                || inputTank >= tanks.getTanks() || outputTank >= tanks.getTanks()) {
            return heat;
        }
        installCoolantValidator(level, tanks, inputTank);
        FluidStack cold = tanks.getFluidInTank(inputTank);
        RecipeHolder<AcceleratorCoolantRecipe> holder = cold.isEmpty() ? null : findCoolantRecipe(level, cold);
        if (holder == null) return heat;

        AcceleratorCoolantRecipe recipe = holder.value();
        long currentTemperatureK = AcceleratorThermal.temperatureK(heat, heatCapacity);
        Long minimumTemperatureK = recipe.minimumTemperatureK();
        if (minimumTemperatureK != null && currentTemperatureK < minimumTemperatureK) return heat;
        int inAmount = recipe.input().amount();
        FluidStack outTemplate = recipe.output().resolve();
        int outAmount = outTemplate.getAmount();
        if (inAmount <= 0 || outTemplate.isEmpty() || outAmount <= 0) return heat;

        long availableOutput = Math.max(0, tanks.getTankCapacity(outputTank)
                - tanks.getFluidInTank(outputTank).getAmount());
        long ops = AcceleratorThermal.coolantOperations(heat, recipe.heatPerMb(), inAmount,
                cold.getAmount(), availableOutput, outAmount);
        if (ops <= 0) return heat;

        FluidStack toOutput = new FluidStack(outTemplate.getFluid(), (int) (ops * outAmount));
        if (tanks.fillTank(outputTank, toOutput, IFluidHandler.FluidAction.SIMULATE) != toOutput.getAmount()) {
            return heat;
        }
        tanks.drainTank(inputTank, (int) (ops * inAmount), IFluidHandler.FluidAction.EXECUTE);
        tanks.fillTank(outputTank, toOutput, IFluidHandler.FluidAction.EXECUTE);
        activeCoolantRecipeId = holder.id();
        return AcceleratorThermal.heatAfterCoolant(heat, recipe.heatPerMb(), inAmount, ops);
    }

    private RecipeHolder<AcceleratorCoolantRecipe> findCoolantRecipe(ServerLevel level, FluidStack coolant) {
        if (cachedCoolantRecipe != null && cachedCoolantRecipe.value().input().test(coolant)) {
            return cachedCoolantRecipe;
        }
        for (RecipeHolder<AcceleratorCoolantRecipe> holder
                : level.getRecipeManager().getAllRecipesFor(ParticleRecipes.ACCELERATOR_COOLANT_TYPE.get())) {
            if (holder.value().input().test(coolant)) {
                cachedCoolantRecipe = holder;
                return holder;
            }
        }
        cachedCoolantRecipe = null;
        return null;
    }

    private void installCoolantValidator(ServerLevel level, FluidCapabilityHandler tanks, int inputTank) {
        if (coolantValidatorInstalled) return;
        Set<Fluid> accepted = new HashSet<>();
        for (RecipeHolder<AcceleratorCoolantRecipe> holder
                : level.getRecipeManager().getAllRecipesFor(ParticleRecipes.ACCELERATOR_COOLANT_TYPE.get())) {
            for (FluidStack candidate : holder.value().input().getFluids()) accepted.add(candidate.getFluid());
        }
        tanks.getInternalHandler().setTankValidator(inputTank, stack -> accepted.contains(stack.getFluid()));
        coolantValidatorInstalled = true;
    }

    protected final void addProcessingHeat(AcceleratorStats stats) {
        long heat;
        try {
            heat = Math.addExact(storedHeat, stats.heatPerTick());
        } catch (ArithmeticException ignored) {
            heat = Long.MAX_VALUE;
        }
        storedHeat = Math.max(0, heat);
    }

    protected final boolean transferPendingOutput(ServerLevel level, BlockPos controllerPos, BlockPos outputPort,
                                                  BiFunction<ParticleStack, Integer, ParticleStack> attenuation) {
        if (!level.hasChunkAt(outputPort)) return false;
        BlockState state = level.getBlockState(outputPort);
        if (!state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) return false;
        Direction direction = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        BeamConnection outputConnection = BeamConnectionResolver.resolve(level, outputPort, direction,
                Multiblocks.particleMachines().accelerator().beamConnectionReach());
        if (outputConnection == null) return false;
        IParticleHandler destination = BeamConnectionResolver.destination(level, outputConnection);
        if (destination == null) return false;

        ParticleTransfer.Result result;
        try {
            int corridorLength = BeamConnectionResolver.corridorLength(outputConnection);
            result = ParticleTransfer.execute(new ParticleTransfer.Request(pendingOutput, 0, destination, 0,
                    List.of(level.dimension().location(), controllerPos), Long.MAX_VALUE,
                    stack -> attenuation.apply(stack, corridorLength)));
        } catch (ArithmeticException | IllegalArgumentException ignored) {
            return false;
        }
        if (result.transferred().isEmpty()) return false;
        recordTransfer(level, outputPort, direction, outputConnection.destination(), result.transferred());
        return true;
    }

    private static void recordTransfer(ServerLevel level, BlockPos source, Direction direction,
                                       BlockPos destination, ParticleStack transferred) {
        if (level.getBlockEntity(source) instanceof MultiblockPortBE port) {
            port.recordParticleTransfer(transferred, level.getGameTime(), direction);
        }
        if (level.getBlockEntity(destination) instanceof MultiblockPortBE port) {
            port.recordParticleTransfer(transferred, level.getGameTime(), direction);
        }
    }

    @Nullable
    public ResourceLocation activeCoolantRecipeId() {
        return activeCoolantRecipeId;
    }
}
