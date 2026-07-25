package igentuman.nc.item;

import igentuman.nc.config.Common;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Rarity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.ArrayList;
import java.util.List;

public final class CrystalAnalysis {

    private CrystalAnalysis() {
    }

    private static volatile List<ResourceLocation> cachedPool;
    private static volatile List<? extends String> cachedBlacklist;

    public static void applyAnalysis(ItemStack out, RandomSource rng) {
        ShardRarity rarity = ShardRarity.roll(rng);
        ResourceLocation effectId = pickEffect(rng);
        CompoundTag tag = getTag(out);
        tag.putBoolean(ResoniteCrystalItem.TAG_ANALYZED, true);
        tag.putByte(ResoniteCrystalItem.TAG_RARITY, (byte) rarity.ordinal());
        tag.putString(ResoniteCrystalItem.TAG_EFFECT, effectId.toString());
        out.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        out.set(DataComponents.RARITY, rarity.vanilla);
    }

    public static ResourceLocation pickEffect(RandomSource rng) {
        List<ResourceLocation> pool = pool();
        if (pool.isEmpty()) {
            return BuiltInRegistries.MOB_EFFECT.getKey(MobEffects.LUCK.value());
        }
        return pool.get(rng.nextInt(pool.size()));
    }

    private static synchronized List<ResourceLocation> pool() {
        List<? extends String> blacklist = Common.ANOMALY_CONFIG.BUFF_EFFECT_BLACKLIST.get();
        if (cachedPool != null && blacklist.equals(cachedBlacklist)) {
            return cachedPool;
        }
        List<ResourceLocation> pool = new ArrayList<>();
        for (MobEffect effect : BuiltInRegistries.MOB_EFFECT) {
            if (effect.getCategory() != MobEffectCategory.BENEFICIAL) {
                continue;
            }
            ResourceLocation id = BuiltInRegistries.MOB_EFFECT.getKey(effect);
            if (id == null || blacklist.contains(id.toString())) {
                continue;
            }
            pool.add(id);
        }
        cachedPool = pool;
        cachedBlacklist = new ArrayList<>(blacklist);
        return pool;
    }

    static CompoundTag getTag(ItemStack stack) {
        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        return cd != null ? cd.copyTag() : new CompoundTag();
    }
}
