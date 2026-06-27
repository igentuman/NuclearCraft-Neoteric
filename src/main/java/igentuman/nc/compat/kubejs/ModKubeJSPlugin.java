package igentuman.nc.compat.kubejs;

import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.*;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchemaRegistry;
import dev.latvian.mods.kubejs.registry.BuilderTypeRegistry;
import igentuman.nc.registration.ModEntry;
import igentuman.nc.setup.ModEntries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.List;

public class ModKubeJSPlugin implements KubeJSPlugin {

    private static final RecipeKey<List<SizedIngredient>> ITEM_INPUTS =
            SizedIngredientComponent.FLAT.instance().asList().inputKey("item_inputs").optional(List.of());

    private static final RecipeKey<List<SizedFluidIngredient>> FLUID_INPUTS =
            SizedFluidIngredientComponent.FLAT.instance().asList().inputKey("fluid_inputs").optional(List.of());

    private static final RecipeKey<List<ItemStack>> ITEM_OUTPUTS =
            ItemStackComponent.ITEM_STACK.instance().asList().outputKey("item_outputs").optional(List.of());

    private static final RecipeKey<List<FluidStack>> FLUID_OUTPUTS =
            FluidStackComponent.FLUID_STACK.instance().asList().outputKey("fluid_outputs").optional(List.of());

    private static final RecipeKey<Integer> PROCESS_TIME =
            NumberComponent.INT.otherKey("process_time");

    private static final RecipeKey<Integer> ENERGY_PER_TICK =
            NumberComponent.INT.otherKey("energy_per_tick");

    private static final RecipeSchema UNIVERSAL_PROCESSOR_SCHEMA = new RecipeSchema(
            ITEM_INPUTS, FLUID_INPUTS, ITEM_OUTPUTS, FLUID_OUTPUTS, PROCESS_TIME, ENERGY_PER_TICK
    );

    @Override
    public void registerBuilderTypes(BuilderTypeRegistry registry) {
    }

    @Override
    public void registerRecipeSchemas(RecipeSchemaRegistry registry) {
        ModEntries.init();
        for (ModEntry entry : ModEntries.ENTRIES.values()) {
            if (entry.hasRecipes()) {
                ResourceLocation recipeTypeId = entry.recipeType().getId();
                registry.register(recipeTypeId, UNIVERSAL_PROCESSOR_SCHEMA);
            }
        }
    }
}
