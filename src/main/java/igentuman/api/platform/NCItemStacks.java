package igentuman.api.platform;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * Platform translation layer for ItemStack custom data.
 * <p>
 * MC 1.21 removed {@code ItemStack.getOrCreateTag()}, {@code getTag()},
 * {@code setTag()}, and {@code hasTag()}. All custom item data now lives in the
 * {@link DataComponents#CUSTOM_DATA} component backed by {@link CustomData}.
 * <p>
 * This class isolates all platform-specific data-component code in one place
 * so that NuclearCraft's content code never touches DataComponents directly.
 */
public final class NCItemStacks {

    private NCItemStacks() {}

    // ---- Tag-level operations ----

    /** Replaces {@code stack.hasTag()}. */
    public static boolean hasCustomData(ItemStack stack) {
        return stack.has(DataComponents.CUSTOM_DATA);
    }

    /**
     * Replaces {@code stack.getTag()}.
     * Returns a COPY, or null if no data exists.
     */
    @Nullable
    public static CompoundTag getTag(ItemStack stack) {
        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        return cd != null ? cd.copyTag() : null;
    }

    /**
     * Replaces {@code stack.getOrCreateTag()} for read contexts.
     * Returns a COPY — never null.
     */
    @NotNull
    public static CompoundTag getTagCopy(ItemStack stack) {
        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        return cd != null ? cd.copyTag() : new CompoundTag();
    }

    /**
     * Replaces {@code stack.setTag(tag)}.
     * Passing null or an empty tag removes the component entirely.
     */
    public static void setTag(ItemStack stack, @Nullable CompoundTag tag) {
        if (tag == null || tag.isEmpty()) {
            stack.remove(DataComponents.CUSTOM_DATA);
        } else {
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
    }

    /**
     * Atomic read-modify-write. The consumer receives a mutable tag;
     * changes are saved back automatically.
     * Replaces the {@code stack.getOrCreateTag().putXxx(...)} pattern.
     */
    public static void modifyTag(ItemStack stack, Consumer<CompoundTag> modifier) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, modifier);
    }

    /** Replaces {@code stack.getOrCreateTag().contains(key)}. */
    public static boolean contains(ItemStack stack, String key) {
        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        return cd != null && cd.contains(key);
    }

    /** Replaces {@code stack.removeTagKey(key)}. */
    public static void remove(ItemStack stack, String key) {
        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        if (cd != null && cd.contains(key)) {
            CompoundTag tag = cd.copyTag();
            tag.remove(key);
            setTag(stack, tag);
        }
    }

    // ---- Typed getters (zero-copy via getUnsafe) ----

    public static int getInt(ItemStack stack, String key) {
        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        return cd != null ? cd.getUnsafe().getInt(key) : 0;
    }

    public static long getLong(ItemStack stack, String key) {
        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        return cd != null ? cd.getUnsafe().getLong(key) : 0L;
    }

    public static boolean getBoolean(ItemStack stack, String key) {
        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        return cd != null && cd.getUnsafe().getBoolean(key);
    }

    public static double getDouble(ItemStack stack, String key) {
        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        return cd != null ? cd.getUnsafe().getDouble(key) : 0.0;
    }

    public static String getString(ItemStack stack, String key) {
        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        return cd != null ? cd.getUnsafe().getString(key) : "";
    }

    public static CompoundTag getCompound(ItemStack stack, String key) {
        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        return cd != null ? cd.getUnsafe().getCompound(key) : new CompoundTag();
    }

    public static ListTag getList(ItemStack stack, String key, int tagType) {
        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        return cd != null ? cd.getUnsafe().getList(key, tagType) : new ListTag();
    }

    public static UUID getUUID(ItemStack stack, String key) {
        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        return cd != null && cd.getUnsafe().hasUUID(key) ? cd.getUnsafe().getUUID(key) : null;
    }

    public static boolean hasUUID(ItemStack stack, String key) {
        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        return cd != null && cd.getUnsafe().hasUUID(key);
    }

    public static long[] getLongArray(ItemStack stack, String key) {
        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        return cd != null ? cd.getUnsafe().getLongArray(key) : new long[0];
    }

    // ---- Typed setters (atomic read-modify-write) ----

    public static void putInt(ItemStack stack, String key, int value) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putInt(key, value));
    }

    public static void putLong(ItemStack stack, String key, long value) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putLong(key, value));
    }

    public static void putBoolean(ItemStack stack, String key, boolean value) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putBoolean(key, value));
    }

    public static void putDouble(ItemStack stack, String key, double value) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putDouble(key, value));
    }

    public static void putString(ItemStack stack, String key, String value) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putString(key, value));
    }

    public static void putCompound(ItemStack stack, String key, CompoundTag value) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.put(key, value));
    }

    public static void putList(ItemStack stack, String key, ListTag value) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.put(key, value));
    }

    public static void putUUID(ItemStack stack, String key, UUID value) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putUUID(key, value));
    }

    public static void putLongArray(ItemStack stack, String key, long[] value) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putLongArray(key, value));
    }

    // ---- Stack utility methods (1.20→1.21 migration) ----

    /**
     * Create a copy of the given stack with a different count.
     * Replaces {@code ItemHandlerHelper.copyStackWithSize(stack, count)}.
     */
    public static ItemStack copyWithCount(ItemStack stack, int count) {
        return stack.copyWithCount(count);
    }

    /**
     * Check if two stacks can be merged (same item + same data components).
     * Replaces {@code ItemHandlerHelper.canItemStacksStack(a, b)}
     * and {@code ItemStack.isSameItemSameTags(a, b)}.
     */
    public static boolean canStack(ItemStack a, ItemStack b) {
        return ItemStack.isSameItemSameComponents(a, b);
    }
}
