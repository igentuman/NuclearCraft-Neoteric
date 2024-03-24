package igentuman.nc.datagen.recipes.recipes;

import igentuman.nc.datagen.recipes.builder.NcRecipeBuilder;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.NcIngredient;
import igentuman.nc.content.processors.Processors;
import igentuman.nc.setup.registration.Fuel;
import igentuman.nc.content.materials.Materials;
import igentuman.nc.setup.registration.NCItems;
import igentuman.nc.content.fuel.FuelManager;
import igentuman.nc.content.materials.NCMaterial;
import net.minecraft.data.recipes.FinishedRecipe;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static net.minecraft.world.item.Items.*;

public class MelterRecipes extends AbstractRecipeProvider {

    public static void generate(Consumer<FinishedRecipe> consumer) {
        MelterRecipes.consumer = consumer;
        ID = Processors.MELTER;
        for(String name: Materials.all().keySet()) {
            NCMaterial material = Materials.all().get(name);
            if(material.fluid && !material.isGas) {
                add(dustIngredient(name), fluidIngredient(name, 144));
                add(ingotIngredient(name), fluidIngredient(name, 144));
                add(oreIngredient(name), fluidIngredient(name, 288));
                add(chunkIngredient(name), fluidIngredient(name, 216));
            }
        }

        add(dustIngredient(Materials.polonium), fluidIngredient(Materials.polonium, 144));
        add(dustIngredient(Materials.sulfur), fluidIngredient(Materials.sulfur, 144));
        add(ingredient(REDSTONE), fluidIngredient("redstone", 144));
        add(ingredient(GLOWSTONE_DUST), fluidIngredient("glowstone", 144));


        for (String name: Materials.isotopes()) {
            for(String type: new String[] {"", "_ox", "_ni", "_za"}) {
                String key = name+type;
                add(ingredient(Fuel.NC_ISOTOPES.get(key).get()), fluidIngredient(key, 144));
            }
        }

        for (String name: FuelManager.all().keySet()) {
            for(String subType: FuelManager.all().get(name).keySet()) {
                for (String type : new String[]{"", "za", "ox","ni"}) {

                    List<String> key = List.of("fuel", name, subType, type);

                    String keyStr = "fuel_"+name +"_"+ subType;
                    if(!type.isEmpty()){
                        keyStr += "_";
                    }
                    keyStr+= type;
                    add(ingredient(Fuel.NC_FUEL.get(key).get()), fluidStackIngredient(keyStr, 144));

                    key = List.of("depleted", name, subType, type);
                    keyStr = "depleted_"+keyStr;
                    add(ingredient(Fuel.NC_DEPLETED_FUEL.get(key).get()), fluidStackIngredient(keyStr, 144));
                }
            }
        }

        add(dustIngredient(Materials.sulfur), fluidIngredient(Materials.sulfur, 144), 0.5D, 3D);
        add(dustIngredient(Materials.sodium_hydroxide), fluidIngredient(Materials.sodium_hydroxide, 144));
        add(dustIngredient(Materials.potassium_hydroxide), fluidIngredient(Materials.potassium_hydroxide, 144));
        add(dustIngredient(Materials.arsenic), fluidIngredient(Materials.arsenic, 144));
        add(gemIngredient(Materials.boron_arsenide), fluidIngredient(Materials.boron_arsenide, 144));
        add(ingredient(OBSIDIAN), fluidIngredient(Materials.obsidian, 288), 2D, 2D);

        add(ingredient(NCItems.NC_ITEMS.get("ground_cocoa_nibs").get()), fluidIngredient("chocolate_liquor", 144), 0.25D, 0.5D);
        add(ingredient(NCItems.NC_ITEMS.get("cocoa_butter").get()), fluidIngredient("cocoa_butter", 144), 0.25D, 0.5D);
        add(ingredient(NCItems.NC_ITEMS.get("unsweetened_chocolate").get()), fluidIngredient("unsweetened_chocolate", 144), 0.25D, 0.5D);
        add(ingredient(NCItems.NC_FOOD.get("dark_chocolate").get()), fluidIngredient("dark_chocolate", 144), 0.25D, 0.5D);
        add(ingredient(NCItems.NC_FOOD.get("milk_chocolate").get()), fluidIngredient("milk_chocolate", 144), 0.25D, 0.5D);
        add(ingredient(SUGAR), fluidIngredient("sugar", 144), 0.5D, 0.5D);
        add(ingredient(NCItems.NC_ITEMS.get("gelatin").get()), fluidIngredient("gelatin", 144), 0.5D, 0.5D);
        add(ingredient(NCItems.NC_FOOD.get("marshmallow").get()), fluidIngredient("marshmallow", 144), 0.5D, 0.5D);

    }

    protected static void add(NcIngredient inputItem, FluidStackIngredient outputFluid, double...modifiers) {
        try {
            itemToFluid(List.of(inputItem), new ArrayList<>(), new ArrayList<>(), List.of(outputFluid), modifiers);
        } catch(IllegalStateException ignored) {}
    }

    private static void itemToFluid(
            List<NcIngredient> inputItems, List<NcIngredient> outputItems,
            List<FluidStackIngredient> inputFluids, List<FluidStackIngredient> outputFluids,
                                       double...params) {
        double timeModifier = params.length>0 ? params[0] : 1.0;
        double powerModifier = params.length>1 ? params[1] : 1.0;
        double radiation = params.length>2 ? params[2] : 1.0;
        NcRecipeBuilder.get(ID)
                .items(inputItems, outputItems)
                .fluids(inputFluids, outputFluids)
                .modifiers(timeModifier, radiation, powerModifier)
                .build(consumer);
    }

}
