package igentuman.nc.datagen.recipes;

import igentuman.nc.setup.registration.FissionFuel;
import igentuman.nc.content.materials.Materials;
import igentuman.nc.content.fuel.FuelManager;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;
import java.util.function.Consumer;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.datagen.recipes.recipes.AbstractRecipeProvider.getIsotope;

public class FuelRecipes extends NCRecipes {


    public FuelRecipes(DataGenerator generatorIn) {
        super(generatorIn);
    }

    public static void generate(Consumer<FinishedRecipe> consumer) {

        for (String name: Materials.isotopes()) {
            for(String type: new String[] {"_ox", "_ni", "_za"}) {
                String key = name+type;
                SimpleCookingRecipeBuilder.smelting(Ingredient.of(FissionFuel.NC_ISOTOPES.get(key).get()),
                                RecipeCategory.MISC,
                                FissionFuel.NC_ISOTOPES.get(name).get(), 1.0f, 100)
                        .unlockedBy("item", inventoryTrigger(ItemPredicate.Builder.item().of(FissionFuel.NC_ISOTOPES.get(name).get()).build()))
                        .save(consumer, MODID+"_"+name+type+"_sml");
            }
        }

        for (String name: FuelManager.all().keySet()) {
            for(String subType: FuelManager.all().get(name).keySet()) {
                for (String type : new String[]{"ox", "ni", "za"}) {
                    List<String> key = List.of("fuel", name, subType, type);
                    SimpleCookingRecipeBuilder.smelting(Ingredient.of(FissionFuel.NC_FUEL.get(key).get()),
                                    RecipeCategory.MISC,
                                    FissionFuel.NC_FUEL.get(List.of("fuel", name, subType, "")).get(), 1.0f, 100)
                            .unlockedBy("item", inventoryTrigger(ItemPredicate.Builder.item().of( FissionFuel.NC_FUEL.get(List.of("fuel", name, subType, "")).get()).build()))
                            .save(consumer, MODID + "_fuel_" + name+subType + type + "_sml");

                    key = List.of("depleted", name, subType, type);
                    SimpleCookingRecipeBuilder.smelting(Ingredient.of(FissionFuel.NC_DEPLETED_FUEL.get(key).get()),
                                    RecipeCategory.MISC,
                                    FissionFuel.NC_DEPLETED_FUEL.get(List.of("depleted", name, subType, "")).get(), 1.0f, 100)
                            .unlockedBy("item", inventoryTrigger(ItemPredicate.Builder.item().of( FissionFuel.NC_DEPLETED_FUEL.get(List.of("depleted", name, subType, "")).get()).build()))
                            .save(consumer, MODID + "_depleted_" + name+subType + type + "_sml");
                }

                fuelPelletRecipe(consumer, name, subType, "",
                        FuelManager.all().get(name).get(subType).getDefault().isotopes[0],
                        FuelManager.all().get(name).get(subType).getDefault().isotopes[1]);

                fuelPelletRecipe(consumer, name, subType, "ox",
                        FuelManager.all().get(name).get(subType).getOxide().isotopes[0],
                        FuelManager.all().get(name).get(subType).getOxide().isotopes[1]);

                fuelPelletRecipe(consumer, name, subType, "ni",
                        FuelManager.all().get(name).get(subType).getNitride().isotopes[0],
                        FuelManager.all().get(name).get(subType).getNitride().isotopes[1]);

                fuelPelletRecipe(consumer, name, subType, "za",
                        FuelManager.all().get(name).get(subType).getZirconiumAlloy().isotopes[0],
                        FuelManager.all().get(name).get(subType).getZirconiumAlloy().isotopes[1]);
            }
        }


    }

    private static void fuelPelletRecipe(Consumer<FinishedRecipe> consumer, String name, String subType, String type, int isotope1, int isotope2)
    {
        int isotope1Cnt = 1;
        int isotope2Cnt = 8;
        if(subType.substring(0,1).equalsIgnoreCase("h")) {
            isotope1Cnt = 3;
            isotope2Cnt = 6;
        }
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, FissionFuel.NC_FUEL.get(List.of("fuel", name, subType, type)).get(), 3)
                .group(MODID+"_ingots")
                .requires(getIsotope(name, String.valueOf(isotope1), type), isotope1Cnt)
                .requires(getIsotope(name, String.valueOf(isotope2), type), isotope2Cnt)
                .unlockedBy("has_ore", inventoryTrigger(ItemPredicate.Builder.item().of( FissionFuel.NC_FUEL.get(List.of("fuel", name, subType, type)).get()).build()))
                .save(consumer, MODID + "_fuel_" + name+subType + type+"_cr");
    }


}
