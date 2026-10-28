package igentuman.nc.handler.crafter;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

public record CraftKey(@Nullable Item item, @Nullable Fluid fluid, DataComponentPatch patch) {

    public static CraftKey of(ItemStack stack) {
        return new CraftKey(stack.getItem(), null, stack.getComponentsPatch());
    }

    public static CraftKey of(FluidStack stack) {
        return new CraftKey(null, stack.getFluid(), stack.getComponentsPatch());
    }

    public static CraftKey of(AggregatedInventory.ItemKey key) {
        return new CraftKey(key.item(), null, key.patch());
    }

    public boolean isFluid() {
        return fluid != null;
    }

    public boolean isItem() {
        return item != null;
    }

    public ItemStack sampleItem() {
        if (item == null) return ItemStack.EMPTY;
        ItemStack stack = new ItemStack(item);
        stack.applyComponents(patch);
        return stack;
    }

    public FluidStack sampleFluid() {
        if (fluid == null) return FluidStack.EMPTY;
        FluidStack stack = new FluidStack(fluid, 1);
        stack.applyComponents(patch);
        return stack;
    }

    public FluidStack sampleFluid(int amount) {
        FluidStack s = sampleFluid();
        if (!s.isEmpty()) s.setAmount(amount);
        return s;
    }

    public AggregatedInventory.ItemKey toItemKey() {
        return new AggregatedInventory.ItemKey(item, patch);
    }
}
