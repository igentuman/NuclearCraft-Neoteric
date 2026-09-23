package igentuman.nc.multiblock.kugelblitz;

import igentuman.nc.api.multiblock.AbstractMultiblockLogic;
import igentuman.nc.block_entity.kugelblitz.BlackHoleBE;
import igentuman.nc.block_entity.kugelblitz.ChamberTerminalBE;
import igentuman.nc.block_entity.kugelblitz.PhotonConcentratorBE;
import igentuman.nc.config.Multiblocks;
import igentuman.nc.handler.energy.LargeEnergyStorage;
import igentuman.nc.handler.fluid.FluidStackHandler;
import igentuman.nc.handler.sided.ItemCapabilityHandler;
import igentuman.nc.recipe.kugelblitz.KugelblitzRecipe;
import igentuman.nc.recipe.kugelblitz.KugelblitzRecipes;
import igentuman.nc.setup.ModEntries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static igentuman.nc.block_entity.kugelblitz.BlackHoleBE.MAX_MASS;
import static igentuman.nc.block_entity.kugelblitz.BlackHoleBE.MIN_MASS;

public class KugelblitzLogic extends AbstractMultiblockLogic<KugelblitzCache> {

    private static final int PULSE_WINDOW = 10;

    private long mass;
    private long feeding;
    private int evaporation;
    private int stability = 100;
    private int energyPerTick;
    private int ticksProcessed;
    private int ticksNeeded;
    private int processEnergy;
    @Nullable
    private RecipeHolder<KugelblitzRecipe> currentRecipe;
    @Nullable
    private ResourceLocation pendingRecipeId;
    private boolean runtimeLoaded;

    private final Set<Direction> pulses = EnumSet.noneOf(Direction.class);
    private int collectingEnergy = PULSE_WINDOW;
    private boolean gotLaserBurst;

    private final List<ItemStack> orderedOutputs = new ArrayList<>();
    private final List<ItemStack> allowedRandomInputs = new ArrayList<>();

    @Override
    public void tickServer(ServerLevel level, BlockPos controllerPos, KugelblitzCache cache) {
        if (!(level.getBlockEntity(controllerPos) instanceof ChamberTerminalBE terminal)) return;
        seedRuntime(terminal);
        resolvePendingRecipe(level);
        BlockPos center = cache.center;
        terminal.updateStructureDisplay(cache.transformers, cache.fluxRegulators, cache.stabilizers, center);
        if (center != null) refreshConcentrators(level, center, controllerPos);

        collectingEnergy--;
        if (collectingEnergy < 0) {
            collectingEnergy = PULSE_WINDOW;
            if (pulses.size() == Direction.values().length) gotLaserBurst = true;
            pulses.clear();
        }
        handleLaserBurst(level, center);

        if (mass > 0) {
            updateBlackhole(level, terminal, cache, center);
            handleMeltdown(level, center);
            processTransmutation(level, terminal, cache);
        }
        publish(terminal);
    }

    public void idle(ChamberTerminalBE terminal) {
        seedRuntime(terminal);
        if (mass > 0) resetBlackhole();
        publish(terminal);
    }

    public void gotEnergy(Direction facing) {
        pulses.add(facing);
        collectingEnergy = PULSE_WINDOW;
    }

    private void seedRuntime(ChamberTerminalBE terminal) {
        if (runtimeLoaded) return;
        mass = Math.max(0, terminal.mass);
        feeding = terminal.feeding;
        evaporation = terminal.evaporation;
        stability = terminal.blackholeStability;
        energyPerTick = terminal.energyPerTick;
        ticksProcessed = terminal.ticksProcessed;
        ticksNeeded = terminal.ticksNeeded;
        processEnergy = terminal.processEnergy;
        runtimeLoaded = true;
    }

    private void resolvePendingRecipe(ServerLevel level) {
        if (pendingRecipeId == null) return;
        currentRecipe = level.getRecipeManager().byKey(pendingRecipeId)
                .filter(holder -> holder.value() instanceof KugelblitzRecipe)
                .map(this::cast)
                .orElse(null);
        pendingRecipeId = null;
    }

    @SuppressWarnings("unchecked")
    private RecipeHolder<KugelblitzRecipe> cast(RecipeHolder<?> holder) {
        return (RecipeHolder<KugelblitzRecipe>) holder;
    }

