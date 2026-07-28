package igentuman.nc.api.pipe;

import igentuman.nc.block.pipe.PipeBlock;
import igentuman.nc.block.pipe.PipeConnectorBlock;
import igentuman.nc.block_entity.pipe.PipeConnectorBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static igentuman.nc.NuclearCraft.debugLog;
import static igentuman.nc.config.Common.PIPE_MAX_NETWORK_SIZE;

public class PipeNetworkManager {

    public static final ConcurrentHashMap<ResourceKey<Level>, PipeNetworkManager> instances = new ConcurrentHashMap<>();

    private final ResourceKey<Level> dimension;
    private final Map<Integer, PipeNetwork> networks = new HashMap<>();
    private final Map<Long, PipeNetwork> posToNetwork = new HashMap<>();
    private final Map<Long, PipeConnectorBE> connectorBEs = new HashMap<>();
    private final Set<PipeConnectorBE> activePullers = new HashSet<>();
    private final Map<Long, Set<Long>> connectorsByChunk = new HashMap<>();
    private int nextNetworkId = 0;

    private PipeNetworkManager(ResourceKey<Level> dimension) {
        this.dimension = dimension;
    }

    public static PipeNetworkManager get(ResourceKey<Level> dimension) {
        return instances.computeIfAbsent(dimension, PipeNetworkManager::new);
    }

    public void onNodeAdded(BlockPos pos, boolean isConnector) {
        long packed = pos.asLong();
        if (posToNetwork.containsKey(packed)) {
            if (isConnector) {
                posToNetwork.get(packed).connectors.add(packed);
            }
            return;
        }

        Set<PipeNetwork> neighborNets = new HashSet<>();
        for (Direction dir : Direction.values()) {
            PipeNetwork net = posToNetwork.get(pos.relative(dir).asLong());
            if (net != null) {
                neighborNets.add(net);
            }
        }

        PipeNetwork target;
        if (neighborNets.isEmpty()) {
            target = createNetwork();
        } else if (neighborNets.size() == 1) {
            target = neighborNets.iterator().next();
        } else {
            target = mergeNetworks(neighborNets);
        }

        target.addNode(packed, isConnector);
        posToNetwork.put(packed, target);
        target.bumpVersion();
        debugLog("Pipe node added at " + pos.toShortString() + " -> network " + target.id
                + " (size " + target.size() + ", connectors " + target.connectors.size() + ")");
    }

    public void onNodeRemoved(BlockPos pos) {
        long packed = pos.asLong();
        PipeConnectorBE removedBE = connectorBEs.remove(packed);
        if (removedBE != null) {
            activePullers.remove(removedBE);
            indexConnectorChunk(packed, false);
        }
        PipeNetwork net = posToNetwork.remove(packed);
        if (net == null) {
            return;
        }
        net.removeNode(packed);
        if (net.isEmpty()) {
            networks.remove(net.id);
            debugLog("Pipe node removed at " + pos.toShortString() + " -> network " + net.id + " emptied");
            return;
        }
        net.bumpVersion();
        splitCheck(net, pos);
        debugLog("Pipe node removed at " + pos.toShortString() + " -> network " + net.id
                + " (size " + net.size() + ", total networks " + networks.size() + ")");
    }

    private PipeNetwork mergeNetworks(Set<PipeNetwork> nets) {
        PipeNetwork survivor = null;
        for (PipeNetwork net : nets) {
            if (survivor == null || net.size() > survivor.size()) {
                survivor = net;
            }
        }
        for (PipeNetwork net : nets) {
            if (net == survivor) {
                continue;
            }
            for (long p : net.nodes) {
                survivor.addNode(p, net.connectors.contains(p));
                posToNetwork.put(p, survivor);
                PipeConnectorBE be = connectorBEs.get(p);
                if (be != null) {
                    be.setNetwork(survivor);
                }
            }
            networks.remove(net.id);
            debugLog("Merged network " + net.id + " into " + survivor.id);
        }
        survivor.bumpVersion();
        return survivor;
    }

