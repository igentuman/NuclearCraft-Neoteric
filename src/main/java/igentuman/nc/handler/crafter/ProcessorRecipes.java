package igentuman.nc.handler.crafter;

import igentuman.nc.compat.ProcessorRecipeStacks;
import igentuman.nc.recipe.UniversalProcessorRecipe;
import igentuman.nc.recipe.UniversalProcessorRecipeSerializer;
import igentuman.nc.registration.ModEntry;
import igentuman.nc.setup.ModEntries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/** Lookup helpers for universal processor recipes shared by the encoder, EMI/JEI bridges and the crafter. */
public final class ProcessorRecipes {

    private ProcessorRecipes() {}

    public static boolean isProcessorEntry(ModEntry entry) {
        return entry != null && entry.hasRecipes() && entry.isEnabled()
                && entry.recipeType() != null && entry.recipeSerializer() != null
                && entry.recipeSerializer().get() instanceof UniversalProcessorRecipeSerializer;
    }

    public static List<ModEntry> processorEntries() {
        List<ModEntry> out = new ArrayList<>();
        for (ModEntry e : ModEntries.ENTRIES.values()) {
            if (isProcessorEntry(e)) out.add(e);
        }
        return out;
    }

    @Nullable
    public static UniversalProcessorRecipe byId(RecipeManager manager, ResourceLocation id) {
        Recipe<?> r = manager.byKey(id).map(RecipeHolder::value).orElse(null);
        return r instanceof UniversalProcessorRecipe upr && upr.isComplete() ? upr : null;
    }

    /**
     * Finds the registered id of a recipe instance (EMI/JEI hand us the value, not the holder). Identity is
     * tried first; a structurally equal recipe is accepted as fallback for copies or stale instances after a reload.
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public static ResourceLocation idOf(RecipeManager manager, UniversalProcessorRecipe recipe) {
        ModEntry entry = ModEntries.get(recipe.getProcessorName());
        if (!isProcessorEntry(entry)) return null;
        RecipeType<UniversalProcessorRecipe> type = (RecipeType<UniversalProcessorRecipe>) entry.recipeType().get();
        var holders = manager.getAllRecipesFor(type);
        for (RecipeHolder<UniversalProcessorRecipe> h : holders) {
            if (h.value() == recipe) return h.id();
        }
        ProcessorRecipeStacks want = ProcessorRecipeStacks.of(recipe);
        for (RecipeHolder<UniversalProcessorRecipe> h : holders) {
            if (sameStacks(want, ProcessorRecipeStacks.of(h.value()))) return h.id();
        }
        return null;
    }

    private static boolean sameStacks(ProcessorRecipeStacks a, ProcessorRecipeStacks b) {
        return sameItems(a.primaryItemInputs(), b.primaryItemInputs())
                && sameFluids(a.primaryFluidInputs(), b.primaryFluidInputs())
                && sameItems(a.itemOutputs(), b.itemOutputs())
                && sameFluids(a.fluidOutputs(), b.fluidOutputs());
    }

    private static boolean sameItems(List<ItemStack> a, List<ItemStack> b) {
        if (a.size() != b.size()) return false;
        for (int i = 0; i < a.size(); i++) {
            if (!ItemStack.isSameItemSameComponents(a.get(i), b.get(i)) || a.get(i).getCount() != b.get(i).getCount()) return false;
        }
        return true;
    }

    private static boolean sameFluids(List<FluidStack> a, List<FluidStack> b) {
        if (a.size() != b.size()) return false;
        for (int i = 0; i < a.size(); i++) {
            if (!FluidStack.isSameFluidSameComponents(a.get(i), b.get(i)) || a.get(i).getAmount() != b.get(i).getAmount()) return false;
        }
        return true;
    }

    /**
     * Finds a complete processor recipe whose item inputs are exactly matched by the given (non-empty)
     * ghost stacks, ignoring counts. The ghost grid cannot express fluids, so among candidates the one
     * with the fewest fluid inputs wins (an item-only recipe beats one that also needs a fluid); ties keep
     * registration order. Used for manual encoding; JEI/EMI transfer is exact.
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public static RecipeHolder<UniversalProcessorRecipe> matchByItems(RecipeManager manager, List<ItemStack> ghosts) {
        List<ItemStack> items = new ArrayList<>();
        for (ItemStack s : ghosts) if (!s.isEmpty()) items.add(s);
        if (items.isEmpty()) return null;
        RecipeHolder<UniversalProcessorRecipe> best = null;
        for (ModEntry entry : processorEntries()) {
            RecipeType<UniversalProcessorRecipe> type = (RecipeType<UniversalProcessorRecipe>) entry.recipeType().get();
            for (RecipeHolder<UniversalProcessorRecipe> h : manager.getAllRecipesFor(type)) {
                UniversalProcessorRecipe r = h.value();
                if (!r.isComplete()) continue;
                if (r.getItemInputs().size() != items.size()) continue;
                if (!matches(r.getItemInputs(), items)) continue;
                if (best == null || r.getFluidInputs().size() < best.value().getFluidInputs().size()) best = h;
                if (best.value().getFluidInputs().isEmpty()) return best;
            }
        }
        return best;
    }

    private static boolean matches(List<SizedIngredient> ingredients, List<ItemStack> items) {
        boolean[] used = new boolean[items.size()];
        return assign(ingredients, items, 0, used);
    }

    private static boolean assign(List<SizedIngredient> ingredients, List<ItemStack> items, int idx, boolean[] used) {
        if (idx == ingredients.size()) return true;
        SizedIngredient ing = ingredients.get(idx);
        for (int i = 0; i < items.size(); i++) {
            if (used[i] || !ing.ingredient().test(items.get(i))) continue;
            used[i] = true;
            if (assign(ingredients, items, idx + 1, used)) return true;
            used[i] = false;
        }
        return false;
    }
}
