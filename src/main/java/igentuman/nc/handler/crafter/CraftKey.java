package igentuman.nc.handler.crafter;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

public record CraftKey(@Nullable Item item, @Nullable Fluid fluid, @Nullable CompoundTag tag) {
    public CraftKey {
        tag = tag == null ? null : tag.copy();
    }

    public static CraftKey of(ItemStack stack) {
        return new CraftKey(stack.getItem(), null, stack.getTag());
    }

    public static CraftKey of(FluidStack stack) {
        return new CraftKey(null, stack.getFluid(), stack.getTag());
    }

    public static CraftKey of(AggregatedInventory.ItemKey key) {
        return new CraftKey(key.item(), null, key.tag());
    }

    public boolean isFluid() { return fluid != null; }
    public boolean isItem() { return item != null; }

    public ItemStack sampleItem() {
        if (item == null) return ItemStack.EMPTY;
        ItemStack stack = new ItemStack(item);
        if (tag != null) stack.setTag(tag.copy());
        return stack;
    }

    public FluidStack sampleFluid() { return sampleFluid(1); }

    public FluidStack sampleFluid(int amount) {
        if (fluid == null) return FluidStack.EMPTY;
        return new FluidStack(fluid, amount, tag == null ? null : tag.copy());
    }

    public AggregatedInventory.ItemKey toItemKey() {
        return new AggregatedInventory.ItemKey(item, tag == null ? null : tag.copy());
    }
}
