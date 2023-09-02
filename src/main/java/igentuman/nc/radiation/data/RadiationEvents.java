package igentuman.nc.radiation.data;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;

import static igentuman.nc.NuclearCraft.MODID;

public class RadiationEvents {

    public static boolean isTracking = false;

    public static void attachWorldRadiation(final AttachCapabilitiesEvent<Level> event) {
        if (!event.getObject().getCapability(WorldRadiationProvider.WORLD_RADIATION).isPresent()) {
            event.addCapability(new ResourceLocation(MODID, "radiation"), new WorldRadiationProvider());
            isTracking = true;
        }
    }

    public static void attachPlayerRadiation(final AttachCapabilitiesEvent<Entity> event) {
        if(event.getObject() instanceof Player) {
            if (!event.getObject().getCapability(PlayerRadiationProvider.PLAYER_RADIATION).isPresent()) {
                event.addCapability(new ResourceLocation(MODID, "radiation"), new PlayerRadiationProvider());
            }
        }
    }

    public static void onPlayerCloned(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            // We need to copyFrom the capabilities
            event.getOriginal().getCapability(PlayerRadiationProvider.PLAYER_RADIATION).ifPresent(oldStore -> {
                event.getEntity().getCapability(PlayerRadiationProvider.PLAYER_RADIATION).ifPresent(newStore -> {
                    newStore.copyFrom(oldStore);
                });
            });
        }
    }

    public static void onWorldTick(TickEvent.LevelTickEvent event) {
        if(!isTracking) {
            return;
        }
        // Don't do anything client side
        if (event.level.isClientSide) {
            return;
        }
        if (event.phase == TickEvent.Phase.START) {
            return;
        }
        RadiationManager manager = RadiationManager.get(event.level);
        manager.tick(event.level);
    }

    public static void stopTracking() {
        isTracking = false;
    }

    public static void startTracking() {
        isTracking = true;
    }
}
