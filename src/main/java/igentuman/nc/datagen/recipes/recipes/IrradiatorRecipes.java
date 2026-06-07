package igentuman.nc.datagen.recipes.recipes;

import igentuman.nc.content.materials.Materials;
import igentuman.nc.content.processors.Processors;
import igentuman.nc.datagen.recipes.builder.NcRecipeBuilder;
import igentuman.nc.recipes.ingredient.NcIngredient;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.Tags;

import java.util.List;
import java.util.function.Consumer;

import static igentuman.nc.content.materials.Materials.quantite_energy;
import static igentuman.nc.content.materials.Materials.subliquid_matter;
import static igentuman.nc.datagen.recipes.NCRecipes.MOLTEN_INGOT;
import static igentuman.nc.setup.registration.NCBlocks.WASTELAND_EARTH;
import static igentuman.nc.setup.registration.NCItems.NC_PARTS;
import static net.minecraft.world.level.block.Blocks.*;


public class IrradiatorRecipes extends AbstractRecipeProvider {

    public static void generate(Consumer<FinishedRecipe> consumer) {
        IrradiatorRecipes.consumer = consumer;
        ID = Processors.IRRADIATOR;

        itemToItem(dustIngredient(Materials.thorium), dustIngredient(Materials.tbp), 1.5D);
        itemsAndFluids(
                List.of(ingredient(Item.byBlock(COBBLESTONE))),
                List.of(ingredient(Item.byBlock(NETHERRACK))),
                List.of(fluidIngredient("redstone", MOLTEN_INGOT/2)),
                List.of(), 0.5D);
        itemToItem(ingredient(Tags.Items.SAND, 1), NcIngredient.stack(stack(GLOWSTONE, 1)), 3D);
        itemToItem(ingredient(COARSE_DIRT, 1), NcIngredient.stack(stack(WASTELAND_EARTH.get(), 1)), 3D);
        itemToItem(ingredient(NC_PARTS.get("silicon_wafer").get(), 1), NcIngredient.stack(stack(NC_PARTS.get("silicon_n_doped").get(), 1)), 0.75D);
        itemToItem(dustIngredient(Materials.tbp), dustIngredient(Materials.protactinium_233), 2.5D);
        itemToItem(dustIngredient(Materials.bismuth), dustIngredient(Materials.polonium), 2D);
        itemToItem(ingotIngredient(Materials.platinum), ingotIngredient(Materials.neutronium), 10D);
        fluidsAndFluids(List.of(fluidIngredient("lithium", 500)), List.of(fluidIngredient("irradiated_lithium", 500)), 1.5D);
        fluidsAndFluids(List.of(fluidIngredient("boron", 500)), List.of(fluidIngredient("irradiated_boron", 500)), 2.5D);
        fluidsAndFluids(List.of(fluidIngredient(Materials.uranium238, 100)), List.of(fluidIngredient(Materials.uranium235, 100)), 5.5D);
        NcRecipeBuilder.get(ID)
                .fluids(List.of(fluidIngredient(subliquid_matter, 1000)), List.of(fluidIngredient(quantite_energy, 1000)))
                .items(List.of(ingredient(isotopeItem(Materials.quantite))), List.of())
                .modifiers(0.25d, 2d, 1)
                .build(consumer);
    }
}
