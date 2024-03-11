package igentuman.nc.recipes.type;

import igentuman.nc.handler.OreVeinProvider;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import org.antlr.v4.runtime.misc.NotNull;;

import static igentuman.nc.setup.registration.NCItems.NC_PARTS;

public class OreVeinRecipe extends NcRecipe {
    public OreVeinRecipe(ResourceLocation id, ItemStackIngredient[] input, ItemStack[] output, FluidStackIngredient[] inputFluids, FluidStack[] outputFluids, double timeModifier, double powerModifier, double radiation, double rarityModifier) {
        super(id, input, output, timeModifier, powerModifier, radiation, rarityModifier);
    }

    @Override
    public @NotNull String getGroup() {
        return codeId;
    }

    @Override
    public @NotNull ItemStack getToastSymbol() {
        return new ItemStack(NC_PARTS.get("research_paper").get());
    }

    public ItemStack getRandomOre(World level, int x, int z, int id) {
        int score = OreVeinProvider.get(level).rand(x, z, id).nextInt(100);
        return getOreByScore(score, level, x, z);
    }

    public ItemStack getOreByScore(int score, World level, int x, int z) {
        int id = OreVeinProvider.get(level).rand(x, z, score).nextInt(inputItems.length);
        for(int i = id; i < inputItems.length; i++) {
            if(score <= inputItems[i].getRepresentations().get(0).getCount()) {
                return inputItems[i].getRepresentations().get(0);
            }
            score -= inputItems[i].getRepresentations().get(0).getCount();
        }
        return getOreByScore(score, level, x, z);
    }

    @Override
    public void write(PacketBuffer buffer) {

        super.write(buffer);

        buffer.writeDouble(rarityModifier);
    }
}
