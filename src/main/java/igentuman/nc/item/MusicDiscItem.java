package igentuman.nc.item;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.RecordItem;

import java.util.function.Supplier;

public class MusicDiscItem extends RecordItem {

    public MusicDiscItem(int pAnalogOutput, Supplier<SoundEvent> pSound, Properties pProperties, int pLengthInSeconds) {
        super(pAnalogOutput, pSound, pProperties.rarity(Rarity.RARE).stacksTo(1), pLengthInSeconds);
    }
}
