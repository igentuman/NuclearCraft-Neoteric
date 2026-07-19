package igentuman.nc.block_entity.kugelblitz;

import igentuman.nc.block_entity.MultiblockControllerBE;
import igentuman.nc.block_entity.RedstoneModeController;
import igentuman.nc.config.Multiblocks;
import igentuman.nc.container.ChamberTerminalContainer;
import igentuman.nc.handler.fluid.FluidStackHandler;
import igentuman.nc.handler.sided.FluidCapabilityHandler;
import igentuman.nc.handler.sided.ItemCapabilityHandler;
import igentuman.nc.multiblock.kugelblitz.KugelblitzCache;
import igentuman.nc.recipe.kugelblitz.KugelblitzRecipe;
import igentuman.nc.recipe.kugelblitz.KugelblitzRecipes;
import igentuman.nc.setup.ModEntries;
import igentuman.nc.util.NBTField;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import static igentuman.nc.block_entity.kugelblitz.BlackHoleBE.MAX_MASS;
import static igentuman.nc.block_entity.kugelblitz.BlackHoleBE.MIN_MASS;

public class ChamberTerminalBE extends MultiblockControllerBE implements RedstoneModeController {

    @NBTField public long feeding = 0;
    @NBTField public long mass = 0;
    @NBTField public int evaporation = 0;
    @NBTField public byte frequency = 0;
    @NBTField public int energyConvertionRate = 7;
    @NBTField public boolean controllerEnabled = false;
    @NBTField public int transformers = 0;
    @NBTField public int fluxRegulators = 0;
    @NBTField public int stabilizers = 0;
    @NBTField public int blackholeStability = 100;
    @NBTField public int energyPerTick = 0;
    @NBTField public BlockPos blackholePos = BlockPos.ZERO;

    @NBTField public int ticksProcessed = 0;
    @NBTField public int ticksNeeded = 0;
    @NBTField public int processEnergy = 0;
    @NBTField(syncToClient = true) public int progress = 0;

    private final HashMap<Direction, Long> pulseEnergy = new HashMap<>();
    private int collectingEnergy = 10;
    private boolean gotLaserBurst = false;

    private KugelblitzRecipe currentRecipe;
    private final List<ItemStack> orderedOutputs = new ArrayList<>();
    private final List<ItemStack> allowedRandomInputs = new ArrayList<>();
    private boolean fluidValidatorReady = false;

    public ChamberTerminalBE(BlockPos pos, BlockState state, String name) {
        super(ModEntries.get(name).blockEntity().get(), pos, state, name);
    }

    @Nullable
    public FluidStackHandler fluidTanks() {
        FluidCapabilityHandler fh = contentHandler.getFluidHandler();
        return fh != null ? fh.getInternalHandler() : null;
    }

    @Nullable
    public ItemCapabilityHandler items() {
        return contentHandler.getItemHandler();
    }

    private void ensureFluidValidator() {
        if (fluidValidatorReady) return;
        FluidStackHandler tanks = fluidTanks();
        if (tanks == null) return;
        var fluid = ModEntries.fluidOf("subliquid_matter");
        tanks.setTankValidator(0, fs -> fluid != null && fs.getFluid() == fluid);
        fluidValidatorReady = true;
    }

    @Override
    public void serverTick() {
        if (!(level instanceof ServerLevel serverLevel)) return;
        ensureFluidValidator();
        tickMultiblock(serverLevel);
        boolean newFormed = mbInstance != null && mbInstance.formed;
        if (formed != newFormed) {
            formed = newFormed;
            wasChanged = true;
        }

        if (formed && mbInstance.cache instanceof KugelblitzCache kc) {
            transformers = kc.transformers;
            fluxRegulators = kc.fluxRegulators;
            stabilizers = kc.stabilizers;
            if (kc.hasCenter()) blackholePos = kc.centerPos();
            refreshConcentrators();

            collectingEnergy--;
            if (collectingEnergy < 0) {
                collectingEnergy = 10;
                if (pulseEnergy.size() == 6) gotLaserBurst = true;
                pulseEnergy.clear();
            }
            handleLaserBurst();

            controllerEnabled = mass > 0;
            if (controllerEnabled) {
                updateBlackhole();
                handleMeltdown();
                processTransmutation();
                wasChanged = true;
            } else if (mass > 0) {
                resetBlackhole();
                wasChanged = true;
            }
        } else if (mass > 0) {
            resetBlackhole();
            wasChanged = true;
        }

        progress = getProgress();

        if (wasChanged) {
            setChanged();
            getLevel().sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
            wasChanged = false;
        }
    }

