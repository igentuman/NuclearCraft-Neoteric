package igentuman.nc.recipe;

import igentuman.nc.block_entity.GlobalBlockEntity;
import igentuman.nc.util.ClientUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import igentuman.nc.registration.ModEntry;
import igentuman.nc.setup.ModEntries;
import igentuman.nc.util.caps.ItemCapDefinition;
import igentuman.nc.util.caps.FluidCapDefinition;

import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;

import static igentuman.nc.NuclearCraft.rlFromString;

/** Per-block-entity recipe runtime: matches a recipe, drains energy, tracks progress, consumes inputs and produces outputs. */
public class RecipeInfo {

    public boolean stuck = false;
    public boolean changed = false;
    public boolean active = false;
    /** True only on the tick an operation's outputs were produced; read by catalysts. */
    public boolean justProduced = false;
    public int ticks = 0;
    public int ticksNeeded = 0;
    public int energyPerTick = 0;
    public final GlobalBlockEntity be;
    private String recipeId;
    public int multiplier = 1;
    public int parallelLimit = 1;
    public int parallelProcessing = 1;
    public Recipe<?> recipe;
    public Recipe<?> lastRecipe;
    private HashMap<String, Recipe<?>> allRecipes = new HashMap<>();

    public RecipeInfo(GlobalBlockEntity be) {
        this.be = be;
    }

    public boolean isDone() {
        return ticks >= ticksNeeded;
    }

    public void tick() {
        changed = false;
        justProduced = false;
        active = false;
        if (!be.supportRecipes()) {
            return;
        }

        if (stuck) {
            if (!produceOutputs()) {
                return;
            }
            changed = true;
            stuck = false;
            justProduced = true;
        }

        if (!hasRecipe()) {
            findRecipe();
        }

        if (!hasRecipe()) {
            ticks = 0;
            return;
        }

        if (be.energyStorage == null) {
            return; // no energy storage, cannot process
        }
        long required = (long) energyPerTick * multiplier;
        if(required > 0) {
            long extracted = be.energyStorage.getEnergyStored() >= required ? required : 0;
            if (extracted < required) {
                return; // not enough energy, stall
            }
            be.energyStorage.drainEnergy(required);
        } else  {
            be.energyStorage.setEnergyStored(be.energyStorage.getEnergyStored()-required);
        }

        ticks+=multiplier;
        ticks = Math.min(ticks, ticksNeeded);
        be.progress = getProgress();
        changed = true;
        active = true;

        if (isDone()) {
            lastRecipe = recipe;
            recipe = null;

            if (!produceOutputs()) {
                stuck = true;
            } else {
                justProduced = true;
            }
        }
    }

    /**
     * Restores catalyst-mutable knobs to their per-recipe baseline. Called by the host BE
     * each tick before catalyst preTicks so catalyst effects reapply fresh and never compound.
     */
    public void resetCatalystModifiers() {
        multiplier = 1;
        parallelLimit = 1;
        if (recipe instanceof UniversalProcessorRecipe upr) {
            energyPerTick = upr.getEnergyPerTick();
        }
    }

