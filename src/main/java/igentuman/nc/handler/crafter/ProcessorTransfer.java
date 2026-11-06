package igentuman.nc.handler.crafter;

import igentuman.nc.block.entity.processor.NCProcessorBE;
import igentuman.nc.handler.crafter.AutoCraftSolver.PatternDef;
import igentuman.nc.handler.sided.capability.FluidCapabilityHandler;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.items.IItemHandler;

public record ProcessorTransfer(IItemHandler items, FluidCapabilityHandler fluids,
                                int inputSlots, int outputSlots, int inputTanks, int outputTanks) {

    public static ProcessorTransfer of(NCProcessorBE proc) {
        var handler = proc.contentHandler();
        return new ProcessorTransfer(handler.itemHandler, handler.fluidHandler,
                handler.inputItemSlots, handler.outputItemSlots, handler.inputFluidSlots, handler.outputFluidSlots);
    }

    public boolean inputsEmpty() {
        for (int s = 0; s < inputSlots; s++) if (!items.getStackInSlot(s).isEmpty()) return false;
        for (int t = 0; t < inputTanks; t++) if (!fluids.tanks.get(t).getFluid().isEmpty()) return false;
        return true;
    }

    public record Placement(CraftKey key, int slot, int amountPerOperation) {}

    public record InputPlan(int operations, List<Placement> placements) {
        public InputPlan {
            placements = List.copyOf(placements);
        }
    }

    public InputPlan plan(PatternDef<CraftKey> pattern, int requested) {
        if (!inputsEmpty()) return null;
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
                int accepted = insert(new Placement(key, slot, perOperation), wanted, true);
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
            if (key.isFluid() ? fluids.tanks.get(slot).isFluidValid(key.sampleFluid()) : items.isItemValid(slot, key.sampleItem())) {
                return slot;
            }
        }
        return -1;
    }

    private int insert(Placement placement, int amount, boolean simulate) {
        if (placement.key().isFluid()) {
            return fluids.tanks.get(placement.slot()).fill(placement.key().sampleFluid(amount),
                    simulate ? FluidAction.SIMULATE : FluidAction.EXECUTE);
        }
        ItemStack stack = placement.key().sampleItem().copyWithCount(amount);
        return amount - items.insertItem(placement.slot(), stack, simulate).getCount();
    }

    public boolean insert(InputPlan plan) {
        List<Integer> accepted = new ArrayList<>();
        for (Placement placement : plan.placements()) {
            int wanted = placement.amountPerOperation() * plan.operations();
            int inserted = insert(placement, wanted, false);
            accepted.add(inserted);
            if (inserted != wanted) {
                for (int i = accepted.size() - 1; i >= 0; i--) {
                    Placement rollback = plan.placements().get(i);
                    if (rollback.key().isFluid()) fluids.tanks.get(rollback.slot()).drain(accepted.get(i), FluidAction.EXECUTE);
                    else items.extractItem(rollback.slot(), accepted.get(i), false);
                }
                return false;
            }
        }
        return true;
    }

    public Map<CraftKey, Integer> collectOutputs() {
        Map<CraftKey, Integer> collected = new LinkedHashMap<>();
        for (int s = inputSlots; s < inputSlots + outputSlots; s++) {
            ItemStack stack = items.extractItem(s, Integer.MAX_VALUE, false);
            if (!stack.isEmpty()) collected.merge(CraftKey.of(stack), stack.getCount(), Integer::sum);
        }
        for (int t = inputTanks; t < inputTanks + outputTanks; t++) {
            FluidStack fluid = fluids.tanks.get(t).drain(Integer.MAX_VALUE, FluidAction.EXECUTE);
            if (!fluid.isEmpty()) collected.merge(CraftKey.of(fluid), fluid.getAmount(), Integer::sum);
        }
        return collected;
    }
}
