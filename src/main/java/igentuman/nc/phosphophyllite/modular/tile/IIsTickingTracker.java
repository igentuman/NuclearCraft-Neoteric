package igentuman.nc.phosphophyllite.modular.tile;

import igentuman.nc.phosphophyllite.modular.api.IModularTile;
import igentuman.nc.phosphophyllite.modular.api.ModuleRegistry;
import igentuman.nc.phosphophyllite.modular.api.TileModule;
import igentuman.nc.phosphophyllite.util.FastArraySet;
import igentuman.nc.util.annotation.NonnullDefault;
import igentuman.nc.util.annotation.OnModLoad;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.ChunkTicketLevelUpdatedEvent;

import javax.annotation.Nullable;

/**
 * Due to the forge event only being fired on the server, this is also only fired on the logical server
 */
@NonnullDefault
public interface IIsTickingTracker {
    default void startTicking() {
    }
    
    default void stopTicking() {
    }
    
    interface Tile extends IIsTickingTracker, IModularTile {
    }
    
    final class Module extends TileModule<Tile> {
        
        private static final class ChunkTracker {
            boolean isTicking = false;
            final FastArraySet<Module> modules = new FastArraySet<>();
        }
        
        private static final Object2ObjectMap<ServerLevel, LongSet> isTickingMap = new Object2ObjectOpenHashMap<>();
        private static final Object2ObjectMap<ServerLevel, Long2ObjectMap<ChunkTracker>> trackers = new Object2ObjectOpenHashMap<>();
        
        @Nullable
        ChunkTracker chunkTracker;
        @Nullable
        Long2ObjectMap<ChunkTracker> levelTrackers;
        @Nullable
        ServerLevel serverLevel;
        
        final ObjectArrayList<IIsTickingTracker> tileTrackers = new ObjectArrayList<>();
        
        @OnModLoad
        private static void onModLoad() {
            ModuleRegistry.registerTileModule(Tile.class, Module::new);
            MinecraftForge.EVENT_BUS.addListener(Module::ticketEventListener);
        }
        
        public Module(IModularTile iface) {
            super(iface);
        }
        
        @Override
        public void postModuleConstruction() {
            iface.modules().forEach(tileModule -> {
                if (tileModule instanceof IIsTickingTracker tracker) {
                    tileTrackers.add(tracker);
                }
            });
        }
        
        @Override
        public void onAdded() {
            var tile = (BlockEntity) iface;
            if (tile.getLevel() instanceof ServerLevel serverLevel) {
                this.serverLevel = serverLevel;
                levelTrackers = trackers.get(serverLevel);
                if (levelTrackers == null) {
                    levelTrackers = new Long2ObjectOpenHashMap<>();
                    trackers.put(serverLevel, levelTrackers);
                }
                chunkTracker = levelTrackers.get(ChunkPos.asLong(tile.getBlockPos()));
                if (chunkTracker == null) {
                    chunkTracker = new ChunkTracker();
                    var tickingSet = isTickingMap.get(serverLevel);
                    if (tickingSet == null) {
                        tickingSet = new LongOpenHashSet();
                        isTickingMap.put(serverLevel, tickingSet);
                    }
                    chunkTracker.isTicking = tickingSet.contains(ChunkPos.asLong(tile.getBlockPos()));
                    levelTrackers.put(ChunkPos.asLong(tile.getBlockPos()), chunkTracker);
                }
                chunkTracker.modules.add(this);
                if (chunkTracker.isTicking) {
                    // start ticking *is* sent here because chunks can be loaded without being in a ticking statte
                    startTicking();
                }
            }
        }
        
        void startTicking() {
            tileTrackers.forEach(IIsTickingTracker::startTicking);
        }
        
        void stopTicking() {
            tileTrackers.forEach(IIsTickingTracker::stopTicking);
        }
        
        @Override
        public void onRemoved(boolean chunkUnload) {
            if (serverLevel == null || chunkTracker == null || levelTrackers == null) {
                return;
            }
            // stop ticking isn't sent here because the tile also receives an onRemoved
            chunkTracker.modules.remove(this);
            if (chunkTracker.modules.size() == 0) {
                levelTrackers.remove(ChunkPos.asLong(((BlockEntity) iface).getBlockPos()));
                if (levelTrackers.isEmpty()) {
                    trackers.remove(serverLevel);
                }
            }
        }
        
        public static void ticketEventListener(ChunkTicketLevelUpdatedEvent ticketEvent) {
            final var level = ticketEvent.getLevel();
            var isTickingSet = isTickingMap.get(level);
            if (ticketEvent.getNewTicketLevel() <= 31) {
                if (isTickingSet == null) {
                    isTickingSet = new LongOpenHashSet();
                    isTickingMap.put(level, isTickingSet);
                }
                if (isTickingSet.add(ticketEvent.getChunkPos())) {
                    // chunk wasn't ticking, push update to a tracker if it has one
                    var chunkTrackers = trackers.get(level);
                    if (chunkTrackers == null) {
                        return;
                    }
                    var tracker = chunkTrackers.get(ticketEvent.getChunkPos());
                    if (tracker == null) {
                        return;
                    }
                    if (tracker.isTicking) {
                        return;
                    }
                    tracker.isTicking = true;
                    tracker.modules.elements().forEach(Module::startTicking);
                }
            } else {
                if (isTickingSet == null) {
                    return;
                }
                if (isTickingSet.remove(ticketEvent.getChunkPos())) {
                    // chunk was ticking, push update to tracker
                    var chunkTrackers = trackers.get(level);
                    if (chunkTrackers == null) {
                        return;
                    }
                    var tracker = chunkTrackers.get(ticketEvent.getChunkPos());
                    if (tracker == null) {
                        return;
                    }
                    if (!tracker.isTicking) {
                        return;
                    }
                    tracker.isTicking = false;
                    tracker.modules.elements().forEach(Module::stopTicking);
                }
            }
        }
    }
}
