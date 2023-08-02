package igentuman.nc.datagen.recipes.recipes;

import igentuman.nc.datagen.recipes.builder.ItemToItemRecipeBuilder;
import igentuman.nc.recipes.ingredient.NcIngredient;
import igentuman.nc.setup.processors.Processors;
import igentuman.nc.setup.registration.Materials;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

import static igentuman.nc.NuclearCraft.rl;

public class AlloySmelterRecipeProvider extends AbstractRecipeProvider {

    public static void generate(Consumer<FinishedRecipe> consumer) {
        AlloySmelterRecipeProvider.consumer = consumer;
        ID = Processors.ALLOY_SMELTER;
        doubleToItem(ID, NcIngredient.stack(new ItemStack(dustItem(Materials.coal), 4)), NcIngredient.of(dustTag(Materials.iron)), new ItemStack(ingotItem(Materials.steel)));
        doubleToItem(ID, NcIngredient.stack(new ItemStack(dustItem(Materials.copper), 3)), NcIngredient.of(dustTag(Materials.tin)), new ItemStack(ingotItem(Materials.bronze), 4));
        doubleToItem(ID, NcIngredient.of(dustTag(Materials.boron)), NcIngredient.of(dustTag(Materials.steel)), new ItemStack(ingotItem(Materials.ferroboron), 2));
        doubleToItem(ID, NcIngredient.of(dustTag(Materials.graphite)), NcIngredient.of(dustTag(Materials.diamond)), new ItemStack(ingotItem(Materials.hard_carbon), 2), 1d, 2d);
        doubleToItem(ID, NcIngredient.of(dustTag(Materials.ferroboron)), NcIngredient.of(dustTag(Materials.lithium)), new ItemStack(ingotItem(Materials.tough_alloy), 2), 1.5d, 1.5d);
        doubleToItem(ID, NcIngredient.of(dustTag(Materials.magnesium)), NcIngredient.stack(new ItemStack(dustItem(Materials.boron), 2)), new ItemStack(ingotItem(Materials.magnesium_diboride), 3));
        doubleToItem(ID, NcIngredient.of(dustTag(Materials.lithium)), NcIngredient.of(dustTag(Materials.manganese_dioxide)), new ItemStack(ingotItem(Materials.lithium_manganese_dioxide), 2), 1.5d);
        doubleToItem(ID, NcIngredient.stack(new ItemStack(dustItem(Materials.copper), 3)), NcIngredient.of(dustTag(Materials.silver)), new ItemStack(ingotItem(Materials.shibuichi), 4), 1.5d);
        doubleToItem(ID, NcIngredient.stack(new ItemStack(dustItem(Materials.tin), 3)), NcIngredient.of(dustTag(Materials.silver)), new ItemStack(ingotItem(Materials.tin_silver), 4));
        doubleToItem(ID, NcIngredient.stack(new ItemStack(dustItem(Materials.lead), 3)), NcIngredient.of(dustTag(Materials.platinum)), new ItemStack(ingotItem(Materials.lead_platinum), 4), 1.5d);
        doubleToItem(ID, NcIngredient.of(dustTag(Materials.tough_alloy)), NcIngredient.of(dustTag(Materials.hard_carbon)), new ItemStack(ingotItem(Materials.extreme), 2));
        doubleToItem(ID, NcIngredient.of(dustTag(Materials.boron_arsenide)), NcIngredient.of(dustTag(Materials.extreme)), new ItemStack(ingotItem(Materials.thermoconducting), 2), 1.5d, 1.5d);
        doubleToItem(ID, NcIngredient.of(gemTag(Materials.silicon)), NcIngredient.of(dustTag(Materials.graphite)), new ItemStack(ingotItem(Materials.silicon_carbide), 2), 2d, 2d);
        doubleToItem(ID, NcIngredient.stack(new ItemStack(dustItem(Materials.zirconium), 7)), NcIngredient.of(dustTag(Materials.tin)), new ItemStack(ingotItem(Materials.zircaloy), 8));
        doubleToItem(ID, NcIngredient.stack(new ItemStack(dustItem(Materials.iron), 15)), NcIngredient.of(dustTag(Materials.carbon_manganese)), new ItemStack(ingotItem(Materials.hsla_steel), 16),8D, 2D);
        doubleToItem(ID, NcIngredient.stack(new ItemStack(dustItem(Materials.molybdenum), 15)), NcIngredient.of(dustTag(Materials.zirconium)), new ItemStack(ingotItem(Materials.zirconium_molybdenum), 16),8D, 2D);
    }

    protected static void doubleToItem(String id, NcIngredient input1, NcIngredient input2, ItemStack output, double... params) {

        double timeModifier = params.length>0 ? params[0] : 1.0;
        double powerModifier = params.length>1 ? params[1] : 1.0;
        double radiation = params.length>2 ? params[2] : 1.0;
        ItemToItemRecipeBuilder.create(id, input1, input2, output)
                .modifiers(timeModifier, radiation, powerModifier)
                .build(consumer, rl(id+"/"+input1.getName()+"-"+output.getItem().toString()));
    }
}
