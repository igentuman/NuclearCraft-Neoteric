package igentuman.nc.setup.entries;

import igentuman.nc.api.particle.ParticleDefinition;
import igentuman.nc.registration.ParticleEntryBuilder;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static igentuman.nc.NuclearCraft.rl;

public final class Particles {

    public static final List<String> SPECIES = List.of(
            "up_quark", "down_quark", "charm_quark", "strange_quark", "top_quark", "bottom_quark",
            "antiup_quark", "antidown_quark", "anticharm_quark", "antistrange_quark", "antitop_quark", "antibottom_quark",
            "electron", "positron", "electron_neutrino", "electron_antineutrino", "muon", "antimuon",
            "muon_neutrino", "muon_antineutrino", "tau", "antitau", "tau_neutrino", "tau_antineutrino",
            "photon", "gluon", "w_plus_boson", "w_minus_boson", "z_boson", "higgs_boson",
            "proton", "antiproton", "neutron", "antineutron", "deuteron", "antideuteron", "alpha", "antialpha",
            "triton", "antitriton", "helion", "antihelion", "boron_ion", "calcium_48_ion",
            "pion_plus", "pion_naught", "pion_minus", "kaon_plus", "kaon_naught", "antikaon_naught", "kaon_minus",
            "eta", "eta_prime", "charmed_eta", "bottom_eta", "glueball", "sigma_plus", "antisigma_plus",
            "sigma_minus", "antisigma_minus", "delta_plus_plus", "antidelta_plus_plus", "delta_minus", "antidelta_minus"
    );

    public static final List<String> ITEM_SOURCES = List.of(
            "source_calcium_48", "source_iridium_192", "tungsten_filament", "antideuterium",
            "antihelium", "antihelium3", "antihydrogen", "antitritium"
    );

    public static final List<String> FLUID_SOURCES = List.of(
            "diborane", "hydrogen", "deuterium", "tritium", "helium"
    );

    private record Draft(double mass, double charge, double spin, boolean weak, boolean strong,
                          List<ParticleDefinition.Component> composition) {
    }

    private record C(String name, int count) {
    }

    private static final Map<String, Draft> DRAFTS = new LinkedHashMap<>();
    private static final Map<String, String> ANTIPARTICLES = new LinkedHashMap<>();
    private static boolean initialized;

    private Particles() {
    }

    public static void particles() {
        if (initialized) return;
        initialized = true;

        fundamental("up_quark", 2.2, 2d / 3d, 0.5, true, true);
        fundamental("down_quark", 4.7, -1d / 3d, 0.5, true, true);
        fundamental("charm_quark", 1280d, 2d / 3d, 0.5, true, true);
        fundamental("strange_quark", 95d, -1d / 3d, 0.5, true, true);
        fundamental("top_quark", 173000d, 2d / 3d, 0.5, true, true);
        fundamental("bottom_quark", 4180d, -1d / 3d, 0.5, true, true);
        mirror("up_quark", "antiup_quark");
        mirror("down_quark", "antidown_quark");
        mirror("charm_quark", "anticharm_quark");
        mirror("strange_quark", "antistrange_quark");
        mirror("top_quark", "antitop_quark");
        mirror("bottom_quark", "antibottom_quark");

        fundamental("electron", 0.511, -1, 0.5, true, false);
        fundamental("electron_neutrino", 0.00000012, 0, 0.5, true, false);
        fundamental("muon", 106d, -1, 0.5, true, false);
        fundamental("muon_neutrino", 0.00000012, 0, 0.5, true, false);
        fundamental("tau", 1780d, -1, 0.5, true, false);
        fundamental("tau_neutrino", 0.00000012, 0, 0.5, true, false);
        mirror("electron", "positron");
        mirror("electron_neutrino", "electron_antineutrino");
        mirror("muon", "antimuon");
        mirror("muon_neutrino", "muon_antineutrino");
        mirror("tau", "antitau");
        mirror("tau_neutrino", "tau_antineutrino");

        fundamental("photon", 0, 0, 1, false, false);
        fundamental("gluon", 0, 0, 1, false, true);
        fundamental("w_plus_boson", 80400d, 1, 1, true, false);
        fundamental("z_boson", 91200d, 0, 1, false, false);
        fundamental("higgs_boson", 125000d, 0, 0, true, false);
        mirror("w_plus_boson", "w_minus_boson");

        composite("proton", 938d, 1, 0.5, true, true, comp(new C("up_quark", 2), new C("down_quark", 1)));
        composite("neutron", 940d, 0, 0.5, true, true, comp(new C("up_quark", 1), new C("down_quark", 2)));
        mirror("proton", "antiproton");
        mirror("neutron", "antineutron");

        composite("deuteron", 1876d, 1, 1, true, true, comp(new C("proton", 1), new C("neutron", 1)));
        composite("alpha", 3727d, 2, 0, true, true, comp(new C("proton", 2), new C("neutron", 2)));
        composite("triton", 2809d, 1, 0.5, true, true, comp(new C("proton", 1), new C("neutron", 2)));
        composite("helion", 2808d, 2, 0.5, true, true, comp(new C("proton", 2), new C("neutron", 1)));
        mirror("deuteron", "antideuteron");
        mirror("alpha", "antialpha");
        mirror("triton", "antitriton");
        mirror("helion", "antihelion");

        composite("boron_ion", 10250d, 1, 0.5, true, true,
                comp(new C("proton", 5), new C("neutron", 6), new C("electron", 4)));
        composite("calcium_48_ion", 44600d, 1, 0, true, true,
                comp(new C("proton", 20), new C("neutron", 28), new C("electron", 19)));

        composite("pion_plus", 140d, 1, 0, true, true, comp(new C("up_quark", 1), new C("antidown_quark", 1)));
        composite("pion_naught", 135d, 0, 0, true, true, comp(new C("up_quark", 1), new C("antiup_quark", 1)));
        mirror("pion_plus", "pion_minus");

        composite("kaon_plus", 464d, 1, 0, true, true, comp(new C("up_quark", 1), new C("antistrange_quark", 1)));
        composite("kaon_naught", 498d, 1, 0, true, true, comp(new C("down_quark", 1), new C("antistrange_quark", 1)));
        mirror("kaon_plus", "kaon_minus");
        mirror("kaon_naught", "antikaon_naught");

        composite("eta", 548d, 0, 0, true, true, comp(new C("down_quark", 1), new C("antidown_quark", 1)));
        composite("eta_prime", 958d, 0, 0, true, true, comp(new C("strange_quark", 1), new C("antistrange_quark", 1)));
        composite("charmed_eta", 2980d, 0, 0, true, true, comp(new C("charm_quark", 1), new C("anticharm_quark", 1)));
        composite("bottom_eta", 9400d, 0, 0, true, true, comp(new C("bottom_quark", 1), new C("antibottom_quark", 1)));

        composite("glueball", 1730d, 0, 0, false, true, comp(new C("gluon", 2)));

        composite("sigma_plus", 1190d, 1, 0.5, true, true, comp(new C("up_quark", 2), new C("strange_quark", 1)));
        composite("sigma_minus", 1200d, -1, 0.5, true, true, comp(new C("down_quark", 2), new C("strange_quark", 1)));
        mirror("sigma_plus", "antisigma_plus");
        mirror("sigma_minus", "antisigma_minus");

        composite("delta_plus_plus", 1232d, 2, 1.5, true, true, comp(new C("up_quark", 3)));
        composite("delta_minus", 1232d, -1, 1.5, true, true, comp(new C("down_quark", 3)));
        mirror("delta_plus_plus", "antidelta_plus_plus");
        mirror("delta_minus", "antidelta_minus");

        DRAFTS.keySet().forEach(Particles::register);
    }

