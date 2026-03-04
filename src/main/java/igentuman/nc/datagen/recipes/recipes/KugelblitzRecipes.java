package igentuman.nc.datagen.recipes.recipes;

import igentuman.nc.recipes.ingredient.NcIngredient;
import net.minecraft.data.recipes.RecipeOutput;

import java.util.List;

import static igentuman.nc.handler.config.KugelblitzConfig.KUGELBLITZ_CONFIG;

public class KugelblitzRecipes extends AbstractRecipeProvider {

    public static void generate(RecipeOutput consumer) {
        KugelblitzRecipes.consumer = consumer;
        ID = "kugelblitz_chamber";
        List<String> items = List.of(
                "nuclearcraft:xenorium_298",
                "minecraft:nether_star",
                "minecraft:ender_eye",
                "minecraft:ender_pearl",
                "minecraft:dragon_breath",
                "#c:raw_materials/iron",
                "#c:raw_materials/gold",
                "#c:raw_materials/copper",
                "#c:raw_materials/zinc",
                "#c:raw_materials/tungsten",
                "#c:raw_materials/titanium",
                "#c:raw_materials/tin",
                "#c:raw_materials/lead",
                "#c:raw_materials/silver",
                "#c:raw_materials/aluminum",
                "#c:raw_materials/uranium",
                "#c:raw_materials/thorium",
                "#c:ingots/iron",
                "#c:ingots/gold",
                "#c:ingots/copper",
                "#c:ingots/zinc",
                "#c:ingots/tungsten",
                "#c:ingots/titanium",
                "#c:ingots/netherite",
                "#c:ingots/tin",
                "#c:ingots/lead",
                "#c:ingots/silver",
                "#c:ingots/aluminum",
                "#c:ingots/uranium",
                "#c:ingots/thorium",
                "#c:ingots/lithium",
                "#c:ingots/beryllium",
                "#c:ingots/steel",
                "#c:ingots/bronze",
                "#c:gems/diamond",
                "#c:gems/ruby",
                "#c:gems/emerald",
                "#c:gems/lapis",
                "#c:gems/sapphire",
                "#c:gems/fluorite",
                "#c:dyes/blue",
                "#c:dyes/white",
                "#c:dyes/red",
                "#c:dyes/green",
                "#c:dyes/yellow",
                "#c:dusts/redstone",
                "#c:dusts/lapis",
                "#c:dusts/coal",
                "#c:dusts/glowstone",
                "#c:blocks/wool"
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
