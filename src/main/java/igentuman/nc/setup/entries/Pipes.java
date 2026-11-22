package igentuman.nc.setup.entries;

import igentuman.nc.block.pipe.PipeBlock;
import igentuman.nc.block.pipe.PipeConnectorBlock;
import igentuman.nc.block_entity.pipe.PipeConnectorBE;
import igentuman.nc.container.PipeConnectorContainer;
import igentuman.nc.api.pipe.ConnectorMode;
import igentuman.nc.pipe.PipeCapabilityType;
import igentuman.nc.pipe.cap.NetworkEnergyStorage;
import igentuman.nc.pipe.cap.NetworkFluidHandler;
import igentuman.nc.pipe.cap.NetworkItemHandler;
import igentuman.nc.setup.ModEntries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import static igentuman.nc.registration.ModEntryBuilder.add;

public class Pipes {

    public static void pipes() {
        add("pipe")
                .block(PipeBlock::new)
                .build();
        add("pipe_connector")
                .block(PipeConnectorBlock::new)
                .blockEntity(PipeConnectorBE::new)
                .menu(PipeConnectorContainer::new)
                .build();
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        BlockEntityType<?> beType = ModEntries.get("pipe_connector").blockEntity().get();
        event.registerBlockEntity(Capabilities.Item.BLOCK, beType, (be, side) ->
                be instanceof PipeConnectorBE c && c.getMode() == ConnectorMode.DEFAULT && c.isEnabled(PipeCapabilityType.ITEM)
                        ? new NetworkItemHandler(c, side) : null);
        event.registerBlockEntity(Capabilities.Fluid.BLOCK, beType, (be, side) ->
                be instanceof PipeConnectorBE c && c.getMode() == ConnectorMode.DEFAULT && c.isEnabled(PipeCapabilityType.FLUID)
                        ? new NetworkFluidHandler(c, side) : null);
        event.registerBlockEntity(Capabilities.Energy.BLOCK, beType, (be, side) ->
                be instanceof PipeConnectorBE c && c.getMode() == ConnectorMode.DEFAULT && c.isEnabled(PipeCapabilityType.ENERGY)
                        ? new NetworkEnergyStorage(c, side) : null);
    }
}
