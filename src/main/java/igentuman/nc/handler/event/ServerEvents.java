package igentuman.nc.handler.event;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import static igentuman.nc.NuclearCraft.TICK_COUNTER;

/** Server event subscriber that advances the global tick counter each server tick. */
public class ServerEvents {

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        TICK_COUNTER++;
    }

}
