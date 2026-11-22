package igentuman.nc.util.persistence;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

/** Retains the old homogeneous-list fallback when reading legacy raw NBT. */
public final class LegacyTagReaders {
    private LegacyTagReaders() {}

    public static ListTag list(CompoundTag tag, String name, int elementType) {
        ListTag list = tag.getListOrEmpty(name);
        // Modern lists can mix tag types; old getList(name, type) rejected a wrong type.
        for (var element : list) {
            if (element.getId() != elementType) return new ListTag();
        }
        return list;
    }
}
