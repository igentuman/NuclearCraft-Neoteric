package igentuman.nc.datagen.recipe.processors;

import igentuman.nc.datagen.recipe.UniversalProcessorRecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;

import static igentuman.nc.datagen.recipe.ModRecipeProvider.*;
import static igentuman.nc.setup.entries.Processors.IRRADIATOR;

public class IrradiatorRecipes {

    public static void irradiator(RecipeOutput out) {
        String id = IRRADIATOR;

        i2i(out, id, "thorium_to_tbp", dust("thorium"), dust("tbp"), 1, (int)(TIME * 1.5));

        UniversalProcessorRecipeBuilder.processor(id)
                .itemInput(Items.COBBLESTONE)
                .fluidInput("redstone", MOLTEN_INGOT / 2)
                .itemOutput(Items.NETHERRACK)
                .processTime((int)(TIME * 0.5))
                .save(out, "cobblestone_to_netherrack");

        UniversalProcessorRecipeBuilder.processor(id)
                .itemInput(ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "sands")))
                .itemOutput(Items.GLOWSTONE)
                .processTime(TIME * 3)
                .save(out, "sand_to_glowstone");

        i2i(out, id, "coarse_dirt_to_wasteland_earth", Items.COARSE_DIRT, part("wasteland_earth"), 1, TIME * 3);
        i2i(out, id, "silicon_wafer_to_n_doped", part("silicon_wafer"), part("silicon_n_doped"), 1, (int)(TIME * 0.75));
        i2i(out, id, "tbp_to_protactinium_233", dust("tbp"), dust("protactinium_233"), 1, (int)(TIME * 2.5));
        i2i(out, id, "bismuth_to_polonium", dust("bismuth"), dust("polonium"), 1, TIME * 2);
        i2i(out, id, "platinum_to_neutronium", ingot("platinum"), ingot("neutronium"), 1, TIME * 10);

        f2f(out, id, "lithium_to_irradiated_lithium", fluidOf("lithium"), 500, fluidOf("irradiated_lithium"), 500, (int)(TIME * 1.5));
        f2f(out, id, "boron_to_irradiated_boron", fluidOf("boron"), 500, fluidOf("irradiated_boron"), 500, (int)(TIME * 2.5));
        f2f(out, id, "uranium_238_to_235", isotopeFluid("uranium/238", ""), 100, isotopeFluid("uranium/235", ""), 100, (int)(TIME * 5.5));

        UniversalProcessorRecipeBuilder.processor(id)
                .itemInput(isotope("quantite"))
                .fluidInput(fluidOf("subliquid_matter"), 1000)
                .fluidOutput(fluidOf("quantite_energy"), 1000)
                .processTime((int)(TIME * 0.25))
                .energyPerTick(ENERGY * 2)
                .save(out, "quantite_energy");
    }
}
