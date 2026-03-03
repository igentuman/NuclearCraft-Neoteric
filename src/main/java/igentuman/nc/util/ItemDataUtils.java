package igentuman.nc.util;

import igentuman.api.platform.NCItemStacks;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

public final class ItemDataUtils {

    private ItemDataUtils() {
    }

    /**
     * Returns a READ-ONLY copy of the NC_DATA sub-compound.
     * Do NOT mutate the returned tag and expect changes to persist on the stack.
     * For writes, use the dedicated setter methods which call modifyTag().
     */
    @NotNull
    public static CompoundTag getDataMap(ItemStack stack) {
        CompoundTag tag = NCItemStacks.getTagCopy(stack);
        if (tag.contains(NBTConstants.NC_DATA, Tag.TAG_COMPOUND)) {
            return tag.getCompound(NBTConstants.NC_DATA);
        }
        return new CompoundTag();
    }

    @Nullable
    public static CompoundTag getDataMapIfPresent(ItemStack stack) {
        CompoundTag tag = NCItemStacks.getTag(stack);
        if (tag != null && tag.contains(NBTConstants.NC_DATA, Tag.TAG_COMPOUND)) {
            return tag.getCompound(NBTConstants.NC_DATA);
        }
        return null;
    }

    public static boolean hasData(ItemStack stack, String key, int type) {
        CompoundTag dataMap = getDataMapIfPresent(stack);
        return dataMap != null && dataMap.contains(key, type);
    }

    public static void removeData(ItemStack stack, String key) {
        CompoundTag existing = getDataMapIfPresent(stack);
        if (existing != null) {
            NCItemStacks.modifyTag(stack, tag -> {
                if (tag.contains(NBTConstants.NC_DATA, Tag.TAG_COMPOUND)) {
                    CompoundTag dataMap = tag.getCompound(NBTConstants.NC_DATA);
                    dataMap.remove(key);
                    if (dataMap.isEmpty()) {
                        //If our data map no longer has any elements after removing a piece of stored data
                        // then remove the data tag to make the stack nice and clean again
                        tag.remove(NBTConstants.NC_DATA);
                    }
                }
            });
        }
    }

    public static <T> T getDataValue(ItemStack stack, Function<CompoundTag, T> getter, T fallback) {
        CompoundTag dataMap = getDataMapIfPresent(stack);
        return dataMap == null ? fallback : getter.apply(dataMap);
    }

    public static int getInt(ItemStack stack, String key) {
        CompoundTag dataMap = getDataMapIfPresent(stack);
        return dataMap == null ? 0 : dataMap.getInt(key);
    }

    public static long getLong(ItemStack stack, String key) {
        CompoundTag dataMap = getDataMapIfPresent(stack);
        return dataMap == null ? 0 : dataMap.getLong(key);
    }

    public static boolean getBoolean(ItemStack stack, String key) {
        CompoundTag dataMap = getDataMapIfPresent(stack);
        return dataMap != null && dataMap.getBoolean(key);
    }

    public static double getDouble(ItemStack stack, String key) {
        CompoundTag dataMap = getDataMapIfPresent(stack);
        return dataMap == null ? 0 : dataMap.getDouble(key);
    }

    public static String getString(ItemStack stack, String key) {
        return getDataValue(stack, dataMap -> dataMap.getString(key), "");
    }

    public static CompoundTag getCompound(ItemStack stack, String key) {
        return getDataValue(stack, dataMap -> dataMap.getCompound(key), new CompoundTag());
    }

    /**
     * Gets or creates a sub-compound within the NC_DATA map.
     * Note: the returned tag is a snapshot. Callers that mutate it must
     * write it back via {@link #setCompound(ItemStack, String, CompoundTag)}.
     */
    public static CompoundTag getOrAddCompound(ItemStack stack, String key) {
        CompoundTag dataMap = getDataMap(stack);
        if (dataMap.contains(key, Tag.TAG_COMPOUND)) {
            return dataMap.getCompound(key);
        }
        CompoundTag compound = new CompoundTag();
        // Persist the new empty compound back to the stack
        NCItemStacks.modifyTag(stack, tag -> {
            if (!tag.contains(NBTConstants.NC_DATA, Tag.TAG_COMPOUND)) {
                tag.put(NBTConstants.NC_DATA, new CompoundTag());
            }
            tag.getCompound(NBTConstants.NC_DATA).put(key, compound);
        });
        return compound;
    }

    public static void setCompoundIfPresent(ItemStack stack, String key, Consumer<CompoundTag> setter) {
        CompoundTag dataMap = getDataMapIfPresent(stack);
        if (dataMap != null && dataMap.contains(key, Tag.TAG_COMPOUND)) {
            NCItemStacks.modifyTag(stack, tag -> {
                CompoundTag ncData = tag.getCompound(NBTConstants.NC_DATA);
                if (ncData.contains(key, Tag.TAG_COMPOUND)) {
                    setter.accept(ncData.getCompound(key));
                }
            });
        }
    }

    @Nullable
    public static UUID getUniqueID(ItemStack stack, String key) {
        CompoundTag dataMap = getDataMapIfPresent(stack);
        if (dataMap != null && dataMap.hasUUID(key)) {
            return dataMap.getUUID(key);
        }
        return null;
    }

