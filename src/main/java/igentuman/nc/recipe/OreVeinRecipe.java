package igentuman.nc.recipe;

import igentuman.nc.config.Common;
import igentuman.nc.setup.ModEntries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

import java.util.List;
import java.util.Random;

/** Custom recipe defining a weighted ore pool for a virtual vein type, with score-based random ore selection. */
public class OreVeinRecipe implements MachineRecipe<ProcessorRecipeInput> {

    private final Identifier id;
    private final List<OreEntry> ores;
    private final double rarityModifier;

    public OreVeinRecipe(Identifier id, List<OreEntry> ores, double rarityModifier) {
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
    public ItemStack assemble(ProcessorRecipeInput input) {
        return ItemStack.EMPTY;
    }

    @Override
    @SuppressWarnings("unchecked")
    public RecipeSerializer<OreVeinRecipe> getSerializer() {
        return (RecipeSerializer<OreVeinRecipe>) ModEntries.get("nc_ore_veins").recipeSerializer().get();
    }

    @Override
    @SuppressWarnings("unchecked")
    public RecipeType<OreVeinRecipe> getType() {
        return (RecipeType<OreVeinRecipe>) ModEntries.get("nc_ore_veins").recipeType().get();
    }

    public Identifier getId() {
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
            return copyOre(entry, random, level);
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
                return copyOre(entry, random, level);
            }
        }
        return copyOre(ores.getLast(), random, level);
    }

    private ItemStack copyOre(OreEntry entry, Random random, ServerLevel level) {
        List<ItemStack> matches = entry.ingredient().ingredient().display()
                .resolveForStacks(SlotDisplayContext.fromLevel(level)).stream()
                .filter(stack -> !stack.isEmpty()).toList();
        if (matches.isEmpty()) return ItemStack.EMPTY;
        return matches.get(random.nextInt(matches.size())).copyWithCount(1);
    }
}
