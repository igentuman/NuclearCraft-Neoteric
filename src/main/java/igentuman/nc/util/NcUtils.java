package igentuman.nc.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import igentuman.nc.NuclearCraft;
import igentuman.nc.util.math.FloatingLong;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import javax.annotation.Nullable;

import java.util.*;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public final class NcUtils {

    public static final float ONE_OVER_ROOT_TWO = (float) (1 / Math.sqrt(2));

    private static final List<UUID> warnedFails = new ArrayList<>();

    //TODO: Evaluate adding an extra optional param to shrink and grow stack that allows for logging if it is mismatched. Defaults to false
    // Deciding on how to implement it into the API will need more thought as we want to keep overriding implementations as simple as
    // possible, and also ideally would use our normal logger instead of the API logger
    public static void logMismatchedStackSize(long actual, long expected) {
        if (expected != actual) {
            NuclearCraft.LOGGER.error("Stack size changed by a different amount ({}) than requested ({}).", actual, expected, new Exception());
        }
    }

    public static void logExpectedZero(FloatingLong actual) {
        if (!actual.isZero()) {
            NuclearCraft.LOGGER.error("Energy value changed by a different amount ({}) than requested (zero).", actual, new Exception());
        }
    }


    public static ItemStack getItemInHand(LivingEntity entity, HumanoidArm side) {
        if (entity instanceof Player player) {
            return getItemInHand(player, side);
        } else if (side == HumanoidArm.RIGHT) {
            return entity.getMainHandItem();
        }
        return entity.getOffhandItem();
    }

    public static ItemStack getItemInHand(Player player, HumanoidArm side) {
        if (player.getMainArm() == side) {
            return player.getMainHandItem();
        }
        return player.getOffhandItem();
    }

    public static int redstoneLevelFromContents(long amount, long capacity) {
        double fractionFull = capacity == 0 ? 0 : amount / (double) capacity;
        return Mth.floor((float) (fractionFull * 14.0F)) + (fractionFull > 0 ? 1 : 0);
    }

    /**
     * Calculates the redstone level based on the percentage of amount stored.
     *
     * @param amount   Amount currently stored
     * @param capacity Total amount that can be stored.
     *
     * @return A redstone level based on the percentage of the amount stored.
     */
    public static int redstoneLevelFromContents(FloatingLong amount, FloatingLong capacity) {
        if (capacity.isZero() || amount.isZero()) {
            return 0;
        }
        return 1 + amount.divide(capacity).multiply(14).intValue();
    }


    /**
     * Checks whether the player is in creative or spectator mode.
     *
     * @param player the player to check.
     *
     * @return true if the player is neither in creative mode, nor in spectator mode.
     */
    public static boolean isPlayingMode(Player player) {
        return !player.isCreative() && !player.isSpectator();
    }

    /**
     * Helper to read the parameter names from the format saved by our annotation processor param name mapper.
     */
    public static List<String> getParameterNames(@Nullable JsonObject classMethods, String method, String signature) {
        if (classMethods != null) {
            JsonObject signatures = classMethods.getAsJsonObject(method);
            if (signatures != null) {
                JsonElement params = signatures.get(signature);
                if (params != null) {
                    if (params.isJsonArray()) {
                        JsonArray paramArray = params.getAsJsonArray();
                        List<String> paramNames = new ArrayList<>(paramArray.size());
                        for (JsonElement param : paramArray) {
                            paramNames.add(param.getAsString());
                        }
                        return Collections.unmodifiableList(paramNames);
                    }
                    return Collections.singletonList(params.getAsString());
                }
            }
        }
        return Collections.emptyList();
    }


}