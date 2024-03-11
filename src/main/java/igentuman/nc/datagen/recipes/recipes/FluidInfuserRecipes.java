package igentuman.nc.datagen.recipes.recipes;

import igentuman.nc.content.fuel.FuelManager;
import igentuman.nc.content.materials.Materials;
import igentuman.nc.content.processors.Processors;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.NcIngredient;
import igentuman.nc.setup.registration.Fuel;
import net.minecraft.data.IFinishedRecipe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static igentuman.nc.setup.registration.NCItems.ALL_NC_ITEMS;
import static net.minecraft.item.Items.ICE;


public class FluidInfuserRecipes extends AbstractRecipeProvider {

    public static void generate(Consumer<IFinishedRecipe> consumer) {
        FluidInfuserRecipes.consumer = consumer;
        ID = Processors.FLUID_INFUSER;


        for(String gas: Arrays.asList("oxygen", "nitrogen", Materials.zircaloy)) {
            String type = gas.substring(0, 2).replace("zi", "za");
            for (String name : Materials.isotopes()) {
                String key = name + "_"+type;
                add(
                        fluidIngredient(gas, 100),
                        ingredient(Fuel.NC_ISOTOPES.get(name).get()),
                        ingredient(Fuel.NC_ISOTOPES.get(key).get())
                );
            }

            for (String name : FuelManager.all().keySet()) {
                for (String subType : FuelManager.all().get(name).keySet()) {
                    List<String> key = Arrays.asList("fuel", name, subType, "");
                    List<String> keyResult = Arrays.asList("fuel", name, subType, type);
                    add(
                            fluidIngredient(gas, 1000),
                            ingredient(Fuel.NC_FUEL.get(key).get()),
                            ingredient(Fuel.NC_FUEL.get(keyResult).get())
                    );
                }
            }
        }

        add(
                fluidIngredient("cryotheum", 1000),
                blockStack("empty_heat_sink"),
                blockStack("cryotheum_heat_sink")
        );

        add(
                fluidIngredient("carbon", 1000),
                dustIngredient(Materials.manganese),
                dustStack(Materials.carbon_manganese)
        );

        add(
                fluidIngredient("oxygen", 1000),
                ingotIngredient(Materials.manganese),
                ingotStack(Materials.manganese_dioxide)
        );

        add(
                fluidIngredient("oxygen", 1000),
                dustIngredient(Materials.manganese),
                dustStack(Materials.manganese_dioxide)
        );

        add(
                fluidIngredient("liquid_helium", 50),
                ingredient(ICE),
                blockStack(Materials.supercold_ice)
        );

        add(
                fluidIngredient("liquid_helium", 1000),
                blockStack("empty_heat_sink"),
                blockStack("liquid_helium_heat_sink")
        );

        add(
                fluidIngredient("liquid_nitrogen", 1000),
                blockStack("empty_heat_sink"),
                blockStack("liquid_nitrogen_heat_sink")
        );

        add(
                fluidIngredient("minecraft:water", 1000),
                blockStack("empty_heat_sink"),
                blockStack("water_heat_sink")
        );

        add(
                fluidIngredient("radaway", 250),
                ingredient(ALL_NC_ITEMS.get("bioplastic").get(), 2),
                ingredient(ALL_NC_ITEMS.get("radaway").get())
        );

        add(
                fluidIngredient("radaway_slow", 250),
                ingredient(ALL_NC_ITEMS.get("bioplastic").get(), 2),
                ingredient(ALL_NC_ITEMS.get("radaway_slow").get())
        );

    }

    protected static void add(FluidStackIngredient inputFluid, NcIngredient inputItem, NcIngredient output, double...modifiers) {
        itemsAndFluids(Arrays.asList(inputItem), Arrays.asList(output), Arrays.asList(inputFluid), new ArrayList<>(), modifiers);
    }
}