    private void publish(ChamberTerminalBE terminal) {
        terminal.updateRuntimeDisplay(mass, feeding, evaporation, stability, energyPerTick,
                ticksProcessed, ticksNeeded, processEnergy, progress());
    }

    private void refreshConcentrators(ServerLevel level, BlockPos center, BlockPos controllerPos) {
        for (Direction dir : Direction.values()) {
            if (level.getBlockEntity(center.relative(dir, 5)) instanceof PhotonConcentratorBE concentrator) {
                concentrator.setControllerPos(controllerPos);
            }
        }
    }

    private void resetBlackhole() {
        mass = 0;
        feeding = 0;
        evaporation = 0;
        energyPerTick = 0;
    }

    private static boolean hasBlackhole(ServerLevel level, @Nullable BlockPos center) {
        return center != null && level.getBlockEntity(center) instanceof BlackHoleBE;
    }

    private void updateBlackhole(ServerLevel level, ChamberTerminalBE terminal, KugelblitzCache cache,
                                 @Nullable BlockPos center) {
        if (!hasBlackhole(level, center)) {
            resetBlackhole();
            return;
        }
        FluidStackHandler tanks = terminal.fluidTanks();
        long amount = tanks != null ? tanks.getFluidInTank(0).getAmount() : 0;
        feeding = amount * 10L;
        mass += feeding;
        if (tanks != null) tanks.voidTank(0);
        updateEnergyGeneration(terminal, cache);
        updateEvaporation(level, terminal, cache);
        mass -= evaporation;
        if (mass < MIN_MASS) doEvaporation(level, center);
        updateStability(level, cache, center);
    }

    private void updateEnergyGeneration(ChamberTerminalBE terminal, KugelblitzCache cache) {
        double massRatio = (double) MAX_MASS / Math.max(mass, MIN_MASS);
        double generated = massRatio * 1000
                * (Math.log(terminal.energyConvertionRate * 50000.0) + 1)
                * (Math.log(Math.max(1, cache.fluxRegulators) * 5000.0) + 1);
        generated *= Multiblocks.kugelblitzGenerationMultiplier;
        energyPerTick = (int) Math.max(0, Math.min(Integer.MAX_VALUE, generated));
        LargeEnergyStorage storage = terminal.energyStorage;
        if (storage != null && energyPerTick > 0) {
            int add = Math.min(energyPerTick, storage.getMaxEnergyStored() - storage.getEnergyStored());
            if (add > 0) storage.setEnergyStored(storage.getEnergyStored() + add);
        }
    }

    private void updateEvaporation(ServerLevel level, ChamberTerminalBE terminal, KugelblitzCache cache) {
        int rate = terminal.energyConvertionRate;
        double fluxLog = Math.log(Math.max(Math.E, cache.fluxRegulators));
        int evaporationRate = (int) Math.max(1, rate * 100 / fluxLog);
        if (currentRecipe != null && !isProcessComplete()) {
            double transformerLog = Math.log(Math.max(Math.E, cache.transformers));
            evaporationRate += (int) ((100 - rate) * 100 / transformerLog);
        }
        evaporationRate = (int) Math.pow(evaporationRate, 1.2);
        double massRatio = Math.log10((double) MAX_MASS / Math.max(mass, MIN_MASS));
        evaporation = (int) (evaporationRate * Multiblocks.kugelblitzEvaporationMultiplier * massRatio);

        if (stability < 20 && level.random.nextDouble() < 0.1) {
            evaporation += (int) (mass * 0.0001 * (level.random.nextDouble() - 0.5) * 2);
        }
        if (stability < 10 && level.random.nextDouble() < 0.1) {
            evaporation += (int) (mass * 0.0005 * level.random.nextDouble() * 20D / Math.max(1, stability));
        }
    }