    public boolean produceOutputs() {
        if (lastRecipe == null) return true;
        if (!(lastRecipe instanceof UniversalProcessorRecipe upr)) return true;

        ModEntry entry = ModEntries.get(be.name);
        if (entry == null) return true;

        ItemCapDefinition itemCap = entry.itemCap();
        FluidCapDefinition fluidCap = entry.fluidCap();

        int itemOutputSize = upr.getItemOutputs().size();
        int fluidOutputSize = upr.getFluidOutputs().size();

        // Determine output slot range: output slots start right after input slots
        int outputSlotStart = (itemCap != null) ? itemCap.inputSlots : 0;
        int outputSlotCount = (itemCap != null) ? itemCap.outputSlots : 0;

        // Determine output tank range: output tanks start right after input tanks
        int outputTankStart = (fluidCap != null) ? fluidCap.inputTanks.size() : 0;
        int outputTankCount = (fluidCap != null) ? fluidCap.outputTanks.size() : 0;

        // Check if all item outputs can fit (tags resolved to concrete stacks here)
        if (be.contentHandler.hasItemCapability()) {
            var itemHandler = be.contentHandler.getItemHandler();
            for (int i = 0; i < itemOutputSize; i++) {
                if (i >= outputSlotCount) return false;
                ItemStack output = scaled(upr.getOutputStack(i), parallelProcessing);
                if (output.isEmpty()) return false;
                int slot = outputSlotStart + i;
                ItemStack existing = itemHandler.getStackInSlot(slot);
                if (!existing.isEmpty()) {
                    if (!ItemStack.isSameItemSameComponents(existing, output)) return false;
                    int limit = Math.min(existing.getMaxStackSize(), itemHandler.getSlotLimit(slot));
                    if ((long) existing.getCount() + output.getCount() > limit) return false;
                } else if (output.getCount() > Math.min(output.getMaxStackSize(), itemHandler.getSlotLimit(slot))) {
                    return false;
                }
            }
        } else if (itemOutputSize > 0) {
            return false;
        }

        // Check if all fluid outputs can fit
        if (be.contentHandler.hasFluidCapability()) {
            var fluidHandler = be.contentHandler.getFluidHandler();
            for (int i = 0; i < fluidOutputSize; i++) {
                if (i >= outputTankCount) return false;
                int tankIdx = outputTankStart + i;
                FluidStack output = scaled(upr.getOutputFluidStack(i), parallelProcessing);
                if (output.isEmpty()) return false;
                int filled = fluidHandler.fillTank(tankIdx, output, IFluidHandler.FluidAction.SIMULATE);
                if (filled < output.getAmount()) return false;
            }
        } else if (fluidOutputSize > 0) {
            return false;
        }

        // All checks passed - place outputs
        if (be.contentHandler.hasItemCapability()) {
            var itemHandler = be.contentHandler.getItemHandler();
            for (int i = 0; i < itemOutputSize; i++) {
                int slot = outputSlotStart + i;
                ItemStack output = scaled(upr.getOutputStack(i), parallelProcessing);
                ItemStack existing = itemHandler.getStackInSlot(slot);
                if (existing.isEmpty()) {
                    itemHandler.setStackInSlot(slot, output);
                } else {
                    existing.grow(output.getCount());
                }
            }
        }

        // Place fluid outputs
        if (be.contentHandler.hasFluidCapability()) {
            var fluidHandler = be.contentHandler.getFluidHandler();
            for (int i = 0; i < fluidOutputSize; i++) {
                int tankIdx = outputTankStart + i;
                fluidHandler.fillTank(tankIdx, scaled(upr.getOutputFluidStack(i), parallelProcessing), IFluidHandler.FluidAction.EXECUTE);
            }
        }

        return true;
    }

    private void findRecipe() {
        if(lastRecipe != null) {
            if (tryStartRecipe(lastRecipe)) {
                return;
            }
        }
        for (Recipe<?> r : getRecipes().values()) {
            if (tryStartRecipe(r)) {
                return;
            }
        }
    }

    private boolean tryStartRecipe(Recipe<?> recipe) {
        if (!(recipe instanceof UniversalProcessorRecipe upr)) return false;
        int batchSize = findMaximumParallelism(upr, parallelLimit);
        if (batchSize == 0) return false;

        int[] itemAssignment = itemInputAssignment(upr, batchSize);
        int[] fluidAssignment = fluidInputAssignment(upr, batchSize);
        if (itemAssignment == null || fluidAssignment == null) return false;

        this.recipe = recipe;
        this.lastRecipe = null;
        this.parallelProcessing = batchSize;
        // Resolve recipeId from the recipes map
        this.recipeId = null;
        for (var entry : getRecipes().entrySet()) {
            if (entry.getValue() == recipe) {
                this.recipeId = entry.getKey();
                break;
            }
        }
        clear();
        this.ticksNeeded = upr.getProcessTime();
        this.energyPerTick = upr.getEnergyPerTick();
        consumeInputs(upr, batchSize, itemAssignment, fluidAssignment);
        return true;
    }

    /** Finds the largest feasible batch in O(log limit) simulations instead of scanning down. */
    private int findMaximumParallelism(UniversalProcessorRecipe recipe, int requested) {
        int high = Math.max(1, requested);
        if (itemInputAssignment(recipe, 1) == null
                || fluidInputAssignment(recipe, 1) == null
                || !canFitOutputs(recipe, 1)) return 0;

        int low = 2;
        int result = 1;
        while (low <= high) {
            int candidate = low + (high - low) / 2;
            if (itemInputAssignment(recipe, candidate) != null
                    && fluidInputAssignment(recipe, candidate) != null
                    && canFitOutputs(recipe, candidate)) {
                result = candidate;
                low = candidate + 1;
            } else {
                high = candidate - 1;
            }
        }
        return result;
    }

    private int[] itemInputAssignment(UniversalProcessorRecipe recipe, int batchSize) {
        List<SizedIngredient> ingredients = recipe.getItemInputs();
        int[] assignment = new int[ingredients.size()];
        java.util.Arrays.fill(assignment, -1);
        if (ingredients.isEmpty()) return assignment;
        if (!be.contentHandler.hasItemCapability()) return null;

        ModEntry entry = ModEntries.get(be.name);
        int slots = entry != null && entry.itemCap() != null
                ? entry.itemCap().inputSlots : be.contentHandler.getItemHandler().getSlots();
        boolean[] used = new boolean[slots];
        return assignItemInput(ingredients, batchSize, 0, used, assignment) ? assignment : null;
    }

