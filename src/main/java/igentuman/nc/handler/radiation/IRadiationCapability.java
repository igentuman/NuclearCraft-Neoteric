package igentuman.nc.handler.radiation;

import net.minecraftforge.common.capabilities.AutoRegisterCapability;

@AutoRegisterCapability
public interface IRadiationCapability {
    int getRadiation();
    void setRadiation(int radiation);
    int getTimestamp();
    void setTimestamp(int timestamp);
}
