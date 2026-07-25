package igentuman.nc.api.pipe;

import igentuman.nc.block_entity.pipe.PipeConnectorBE;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PipeNetwork {

    public final int id;
    public final ResourceKey<Level> dimension;
    public final Set<Long> nodes = new HashSet<>();
    public final Set<Long> connectors = new HashSet<>();
    public int topologyVersion = 0;

    private final EnumMap<PipeCapabilityType, List<Long>> destinationCache = new EnumMap<>(PipeCapabilityType.class);
    private final EnumMap<PipeCapabilityType, Integer> destinationCacheVersion = new EnumMap<>(PipeCapabilityType.class);
    private final EnumMap<PipeCapabilityType, List<Long>> sourceCache = new EnumMap<>(PipeCapabilityType.class);
    private final EnumMap<PipeCapabilityType, Integer> sourceCacheVersion = new EnumMap<>(PipeCapabilityType.class);

    public PipeNetwork(int id, ResourceKey<Level> dimension) {
        this.id = id;
        this.dimension = dimension;
    }

    public List<Long> getDestinations(PipeCapabilityType type, PipeNetworkManager manager) {
        return collect(type, manager, destinationCache, destinationCacheVersion, false);
    }

    public List<Long> getSources(PipeCapabilityType type, PipeNetworkManager manager) {
        return collect(type, manager, sourceCache, sourceCacheVersion, true);
    }

    private List<Long> collect(PipeCapabilityType type, PipeNetworkManager manager,
                               EnumMap<PipeCapabilityType, List<Long>> cache,
                               EnumMap<PipeCapabilityType, Integer> cacheVersion, boolean sources) {
        Integer cachedVersion = cacheVersion.get(type);
        if (cachedVersion != null && cachedVersion == topologyVersion) {
            return cache.get(type);
        }
        List<Long> list = new ArrayList<>();
        for (long packed : connectors) {
            PipeConnectorBE be = manager.getConnectorBE(packed);
            if (be == null || !be.isEnabled(type)) {
                continue;
            }
            if (sources ? !be.getMode().isSource() : !be.getMode().isDestination()) {
                continue;
            }
            list.add(packed);
        }
        cache.put(type, list);
        cacheVersion.put(type, topologyVersion);
        return list;
    }

    public void addNode(long packed, boolean isConnector) {
        nodes.add(packed);
        if (isConnector) {
            connectors.add(packed);
        }
    }

    public void removeNode(long packed) {
        nodes.remove(packed);
        connectors.remove(packed);
    }

    public boolean contains(long packed) {
        return nodes.contains(packed);
    }

    public int size() {
        return nodes.size();
    }

    public boolean isEmpty() {
        return nodes.isEmpty();
    }

    public void bumpVersion() {
        topologyVersion++;
    }
}
