package igentuman.nc.compat.jei;

import igentuman.nc.NuclearCraft;
import igentuman.nc.block.entity.fission.FissionControllerBE;
import igentuman.nc.block.entity.fusion.FusionCoreBE;
import igentuman.nc.block.entity.turbine.TurbineControllerBE;
import igentuman.nc.client.NcClient;
import igentuman.nc.client.gui.fission.FissionControllerScreen;
import igentuman.nc.client.gui.processor.NCProcessorScreen;
import igentuman.nc.content.processors.Processors;
import igentuman.nc.recipes.AbstractRecipe;
import igentuman.nc.recipes.NcRecipeType;
import igentuman.nc.recipes.type.MekChemicalConversionRecipe;
import igentuman.nc.recipes.type.NcRecipe;
import igentuman.nc.recipes.type.OreVeinRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.compat.GlobalVars.*;
import static igentuman.nc.util.ModUtil.isMekanismLoadeed;

@JeiPlugin
public  class JEIPlugin implements IModPlugin {
    public static HashMap<String, RecipeType<? extends NcRecipe>> recipeTypes;

    public static final RecipeType<FissionControllerBE.Recipe> FISSION = new RecipeType<>(new ResourceLocation(MODID, FissionControllerBE.NAME), FissionControllerBE.Recipe.class);
    public static final RecipeType<FusionCoreBE.Recipe> FUSION = new RecipeType<>(new ResourceLocation(MODID, "fusion_core"), FusionCoreBE.Recipe.class);
    public static final RecipeType<FusionCoreBE.FusionCoolantRecipe> FUSION_COOLANT = new RecipeType<>(new ResourceLocation(MODID, "fusion_coolant"), FusionCoreBE.FusionCoolantRecipe.class);
    public static final RecipeType<FissionControllerBE.FissionBoilingRecipe> FISSION_BOILING = new RecipeType<>(new ResourceLocation(MODID, "fission_boiling"), FissionControllerBE.FissionBoilingRecipe.class);
    public static final RecipeType<TurbineControllerBE.Recipe> TURBINE_CONTROLLER = new RecipeType<>(new ResourceLocation(MODID, TurbineControllerBE.NAME), TurbineControllerBE.Recipe.class);
    public static final RecipeType<MekChemicalConversionRecipe> CHEMICAL_TO_FLUID = new RecipeType<>(new ResourceLocation(MODID, "mek_chemical_to_fluid"), MekChemicalConversionRecipe.class);;
    public static final RecipeType<OreVeinRecipe> ORE_VEINS = new RecipeType<>(new ResourceLocation(MODID, "nc_ore_veins"), OreVeinRecipe.class);

    private static HashMap<String, RecipeType<? extends NcRecipe>> getRecipeTypes() {
        if (recipeTypes == null) {
            recipeTypes = new HashMap<>();
            for (String name : RECIPE_CLASSES.keySet()) {
                recipeTypes.put(name, new RecipeType<>(new ResourceLocation(MODID, name), RECIPE_CLASSES.get(name)));
            }
        }
        return recipeTypes;
    }


    public ResourceLocation getPluginUid() {
        return new ResourceLocation(MODID, "jei_plugin");
    }

    public void registerCategories(IRecipeCategoryRegistration registration) {
        for (String name : getRecipeTypes().keySet()) {
            if (Processors.registered().get(name) == null) continue;
            registration.addRecipeCategories(new ProcessorCategoryWrapper<>(registration.getJeiHelpers().getGuiHelper(), getRecipeType(name)));
        }
        registration.addRecipeCategories(new OreVeinCategoryWrapper<>(registration.getJeiHelpers().getGuiHelper(), ORE_VEINS));
        registration.addRecipeCategories(new FusionCategoryWrapper<>(registration.getJeiHelpers().getGuiHelper(), FUSION));
        registration.addRecipeCategories(new FusionCoolantCategoryWrapper<>(registration.getJeiHelpers().getGuiHelper(), FUSION_COOLANT));
        registration.addRecipeCategories(new FissionBoilingCategoryWrapper<>(registration.getJeiHelpers().getGuiHelper(), FISSION_BOILING));
        registration.addRecipeCategories(new TurbineControllerCategoryWrapper<>(registration.getJeiHelpers().getGuiHelper(), TURBINE_CONTROLLER));
        registration.addRecipeCategories(new FissionCategoryWrapper<>(registration.getJeiHelpers().getGuiHelper(), FISSION));
        if(isMekanismLoadeed()) {
            registration.addRecipeCategories(new MekChemicalConversionCategoryWrapper<>(registration.getJeiHelpers().getGuiHelper(), CHEMICAL_TO_FLUID));

        }
    }

