package igentuman.nc.datagen.recipes.recipes;

import igentuman.nc.content.fuel.FuelManager;
import igentuman.nc.content.materials.Materials;
import igentuman.nc.content.processors.Processors;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.NcIngredient;
import igentuman.nc.setup.registration.FissionFuel;
import net.minecraft.data.recipes.RecipeOutput;

import java.util.ArrayList;
import java.util.List;

import static igentuman.nc.setup.registration.FissionFuel.NC_FUEL;
import static igentuman.nc.setup.registration.FissionFuel.NC_ISOTOPES;
import static igentuman.nc.setup.registration.NCItems.ALL_NC_ITEMS;
import static igentuman.nc.multiblock.accelerator.AcceleratorRegistration.ACCELERATOR_BLOCKS;
import static net.minecraft.world.item.Items.ICE;


public class FluidInfuserRecipes extends AbstractRecipeProvider {

    public static void generate(RecipeOutput consumer) {
        FluidInfuserRecipes.consumer = consumer;
        ID = Processors.FLUID_INFUSER;


        for(String gas: List.of("oxygen", "nitrogen", Materials.zircaloy)) {
            String type = gas.substring(0, 2).replace("zi", "za");
            for (String name : Materials.isotopes()) {
                String key = name + "_"+type;
                if(!NC_ISOTOPES.containsKey(key)) {
                    continue;
                }
                add(
                        fluidIngredient(gas, 100),
                        ingredient(NC_ISOTOPES.get(name).get()),
                        ingredient(NC_ISOTOPES.get(key).get())
                );
            }

            for (String name : FuelManager.all().keySet()) {
                if(name.matches("xenorium.*|quantite.*")) continue;
                for (String subType : FuelManager.all().get(name).keySet()) {
                    List<String> key = List.of("fuel", name, subType, "");
                    List<String> keyResult = List.of("fuel", name, subType, type);
                    add(
                            fluidIngredient(gas, 1000),
                            ingredient(NC_FUEL.get(key).get()),
                            ingredient(NC_FUEL.get(keyResult).get())
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
                dustIngredient(Materials.carbon_manganese)
        );

        add(
                fluidIngredient("oxygen", 1000),
                ingotIngredient(Materials.manganese_oxide),
                ingotStack(Materials.manganese_dioxide)
        );

        add(
                fluidIngredient("oxygen", 1000),
                dustIngredient(Materials.manganese_oxide),
                dustIngredient(Materials.manganese_dioxide)
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
                fluidIngredient("cryotheum", 1000),
                blockStack("empty_active_heat_sink"),
                blockStack("active_cryotheum_heat_sink")
        );

        add(
                fluidIngredient("liquid_helium", 1000),
                blockStack("empty_active_heat_sink"),
                blockStack("active_liquid_helium_heat_sink")
        );

        add(
                fluidIngredient("liquid_nitrogen", 1000),
                blockStack("empty_active_heat_sink"),
                blockStack("active_liquid_nitrogen_heat_sink")
        );

        add(
                fluidIngredient("minecraft:water", 1000),
                blockStack("empty_active_heat_sink"),
                blockStack("active_water_heat_sink")
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

        // Accelerator cooler recipes
        add(
                fluidIngredient("minecraft:water", 1000),
                ingredient(ACCELERATOR_BLOCKS.get("empty_cooler").get()),
                ingredient(ACCELERATOR_BLOCKS.get("water_cooler").get())
        );

        add(
                fluidIngredient("liquid_helium", 1000),
                ingredient(ACCELERATOR_BLOCKS.get("empty_cooler").get()),
                ingredient(ACCELERATOR_BLOCKS.get("liquid_helium_cooler").get())
        );

        add(
                fluidIngredient("liquid_nitrogen", 1000),
                ingredient(ACCELERATOR_BLOCKS.get("empty_cooler").get()),
                ingredient(ACCELERATOR_BLOCKS.get("liquid_nitrogen_cooler").get())
        );

        add(
                fluidIngredient("cryotheum", 1000),
                ingredient(ACCELERATOR_BLOCKS.get("empty_cooler").get()),
                ingredient(ACCELERATOR_BLOCKS.get("cryotheum_cooler").get())
        );

        add(
                fluidIngredient("enderium", 576), // 4 ingots worth (144 * 4 = 576)
                ingredient(ACCELERATOR_BLOCKS.get("empty_cooler").get()),
                ingredient(ACCELERATOR_BLOCKS.get("enderium_cooler").get())
        );

    }

    protected static void add(FluidStackIngredient inputFluid, NcIngredient inputItem, NcIngredient output, double...modifiers) {
        itemsAndFluids(List.of(inputItem), List.of(output), List.of(inputFluid), new ArrayList<>(), modifiers);
    }
}
