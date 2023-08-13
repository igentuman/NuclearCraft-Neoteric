package igentuman.nc.setup.registration;

import igentuman.nc.setup.recipes.WrappedRegistryObject;
import igentuman.nc.util.annotation.NothingNullByDefault;
import net.minecraft.Util;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.RegistryObject;
@NothingNullByDefault
public class SoundEventRegistryObject<SOUND extends SoundEvent> extends WrappedRegistryObject<SOUND> {

    private final String translationKey;

    public SoundEventRegistryObject(RegistryObject<SOUND> registryObject) {
        super(registryObject);
        translationKey = Util.makeDescriptionId("sound_event", this.registryObject.getId());
    }

}