    private void refreshConcentrators() {
        BlockPos center = blackholePos;
        if (center.equals(BlockPos.ZERO)) return;
        for (Direction dir : Direction.values()) {
            if (level.getBlockEntity(center.relative(dir, 5)) instanceof PhotonConcentratorBE pc) {
                pc.setControllerPos(worldPosition);
            }
        }
    }

    private void resetBlackhole() {
        mass = 0;
        feeding = 0;
        evaporation = 0;
        energyPerTick = 0;
    }

    public boolean hasBlackhole() {
        return level.getBlockEntity(blackholePos) instanceof BlackHoleBE;
    }

    @Nullable
    private BlackHoleBE blackHole() {
        return level.getBlockEntity(blackholePos) instanceof BlackHoleBE bh ? bh : null;
    }

    private void updateBlackhole() {
        if (!hasBlackhole()) {
            resetBlackhole();
            return;
        }
        FluidStackHandler tanks = fluidTanks();
        long amount = tanks != null ? tanks.getFluidInTank(0).getAmount() : 0;
        feeding = amount * 10L;
        mass += feeding;
        if (tanks != null) tanks.voidTank(0);
        updateEnergyGeneration();
        updateEvaporation();
        mass -= evaporation;
        if (mass < MIN_MASS) {
            doEvaporation();
        }
        updateBlackholeStability();
    }

    private void updateEnergyGeneration() {
        double massRatio = (double) MAX_MASS / Math.max(mass, MIN_MASS);
        double gen = massRatio * 1000
                * (Math.log(energyConvertionRate * 50000.0) + 1)
                * (Math.log(Math.max(1, fluxRegulators) * 5000.0) + 1);
        gen *= Multiblocks.kugelblitzGenerationMultiplier;
        energyPerTick = (int) Math.max(0, Math.min(Integer.MAX_VALUE, gen));
        if (energyStorage != null && energyPerTick > 0) {
            int add = Math.min(energyPerTick, energyStorage.getMaxEnergyStored() - energyStorage.getEnergyStored());
            if (add > 0) energyStorage.setEnergyStored(energyStorage.getEnergyStored() + add);
        }
    }

    private void updateEvaporation() {
        double fluxLog = Math.log(Math.max(Math.E, fluxRegulators));
        int rate = (int) Math.max(1, energyConvertionRate * 100 / fluxLog);
        if (currentRecipe != null && !isProcessComplete()) {
            double transformerLog = Math.log(Math.max(Math.E, transformers));
            rate += (int) ((100 - energyConvertionRate) * 100 / transformerLog);
        }
        rate = (int) Math.pow(rate, 1.2);
        double massRatio = Math.log10((double) MAX_MASS / Math.max(mass, MIN_MASS));
        evaporation = (int) (rate * Multiblocks.kugelblitzEvaporationMultiplier * massRatio);

        if (blackholeStability < 20 && level.random.nextDouble() < 0.1) {
            evaporation += (int) (mass * 0.0001 * (level.random.nextDouble() - 0.5) * 2);
        }
        if (blackholeStability < 10 && level.random.nextDouble() < 0.1) {
            evaporation += (int) (mass * 0.0005 * level.random.nextDouble() * 20D / Math.max(1, blackholeStability));
        }
    }

