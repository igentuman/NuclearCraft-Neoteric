package igentuman.nc.recipes.processors;

import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import igentuman.nc.recipes.type.NcRecipe;
import igentuman.nc.setup.processors.Processors;
import igentuman.nc.util.annotation.NothingNullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

import static igentuman.nc.compat.GlobalVars.CATALYSTS;
import static igentuman.nc.compat.GlobalVars.RECIPE_CLASSES;

@NothingNullByDefault
public class RockCrusherRecipe extends NcRecipe {
    public RockCrusherRecipe(ResourceLocation id,
                             ItemStackIngredient[] input, ItemStack[] output,
                             FluidStackIngredient[] inputFluids, FluidStack[] outputFluids,
                             double timeModifier, double powerModifier, double heatModifier) {
        super(id, input, output, timeModifier, powerModifier, heatModifier);
        ID = Processors.ROCK_CRUSHER;
        RECIPE_CLASSES.put(ID, this.getClass());
        CATALYSTS.put(ID, List.of(getToastSymbol()));
    }
}