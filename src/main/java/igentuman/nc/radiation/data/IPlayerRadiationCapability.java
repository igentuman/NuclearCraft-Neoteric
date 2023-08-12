package igentuman.nc.radiation.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;


public interface IPlayerRadiationCapability extends INBTSerializable<CompoundTag> {
    int getRadiation();
    void setRadiation(int radiation);

    int getTimestamp();
    void setTimestamp(int timestamp);
}
