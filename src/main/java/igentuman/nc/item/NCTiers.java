package igentuman.nc.item;

import igentuman.nc.setup.ModEntries;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;

public final class NCTiers {

    private NCTiers() {}

    public static final Tier TOUGH = new NCTier(
            10000, 12.0F, 10.0F, 22,
            BlockTags.INCORRECT_FOR_NETHERITE_TOOL,
            () -> Ingredient.of(ModEntries.get("tough_alloy").materialEntry().ingot().get()));

    public static final Tier THORIUM = new NCTier(
            10000, 10.0F, 6.0F, 18,
            BlockTags.INCORRECT_FOR_NETHERITE_TOOL,
            () -> Ingredient.of(ModEntries.get("thorium").materialEntry().ingot().get()));

    private record NCTier(int uses, float speed, float attackDamageBonus, int enchantmentValue,
                          TagKey<Block> incorrectBlocks, Supplier<Ingredient> repair) implements Tier {

        @Override public int getUses() { return uses; }
        @Override public float getSpeed() { return speed; }
        @Override public float getAttackDamageBonus() { return attackDamageBonus; }
        @Override public TagKey<Block> getIncorrectBlocksForDrops() { return incorrectBlocks; }
        @Override public int getEnchantmentValue() { return enchantmentValue; }
        @Override public Ingredient getRepairIngredient() { return repair.get(); }
    }
}
