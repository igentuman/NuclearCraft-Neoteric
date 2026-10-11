package igentuman.nc.compat.tis3d;

import igentuman.nc.block_entity.fission.FissionPortBE;
import igentuman.nc.block_entity.fission.FissionReactorControllerBE;
import igentuman.nc.block_entity.fusion.FusionReactorControllerBE;
import li.cil.tis3d.api.serial.SerialInterfaceProvider;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import static igentuman.nc.NuclearCraft.MODID;

public class TIS3DCompatHandler {

    private static final DeferredRegister<SerialInterfaceProvider> SERIAL_PROVIDERS =
            DeferredRegister.create(SerialInterfaceProvider.REGISTRY, MODID);

    static {
        SERIAL_PROVIDERS.register("fission_reactor", () -> new ControllerSerialProvider(
                FissionReactorControllerBE.class, "nuclearcraft_fission", 1,
                FissionPortBE.MODE_ENERGY, FissionPortBE.MODE_HEAT, FissionPortBE.MODE_PROGRESS, FissionPortBE.MODE_ITEMS));
        SERIAL_PROVIDERS.register("fusion_reactor", () -> new ControllerSerialProvider(
                FusionReactorControllerBE.class, "nuclearcraft_fusion", 1,
                FusionReactorControllerBE.MODE_ENERGY, FusionReactorControllerBE.MODE_HEAT, FusionReactorControllerBE.MODE_EFFICIENCY));
    }

    public static void register(IEventBus modEventBus) {
        SERIAL_PROVIDERS.register(modEventBus);
    }
}
