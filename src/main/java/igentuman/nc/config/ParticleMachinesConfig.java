package igentuman.nc.config;

public record ParticleMachinesConfig(
        Accelerator accelerator,
        Chambers chambers,
        Scheduler scheduler,
        long revision
) {

    public record Accelerator(
            int scalePreset,
            double beamAttenuation,
            long beamScaling,
            long heatCapacityPerBlock,
            long baseEnergyRequirement,
            double thermalConductivity,
            long ringMinimumInputEnergyKeV,
            int overheatCooldownTicks,
            int beamConnectionReach
    ) {
    }

    public record Chambers(
            int targetMinimumSize,
            int targetMaximumSize,
            int decayMinimumSize,
            int decayMaximumSize,
            long decayBasePower,
            int collisionMinimumTransverseSize,
            int collisionMaximumTransverseSize,
            int collisionMinimumLength,
            int collisionMaximumLength,
            int collisionPreferredLength,
            long collisionBasePower
    ) {
    }

    public record Scheduler(
            long timeBudgetNanos,
            int blockReadBudget,
            int blockEntityReadBudget,
            int mutationBudget,
            int auditIntervalTicks
    ) {
    }
}
