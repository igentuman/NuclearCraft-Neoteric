package igentuman.nc.pipe;

import igentuman.nc.block_entity.pipe.PipeConnectorBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import java.util.ArrayList;
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
            BlockPos sPos = source.getBlockPos();
            int budget = throughput();
            long total = 0;

            for (Direction srcFace : Direction.values()) {
                if (budget <= 0) break;
                IItemHandler src = source.getExternalCapability(srcFace, Capabilities.ItemHandler.BLOCK);
                if (src == null) continue;
                BlockPos srcNeighbor = sPos.relative(srcFace);

                for (int srcSlot = 0; srcSlot < src.getSlots(); srcSlot++) {
                    if (budget <= 0) break;
                    ItemStack moving = src.extractItem(srcSlot, budget, true);
                    if (moving.isEmpty()) continue;

                    int remaining = moving.getCount();
                    List<IItemHandler> targets = new ArrayList<>();
                    List<Integer> amounts = new ArrayList<>();
                    for (long packed : destinations) {
                        if (remaining <= 0) break;
                        PipeConnectorBE destBE = manager.getConnectorBE(packed);
                        if (destBE == null || destBE == source) continue;
                        BlockPos dPos = destBE.getBlockPos();
                        for (Direction face : Direction.values()) {
                            BlockPos np = dPos.relative(face);
                            if (np.equals(srcNeighbor)) continue;
                            IItemHandler h = destBE.getExternalCapability(face, Capabilities.ItemHandler.BLOCK);
                            if (h == null) continue;
                            ItemStack attempt = moving.copyWithCount(remaining);
                            int accepted = remaining - ItemHandlerHelper.insertItem(h, attempt, true).getCount();
                            if (accepted <= 0) continue;
                            targets.add(h);
                            amounts.add(accepted);
                            remaining -= accepted;
                            break;
                        }
                    }
                    int moved = moving.getCount() - remaining;
                    if (moved <= 0) continue;

                    ItemStack extracted = src.extractItem(srcSlot, moved, false);
                    if (extracted.isEmpty()) continue;
                    int cursor = 0;
                    for (int i = 0; i < targets.size() && cursor < extracted.getCount(); i++) {
                        int amt = Math.min(amounts.get(i), extracted.getCount() - cursor);
                        ItemStack part = extracted.copyWithCount(amt);
                        cursor += amt - ItemHandlerHelper.insertItem(targets.get(i), part, false).getCount();
                    }
                    int notInserted = extracted.getCount() - cursor;
                    if (notInserted > 0) {
                        ItemHandlerHelper.insertItem(src, extracted.copyWithCount(notInserted), false);
                    }
                    total += cursor;
                    budget -= cursor;
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
            BlockPos sPos = source.getBlockPos();
            int budget = throughput();
            long total = 0;

            for (Direction srcFace : Direction.values()) {
                if (budget <= 0) break;
                IFluidHandler src = source.getExternalCapability(srcFace, Capabilities.FluidHandler.BLOCK);
                if (src == null) continue;
                FluidStack drained = src.drain(budget, IFluidHandler.FluidAction.SIMULATE);
                if (drained.isEmpty()) continue;
                BlockPos srcNeighbor = sPos.relative(srcFace);

                int remaining = drained.getAmount();
                List<IFluidHandler> targets = new ArrayList<>();
                List<Integer> amounts = new ArrayList<>();
                for (long packed : destinations) {
                    if (remaining <= 0) break;
                    PipeConnectorBE destBE = manager.getConnectorBE(packed);
                    if (destBE == null || destBE == source) continue;
                    BlockPos dPos = destBE.getBlockPos();
                    for (Direction face : Direction.values()) {
                        BlockPos np = dPos.relative(face);
                        if (np.equals(srcNeighbor)) continue;
                        IFluidHandler h = destBE.getExternalCapability(face, Capabilities.FluidHandler.BLOCK);
                        if (h == null) continue;
                        FluidStack attempt = drained.copyWithAmount(remaining);
                        int accepted = h.fill(attempt, IFluidHandler.FluidAction.SIMULATE);
                        if (accepted <= 0) continue;
                        targets.add(h);
                        amounts.add(accepted);
                        remaining -= accepted;
                        break;
                    }
                }
                int moved = drained.getAmount() - remaining;
                if (moved <= 0) continue;

                FluidStack extracted = src.drain(drained.copyWithAmount(moved), IFluidHandler.FluidAction.EXECUTE);
                if (extracted.isEmpty()) continue;
                int cursor = 0;
                for (int i = 0; i < targets.size() && cursor < extracted.getAmount(); i++) {
                    int amt = Math.min(amounts.get(i), extracted.getAmount() - cursor);
                    cursor += targets.get(i).fill(extracted.copyWithAmount(amt), IFluidHandler.FluidAction.EXECUTE);
                }
                total += cursor;
                budget -= cursor;
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
                IEnergyStorage src = source.getExternalCapability(srcFace, Capabilities.EnergyStorage.BLOCK);
                if (src == null || !src.canExtract()) continue;
                int available = src.extractEnergy(budget, true);
                if (available <= 0) continue;
                BlockPos srcNeighbor = sPos.relative(srcFace);

                int remaining = available;
                List<IEnergyStorage> targets = new ArrayList<>();
                List<Integer> amounts = new ArrayList<>();
                for (long packed : destinations) {
                    if (remaining <= 0) break;
                    PipeConnectorBE destBE = manager.getConnectorBE(packed);
                    if (destBE == null || destBE == source) continue;
                    BlockPos dPos = destBE.getBlockPos();
                    for (Direction face : Direction.values()) {
                        BlockPos np = dPos.relative(face);
                        if (np.equals(srcNeighbor)) continue;
                        IEnergyStorage h = destBE.getExternalCapability(face, Capabilities.EnergyStorage.BLOCK);
                        if (h == null || !h.canReceive()) continue;
                        int accepted = h.receiveEnergy(remaining, true);
                        if (accepted <= 0) continue;
                        targets.add(h);
                        amounts.add(accepted);
                        remaining -= accepted;
                        break;
                    }
                }
                int moved = available - remaining;
                if (moved <= 0) continue;

                src.extractEnergy(moved, false);
                for (int i = 0; i < targets.size(); i++) {
                    targets.get(i).receiveEnergy(amounts.get(i), false);
                }
                total += moved;
                budget -= moved;
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
