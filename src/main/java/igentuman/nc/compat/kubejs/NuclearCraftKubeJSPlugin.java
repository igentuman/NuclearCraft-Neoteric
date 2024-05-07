package igentuman.nc.compat.kubejs;

import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.recipe.RegisterRecipeHandlersEvent;
import igentuman.nc.block.entity.fission.FissionControllerBE;
import igentuman.nc.block.entity.turbine.TurbineControllerBE;
import igentuman.nc.recipes.NcRecipeType;

public class NuclearCraftKubeJSPlugin extends KubeJSPlugin {

    @Override
    public void addRecipes(RegisterRecipeHandlersEvent event) {

        event.register(FissionControllerBE.NAME, NCRecipeJS::new);
        event.register(TurbineControllerBE.NAME, NCRecipeJS::new);
        event.register("nc_ore_veins", NCRecipeJS::new);
        event.register("fusion_core", NCRecipeJS::new);
        event.register("fusion_coolant", NCRecipeJS::new);
        event.register("fission_boiling", NCRecipeJS::new);
        event.register("reset_nbt", NCRecipeJS::new);
        event.register("shielding", NCRecipeJS::new);
        for (String recipeType : NcRecipeType.ALL_RECIPES.keySet()) {
            event.register(recipeType, NCRecipeJS::new);
        }
    }

}