    private boolean assignItemInput(List<SizedIngredient> ingredients, int batchSize, int index,
                                    boolean[] used, int[] assignment) {
        if (index == ingredients.size()) return true;
        SizedIngredient ingredient = ingredients.get(index);
        int required = scaledAmount(ingredient.count(), be.shouldConsumeItemInputs() ? batchSize : 1);
        if (required < 0) return false;
        var handler = be.contentHandler.getItemHandler();
        for (int slot = 0; slot < used.length; slot++) {
            ItemStack stack = handler.getStackInSlot(slot);
            if (used[slot] || stack.getCount() < required || !ingredient.ingredient().test(stack)) continue;
            used[slot] = true;
            assignment[index] = slot;
            if (assignItemInput(ingredients, batchSize, index + 1, used, assignment)) return true;
            assignment[index] = -1;
            used[slot] = false;
        }
        return false;
    }

    private int[] fluidInputAssignment(UniversalProcessorRecipe recipe, int batchSize) {
        List<SizedFluidIngredient> ingredients = recipe.getFluidInputs();
        int[] assignment = new int[ingredients.size()];
        java.util.Arrays.fill(assignment, -1);
        if (ingredients.isEmpty()) return assignment;
        if (!be.contentHandler.hasFluidCapability()) return null;

        ModEntry entry = ModEntries.get(be.name);
        int tanks = entry != null && entry.fluidCap() != null
                ? entry.fluidCap().inputTanks.size() : be.contentHandler.getFluidHandler().getTanks();
        boolean[] used = new boolean[tanks];
        return assignFluidInput(ingredients, batchSize, 0, used, assignment) ? assignment : null;
    }

    private boolean assignFluidInput(List<SizedFluidIngredient> ingredients, int batchSize, int index,
                                     boolean[] used, int[] assignment) {
        if (index == ingredients.size()) return true;
        SizedFluidIngredient ingredient = ingredients.get(index);
        int required = scaledAmount(ingredient.amount(), batchSize);
        if (required < 0) return false;
        var handler = be.contentHandler.getFluidHandler();
        for (int tank = 0; tank < used.length; tank++) {
            FluidStack stack = handler.getFluidInTank(tank);
            if (used[tank] || stack.getAmount() < required || !ingredient.ingredient().test(stack)) continue;
            used[tank] = true;
            assignment[index] = tank;
            if (assignFluidInput(ingredients, batchSize, index + 1, used, assignment)) return true;
            assignment[index] = -1;
            used[tank] = false;
        }
        return false;
    }

    private boolean canFitOutputs(UniversalProcessorRecipe recipe, int batchSize) {
        ModEntry entry = ModEntries.get(be.name);
        ItemCapDefinition itemCap = entry != null ? entry.itemCap() : null;
        FluidCapDefinition fluidCap = entry != null ? entry.fluidCap() : null;

        if (!recipe.getItemOutputs().isEmpty()) {
            if (!be.contentHandler.hasItemCapability() || itemCap == null
                    || recipe.getItemOutputs().size() > itemCap.outputSlots) return false;
            var handler = be.contentHandler.getItemHandler();
            for (int i = 0; i < recipe.getItemOutputs().size(); i++) {
                ItemStack output = scaled(recipe.getOutputStack(i), batchSize);
                if (output.isEmpty()) return false;
                int slot = itemCap.inputSlots + i;
                ItemStack existing = handler.getStackInSlot(slot);
                int limit = Math.min(output.getMaxStackSize(), handler.getSlotLimit(slot));
                if (!existing.isEmpty()) {
                    if (!ItemStack.isSameItemSameComponents(existing, output)) return false;
                    if ((long) existing.getCount() + output.getCount() > limit) return false;
                } else if (output.getCount() > limit) {
                    return false;
                }
            }
        }

        if (!recipe.getFluidOutputs().isEmpty()) {
            if (!be.contentHandler.hasFluidCapability() || fluidCap == null
                    || recipe.getFluidOutputs().size() > fluidCap.outputTanks.size()) return false;
            var handler = be.contentHandler.getFluidHandler();
            int start = fluidCap.inputTanks.size();
            for (int i = 0; i < recipe.getFluidOutputs().size(); i++) {
                FluidStack output = scaled(recipe.getOutputFluidStack(i), batchSize);
                if (output.isEmpty()) return false;
                if (handler.fillTank(start + i, output, IFluidHandler.FluidAction.SIMULATE) < output.getAmount()) {
                    return false;
                }
            }
        }
        return true;
    }

