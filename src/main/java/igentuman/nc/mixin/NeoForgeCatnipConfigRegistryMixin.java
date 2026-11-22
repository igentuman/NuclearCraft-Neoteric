package igentuman.nc.mixin;

import com.electronwill.nightconfig.core.EnumGetMethod;
import net.createmod.catnip.api.config.CatnipConfigRegistry;
import net.createmod.catnip.api.config.ConfigId;
import net.createmod.catnip.api.config.definition.CatnipConfig;
import net.createmod.catnip.api.config.definition.ConfigValue;
import net.createmod.catnip.impl.neoforge.service.NeoForgeCatnipConfigRegistry;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Allows Catnip 1.0.52 to load the client configs registered early by
 * {@link igentuman.nc.compat.ponder.PonderConfigFix}.
 */
@Mixin(value = NeoForgeCatnipConfigRegistry.class, remap = false)
public class NeoForgeCatnipConfigRegistryMixin {

    @Inject(method = "register", at = @At("HEAD"), cancellable = true)
    private void nc$skipDuplicateRegistration(ConfigId id, CatnipConfig.Root config, CallbackInfo ci) {
        if (((CatnipConfigRegistry) (Object) this).get(id).filter(existing -> existing == config).isPresent()) {
            ci.cancel();
        }
    }

    @Redirect(method = "define", at = @At(value = "INVOKE",
            target = "Lnet/neoforged/neoforge/common/ModConfigSpec$Builder;define(Ljava/util/List;Ljava/util/function/Supplier;Ljava/util/function/Predicate;Ljava/lang/Class;)Lnet/neoforged/neoforge/common/ModConfigSpec$ConfigValue;"))
    private static ModConfigSpec.ConfigValue<?> nc$defineConfigValue(ModConfigSpec.Builder builder,
            List<String> path, Supplier<?> defaultValue, Predicate<Object> validator, Class<?> type) {
        if (defaultValue.get() instanceof Enum<?> constant) {
            return nc$defineEnum(builder, path, constant, validator);
        }
        if (type == Float.class) {
            // NightConfig reads TOML decimals as doubles, while Catnip validates floats.
            return builder.define(path, defaultValue,
                    raw -> raw instanceof Number number && validator.test(number.floatValue()), type);
        }
        return builder.define(path, defaultValue, raw -> type.isInstance(raw) && validator.test(raw), type);
    }

    @Unique
    private static <E extends Enum<E>> ModConfigSpec.EnumValue<E> nc$defineEnum(ModConfigSpec.Builder builder,
            List<String> path, Enum<E> constant, Predicate<Object> validator) {
        Class<E> type = constant.getDeclaringClass();
        EnumGetMethod converter = EnumGetMethod.NAME_IGNORECASE;
        return builder.defineEnum(path, () -> type.cast(constant), converter,
                raw -> converter.validate(raw, type) && validator.test(converter.get(raw, type)), type);
    }

    @Redirect(method = "lambda$define$2", at = @At(value = "INVOKE",
            target = "Lnet/createmod/catnip/api/config/definition/ConfigValue;set(Ljava/lang/Object;)V"))
    private static void nc$syncConfigValue(ConfigValue<Object> value, Object loadedValue) {
        if (value.defaultValue instanceof Float && loadedValue instanceof Number number) {
            loadedValue = number.floatValue();
        }

        var accessor = (CatnipConfigValueAccessor) (Object) value;
        if (accessor.nc$getValue() == null) {
            // Catnip's setter requires an existing value and its change callback writes to disk.
            accessor.nc$setValue(loadedValue);
            return;
        }
        value.set(loadedValue);
    }
}
