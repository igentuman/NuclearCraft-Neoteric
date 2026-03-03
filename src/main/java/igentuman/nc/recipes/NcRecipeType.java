package igentuman.nc.recipes;

import igentuman.api.platform.NCRecipes;
import igentuman.nc.block.fission.entity.FissionControllerBE;
import igentuman.nc.block.entity.processor.NuclearFurnaceBE;
import igentuman.nc.block.fission.entity.MSRControllerBE;
import igentuman.nc.block.turbine.entity.TurbineControllerBE;
import igentuman.nc.client.NcClient;
import igentuman.nc.content.processors.Processors;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import igentuman.nc.recipes.ingredient.creator.IngredientCreatorAccess;
import igentuman.nc.recipes.type.NcRecipe;
import igentuman.nc.registry.RecipeTypeDeferredRegister;
import igentuman.nc.registry.RecipeTypeRegistryObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import net.neoforged.api.distmarker.Dist;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.*;
import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.NuclearCraft.rl;

@SuppressWarnings("unchecked")
public class NcRecipeType<RECIPE extends NcRecipe> implements RecipeType<RECIPE>,
        INcRecipeTypeProvider<RECIPE> {

    public static final RecipeTypeDeferredRegister RECIPE_TYPES = new RecipeTypeDeferredRegister(MODID);
    public static boolean initialized = false;
    public static final HashMap<String, RecipeTypeRegistryObject<? extends NcRecipe>> ALL_RECIPES = initializeRecipes();

    private static HashMap<String, RecipeTypeRegistryObject<? extends NcRecipe>> initializeRecipes() {
        HashMap<String, RecipeTypeRegistryObject<? extends NcRecipe>> recipes = new HashMap<>();
        recipes.put(FissionControllerBE.NAME, register(FissionControllerBE.NAME));
        recipes.put(MSRControllerBE.NAME, register(MSRControllerBE.NAME));
        recipes.put("nc_ore_veins", register("nc_ore_veins"));
        recipes.put("fusion_core", register("fusion_core"));
        recipes.put("fusion_coolant", register("fusion_coolant"));
        recipes.put("accelerator_coolant", register("accelerator_coolant"));
        recipes.put("fission_boiling", register("fission_boiling"));
        recipes.put("kugelblitz_chamber", register("kugelblitz_chamber"));
        recipes.put("target_chamber", register("target_chamber"));
        recipes.put(TurbineControllerBE.NAME, register(TurbineControllerBE.NAME));

        for(String processorName: Processors.all().keySet()) {
            if(Processors.all().get(processorName).hasRecipes()) {
                recipes.put(processorName, register(processorName));
            }
        }
        initialized = true;
        return recipes;
    }

    public static <RECIPE extends NcRecipe> RecipeTypeRegistryObject<RECIPE> register(String name) {
        return RECIPE_TYPES.register(name, () -> new NcRecipeType<>(name));
    }
    private List<RECIPE> cachedRecipes = Collections.emptyList();
    private final ResourceLocation registryName;

    private NcRecipeType(String name) {
        this.registryName = rl(name);
    }

    public static void invalidateCache() {
        if(!initialized) return;
        for (RecipeTypeRegistryObject<? extends NcRecipe> recipeType : ALL_RECIPES.values()) {
            recipeType.getRecipeType().cachedRecipes = Collections.emptyList();
        }
    }

    public static void init() {
    }

    public static List<? extends NcRecipe> getAllRecipesFor(String name, Level level) {
        return ALL_RECIPES.get(name).getRecipeType().getRecipes(level);
    }

    @Override
    public String toString() {
        return registryName.toString();
    }

    @Override
    public ResourceLocation getRegistryName() {
        return registryName;
    }

    @Override
    public NcRecipeType<RECIPE> getRecipeType() {
        return this;
    }


    @NotNull
    @Override
    public List<RECIPE> getRecipes(@Nullable Level world) {
        if(Processors.all().containsKey(registryName.getPath()) && !Processors.all().get(registryName.getPath()).config().isRegistered()) {
            return Collections.emptyList();
        }
        if (world == null) {
            world = FMLEnvironment.dist == Dist.CLIENT ? NcClient.tryGetClientWorld() : ServerLifecycleHooks.getCurrentServer().overworld();
            if (world == null) {
                return cachedRecipes;
            }
        }
        if (cachedRecipes.isEmpty()) {
            RecipeManager recipeManager = world.getRecipeManager();
            List<RECIPE> recipes;
            if(this.registryName.getPath().equals("nuclear_furnace")) {
                recipes = getSmeltingRecipes(recipeManager, world);
            } else {
                recipes = NCRecipes.getAllRecipesFor(recipeManager, this);
            }
            cachedRecipes = recipes.stream()
                    .filter(recipe -> !recipe.isIncomplete())
                    .toList();
        }
        return cachedRecipes;
    }

    private List<RECIPE> getSmeltingRecipes(RecipeManager recipeManager, Level level) {
        List<RecipeHolder<SmeltingRecipe>> smelting = NCRecipes.getVanillaRecipes(recipeManager, RecipeType.SMELTING);
        List<RECIPE> recipes = new ArrayList<>();
        for(RecipeHolder<SmeltingRecipe> holder: smelting) {
            SmeltingRecipe recipe = holder.value();
            if(recipe.isIncomplete()) {
                continue;
            }
            ItemStack result = recipe.getResultItem(level.registryAccess());
            Ingredient input = recipe.getIngredients().get(0);
            if (result.isEmpty() || input.isEmpty()) {
                continue;
            }
            ItemStackIngredient output = IngredientCreatorAccess.item().from(result);
            NuclearFurnaceBE.Recipe nfRecipe = new NuclearFurnaceBE.Recipe(
                    "nuclear_furnace",
                    new ItemStackIngredient[]{IngredientCreatorAccess.item().from(input)},
                    new ItemStackIngredient[]{output},
                    new FluidStackIngredient[0],
                    new FluidStackIngredient[0],
                    recipe.getCookingTime()/2000D, 1, 1, 1);
            nfRecipe.setId(rl(getNFRecipeId(holder)));
            recipes.add((RECIPE) nfRecipe);
        }
        return recipes;
    }

    private String getNFRecipeId(RecipeHolder<SmeltingRecipe> holder) {
        return holder.id().toString().replaceAll("[^a-z0-9/._-]", "_") + "_nf";
    }

    /**
     * Helper for getting a recipe from a world's recipe manager.
     */
    public static <I extends RecipeInput, RECIPE_TYPE extends Recipe<I>> Optional<RECIPE_TYPE> getRecipeFor(RecipeType<RECIPE_TYPE> recipeType, I input, Level level) {
        return level.getRecipeManager().getRecipeFor(recipeType, input, level)
              .map(RecipeHolder::value)
              .filter(recipe -> !recipe.isIncomplete());
    }

    /**
     * Helper for getting a recipe from a world's recipe manager.
     */
    public static Optional<? extends Recipe<?>> byKey(Level level, ResourceLocation id) {
        return NCRecipes.byKey(level.getRecipeManager(), id)
              .map(RecipeHolder::value)
              .filter(recipe -> !recipe.isIncomplete());
    }
    public boolean isLoaded = false;
    public void loadRecipes(Level level) {
        if(isLoaded) return;
        getRecipes(level);
        isLoaded = true;
    }

    public void loadRecipes(RecipeManager manager) {
        getRecipes(manager);
        isLoaded = true;
    }

    private void getRecipes(RecipeManager manager) {
        List<RECIPE> recipes = NCRecipes.getAllRecipesFor(manager, this);
        cachedRecipes = recipes.stream()
                .filter(recipe -> !recipe.isIncomplete())
                .toList();
    }
}
