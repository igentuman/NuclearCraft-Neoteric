package igentuman.nc.handler.event;

import igentuman.nc.pipe.PipeNetworkManager;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import static igentuman.nc.NuclearCraft.TICK_COUNTER;
import static igentuman.nc.config.Common.PIPE_TRANSFER_INTERVAL_TICKS;

public class PipeEvents {

    @SubscribeEvent
    public void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        int interval = Math.max(1, PIPE_TRANSFER_INTERVAL_TICKS.get());
        if (TICK_COUNTER % interval == 0) {
            PipeNetworkManager.get(level.dimension()).tickTransfers(level);
        }
    }

    @SubscribeEvent
    public void onChunkLoad(ChunkEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel level) {
            PipeNetworkManager.get(level.dimension()).onChunkLoaded(level, event.getChunk().getPos());
        }
    }

    @SubscribeEvent
    public void onChunkUnload(ChunkEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level) {
            PipeNetworkManager.get(level.dimension()).onChunkUnloaded(event.getChunk().getPos());
        }
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        PipeNetworkManager.clearAll();
    }
}
