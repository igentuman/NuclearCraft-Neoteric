package igentuman.nc.api.recipe;

import igentuman.nc.recipe.RecipeSources;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.List;
import java.util.Optional;

/** Read-only description of a NuclearCraft processor recipe with unmodifiable input and output lists. */
public interface ProcessorRecipeView {

    /** Looks up a processor recipe in the server recipe map or the synchronized client snapshot. */
    static Optional<ProcessorRecipeView> find(Level level, Identifier id) {
        return RecipeSources.byId(level, id).map(RecipeHolder::value)
                .filter(ProcessorRecipeView.class::isInstance)
                .map(ProcessorRecipeView.class::cast);
    }

    String getProcessorName();

    List<SizedIngredient> getItemInputs();

    List<SizedFluidIngredient> getFluidInputs();

    List<ItemOutput> getItemOutputs();

    List<FluidOutput> getFluidOutputs();

    int getBaseProcessTime();

    int getBaseEnergyPerTick();

    /** Returns the duration in ticks after applying the local processor configuration. */
    int getProcessTime();

    /** Returns the FE per tick after applying the local processor configuration. */
    int getEnergyPerTick();
}
