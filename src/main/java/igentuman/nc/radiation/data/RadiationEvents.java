package igentuman.nc.radiation.data;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;

import static igentuman.nc.NuclearCraft.MODID;

public class RadiationEvents {

    public static void attacWorldRadiation(final AttachCapabilitiesEvent<Level> event) {
        if (!event.getObject().getCapability(WorldRadiationProvider.WORLD_RADIATION).isPresent()) {
            event.addCapability(new ResourceLocation(MODID, "radiation"), new WorldRadiationProvider());
        }
    }

    public static void onWorldTick(TickEvent.LevelTickEvent event) {
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
}
