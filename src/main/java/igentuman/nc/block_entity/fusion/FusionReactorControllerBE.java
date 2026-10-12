package igentuman.nc.block_entity.fusion;

import igentuman.nc.block_entity.MultiblockControllerBE;
import igentuman.nc.block_entity.MultiblockPortBE;
import igentuman.nc.block_entity.OutputLevelSource;
import igentuman.nc.client.particle.FusionBeamParticleData;
import igentuman.nc.container.FusionReactorContainer;
import igentuman.nc.handler.fluid.FluidStackHandler;
import igentuman.nc.handler.sided.FluidCapabilityHandler;
import igentuman.nc.multiblock.fusion.FusionReactorLogic;
import igentuman.nc.multiblock.fusion.FusionReactorCache;
import igentuman.nc.recipe.fusion.FusionRecipe;
import igentuman.nc.recipe.fusion.FusionRecipes;
import igentuman.nc.setup.ModEntries;
import igentuman.nc.setup.NCSounds;
import igentuman.nc.util.NBTField;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Controller block entity for the fusion reactor; manages the core cage, runs the reaction, and emits beam particles. */
public class FusionReactorControllerBE extends MultiblockControllerBE implements OutputLevelSource {

    public static final int MODE_ENERGY = 0;
    public static final int MODE_HEAT = 1;
    public static final int MODE_EFFICIENCY = 2;
    public static final int MODE_COUNT = 3;
    public static final String[] MODE_KEYS = {"energy", "heat", "efficiency"};

    private static int fuelRecipeVersion;
    private int installedFuelRecipeVersion = -1;
    private final Map<Fluid, List<FusionRecipe>> fuelRecipesByFluid = new HashMap<>();
    private CompoundTag reactionRuntime;

    @NBTField(syncToClient = true) public int functionalBlocksCharge;
    @NBTField(syncToClient = true) public int energyPerTick;
    @NBTField(syncToClient = true) public int amplificationAdjustment = 50;
    @NBTField(syncToClient = true) public boolean running;
    @NBTField(syncToClient = true) public int redstoneMode = MODE_HEAT;

    @NBTField(syncToClient = true) public double reactorHeat;
    @NBTField(syncToClient = true) public long plasmaTemperature;
    @NBTField(syncToClient = true) public long maxPlasmaTemperature;
    @NBTField public long chargeAmount;
    @NBTField(syncToClient = true) public double efficiency;
    @NBTField(syncToClient = true) public double maxHeat;
    @NBTField public int inputRedstoneSignal;
    @NBTField(syncToClient = true) public int rfAmplificationRatio;

    @NBTField(syncToClient = true) public double magneticFieldStrength;
    @NBTField(syncToClient = true) public int magnetsPower;
    @NBTField(syncToClient = true) public int maxMagnetsTemp;
    @NBTField(syncToClient = true) public int rfVoltage;
    @NBTField(syncToClient = true) public int rfAmplifiersPower;
    @NBTField(syncToClient = true) public int minRFAmplifiersTemp;
    @NBTField(syncToClient = true) public int magnetCount;
    @NBTField(syncToClient = true) public int amplifierCount;

    public FusionReactorControllerBE(BlockPos pos, BlockState state, String name) {
        super(ModEntries.get(name).blockEntity().get(), pos, state, name);
    }

    @Nullable
    public FluidStackHandler fluidTanks() {
        FluidCapabilityHandler fh = contentHandler.getFluidHandler();
        return fh != null ? fh.getInternalHandler() : null;
    }

    @Nullable
    @Override
    public IFluidHandler getFluidHandler(@Nullable Direction side) {
        if (level instanceof ServerLevel serverLevel) ensureFluidValidators(serverLevel);
        return super.getFluidHandler(side);
    }

    public static void invalidateFuelRecipes() {
        fuelRecipeVersion++;
    }

    private void ensureFluidValidators(ServerLevel level) {
        if (installedFuelRecipeVersion == fuelRecipeVersion) return;
        FluidStackHandler tanks = fluidTanks();
        if (tanks == null) return;
        fuelRecipesByFluid.clear();
        for (RecipeHolder<FusionRecipe> holder : level.getRecipeManager().getAllRecipesFor(FusionRecipes.FUSION_TYPE.get())) {
            FusionRecipe recipe = holder.value();
            indexFuel(recipe.inputA().ingredient(), recipe);
            indexFuel(recipe.inputB().ingredient(), recipe);
        }
        tanks.setTankValidator(0, fs -> acceptsFuel(0, fs));
        tanks.setTankValidator(1, fs -> acceptsFuel(1, fs));
        installedFuelRecipeVersion = fuelRecipeVersion;
    }

