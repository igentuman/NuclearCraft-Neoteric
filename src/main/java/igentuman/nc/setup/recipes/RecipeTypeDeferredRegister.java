package igentuman.nc.setup.recipes;

import igentuman.nc.recipes.INcRecipeTypeProvider;
import igentuman.nc.recipes.NcRecipe;
import igentuman.nc.recipes.NcRecipeType;
import igentuman.nc.recipes.cache.IInputRecipeCache;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class RecipeTypeDeferredRegister extends WrappedDeferredRegister<RecipeType<?>> {

    private final List<INcRecipeTypeProvider<?, ?>> recipeTypes = new ArrayList<>();

    public RecipeTypeDeferredRegister(String modid) {
        super(modid, ForgeRegistries.RECIPE_TYPES);
    }

    public <RECIPE extends NcRecipe, INPUT_CACHE extends IInputRecipeCache> RecipeTypeRegistryObject<RECIPE, INPUT_CACHE> register(String name,
                                                                                                                                   Supplier<? extends NcRecipeType<RECIPE, INPUT_CACHE>> sup) {
        RecipeTypeRegistryObject<RECIPE, INPUT_CACHE> registeredRecipeType = register(name, sup, RecipeTypeRegistryObject::new);
        recipeTypes.add(registeredRecipeType);
        return registeredRecipeType;
    }

    public List<INcRecipeTypeProvider<?, ?>> getAllRecipeTypes() {
        return Collections.unmodifiableList(recipeTypes);
    }
}