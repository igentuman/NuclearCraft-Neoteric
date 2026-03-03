package igentuman.nc.util;

import igentuman.api.platform.NCSerialization;
import igentuman.nc.util.annotation.NothingNullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.fluids.IFluidTank;

import java.util.List;

@NothingNullByDefault
public class DataHandlerUtils {

    private DataHandlerUtils() {
    }

    /**
     * Helper to read and load a list of containers from a {@link ListTag}
     */
    public static void readContainers(HolderLookup.Provider provider, List<? extends INBTSerializable<CompoundTag>> containers, ListTag storedContainers) {
        readContents(provider, containers, storedContainers, getTagByType(containers));
    }

    /**
     * Helper to read and load a list of containers to a {@link ListTag}
     */
    public static ListTag writeContainers(HolderLookup.Provider provider, List<? extends INBTSerializable<CompoundTag>> containers) {
        return writeContents(provider, containers, getTagByType(containers));
    }

    /**
     * Helper to read and load a list of handler contents from a {@link ListTag}
     */
    public static void readContents(HolderLookup.Provider provider, List<? extends INBTSerializable<CompoundTag>> contents, ListTag storedContents, String key) {
        int size = contents.size();
        for (int tagCount = 0; tagCount < storedContents.size(); tagCount++) {
            CompoundTag tagCompound = storedContents.getCompound(tagCount);
            byte id = tagCompound.getByte(key);
            if (id >= 0 && id < size) {
                NCSerialization.deserialize(contents.get(id), provider, tagCompound);
            }
        }
    }

    /**
     * Helper to read and load a list of handler contents to a {@link ListTag}
     */
    public static ListTag writeContents(HolderLookup.Provider provider, List<? extends INBTSerializable<CompoundTag>> contents, String key) {
        ListTag storedContents = new ListTag();
        for (int tank = 0; tank < contents.size(); tank++) {
            CompoundTag tagCompound = NCSerialization.serialize(contents.get(tank), provider);
            if (!tagCompound.isEmpty()) {
                tagCompound.putByte(key, (byte) tank);
                storedContents.add(tagCompound);
            }
        }
        return storedContents;
    }

    // keep this only for backwards compat
    private static String getTagByType(List<? extends INBTSerializable<CompoundTag>> containers) {
        if (containers.isEmpty()) {
            return NBTConstants.CONTAINER;
        }
        INBTSerializable<CompoundTag> obj = containers.get(0);
        if (obj instanceof IFluidTank) {
            return NBTConstants.TANK;
        }
        return NBTConstants.CONTAINER;
    }

    /**
     * Helper to calculate what the maximum id is in a list of contents.
     */
    public static int getMaxId(ListTag storedContents, String key) {
        int maxId = -1;
        for (int tagCount = 0; tagCount < storedContents.size(); tagCount++) {
            byte id = storedContents.getCompound(tagCount).getByte(key);
            if (id > maxId) {
                maxId = id;
            }
        }
        return maxId + 1;
    }
}