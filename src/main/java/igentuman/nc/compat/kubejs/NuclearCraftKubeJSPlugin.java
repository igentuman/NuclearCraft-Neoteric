package igentuman.nc.compat.kubejs;

import com.mojang.datafixers.util.Either;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.fluid.InputFluid;
import dev.latvian.mods.kubejs.fluid.OutputFluid;
import dev.latvian.mods.kubejs.item.InputItem;
import dev.latvian.mods.kubejs.item.OutputItem;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.FluidComponents;
import dev.latvian.mods.kubejs.recipe.component.ItemComponents;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import dev.latvian.mods.kubejs.recipe.schema.RegisterRecipeSchemasEvent;
import igentuman.nc.recipes.NcRecipeType;

import static igentuman.nc.NuclearCraft.MODID;

public class NuclearCraftKubeJSPlugin extends KubeJSPlugin {
    RecipeKey<Either<OutputFluid, OutputItem>[]> RESULTS = FluidComponents.OUTPUT_OR_ITEM_ARRAY.key("output").alt("output", "outputFluids");
    RecipeKey<Either<InputFluid, InputItem>[]> INGREDIENTS = FluidComponents.INPUT_OR_ITEM_ARRAY.key("input").alt("input", "inputFluids");
    RecipeSchema SCHEMA = new RecipeSchema(NCRecipeJS.class, NCRecipeJS::new, RESULTS, INGREDIENTS);

    @Override
    public void registerRecipeSchemas(RegisterRecipeSchemasEvent event) {
        for(String recipeType: NcRecipeType.ALL_RECIPES.keySet()) {
            event.namespace(MODID).register(recipeType, SCHEMA);
        }
    }
}
