package igentuman.nc.setup.entries;

import igentuman.nc.block.ChargingStationBlock;
import igentuman.nc.block_entity.ChargingStationBE;
import igentuman.nc.container.UniversalProcessorContainer;
import igentuman.nc.setup.ModEntries;
import igentuman.nc.util.SlotsLayout;

import static igentuman.nc.registration.ModEntryBuilder.add;

public class ChargingStation extends ModEntries {

    public static void chargingStation() {
        add("charging_station")
                .block(ChargingStationBlock::new)
                .blockEntity(ChargingStationBE::new)
                .menu(UniversalProcessorContainer::new)
                .itemCap(1, 0)
                .fluidCap(1, 0, 0)
                .withEnergy(1_000_000, 1_000_000, 1_000_000)
                .withLayout(SlotsLayout.create().addInput(80, 35).addInput(24, 40))
                .build();
    }
}
