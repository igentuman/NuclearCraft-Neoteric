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
                "nuclearcraft:xenorium_298",
                "minecraft:nether_star",
                "minecraft:ender_eye",
                "minecraft:ender_pearl",
                "minecraft:dragon_breath",
                "#forge:raw_materials/iron",
                "#forge:raw_materials/gold",
                "#forge:raw_materials/copper",
                "#forge:raw_materials/zinc",
                "#forge:raw_materials/tungsten",
                "#forge:raw_materials/titanium",
                "#forge:raw_materials/tin",
                "#forge:raw_materials/lead",
                "#forge:raw_materials/silver",
                "#forge:raw_materials/aluminum",
                "#forge:raw_materials/uranium",
                "#forge:raw_materials/thorium",
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
                "#forge:gems/lapis",
                "#forge:gems/sapphire",
                "#forge:gems/fluorite",
                "#forge:dyes/blue",
                "#forge:dyes/white",
                "#forge:dyes/red",
                "#forge:dyes/green",
                "#forge:dyes/yellow",
                "#forge:dusts/redstone",
                "#forge:dusts/lapis",
                "#forge:dusts/coal",
                "#forge:dusts/glowstone",
                "#forge:blocks/wool"
        );
        itemToItem(ingotStack("tough_alloy"), dustStack("bscco", 1), 2);
        for(String name: items) {
            NcIngredient ingredient = NcIngredient.of(name);
            if(ingredient.isEmpty()) continue;
            double timeModifier = 1.0D;

            itemToItem(ingredient, ingredient, timeModifier);
        }
    }
}
