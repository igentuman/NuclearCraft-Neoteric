package igentuman.nc.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

import static igentuman.nc.handler.config.CommonConfig.ANOMALY_CONFIG;

public final class CrystalAnalysis {

    private CrystalAnalysis() {
    }

    private static List<MobEffect> cachedPool;
    private static List<? extends String> cachedBlacklist;

    /** Rolls rarity + effect and writes the analyzed NBT onto {@code out}. */
    public static void applyAnalysis(ItemStack out, RandomSource rng) {
        ShardRarity rarity = ShardRarity.roll(rng,
                ANOMALY_CONFIG.RARITY_WEIGHT_COMMON.get(),
                ANOMALY_CONFIG.RARITY_WEIGHT_RARE.get(),
                ANOMALY_CONFIG.RARITY_WEIGHT_EPIC.get(),
                ANOMALY_CONFIG.RARITY_WEIGHT_LEGENDARY.get());
        MobEffect effect = pickEffect(rng);
        ResourceLocation id = ForgeRegistries.MOB_EFFECTS.getKey(effect);
        out.getOrCreateTag().putBoolean(ResoniteCrystalItem.TAG_ANALYZED, true);
        out.getOrCreateTag().putByte(ResoniteCrystalItem.TAG_RARITY, (byte) rarity.ordinal());
        out.getOrCreateTag().putString(ResoniteCrystalItem.TAG_EFFECT,
                id != null ? id.toString() : "minecraft:luck");
    }

    public static MobEffect pickEffect(RandomSource rng) {
        List<MobEffect> pool = pool();
        return pool.isEmpty() ? MobEffects.LUCK : pool.get(rng.nextInt(pool.size()));
    }

    private static synchronized List<MobEffect> pool() {
        List<? extends String> blacklist = ANOMALY_CONFIG.BUFF_EFFECT_BLACKLIST.get();
        if (cachedPool != null && blacklist.equals(cachedBlacklist)) {
            return cachedPool;
        }
        List<MobEffect> pool = new ArrayList<>();
        for (MobEffect effect : ForgeRegistries.MOB_EFFECTS) {
            if (effect.getCategory() != MobEffectCategory.BENEFICIAL) {
                continue;
            }
            ResourceLocation id = ForgeRegistries.MOB_EFFECTS.getKey(effect);
            if (id == null || blacklist.contains(id.toString())) {
                continue;
            }
            pool.add(effect);
        }
        cachedPool = pool;
        cachedBlacklist = new ArrayList<>(blacklist);
        return pool;
    }
}
