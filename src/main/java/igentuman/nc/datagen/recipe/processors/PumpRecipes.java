package igentuman.nc.datagen.recipe.processors;

import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.level.material.Fluids;

import static igentuman.nc.datagen.recipe.ModRecipeProvider.*;
import static igentuman.nc.setup.entries.Processors.PUMP;

public class PumpRecipes {

    public static void pump(RecipeOutput out) {
        String id = PUMP;
        melt(out, id, "water_collector",         part("water_collector"),         1, Fluids.WATER, 200, 20);
        melt(out, id, "compact_water_collector", part("compact_water_collector"), 1, Fluids.WATER, 2000, 20);
        melt(out, id, "dense_water_collector",   part("dense_water_collector"),   1, Fluids.WATER, 10000, 20);
        melt(out, id, "nitrogen_collector",         part("nitrogen_collector"),         1, fluidOf("nitrogen"), 50, 20);
        melt(out, id, "compact_nitrogen_collector", part("compact_nitrogen_collector"), 1, fluidOf("nitrogen"), 500, 20);
        melt(out, id, "dense_nitrogen_collector",   part("dense_nitrogen_collector"),   1, fluidOf("nitrogen"), 2500, 20);
        melt(out, id, "helium_collector",         part("helium_collector"),         1, fluidOf("helium"), 50, 20);
        melt(out, id, "compact_helium_collector", part("compact_helium_collector"), 1, fluidOf("helium"), 500, 20);
        melt(out, id, "dense_helium_collector",   part("dense_helium_collector"),   1, fluidOf("helium"), 2500, 20);
        melt(out, id, "lava_collector",          part("lava_collector"),          1, Fluids.LAVA, 1000, 200, 150);
    }
}