    private void consumeInputs(UniversalProcessorRecipe recipe, int batchSize,
                               int[] itemAssignment, int[] fluidAssignment) {
        if (be.shouldConsumeItemInputs() && be.contentHandler.hasItemCapability()) {
            var handler = be.contentHandler.getItemHandler();
            for (int i = 0; i < itemAssignment.length; i++) {
                handler.extractItem(itemAssignment[i], recipe.getItemInputs().get(i).count() * batchSize, false);
            }
        }
        if (be.contentHandler.hasFluidCapability()) {
            var handler = be.contentHandler.getFluidHandler();
            for (int i = 0; i < fluidAssignment.length; i++) {
                handler.drainTank(fluidAssignment[i], recipe.getFluidInputs().get(i).amount() * batchSize,
                        IFluidHandler.FluidAction.EXECUTE);
            }
        }
        changed = true;
    }

    private static int scaledAmount(int amount, int multiplier) {
        long scaled = (long) amount * multiplier;
        return scaled > Integer.MAX_VALUE ? -1 : (int) scaled;
    }

    private static ItemStack scaled(ItemStack stack, int multiplier) {
        int amount = scaledAmount(stack.getCount(), multiplier);
        return stack.isEmpty() || amount < 0 ? ItemStack.EMPTY : stack.copyWithCount(amount);
    }

    private static FluidStack scaled(FluidStack stack, int multiplier) {
        int amount = scaledAmount(stack.getAmount(), multiplier);
        return stack.isEmpty() || amount < 0 ? FluidStack.EMPTY : stack.copyWithAmount(amount);
    }

    @SuppressWarnings("unchecked")
    public boolean isValidRecipe(Recipe<?> recipe) {
        if (!(recipe instanceof UniversalProcessorRecipe)) return false;
        return ((Recipe<ProcessorRecipeInput>) recipe).matches(be.inputs(), getLevel());
    }

    private boolean hasRecipe() {
        return recipe != null;
    }

    public int getProgress() {
        return (int) ((double)ticks / ticksNeeded * 100);
    }

    public void clear() {
        ticks = 0;
        ticksNeeded = 0;
        energyPerTick = 0;
        be.progress = 0;
    }

    public Recipe<?> recipe() {
        if(recipe == null && recipeId != null && !recipeId.isEmpty()) {
            recipe = getRecipeFromTag(recipeId);
        }
        return recipe;
    }

    @SuppressWarnings("unchecked")
    public HashMap<String, Recipe<?>> getRecipes() {
        if(allRecipes.isEmpty()) {
            Level level = getLevel();
            if (level != null) {
                ModEntry entry = ModEntries.get(be.name);
                if (entry != null) {
                    var recipeType = (RecipeType<UniversalProcessorRecipe>) entry.recipeType().get();
                    level.getRecipeManager().getAllRecipesFor(recipeType).forEach(r -> {
                        if (r.value().isComplete()) {
                            allRecipes.put(r.id().toString(), r.value());
                        }
                    });
                }
            }
        }
        return allRecipes;
    }

    private Recipe<?> getRecipeFromTag(String recipe) {
        Recipe<?> cachedRecipe = getRecipes().getOrDefault(recipe, null);
        if(cachedRecipe != null) {
            return cachedRecipe;
        }
        ResourceLocation id = rlFromString(recipe);
        if(getLevel() == null) return null;
        try {
            return getLevel().getRecipeManager().byKey(id).get().value();
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    private Level getLevel()
    {
        if(be != null) return be.getLevel();
        return switch (FMLEnvironment.dist) {
            case CLIENT -> ClientUtil.tryGetClientWorld();
            case DEDICATED_SERVER -> ServerLifecycleHooks.getCurrentServer().overworld();
        };
    }

    public Tag save() {
        CompoundTag data = new CompoundTag();
        data.putInt("ticks", ticks);
        data.putInt("ticksNeeded", ticksNeeded);
        data.putInt("energyPerTick", energyPerTick);
        data.putInt("parallelProcessing", parallelProcessing);
        data.putBoolean("stuck", stuck);
        if((recipe != null || stuck) && recipeId != null) {
            data.putString("recipe", recipeId);
        }
        return data;
    }

    public void load(Tag nbt) {
        if(nbt instanceof CompoundTag) {
            ticks = ((CompoundTag) nbt).getInt("ticks");
            ticksNeeded = ((CompoundTag) nbt).getInt("ticksNeeded");
            energyPerTick = ((CompoundTag) nbt).getInt("energyPerTick");
            parallelProcessing = Math.max(1, ((CompoundTag) nbt).getInt("parallelProcessing"));
            stuck = ((CompoundTag) nbt).getBoolean("stuck");
            recipeId = ((CompoundTag) nbt).getString("recipe");
            recipe = null;
            if(!recipeId.isEmpty()) {
                Recipe<?> loadedRecipe = getRecipeFromTag(recipeId);
                if (stuck) {
                    lastRecipe = loadedRecipe;
                } else {
                    recipe = loadedRecipe;
                }
            }
        }
    }
}
