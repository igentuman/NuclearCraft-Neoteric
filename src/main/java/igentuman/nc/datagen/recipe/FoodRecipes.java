package igentuman.nc.datagen.recipe;

import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.world.item.crafting.Ingredient;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.datagen.recipe.ModRecipeProvider.*;
import static igentuman.nc.datagen.recipe.VanillaRecipes.has;
import static net.minecraft.world.item.Items.*;

public class FoodRecipes {

    public static void food(RecipeOutput out) {
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(COCOA_BEANS), RecipeCategory.FOOD,
                        part("roasted_cocoa_beans"), 1.0f, 200)
                .unlockedBy("has_cocoa_beans", has(COCOA_BEANS))
                .save(out, rl("roasted_cocoa_beans"));
        SimpleCookingRecipeBuilder.smoking(Ingredient.of(COCOA_BEANS), RecipeCategory.FOOD,
                        part("roasted_cocoa_beans"), 1.0f, 100)
                .unlockedBy("has_cocoa_beans", has(COCOA_BEANS))
                .save(out, rl("roasted_cocoa_beans_from_smoking"));
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(MILK_BUCKET), RecipeCategory.FOOD,
                        fluidOf("pasteurized_milk").getBucket(), 1.0f, 200)
                .unlockedBy("has_milk", has(MILK_BUCKET))
                .save(out, rl("pasteurized_milk"));

        String base = "graham_cracker";
        for (String result : new String[]{"smore", "moresmore", "foursmore"}) {
            ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, part(result))
                    .requires(part(base), 2)
                    .requires(part("milk_chocolate"))
                    .requires(part("marshmallow"))
                    .unlockedBy("has_base", has(part(base)))
                    .save(out, rl(result));
            base = result;
        }
    }
}
