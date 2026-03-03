package igentuman.nc.util;

import com.mojang.serialization.MapCodec;
import net.neoforged.neoforge.common.conditions.ICondition;

import static igentuman.nc.block.entity.NuclearCraftBE.isGTEUCapEnabled;
import static igentuman.nc.util.ModUtil.isGtLoaded;

public class GTCEUCompatibilityCondition implements ICondition {
    public static final GTCEUCompatibilityCondition INSTANCE = new GTCEUCompatibilityCondition();

    public static final MapCodec<GTCEUCompatibilityCondition> CODEC = MapCodec.unit(INSTANCE).stable();

    public GTCEUCompatibilityCondition() {}

    @Override
    public boolean test(IContext context) {
        return isGtLoaded() && isGTEUCapEnabled();
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }

    @Override
    public String toString() {
        return "is GT loaded and configured for compatibility";
    }
}