    private void updateStability(ServerLevel level, KugelblitzCache cache, @Nullable BlockPos center) {
        if (!hasBlackhole(level, center) || mass <= 0) {
            stability = 100;
            return;
        }
        if (level.getGameTime() % 5 != 0 || level.random.nextInt(96) < cache.stabilizers) return;
        if (level.random.nextDouble() > 0.98D) {
            stability = Math.min(100, stability + 1);
            return;
        }
        double massRange = MAX_MASS - MIN_MASS * 5.0;
        double normalizedMass = (mass - MIN_MASS) / massRange;
        double distanceFromOptimal = Math.abs(normalizedMass - 0.5) * 2.0;
        double decreaseChance = 0.05 + (distanceFromOptimal * 0.1);
        if (mass > MAX_MASS * 0.9) decreaseChance += 0.1;
        if (level.random.nextDouble() < decreaseChance) {
            stability = Math.max(0, stability - 1);
        }
        if (distanceFromOptimal < 0.3 && level.random.nextDouble() < 0.02) {
            stability = Math.min(100, stability + 1);
        }
    }

    private void doEvaporation(ServerLevel level, @Nullable BlockPos center) {
        if (hasBlackhole(level, center)) {
            level.setBlockAndUpdate(center, Blocks.AIR.defaultBlockState());
        }
        resetBlackhole();
    }

    private void handleMeltdown(ServerLevel level, @Nullable BlockPos center) {
        if (mass <= MAX_MASS || center == null) return;
        if (level.getBlockEntity(center) instanceof BlackHoleBE blackHole) {
            blackHole.meltdown();
            resetBlackhole();
        }
    }

    private void handleLaserBurst(ServerLevel level, @Nullable BlockPos center) {
        if (!gotLaserBurst) return;
        if (!hasBlackhole(level, center)) {
            stability = 100;
            spawnBlackhole(level, center);
            return;
        }
        stability = Math.min(100, stability + 50 + level.random.nextInt(50));
        mass += (level.random.nextInt(200) + 200) * 10000L;
        gotLaserBurst = false;
    }

    private void spawnBlackhole(ServerLevel level, @Nullable BlockPos center) {
        if (center == null) return;
        level.setBlockAndUpdate(center, ModEntries.get("black_hole").block().get().defaultBlockState());
        mass = (long) (MIN_MASS * (1 + level.random.nextDouble()));
        gotLaserBurst = false;
    }

    private boolean isProcessComplete() {
        return ticksNeeded > 0 && ticksProcessed >= ticksNeeded;
    }

    private int progress() {
        return ticksNeeded > 0 ? (int) ((double) ticksProcessed / ticksNeeded * 100) : 0;
    }

    private void processTransmutation(ServerLevel level, ChamberTerminalBE terminal, KugelblitzCache cache) {
        ItemCapabilityHandler inventory = terminal.items();
        if (inventory == null) return;
        if (currentRecipe != null && isProcessComplete()) {
            handleRecipeOutput(level, terminal, inventory);
        }
        if (currentRecipe == null) {
            updateRecipe(level, terminal, inventory);
        }
        if (currentRecipe != null && !isProcessComplete()) {
            double multiplier = Math.log10(Math.max(1, cache.transformers)) * (100 - terminal.energyConvertionRate) / 100D;
            int step = (int) Math.max(1, multiplier);
            LargeEnergyStorage storage = terminal.energyStorage;
            if (processEnergy > 0 && storage != null) {
                if (storage.getEnergyStored() < (long) processEnergy * step) return;
                storage.setEnergyStored(storage.getEnergyStored() - processEnergy * step);
            }
            ticksProcessed = Math.min(ticksNeeded, ticksProcessed + step);
        }
    }

    private void updateRecipe(ServerLevel level, ChamberTerminalBE terminal, ItemCapabilityHandler inventory) {
        ItemStack input = inventory.getStackInSlot(0);
        if (input.isEmpty()) {
            currentRecipe = null;
            ticksProcessed = 0;
            ticksNeeded = 0;
            return;
        }
        RecipeHolder<KugelblitzRecipe> recipe = findRecipe(level, terminal, input);
        if (recipe != null) {
            currentRecipe = recipe;
            ticksProcessed = 0;
            ticksNeeded = recipe.value().getBaseTime();
            processEnergy = recipe.value().getEnergy();
            inventory.extractItem(0, recipe.value().input().count(), false);
        } else {
            currentRecipe = null;
            ticksNeeded = 0;
        }
    }

