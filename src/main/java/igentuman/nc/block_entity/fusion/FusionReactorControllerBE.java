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
import igentuman.nc.recipe.fusion.FusionCoolantRecipe;
import igentuman.nc.recipe.fusion.FusionRecipe;
import igentuman.nc.recipe.RecipeSources;
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
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FusionReactorControllerBE extends MultiblockControllerBE implements OutputLevelSource {

    public static final int MODE_ENERGY = 0;
    public static final int MODE_HEAT = 1;
    public static final int MODE_EFFICIENCY = 2;
    public static final int MODE_COUNT = 3;
    public static final String[] MODE_KEYS = {"energy", "heat", "efficiency"};

    private RecipeMap fuelRecipeMap;
    private final Map<Fluid, List<FusionRecipe>> fuelRecipesByFluid = new HashMap<>();
    private final List<SizedFluidIngredient> coolantInputs = new ArrayList<>();
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
    public ResourceHandler<FluidResource> getFluidHandler(@Nullable Direction side) {
        ensureFluidValidators();
        return super.getFluidHandler(side);
    }

    private void ensureFluidValidators() {
        if (level == null) return;
        RecipeMap current = RecipeSources.recipes(level);
        if (fuelRecipeMap == current) return;
        FluidStackHandler tanks = fluidTanks();
        if (tanks == null) return;
        fuelRecipesByFluid.clear();
        for (RecipeHolder<FusionRecipe> holder : current.byType(FusionRecipes.FUSION_TYPE.get())) {
            FusionRecipe recipe = holder.value();
            indexFuel(recipe.inputA().ingredient(), recipe);
            indexFuel(recipe.inputB().ingredient(), recipe);
        }
        coolantInputs.clear();
        for (RecipeHolder<FusionCoolantRecipe> holder : current.byType(FusionRecipes.COOLANT_TYPE.get())) {
            coolantInputs.add(holder.value().input());
        }
        tanks.setTankValidator(0, fs -> acceptsFuel(0, fs));
        tanks.setTankValidator(1, fs -> acceptsFuel(1, fs));
        // Fuel overflowing into the coolant tank would block cooling until meltdown.
        tanks.setTankValidator(FusionReactorContainer.TANK_COOLANT,
                fs -> coolantInputs.stream().anyMatch(input -> input.ingredient().test(fs)));
        fuelRecipeMap = current;
    }

    private void indexFuel(FluidIngredient ingredient, FusionRecipe recipe) {
        for (var holder : ingredient.fluids()) {
            fuelRecipesByFluid.computeIfAbsent(holder.value(), fluid -> new ArrayList<>()).add(recipe);
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
    protected void saveTagData(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveTagData(tag, registries);
        if (tag.contains("runtime")) {
            reactionRuntime = tag.getCompoundOrEmpty("runtime").copy();
        } else if (reactionRuntime != null) {
            tag.put("runtime", reactionRuntime.copy());
        }
    }

    @Override
    public void clientTick() {
        super.clientTick();
        ensureFluidValidators();
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
        ensureFluidValidators();
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
            level.sendParticles(player, data, true, false, vec.x, vec.y, vec.z, 1, 0, 0, 0, 0);
        }
    }

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

    public void toggleRedstoneMode() {
        redstoneMode = (redstoneMode + 1) % MODE_COUNT;
        markDirty();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

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
    protected void loadTagData(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadTagData(tag, registries);
        reactionRuntime = tag.contains("runtime") ? tag.getCompoundOrEmpty("runtime").copy() : null;
    }
}
