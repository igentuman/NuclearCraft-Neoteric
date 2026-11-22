package igentuman.nc.registration;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

import java.util.EnumMap;
import java.util.Map;

import static igentuman.nc.NuclearCraft.rl;

/** Builds an armor material's stats, repair tag and equipment asset without a material registry. */
public class ArmorMaterialEntry {

    public final String name;
    private final ArmorMaterial material;

    private ArmorMaterialEntry(String name, ArmorMaterial material) {
        this.name = name;
        this.material = material;
    }

    public ArmorMaterial material() { return material; }

    public static Builder builder(String name) {
        return new Builder(name);
    }

    public static class Builder {
        private final String name;
        private final EnumMap<ArmorType, Integer> defense = new EnumMap<>(ArmorType.class);
        private int enchantmentValue = 9;
        private Holder<SoundEvent> equipSound = SoundEvents.ARMOR_EQUIP_IRON;
        private TagKey<Item> repairIngredient;
        private float toughness = 0.0F;
        private float knockbackResistance = 0.0F;
        private int durabilityMultiplier = 15;
        private ResourceKey<EquipmentAsset> asset;

        private Builder(String name) {
            this.name = name;
            this.asset = ResourceKey.create(EquipmentAssets.ROOT_ID, rl(name));
            for (ArmorType type : ArmorType.values()) defense.put(type, 0);
        }

        public Builder defense(int boots, int leggings, int chestplate, int helmet) {
            defense.put(ArmorType.BOOTS, boots);
            defense.put(ArmorType.LEGGINGS, leggings);
            defense.put(ArmorType.CHESTPLATE, chestplate);
            defense.put(ArmorType.HELMET, helmet);
            defense.put(ArmorType.BODY, chestplate);
            return this;
        }

        public Builder bodyDefense(int body) {
            defense.put(ArmorType.BODY, body);
            return this;
        }

        public Builder enchantmentValue(int value) { this.enchantmentValue = value; return this; }
        public Builder equipSound(Holder<SoundEvent> sound) { this.equipSound = sound; return this; }
        public Builder toughness(float value) { this.toughness = value; return this; }
        public Builder knockbackResistance(float value) { this.knockbackResistance = value; return this; }
        public Builder durabilityMultiplier(int value) { this.durabilityMultiplier = value; return this; }
        public Builder repairItem(TagKey<Item> tag) { this.repairIngredient = tag; return this; }

        public Builder asset(Identifier assetName) {
            this.asset = ResourceKey.create(EquipmentAssets.ROOT_ID, assetName);
            return this;
        }

        public ArmorMaterialEntry build() {
            if (repairIngredient == null) throw new IllegalStateException("Missing repair tag for armor " + name);
            return new ArmorMaterialEntry(name, new ArmorMaterial(durabilityMultiplier, Map.copyOf(defense),
                    enchantmentValue, equipSound, toughness, knockbackResistance, repairIngredient, asset));
        }
    }
}
