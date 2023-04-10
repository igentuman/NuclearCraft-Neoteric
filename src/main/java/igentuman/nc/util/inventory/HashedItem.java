package igentuman.nc.util.inventory;

import igentuman.nc.util.StackUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class HashedItem {

    public static HashedItem create(@NotNull ItemStack stack) {
        return new HashedItem(StackUtils.size(stack, 1));
    }

    public static HashedItem raw(@NotNull ItemStack stack) {
        return new HashedItem(stack);
    }

    @NotNull
    private final ItemStack itemStack;
    private final int hashCode;

    protected HashedItem(@NotNull ItemStack stack) {
        this.itemStack = stack;
        this.hashCode = initHashCode();
    }

    protected HashedItem(HashedItem other) {
        this.itemStack = other.itemStack;
        this.hashCode = other.hashCode;
    }

    @NotNull
    public ItemStack getStack() {
        return itemStack;
    }

    @NotNull
    public ItemStack createStack(int size) {
        return StackUtils.size(itemStack, size);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        return obj instanceof HashedItem other && ItemHandlerHelper.canItemStacksStack(itemStack, other.itemStack);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    private int initHashCode() {
        int code = 1;
        code = 31 * code + itemStack.getItem().hashCode();
        if (itemStack.hasTag()) {
            code = 31 * code + itemStack.getTag().hashCode();
        }
        return code;
    }

    public static class UUIDAwareHashedItem extends HashedItem {

        private final UUID uuid;
        private final boolean overrideHash;

        /**
         * @apiNote For use on the client side, hash is taken into account for equals and hashCode
         */
        public UUIDAwareHashedItem(ItemStack stack, UUID uuid) {
            super(StackUtils.size(stack, 1));
            this.uuid = uuid;
            this.overrideHash = true;
        }

        public UUIDAwareHashedItem(HashedItem other, UUID uuid) {
            super(other);
            this.uuid = uuid;
            this.overrideHash = false;
        }

        @Nullable
        public UUID getUUID() {
            return uuid;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (overrideHash && uuid != null) {
                return obj instanceof UUIDAwareHashedItem uuidAware && uuid.equals(uuidAware.uuid) && super.equals(obj);
            }
            return super.equals(obj);
        }

        @Override
        public int hashCode() {
            if (overrideHash && uuid != null) {
                return 31 * super.hashCode() + uuid.hashCode();
            }
            return super.hashCode();
        }

        public HashedItem asRawHashedItem() {
            return new HashedItem(this);
        }
    }
}