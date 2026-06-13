package igentuman.nc.datagen.recipes.recipes;

import igentuman.nc.content.particles.Particle;
import igentuman.nc.content.particles.ParticleStack;
import net.minecraft.data.recipes.FinishedRecipe;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static igentuman.nc.content.particles.Particles.*;

/**
 * Collision chamber datagen recipes. Ported from QMD's CollisionChamberRecipes.
 * 2 input particles -> up to 4 output particles. Energy released computed from particle mass deltas
 * unless explicit values supplied (matching QMD's two addCollisionRecipe overloads).
 */
public class CollisionChamberRecipes extends AbstractRecipeProvider {

    public static void generate(Consumer<FinishedRecipe> consumer) {
        CollisionChamberRecipes.consumer = consumer;
        ID = "collision_chamber";

        // ===== Neutron absorption =====
        collide(in(proton, 5), in(neutron, 5),
                out(deuteron), out(photon), null, null,
                0L, 30000L, 2220L, 0.5);
        collide(in(antiproton, 5), in(antineutron, 5),
                out(antideuteron), out(photon), null, null,
                0L, 30000L, 2220L, 0.5);
        collide(in(deuteron, 5), in(neutron, 5),
                out(triton), out(photon), null, null,
                0L, 30000L, 6260L, 0.5);
        collide(in(antideuteron, 5), in(antineutron, 5),
                out(antitriton), out(photon), null, null,
                0L, 30000L, 6260L, 0.5);
        collide(in(helion, 5), in(neutron, 5),
                out(alpha), out(photon), null, null,
                0L, 30000L, 20600L, 0.5);
        collide(in(antihelion, 5), in(antineutron, 5),
                out(antialpha), out(photon), null, null,
                0L, 30000L, 20600L, 0.5);

        // ===== Fusion =====
        collide(in(proton, 5), in(proton, 5),
                out(deuteron), out(positron), out(electron_neutrino), null,
                700L, 10000L, 420L, 0.25);
        collide(in(antiproton, 5), in(antiproton, 5),
                out(antideuteron), out(electron), out(electron_antineutrino), null,
                700L, 10000L, 420L, 0.25);
        collide(in(proton, 5), in(deuteron, 5),
                out(helion), out(photon), null, null,
                700L, 10000L, 5490L, 0.5);
        collide(in(antiproton, 5), in(antideuteron, 5),
                out(antihelion), out(photon), null, null,
                700L, 10000L, 5490L, 0.5);
        collide(in(helion, 5), in(helion, 5),
                out(alpha), out(2, proton), null, null,
                700L, 10000L, 12900L, 0.5);
        collide(in(antihelion, 5), in(antihelion, 5),
                out(antialpha), out(2, antiproton), null, null,
                700L, 10000L, 12900L, 0.5);
        collide(in(deuteron, 5), in(triton, 5),
                out(alpha), out(neutron), null, null,
                700L, 10000L, 17600L, 0.5);
        collide(in(antideuteron, 5), in(antitriton, 5),
                out(antialpha), out(antineutron), null, null,
                700L, 10000L, 17600L, 0.5);
        collide(in(triton, 5), in(triton, 5),
                out(alpha), out(2, neutron), null, null,
                700L, 10000L, 11300L, 0.5);
        collide(in(antitriton, 5), in(antitriton, 5),
                out(antialpha), out(2, antineutron), null, null,
                700L, 10000L, 11300L, 0.5);
        collide(in(deuteron, 5), in(helion, 5),
                out(alpha), out(proton), null, null,
                700L, 10000L, 18400L, 0.5);
        collide(in(antideuteron, 5), in(antihelion, 5),
                out(antialpha), out(antiproton), null, null,
                700L, 10000L, 18400L, 0.5);
        collide(in(helion, 5), in(triton, 5),
                out(alpha), out(proton), out(neutron), null,
                700L, 10000L, 12100L, 0.5);
        collide(in(antihelion, 5), in(antitriton, 5),
                out(antialpha), out(antiproton), out(antineutron), null,
                700L, 10000L, 12100L, 0.5);
        collide(in(deuteron, 5), in(deuteron, 5),
                out(triton), out(proton), null, null,
                700L, 10000L, 4030L, 0.5);
        collide(in(antideuteron, 5), in(antideuteron, 5),
                out(antitriton), out(antiproton), null, null,
                700L, 10000L, 4030L, 0.5);

        // ===== Antimatter annihilation =====
        collide(in(proton, 5), in(antiproton, 5),
                out(4, pion_plus), out(4, pion_naught), out(4, pion_minus), null,
                0L, 50_000_000L, 220000L, 1.0);
        collide(in(neutron, 5), in(antineutron, 5),
                out(4, pion_plus), out(4, pion_naught), out(4, pion_minus), null,
                0L, 50_000_000L, 223000L, 1.0);
        collide(in(proton, 5), in(antineutron, 5),
                out(5, pion_plus), out(4, pion_naught), out(4, pion_minus), null,
                0L, 50_000_000L, 81800L, 1.0);
        collide(in(antiproton, 5), in(neutron, 5),
                out(4, pion_plus), out(4, pion_naught), out(5, pion_minus), null,
                0L, 50_000_000L, 81800L, 1.0);

        // ===== High energy collisions (energyReleased auto-computed) =====
        collideAuto(in(electron, 5), in(electron, 5),
                out(2, electron_neutrino), out(2, muon), out(2, muon_antineutrino), null, 0.025);
        collideAuto(in(positron, 5), in(positron, 5),
                out(2, electron_antineutrino), out(2, antimuon), out(2, muon_neutrino), null, 0.025);
        collideAuto(in(electron, 5), in(positron, 5),
                out(muon), out(antimuon), null, null, 0.10);
        collideAuto(in(proton, 5), in(proton, 5),
                out(2, proton), out(pion_plus), out(pion_minus), out(pion_naught), 0.10);
        collideAuto(in(antiproton, 5), in(antiproton, 5),
                out(2, antiproton), out(pion_minus), out(pion_plus), out(pion_naught), 0.10);
        collideAuto(in(proton, 5), in(proton, 5),
                out(delta_minus), out(sigma_plus), out(kaon_plus), out(pion_plus), 0.025);
        collideAuto(in(antiproton, 5), in(antiproton, 5),
                out(antidelta_minus), out(antisigma_plus), out(kaon_minus), out(pion_minus), 0.025);
        collideAuto(in(electron, 5), in(positron, 5),
                out(tau), out(antitau), null, null, 0.025);
        collideAuto(in(proton, 5), in(proton, 5),
                out(neutron), out(delta_plus_plus), out(z_boson), null, 0.025);
        collideAuto(in(antiproton, 5), in(antiproton, 5),
                out(antineutron), out(antidelta_plus_plus), out(z_boson), null, 0.025);
        collideAuto(in(proton, 5), in(antiproton, 5),
                out(glueball), out(w_plus_boson), out(w_minus_boson), out(charmed_eta), 0.025);
        collideAuto(in(proton, 5), in(proton, 5),
                out(2, higgs_boson), out(delta_plus_plus), out(kaon_plus), out(sigma_minus), 0.025);
        collideAuto(in(antiproton, 5), in(antiproton, 5),
                out(2, higgs_boson), out(antidelta_plus_plus), out(kaon_minus), out(antisigma_minus), 0.025);
    }

