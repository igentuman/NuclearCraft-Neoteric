package igentuman.nc.item;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Rarity;

public enum ShardRarity {
    COMMON   (0, 100,     Rarity.COMMON, 0xFFFFFF),
    RARE     (1, 500,   Rarity.RARE,   0x55FFFF),
    EPIC     (2, 1000,  Rarity.EPIC,   0xFF55FF),
    LEGENDARY(3, 5000, Rarity.EPIC,   0xFFAA00);

    public final int amplifier;
    public final int fePerTick;
    public final Rarity vanilla;
    public final int color;

    private static final ShardRarity[] VALUES = values();

    ShardRarity(int amplifier, int fePerTick, Rarity vanilla, int color) {
        this.amplifier = amplifier;
        this.fePerTick = fePerTick;
        this.vanilla = vanilla;
        this.color = color;
    }

    public static ShardRarity byOrdinal(int ordinal) {
        return (ordinal < 0 || ordinal >= VALUES.length) ? COMMON : VALUES[ordinal];
    }

    public static ShardRarity roll(RandomSource rng, int wCommon, int wRare, int wEpic, int wLegendary) {
        int[] weights = {wCommon, wRare, wEpic, wLegendary};
        int total = 0;
        for (int w : weights) {
            total += Math.max(0, w);
        }
        if (total <= 0) {
            return COMMON;
        }
        int r = rng.nextInt(total);
        int acc = 0;
        for (int i = 0; i < VALUES.length; i++) {
            acc += Math.max(0, weights[i]);
            if (r < acc) {
                return VALUES[i];
            }
        }
        return COMMON;
    }
}
