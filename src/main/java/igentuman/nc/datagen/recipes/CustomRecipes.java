package igentuman.nc.datagen.recipes;

import igentuman.nc.datagen.recipes.builder.NcRecipeBuilder;
import igentuman.nc.datagen.recipes.recipes.*;
import igentuman.nc.recipes.ingredient.NcIngredient;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

import static igentuman.nc.NuclearCraft.rl;

public class CustomRecipes extends NCRecipes {
    public CustomRecipes(DataGenerator generatorIn) {
        super(generatorIn);
    }
    public static Consumer<FinishedRecipe> consumer;
    public static void generate(Consumer<FinishedRecipe> consumer) {
        CustomRecipes.consumer = consumer;
        FissionRecipes.generate(consumer);
        ManufactoryRecipes.generate(consumer);
        DecayHastenerRecipes.generate(consumer);
        PressurizerRecipes.generate(consumer);
        AlloySmelterRecipeProvider.generate(consumer);
        RockCrusherRecipes.generate(consumer);
    }
}