    /** Explicit-energy collision recipe (matches QMD addCollisionRecipe with minEnergy/energyReleased/maxEnergy). */
    private static void collide(ParticleStack inA, ParticleStack inB,
                                ParticleStack out0, ParticleStack out1, ParticleStack out2, ParticleStack out3,
                                long minEnergy, long maxEnergy, long energyReleased, double crossSection) {
        List<ParticleStack> outs = packOutputs(out0, out1, out2, out3);
        if (outs.isEmpty()) return;
        // QMD seeds inputs with minEnergy as meanEnergy so JEI displays it correctly
        inA.setMeanEnergy(minEnergy);
        inB.setMeanEnergy(minEnergy);
        collisionChamber(inA, inB, outs, minEnergy, maxEnergy, energyReleased, crossSection);
    }

    /** Mass-derived energy collision recipe (matches QMD addCollisionRecipe short overload). */
    private static void collideAuto(ParticleStack inA, ParticleStack inB,
                                    ParticleStack out0, ParticleStack out1, ParticleStack out2, ParticleStack out3,
                                    double crossSection) {
        List<ParticleStack> outs = packOutputs(out0, out1, out2, out3);
        if (outs.isEmpty()) return;
        double inputMass = inA.getParticle().getMass() * inA.getAmount() + inB.getParticle().getMass() * inB.getAmount();
        double outputMass = 0;
        for (ParticleStack s : outs) outputMass += s.getParticle().getMass() * s.getAmount();
        long energyReleased = Math.round((inputMass - outputMass) * 1000D);
        long minEnergy = Math.round(Math.abs((inputMass - outputMass) * 1000D) * 1.1D);
        long maxEnergy = Math.round(minEnergy * 1.5D);
        inA.setMeanEnergy(minEnergy);
        inB.setMeanEnergy(minEnergy);
        collisionChamber(inA, inB, outs, minEnergy, maxEnergy, energyReleased, crossSection);
    }

    private static List<ParticleStack> packOutputs(ParticleStack... outs) {
        List<ParticleStack> list = new ArrayList<>(outs.length);
        for (ParticleStack s : outs) {
            if (s != null) list.add(s);
        }
        return list;
    }

    private static ParticleStack in(Particle particle, double focus) {
        return new ParticleStack(particle, 1, 0, focus);
    }

    private static ParticleStack out(Particle particle) {
        return new ParticleStack(particle, 1, 0, 1.0);
    }

    private static ParticleStack out(int amount, Particle particle) {
        return new ParticleStack(particle, amount, 0, 1.0);
    }
}
