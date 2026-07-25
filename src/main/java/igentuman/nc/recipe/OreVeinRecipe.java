package igentuman.nc.recipe;

import igentuman.nc.config.Common;
import igentuman.nc.setup.ModEntries;
import igentuman.nc.util.insitu_leaching.OreVeinProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Custom recipe defining a weighted ore pool for a virtual vein type, with score-based random ore selection. */
public class OreVeinRecipe implements Recipe<ProcessorRecipeInput> {

    private final ResourceLocation id;
    private final List<OreEntry> ores;
    private final double rarityModifier;

    public OreVeinRecipe(ResourceLocation id, List<OreEntry> ores, double rarityModifier) {
        this.id = id;
        this.ores = ores;
        this.rarityModifier = rarityModifier;
    }

    public record OreEntry(SizedIngredient ingredient, int weight) {}

    @Override
    public boolean matches(ProcessorRecipeInput input, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(ProcessorRecipeInput input, HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return ores.isEmpty() ? ItemStack.EMPTY : ores.getFirst().ingredient().getItems()[0];
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        for (OreEntry entry : ores) {
            list.add(entry.ingredient().ingredient());
        }
        return list;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModEntries.get("nc_ore_veins").recipeSerializer().get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModEntries.get("nc_ore_veins").recipeType().get();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    public ResourceLocation getId() {
        return id;
    }

    public List<OreEntry> getOres() {
        return ores;
    }

    public double getRarityModifier() {
        return rarityModifier;
    }

    /** Weighted random selection of an ore from the pool, seeded by chunk coordinates and game time. */
    public ItemStack getRandomOre(ServerLevel level, int x, int z, int id) {
        if (ores.isEmpty()) return ItemStack.EMPTY;
        Random random = new Random(level.getSeed() / 2 + x + z + id + level.getGameTime());
        if (Common.IN_SITU_RANDOMIZED_ORES.get()) {
            OreEntry entry = ores.get(random.nextInt(ores.size()));
            return copyOre(entry, random);
        }
        int totalWeight = 0;
        for (OreEntry entry : ores) {
            totalWeight += entry.weight();
        }
        int roll = random.nextInt(totalWeight);
        int accumulated = 0;
        for (OreEntry entry : ores) {
            accumulated += entry.weight();
            if (roll < accumulated) {
                return copyOre(entry, random);
            }
        }
        return copyOre(ores.getLast(), random);
    }

    private ItemStack copyOre(OreEntry entry, Random random) {
        ItemStack[] matches = entry.ingredient().getItems();
        if (matches.length == 0) return ItemStack.EMPTY;
        ItemStack ore = matches[random.nextInt(matches.length)].copy();
        ore.setCount(1);
        return ore;
    }
}
