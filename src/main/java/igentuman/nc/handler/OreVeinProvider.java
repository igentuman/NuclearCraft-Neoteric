package igentuman.nc.handler;

import igentuman.nc.recipes.NcRecipeType;
import igentuman.nc.recipes.type.OreVeinRecipe;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static igentuman.nc.handler.config.ProcessorsConfig.IN_SITU_LEACHING;

public class OreVeinProvider {
    private ServerLevel level;
    protected List<OreVeinRecipe> recipes;
    protected static Map<Level, OreVeinProvider> providers = new HashMap<>();

    @SuppressWarnings("unchecked")
    private OreVeinProvider(ServerLevel level) {
        this.level = level;
        recipes = (List<OreVeinRecipe>) level.getRecipeManager()
                .getAllRecipesFor(NcRecipeType.ALL_RECIPES.get("nc_ore_veins").getRecipeType());
    }

    public boolean chunkContainsVein(int chunkX, int chunkZ)
    {
        return rand(chunkX, chunkZ).nextInt(IN_SITU_LEACHING.VEINS_RARITY.get()) <= 10;
    }

    public int getVeinSize(int x, int z) {
        List<Integer> range = IN_SITU_LEACHING.VEIN_BLOCKS_AMOUNT.get();
        return rand(x, z).nextInt(range.get(1)-range.get(0))+range.get(0);
    }

    public Random rand(int x, int z, int ... seeds) {
        int additional = 0;
        for(int seed : seeds)
        {
            additional += seed;
        }
        return new Random(level.getSeed()/2+x+z+additional);
    }

    public static OreVeinProvider get(ServerLevel level)
    {
        if(!providers.containsKey(level))
        {
            providers.put(level, new OreVeinProvider(level));
        }
        return providers.get(level);
    }

    private int rolls = 0;

    public OreVeinRecipe selectRandomVein(Random random, int x, int z)
    {
        OreVeinRecipe recipe = recipes.get(random.nextInt(recipes.size()));
        if(recipe.rarityModifier > rolls)
        {
            rolls++;
            return selectRandomVein(rand(x, z, rolls), x, z);
        }
        rolls = 0;
        return recipe;
    }

    public OreVeinRecipe getVeinForChunk(int chunkX, int chunkZ)
    {
        if(!chunkContainsVein(chunkX, chunkZ))
        {
            return null;
        }
        return selectRandomVein(rand(chunkX, chunkZ), chunkX, chunkZ);
    }
}
