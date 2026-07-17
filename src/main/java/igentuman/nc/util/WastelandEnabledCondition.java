package igentuman.nc.util;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;
import net.minecraftforge.fml.ModList;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.handler.config.WorldConfig.DIMENSION_CONFIG;

public class WastelandEnabledCondition implements ICondition {
    private static final ResourceLocation NAME = rl("wasteland_enabled");

    public WastelandEnabledCondition() {

    }

    public ResourceLocation getID() {
        return NAME;
    }

    public boolean test(ICondition.IContext context) {
        return DIMENSION_CONFIG.registerWasteland.get();
    }

    public String toString() {
        return "is wasteland dim enabled in config";
    }

    public static class Serializer implements IConditionSerializer<WastelandEnabledCondition> {
        public static final Serializer INSTANCE = new Serializer();

        public Serializer() {
        }

        public void write(JsonObject json, WastelandEnabledCondition value) {

        }

        public WastelandEnabledCondition read(JsonObject json) {
            return new WastelandEnabledCondition();
        }

        public ResourceLocation getID() {
            return WastelandEnabledCondition.NAME;
        }
    }
}