    @Nullable
    private RecipeHolder<KugelblitzRecipe> findRecipe(ServerLevel level, ChamberTerminalBE terminal, ItemStack input) {
        for (RecipeHolder<KugelblitzRecipe> holder : level.getRecipeManager()
                .getAllRecipesFor(KugelblitzRecipes.KUGELBLITZ_TYPE.get())) {
            KugelblitzRecipe recipe = holder.value();
            if (recipe.input().test(input) && terminal.frequency == targetFrequency(level, recipe.getResultStack())) {
                return holder;
            }
        }
        return null;
    }

    private void handleRecipeOutput(ServerLevel level, ChamberTerminalBE terminal, ItemCapabilityHandler inventory) {
        ItemStack output = resolveOutput(level, currentRecipe.value());
        if (output.isEmpty()) {
            currentRecipe = null;
            return;
        }
        ItemStack existing = inventory.getStackInSlot(1);
        if (existing.isEmpty()) {
            inventory.setStackInSlot(1, output.copy());
        } else if (ItemStack.isSameItemSameComponents(existing, output)
                && existing.getCount() + output.getCount() <= existing.getMaxStackSize()) {
            existing.grow(output.getCount());
            inventory.setStackInSlot(1, existing);
        } else {
            return;
        }
        currentRecipe = null;
        ticksProcessed = 0;
        ticksNeeded = 0;
        terminal.markDirty();
    }

    private ItemStack resolveOutput(ServerLevel level, KugelblitzRecipe recipe) {
        ItemStack result = recipe.getResultStack();
        ItemStack input = recipe.getInputStack();
        if (!result.is(input.getItem())) return result;
        buildRandomPool(level);
        int id = ingredientId(result);
        return id >= 0 && id < orderedOutputs.size() ? orderedOutputs.get(id).copy() : result;
    }

    private int ingredientId(ItemStack result) {
        for (int i = 0; i < allowedRandomInputs.size(); i++) {
            if (allowedRandomInputs.get(i).is(result.getItem())) return i;
        }
        return -1;
    }

    private void buildRandomPool(ServerLevel level) {
        if (!orderedOutputs.isEmpty()) return;
        for (RecipeHolder<KugelblitzRecipe> holder : level.getRecipeManager()
                .getAllRecipesFor(KugelblitzRecipes.KUGELBLITZ_TYPE.get())) {
            KugelblitzRecipe recipe = holder.value();
            ItemStack in = recipe.getInputStack();
            if (recipe.getResultStack().is(in.getItem())) allowedRandomInputs.add(in);
        }
        orderedOutputs.addAll(allowedRandomInputs.stream().map(ItemStack::copy).toList());
        Collections.shuffle(orderedOutputs, new Random(level.getSeed()));
    }

    private static int targetFrequency(ServerLevel level, ItemStack stack) {
        return new Random(level.getSeed() + stack.getItem().toString().hashCode()).nextInt(15);
    }

    @Override
    public void saveRuntime(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putLong("mass", mass);
        tag.putLong("feeding", feeding);
        tag.putInt("evaporation", evaporation);
        tag.putInt("blackholeStability", stability);
        tag.putInt("energyPerTick", energyPerTick);
        tag.putInt("ticksProcessed", ticksProcessed);
        tag.putInt("ticksNeeded", ticksNeeded);
        tag.putInt("processEnergy", processEnergy);
        ResourceLocation recipeId = currentRecipe != null ? currentRecipe.id() : pendingRecipeId;
        if (recipeId != null) tag.putString("recipe", recipeId.toString());
    }

    @Override
    public void loadRuntime(CompoundTag tag, HolderLookup.Provider registries) {
        if (!tag.contains("mass")) return;
        mass = Math.max(0, tag.getLong("mass"));
        feeding = tag.getLong("feeding");
        evaporation = tag.getInt("evaporation");
        stability = tag.getInt("blackholeStability");
        energyPerTick = tag.getInt("energyPerTick");
        ticksProcessed = tag.getInt("ticksProcessed");
        ticksNeeded = tag.getInt("ticksNeeded");
        processEnergy = tag.getInt("processEnergy");
        pendingRecipeId = tag.contains("recipe") ? ResourceLocation.tryParse(tag.getString("recipe")) : null;
        currentRecipe = null;
        runtimeLoaded = true;
    }
}
