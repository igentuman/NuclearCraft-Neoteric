package igentuman.nc.pipe;

import igentuman.nc.block_entity.pipe.PipeConnectorBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.energy.EnergyHandlerUtil;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.RangedResourceHandler;

import java.util.List;

import static igentuman.nc.block_entity.pipe.PipeConnectorBE.CAP_ENERGY;
import static igentuman.nc.block_entity.pipe.PipeConnectorBE.CAP_FLUID;
import static igentuman.nc.block_entity.pipe.PipeConnectorBE.CAP_ITEM;
import static igentuman.nc.config.Common.PIPE_ENERGY_THROUGHPUT;
import static igentuman.nc.config.Common.PIPE_FLUID_THROUGHPUT;
import static igentuman.nc.config.Common.PIPE_ITEM_THROUGHPUT;

public enum PipeCapabilityType {

    ITEM(CAP_ITEM) {
        @Override
        public int throughput() {
            return PIPE_ITEM_THROUGHPUT.get();
        }

        @Override
        public long pull(ServerLevel level, PipeConnectorBE source, List<Long> destinations, PipeNetworkManager manager) {
            if (source.isRemoved() || !source.isEnabled(ITEM)
                    || !source.getMode().isSource() || !source.redstoneAllows()) return 0;
            BlockPos sPos = source.getBlockPos();
            int budget = throughput();
            long total = 0;

            for (Direction srcFace : Direction.values()) {
                if (budget <= 0) break;
                ResourceHandler<ItemResource> src = source.getExternalCapability(srcFace, Capabilities.Item.BLOCK);
                if (src == null) continue;
                BlockPos srcNeighbor = sPos.relative(srcFace);

                for (int srcSlot = 0; srcSlot < src.size(); srcSlot++) {
                    if (budget <= 0) break;
                    if (src.getResource(srcSlot).isEmpty()) continue;
                    // Keep slot order and try only the first accepting face of each destination.
                    var slot = RangedResourceHandler.ofSingleIndex(src, srcSlot);
                    for (long packed : destinations) {
                        if (budget <= 0) break;
                        PipeConnectorBE destBE = manager.getConnectorBE(packed);
                        if (destBE == null || destBE == source || destBE.isRemoved() || !destBE.isEnabled(ITEM)
                                || !destBE.getMode().isDestination() || !destBE.redstoneAllows()) continue;
                        BlockPos dPos = destBE.getBlockPos();
                        for (Direction face : Direction.values()) {
                            if (dPos.relative(face).equals(srcNeighbor)) continue;
                            ResourceHandler<ItemResource> target = destBE.getExternalCapability(face, Capabilities.Item.BLOCK);
                            int moved = ResourceHandlerUtil.move(slot, target, resource -> true, budget, null);
                            if (moved <= 0) continue;
                            total += moved;
                            budget -= moved;
                            break;
                        }
                    }
                }
            }
            return total;
        }
    },

    FLUID(CAP_FLUID) {
        @Override
        public int throughput() {
            return PIPE_FLUID_THROUGHPUT.get();
        }

        @Override
        public long pull(ServerLevel level, PipeConnectorBE source, List<Long> destinations, PipeNetworkManager manager) {
            if (source.isRemoved() || !source.isEnabled(FLUID)
                    || !source.getMode().isSource() || !source.redstoneAllows()) return 0;
            BlockPos sPos = source.getBlockPos();
            int budget = throughput();
            long total = 0;

            for (Direction srcFace : Direction.values()) {
                if (budget <= 0) break;
                ResourceHandler<FluidResource> src = source.getExternalCapability(srcFace, Capabilities.Fluid.BLOCK);
                if (src == null) continue;
                // Retain the first extractable fluid for this face throughout the pull.
                FluidResource fluid = ResourceHandlerUtil.findExtractableResource(src, resource -> true, null);
                if (fluid == null) continue;
                BlockPos srcNeighbor = sPos.relative(srcFace);
                for (long packed : destinations) {
                    if (budget <= 0) break;
                    PipeConnectorBE destBE = manager.getConnectorBE(packed);
                    if (destBE == null || destBE == source || destBE.isRemoved() || !destBE.isEnabled(FLUID)
                            || !destBE.getMode().isDestination() || !destBE.redstoneAllows()) continue;
                    BlockPos dPos = destBE.getBlockPos();
                    for (Direction face : Direction.values()) {
                        if (dPos.relative(face).equals(srcNeighbor)) continue;
                        ResourceHandler<FluidResource> target = destBE.getExternalCapability(face, Capabilities.Fluid.BLOCK);
                        int moved = ResourceHandlerUtil.move(src, target, fluid::equals, budget, null);
                        if (moved <= 0) continue;
                        total += moved;
                        budget -= moved;
                        break;
                    }
                }
            }
            return total;
        }
    },

    ENERGY(CAP_ENERGY) {
        @Override
        public int throughput() {
            return PIPE_ENERGY_THROUGHPUT.get();
        }

        @Override
        public long pull(ServerLevel level, PipeConnectorBE source, List<Long> destinations, PipeNetworkManager manager) {
            BlockPos sPos = source.getBlockPos();
            int budget = throughput();
            long total = 0;

            for (Direction srcFace : Direction.values()) {
                if (budget <= 0) break;
                EnergyHandler src = source.getExternalCapability(srcFace, Capabilities.Energy.BLOCK);
                if (src == null) continue;
                BlockPos srcNeighbor = sPos.relative(srcFace);
                for (long packed : destinations) {
                    if (budget <= 0) break;
                    PipeConnectorBE destBE = manager.getConnectorBE(packed);
                    if (destBE == null || destBE == source || !destBE.isEnabled(ENERGY)
                            || !destBE.getMode().isDestination() || !destBE.redstoneAllows()) continue;
                    BlockPos dPos = destBE.getBlockPos();
                    for (Direction face : Direction.values()) {
                        if (dPos.relative(face).equals(srcNeighbor)) continue;
                        EnergyHandler target = destBE.getExternalCapability(face, Capabilities.Energy.BLOCK);
                        int moved = EnergyHandlerUtil.move(src, target, budget, null);
                        if (moved <= 0) continue;
                        total += moved;
                        budget -= moved;
                        break;
                    }
                }
            }
            return total;
        }
    };

    public final int index;

    PipeCapabilityType(int index) {
        this.index = index;
    }

    public abstract int throughput();

    public abstract long pull(ServerLevel level, PipeConnectorBE source, List<Long> destinations, PipeNetworkManager manager);
}
