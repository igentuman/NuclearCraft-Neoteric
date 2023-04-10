package igentuman.nc.capability;

import igentuman.nc.handler.radiation.capability.IRadiationEntity;
import igentuman.nc.handler.radiation.capability.IRadiationShielding;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public class Capabilities {

    private Capabilities() {
    }

    public static final Capability<IRadiationShielding> RADIATION_SHIELDING = CapabilityManager.get(new CapabilityToken<>() {});

    public static final Capability<IRadiationEntity> RADIATION_ENTITY = CapabilityManager.get(new CapabilityToken<>() {});

}