    private void updateBlackholeStability() {
        if (!hasBlackhole() || mass <= 0) {
            blackholeStability = 100;
            return;
        }
        if (level.getGameTime() % 5 != 0 || level.random.nextInt(96) < stabilizers) return;
        if (level.random.nextDouble() > 0.98D) {
            blackholeStability = Math.min(100, blackholeStability + 1);
            return;
        }
        double massRange = MAX_MASS - MIN_MASS * 5.0;
        double normalizedMass = (mass - MIN_MASS) / massRange;
        double distanceFromOptimal = Math.abs(normalizedMass - 0.5) * 2.0;
        double decreaseChance = 0.05 + (distanceFromOptimal * 0.1);
        if (mass > MAX_MASS * 0.9) decreaseChance += 0.1;
        if (level.random.nextDouble() < decreaseChance) {
            blackholeStability = Math.max(0, blackholeStability - 1);
        }
        if (distanceFromOptimal < 0.3 && level.random.nextDouble() < 0.02) {
            blackholeStability = Math.min(100, blackholeStability + 1);
        }
    }

    private void doEvaporation() {
        if (hasBlackhole()) {
            level.setBlockAndUpdate(blackholePos, Blocks.AIR.defaultBlockState());
        }
        resetBlackhole();
    }

    private void handleMeltdown() {
        if (mass > MAX_MASS) {
            BlackHoleBE bh = blackHole();
            if (bh != null) {
                bh.meltdown();
                resetBlackhole();
            }
        }
    }

    public void gotEnergy(Direction facing) {
        pulseEnergy.put(facing, 1L);
        collectingEnergy = 10;
    }

    private void handleLaserBurst() {
        if (!gotLaserBurst) return;
        if (!hasBlackhole()) {
            blackholeStability = 100;
            spawnBlackhole();
            return;
        }
        blackholeStability = Math.min(100, blackholeStability + 50 + level.random.nextInt(50));
        mass += (level.random.nextInt(200) + 200) * 10000L;
        gotLaserBurst = false;
    }

    private void spawnBlackhole() {
        if (blackholePos.equals(BlockPos.ZERO)) return;
        if (!hasBlackhole()) {
            level.setBlockAndUpdate(blackholePos, ModEntries.get("black_hole").block().get().defaultBlockState());
            mass = (long) (MIN_MASS * (1 + level.random.nextDouble()));
            gotLaserBurst = false;
            setChanged();
        }
    }

    // ---- Transmutation ----

    private boolean isProcessComplete() {
        return ticksNeeded > 0 && ticksProcessed >= ticksNeeded;
    }

    private void processTransmutation() {
        ItemCapabilityHandler inv = items();
        if (inv == null) return;
        if (currentRecipe != null && isProcessComplete()) {
            handleRecipeOutput(inv);
        }
        if (currentRecipe == null) {
            updateRecipe(inv);
        }
        if (currentRecipe != null && !isProcessComplete()) {
            double multiplier = Math.log10(Math.max(1, transformers)) * (100 - energyConvertionRate) / 100D;
            int step = (int) Math.max(1, multiplier);
            if (processEnergy > 0 && energyStorage != null) {
                if (energyStorage.getEnergyStored() < (long) processEnergy * step) return;
                energyStorage.setEnergyStored(energyStorage.getEnergyStored() - processEnergy * step);
            }
            ticksProcessed = Math.min(ticksNeeded, ticksProcessed + step);
        }
    }

    private void updateRecipe(ItemCapabilityHandler inv) {
        ItemStack input = inv.getStackInSlot(0);
        if (input.isEmpty()) {
            currentRecipe = null;
            ticksProcessed = 0;
            ticksNeeded = 0;
            return;
        }
        KugelblitzRecipe recipe = findRecipe(input);
        if (recipe != null) {
            currentRecipe = recipe;
            ticksProcessed = 0;
            ticksNeeded = recipe.getBaseTime();
            processEnergy = recipe.getEnergy();
            inv.extractItem(0, 1, false);
        } else {
            currentRecipe = null;
            ticksNeeded = 0;
        }
    }

    @Nullable
    private KugelblitzRecipe findRecipe(ItemStack input) {
        if (!(level instanceof ServerLevel serverLevel)) return null;
        for (RecipeHolder<KugelblitzRecipe> holder : serverLevel.getRecipeManager()
                .getAllRecipesFor(KugelblitzRecipes.KUGELBLITZ_TYPE.get())) {
            KugelblitzRecipe recipe = holder.value();
            if (recipe.input().test(input) && frequencyMatches(recipe)) {
                return recipe;
            }
        }
        return null;
    }

