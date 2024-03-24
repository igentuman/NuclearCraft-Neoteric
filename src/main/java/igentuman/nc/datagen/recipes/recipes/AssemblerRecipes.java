package igentuman.nc.datagen.recipes.recipes;

import igentuman.nc.content.fuel.FuelManager;
import igentuman.nc.content.processors.Processors;
import igentuman.nc.content.materials.Materials;
import net.minecraft.data.recipes.FinishedRecipe;

import java.util.List;
import java.util.function.Consumer;

import static igentuman.nc.setup.registration.NCItems.*;
import static net.minecraft.world.item.Items.*;

public class AssemblerRecipes extends AbstractRecipeProvider {

    public static void generate(Consumer<FinishedRecipe> consumer) {
        AssemblerRecipes.consumer = consumer;
        ID = Processors.ASSEMBLER;

        for (String name: FuelManager.all().keySet()) {
            for(String subType: FuelManager.all().get(name).keySet()) {
                itemsToItems(
                        List.of(
                            ingredient(fuelItem(List.of("fuel", name, subType, "")), 9),
                            dustIngredient(Materials.graphite),
                            ingotIngredient(Materials.pyrolitic_carbon),
                            ingotIngredient(Materials.silicon_carbide)
                        ),
                        List.of(ingredient(fuelItem(List.of("fuel", name, subType, "tr")), 9))
                );
            }
        }

        itemsToItems(
                List.of(
                        dustIngredient(Materials.rhodochrosite),
                        dustIngredient(Materials.calcium_sulfate),
                        dustIngredient(Materials.magnesium),
                        dustIngredient(Materials.obsidian)
                ),
                List.of(ingredient(dustItem(Materials.crystal_binder), 2))
        );

        itemsToItems(
                List.of(
                        plateIngredient(Materials.tough_alloy, 5),
                        ingredient(NETHERITE_AXE),
                        ingredient(NETHERITE_PICKAXE),
                        ingredient(NETHERITE_SHOVEL)
                ),
                List.of(ingredient(SPAXELHOE_TOUGH.get()))
        );

        itemsToItems(
                List.of(
                        plateIngredient(Materials.steel, 3),
                        dustIngredient(Materials.lead),
                        ingredient(NC_PARTS.get("motor").get()),
                        ingredient(NC_PARTS.get("actuator").get())
                ),
                List.of(ingredient(MULTITOOL.get()))
        );

        itemsToItems(
                List.of(
                        plateIngredient(Materials.hsla_steel, 3),
                        ingredient(NC_PARTS.get("basic_electric_circuit").get()),
                        ingredient(LITHIUM_ION_CELL.get()),
                        ingredient(NC_PARTS.get("coil_magnesium_diboride").get()),
                        ingredient(NC_PARTS.get("actuator").get())
                ),
                List.of(ingredient(QNP.get()))
        );

        itemsToItems(
                List.of(
                        plateIngredient(Materials.electrum),
                        ingredient(NC_PARTS.get("bioplastic").get()),
                        dustIngredient(Materials.energetic_blend),
                        ingredient(REDSTONE),
                        ingredient(NC_PARTS.get("coil_copper").get())
                ),
                List.of(ingredient(NC_PARTS.get("basic_electric_circuit").get()))
        );

        itemsToItems(
                List.of(
                        ingredient(GLOWSTONE_DUST),
                        dustIngredient(Materials.quartz),
                        ingredient(REDSTONE),
                        dustIngredient(Materials.emerald)
                ),
                List.of(dustIngredient(Materials.energetic_blend, 2))
        );

        itemsToItems(
                List.of(
                        ingredient(NC_ITEMS.get("compact_water_collector").get(),4),
                        ingredient(HEART_OF_THE_SEA),
                        plateIngredient(Materials.platinum, 4),
                        ingredient(NC_PARTS.get("motor").get())
                ),
                List.of(ingredient(NC_ITEMS.get("dense_water_collector").get()))
        );

        itemsToItems(
                List.of(
                        ingredient(NC_ITEMS.get("compact_nitrogen_collector").get(),4),
                        plateIngredient(Materials.beryllium),
                        plateIngredient(Materials.netherite, 4),
                        ingredient(NC_PARTS.get("motor").get())
                ),
                List.of(ingredient(NC_ITEMS.get("dense_nitrogen_collector").get()))
        );

        itemsToItems(
                List.of(
                        ingredient(NC_ITEMS.get("compact_helium_collector").get(),4),
                        plateIngredient(Materials.zinc),
                        plateIngredient(Materials.cobalt, 4),
                        ingredient(NC_PARTS.get("motor").get())
                ),
                List.of(ingredient(NC_ITEMS.get("dense_helium_collector").get()))
        );

        itemsToItems(
                List.of(
                        dustIngredient(Materials.bscco, 3),
                        ingotIngredient(Materials.silver, 6)
                ),
                List.of(ingredient(NC_PARTS.get("coil_bscco").get(), 3))
        );

        itemsToItems(
                List.of(
                        dustIngredient(Materials.bismuth, 2),
                        dustIngredient(Materials.strontium, 2),
                        dustIngredient(Materials.calcium, 2),
                        dustIngredient(Materials.copper, 2)
                ),
                List.of(dustIngredient(Materials.bscco, 3))
        );
    }
}
