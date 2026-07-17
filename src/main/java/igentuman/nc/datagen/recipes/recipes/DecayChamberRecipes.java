package igentuman.nc.datagen.recipes.recipes;

import igentuman.nc.content.particles.Particle;
import igentuman.nc.content.particles.ParticleStack;
import net.minecraft.data.recipes.FinishedRecipe;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static igentuman.nc.content.particles.Particles.*;

/**
 * Decay chamber datagen recipes. Ported from QMD's DecayChamberRecipes.
 * 1 input particle -> up to 3 output particles. Energy released computed from particle mass deltas.
 */
public class DecayChamberRecipes extends AbstractRecipeProvider {

    public static void generate(Consumer<FinishedRecipe> consumer) {
        DecayChamberRecipes.consumer = consumer;
        ID = "decay_chamber";

        // Baryon decays
        decay(p(neutron), out(proton), out(electron_antineutrino), out(electron), 1.0);
        decay(p(antineutron), out(positron), out(electron_neutrino), out(antiproton), 1.0);

        // Pion decays
        decay(p(pion_naught), null, out(2, photon), null, 0.98);
        decay(p(pion_plus), out(antimuon), out(muon_neutrino), null, 0.99);
        decay(p(pion_minus), null, out(muon_antineutrino), out(muon), 0.99);

        // Muon decays
        decay(p(muon), out(electron_antineutrino), out(muon_neutrino), out(electron), 1.0);
        decay(p(antimuon), out(positron), out(muon_antineutrino), out(electron_neutrino), 1.0);

        // Tau decays
        decay(p(tau), out(pion_naught), out(tau_neutrino), out(pion_minus), 0.25);
        decay(p(antitau), out(pion_plus), out(tau_antineutrino), out(pion_naught), 0.25);

        // Kaon decays
        decay(p(kaon_plus), out(antimuon), out(muon_neutrino), null, 0.63);
        decay(p(kaon_minus), null, out(muon_antineutrino), out(muon), 0.63);
        decay(p(kaon_naught), out(pion_plus), null, out(pion_minus), 0.77);
        decay(p(antikaon_naught), out(pion_plus), null, out(pion_minus), 0.77);

        // Heavy boson decays
        decay(p(w_minus_boson), null, null, out(pion_minus), 0.32);
        decay(p(w_plus_boson), out(pion_plus), null, null, 0.32);
        decay(p(z_boson), out(electron_neutrino), null, out(electron_antineutrino), 0.068);
        decay(p(higgs_boson), null, out(bottom_eta), null, 0.57);

        // Eta family decays
        decay(p(eta), out(pion_plus), out(pion_naught), out(pion_minus), 0.33);
        decay(p(eta_prime), out(pion_plus), out(eta), out(pion_minus), 0.33);
        decay(p(charmed_eta), out(kaon_naught), null, out(antikaon_naught), 0.07);
        decay(p(bottom_eta), out(antitau), null, out(tau), 0.08);

        // Triton beta decay (QMD overrides energyReleased=19)
        decayExplicit(p(triton), out(helion), out(electron_antineutrino), out(electron),
                0L, Long.MAX_VALUE, 19L, 1.0);
        decayExplicit(p(antitriton), out(positron), out(electron_neutrino), out(antihelion),
                0L, Long.MAX_VALUE, 19L, 1.0);

        // Glueball decay
        decay(p(glueball), out(kaon_plus), null, out(kaon_minus), 0.33);

        // Sigma decays
        decay(p(sigma_plus), out(proton), out(pion_naught), null, 0.52);
        decay(p(antisigma_plus), null, out(pion_naught), out(antiproton), 0.52);
        decay(p(sigma_minus), null, out(neutron), out(pion_minus), 0.99);
        decay(p(antisigma_minus), out(pion_plus), out(antineutron), null, 0.99);

        // Delta resonance decays
        decay(p(delta_plus_plus), out(proton), null, out(pion_plus), 1.0);
        decay(p(antidelta_plus_plus), out(antiproton), null, out(pion_minus), 1.0);
        decay(p(delta_minus), out(neutron), null, out(pion_minus), 1.0);
        decay(p(antidelta_minus), out(antineutron), null, out(pion_plus), 1.0);

        // Pair production from photons (negative energyReleased = absorbed)
        decayExplicit(particle(photon, 1, 1120, 1.0), out(electron), null, out(positron),
                0L, 230000L, -1020L, 1.0);
        decayExplicit(particle(photon, 1, 233000, 1.0), out(muon), null, out(antimuon),
                0L, Long.MAX_VALUE, -211000L, 0.5);
    }

    /**
     * Add a decay recipe with computed energy released (in keV from particle mass delta).
     * Pads missing outputs to a 3-slot list, preserving slot order to match QMD semantics.
     */
    private static void decay(ParticleStack input, ParticleStack out0, ParticleStack out1, ParticleStack out2, double crossSection) {
        long energyReleased = computeEnergyReleased(input, out0, out1, out2);
        List<ParticleStack> outs = packOutputs(out0, out1, out2);
        if (outs.isEmpty()) return;
        decayChamber(input, outs, 0L, Long.MAX_VALUE, energyReleased, crossSection);
    }

    /**
     * Add a decay recipe with explicit energy bounds and energyReleased value.
     */
    private static void decayExplicit(ParticleStack input, ParticleStack out0, ParticleStack out1, ParticleStack out2,
                                      long minEnergy, long maxEnergy, long energyReleased, double crossSection) {
        List<ParticleStack> outs = packOutputs(out0, out1, out2);
        if (outs.isEmpty()) return;
        decayChamber(input, outs, minEnergy, maxEnergy, energyReleased, crossSection);
    }

    private static List<ParticleStack> packOutputs(ParticleStack... outs) {
        List<ParticleStack> list = new ArrayList<>(outs.length);
        for (ParticleStack s : outs) {
            if (s != null) list.add(s);
        }
        return list;
    }

    private static long computeEnergyReleased(ParticleStack in, ParticleStack... outs) {
        double inMass = in.getParticle().getMass() * in.getAmount();
        double outMass = 0;
        for (ParticleStack s : outs) {
            if (s == null) continue;
            outMass += s.getParticle().getMass() * s.getAmount();
        }
        return Math.round((inMass - outMass) * 1000D);
    }

    private static ParticleStack p(Particle particle) {
        return new ParticleStack(particle, 1, 0, 1.0);
    }

    private static ParticleStack particle(Particle particle, int amount, long meanEnergy, double focus) {
        return new ParticleStack(particle, amount, meanEnergy, focus);
    }

    private static ParticleStack out(Particle particle) {
        return new ParticleStack(particle, 1, 0, 1.0);
    }

    private static ParticleStack out(int amount, Particle particle) {
        return new ParticleStack(particle, amount, 0, 1.0);
    }
}
