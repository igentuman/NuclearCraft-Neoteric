package igentuman.nc.multiblock.particle_chamber;

import igentuman.nc.api.particle.ParticleIngredient;
import igentuman.nc.api.particle.ParticleStack;

public final class TargetChamberProcessor {

    private TargetChamberProcessor() {
    }

    public static boolean matchesSpecies(ParticleIngredient ingredient, ParticleStack candidate) {
        if (candidate.isEmpty()) return false;
        if (!ingredient.species().contains(candidate.particleId())) return false;
        if (candidate.meanEnergyKeV() < ingredient.minimumEnergyKeV()
                || candidate.meanEnergyKeV() > ingredient.maximumEnergyKeV()) return false;
        return candidate.focus() >= ingredient.minimumFocus();
    }

    public static double yieldFactor(double crossSection, double efficiency) {
        return Math.max(0D, Math.min(1D, crossSection * efficiency));
    }

    public static double workAdded(long consumedAmount, double yieldFactor) {
        return consumedAmount * yieldFactor;
    }

    public record ByproductBatch(long amount, double carry) {
    }

    /** Next whole byproduct amount for one output row, carrying the fractional remainder forward. */
    public static ByproductBatch nextByproduct(long templateAmount, double yieldFactor, long consumedAmount,
                                                double previousCarry) {
        double raw = templateAmount * yieldFactor * consumedAmount + previousCarry;
        long amount = (long) Math.floor(raw);
        return new ByproductBatch(Math.max(0, amount), raw - amount);
    }
}
