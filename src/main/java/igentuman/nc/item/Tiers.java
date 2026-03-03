package igentuman.nc.item;

import java.util.function.Supplier;

import com.google.common.base.Suppliers;
import igentuman.nc.content.materials.Materials;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

import static igentuman.nc.datagen.recipes.recipes.AbstractRecipeProvider.ingotIngredient;
import static igentuman.nc.setup.registration.NCItems.LITHIUM_ION_CELL;

public enum Tiers implements Tier {

   TOUGH(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 10000, 12.0F, 10.0F, 22, () -> {
      return ingotIngredient(Materials.tough_alloy).asIngredient();
   }),
   THORIUM(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 10000, 10.0F, 6.0F, 18, () -> {
      return ingotIngredient(Materials.thorium).asIngredient();
   }),
   QNP(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 50000, 20.0F, 14.0F, 25, () -> {
      return Ingredient.of(LITHIUM_ION_CELL.get());
   });

   private final TagKey<Block> incorrectBlocksForDrops;
   private final int uses;
   private final float speed;
   private final float damage;
   private final int enchantmentValue;
   private final Supplier<Ingredient> repairIngredient;

   private Tiers(TagKey<Block> pIncorrectBlocks, int pUses, float pSpeed, float pDamage, int pEnchantmentValue, Supplier<Ingredient> pRepairIngredient) {
      this.incorrectBlocksForDrops = pIncorrectBlocks;
      this.uses = pUses;
      this.speed = pSpeed;
      this.damage = pDamage;
      this.enchantmentValue = pEnchantmentValue;
      this.repairIngredient = Suppliers.memoize(pRepairIngredient::get);
   }

   public int getUses() {
      return this.uses;
   }

   public float getSpeed() {
      return this.speed;
   }

   public float getAttackDamageBonus() {
      return this.damage;
   }

   @Override
   public TagKey<Block> getIncorrectBlocksForDrops() {
      return this.incorrectBlocksForDrops;
   }

   public int getEnchantmentValue() {
      return this.enchantmentValue;
   }

   public Ingredient getRepairIngredient() {
      return this.repairIngredient.get();
   }
}
