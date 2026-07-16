package igentuman.nc.handler.crafter;

import igentuman.nc.setup.registration.NCCrafter;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * A crafting pattern encoded onto a {@link NCCrafter#CRAFTING_PATTERN} item via NBT.
 * Inputs are stored one-per-occupied-grid-slot with count normalized to 1
 * (a vanilla crafting recipe consumes exactly one item per occupied slot).
 */
public class CraftingPattern {

    public static final String TAG = "NCPattern";
    private static final String INPUTS = "Inputs";
    private static final String OUTPUT = "Output";
    private static final String SLOT = "Slot";

    public static final int GRID_SIZE = 9;

    private final NonNullList<ItemStack> inputs;
    private final ItemStack output;

    public CraftingPattern(NonNullList<ItemStack> inputs, ItemStack output) {
        this.inputs = inputs;
        this.output = output;
    }

    public NonNullList<ItemStack> inputs() {
        return inputs;
    }

    public ItemStack output() {
        return output;
    }

    /** True for a pattern item that carries an encoded recipe. */
    public static boolean isPattern(ItemStack stack) {
        return isEncoded(stack);
    }

    public static boolean isPatternItem(ItemStack stack) {
        return !stack.isEmpty() && stack.is(NCCrafter.CRAFTING_PATTERN.get());
    }

    public static boolean isEncoded(ItemStack stack) {
        return isPatternItem(stack) && stack.hasTag() && stack.getOrCreateTag().contains(TAG);
    }

    public static boolean isBlank(ItemStack stack) {
        return isPatternItem(stack) && !isEncoded(stack);
    }

    /** The encoded output stack, or empty when the stack is not an encoded pattern. */
    public static ItemStack output(ItemStack stack) {
        if (!isEncoded(stack)) return ItemStack.EMPTY;
        CompoundTag pattern = stack.getOrCreateTag().getCompound(TAG);
        return ItemStack.of(pattern.getCompound(OUTPUT));
    }

    /** Encodes the grid + output onto a single blank {@link NCCrafter#CRAFTING_PATTERN}. */
    public static ItemStack encode(Container grid, ItemStack output) {
        CompoundTag pattern = new CompoundTag();
        ListTag list = new ListTag();
        for (int i = 0; i < GRID_SIZE && i < grid.getContainerSize(); i++) {
            ItemStack s = grid.getItem(i);
            if (s.isEmpty()) continue;
            ItemStack one = s.copy();
            one.setCount(1);
            CompoundTag entry = new CompoundTag();
            entry.putByte(SLOT, (byte) i);
            one.save(entry);
            list.add(entry);
        }
        pattern.put(INPUTS, list);
        pattern.put(OUTPUT, output.save(new CompoundTag()));

        ItemStack encoded = new ItemStack(NCCrafter.CRAFTING_PATTERN.get());
        encoded.getOrCreateTag().put(TAG, pattern);
        return encoded;
    }

    @Nullable
    public static CraftingPattern from(ItemStack stack) {
        if (!isPattern(stack)) return null;
        CompoundTag pattern = stack.getOrCreateTag().getCompound(TAG);
        NonNullList<ItemStack> inputs = NonNullList.withSize(GRID_SIZE, ItemStack.EMPTY);
        ListTag list = pattern.getList(INPUTS, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            int slot = entry.getByte(SLOT) & 0xFF;
            if (slot >= GRID_SIZE) continue;
            inputs.set(slot, ItemStack.of(entry));
        }
        ItemStack output = ItemStack.of(pattern.getCompound(OUTPUT));
        if (output.isEmpty()) return null;
        return new CraftingPattern(inputs, output);
    }
}
