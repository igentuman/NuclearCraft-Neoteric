package igentuman.api.platform;

import com.mojang.serialization.MapCodec;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

/**
 * Platform wrapper for recipe/data condition registration.
 * In 1.21.1: IConditionSerializer is gone, CraftingHelper.register() is gone.
 * Conditions now require a MapCodec and are registered via DeferredRegister
 * into NeoForgeRegistries.Keys.CONDITION_CODECS.
 */
public final class NCConditions {
    private NCConditions() {}

    /**
     * Creates a DeferredRegister for condition codecs.
     */
    public static DeferredRegister<MapCodec<? extends ICondition>> createRegistry(String modId) {
        return DeferredRegister.create(NeoForgeRegistries.Keys.CONDITION_CODECS, modId);
    }

    /**
     * Registers a condition codec with the given name.
     */
    public static <T extends ICondition> void register(
            DeferredRegister<MapCodec<? extends ICondition>> registry,
            String name,
            Supplier<MapCodec<T>> codecSupplier) {
        registry.register(name, codecSupplier);
    }
}
