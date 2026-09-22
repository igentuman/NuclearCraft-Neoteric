package igentuman.nc.particle;

public final class ParticlePhysics {

    private ParticlePhysics() {
    }

    /** {@code a * L * (1 + abs(q) * sqrt(N / S))}. */
    public static double focusLoss(long amount, double charge, double distance, double attenuation, double scaling) {
        requireNonNegative(amount, "amount");
        requireFinite(charge, "charge");
        requireNonNegative(distance, "distance");
        requireNonNegative(attenuation, "attenuation");
        requirePositive(scaling, "scaling");
        return attenuation * distance * (1 + Math.abs(charge) * Math.sqrt(amount / scaling));
    }

    /** {@code quadrupoleStrength * abs(q)}. */
    public static double focusGain(double quadrupoleStrength, double charge) {
        requireNonNegative(quadrupoleStrength, "quadrupoleStrength");
        requireFinite(charge, "charge");
        return quadrupoleStrength * Math.abs(charge);
    }

    /** {@code floor(voltageV * abs(q) / 1000 * controlFraction)}. */
    public static long linearGainKeV(long voltageV, double charge, double controlFraction) {
        requireNonNegative(voltageV, "voltageV");
        requireFinite(charge, "charge");
        requireRange(controlFraction, 0, 1, "controlFraction");
        return toCheckedLong(voltageV * Math.abs(charge) / 1000.0 * controlFraction);
    }

    /**
     * The lower of the dipole energy limit {@code (q*B*radius)^2 / (2m) * 1,000,000} and the radiation energy
     * limit {@code m * (3*voltageV*radius/abs(q))^(1/4) * 1,000,000}.
     */
    public static long ringMaximumEnergyKeV(double charge, double massMeV, double field, double radius, long voltageV) {
        requireFinite(charge, "charge");
        if (charge == 0) {
            throw new IllegalArgumentException("charge must not be zero");
        }
        requirePositive(massMeV, "massMeV");
        requireFinite(field, "field");
        requirePositive(radius, "radius");
        requireNonNegative(voltageV, "voltageV");

        double dipoleLimit = Math.pow(charge * field * radius, 2) / (2 * massMeV) * 1_000_000;
        double radicand = 3 * voltageV * radius / Math.abs(charge);
        double radiationLimit = massMeV * Math.pow(radicand, 0.25) * 1_000_000;
        return toCheckedLong(Math.min(dipoleLimit, radiationLimit));
    }

    /** {@code (E / (1000m))^3 / (2*pi*1,000,000*radius)}. */
    public static long synchrotronLossKeV(long energyKeV, double massMeV, double radius) {
        requireNonNegative(energyKeV, "energyKeV");
        requirePositive(massMeV, "massMeV");
        requirePositive(radius, "radius");
        double ratio = energyKeV / (1000.0 * massMeV);
        double loss = Math.pow(ratio, 3) / (2 * Math.PI * 1_000_000 * radius);
        return toCheckedLong(loss);
    }

    /** Focus loss for a fixed diverter straight-section distance of 3 blocks. */
    public static double diverterStraightFocusLoss(long amount, double charge, double attenuation, double scaling) {
        return focusLoss(amount, charge, 3, attenuation, scaling);
    }

    /** {@code 160 * (ln(dipoleStrength * 10) + 0.2)}. */
    public static double diverterTurnRadius(double dipoleStrength) {
        requirePositive(dipoleStrength, "dipoleStrength");
        return 160 * (Math.log(dipoleStrength * 10) + 0.2);
    }

    /** {@code E*q^2 / (6*m^4*radius^2)}. */
    public static long cornerEnergyLossKeV(long energyKeV, double charge, double massMeV, double radius) {
        requireNonNegative(energyKeV, "energyKeV");
        requireFinite(charge, "charge");
        requirePositive(massMeV, "massMeV");
        requirePositive(radius, "radius");
        double loss = energyKeV * (charge * charge) / (6 * Math.pow(massMeV, 4) * (radius * radius));
        return toCheckedLong(loss);
    }

    /** {@code 2*sqrt(Ea)*sqrt(Eb)}, avoiding overflow from computing {@code Ea*Eb} directly. */
    public static long collisionEnergyKeV(long energyA, long energyB) {
        requireNonNegative(energyA, "energyA");
        requireNonNegative(energyB, "energyB");
        return toCheckedLong(2 * Math.sqrt(energyA) * Math.sqrt(energyB));
    }

    /** {@code 1 - abs(Ea-Eb)/(Ea+Eb)}; defined as {@code 0} when both energies are zero. */
    public static double collisionSymmetry(long energyA, long energyB) {
        requireNonNegative(energyA, "energyA");
        requireNonNegative(energyB, "energyB");
        long total = Math.addExact(energyA, energyB);
        if (total == 0) {
            return 0.0;
        }
        long difference = Math.abs(Math.subtractExact(energyA, energyB));
        return 1.0 - (double) difference / (double) total;
    }

    /** {@code clamp(crossSection * eta * symmetry, 0, 1)}. */
    public static double collectionFactor(double crossSection, double efficiency, double symmetry) {
        requireNonNegative(crossSection, "crossSection");
        requireFinite(efficiency, "efficiency");
        requireFinite(symmetry, "symmetry");
        return Math.max(0, Math.min(1, crossSection * efficiency * symmetry));
    }

    private static long toCheckedLong(double value) {
        if (!Double.isFinite(value)) {
            throw new ArithmeticException("Non-finite result: " + value);
        }
        double floored = Math.floor(value);
        if (floored < Long.MIN_VALUE || floored > Long.MAX_VALUE) {
            throw new ArithmeticException("Result overflows long: " + floored);
        }
        return (long) floored;
    }

    private static void requireFinite(double value, String name) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException(name + " must be finite: " + value);
        }
    }

    private static void requireNonNegative(double value, String name) {
        requireFinite(value, name);
        if (value < 0) {
            throw new IllegalArgumentException(name + " must not be negative: " + value);
        }
    }

    private static void requireNonNegative(long value, String name) {
        if (value < 0) {
            throw new IllegalArgumentException(name + " must not be negative: " + value);
        }
    }

    private static void requirePositive(double value, String name) {
        requireFinite(value, name);
        if (value <= 0) {
            throw new IllegalArgumentException(name + " must be positive: " + value);
        }
    }

    private static void requireRange(double value, double minInclusive, double maxInclusive, String name) {
        requireFinite(value, name);
        if (value < minInclusive || value > maxInclusive) {
            throw new IllegalArgumentException(name + " must be within [" + minInclusive + ", " + maxInclusive + "]: " + value);
        }
    }
}
