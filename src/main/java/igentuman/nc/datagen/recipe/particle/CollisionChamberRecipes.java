package igentuman.nc.datagen.recipe.particle;

import igentuman.nc.api.particle.ParticleIngredient;
import igentuman.nc.api.particle.ParticleStack;
import igentuman.nc.recipe.particle.CollisionChamberRecipe;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Set;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.NuclearCraft.rl;

/**
 * Generates the full legacy collision-chamber recipe set (39 recipes), ported directly from legacy explicit and
 * mass-derived collision data. Every referenced species already exists in this project's 64-species manifest, so
 * this family has no unregistered-content skips, matching decay's precedent.
 */
public final class CollisionChamberRecipes {

    public static final int EXPECTED_COUNT = 39;

    private CollisionChamberRecipes() {
    }

    public static void generate(RecipeOutput out) {
        collide(out, "proton_neutron_deuteron", "proton", "neutron", 0L, 30_000L, 2220L, 0.5,
                stack("deuteron"), stack("photon"));
        collide(out, "antiproton_antineutron_antideuteron", "antiproton", "antineutron", 0L, 30_000L, 2220L, 0.5,
                stack("antideuteron"), stack("photon"));
        collide(out, "deuteron_neutron_triton", "deuteron", "neutron", 0L, 30_000L, 6260L, 0.5,
                stack("triton"), stack("photon"));
        collide(out, "antideuteron_antineutron_antitriton", "antideuteron", "antineutron", 0L, 30_000L, 6260L, 0.5,
                stack("antitriton"), stack("photon"));
        collide(out, "helion_neutron_alpha", "helion", "neutron", 0L, 30_000L, 20_600L, 0.5,
                stack("alpha"), stack("photon"));
        collide(out, "antihelion_antineutron_antialpha", "antihelion", "antineutron", 0L, 30_000L, 20_600L, 0.5,
                stack("antialpha"), stack("photon"));

        collide(out, "proton_proton_deuteron", "proton", "proton", 700L, 10_000L, 420L, 0.25,
                stack("deuteron"), stack("positron"), stack("electron_neutrino"));
        collide(out, "antiproton_antiproton_antideuteron", "antiproton", "antiproton", 700L, 10_000L, 420L, 0.25,
                stack("antideuteron"), stack("electron"), stack("electron_antineutrino"));
        collide(out, "proton_deuteron_helion", "proton", "deuteron", 700L, 10_000L, 5490L, 0.5,
                stack("helion"), stack("photon"));
        collide(out, "antiproton_antideuteron_antihelion", "antiproton", "antideuteron", 700L, 10_000L, 5490L, 0.5,
                stack("antihelion"), stack("photon"));
        collide(out, "helion_helion_alpha", "helion", "helion", 700L, 10_000L, 12_900L, 0.5,
                stack("alpha"), stack(2, "proton"));
        collide(out, "antihelion_antihelion_antialpha", "antihelion", "antihelion", 700L, 10_000L, 12_900L, 0.5,
                stack("antialpha"), stack(2, "antiproton"));
        collide(out, "deuteron_triton_alpha", "deuteron", "triton", 700L, 10_000L, 17_600L, 0.5,
                stack("alpha"), stack("neutron"));
        collide(out, "antideuteron_antitriton_antialpha", "antideuteron", "antitriton", 700L, 10_000L, 17_600L, 0.5,
                stack("antialpha"), stack("antineutron"));
        collide(out, "triton_triton_alpha", "triton", "triton", 700L, 10_000L, 11_300L, 0.5,
                stack("alpha"), stack(2, "neutron"));
        collide(out, "antitriton_antitriton_antialpha", "antitriton", "antitriton", 700L, 10_000L, 11_300L, 0.5,
                stack("antialpha"), stack(2, "antineutron"));
        collide(out, "deuteron_helion_alpha", "deuteron", "helion", 700L, 10_000L, 18_400L, 0.5,
                stack("alpha"), stack("proton"));
        collide(out, "antideuteron_antihelion_antialpha", "antideuteron", "antihelion", 700L, 10_000L, 18_400L, 0.5,
                stack("antialpha"), stack("antiproton"));
        collide(out, "helion_triton_alpha", "helion", "triton", 700L, 10_000L, 12_100L, 0.5,
                stack("alpha"), stack("proton"), stack("neutron"));
        collide(out, "antihelion_antitriton_antialpha", "antihelion", "antitriton", 700L, 10_000L, 12_100L, 0.5,
                stack("antialpha"), stack("antiproton"), stack("antineutron"));
        collide(out, "deuteron_deuteron_triton", "deuteron", "deuteron", 700L, 10_000L, 4030L, 0.5,
                stack("triton"), stack("proton"));
        collide(out, "antideuteron_antideuteron_antitriton", "antideuteron", "antideuteron", 700L, 10_000L, 4030L, 0.5,
                stack("antitriton"), stack("antiproton"));

        collide(out, "proton_antiproton_pions", "proton", "antiproton", 0L, 50_000_000L, 220_000L, 1.0,
                stack(4, "pion_plus"), stack(4, "pion_naught"), stack(4, "pion_minus"));
        collide(out, "neutron_antineutron_pions", "neutron", "antineutron", 0L, 50_000_000L, 223_000L, 1.0,
                stack(4, "pion_plus"), stack(4, "pion_naught"), stack(4, "pion_minus"));
        collide(out, "proton_antineutron_pions", "proton", "antineutron", 0L, 50_000_000L, 81_800L, 1.0,
                stack(5, "pion_plus"), stack(4, "pion_naught"), stack(4, "pion_minus"));
        collide(out, "antiproton_neutron_pions", "antiproton", "neutron", 0L, 50_000_000L, 81_800L, 1.0,
                stack(4, "pion_plus"), stack(4, "pion_naught"), stack(5, "pion_minus"));

        // Mass-derived (legacy collideAuto): energyReleased = round((inMass - outMass) * 1000) keV,
        // minEnergy = round(abs(delta) * 1000 * 1.1), maxEnergy = round(minEnergy * 1.5).
        collide(out, "electron_electron_muons", "electron", "electron", 232_076L, 348_114L, -210_978L, 0.025,
                stack(2, "electron_neutrino"), stack(2, "muon"), stack(2, "muon_antineutrino"));
        collide(out, "positron_positron_antimuons", "positron", "positron", 232_076L, 348_114L, -210_978L, 0.025,
                stack(2, "electron_antineutrino"), stack(2, "antimuon"), stack(2, "muon_neutrino"));
        collide(out, "electron_positron_muons", "electron", "positron", 232_076L, 348_114L, -210_978L, 0.10,
                stack("muon"), stack("antimuon"));
        collide(out, "proton_proton_pions", "proton", "proton", 456_500L, 684_750L, -415_000L, 0.10,
                stack(2, "proton"), stack("pion_plus"), stack("pion_minus"), stack("pion_naught"));
        collide(out, "antiproton_antiproton_pions", "antiproton", "antiproton", 456_500L, 684_750L, -415_000L, 0.10,
                stack(2, "antiproton"), stack("pion_minus"), stack("pion_plus"), stack("pion_naught"));
        collide(out, "proton_proton_baryons", "proton", "proton", 1_265_000L, 1_897_500L, -1_150_000L, 0.025,
                stack("delta_minus"), stack("sigma_plus"), stack("kaon_plus"), stack("pion_plus"));
        collide(out, "antiproton_antiproton_baryons", "antiproton", "antiproton", 1_265_000L, 1_897_500L, -1_150_000L, 0.025,
                stack("antidelta_minus"), stack("antisigma_plus"), stack("kaon_minus"), stack("pion_minus"));
        collide(out, "electron_positron_taus", "electron", "positron", 3_914_876L, 5_872_314L, -3_558_978L, 0.025,
                stack("tau"), stack("antitau"));
        collide(out, "proton_proton_z_boson", "proton", "proton", 100_645_600L, 150_968_400L, -91_496_000L, 0.025,
                stack("neutron"), stack("delta_plus_plus"), stack("z_boson"));
        collide(out, "antiproton_antiproton_z_boson", "antiproton", "antiproton", 100_645_600L, 150_968_400L, -91_496_000L, 0.025,
                stack("antineutron"), stack("antidelta_plus_plus"), stack("z_boson"));
        collide(out, "proton_antiproton_bosons", "proton", "antiproton", 179_997_400L, 269_996_100L, -163_634_000L, 0.025,
                stack("glueball"), stack("w_plus_boson"), stack("w_minus_boson"), stack("charmed_eta"));
        collide(out, "proton_proton_higgs", "proton", "proton", 276_122_000L, 414_183_000L, -251_020_000L, 0.025,
                stack(2, "higgs_boson"), stack("delta_plus_plus"), stack("kaon_plus"), stack("sigma_minus"));
        collide(out, "antiproton_antiproton_higgs", "antiproton", "antiproton", 276_122_000L, 414_183_000L, -251_020_000L, 0.025,
                stack(2, "higgs_boson"), stack("antidelta_plus_plus"), stack("kaon_minus"), stack("antisigma_minus"));
    }

    private static void collide(RecipeOutput out, String id, String speciesA, String speciesB, long minimumEnergyKeV,
                                 long maximumEnergyKeV, long releasedEnergyKeV, double crossSection,
                                 ParticleStack... outputs) {
        ParticleIngredient inputA = new ParticleIngredient(Set.of(rl(speciesA)), 1, minimumEnergyKeV, maximumEnergyKeV, 1.0);
        ParticleIngredient inputB = new ParticleIngredient(Set.of(rl(speciesB)), 1, minimumEnergyKeV, maximumEnergyKeV, 1.0);
        CollisionChamberRecipe recipe = new CollisionChamberRecipe(List.of(inputA, inputB), List.of(outputs),
                crossSection, releasedEnergyKeV, false, 0);
        out.accept(ResourceLocation.fromNamespaceAndPath(MODID, "collision_chamber/" + id), recipe, null);
    }

    private static ParticleStack stack(String species) {
        return stack(1, species);
    }

    private static ParticleStack stack(int amount, String species) {
        return new ParticleStack(rl(species), amount, 0, 1.0);
    }
}
