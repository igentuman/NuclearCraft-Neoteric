package igentuman.nc.setup.entries;

import igentuman.nc.setup.ModEntries;

import static igentuman.nc.registration.ModEntryBuilder.addIsotope;

/** Declares all radioactive isotope entries and their decay rates. */
public class Isotopes extends ModEntries {
    public static void isotopes() {
        addIsotope("americium/241", 0.02);
        addIsotope("copernicium/291", 0.32);
        addIsotope("americium/242", 0.07);
        addIsotope("americium/243", 0.0013);
        addIsotope("berkelium/247", 0.0075);
        addIsotope("berkelium/248", 0.003);
        addIsotope("boron/10", 0.001);
        addIsotope("boron/11", 0.007);
        addIsotope("californium/249", 0.002);
        addIsotope("californium/250", 0.076);
        addIsotope("californium/251", 0.005);
        addIsotope("californium/252", 0.38);
        addIsotope("curium/243", 0.034);
        addIsotope("curium/245", 0.0012);
        addIsotope("curium/246", 0.0021);
        addIsotope("curium/247", 0.0004);
        addIsotope("lithium/6", 0.002);
        addIsotope("lithium/7", 0.002);
        addIsotope("neptunium/236", 0.006);
        addIsotope("neptunium/237", 0.00047);
        addIsotope("plutonium/238", 0.011);
        addIsotope("plutonium/239", 0.05);
        addIsotope("plutonium/241", 0.071);
        addIsotope("plutonium/242", 0.002);
        addIsotope("thorium/230", 0.0009);
        addIsotope("thorium/232", 0.002);
        addIsotope("uranium/233", 0.003);
        addIsotope("uranium/234", 0.013);
        addIsotope("uranium/235", 0.02);
        addIsotope("uranium/238", 0.0002);
        addIsotope("xenorium/298", 1.2);
        addIsotope("quantite", 0.3);
        addIsotope("beryllium_7", 0.1);
        addIsotope("calcium_48", 0.1);
        addIsotope("cobalt_60", 0.1);
        addIsotope("iridium_192", 0.1);
        addIsotope("magnesium_24", 0.1);
        addIsotope("magnesium_26", 0.1);
        addIsotope("sodium_22", 0.1);
    }
}
