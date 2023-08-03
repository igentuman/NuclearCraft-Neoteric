package igentuman.nc.recipes.processors;

import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import igentuman.nc.recipes.type.ItemStackToItemStackRecipe;
import igentuman.nc.setup.processors.Processors;
import igentuman.nc.util.annotation.NothingNullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

import static igentuman.nc.compat.GlobalVars.CATALYSTS;
import static igentuman.nc.compat.GlobalVars.RECIPE_CLASSES;

@NothingNullByDefault
public class RockCrusherRecipe extends ItemStackToItemStackRecipe {
    public RockCrusherRecipe(ResourceLocation id, ItemStackIngredient input, ItemStack[] output, double timeModifier, double powerModifier, double heatModifier) {
        super(id, new ItemStackIngredient[] {input}, output, timeModifier, powerModifier, heatModifier);
        ID = Processors.ROCK_CRUSHER;
        RECIPE_CLASSES.put(ID, this.getClass());
        CATALYSTS.put(ID, List.of(getToastSymbol()));
    }
}