    private static void fundamental(String name, double mass, double charge, double spin, boolean weak, boolean strong) {
        DRAFTS.put(name, new Draft(mass, charge, spin, weak, strong, List.of()));
    }

    private static void composite(String name, double mass, double charge, double spin, boolean weak, boolean strong,
                                   List<ParticleDefinition.Component> composition) {
        DRAFTS.put(name, new Draft(mass, charge, spin, weak, strong, composition));
    }

    private static List<ParticleDefinition.Component> comp(C... components) {
        List<ParticleDefinition.Component> result = new ArrayList<>(components.length);
        for (C component : components) {
            result.add(new ParticleDefinition.Component(rl(component.name()), component.count()));
        }
        return result;
    }

    private static void mirror(String base, String antiName) {
        Draft b = DRAFTS.get(base);
        double antiCharge = b.charge() == 0 ? 0 : -b.charge();
        List<ParticleDefinition.Component> antiComposition = new ArrayList<>(b.composition().size());
        for (ParticleDefinition.Component component : b.composition()) {
            String componentName = component.particleId().getPath();
            String componentAnti = ANTIPARTICLES.getOrDefault(componentName, componentName);
            antiComposition.add(new ParticleDefinition.Component(rl(componentAnti), component.count()));
        }
        DRAFTS.put(antiName, new Draft(b.mass(), antiCharge, b.spin(), b.weak(), b.strong(), antiComposition));
        ANTIPARTICLES.put(base, antiName);
        ANTIPARTICLES.put(antiName, base);
    }

    private static void register(String name) {
        Draft d = DRAFTS.get(name);
        String antiName = ANTIPARTICLES.getOrDefault(name, name);
        ParticleDefinition definition = new ParticleDefinition(
                d.mass(), d.charge(), d.spin(), interactionSet(d.charge(), d.weak(), d.strong()),
                rl(antiName), d.composition(), rl("textures/particles/" + name + ".png"),
                "nuclearcraft.particle." + name + ".name"
        );
        ParticleEntryBuilder.add(name).definition(definition).build();
    }

    private static Set<ParticleDefinition.Interaction> interactionSet(double charge, boolean weak, boolean strong) {
        Set<ParticleDefinition.Interaction> set = EnumSet.noneOf(ParticleDefinition.Interaction.class);
        if (charge != 0) set.add(ParticleDefinition.Interaction.ELECTROMAGNETIC);
        if (strong) set.add(ParticleDefinition.Interaction.STRONG);
        if (weak) set.add(ParticleDefinition.Interaction.WEAK);
        return set;
    }
}