    private void handleRecipeOutput(ItemCapabilityHandler inv) {
        ItemStack output = resolveOutput(currentRecipe);
        if (output.isEmpty()) {
            currentRecipe = null;
            return;
        }
        ItemStack existing = inv.getStackInSlot(1);
        if (existing.isEmpty()) {
            inv.setStackInSlot(1, output.copy());
        } else if (ItemStack.isSameItemSameComponents(existing, output)
                && existing.getCount() + output.getCount() <= existing.getMaxStackSize()) {
            existing.grow(output.getCount());
            inv.setStackInSlot(1, existing);
        } else {
            return; // output blocked; stay complete until slot frees
        }
        currentRecipe = null;
        ticksProcessed = 0;
        ticksNeeded = 0;
        setChanged();
    }

    private ItemStack resolveOutput(KugelblitzRecipe recipe) {
        ItemStack result = recipe.getResultStack();
        ItemStack input = recipe.getInputStack();
        if (!result.is(input.getItem())) {
            return result;
        }
        buildRandomPool();
        int id = ingredientId(result);
        return id >= 0 && id < orderedOutputs.size() ? orderedOutputs.get(id).copy() : result;
    }

    private int ingredientId(ItemStack result) {
        for (int i = 0; i < allowedRandomInputs.size(); i++) {
            if (allowedRandomInputs.get(i).is(result.getItem())) return i;
        }
        return -1;
    }

    private void buildRandomPool() {
        if (!orderedOutputs.isEmpty()) return;
        for (RecipeHolder<KugelblitzRecipe> holder : ((ServerLevel) level).getRecipeManager()
                .getAllRecipesFor(KugelblitzRecipes.KUGELBLITZ_TYPE.get())) {
            KugelblitzRecipe recipe = holder.value();
            ItemStack in = recipe.getInputStack();
            if (recipe.getResultStack().is(in.getItem())) {
                allowedRandomInputs.add(in);
            }
        }
        orderedOutputs.addAll(allowedRandomInputs.stream().map(ItemStack::copy).toList());
        Collections.shuffle(orderedOutputs, new Random(((ServerLevel) level).getSeed()));
    }

    private boolean frequencyMatches(KugelblitzRecipe recipe) {
        return frequency == targetFrequency(recipe.getResultStack());
    }

    private int targetFrequency(ItemStack stack) {
        long seed = ((ServerLevel) level).getSeed();
        return new Random(seed + stack.getItem().toString().hashCode()).nextInt(15);
    }

    // ---- GUI / redstone ----

    public void handleSliderUpdate(int buttonId, int ratio) {
        switch (buttonId) {
            case 0 -> energyConvertionRate = ratio;
            case 1 -> frequency = (byte) (0.15D * ratio);
        }
        markDirty();
    }

    public int getProgress() {
        return ticksNeeded > 0 ? (int) ((double) ticksProcessed / ticksNeeded * 100) : 0;
    }

    @Override
    public int comparatorSignal(int mode) {
        return switch (mode) {
            case ChamberPortBE.MODE_ENERGY -> energyStorage != null && energyStorage.getMaxEnergyStored() > 0
                    ? (int) ((long) energyStorage.getEnergyStored() * 15 / energyStorage.getMaxEnergyStored()) : 0;
            case ChamberPortBE.MODE_MASS -> (int) (mass * 15 / MAX_MASS);
            case ChamberPortBE.MODE_PROGRESS -> getProgress() * 15 / 100;
            case ChamberPortBE.MODE_ITEMS -> {
                ItemCapabilityHandler inv = items();
                if (inv == null) yield 0;
                ItemStack s = inv.getStackInSlot(0);
                yield s.isEmpty() ? 0 : s.getCount() * 15 / s.getMaxStackSize();
            }
            default -> 0;
        };
    }

    @Override
    public void applyRedstoneInput(int mode, int signal) {
        switch (mode) {
            case ChamberPortBE.MODE_FREQUENCY -> frequency = (byte) Math.clamp(signal, 0, 15);
            case ChamberPortBE.MODE_TRANSFORMATION_ENERGY_RATE -> energyConvertionRate = signal * 100 / 15;
        }
        markDirty();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new ChamberTerminalContainer(containerId, playerInventory, this, containerData);
    }
}
