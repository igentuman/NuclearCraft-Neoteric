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
        int required = energyPerTick * multiplier;
        if(required > 0) {
            int extracted = be.energyStorage.getEnergyStored() >= required ? required : 0;
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
                ItemStack output = upr.getOutputStack(i);
                int slot = outputSlotStart + i;
                ItemStack existing = itemHandler.getStackInSlot(slot);
                if (!existing.isEmpty()) {
                    if (!ItemStack.isSameItemSameComponents(existing, output)) return false;
                    if (existing.getCount() + output.getCount() > existing.getMaxStackSize()) return false;
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
                FluidStack output = upr.getOutputFluidStack(i);
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
                ItemStack output = upr.getOutputStack(i);
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
                fluidHandler.fillTank(tankIdx, upr.getOutputFluidStack(i), IFluidHandler.FluidAction.EXECUTE);
            }
        }

        return true;
    }

    public void consumeInputs() {
        if (recipe == null) return;
        if (!(recipe instanceof UniversalProcessorRecipe upr)) return;

        ModEntry entry = be.name != null ? ModEntries.get(be.name) : null;

        // Consume item inputs
        List<SizedIngredient> itemInputs = upr.getItemInputs();
        if (be.shouldConsumeItemInputs() && be.contentHandler.hasItemCapability()) {
            var itemHandler = be.contentHandler.getItemHandler();
            int inputSlots = (entry != null && entry.itemCap() != null)
                    ? entry.itemCap().inputSlots : itemHandler.getSlots();
            boolean[] usedItemSlots = new boolean[inputSlots];
            for (SizedIngredient ingredient : itemInputs) {
                for (int slot = 0; slot < inputSlots; slot++) {
                    if (usedItemSlots[slot]) continue;
                    if (ingredient.test(itemHandler.getStackInSlot(slot))) {
                        itemHandler.extractItem(slot, ingredient.count(), false);
                        usedItemSlots[slot] = true;
                        break;
                    }
                }
            }
        }

        // Consume fluid inputs
        List<SizedFluidIngredient> fluidInputs = upr.getFluidInputs();
        if (be.contentHandler.hasFluidCapability()) {
            var fluidHandler = be.contentHandler.getFluidHandler();
            int inputTanks = (entry != null && entry.fluidCap() != null)
                    ? entry.fluidCap().inputTanks.size() : fluidHandler.getTanks();
            boolean[] usedFluidTanks = new boolean[inputTanks];
            for (SizedFluidIngredient ingredient : fluidInputs) {
                for (int tank = 0; tank < inputTanks; tank++) {
                    if (usedFluidTanks[tank]) continue;
                    if (ingredient.test(fluidHandler.getFluidInTank(tank))) {
                        fluidHandler.drainTank(tank, ingredient.amount(), IFluidHandler.FluidAction.EXECUTE);
                        usedFluidTanks[tank] = true;
                        break;
                    }
                }
            }
        }
        changed = true;
    }

    private void findRecipe() {
        if(lastRecipe != null) {
            if (isValidRecipe(lastRecipe)) {
                setRecipe(lastRecipe);
                return;
            }
        }
        for (Recipe<?> r : getRecipes().values()) {
            if (isValidRecipe(r)) {
                setRecipe(r);
                return;
            }
        }
    }

    public void setRecipe(Recipe<?> recipe) {
        this.recipe = recipe;
        this.lastRecipe = null;
        // Resolve recipeId from the recipes map
        this.recipeId = null;
        for (var entry : getRecipes().entrySet()) {
            if (entry.getValue() == recipe) {
                this.recipeId = entry.getKey();
                break;
            }
        }
        clear();
        if (recipe instanceof UniversalProcessorRecipe upr) {
            this.ticksNeeded = upr.getProcessTime();
            this.energyPerTick = upr.getEnergyPerTick();
        }
        consumeInputs();
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
        if(recipe != null && recipeId != null) {
            data.putString("recipe", recipeId);
        }
        return data;
    }

    public void load(Tag nbt) {
        if(nbt instanceof CompoundTag) {
            ticks = ((CompoundTag) nbt).getInt("ticks");
            ticksNeeded = ((CompoundTag) nbt).getInt("ticksNeeded");
            energyPerTick = ((CompoundTag) nbt).getInt("energyPerTick");
            recipeId = ((CompoundTag) nbt).getString("recipe");
            recipe = null;
            if(!recipeId.isEmpty()) {
                recipe = getRecipeFromTag(recipeId);
            }
        }
    }
}