    private void indexFuel(FluidIngredient ingredient, FusionRecipe recipe) {
        for (FluidStack stack : ingredient.getStacks()) {
            fuelRecipesByFluid.computeIfAbsent(stack.getFluid(), fluid -> new ArrayList<>()).add(recipe);
        }
    }

    private boolean acceptsFuel(int tank, FluidStack incoming) {
        List<FusionRecipe> candidates = fuelRecipesByFluid.get(incoming.getFluid());
        if (candidates == null) return false;
        FluidStack other = fluidTanks().getFluidInTank(1 - tank);
        for (FusionRecipe recipe : candidates) {
            FluidIngredient a = recipe.inputA().ingredient();
            FluidIngredient b = recipe.inputB().ingredient();
            if ((a.test(incoming) && (other.isEmpty() || b.test(other)))
                    || (b.test(incoming) && (other.isEmpty() || a.test(other)))) return true;
        }
        return false;
    }

    @Nullable
    private FusionReactorLogic reaction() {
        return mbInstance != null && mbInstance.logic instanceof FusionReactorLogic logic ? logic : null;
    }

    public boolean isRunning() {
        return running;
    }

    public double getEfficiency() {
        return efficiency;
    }

    public double getMaxHeat() {
        return maxHeat;
    }

    public double rfAmplifierRatio() {
        return Math.clamp((rfAmplificationRatio / 100.0) * (amplificationAdjustment / 100.0), 0, 1.0);
    }

    public void voidFuel() {
        FluidStackHandler tanks = fluidTanks();
        if (tanks == null) return;
        tanks.voidTank(0);
        tanks.voidTank(1);
        setChanged();
    }

    /** GUI slider (1..100) tuning the amplification ratio. */
    public void setAmplificationAdjustment(int value) {
        int clamped = Math.clamp(value, 1, 100);
        if (clamped != amplificationAdjustment) {
            amplificationAdjustment = clamped;
            setChanged();
        }
    }

    public void captureReactionRuntime(FusionReactorLogic reaction) {
        if (level == null) return;
        CompoundTag snapshot = new CompoundTag();
        reaction.saveRuntime(snapshot, level.registryAccess());
        reactionRuntime = snapshot;
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (tag.contains("runtime")) {
            reactionRuntime = tag.getCompound("runtime").copy();
        } else if (reactionRuntime != null) {
            tag.put("runtime", reactionRuntime.copy());
        }
    }

    @Override
    public void clientTick() {
        super.clientTick();
        if (!formed) {
            stopSound();
            return;
        }
        if (functionalBlocksCharge > 0 && functionalBlocksCharge < 100) {
            playSound(NCSounds.FUSION_CHARGING.get(), 0.7f);
            return;
        }
        boolean ready = magnetsPower > 0 && rfAmplifiersPower > 0 && functionalBlocksCharge > 99;
        if (ready) {
            if (running && energyPerTick > 0) {
                playSound(NCSounds.FUSION_RUNNING.get(), 0.5f);
            } else {
                playSound(NCSounds.FUSION_READY.get(), 0.7f);
            }
        } else {
            stopSound();
        }
    }

    @Override
    public void serverTick() {
        if (!(level instanceof ServerLevel serverLevel)) return;
        ensureFluidValidators(serverLevel);
        tickMultiblock(serverLevel);
        boolean newFormed = mbInstance != null && mbInstance.formed;
        if (formed != newFormed) {
            formed = newFormed;
            wasChanged = true;
        }
        if (!formed && (mbInstance == null || !mbInstance.dirty)) {
            FusionReactorLogic reaction = reaction();
            if (reaction != null) reaction.idle(this);
        }
        if (wasChanged) {
            getLevel().sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
            wasChanged = false;
        }
    }

    public void updateStructureDisplay(FusionReactorCache fc) {
        magneticFieldStrength = fc.magneticFieldStrength;
        magnetsPower = fc.magnetsPower;
        maxMagnetsTemp = fc.maxMagnetsTemp;
        rfVoltage = fc.rfAmplification;
        rfAmplifiersPower = fc.rfAmplifiersPower;
        minRFAmplifiersTemp = fc.minRFAmplifiersTemp;
        magnetCount = fc.magnetCount;
        amplifierCount = fc.amplifierCount;
    }

