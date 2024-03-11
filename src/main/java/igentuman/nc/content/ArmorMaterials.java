package igentuman.nc.content;

import igentuman.nc.setup.registration.NCItems;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;

import java.util.function.Supplier;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.setup.registration.NCItems.NC_INGOTS;
import static igentuman.nc.setup.registration.NCItems.NC_SHIELDING;

public enum ArmorMaterials implements IArmorMaterial {
   HAZMAT(MODID+":hazmat", 5, new int[]{1, 2, 3, 1}, 15, SoundEvents.ARMOR_EQUIP_LEATHER, 0.0F, 0.0F, () -> {
      return Ingredient.of(NCItems.NC_ITEMS.get("bioplastic").get());
   }),
   TOUGH(MODID+":tough", 33, new int[]{3, 6, 8, 3}, 15, SoundEvents.ARMOR_EQUIP_DIAMOND, 3.5F, 0.2F, () -> {
      return Ingredient.of(NC_INGOTS.get("tough_alloy").get());
   }),
   HEV(MODID+":hev", 37, new int[]{3, 5, 7, 3}, 25, SoundEvents.ARMOR_EQUIP_NETHERITE, 4.0F, 0.3F, () -> {
      return Ingredient.of(NC_SHIELDING.get("dps").get());
   });

   private static final int[] HEALTH_PER_SLOT = new int[]{13, 15, 16, 11};
   private final String name;
   private final int durabilityMultiplier;
   private final int[] slotProtections;
   private final int enchantmentValue;
   private final float toughness;
   private final float knockbackResistance;

   private final Supplier<Ingredient> repairIngredient;

   private ArmorMaterials(String pName, int pDurabilityMultiplier, int[] pSlotProtections, int pEnchantmentValue, SoundEvent pSound, float pToughness, float pKnockbackResistance, Supplier<Ingredient> pRepairIngredient) {
      this.name = pName;
      this.durabilityMultiplier = pDurabilityMultiplier;
      this.slotProtections = pSlotProtections;
      this.enchantmentValue = pEnchantmentValue;
      this.toughness = pToughness;
      this.knockbackResistance = pKnockbackResistance;
      this.repairIngredient = pRepairIngredient;
   }

   @Override
   public int getDurabilityForSlot(EquipmentSlotType equipmentSlotType) {
      return HEALTH_PER_SLOT[equipmentSlotType.getIndex()] * this.durabilityMultiplier;

   }

   @Override
   public int getDefenseForSlot(EquipmentSlotType equipmentSlotType) {
      return this.slotProtections[equipmentSlotType.getIndex()];
   }

   public int getEnchantmentValue() {
      return this.enchantmentValue;
   }

   @Override
   public SoundEvent getEquipSound() {
      return null;
   }

   @Override
   public Ingredient getRepairIngredient() {
      return repairIngredient.get();
   }

   public String getName() {
      return this.name;
   }

   public float getToughness() {
      return this.toughness;
   }

   public float getKnockbackResistance() {
      return this.knockbackResistance;
   }
}