    public <TYPE> RecipeType<TYPE> getRecipeType(String name) {
        return (RecipeType<TYPE>) recipeTypes.get(name);
    }

    public <TYPE> RecipeType<TYPE> getRecipeType(RecipeType<? extends AbstractRecipe> in) {
        return (RecipeType<TYPE>) in;
    }


    public void registerRecipes(IRecipeRegistration registration) {
        try {
            for (String name : getRecipeTypes().keySet()) {
                registration.addRecipes(
                        getRecipeType(name),
                        NcRecipeType.ALL_RECIPES.get(name).getRecipes(NcClient.tryGetClientWorld()));
            }
            registration.addRecipes(
                    getRecipeType(FUSION),
                    NcRecipeType.ALL_RECIPES.get("fusion_core").getRecipes(NcClient.tryGetClientWorld()));
            registration.addRecipes(
                    getRecipeType(FUSION_COOLANT),
                    NcRecipeType.ALL_RECIPES.get("fusion_coolant").getRecipes(NcClient.tryGetClientWorld()));
            registration.addRecipes(
                    getRecipeType(FISSION),
                    NcRecipeType.ALL_RECIPES.get(FissionControllerBE.NAME).getRecipes(NcClient.tryGetClientWorld()));
            registration.addRecipes(
                    getRecipeType(FISSION_BOILING),
                    NcRecipeType.ALL_RECIPES.get("fission_boiling").getRecipes(NcClient.tryGetClientWorld()));
            registration.addRecipes(
                    getRecipeType(TURBINE_CONTROLLER),
                    NcRecipeType.ALL_RECIPES.get(TurbineControllerBE.NAME).getRecipes(NcClient.tryGetClientWorld()));
            registration.addRecipes(
                    getRecipeType(ORE_VEINS),
                    NcRecipeType.ALL_RECIPES.get("nc_ore_veins").getRecipes(NcClient.tryGetClientWorld()));
            registration.addRecipes(getRecipeType(CHEMICAL_TO_FLUID), MekChemicalConversionRecipe.getRecipes());
        } catch (IllegalArgumentException ex) {
            NuclearCraft.LOGGER.error("Error registering recipes for JEI: " + ex.getMessage());
        }
    }

    private <T extends AbstractContainerScreen<?>> void addRecipeClickArea(IGuiHandlerRegistration registration, Class<? extends T> containerScreenClass, int xPos, int yPos, int width, int height, RecipeType<?>... recipeTypes) {
        registration.addGuiContainerHandler(containerScreenClass, new IGuiContainerHandler<T>() {
            @Override
            public Collection<IGuiClickableArea> getGuiClickableAreas(T containerScreen, double mouseX, double mouseY) {
                NCProcessorScreen<?> screen = (NCProcessorScreen<?>) containerScreen;
                String name = screen.getRecipeTypeName();
                IGuiClickableArea clickableArea = IGuiClickableArea.createBasic(xPos, yPos, width, height, getRecipeTypes().get(name));
                return List.of(clickableArea);
            }
        });
    }

    public  void registerGuiHandlers(IGuiHandlerRegistration registration) {
        for (String name : getRecipeTypes().keySet()) {
            if (!Processors.registered().containsKey(name)) continue;
            addRecipeClickArea(registration, NCProcessorScreen.class, 67, 74, 18, 18, getRecipeType(name));
        }
        registration.addRecipeClickArea(FissionControllerScreen.class,65, 42, 36, 26, FISSION);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registry) {
        for(String  name: getRecipeTypes().keySet()) {
            if(!CATALYSTS.containsKey(name)) continue;
            for(ItemStack stack: CATALYSTS.get(name)) {
                registry.addRecipeCatalyst(stack, getRecipeType(name));
            }
        }

        if(CATALYSTS.containsKey(FissionControllerBE.NAME)) {
            registry.addRecipeCatalyst(CATALYSTS.get(FissionControllerBE.NAME).get(0), FISSION);
        }

        if(CATALYSTS.containsKey("nc_ore_veins")) {
            registry.addRecipeCatalyst(CATALYSTS.get("nc_ore_veins").get(0), ORE_VEINS);
        }
    }
}
