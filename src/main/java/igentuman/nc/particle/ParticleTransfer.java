package igentuman.nc.particle;

import igentuman.nc.api.particle.IParticleHandler;
import igentuman.nc.api.particle.ParticleAction;
import igentuman.nc.api.particle.ParticleStack;

import java.util.HashSet;
import java.util.Set;
import java.util.function.UnaryOperator;

public final class ParticleTransfer {

    private static final ThreadLocal<Boolean> IN_TRANSFER = ThreadLocal.withInitial(() -> false);
    private static final ThreadLocal<Set<Object>> HOPPED_THIS_TICK = ThreadLocal.withInitial(HashSet::new);

    private ParticleTransfer() {
    }

    public static void resetTick() {
        HOPPED_THIS_TICK.get().clear();
    }

    public record Request(
            IParticleHandler source,
            int sourceChannel,
            IParticleHandler destination,
            int destinationChannel,
            Object hopKey,
            long maximumAmount,
            UnaryOperator<ParticleStack> attenuation
    ) {

        public Request(IParticleHandler source, int sourceChannel, IParticleHandler destination, int destinationChannel, Object hopKey) {
            this(source, sourceChannel, destination, destinationChannel, hopKey, Long.MAX_VALUE, UnaryOperator.identity());
        }
    }

    public record Result(ParticleStack transferred, ParticleStack remainder) {

        public static final Result NONE = new Result(ParticleStack.EMPTY, ParticleStack.EMPTY);
    }

    public static Result execute(Request request) {
        if (Boolean.TRUE.equals(IN_TRANSFER.get())) {
            throw new IllegalStateException("Particle transfer re-entered on the same server thread");
        }
        if (!HOPPED_THIS_TICK.get().add(request.hopKey())) {
            return Result.NONE;
        }

        IN_TRANSFER.set(true);
        try {
            ParticleStack snapshot = request.source().snapshot(request.sourceChannel());
            if (snapshot.isEmpty()) {
                return Result.NONE;
            }

            long cappedAmount = Math.min(snapshot.amount(), request.maximumAmount());
            if (cappedAmount <= 0) {
                return Result.NONE;
            }
            ParticleStack candidate = cappedAmount == snapshot.amount()
                    ? snapshot
                    : new ParticleStack(snapshot.particleId(), cappedAmount, snapshot.meanEnergyKeV(), snapshot.focus());

            ParticleStack attenuated = request.attenuation().apply(candidate);
            if (attenuated.isEmpty()) {
                return Result.NONE;
            }

            ParticleStack simulateRemainder = request.destination().insert(request.destinationChannel(), attenuated, ParticleAction.SIMULATE);
            long acceptedAmount = attenuated.amount() - simulateRemainder.amount();
            if (acceptedAmount <= 0) {
                return new Result(ParticleStack.EMPTY, attenuated);
            }
            ParticleStack acceptedCandidate = new ParticleStack(attenuated.particleId(), acceptedAmount, attenuated.meanEnergyKeV(), attenuated.focus());

            ParticleStack executeRemainder = request.destination().insert(request.destinationChannel(), acceptedCandidate, ParticleAction.EXECUTE);
            long committedAmount = acceptedAmount - executeRemainder.amount();
            if (committedAmount <= 0) {
                return new Result(ParticleStack.EMPTY, attenuated);
            }

            ParticleStack extracted = request.source().extract(request.sourceChannel(), committedAmount, ParticleAction.EXECUTE);
            long transferredAmount = Math.min(extracted.amount(), committedAmount);
            ParticleStack transferred = transferredAmount > 0
                    ? new ParticleStack(attenuated.particleId(), transferredAmount, attenuated.meanEnergyKeV(), attenuated.focus())
                    : ParticleStack.EMPTY;
            long refusedAmount = attenuated.amount() - transferredAmount;
            ParticleStack refused = refusedAmount > 0
                    ? new ParticleStack(attenuated.particleId(), refusedAmount, attenuated.meanEnergyKeV(), attenuated.focus())
                    : ParticleStack.EMPTY;
            return new Result(transferred, refused);
        } finally {
            IN_TRANSFER.set(false);
        }
    }
}
