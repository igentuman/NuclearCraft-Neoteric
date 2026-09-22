package igentuman.nc.multiblock.validation;

public record ValidationBudget(
        long maximumNanos,
        int maximumBlockReads,
        int maximumBlockEntityReads,
        int maximumMutations
) {
    public ValidationBudget {
        if (maximumNanos <= 0 || maximumBlockReads <= 0 || maximumBlockEntityReads < 0 || maximumMutations < 0) {
            throw new IllegalArgumentException("Validation budget values are invalid");
        }
    }

    public ValidationBudget withMaximumNanos(long nanos) {
        return new ValidationBudget(Math.max(1, Math.min(maximumNanos, nanos)), maximumBlockReads,
                maximumBlockEntityReads, maximumMutations);
    }
}
