package igentuman.nc.compat.cc;

import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.PeripheralCapability;
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
import igentuman.nc.registration.ModEntry;
import igentuman.nc.setup.ModEntries;
import igentuman.nc.setup.entries.Crafter;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.jetbrains.annotations.Nullable;

/** Registers CC:Tweaked peripheral capabilities on controller and processor block entities. */
public class CCCompatHandler {

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(CCCompatHandler::registerCapabilities);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        for (ModEntry entry : ModEntries.ENTRIES.values()) {
            if (!entry.hasBlockEntity()) continue;
            event.registerBlockEntity(PeripheralCapability.get(), entry.blockEntity().get(),
                    (be, side) -> peripheral(be));
        }
        event.registerBlockEntity(PeripheralCapability.get(), Crafter.ENGINEERS_CRAFTING_TABLE_BE.get(),
                (be, side) -> new EngineersCrafterPeripheral(be));
    }

    @Nullable
    private static IPeripheral peripheral(Object be) {
        return switch (be) {
            case FissionReactorControllerBE c -> new FissionReactorPeripheral(c);
            case MsrControllerBE c -> new MsrPeripheral(c);
            case FusionReactorControllerBE c -> new FusionReactorPeripheral(c);
            case TurbineControllerBE c -> new TurbinePeripheral(c);
            case HeatExchangerControllerBE c -> new HeatExchangerPeripheral(c);
            case ChamberTerminalBE c -> new KugelblitzPeripheral(c);
            case BeamDiverterControllerBE c -> new BeamDiverterPeripheral(c);
            case AbstractAcceleratorControllerBE c -> new AcceleratorPeripheral<>(c);
            case ParticleChamberControllerBE c -> new ParticleChamberPeripheral(c);
            case MultiblockControllerBE c -> new ControllerPeripheral<>(c);
            case UniversalProcessorBE p -> new ProcessorPeripheral(p);
            default -> null;
        };
    }
}
