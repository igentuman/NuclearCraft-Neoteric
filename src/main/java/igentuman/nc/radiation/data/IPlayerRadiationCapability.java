package igentuman.nc.radiation.data;

import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.INBTSerializable;


public interface IPlayerRadiationCapability extends INBTSerializable<CompoundNBT> {
    int getRadiation();
    void setRadiation(int radiation);

    int getTimestamp();
    void setTimestamp(int timestamp);
}
