package igentuman.nc.datagen.recipes.recipes;

import igentuman.nc.recipes.ingredient.NcIngredient;
import net.minecraft.data.recipes.FinishedRecipe;

import java.util.List;
import java.util.function.Consumer;

import static igentuman.nc.handler.config.KugelblitzConfig.KUGELBLITZ_CONFIG;

public class KugelblitzRecipes extends AbstractRecipeProvider {

    public static void generate(Consumer<FinishedRecipe> consumer) {
        KugelblitzRecipes.consumer = consumer;
        ID = "kugelblitz_chamber";
        List<String> items = List.of(
                "nuclearcraft:xenorium298",
                "minecraft:nether_star",
                "minecraft:ender_eye",
                "minecraft:ender_pearl",
                "minecraft:dragon_breath",
                "#forge:ingots/iron",
                "#forge:ingots/gold",
                "#forge:ingots/copper",
                "#forge:ingots/zinc",
                "#forge:ingots/tungsten",
                "#forge:ingots/titanium",
                "#forge:ingots/netherite",
                "#forge:ingots/tin",
                "#forge:ingots/lead",
                "#forge:ingots/silver",
                "#forge:ingots/aluminum",
                "#forge:ingots/uranium",
                "#forge:ingots/thorium",
                "#forge:ingots/lithium",
                "#forge:ingots/beryllium",
                "#forge:ingots/steel",
                "#forge:ingots/bronze",
                "#forge:gems/diamond",
                "#forge:gems/ruby",
                "#forge:gems/emerald",
                "#forge:gems/sapphire",
                "#forge:gems/fluorite",
                "#forge:dusts/redstone",
                "#forge:dusts/lapis",
                "#forge:dusts/coal",
                "#forge:dusts/glowstone",
                "#forge:blocks/wool"
        );

        for(String name: items) {
            NcIngredient ingredient = NcIngredient.of(name);
            if(ingredient.isEmpty()) continue;
            itemToItem(ingredient, ingredient);
        }
    }
}
