package igentuman.nc.recipes.type;

import igentuman.nc.util.insitu_leaching.OreVeinProvider;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.ItemStackIngredient;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Random;

import static igentuman.nc.NuclearCraft.currentTick;
import static igentuman.nc.setup.registration.NCItems.NC_PARTS;
import static net.minecraft.world.item.Items.BARRIER;

public class OreVeinRecipe extends NcRecipe {
    public OreVeinRecipe(String codeId, ItemStackIngredient[] input, ItemStackIngredient[] output, FluidStackIngredient[] inputFluids, FluidStackIngredient[] outputFluids, double timeModifier, double powerModifier, double radiation, double rarityModifier) {
        super(codeId, input, output, timeModifier, powerModifier, radiation, rarityModifier);
    }

    private HashMap<ItemStackIngredient, Integer> itemsPool = new HashMap<>();
    private int roll;

    public HashMap<ItemStackIngredient, Integer> getItemsPool() {
        if(itemsPool.isEmpty()) {
            for(ItemStackIngredient item : inputItems) {
                if(item.isValid()) {
                    itemsPool.put(item, item.getAmount());
                }
            }
        }
        return itemsPool;
    }

    @Override
    public @NotNull String getGroup() {
        return codeId;
    }

    @Override
    public @NotNull ItemStack getToastSymbol() {
        return new ItemStack(NC_PARTS.get("research_paper").get());
    }

    private int gameTimeSeed(ServerLevel level) {
        Random rand = new Random(currentTick);
        return rand.nextInt();
    }

    public ItemStack getRandomOre(ServerLevel level, int x, int z, int id) {
        int score = OreVeinProvider.get(level).rand(x, z, id, gameTimeSeed(level)).nextInt(50)+50;
        roll = 1;
        ItemStack ore = getOreByScore(score, level, x, z).copy();
        ore.setCount(1);
        return ore;
    }

    public ItemStack getOreByScore(int score, ServerLevel level, int x, int z) {
        for (ItemStackIngredient item: getItemsPool().keySet()) {
            if (score <= getItemsPool().get(item) && OreVeinProvider.get(level).rand(x, z, score, gameTimeSeed(level)).nextInt(10)>roll) {
                return item.getRepresentations().get(0);
            }
            roll++;
            roll = Math.min(roll, 8);
            score -= getItemsPool().get(item)/2;
        }
        return getOreByScore(score, level, x, z);
    }

    @Override
    public void write(RegistryFriendlyByteBuf buffer) {
        super.write(buffer);
        buffer.writeDouble(rarityModifier);
    }
}
