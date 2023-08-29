package igentuman.nc.recipes.type;

import igentuman.nc.handler.OreVeinProvider;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;

import static igentuman.nc.compat.GlobalVars.CATALYSTS;
import static igentuman.nc.handler.config.CommonConfig.IN_SITU_LEACHING;
import static igentuman.nc.setup.registration.NCItems.NC_PARTS;

public class OreVeinRecipe extends NcRecipe {
    public OreVeinRecipe(ResourceLocation id, ItemStackIngredient[] input, ItemStack[] output, FluidStackIngredient[] inputFluids, FluidStack[] outputFluids, double timeModifier, double powerModifier, double radiation, double rarityModifier) {
        super(id, input, output, timeModifier, powerModifier, radiation, rarityModifier);
        ID = "nc_ore_veins";
        CATALYSTS.put(ID, List.of(getToastSymbol()));
    }

    @Override
    public @NotNull String getGroup() {
        return ID;
    }

    @Override
    public @NotNull ItemStack getToastSymbol() {
        return new ItemStack(NC_PARTS.get("research_paper").get());
    }



    public ItemStack getRandomOre(ServerLevel level, int x, int z, int id) {
        int score = OreVeinProvider.get(level).rand(x, z, id).nextInt(100);
        return getOreByScore(score, level, x, z);
    }

    public ItemStack getOreByScore(int score, ServerLevel level, int x, int z) {
        int id = OreVeinProvider.get(level).rand(x, z, score).nextInt(inputItems.length);
        for(int i = id; i < inputItems.length; i++) {
            if(score <= inputItems[i].getRepresentations().get(0).getCount()) {
                return inputItems[i].getRepresentations().get(0);
            }
            score -= inputItems[i].getRepresentations().get(0).getCount();
        }
        return getOreByScore(score, level, x, z);
    }
}
