package igentuman.nc.compat.oc2;

import igentuman.nc.block_entity.MultiblockControllerBE;
import igentuman.nc.block_entity.UniversalProcessorBE;
import igentuman.nc.block_entity.accelerator.AbstractAcceleratorControllerBE;
import igentuman.nc.block_entity.accelerator.BeamDiverterControllerBE;
import igentuman.nc.block_entity.fission.FissionReactorControllerBE;
import igentuman.nc.block_entity.fission.MsrControllerBE;
import igentuman.nc.block_entity.fusion.FusionReactorControllerBE;
import igentuman.nc.block_entity.heat_exchanger.HeatExchangerControllerBE;
import igentuman.nc.block_entity.kugelblitz.ChamberTerminalBE;
import igentuman.nc.block_entity.particle.ParticleChamberControllerBE;
import igentuman.nc.block_entity.turbine.TurbineControllerBE;
import igentuman.nc.compat.computer.AcceleratorApi;
import igentuman.nc.compat.computer.BeamDiverterApi;
import igentuman.nc.compat.computer.ComputerApi;
import igentuman.nc.compat.computer.ControllerApi;
import igentuman.nc.compat.computer.EngineersCrafterApi;
import igentuman.nc.compat.computer.FissionReactorApi;
import igentuman.nc.compat.computer.FusionReactorApi;
import igentuman.nc.compat.computer.HeatExchangerApi;
import igentuman.nc.compat.computer.KugelblitzApi;
import igentuman.nc.compat.computer.MsrApi;
import igentuman.nc.compat.computer.ParticleChamberApi;
import igentuman.nc.compat.computer.ProcessorApi;
import igentuman.nc.compat.computer.TurbineApi;
import igentuman.nc.registration.ModEntry;
import igentuman.nc.setup.ModEntries;
import igentuman.nc.setup.entries.Crafter;
import li.cil.oc2.api.bus.device.Device;
import li.cil.oc2.api.bus.device.object.ObjectDevice;
import li.cil.oc2.api.neoforge.Capabilities;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.jetbrains.annotations.Nullable;

import static igentuman.nc.compat.computer.ComputerErrors.DEFAULT;

public class OC2CompatHandler {

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(OC2CompatHandler::registerCapabilities);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        for (ModEntry entry : ModEntries.ENTRIES.values()) {
            if (!entry.hasBlockEntity()) continue;
            event.registerBlockEntity(Capabilities.Device.BLOCK, entry.blockEntity().get(),
                    (be, side) -> device(api(be)));
        }
        event.registerBlockEntity(Capabilities.Device.BLOCK, Crafter.ENGINEERS_CRAFTING_TABLE_BE.get(),
                (be, side) -> device(new EngineersCrafterApi(be, DEFAULT)));
    }

    @Nullable
    private static Device device(@Nullable ComputerApi<?> api) {
        return api == null ? null : new ObjectDevice(api, api.typeName());
    }

    @Nullable
    private static ComputerApi<?> api(Object be) {
        return switch (be) {
            case FissionReactorControllerBE c -> new FissionReactorApi(c, DEFAULT);
            case MsrControllerBE c -> new MsrApi(c, DEFAULT);
            case FusionReactorControllerBE c -> new FusionReactorApi(c, DEFAULT);
            case TurbineControllerBE c -> new TurbineApi(c, DEFAULT);
            case HeatExchangerControllerBE c -> new HeatExchangerApi(c, DEFAULT);
            case ChamberTerminalBE c -> new KugelblitzApi(c, DEFAULT);
            case BeamDiverterControllerBE c -> new BeamDiverterApi(c, DEFAULT);
            case AbstractAcceleratorControllerBE c -> new AcceleratorApi<>(c, DEFAULT);
            case ParticleChamberControllerBE c -> new ParticleChamberApi(c, DEFAULT);
            case MultiblockControllerBE c -> new ControllerApi<>(c, DEFAULT);
            case UniversalProcessorBE p -> new ProcessorApi(p, DEFAULT);
            default -> null;
        };
    }
}
