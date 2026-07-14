package igentuman.nc.compat.cc;

import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.PeripheralCapability;
import igentuman.nc.block_entity.MultiblockControllerBE;
import igentuman.nc.block_entity.UniversalProcessorBE;
import igentuman.nc.registration.ModEntry;
import igentuman.nc.setup.ModEntries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

/** Registers CC:Tweaked peripheral capabilities on controller and processor block entities. */
public class CCCompatHandler {

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(CCCompatHandler::registerCapabilities);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        for (ModEntry entry : ModEntries.ENTRIES.values()) {
            if (!entry.hasBlockEntity()) continue;
            event.registerBlockEntity(
                    PeripheralCapability.get(),
                    entry.blockEntity().get(),
                    (be, side) -> {
                        if (be instanceof MultiblockControllerBE controller) return new ControllerPerihperal(controller);
                        if (be instanceof UniversalProcessorBE processor) return new ProcessorPeripheral(processor);
                        return (IPeripheral) null;
                    }
            );
        }
    }
}
