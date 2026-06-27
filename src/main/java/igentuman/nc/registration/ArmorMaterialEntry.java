package igentuman.nc.registration;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static igentuman.nc.Main.MODID;
import static igentuman.nc.setup.Registers.ARMOR_MATERIALS;

public class ArmorMaterialEntry {

    public final String name;
    private final DeferredHolder<ArmorMaterial, ArmorMaterial> holder;
    private final int durabilityMultiplier;

    private ArmorMaterialEntry(String name, DeferredHolder<ArmorMaterial, ArmorMaterial> holder, int durabilityMultiplier) {
        this.name = name;
        this.holder = holder;
        this.durabilityMultiplier = durabilityMultiplier;
    }

    public Holder<ArmorMaterial> holder() { return holder; }
    public int durabilityMultiplier() { return durabilityMultiplier; }

    public static Builder builder(String name) {
        return new Builder(name);
    }

    public static class Builder {
        private final String name;
        private final EnumMap<ArmorItem.Type, Integer> defense = new EnumMap<>(ArmorItem.Type.class);
        private int enchantmentValue = 9;
        private Holder<SoundEvent> equipSound = SoundEvents.ARMOR_EQUIP_IRON;
        private Supplier<Ingredient> repairIngredient = () -> Ingredient.EMPTY;
        private float toughness = 0.0F;
        private float knockbackResistance = 0.0F;
        private int durabilityMultiplier = 15;
        private final List<ArmorMaterial.Layer> layers = new ArrayList<>();

        private Builder(String name) {
            this.name = name;
            for (ArmorItem.Type t : ArmorItem.Type.values()) defense.put(t, 0);
        }

        public Builder defense(int boots, int leggings, int chestplate, int helmet) {
            defense.put(ArmorItem.Type.BOOTS, boots);
            defense.put(ArmorItem.Type.LEGGINGS, leggings);
            defense.put(ArmorItem.Type.CHESTPLATE, chestplate);
            defense.put(ArmorItem.Type.HELMET, helmet);
            defense.put(ArmorItem.Type.BODY, chestplate);
            return this;
        }

        public Builder bodyDefense(int body) {
            defense.put(ArmorItem.Type.BODY, body);
            return this;
        }

        public Builder enchantmentValue(int v) { this.enchantmentValue = v; return this; }
        public Builder equipSound(Holder<SoundEvent> s) { this.equipSound = s; return this; }
        public Builder toughness(float v) { this.toughness = v; return this; }
        public Builder knockbackResistance(float v) { this.knockbackResistance = v; return this; }
        public Builder durabilityMultiplier(int v) { this.durabilityMultiplier = v; return this; }

        public Builder repairItem(Supplier<Ingredient> ing) { this.repairIngredient = ing; return this; }
        public Builder repairItem(Item item) { this.repairIngredient = () -> Ingredient.of(item); return this; }

        public Builder layer(ResourceLocation assetName) {
            layers.add(new ArmorMaterial.Layer(assetName));
            return this;
        }

        public Builder layer(ResourceLocation assetName, String suffix, boolean dyeable) {
            layers.add(new ArmorMaterial.Layer(assetName, suffix, dyeable));
            return this;
        }

        public ArmorMaterialEntry build() {
            List<ArmorMaterial.Layer> finalLayers = layers.isEmpty()
                    ? List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(MODID, name)))
                    : List.copyOf(layers);
            Map<ArmorItem.Type, Integer> defenseCopy = new EnumMap<>(defense);
            int ev = enchantmentValue;
            Holder<SoundEvent> sound = equipSound;
            Supplier<Ingredient> repair = repairIngredient;
            float t = toughness;
            float kb = knockbackResistance;
            DeferredHolder<ArmorMaterial, ArmorMaterial> holder = ARMOR_MATERIALS.register(name,
                    () -> new ArmorMaterial(defenseCopy, ev, sound, repair, finalLayers, t, kb));
            return new ArmorMaterialEntry(name, holder, durabilityMultiplier);
        }
    }
}
