package igentuman.nc.setup.entries;

import igentuman.nc.setup.ModEntries;

public class Fluids extends ModEntries {
    public static void fluids() {
        acid("hydrofluoric_acid", 0xCCFFEE99);
        acid("hydrochloric_acid", 0xBBEEEEFF);
        acid("boric_acid", 0xCCA0EFFF);
        acid("sulfuric_acid", 0xCCF8FFD3);
        acid("nitric_acid", 0xCC4F9EFF);
        acid("aqua_regia_acid", 0xCCFFBB99);

        gas("quantite_energy", 0x369CD192, 2372);
        gas("steam", 0xCC929292, 373);
        gas("high_pressure_steam", 0xCCBDBDBD, 383);
        gas("exhaust_steam", 0xCC7E7E7E, 292);
        gas("low_pressure_steam", 0xCCA8A8A8, 272);
        gas("low_quality_steam", 0xCC828282, 272);
        gas("argon", 0xCCFF75DD, 87);
        gas("neon", 0xCCFF9F7A, 27);
        gas("chlorine", 0xCCFFFF8F, 239);
        gas("nitric_oxide", 0xCCC9EEFF, 121);
        gas("nitrogen_dioxide", 0xCC782A10, 294);
        gas("hydrogen", 0xCCA0EFFF, 239);
        gas("helium", 0xCCC57B81, 293);
        gas("hot_helium", 0xAAC57B81, 693);
        gas("helium_3", 0xCCCBBB67, 293);
        gas("tritium", 0xCC5DBBD6, 20);
        gas("deuterium", 0xCC9E6FEF, 239);
        gas("oxygen", 0xCC7E8CC8, 293);
        gas("nitrogen", 0xCC7CC37B, 293);
        gas("fluorine", 0xCCD3C75D, 293);
        gas("carbon", 0xCC5C635A, 400);
        gas("carbon_dioxide", 0xCC5C635A, 394);
        gas("carbon_monoxide", 0xCC4C5649, 381);
        gas("ethene", 0xCCFFE4A3, 169);
        gas("fluoromethane", 0xCC424C05, 194);
        gas("ammonia", 0xCC7AC3A0, 240);
        gas("oxygen_difluoride", 0xCCEA1B01, 128);
        gas("diborane", 0xCC5DBBD6, 180);
        gas("sulfur_dioxide", 0xCCC3BC7A, 400);
        gas("sulfur_trioxide", 0xCCD3AE5D, 400);
        gas("radon", 0xFFFFFFFF, 260);
    }
}