    private void splitCheck(PipeNetwork net, BlockPos removed) {
        List<Long> seeds = new ArrayList<>();
        for (Direction dir : Direction.values()) {
            long np = removed.relative(dir).asLong();
            if (net.nodes.contains(np)) {
                seeds.add(np);
            }
        }
        if (seeds.size() <= 1) {
            return;
        }
        Set<Long> unassigned = new HashSet<>(net.nodes);
        boolean first = true;
        for (long seed : seeds) {
            if (!unassigned.contains(seed)) {
                continue;
            }
            Set<Long> component = floodFill(seed, unassigned);
            if (first) {
                first = false;
                continue;
            }
            PipeNetwork split = createNetwork();
            for (long p : component) {
                boolean isConnector = net.connectors.contains(p);
                net.removeNode(p);
                split.addNode(p, isConnector);
                posToNetwork.put(p, split);
                PipeConnectorBE be = connectorBEs.get(p);
                if (be != null) {
                    be.setNetwork(split);
                }
            }
            split.bumpVersion();
            debugLog("Split network " + net.id + " -> new network " + split.id + " (size " + split.size() + ")");
        }
    }

    private Set<Long> floodFill(long start, Set<Long> unassigned) {
        Set<Long> visited = new HashSet<>();
        Deque<Long> queue = new ArrayDeque<>();
        queue.add(start);
        unassigned.remove(start);
        visited.add(start);
        while (!queue.isEmpty()) {
            BlockPos cur = BlockPos.of(queue.poll());
            for (Direction dir : Direction.values()) {
                long np = cur.relative(dir).asLong();
                if (unassigned.remove(np)) {
                    visited.add(np);
                    queue.add(np);
                }
            }
        }
        return visited;
    }

    private PipeNetwork createNetwork() {
        PipeNetwork net = new PipeNetwork(nextNetworkId++, dimension);
        networks.put(net.id, net);
        return net;
    }

    public void registerConnector(PipeConnectorBE be) {
        BlockPos pos = be.getBlockPos();
        long packed = pos.asLong();
        connectorBEs.put(packed, be);
        indexConnectorChunk(packed, true);
        boolean known = posToNetwork.containsKey(packed);
        onNodeAdded(pos, true);
        if (!known && be.getLevel() != null) {
            discoverConnected(be.getLevel(), pos);
        }
        be.setNetwork(posToNetwork.get(packed));
        onConnectorConfigChanged(be);
    }

    public void unregisterConnectorBE(BlockPos pos) {
        long packed = pos.asLong();
        PipeConnectorBE be = connectorBEs.remove(packed);
        if (be != null) {
            activePullers.remove(be);
            indexConnectorChunk(packed, false);
        }
    }

    private void indexConnectorChunk(long packed, boolean add) {
        long chunkKey = new ChunkPos(BlockPos.of(packed)).toLong();
        if (add) {
            connectorsByChunk.computeIfAbsent(chunkKey, k -> new HashSet<>()).add(packed);
        } else {
            Set<Long> set = connectorsByChunk.get(chunkKey);
            if (set != null) {
                set.remove(packed);
                if (set.isEmpty()) {
                    connectorsByChunk.remove(chunkKey);
                }
            }
        }
    }

    public void onConnectorConfigChanged(PipeConnectorBE be) {
        PipeNetwork net = be.getNetwork();
        if (net != null) {
            net.bumpVersion();
        }
        if (be.getMode() == ConnectorMode.PULL) {
            activePullers.add(be);
        } else {
            activePullers.remove(be);
        }
    }

