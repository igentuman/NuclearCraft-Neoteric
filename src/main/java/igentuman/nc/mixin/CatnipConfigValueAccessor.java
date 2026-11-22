package igentuman.nc.mixin;

import net.createmod.catnip.api.config.definition.ConfigValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Exposes the backing value for Catnip's initial config load. */
@Mixin(value = ConfigValue.class, remap = false)
public interface CatnipConfigValueAccessor {
    @Accessor("value")
    Object nc$getValue();

    @Accessor("value")
    void nc$setValue(Object value);
}
