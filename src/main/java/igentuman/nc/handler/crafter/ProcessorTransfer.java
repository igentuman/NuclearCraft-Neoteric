package igentuman.nc.handler.crafter;

import igentuman.nc.block_entity.UniversalProcessorBE;
import igentuman.nc.handler.crafter.AutoCraftSolver.PatternDef;
import igentuman.nc.handler.fluid.FluidStackHandler;
import igentuman.nc.registration.ModEntry;
import igentuman.nc.setup.ModEntries;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import igentuman.nc.handler.sided.ItemCapabilityHandler;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Internal processor access with atomic native input placement and output collection. */
public record ProcessorTransfer(ItemCapabilityHandler items, FluidStackHandler fluids,
                                int inputSlots, int outputSlots, int inputTanks, int outputTanks) {

    public static ProcessorTransfer of(UniversalProcessorBE proc) {
        ModEntry entry = ModEntries.get(proc.name);
        var itemCap = entry.itemCap();
        var fluidCap = entry.fluidCap();
        return new ProcessorTransfer(proc.contentHandler.getItemHandler(),
                proc.contentHandler.hasFluidCapability() ? proc.contentHandler.getFluidHandler().getInternalHandler() : null,
                itemCap == null ? 0 : itemCap.inputSlots, itemCap == null ? 0 : itemCap.outputSlots,
                fluidCap == null ? 0 : fluidCap.inputTanks.size(), fluidCap == null ? 0 : fluidCap.outputTanks.size());
    }

    public boolean inputsEmpty() {
        for (int s = 0; s < inputSlots; s++) if (!items.getStackInSlot(s).isEmpty()) return false;
        for (int t = 0; t < inputTanks; t++) if (!fluids.getFluidInTank(t).isEmpty()) return false;
        return true;
    }

    public record Placement(CraftKey key, int slot, int amountPerOperation) {}

    public record InputPlan(int operations, List<Placement> placements) {
        public InputPlan {
            placements = List.copyOf(placements);
        }
    }

    public InputPlan plan(PatternDef<CraftKey> pattern, int requested) {
        if (requested <= 0 || !inputsEmpty()) return null;
        int operations = requested;
        boolean[] usedSlots = new boolean[inputSlots];
        boolean[] usedTanks = new boolean[inputTanks];
        List<Placement> placements = new ArrayList<>();
        for (CraftKey key : pattern.inputs().keySet()) {
            for (int perOperation : pattern.amounts(key)) {
                if (perOperation <= 0) return null;
                int slot = emptySlot(key, key.isFluid() ? usedTanks : usedSlots);
                if (slot < 0) return null;
                (key.isFluid() ? usedTanks : usedSlots)[slot] = true;
                int wanted = (int) Math.min(Integer.MAX_VALUE, (long) perOperation * operations);
                int accepted;
                try (var transaction = Transaction.openRoot()) {
                    accepted = insert(new Placement(key, slot, perOperation), wanted, transaction);
                }
                operations = Math.min(operations, accepted / perOperation);
                if (operations <= 0) return null;
                placements.add(new Placement(key, slot, perOperation));
            }
        }
        return new InputPlan(operations, placements);
    }

    private int emptySlot(CraftKey key, boolean[] used) {
        for (int slot = 0; slot < used.length; slot++) {
            if (used[slot]) continue;
            if (key.isFluid() ? fluids.isFluidValid(slot, key.sampleFluid()) : items.isItemValid(slot, key.sampleItem())) {
                return slot;
            }
        }
        return -1;
    }

    private int insert(Placement placement, int amount, TransactionContext transaction) {
        return placement.key().isFluid()
                ? fluids.insert(placement.slot(), FluidResource.of(placement.key().sampleFluid()), amount, transaction)
                : items.insert(placement.slot(), ItemResource.of(placement.key().sampleItem()), amount, transaction);
    }

    /** A refusal aborts every input insertion, including notifications, before the processor can tick. */
    public boolean insert(InputPlan plan) {
        if (plan.operations() <= 0 || !inputsEmpty()) return false;
        try (var transaction = Transaction.openRoot()) {
            for (Placement placement : plan.placements()) {
                int wanted = Math.multiplyExact(placement.amountPerOperation(), plan.operations());
                if (insert(placement, wanted, transaction) != wanted) return false;
            }
            transaction.commit();
        }
        return true;
    }

    public Map<CraftKey, Integer> collectOutputs() {
        Map<CraftKey, Integer> collected = new LinkedHashMap<>();
        try (var transaction = Transaction.openRoot()) {
            for (int s = inputSlots; s < inputSlots + outputSlots; s++) {
                ItemResource resource = items.getResource(s);
                if (resource.isEmpty()) continue;
                int extracted = items.extract(s, resource, Integer.MAX_VALUE, transaction);
                if (extracted > 0) collected.merge(CraftKey.of(resource.toStack(1)), extracted, Math::addExact);
            }
            for (int t = inputTanks; t < inputTanks + outputTanks; t++) {
                FluidResource resource = fluids.getResource(t);
                if (resource.isEmpty()) continue;
                int extracted = fluids.extract(t, resource, Integer.MAX_VALUE, transaction);
                if (extracted > 0) collected.merge(CraftKey.of(resource.toStack(1)), extracted, Math::addExact);
            }
            transaction.commit();
        }
        return collected;
    }
}
