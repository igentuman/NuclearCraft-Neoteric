package igentuman.nc.util;

import com.mojang.serialization.MapCodec;
import net.neoforged.neoforge.common.conditions.ICondition;

import static igentuman.nc.handler.config.WorldConfig.DIMENSION_CONFIG;

public class WastelandEnabledCondition implements ICondition {
    public static final WastelandEnabledCondition INSTANCE = new WastelandEnabledCondition();

    public static final MapCodec<WastelandEnabledCondition> CODEC = MapCodec.unit(INSTANCE).stable();

    public WastelandEnabledCondition() {}

    @Override
    public boolean test(IContext context) {
        return DIMENSION_CONFIG.registerWasteland.get();
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }

    @Override
    public String toString() {
        return "is wasteland dim enabled in config";
    }
}
