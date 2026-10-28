package igentuman.nc.handler.crafter;

import igentuman.nc.api.pipe.PipeCapabilityType;
import igentuman.nc.api.pipe.PipeNetwork;
import igentuman.nc.api.pipe.PipeNetworkManager;
import igentuman.nc.block_entity.UniversalProcessorBE;
import igentuman.nc.block_entity.pipe.PipeConnectorBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ProcessorIndex {

    public record Link(BlockPos pos, String type, boolean items, boolean fluids) {

        public boolean supports(CraftingPattern pattern) {
            if (!type.equals(pattern.processor())) return false;
            if (pattern.usesItems() && !items) return false;
            return !pattern.usesFluids() || fluids;
        }

        public boolean supports(String processor, boolean needItems, boolean needFluids) {
            return type.equals(processor) && (!needItems || items) && (!needFluids || fluids);
        }
    }

    private ProcessorIndex() {}

    public static List<Link> scan(ServerLevel level, BlockPos crafterPos) {
        PipeNetworkManager manager = PipeNetworkManager.get(level.dimension());
        Set<PipeNetwork> networks = new LinkedHashSet<>();
        for (Direction dir : Direction.values()) {
            BlockPos np = crafterPos.relative(dir);
            if (!level.isLoaded(np) || !PipeNetworkManager.isPipeNode(level.getBlockState(np))) continue;
            PipeNetwork net = manager.getNetwork(np);
            if (net != null) networks.add(net);
        }
        if (networks.isEmpty()) return List.of();

        Map<Long, Link> found = new LinkedHashMap<>();
        for (PipeNetwork net : networks) {
            for (long packed : new ArrayList<>(net.connectors)) {
                PipeConnectorBE connector = manager.getConnectorBE(packed);
                if (connector == null || connector.isRemoved()) continue;
                boolean items = connector.isEnabled(PipeCapabilityType.ITEM);
                boolean fluids = connector.isEnabled(PipeCapabilityType.FLUID);
                if (!items && !fluids) continue;
                BlockPos cpos = connector.getBlockPos();
                for (Direction face : Direction.values()) {
                    BlockPos pp = cpos.relative(face);
                    if (pp.equals(crafterPos) || !level.isLoaded(pp)) continue;
                    BlockEntity be = level.getBlockEntity(pp);
                    if (!(be instanceof UniversalProcessorBE proc) || proc.name == null) continue;
                    long key = pp.asLong();
                    Link prev = found.get(key);
                    if (prev == null) {
                        found.put(key, new Link(pp.immutable(), proc.name, items, fluids));
                    } else if ((items && !prev.items()) || (fluids && !prev.fluids())) {
                        found.put(key, new Link(prev.pos(), prev.type(), prev.items() || items, prev.fluids() || fluids));
                    }
                }
            }
        }
        return new ArrayList<>(found.values());
    }

    public static int topologyStamp(ServerLevel level, BlockPos crafterPos) {
        PipeNetworkManager manager = PipeNetworkManager.get(level.dimension());
        int stamp = 0;
        for (Direction dir : Direction.values()) {
            BlockPos np = crafterPos.relative(dir);
            if (!level.isLoaded(np) || !PipeNetworkManager.isPipeNode(level.getBlockState(np))) continue;
            PipeNetwork net = manager.getNetwork(np);
            if (net != null) stamp = stamp * 31 + net.id * 7 + net.topologyVersion;
        }
        return stamp;
    }
}
