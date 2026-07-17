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
            int max = throughput();

            IItemHandler src = null;
            int srcSlot = -1;
            ItemStack moving = ItemStack.EMPTY;
            BlockPos srcNeighbor = null;
            source:
            for (Direction face : Direction.values()) {
                BlockEntity nbe = source.getExternalNeighbor(face);
                if (nbe == null) continue;
                IItemHandler h = nbe.getCapability(ForgeCapabilities.ITEM_HANDLER, face.getOpposite()).resolve().orElse(null);
                if (h == null) continue;
                for (int slot = 0; slot < h.getSlots(); slot++) {
                    ItemStack sim = h.extractItem(slot, max, true);
                    if (!sim.isEmpty()) {
                        src = h;
                        srcSlot = slot;
                        moving = sim;
                        srcNeighbor = sPos.relative(face);
                        break source;
                    }
                }
            }
            if (src == null || moving.isEmpty()) return 0;

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
                    BlockEntity nbe = destBE.getExternalNeighbor(face);
                    if (nbe == null) continue;
                    IItemHandler h = nbe.getCapability(ForgeCapabilities.ITEM_HANDLER, face.getOpposite()).resolve().orElse(null);
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
            if (moved <= 0) return 0;

            ItemStack extracted = src.extractItem(srcSlot, moved, false);
            if (extracted.isEmpty()) return 0;
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
            return cursor;
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
            int max = throughput();

            IFluidHandler src = null;
            FluidStack drained = FluidStack.EMPTY;
            BlockPos srcNeighbor = null;
            for (Direction face : Direction.values()) {
                BlockEntity nbe = source.getExternalNeighbor(face);
                if (nbe == null) continue;
                IFluidHandler h = nbe.getCapability(ForgeCapabilities.FLUID_HANDLER, face.getOpposite()).resolve().orElse(null);
                if (h == null) continue;
                FluidStack sim = h.drain(max, IFluidHandler.FluidAction.SIMULATE);
                if (!sim.isEmpty()) {
                    src = h;
                    drained = sim;
                    srcNeighbor = sPos.relative(face);
                    break;
                }
            }
            if (src == null || drained.isEmpty()) return 0;

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
                    BlockEntity nbe = destBE.getExternalNeighbor(face);
                    if (nbe == null) continue;
                    IFluidHandler h = nbe.getCapability(ForgeCapabilities.FLUID_HANDLER, face.getOpposite()).resolve().orElse(null);
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
            if (moved <= 0) return 0;

            FluidStack toExtract = drained.copy();
            toExtract.setAmount(moved);
            FluidStack extracted = src.drain(toExtract, IFluidHandler.FluidAction.EXECUTE);
            if (extracted.isEmpty()) return 0;
            int cursor = 0;
            for (int i = 0; i < targets.size() && cursor < extracted.getAmount(); i++) {
                int amt = Math.min(amounts.get(i), extracted.getAmount() - cursor);
                FluidStack part = extracted.copy();
                part.setAmount(amt);
                cursor += targets.get(i).fill(part, IFluidHandler.FluidAction.EXECUTE);
            }
            return cursor;
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
            int max = throughput();

            IEnergyStorage src = null;
            BlockPos srcNeighbor = null;
            for (Direction face : Direction.values()) {
                BlockEntity nbe = source.getExternalNeighbor(face);
                if (nbe == null) continue;
                IEnergyStorage h = nbe.getCapability(ForgeCapabilities.ENERGY, face.getOpposite()).resolve().orElse(null);
                if (h == null || !h.canExtract() || h.extractEnergy(max, true) <= 0) continue;
                src = h;
                srcNeighbor = sPos.relative(face);
                break;
            }
            if (src == null) return 0;
            int available = src.extractEnergy(max, true);
            if (available <= 0) return 0;

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
                    BlockEntity nbe = destBE.getExternalNeighbor(face);
                    if (nbe == null) continue;
                    IEnergyStorage h = nbe.getCapability(ForgeCapabilities.ENERGY, face.getOpposite()).resolve().orElse(null);
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
            if (moved <= 0) return 0;

            src.extractEnergy(moved, false);
            for (int i = 0; i < targets.size(); i++) {
                targets.get(i).receiveEnergy(amounts.get(i), false);
            }
            return moved;
        }
    };

    public final int index;

    PipeCapabilityType(int index) {
        this.index = index;
    }

    public abstract int throughput();

    public abstract long pull(ServerLevel level, PipeConnectorBE source, List<Long> destinations, PipeNetworkManager manager);
}
