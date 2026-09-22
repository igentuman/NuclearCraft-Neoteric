package igentuman.nc.setup.entries;

import igentuman.nc.setup.ModEntries;

import static igentuman.nc.registration.ModEntryBuilder.*;

public class Waste extends ModEntries {
    public static void waste() {
        String[] names = {
                "americium",
                "berkelium",
                "bismuth",
                "californium",
                "curium",
                "gold",
                "hafnium",
                "heavy",
                "iridium",
                "lead",
                "light",
                "mercury",
                "neptunium",
                "osmium",
                "platinum",
                "plutonium",
                "polonium",
                "protactinium",
                "radium",
                "thorium",
                "tungsten",
                "uranium",
        };
        for (String name : names) {
            addItem(name + "_spallation_waste").build();
        }
    }
}
