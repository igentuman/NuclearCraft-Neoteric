package igentuman.nc.pipe;

import igentuman.nc.block.pipe.entity.PipeConnectorBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.ArrayList;
import java.util.List;

import static igentuman.nc.block.pipe.entity.PipeConnectorBE.CAP_ENERGY;
import static igentuman.nc.block.pipe.entity.PipeConnectorBE.CAP_FLUID;
import static igentuman.nc.block.pipe.entity.PipeConnectorBE.CAP_ITEM;
import static igentuman.nc.handler.config.CommonConfig.PIPE_CONFIG;

public enum PipeCapabilityType {

    ITEM(CAP_ITEM) {
        @Override
        public int throughput() {
            return PIPE_CONFIG.ITEM_THROUGHPUT.get();
        }

        @Override
        public long pull(ServerLevel level, PipeConnectorBE source, List<Long> destinations, PipeNetworkManager manager) {
            BlockPos sPos = source.getBlockPos();
            int budget = throughput();
            long total = 0;

            for (Direction srcFace : Direction.values()) {
                if (budget <= 0) break;
                BlockEntity nbe = source.getExternalNeighbor(srcFace);
                if (nbe == null) continue;
                IItemHandler src = nbe.getCapability(ForgeCapabilities.ITEM_HANDLER, srcFace.getOpposite()).resolve().orElse(null);
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
                            BlockEntity dnbe = destBE.getExternalNeighbor(face);
                            if (dnbe == null) continue;
                            IItemHandler h = dnbe.getCapability(ForgeCapabilities.ITEM_HANDLER, face.getOpposite()).resolve().orElse(null);
                            if (h == null) continue;
                            ItemStack attempt = moving.copy();
                            attempt.setCount(remaining);
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
                        ItemStack part = extracted.copy();
                        part.setCount(amt);
                        cursor += amt - ItemHandlerHelper.insertItem(targets.get(i), part, false).getCount();
                    }
                    int notInserted = extracted.getCount() - cursor;
                    if (notInserted > 0) {
                        ItemStack back = extracted.copy();
                        back.setCount(notInserted);
                        ItemHandlerHelper.insertItem(src, back, false);
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
            return PIPE_CONFIG.FLUID_THROUGHPUT.get();
        }

        @Override
        public long pull(ServerLevel level, PipeConnectorBE source, List<Long> destinations, PipeNetworkManager manager) {
            BlockPos sPos = source.getBlockPos();
            int budget = throughput();
            long total = 0;

            for (Direction srcFace : Direction.values()) {
                if (budget <= 0) break;
                BlockEntity nbe = source.getExternalNeighbor(srcFace);
                if (nbe == null) continue;
                IFluidHandler src = nbe.getCapability(ForgeCapabilities.FLUID_HANDLER, srcFace.getOpposite()).resolve().orElse(null);
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
                        BlockEntity dnbe = destBE.getExternalNeighbor(face);
                        if (dnbe == null) continue;
                        IFluidHandler h = dnbe.getCapability(ForgeCapabilities.FLUID_HANDLER, face.getOpposite()).resolve().orElse(null);
                        if (h == null) continue;
                        FluidStack attempt = drained.copy();
                        attempt.setAmount(remaining);
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

                FluidStack toExtract = drained.copy();
                toExtract.setAmount(moved);
                FluidStack extracted = src.drain(toExtract, IFluidHandler.FluidAction.EXECUTE);
                if (extracted.isEmpty()) continue;
                int cursor = 0;
                for (int i = 0; i < targets.size() && cursor < extracted.getAmount(); i++) {
                    int amt = Math.min(amounts.get(i), extracted.getAmount() - cursor);
                    FluidStack part = extracted.copy();
                    part.setAmount(amt);
                    cursor += targets.get(i).fill(part, IFluidHandler.FluidAction.EXECUTE);
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
            return PIPE_CONFIG.ENERGY_THROUGHPUT.get();
        }

        @Override
        public long pull(ServerLevel level, PipeConnectorBE source, List<Long> destinations, PipeNetworkManager manager) {
            BlockPos sPos = source.getBlockPos();
            int budget = throughput();
            long total = 0;

            for (Direction srcFace : Direction.values()) {
                if (budget <= 0) break;
                BlockEntity nbe = source.getExternalNeighbor(srcFace);
                if (nbe == null) continue;
                IEnergyStorage src = nbe.getCapability(ForgeCapabilities.ENERGY, srcFace.getOpposite()).resolve().orElse(null);
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
                        BlockEntity dnbe = destBE.getExternalNeighbor(face);
                        if (dnbe == null) continue;
                        IEnergyStorage h = dnbe.getCapability(ForgeCapabilities.ENERGY, face.getOpposite()).resolve().orElse(null);
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
