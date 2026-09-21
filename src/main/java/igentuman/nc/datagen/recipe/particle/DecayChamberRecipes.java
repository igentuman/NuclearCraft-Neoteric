package igentuman.nc.datagen.recipe.particle;

import igentuman.nc.api.particle.ParticleIngredient;
import igentuman.nc.api.particle.ParticleStack;
import igentuman.nc.recipe.particle.DecayChamberRecipe;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Set;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.NuclearCraft.rl;

/**
 * Generates the full legacy decay-chamber recipe set (34 recipes), ported directly from legacy mass-defect/explicit
 * decay data. Every referenced species already exists in this project's 64-species manifest, so this family has no
 * unregistered-content skips unlike the target/collision families.
 */
public final class DecayChamberRecipes {

    public static final int EXPECTED_COUNT = 34;

    private DecayChamberRecipes() {
    }

    public static void generate(RecipeOutput out) {
        decay(out, "neutron", "neutron", 1489L, 1.0,
                stack("proton"), stack("electron_antineutrino"), stack("electron"));
        decay(out, "antineutron", "antineutron", 1489L, 1.0,
                stack("positron"), stack("electron_neutrino"), stack("antiproton"));
        decay(out, "pion_naught", "pion_naught", 135_000L, 0.98,
                stack(2, "photon"));
        decay(out, "pion_plus", "pion_plus", 34_000L, 0.99,
                stack("antimuon"), stack("muon_neutrino"));
        decay(out, "pion_minus", "pion_minus", 34_000L, 0.99,
                stack("muon_antineutrino"), stack("muon"));
        decay(out, "muon", "muon", 105_489L, 1.0,
                stack("electron_antineutrino"), stack("muon_neutrino"), stack("electron"));
        decay(out, "antimuon", "antimuon", 105_489L, 1.0,
                stack("positron"), stack("muon_antineutrino"), stack("electron_neutrino"));
        decay(out, "tau", "tau", 1_505_000L, 0.25,
                stack("pion_naught"), stack("tau_neutrino"), stack("pion_minus"));
        decay(out, "antitau", "antitau", 1_505_000L, 0.25,
                stack("pion_plus"), stack("tau_antineutrino"), stack("pion_naught"));
        decay(out, "kaon_plus", "kaon_plus", 358_000L, 0.63,
                stack("antimuon"), stack("muon_neutrino"));
        decay(out, "kaon_minus", "kaon_minus", 358_000L, 0.63,
                stack("muon_antineutrino"), stack("muon"));
        decay(out, "kaon_naught", "kaon_naught", 218_000L, 0.77,
                stack("pion_plus"), stack("pion_minus"));
        decay(out, "antikaon_naught", "antikaon_naught", 218_000L, 0.77,
                stack("pion_plus"), stack("pion_minus"));
        decay(out, "w_minus_boson", "w_minus_boson", 80_260_000L, 0.32,
                stack("pion_minus"));
        decay(out, "w_plus_boson", "w_plus_boson", 80_260_000L, 0.32,
                stack("pion_plus"));
        decay(out, "z_boson", "z_boson", 91_200_000L, 0.068,
                stack("electron_neutrino"), stack("electron_antineutrino"));
        decay(out, "higgs_boson", "higgs_boson", 115_600_000L, 0.57,
                stack("bottom_eta"));
        decay(out, "eta", "eta", 133_000L, 0.33,
                stack("pion_plus"), stack("pion_naught"), stack("pion_minus"));
        decay(out, "eta_prime", "eta_prime", 130_000L, 0.33,
                stack("pion_plus"), stack("eta"), stack("pion_minus"));
        decay(out, "charmed_eta", "charmed_eta", 1_984_000L, 0.07,
                stack("kaon_naught"), stack("antikaon_naught"));
        decay(out, "bottom_eta", "bottom_eta", 5_840_000L, 0.08,
                stack("antitau"), stack("tau"));
        decayExplicit(out, "triton_beta", "triton", 0L, Long.MAX_VALUE, 19L, 1.0,
                stack("helion"), stack("electron_antineutrino"), stack("electron"));
        decayExplicit(out, "antitriton_beta", "antitriton", 0L, Long.MAX_VALUE, 19L, 1.0,
                stack("positron"), stack("electron_neutrino"), stack("antihelion"));
        decay(out, "glueball", "glueball", 802_000L, 0.33,
                stack("kaon_plus"), stack("kaon_minus"));
        decay(out, "sigma_plus", "sigma_plus", 117_000L, 0.52,
                stack("proton"), stack("pion_naught"));
        decay(out, "antisigma_plus", "antisigma_plus", 117_000L, 0.52,
                stack("pion_naught"), stack("antiproton"));
        decay(out, "sigma_minus", "sigma_minus", 120_000L, 0.99,
                stack("neutron"), stack("pion_minus"));
        decay(out, "antisigma_minus", "antisigma_minus", 120_000L, 0.99,
                stack("pion_plus"), stack("antineutron"));
        decay(out, "delta_plus_plus", "delta_plus_plus", 154_000L, 1.0,
                stack("proton"), stack("pion_plus"));
        decay(out, "antidelta_plus_plus", "antidelta_plus_plus", 154_000L, 1.0,
                stack("antiproton"), stack("pion_minus"));
        decay(out, "delta_minus", "delta_minus", 152_000L, 1.0,
                stack("neutron"), stack("pion_minus"));
        decay(out, "antidelta_minus", "antidelta_minus", 152_000L, 1.0,
                stack("antineutron"), stack("pion_plus"));
        decayExplicit(out, "photon_pair_production", "photon", 1120L, 230_000L, -1020L, 1.0,
                stack("electron"), stack("positron"));
        decayExplicit(out, "photon_muon_pair_production", "photon", 233_000L, Long.MAX_VALUE, -211_000L, 0.5,
                stack("muon"), stack("antimuon"));
    }

    private static void decay(RecipeOutput out, String id, String species, long releasedEnergyKeV,
                               double crossSection, ParticleStack... outputs) {
        decayExplicit(out, id, species, 0L, Long.MAX_VALUE, releasedEnergyKeV, crossSection, outputs);
    }

    private static void decayExplicit(RecipeOutput out, String id, String species, long minimumEnergyKeV,
                                       long maximumEnergyKeV, long releasedEnergyKeV, double crossSection,
                                       ParticleStack... outputs) {
        ParticleIngredient particleInput = new ParticleIngredient(Set.of(rl(species)), 1,
                minimumEnergyKeV, maximumEnergyKeV, 1.0);
        DecayChamberRecipe recipe = new DecayChamberRecipe(particleInput, List.of(outputs),
                crossSection, releasedEnergyKeV, 0);
        out.accept(ResourceLocation.fromNamespaceAndPath(MODID, "decay_chamber/" + id), recipe, null);
    }

    private static ParticleStack stack(String species) {
        return stack(1, species);
    }

    private static ParticleStack stack(int amount, String species) {
        return new ParticleStack(rl(species), amount, 0, 1.0);
    }
}
