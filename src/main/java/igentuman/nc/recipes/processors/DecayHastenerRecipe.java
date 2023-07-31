package igentuman.nc.recipes.processors;

import igentuman.nc.recipes.type.ItemStackToItemStackRecipe;
import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import igentuman.nc.setup.processors.Processors;
import igentuman.nc.util.annotation.NothingNullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

import static igentuman.nc.compat.GlobalVars.*;

@NothingNullByDefault
public class DecayHastenerRecipe extends ItemStackToItemStackRecipe {

    public DecayHastenerRecipe(ResourceLocation id, ItemStackIngredient input, ItemStack[] output, double timeModifier, double powerModifier, double heatModifier) {
        super(id, new ItemStackIngredient[] {input}, output, timeModifier, powerModifier, heatModifier);
        ID = Processors.DECAY_HASTENER;
        RECIPE_CLASSES.put(ID, this.getClass());
        CATALYSTS.put(ID, List.of(getToastSymbol()));
    }
}