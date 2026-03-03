package igentuman.nc.registry;

import igentuman.nc.util.annotation.NothingNullByDefault;
import net.minecraft.Util;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

@NothingNullByDefault
public class SoundEventRegistryObject<SOUND extends SoundEvent> extends WrappedRegistryObject<SOUND> {

    private final String translationKey;

    public SoundEventRegistryObject(DeferredHolder<?, ?> registryObject) {
        super(registryObject);
        translationKey = Util.makeDescriptionId("sound_event", registryObject.getId());
    }
}
