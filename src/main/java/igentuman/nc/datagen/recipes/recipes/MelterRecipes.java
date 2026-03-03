package igentuman.nc.datagen.recipes.recipes;

import igentuman.nc.datagen.recipes.builder.NcRecipeBuilder;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.NcIngredient;
import igentuman.nc.content.processors.Processors;
import igentuman.nc.setup.registration.FissionFuel;
import igentuman.nc.content.materials.Materials;
import igentuman.nc.setup.registration.NCItems;
import igentuman.nc.content.fuel.FuelManager;
import igentuman.nc.content.materials.NCMaterial;
import net.minecraft.data.recipes.RecipeOutput;

import java.util.ArrayList;
import java.util.List;

import static igentuman.nc.datagen.recipes.NCRecipes.MOLTEN_INGOT;
import static igentuman.nc.datagen.recipes.NCRecipes.MOLTEN_NUGGET;
import static igentuman.nc.setup.registration.FissionFuel.NC_ISOTOPES;
import static net.minecraft.world.item.Items.*;

public class MelterRecipes extends AbstractRecipeProvider {

    public static void generate(RecipeOutput consumer) {
        MelterRecipes.consumer = consumer;
        ID = Processors.MELTER;
        for(String name: Materials.all().keySet()) {
            NCMaterial material = Materials.all().get(name);
            if(material.fluid && !material.isGas) {
                add(dustIngredient(name), fluidIngredient("molten_"+name, MOLTEN_INGOT));
                add(ingotIngredient(name), fluidIngredient("molten_"+name, MOLTEN_INGOT));
                add(oreIngredient(name), fluidIngredient("molten_"+name, MOLTEN_INGOT*2+MOLTEN_NUGGET*6));
                add(chunkIngredient(name), fluidIngredient("molten_"+name, MOLTEN_INGOT+MOLTEN_NUGGET*3));
            }
        }

        add(dustIngredient(Materials.polonium), fluidIngredient(Materials.polonium, MOLTEN_INGOT));
        add(dustIngredient(Materials.sulfur), fluidIngredient(Materials.sulfur, MOLTEN_INGOT));
        add(ingredient(REDSTONE), fluidIngredient("redstone", MOLTEN_INGOT));
        add(ingredient(GLOWSTONE_DUST), fluidIngredient("glowstone", MOLTEN_INGOT));


        for (String name: Materials.isotopes()) {
            for(String type: new String[] {"", "_ox", "_ni", "_za"}) {
                String key = name+type;
                if(!NC_ISOTOPES.containsKey(key)) {
                    continue;
                }
                add(ingredient(NC_ISOTOPES.get(key).get()), fluidIngredient(key, MOLTEN_INGOT));
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
                    add(ingredient(FissionFuel.NC_FUEL.get(key).get()), fluidStackIngredient(keyStr, MOLTEN_INGOT));

                    key = List.of("depleted", name, subType, type);
                    keyStr = "depleted_"+keyStr;
                    add(ingredient(FissionFuel.NC_DEPLETED_FUEL.get(key).get()), fluidStackIngredient(keyStr, MOLTEN_INGOT));
                    if(name.matches("xenorium.*|quantite.*")) break;
                }
            }
        }

        add(dustIngredient(Materials.sulfur), fluidIngredient(Materials.sulfur, MOLTEN_INGOT), 0.5D, 3D);
        add(dustIngredient(Materials.sodium_hydroxide), fluidIngredient(Materials.sodium_hydroxide, MOLTEN_INGOT));
        add(dustIngredient(Materials.potassium_hydroxide), fluidIngredient(Materials.potassium_hydroxide, MOLTEN_INGOT));
        add(dustIngredient(Materials.arsenic), fluidIngredient(Materials.arsenic, MOLTEN_INGOT));
        add(gemIngredient(Materials.boron_arsenide), fluidIngredient(Materials.boron_arsenide, MOLTEN_INGOT));
        add(ingredient(OBSIDIAN), fluidIngredient(Materials.obsidian, MOLTEN_INGOT*2), 2D, 2D);

        add(ingredient(NCItems.NC_ITEMS.get("ground_cocoa_nibs").get()), fluidIngredient("chocolate_liquor", MOLTEN_INGOT), 0.25D, 0.5D);
        add(ingredient(NCItems.NC_ITEMS.get("cocoa_butter").get()), fluidIngredient("cocoa_butter", MOLTEN_INGOT), 0.25D, 0.5D);
        add(ingredient(NCItems.NC_ITEMS.get("unsweetened_chocolate").get()), fluidIngredient("unsweetened_chocolate", MOLTEN_INGOT), 0.25D, 0.5D);
        add(ingredient(NCItems.NC_FOOD.get("dark_chocolate").get()), fluidIngredient("dark_chocolate", MOLTEN_INGOT), 0.25D, 0.5D);
        add(ingredient(NCItems.NC_FOOD.get("milk_chocolate").get()), fluidIngredient("milk_chocolate", MOLTEN_INGOT), 0.25D, 0.5D);
        add(ingredient(SUGAR), fluidIngredient("sugar", MOLTEN_INGOT), 0.5D, 0.5D);
        add(ingredient(NCItems.NC_ITEMS.get("gelatin").get()), fluidIngredient("gelatin", MOLTEN_INGOT), 0.5D, 0.5D);
        add(ingredient(NCItems.NC_FOOD.get("marshmallow").get()), fluidIngredient("marshmallow", MOLTEN_INGOT), 0.5D, 0.5D);

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
