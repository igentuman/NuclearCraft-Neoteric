package igentuman.nc.setup.recipes;

import igentuman.nc.recipes.INcRecipeTypeProvider;
import igentuman.nc.recipes.NcRecipe;
import igentuman.nc.recipes.NcRecipeType;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class RecipeTypeDeferredRegister extends WrappedDeferredRegister<RecipeType<?>> {

    private final List<INcRecipeTypeProvider<?>> recipeTypes = new ArrayList<>();

    public RecipeTypeDeferredRegister(String modid) {
        super(modid, ForgeRegistries.RECIPE_TYPES);
    }

    public <RECIPE extends NcRecipe> RecipeTypeRegistryObject<RECIPE> register(String name,  Supplier<? extends NcRecipeType<RECIPE>> sup) {
        RecipeTypeRegistryObject<RECIPE> registeredRecipeType = register(name, sup, RecipeTypeRegistryObject::new);
        recipeTypes.add(registeredRecipeType);
        return registeredRecipeType;
    }

}