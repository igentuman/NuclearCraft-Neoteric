package igentuman.nc.handler.storage;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * Fluid handler for a barrel in item form. Reads and writes the same {@code FluidTank} tag the
 * {@link igentuman.nc.block_entity.storage.BarrelBE} persists in the stack's block-entity data,
 * so a barrel filled while sitting inside the crafter places back down with its content.
 */
public class BarrelItemFluidHandler implements IFluidHandlerItem {

    private static final String TANK_KEY = "FluidTank";

    private final ItemStack stack;
    private final int capacity;
    private final Supplier<BlockEntityType<?>> beType;

    public BarrelItemFluidHandler(ItemStack stack, int capacity, Supplier<BlockEntityType<?>> beType) {
        this.stack = stack;
        this.capacity = capacity;
        this.beType = beType;
    }

    @Override
    public @NotNull ItemStack getContainer() {
        return stack;
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        return tank == 0 ? read() : FluidStack.EMPTY;
    }

    @Override
    public int getTankCapacity(int tank) {
        return tank == 0 ? capacity : 0;
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack fluid) {
        return tank == 0;
    }

    @Override
    public int fill(@NotNull FluidStack resource, FluidAction action) {
        if (resource.isEmpty() || stack.getCount() != 1) return 0;
        FluidStack current = read();
        if (!current.isEmpty() && !FluidStack.isSameFluidSameComponents(current, resource)) return 0;
        int space = capacity - current.getAmount();
        int filled = Math.min(space, resource.getAmount());
        if (filled <= 0) return 0;
        if (action.execute()) {
            FluidStack next = current.isEmpty() ? resource.copyWithAmount(filled) : current.copyWithAmount(current.getAmount() + filled);
            write(next);
        }
        return filled;
    }

    @Override
    public @NotNull FluidStack drain(@NotNull FluidStack resource, FluidAction action) {
        FluidStack current = read();
        if (resource.isEmpty() || current.isEmpty() || !FluidStack.isSameFluidSameComponents(current, resource)) {
            return FluidStack.EMPTY;
        }
        return drain(resource.getAmount(), action);
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
        FluidStack current = read();
        if (current.isEmpty() || maxDrain <= 0 || stack.getCount() != 1) return FluidStack.EMPTY;
        int drained = Math.min(maxDrain, current.getAmount());
        if (action.execute()) {
            int left = current.getAmount() - drained;
            write(left <= 0 ? FluidStack.EMPTY : current.copyWithAmount(left));
        }
        return current.copyWithAmount(drained);
    }

    private FluidStack read() {
        CustomData data = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        if (data == null) return FluidStack.EMPTY;
        CompoundTag root = data.copyTag();
        if (!root.contains(TANK_KEY, Tag.TAG_COMPOUND)) return FluidStack.EMPTY;
        ListTag tanks = root.getCompound(TANK_KEY).getList("Tanks", Tag.TAG_COMPOUND);
        for (int i = 0; i < tanks.size(); i++) {
            CompoundTag t = tanks.getCompound(i);
            if (t.getInt("Tank") != 0) continue;
            Tag fluidTag = t.contains("Fluid") ? t.get("Fluid") : t;
            return FluidStack.OPTIONAL_CODEC.parse(NbtOps.INSTANCE, fluidTag).result().orElse(FluidStack.EMPTY);
        }
        return FluidStack.EMPTY;
    }

    private void write(FluidStack fluid) {
        CompoundTag root = stack.getOrDefault(DataComponents.BLOCK_ENTITY_DATA, CustomData.EMPTY).copyTag();
        if (fluid.isEmpty()) {
            root.remove(TANK_KEY);
        } else {
            CompoundTag tankTag = new CompoundTag();
            tankTag.putInt("Tank", 0);
            tankTag.putInt("Capacity", capacity);
            FluidStack.CODEC.encodeStart(NbtOps.INSTANCE, fluid).result().ifPresent(t -> tankTag.put("Fluid", t));
            ListTag list = new ListTag();
            list.add(tankTag);
            CompoundTag handler = new CompoundTag();
            handler.put("Tanks", list);
            handler.putInt("Size", 1);
            root.put(TANK_KEY, handler);
        }
        // BlockItem.setBlockEntityData strips the component when only the id is left
        BlockItem.setBlockEntityData(stack, beType.get(), root);
    }
}
