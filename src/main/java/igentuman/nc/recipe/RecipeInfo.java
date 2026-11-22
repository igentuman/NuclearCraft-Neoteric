package igentuman.nc.recipe;

import igentuman.nc.block_entity.GlobalBlockEntity;
import igentuman.nc.block_entity.crafter.EngineersCrafterBE;
import igentuman.nc.config.Common;
import igentuman.nc.handler.crafter.ProcessorRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import igentuman.nc.registration.ModEntry;
import igentuman.nc.registration.RecipeSchemaKind;
import igentuman.nc.setup.ModEntries;
import igentuman.nc.util.caps.ItemCapDefinition;
import igentuman.nc.util.caps.FluidCapDefinition;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

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
    /** Set by load(): the saved batch's recipe is resolved on the first tick, once the level's recipe map exists. */
    private boolean recipePending = false;
    public int multiplier = 1;
    public int parallelLimit = 1;
    public int parallelProcessing = 1;
    public Recipe<?> recipe;
    public Recipe<?> lastRecipe;
    private final HashMap<String, Recipe<?>> allRecipes = new HashMap<>();
    private RecipeMap cachedRecipeMap;
    private ProcessingOrder craftingOrder;

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
        if (!validateCraftingOwner()) return;
        resolvePendingRecipe();

        if (stuck) {
            if (!produceOutputs()) {
                return;
            }
            changed = true;
            stuck = false;
            justProduced = true;
            completeCraftingBatch();
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
        if (required > 0 && be.generatesRecipeEnergy()) {
            long generated = Common.generated(required);
            if (generated > be.energyStorage.getMaxEnergyStored() - be.energyStorage.getEnergyStored()) {
                return;
            }
            be.energyStorage.addEnergy(generated);
        } else if(required > 0) {
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
                completeCraftingBatch();
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

        if (ModEntries.get(be.name) == null) return true;
        try (var transaction = Transaction.openRoot()) {
            if (!insertOutputs(upr, parallelProcessing, transaction)) return false;
            transaction.commit();
        }
        return true;
    }

    private void findRecipe() {
        if (craftingOrder != null) {
            Recipe<?> selected = getRecipes().get(craftingOrder.recipeId());
            int limit = craftingOrder.batchLimit(parallelLimit);
            if (limit > 0 && tryStartRecipe(selected, limit)) {
                craftingOrder.started(parallelProcessing);
            }
            return;
        }
        // Refresh before reusing an idle recipe; an active batch retains its consumed-input snapshot.
        var recipes = getRecipes();
        if(lastRecipe != null && recipes.containsValue(lastRecipe)) {
            if (tryStartRecipe(lastRecipe)) {
                return;
            }
        }
        for (Recipe<?> r : recipes.values()) {
            if (tryStartRecipe(r)) {
                return;
            }
        }
    }

    private boolean tryStartRecipe(Recipe<?> recipe) {
        return tryStartRecipe(recipe, parallelLimit);
    }

    private boolean tryStartRecipe(Recipe<?> recipe, int limit) {
        if (!(recipe instanceof UniversalProcessorRecipe upr)) return false;
        int batchSize = findMaximumParallelism(upr, limit);
        if (batchSize == 0) return false;

        int[] itemAssignment = itemInputAssignment(upr, batchSize);
        int[] fluidAssignment = fluidInputAssignment(upr, batchSize);
        if (itemAssignment == null || fluidAssignment == null) return false;
        if (!consumeInputs(upr, batchSize, itemAssignment, fluidAssignment)) return false;

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
        return true;
    }

    public boolean canAcceptCraftingOrder() {
        return craftingOrder == null && recipe == null && !stuck;
    }

    public boolean reserveCrafting(UUID jobId, BlockPos crafter, String selectedRecipe, int operations) {
        if (!canAcceptCraftingOrder() || operations <= 0 || !ProcessorRecipes.isAllowed(be.name)) return false;
        if (!(getRecipes().get(selectedRecipe) instanceof UniversalProcessorRecipe)) return false;
        craftingOrder = new ProcessingOrder(jobId, crafter, selectedRecipe, operations);
        be.setChanged();
        return true;
    }

    public boolean isReservedBy(UUID jobId) {
        return craftingOrder != null && craftingOrder.jobId().equals(jobId);
    }

    public boolean isCraftingComplete(UUID jobId) {
        return isReservedBy(jobId) && craftingOrder.isComplete();
    }

    public void releaseCrafting(UUID jobId) {
        if (!isReservedBy(jobId)) return;
        craftingOrder = null;
        be.setChanged();
    }

    private void completeCraftingBatch() {
        if (craftingOrder != null) craftingOrder.completed();
    }

    private boolean validateCraftingOwner() {
        if (craftingOrder == null) return true;
        Level level = getLevel();
        if (level == null || !level.isLoaded(craftingOrder.crafter())) return false;
        if (!ProcessorRecipes.isAllowed(be.name)
                || !(level.getBlockEntity(craftingOrder.crafter()) instanceof EngineersCrafterBE crafter)
                || !crafter.ownsProcessor(craftingOrder.jobId(), be.getBlockPos())) {
            releaseCrafting(craftingOrder.jobId());
        }
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
        try (var transaction = Transaction.openRoot()) {
            return insertOutputs(recipe, batchSize, transaction);
        }
    }

    /** Stages all outputs together; callers either commit the batch or discard the capacity probe. */
    private boolean insertOutputs(UniversalProcessorRecipe recipe, int batchSize, TransactionContext transaction) {
        ModEntry entry = ModEntries.get(be.name);
        ItemCapDefinition itemCap = entry != null ? entry.itemCap() : null;
        FluidCapDefinition fluidCap = entry != null ? entry.fluidCap() : null;

        if (!recipe.getItemOutputs().isEmpty()) {
            if (!be.contentHandler.hasItemCapability() || itemCap == null
                    || recipe.getItemOutputs().size() > itemCap.outputSlots) return false;
            var handler = be.contentHandler.getItemHandler();
            for (int i = 0; i < recipe.getItemOutputs().size(); i++) {
                ItemStack output = scaled(recipe.getOutputStack(i), batchSize);
                if (output.isEmpty() || handler.insert(itemCap.inputSlots + i, ItemResource.of(output),
                        output.getCount(), transaction) != output.getCount()) return false;
            }
        }
        if (!recipe.getFluidOutputs().isEmpty()) {
            if (!be.contentHandler.hasFluidCapability() || fluidCap == null
                    || recipe.getFluidOutputs().size() > fluidCap.outputTanks.size()) return false;
            var handler = be.contentHandler.getFluidHandler();
            int start = fluidCap.inputTanks.size();
            for (int i = 0; i < recipe.getFluidOutputs().size(); i++) {
                FluidStack output = scaled(recipe.getOutputFluidStack(i), batchSize);
                if (output.isEmpty() || handler.insert(start + i, FluidResource.of(output),
                        output.getAmount(), transaction) != output.getAmount()) return false;
            }
        }
        return true;
    }

    private boolean consumeInputs(UniversalProcessorRecipe recipe, int batchSize,
                                  int[] itemAssignment, int[] fluidAssignment) {
        try (var transaction = Transaction.openRoot()) {
            if (be.shouldConsumeItemInputs() && be.contentHandler.hasItemCapability()) {
                var handler = be.contentHandler.getItemHandler();
                for (int i = 0; i < itemAssignment.length; i++) {
                    int slot = itemAssignment[i];
                    var ingredient = recipe.getItemInputs().get(i);
                    int amount = scaledAmount(ingredient.count(), batchSize);
                    ItemStack input = handler.getStackInSlot(slot);
                    if (input.isEmpty() || amount <= 0 || !ingredient.ingredient().test(input)
                            || handler.extract(slot, ItemResource.of(input), amount, transaction) != amount) return false;
                }
            }
            if (be.contentHandler.hasFluidCapability()) {
                var handler = be.contentHandler.getFluidHandler();
                for (int i = 0; i < fluidAssignment.length; i++) {
                    int tank = fluidAssignment[i];
                    var ingredient = recipe.getFluidInputs().get(i);
                    int amount = scaledAmount(ingredient.amount(), batchSize);
                    FluidStack input = handler.getFluidInTank(tank);
                    if (input.isEmpty() || amount <= 0 || !ingredient.ingredient().test(input)
                            || handler.extract(tank, FluidResource.of(input), amount, transaction) != amount) return false;
                }
            }
            transaction.commit();
        }
        changed = true;
        return true;
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

    /**
     * Restores the in-progress (or stuck) batch saved by {@link #save()}. Its inputs were
     * consumed before the save, so it must resume rather than start a new batch. Block entities
     * load before they get a level, when no recipe map is reachable, so this waits for the first
     * tick. A recipe removed by a datapack since the save leaves the machine idle.
     */
    private void resolvePendingRecipe() {
        if (!recipePending || getLevel() == null) return;
        recipePending = false;
        Recipe<?> loadedRecipe = recipeId == null || recipeId.isEmpty() ? null : getRecipeFromTag(recipeId);
        if (stuck) {
            lastRecipe = loadedRecipe;
        } else {
            recipe = loadedRecipe;
        }
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
        if (getLevel() != null && getLevel().isClientSide()) getRecipes();
        if(recipe == null && recipeId != null && !recipeId.isEmpty()) {
            recipe = getRecipeFromTag(recipeId);
        }
        return recipe;
    }

    @SuppressWarnings("unchecked")
    public HashMap<String, Recipe<?>> getRecipes() {
        RecipeMap current = RecipeSources.recipes(getLevel());
        if (cachedRecipeMap != current) {
            allRecipes.clear();
            cachedRecipeMap = current;
            if (getLevel() != null && getLevel().isClientSide()) {
                recipe = null;
                lastRecipe = null;
            }
            ModEntry entry = ModEntries.get(be.name);
            if (entry != null && entry.recipeType() != null && entry.recipeFamily() != null
                    && entry.recipeFamily().schemaKind() == RecipeSchemaKind.UNIVERSAL_PROCESSOR) {
                var recipeType = (RecipeType<UniversalProcessorRecipe>) entry.recipeType().get();
                current.byType(recipeType).forEach(holder -> {
                    if (holder.value().isComplete()) {
                        allRecipes.put(holder.id().identifier().toString(), holder.value());
                    }
                });
            }
        }
        return allRecipes;
    }

    private Recipe<?> getRecipeFromTag(String recipe) {
        Recipe<?> cachedRecipe = getRecipes().get(recipe);
        if (cachedRecipe != null) return cachedRecipe;
        Identifier id = Identifier.tryParse(recipe);
        if (id == null) return null;
        return RecipeSources.byId(getLevel(), id)
                .map(holder -> holder.value()).orElse(null);
    }

    private Level getLevel() {
        return be.getLevel();
    }

    public Tag save() {
        CompoundTag data = new CompoundTag();
        data.putInt("ticks", ticks);
        data.putInt("ticksNeeded", ticksNeeded);
        data.putInt("energyPerTick", energyPerTick);
        data.putInt("parallelProcessing", parallelProcessing);
        data.putBoolean("stuck", stuck);
        if (craftingOrder != null) data.put("CraftingOrder", craftingOrder.save());
        if((recipe != null || stuck || recipePending) && recipeId != null && !recipeId.isEmpty()) {
            data.putString("recipe", recipeId);
        }
        return data;
    }

    public void load(Tag nbt) {
        if(nbt instanceof CompoundTag) {
            craftingOrder = ((CompoundTag) nbt).contains("CraftingOrder")
                    ? ProcessingOrder.load(((CompoundTag) nbt).getCompoundOrEmpty("CraftingOrder")) : null;
            ticks = ((CompoundTag) nbt).getIntOr("ticks", 0);
            ticksNeeded = ((CompoundTag) nbt).getIntOr("ticksNeeded", 0);
            energyPerTick = ((CompoundTag) nbt).getIntOr("energyPerTick", 0);
            parallelProcessing = Math.max(1, ((CompoundTag) nbt).getIntOr("parallelProcessing", 0));
            stuck = ((CompoundTag) nbt).getBooleanOr("stuck", false);
            recipeId = ((CompoundTag) nbt).getStringOr("recipe", "");
            recipe = null;
            recipePending = !recipeId.isEmpty();
            resolvePendingRecipe();
        }
    }
}
