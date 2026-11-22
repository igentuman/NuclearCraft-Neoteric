package igentuman.nc.handler.energy;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandlerUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStackResourceHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Predicate;

/** Charges a validated machine slot, publishing its replacement only after the transfer commits. */
public final class ItemEnergyTransfer {
    private ItemEnergyTransfer() {}

    /** Creates one access for an operation; reuse it for every exchange within the same transaction. */
    public static ItemAccess forSlot(ItemStack stack, int slotLimit,
                                     Predicate<ItemStack> validator, Consumer<ItemStack> onCommit) {
        var slot = new ItemStackResourceHandler() {
            private ItemStack contents = stack.copy();
            @Override protected ItemStack getStack() { return contents; }
            @Override protected void setStack(ItemStack replacement) { contents = replacement; }
            @Override protected boolean isValid(ItemResource resource) { return validator.test(resource.toStack()); }
            @Override protected int getCapacity(ItemResource resource) {
                return Math.min(slotLimit, super.getCapacity(resource));
            }
            @Override protected void onRootCommit(ItemStack original) { onCommit.accept(contents); }
        };
        return ItemAccess.forHandlerIndexStrict(slot, 0);
    }

    public static int charge(EnergyHandler source, ItemAccess target, int amount,
                             @Nullable TransactionContext transaction) {
        return EnergyHandlerUtil.move(source, target.getCapability(Capabilities.Energy.ITEM), amount, transaction);
    }
}
