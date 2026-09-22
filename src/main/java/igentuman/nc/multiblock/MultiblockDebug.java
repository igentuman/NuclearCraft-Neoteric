package igentuman.nc.multiblock;

import igentuman.nc.NuclearCraft;
import igentuman.nc.api.multiblock.IMultiblockCache;
import igentuman.nc.api.multiblock.StructureFootprint;
import igentuman.nc.config.Multiblocks;
import igentuman.nc.multiblock.geometry.BoxFootprint;
import igentuman.nc.multiblock.geometry.LinearTubeFootprint;
import igentuman.nc.multiblock.geometry.SquareRingFootprint;
import igentuman.nc.multiblock.geometry.StructureTransform;
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

    private static final ThreadLocal<LegacyTrace> LEGACY_TRACE = new ThreadLocal<>();

    private MultiblockDebug() {
    }

    public static boolean enabled() {
        return Multiblocks.SPEC.isLoaded() && Multiblocks.DEBUG_LOGGING.get();
    }

    public static void log(String message, Object... arguments) {
        if (enabled()) NuclearCraft.LOGGER.info("[Multiblock] " + message, arguments);
    }

    public static void beginLegacy(ResourceLocation machineId, BlockPos controllerPos) {
        LEGACY_TRACE.remove();
        if (!enabled()) return;
        LEGACY_TRACE.set(new LegacyTrace(machineId, controllerPos.immutable(), System.nanoTime()));
        log("{} validation START controller={}", machineId, controllerPos.toShortString());
    }

    public static void step(String step, Object... arguments) {
        if (!enabled()) return;
        LegacyTrace trace = LEGACY_TRACE.get();
        if (trace == null) return;
        log("{} validation STEP {} - {}", trace.machineId, trace.controllerPos.toShortString(),
                format(step, arguments));
    }

    public static void bounds(BlockPos minimum, BlockPos maximum) {
        LegacyTrace trace = LEGACY_TRACE.get();
        if (trace == null) return;
        trace.minimum = minimum.immutable();
        trace.maximum = maximum.immutable();
        trace.start = trace.minimum;
        trace.end = trace.maximum;
        step("resolved bounds {} -> {}", minimum.toShortString(), maximum.toShortString());
    }

    public static void fail(String diagnosticKey, BlockPos position,
                            @Nullable ResourceLocation expected, @Nullable ResourceLocation actual) {
        LegacyTrace trace = LEGACY_TRACE.get();
        if (trace == null) return;
        trace.diagnosticKey = diagnosticKey;
        trace.failingPosition = position.immutable();
        trace.expected = expected;
        trace.actual = actual;
        log("{} validation FAIL controller={} step={} pos={} expected={} actual={}", trace.machineId,
                trace.controllerPos.toShortString(), diagnosticKey, position.toShortString(), expected, actual);
    }

    public static void finishLegacy(ServerLevel level, ResourceLocation machineId, BlockPos controllerPos,
                                    IMultiblockCache cache, boolean valid) {
        LegacyTrace trace = LEGACY_TRACE.get();
        LEGACY_TRACE.remove();
        if (!enabled()) return;
        if (trace == null) trace = new LegacyTrace(machineId, controllerPos.immutable(), System.nanoTime());
        if (trace.minimum == null || trace.maximum == null) {
            Bounds cacheBounds = bounds(cache);
            if (cacheBounds != null) {
                trace.minimum = cacheBounds.minimum;
                trace.maximum = cacheBounds.maximum;
                trace.start = cacheBounds.start;
                trace.end = cacheBounds.end;
            }
        }
        long elapsed = Math.max(0, System.nanoTime() - trace.startedNanos);
        String key = valid ? "multiblock.validation.valid" : trace.diagnosticKey;
        log("{} validation END controller={} result={} elapsed={}ms bounds={} -> {} error={} diagnostic={}",
                machineId, controllerPos.toShortString(), valid ? "VALID" : "INVALID",
                String.format(Locale.ROOT, "%.3f", elapsed / 1_000_000D), trace.minimum, trace.maximum,
                trace.failingPosition, key);
        send(level, new PacketMultiblockDebug(controllerPos, machineId, "validation",
                valid ? "VALID" : "INVALID", key, trace.minimum, trace.maximum, trace.start, trace.end,
                trace.failingPosition, trace.expected, trace.actual, valid ? cache.getStructurePositions().size() : 0,
                cache.getStructurePositions().size(), elapsed));
    }

    public static void send(ServerLevel level, PacketMultiblockDebug packet) {
        if (!enabled()) return;
        PacketDistributor.sendToPlayersTrackingChunk(level,
                new ChunkPos(packet.controllerPos()), packet);
    }

    @Nullable
    public static Bounds bounds(IMultiblockCache cache) {
        if (cache.hasAABB()) {
            BlockPos minimum = BlockPos.of(cache.aabbMinPacked());
            BlockPos maximum = BlockPos.of(cache.aabbMaxPacked());
            return new Bounds(minimum, maximum, minimum, maximum);
        }
        if (cache.getStructurePositions().isEmpty()) return null;
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
        for (long packed : cache.getStructurePositions()) {
            BlockPos pos = BlockPos.of(packed);
            minX = Math.min(minX, pos.getX());
            minY = Math.min(minY, pos.getY());
            minZ = Math.min(minZ, pos.getZ());
            maxX = Math.max(maxX, pos.getX());
            maxY = Math.max(maxY, pos.getY());
            maxZ = Math.max(maxZ, pos.getZ());
        }
        BlockPos minimum = new BlockPos(minX, minY, minZ);
        BlockPos maximum = new BlockPos(maxX, maxY, maxZ);
        return new Bounds(minimum, maximum, minimum, maximum);
    }

    public static Bounds bounds(StructureFootprint footprint) {
        StructureTransform transform;
        int width;
        int height;
        int depth;
        if (footprint instanceof BoxFootprint box) {
            transform = box.transform();
            width = box.width();
            height = box.height();
            depth = box.depth();
        } else if (footprint instanceof LinearTubeFootprint linear) {
            transform = linear.transform();
            width = linear.crossSection();
            height = linear.crossSection();
            depth = linear.length();
        } else if (footprint instanceof SquareRingFootprint ring) {
            transform = ring.transform();
            width = ring.outerSide();
            height = ring.height();
            depth = ring.outerSide();
        } else {
            return cursorBounds(footprint);
        }
        BlockPos start = transform.origin();
        BlockPos end = transform.toWorld(width - 1, height - 1, depth - 1);
        return cornerBounds(transform, width, height, depth, start, end);
    }

    private static Bounds cornerBounds(StructureTransform transform, int width, int height, int depth,
                                       BlockPos start, BlockPos end) {
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
        for (int right : new int[]{0, width - 1}) {
            for (int up : new int[]{0, height - 1}) {
                for (int forward : new int[]{0, depth - 1}) {
                    BlockPos pos = transform.toWorld(right, up, forward);
                    minX = Math.min(minX, pos.getX());
                    minY = Math.min(minY, pos.getY());
                    minZ = Math.min(minZ, pos.getZ());
                    maxX = Math.max(maxX, pos.getX());
                    maxY = Math.max(maxY, pos.getY());
                    maxZ = Math.max(maxZ, pos.getZ());
                }
            }
        }
        return new Bounds(new BlockPos(minX, minY, minZ), new BlockPos(maxX, maxY, maxZ), start, end);
    }

    private static Bounds cursorBounds(StructureFootprint footprint) {
        StructureFootprint.Cursor cursor = footprint.cursor();
        BlockPos start = null;
        BlockPos end = null;
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
        while (cursor.hasNext()) {
            BlockPos pos = cursor.next().pos();
            if (start == null) start = pos;
            end = pos;
            minX = Math.min(minX, pos.getX());
            minY = Math.min(minY, pos.getY());
            minZ = Math.min(minZ, pos.getZ());
            maxX = Math.max(maxX, pos.getX());
            maxY = Math.max(maxY, pos.getY());
            maxZ = Math.max(maxZ, pos.getZ());
        }
        if (start == null) throw new IllegalArgumentException("Multiblock footprint must not be empty");
        return new Bounds(new BlockPos(minX, minY, minZ), new BlockPos(maxX, maxY, maxZ), start, end);
    }

    private static String format(String template, Object... arguments) {
        String result = template;
        for (Object argument : arguments) result = result.replaceFirst("\\{\\}",
                Matcher.quoteReplacement(String.valueOf(argument)));
        return result;
    }

    public record Bounds(BlockPos minimum, BlockPos maximum, BlockPos start, BlockPos end) {
    }

    private static final class LegacyTrace {
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

        private LegacyTrace(ResourceLocation machineId, BlockPos controllerPos, long startedNanos) {
            this.machineId = machineId;
            this.controllerPos = controllerPos;
            this.startedNanos = startedNanos;
        }
    }
}