    public static ListTag getList(ItemStack stack, String key) {
        return getDataValue(stack, dataMap -> dataMap.getList(key, Tag.TAG_COMPOUND), new ListTag());
    }

    public static void setInt(ItemStack stack, String key, int i) {
        NCItemStacks.modifyTag(stack, tag -> {
            if (!tag.contains(NBTConstants.NC_DATA, Tag.TAG_COMPOUND)) {
                tag.put(NBTConstants.NC_DATA, new CompoundTag());
            }
            tag.getCompound(NBTConstants.NC_DATA).putInt(key, i);
        });
    }

    public static void setIntOrRemove(ItemStack stack, String key, int i) {
        if (i == 0) {
            removeData(stack, key);
        } else {
            setInt(stack, key, i);
        }
    }

    public static void setLong(ItemStack stack, String key, long l) {
        NCItemStacks.modifyTag(stack, tag -> {
            if (!tag.contains(NBTConstants.NC_DATA, Tag.TAG_COMPOUND)) {
                tag.put(NBTConstants.NC_DATA, new CompoundTag());
            }
            tag.getCompound(NBTConstants.NC_DATA).putLong(key, l);
        });
    }

    public static void setLongOrRemove(ItemStack stack, String key, long l) {
        if (l == 0) {
            removeData(stack, key);
        } else {
            setLong(stack, key, l);
        }
    }

    public static void setBoolean(ItemStack stack, String key, boolean b) {
        NCItemStacks.modifyTag(stack, tag -> {
            if (!tag.contains(NBTConstants.NC_DATA, Tag.TAG_COMPOUND)) {
                tag.put(NBTConstants.NC_DATA, new CompoundTag());
            }
            tag.getCompound(NBTConstants.NC_DATA).putBoolean(key, b);
        });
    }

    public static void setDouble(ItemStack stack, String key, double d) {
        NCItemStacks.modifyTag(stack, tag -> {
            if (!tag.contains(NBTConstants.NC_DATA, Tag.TAG_COMPOUND)) {
                tag.put(NBTConstants.NC_DATA, new CompoundTag());
            }
            tag.getCompound(NBTConstants.NC_DATA).putDouble(key, d);
        });
    }

    public static void setString(ItemStack stack, String key, String s) {
        NCItemStacks.modifyTag(stack, tag -> {
            if (!tag.contains(NBTConstants.NC_DATA, Tag.TAG_COMPOUND)) {
                tag.put(NBTConstants.NC_DATA, new CompoundTag());
            }
            tag.getCompound(NBTConstants.NC_DATA).putString(key, s);
        });
    }

    public static void setCompound(ItemStack stack, String key, CompoundTag compoundTag) {
        NCItemStacks.modifyTag(stack, tag -> {
            if (!tag.contains(NBTConstants.NC_DATA, Tag.TAG_COMPOUND)) {
                tag.put(NBTConstants.NC_DATA, new CompoundTag());
            }
            tag.getCompound(NBTConstants.NC_DATA).put(key, compoundTag);
        });
    }

    public static void setUUID(ItemStack stack, String key, @Nullable UUID uuid) {
        if (uuid == null) {
            removeData(stack, key);
        } else {
            NCItemStacks.modifyTag(stack, tag -> {
                if (!tag.contains(NBTConstants.NC_DATA, Tag.TAG_COMPOUND)) {
                    tag.put(NBTConstants.NC_DATA, new CompoundTag());
                }
                tag.getCompound(NBTConstants.NC_DATA).putUUID(key, uuid);
            });
        }
    }

    public static void setList(ItemStack stack, String key, ListTag listTag) {
        NCItemStacks.modifyTag(stack, tag -> {
            if (!tag.contains(NBTConstants.NC_DATA, Tag.TAG_COMPOUND)) {
                tag.put(NBTConstants.NC_DATA, new CompoundTag());
            }
            tag.getCompound(NBTConstants.NC_DATA).put(key, listTag);
        });
    }

    public static void setListOrRemove(ItemStack stack, String key, ListTag tag) {
        if (tag.isEmpty()) {
            removeData(stack, key);
        } else {
            setList(stack, key, tag);
        }
    }

    public static long[] getLongArray(ItemStack stack, String key) {
        return getDataValue(stack, dataMap -> dataMap.getLongArray(key), new long[0]);
    }

    public static void setLongArrayOrRemove(ItemStack stack, String key, long[] array) {
        if (array.length == 0) {
            removeData(stack, key);
        } else {
            NCItemStacks.modifyTag(stack, tag -> {
                if (!tag.contains(NBTConstants.NC_DATA, Tag.TAG_COMPOUND)) {
                    tag.put(NBTConstants.NC_DATA, new CompoundTag());
                }
                tag.getCompound(NBTConstants.NC_DATA).putLongArray(key, array);
            });
        }
    }

    public static void readContainers(HolderLookup.Provider provider, ItemStack stack, String containerKey, List<? extends INBTSerializable<CompoundTag>> containers) {
        if (!stack.isEmpty()) {
            DataHandlerUtils.readContainers(provider, containers, getList(stack, containerKey));
        }
    }

    public static void writeContainers(HolderLookup.Provider provider, ItemStack stack, String containerKey, List<? extends INBTSerializable<CompoundTag>> containers) {
        if (!stack.isEmpty()) {
            setListOrRemove(stack, containerKey, DataHandlerUtils.writeContainers(provider, containers));
        }
    }
}