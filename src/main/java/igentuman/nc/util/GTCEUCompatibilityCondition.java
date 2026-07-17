package igentuman.nc.util;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.block.entity.NuclearCraftBE.isGTEUCapEnabled;
import static igentuman.nc.handler.config.WorldConfig.DIMENSION_CONFIG;
import static igentuman.nc.util.ModUtil.isGtLoaded;

public class GTCEUCompatibilityCondition implements ICondition {
    private static final ResourceLocation NAME = rl("gtceu_compat_enabled");

    public GTCEUCompatibilityCondition() {

    }

    public ResourceLocation getID() {
        return NAME;
    }

    public boolean test(IContext context) {
        return isGtLoaded() && isGTEUCapEnabled();
    }

    public String toString() {
        return "is GT loaded and configured for compatibility";
    }

    public static class Serializer implements IConditionSerializer<GTCEUCompatibilityCondition> {
        public static final Serializer INSTANCE = new Serializer();

        public Serializer() {
        }

        public void write(JsonObject json, GTCEUCompatibilityCondition value) {

        }

        public GTCEUCompatibilityCondition read(JsonObject json) {
            return new GTCEUCompatibilityCondition();
        }

        public ResourceLocation getID() {
            return GTCEUCompatibilityCondition.NAME;
        }
    }
}