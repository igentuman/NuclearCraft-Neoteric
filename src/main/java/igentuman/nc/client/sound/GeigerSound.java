package igentuman.nc.client.sound;

import igentuman.nc.radiation.client.ClientRadiationData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.antlr.v4.runtime.misc.NotNull;;

import static igentuman.nc.setup.registration.NCItems.GEIGER_COUNTER;
import static igentuman.nc.setup.registration.NCSounds.GEIGER_SOUNDS;

public class GeigerSound extends PlayerSound {

    public int radiationLevel = 0;
    public static GeigerSound create(@NotNull PlayerEntity player) {
        if(!playerHasGeigerCounter(player)) return null;
        ClientRadiationData.setCurrentChunk(player.xChunk, player.zChunk);
        int level =Math.max(0, Math.min(5, (int)((float)ClientRadiationData.getCurrentWorldRadiation()/200000)));
        if(level == 0) return null;
        return new GeigerSound(player, level);
    }

    private static boolean playerHasGeigerCounter(PlayerEntity player) {
        return player.inventory.contains(new ItemStack(GEIGER_COUNTER.get()));
    }

    private GeigerSound(@NotNull PlayerEntity player, int level) {
        super(player, GEIGER_SOUNDS.get(level-1).get());
        radiationLevel = level;
        setFade(1, 1);
    }

    @Override
    public boolean shouldPlaySound(@NotNull PlayerEntity player) {
        return radiationLevel > 0;
    }

    @Override
    public float getVolume() {
        return super.getVolume() * 0.5F;
    }
}