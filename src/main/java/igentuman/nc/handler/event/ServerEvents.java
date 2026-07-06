package igentuman.nc.handler.event;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import static igentuman.nc.NuclearCraft.TICK_COUNTER;

public class ServerEvents {

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        TICK_COUNTER++;
    }

}
