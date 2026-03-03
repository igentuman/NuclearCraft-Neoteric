package igentuman.nc.radiation.data;

import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;


public interface IPlayerRadiationCapability extends INBTSerializable<CompoundTag> {
    long getRadiation();
    void setRadiation(long radiation);

    int getTimestamp();
    void setTimestamp(int timestamp);
}