    /** Emits four beam particles tracing the plasma ring, one per edge of the square. */
    public void renderBeam(ServerLevel level, int size) {
        BlockPos mid = worldPosition.above();
        float beamLength = size * 2 + 4;
        sendBeamData(level, new FusionBeamParticleData(Direction.EAST, beamLength, 0.35f),
                mid.relative(Direction.NORTH, size + 2).relative(Direction.WEST, size + 2));
        sendBeamData(level, new FusionBeamParticleData(Direction.EAST, beamLength, 0.35f),
                mid.relative(Direction.SOUTH, size + 2).relative(Direction.WEST, size + 2));
        sendBeamData(level, new FusionBeamParticleData(Direction.SOUTH, beamLength, 0.35f),
                mid.relative(Direction.EAST, size + 2).relative(Direction.NORTH, size + 2));
        sendBeamData(level, new FusionBeamParticleData(Direction.SOUTH, beamLength, 0.35f),
                mid.relative(Direction.WEST, size + 2).relative(Direction.NORTH, size + 2));
    }

    private void sendBeamData(ServerLevel level, FusionBeamParticleData data, BlockPos from) {
        Vec3 vec = Vec3.atCenterOf(from);
        for (ServerPlayer player : level.players()) {
            level.sendParticles(player, data, true, vec.x, vec.y, vec.z, 1, 0, 0, 0, 0);
        }
    }

    /** Strongest redstone signal touching any cell of the 3x3x3 core cage. Drives amplification. */
    public void updateRedstoneInput(ServerLevel level) {
        int signal = level.getBestNeighborSignal(worldPosition);
        for (int y = 0; y < 3; y++) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 && z == 0) continue;
                    signal = Math.max(signal, level.getBestNeighborSignal(worldPosition.offset(x, y, z)));
                }
            }
        }
        int ratio = Math.clamp((int) (signal / 0.15), 0, 100);
        if (signal != inputRedstoneSignal || ratio != rfAmplificationRatio) {
            inputRedstoneSignal = signal;
            rfAmplificationRatio = ratio;
            markDirty();
        }
    }

    public void updateReactionDisplay(double heat, long plasma, long charge) {
        if (heat == reactorHeat && plasma == plasmaTemperature && charge == chargeAmount) return;
        reactorHeat = heat;
        plasmaTemperature = plasma;
        chargeAmount = charge;
        markDirty();
    }

    public boolean hasRedstoneSignal() {
        return inputRedstoneSignal > 0;
    }

    /** Cycles the comparator output source (energy -> heat -> efficiency). Called from a cage cell. */
    public void toggleRedstoneMode() {
        redstoneMode = (redstoneMode + 1) % MODE_COUNT;
        markDirty();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    /** Comparator strength (0-15) for the given output mode. */
    public int comparatorSignal(int mode) {
        return OutputLevelSource.comparatorSignal(outputLevel(mode));
    }

    @Override
    public double outputLevel(int mode) {
        return switch (mode) {
            case MODE_ENERGY ->
                    energyStorage != null ? OutputLevelSource.fraction(energyStorage.getEnergyStored(), energyStorage.getMaxEnergyStored()) : 0;
            case MODE_HEAT -> OutputLevelSource.fraction(reactorHeat, maxHeat);
            case MODE_EFFICIENCY -> Math.clamp(efficiency, 0, 1);
            default -> 0;
        };
    }

    @Override
    public void onControllerPlaced(ServerLevel level) {
        placeProxyCage(level);
        super.onControllerPlaced(level);
    }

    @Override
    public void onControllerRemoved(ServerLevel level) {
        removeProxyCage(level);
        super.onControllerRemoved(level);
    }

    private void placeProxyCage(ServerLevel level) {
        Block proxy = ModEntries.get("fusion_reactor_core_proxy").block().get();
        for (int y = 0; y < 3; y++) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 && z == 0) continue;
                    BlockPos p = worldPosition.offset(x, y, z);
                    BlockState s = level.getBlockState(p);
                    if (!s.isAir() && !s.is(proxy)) return; // blocked - build no cage
                }
            }
        }
        for (int y = 0; y < 3; y++) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 && z == 0) continue;
                    BlockPos p = worldPosition.offset(x, y, z);
                    level.setBlock(p, proxy.defaultBlockState(), Block.UPDATE_ALL);
                    if (level.getBlockEntity(p) instanceof MultiblockPortBE part) {
                        part.setControllerPos(worldPosition);
                    }
                }
            }
        }
    }

    private void removeProxyCage(ServerLevel level) {
        Block proxy = ModEntries.get("fusion_reactor_core_proxy").block().get();
        for (int y = 0; y < 3; y++) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 && z == 0) continue;
                    BlockPos p = worldPosition.offset(x, y, z);
                    if (level.getBlockState(p).is(proxy)) {
                        level.removeBlock(p, false);
                    }
                }
            }
        }
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new FusionReactorContainer(containerId, playerInventory, this, containerData);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        if (!tag.contains("runtime") && tag.contains("Reaction")) tag.put("runtime", tag.getCompound("Reaction"));
        super.loadAdditional(tag, registries);
        reactionRuntime = tag.contains("runtime") ? tag.getCompound("runtime").copy() : null;
    }
}