    private void discoverConnected(Level level, BlockPos start) {
        int cap = PIPE_MAX_NETWORK_SIZE.get();
        Set<Long> visited = new HashSet<>();
        Deque<BlockPos> queue = new ArrayDeque<>();
        queue.add(start);
        visited.add(start.asLong());
        while (!queue.isEmpty()) {
            if (visited.size() >= cap) {
                debugLog("Pipe discovery hit max_network_size (" + cap + ") near " + start.toShortString() + "; deferring rest");
                break;
            }
            BlockPos cur = queue.poll();
            for (Direction dir : Direction.values()) {
                BlockPos np = cur.relative(dir);
                long packed = np.asLong();
                if (!visited.add(packed)) {
                    continue;
                }
                BlockState st = blockStateIfLoaded(level, np);
                if (st == null || !isPipeNode(st)) {
                    continue;
                }
                if (!posToNetwork.containsKey(packed)) {
                    onNodeAdded(np, isConnectorState(st));
                }
                queue.add(np);
            }
        }
    }

    private static BlockState blockStateIfLoaded(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel sl)) {
            return null;
        }
        LevelChunk chunk = sl.getChunkSource().getChunkNow(
                SectionPos.blockToSectionCoord(pos.getX()),
                SectionPos.blockToSectionCoord(pos.getZ()));
        return chunk == null ? null : chunk.getBlockState(pos);
    }

    public static boolean isPipeNode(BlockState state) {
        return state.getBlock() instanceof PipeBlock || state.getBlock() instanceof PipeConnectorBlock;
    }

    private static boolean isConnectorState(BlockState state) {
        return state.getBlock() instanceof PipeConnectorBlock;
    }

    public PipeNetwork getNetwork(BlockPos pos) {
        return posToNetwork.get(pos.asLong());
    }

    public PipeConnectorBE getConnectorBE(BlockPos pos) {
        return connectorBEs.get(pos.asLong());
    }

    public PipeConnectorBE getConnectorBE(long packed) {
        return connectorBEs.get(packed);
    }

    public int getNetworkCount() {
        return networks.size();
    }

    public void onChunkLoaded(ServerLevel level, ChunkPos cp) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                Set<Long> set = connectorsByChunk.get(new ChunkPos(cp.x + dx, cp.z + dz).toLong());
                if (set == null) {
                    continue;
                }
                for (long packed : new ArrayList<>(set)) {
                    PipeConnectorBE be = connectorBEs.get(packed);
                    if (be != null && !be.isRemoved()) {
                        discoverConnected(level, BlockPos.of(packed));
                    }
                }
            }
        }
    }

    public void onChunkUnloaded(ChunkPos cp) {
        long chunkKey = cp.toLong();
        List<Long> inChunk = new ArrayList<>();
        for (long packed : posToNetwork.keySet()) {
            if (new ChunkPos(BlockPos.of(packed)).toLong() == chunkKey) {
                inChunk.add(packed);
            }
        }
        for (long packed : inChunk) {
            onNodeRemoved(BlockPos.of(packed));
        }
    }

    public void tickTransfers(ServerLevel level) {
        if (activePullers.isEmpty()) {
            return;
        }
        for (PipeConnectorBE puller : new ArrayList<>(activePullers)) {
            if (puller.isRemoved()) {
                activePullers.remove(puller);
                continue;
            }
            BlockPos pos = puller.getBlockPos();
            if (!level.isLoaded(pos)) {
                continue;
            }
            if (!puller.redstoneAllows()) {
                continue;
            }
            PipeNetwork net = puller.getNetwork();
            if (net == null) {
                continue;
            }
            for (PipeCapabilityType type : PipeCapabilityType.values()) {
                if (!puller.isEnabled(type)) {
                    continue;
                }
                List<Long> destinations = net.getDestinations(type, this);
                if (destinations.isEmpty()) {
                    continue;
                }
                type.pull(level, puller, destinations, this);
            }
        }
    }

    public void clear() {
        networks.clear();
        posToNetwork.clear();
        connectorBEs.clear();
        activePullers.clear();
        connectorsByChunk.clear();
        nextNetworkId = 0;
    }

    public static void clearAll() {
        debugLog("Clearing all pipe network managers for " + instances.size() + " dimensions");
        for (PipeNetworkManager manager : instances.values()) {
            manager.clear();
        }
        instances.clear();
    }
}
