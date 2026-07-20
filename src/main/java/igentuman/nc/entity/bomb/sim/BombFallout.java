package igentuman.nc.entity.bomb.sim;

import igentuman.nr.api.isotope.IsotopeStack;
import igentuman.nr.api.RadiationProfile;
import igentuman.nr.api.isotope.Isotope;
import igentuman.nr.api.isotope.IsotopeRegistry;
import igentuman.nr.registry.Isotopes;

import java.util.LinkedHashMap;
import java.util.Map;

public final class BombFallout {

    private static final Map<String, Double> FALLOUT_ATOMS = new LinkedHashMap<>();
    static {
        FALLOUT_ATOMS.put(Isotopes.XE_133, 4.5e15);
        FALLOUT_ATOMS.put(Isotopes.CS_137, 4.2e15);
        FALLOUT_ATOMS.put(Isotopes.SR_90,  2.1e15);
        FALLOUT_ATOMS.put(Isotopes.I_131,  2.9e15);
        FALLOUT_ATOMS.put(Isotopes.H_3,    1.2e15);
        FALLOUT_ATOMS.put(Isotopes.KR_85,  8.0e15);
        FALLOUT_ATOMS.put(Isotopes.PU_239, 6.0e15);
    }

    private BombFallout() {}

    public static Map<String, Double> sourceAtoms() {
        Map<String, Double> src = new LinkedHashMap<>();
        src.put(Isotopes.PU_239, 2.0e15);
        src.put(Isotopes.CS_137, 1.4e16);
        src.put(Isotopes.SR_90,  7.0e16);
        src.put(Isotopes.XE_133, 6.0e16);
        src.put(Isotopes.I_131,  4.0e15);
        src.put(Isotopes.KR_85,  2.0e15);
        return src;
    }

    public static Map<String, Double> fallbackAtoms() {
        return FALLOUT_ATOMS;
    }


    public static RadiationProfile profileFrom(Map<String, Double> atoms, long now) {
        RadiationProfile profile = new RadiationProfile();
        for (Map.Entry<String, Double> e : atoms.entrySet()) {
            Isotope iso = IsotopeRegistry.get(e.getKey());
            if (iso == null) continue;
            profile.put(new IsotopeStack(iso, e.getValue(), now));
        }
        return profile;
    }
}
