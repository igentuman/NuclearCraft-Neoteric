package igentuman.nc.util.insitu_leaching;

import igentuman.nc.config.Common;
import igentuman.nc.recipe.OreVeinRecipe;
import igentuman.nc.setup.ModEntries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/** Deterministic per-chunk vein selection using a seed derived from the level seed and chunk coordinates. */
public class OreVeinProvider {

    private final ServerLevel level;
    protected List<OreVeinRecipe> recipes;
    protected static final Map<Level, OreVeinProvider> providers = new HashMap<>();

    @SuppressWarnings("unchecked")
    private OreVeinProvider(ServerLevel level) {
        this.level = level;
        RecipeType<OreVeinRecipe> type = (RecipeType<OreVeinRecipe>) ModEntries.get("nc_ore_veins").recipeType().get();
        this.recipes = level.getRecipeManager().getAllRecipesFor(type).stream().map(RecipeHolder::value).toList();
    }

    /** Rolls against the configured veins rarity to determine if a chunk contains a vein. */
    public boolean chunkContainsVein(int chunkX, int chunkZ) {
        if (!Common.IN_SITU_ENABLE_VEINS.get()) return false;
        return rand(chunkX, chunkZ).nextInt(Common.IN_SITU_VEINS_RARITY.get()) <= 10;
    }

    /** Rolls within the configured vein blocks amount range to determine vein size. */
    public int getVeinSize(int x, int z) {
        List<? extends Integer> range = Common.IN_SITU_VEIN_BLOCKS_AMOUNT.get();
        int min = range.get(0);
        int max = range.get(1);
        return rand(x, z).nextInt(max - min + 1) + min;
    }

    /** Creates a deterministic Random from the level seed, chunk coordinates, and optional additional seeds. */
    public Random rand(int x, int z, int... seeds) {
        int additional = 0;
        for (int seed : seeds) {
            additional += seed;
        }
        return new Random(level.getSeed() / 2 + x + z + additional);
    }

    public static OreVeinProvider get(ServerLevel level) {
        if (!providers.containsKey(level)) {
            providers.put(level, new OreVeinProvider(level));
        }
        return providers.get(level);
    }

    /** Selects a random vein recipe from the pool, weighted by rarity modifier (higher = rarer). */
    public OreVeinRecipe selectRandomVein(Random random, int x, int z) {
        if (recipes.isEmpty()) return null;
        if (Common.IN_SITU_RANDOMIZED_ORES.get()) {
            return recipes.get(random.nextInt(recipes.size()));
        }
        return selectRandomVeinWeighted(random, x, z, 0);
    }

    private OreVeinRecipe selectRandomVeinWeighted(Random random, int x, int z, int rolls) {
        OreVeinRecipe recipe = recipes.get(random.nextInt(recipes.size()));
        if (recipe.getRarityModifier() > rolls) {
            return selectRandomVeinWeighted(rand(x, z, rolls + 1), x, z, rolls + 1);
        }
        return recipe;
    }

    /** Returns the vein recipe for a chunk, or null if the chunk has no vein. */
    public OreVeinRecipe getVeinForChunk(int chunkX, int chunkZ) {
        if (!chunkContainsVein(chunkX, chunkZ)) {
            return null;
        }
        return selectRandomVein(rand(chunkX, chunkZ), chunkX, chunkZ);
    }
}
