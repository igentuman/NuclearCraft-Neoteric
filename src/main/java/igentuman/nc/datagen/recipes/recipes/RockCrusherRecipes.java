package igentuman.nc.datagen.recipes.recipes;

import igentuman.nc.recipes.ingredient.NcIngredient;
import igentuman.nc.setup.processors.Processors;
import igentuman.nc.setup.registration.Materials;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.Tags;

import java.util.function.Consumer;

import static net.minecraft.world.item.Items.*;

public class RockCrusherRecipes extends AbstractRecipeProvider {

    public static void generate(Consumer<FinishedRecipe> consumer) {
        RockCrusherRecipes.consumer = consumer;
        ID = Processors.ROCK_CRUSHER;
        add(
                NcIngredient.stack(new ItemStack(GRANITE, 4)),
                new ItemStack[]{new ItemStack(dustItem(Materials.rhodochrosite), 2), new ItemStack(dustItem(Materials.villiaumite))}
        );

        add(
                NcIngredient.stack(new ItemStack(DIORITE, 4)),
                new ItemStack[]{new ItemStack(dustItem(Materials.zirconium), 1), new ItemStack(dustItem(Materials.fluorite)), new ItemStack(dustItem(Materials.carobbiite))}
        );

        add(
                NcIngredient.stack(new ItemStack(ANDESITE, 4)),
                new ItemStack[]{new ItemStack(dustItem(Materials.beryllium), 1), new ItemStack(dustItem(Materials.arsenic))}
        );

    }


}
