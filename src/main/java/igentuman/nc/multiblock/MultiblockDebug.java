package igentuman.nc.multiblock;

import igentuman.nc.NuclearCraft;
import igentuman.nc.api.multiblock.AbstractMultiblockCache;
import igentuman.nc.api.multiblock.AbstractMultiblockValidator;
import igentuman.nc.config.Multiblocks;
import igentuman.nc.network.PacketMultiblockDebug;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.regex.Matcher;

public final class MultiblockDebug {

    private static final ThreadLocal<Trace> TRACE = new ThreadLocal<>();

    private MultiblockDebug() {
    }

    public static boolean enabled() {
        return Multiblocks.SPEC.isLoaded() && Multiblocks.DEBUG_LOGGING.get();
    }

    public static void log(String message, Object... arguments) {
        if (enabled()) NuclearCraft.LOGGER.info("[Multiblock] " + message, arguments);
    }

    public static void beginTrace(ResourceLocation machineId, BlockPos controllerPos) {
        TRACE.remove();
        if (!enabled()) return;
        TRACE.set(new Trace(machineId, controllerPos.immutable(), System.nanoTime()));
        log("{} validation START controller={}", machineId, controllerPos.toShortString());
    }

    public static void step(String step, Object... arguments) {
        if (!enabled()) return;
        Trace trace = TRACE.get();
        if (trace == null) return;
        log("{} validation STEP {} - {}", trace.machineId, trace.controllerPos.toShortString(),
                format(step, arguments));
    }

    public static void bounds(BlockPos minimum, BlockPos maximum) {
        Trace trace = TRACE.get();
        if (trace == null) return;
        trace.minimum = minimum.immutable();
        trace.maximum = maximum.immutable();
        trace.start = trace.minimum;
        trace.end = trace.maximum;
        step("resolved bounds {} -> {}", minimum.toShortString(), maximum.toShortString());
    }

    public static void fail(String diagnosticKey, BlockPos position,
                            @Nullable ResourceLocation expected, @Nullable ResourceLocation actual) {
        Trace trace = TRACE.get();
        if (trace == null) return;
        trace.diagnosticKey = diagnosticKey;
        trace.failingPosition = position.immutable();
        trace.expected = expected;
        trace.actual = actual;
        log("{} validation FAIL controller={} step={} pos={} expected={} actual={}", trace.machineId,
                trace.controllerPos.toShortString(), diagnosticKey, position.toShortString(), expected, actual);
    }

    public static void endTrace() {
        TRACE.remove();
    }

    public static void finishValidation(ServerLevel level, ResourceLocation machineId, BlockPos controllerPos,
                                        AbstractMultiblockCache cache,
                                        AbstractMultiblockValidator.Result result, long elapsedNanos) {
        if (!enabled()) return;
        BlockPos minimum = cache.min();
        BlockPos maximum = cache.max();
        int cells = minimum == null || maximum == null ? 0
                : (maximum.getX() - minimum.getX() + 1) * (maximum.getY() - minimum.getY() + 1)
                * (maximum.getZ() - minimum.getZ() + 1);
        String key = result.valid() || result.errorKey() == null
                ? "multiblock.validation.valid" : result.errorKey();
        log("{} validation END controller={} result={} elapsed={}ms bounds={} -> {} error={} diagnostic={}",
                machineId, controllerPos.toShortString(), result.outcome(),
                String.format(Locale.ROOT, "%.3f", elapsedNanos / 1_000_000D), minimum, maximum,
                result.errorPos(), key);
        send(level, new PacketMultiblockDebug(controllerPos, machineId, "validation", result.outcome().name(), key,
                minimum, maximum, minimum, maximum, result.errorPos(), result.expected(), result.actual(),
                result.valid() ? cells : 0, cells, elapsedNanos));
    }

    public static void send(ServerLevel level, PacketMultiblockDebug packet) {
        if (!enabled()) return;
        PacketDistributor.sendToPlayersTrackingChunk(level,
                new ChunkPos(packet.controllerPos()), packet);
    }

    private static String format(String template, Object... arguments) {
        String result = template;
        for (Object argument : arguments) result = result.replaceFirst("\\{\\}",
                Matcher.quoteReplacement(String.valueOf(argument)));
        return result;
    }

    private static final class Trace {
        private final ResourceLocation machineId;
        private final BlockPos controllerPos;
        private final long startedNanos;
        private String diagnosticKey = "multiblock.validation.invalid";
        private BlockPos minimum;
        private BlockPos maximum;
        private BlockPos start;
        private BlockPos end;
        private BlockPos failingPosition;
        private ResourceLocation expected;
        private ResourceLocation actual;

        private Trace(ResourceLocation machineId, BlockPos controllerPos, long startedNanos) {
            this.machineId = machineId;
            this.controllerPos = controllerPos;
            this.startedNanos = startedNanos;
        }
    }
}
