package igentuman.nc.util;

import net.minecraft.nbt.CompoundTag;

/** Buffer object persisted/synced as a nested CompoundTag under its owning @NBTField name. */
public interface NBTSerializable {
    void save(CompoundTag tag);
    void load(CompoundTag tag);
}
