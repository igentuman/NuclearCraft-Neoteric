package igentuman.nc.entity.bomb.sim;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

public class BlastTask implements Runnable {

    public static final double VOID_RATIO = 0.22;
    public static final int RIM_SKIP = 2;
    public static final double EDGE_JITTER_INNER = 0.01;
    public static final double EDGE_JITTER_OUTER = 0.825;
    public static final float EDGE_PRESERVE_MAX = 0.025f;
    public static final double SPILL_OUTER_SCALE = 1.4;
    public static final float SPILL_CHANCE_MAX = 0.015f;

    public final BlockPos epicenter;
    public final int hRadius;
    public final int vRadius;
    public final float yield;
    public final Map<Long, ChunkAccess> chunkSnapshot;
    public final Queue<BlastOp> outQueue = new ConcurrentLinkedQueue<>();
    public final AtomicBoolean done = new AtomicBoolean(false);
    public volatile long simStartNanos = 0L;
    public volatile long voidEmitNanos = 0L;
    public volatile long simEndNanos = 0L;
    public volatile int opsEmitted = 0;
    private final Set<Long> voidSections = new HashSet<>();

    public BlastTask(BlockPos epicenter, int hRadius, int vRadius, float yield, Map<Long, ChunkAccess> chunkSnapshot) {
        this.epicenter = epicenter;
        this.hRadius = hRadius;
        this.vRadius = vRadius;
        this.yield = yield;
        this.chunkSnapshot = chunkSnapshot;
    }

    @Override
    public void run() {
        simStartNanos = System.nanoTime();
        try {
            emitVoidSections();
            voidEmitNanos = System.nanoTime();
            march();
            emitRadiationDeposits();
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            simEndNanos = System.nanoTime();
            opsEmitted = outQueue.size();
            done.set(true);
            igentuman.nc.NuclearCraft.LOGGER.info(
                "[Bomb] Sim complete: void={}ms march={}ms total={}ms ops={}",
                (voidEmitNanos - simStartNanos) / 1_000_000L,
                (simEndNanos - voidEmitNanos) / 1_000_000L,
                (simEndNanos - simStartNanos) / 1_000_000L,
                opsEmitted);
        }
    }

    private void emitVoidSections() {
        double hVoid = hRadius * VOID_RATIO;
        double vVoid = vRadius * VOID_RATIO;
        if (hVoid < 8 || vVoid < 8) return;
        int sxMin = (int) Math.floor((epicenter.getX() - hVoid) / 16.0);
        int sxMax = (int) Math.floor((epicenter.getX() + hVoid) / 16.0);
        int syMin = (int) Math.floor((epicenter.getY() - vVoid) / 16.0);
        int syMax = (int) Math.floor((epicenter.getY() + vVoid) / 16.0);
        int szMin = (int) Math.floor((epicenter.getZ() - hVoid) / 16.0);
        int szMax = (int) Math.floor((epicenter.getZ() + hVoid) / 16.0);
        double ex = epicenter.getX();
        double ey = epicenter.getY();
        double ez = epicenter.getZ();
        for (int sx = sxMin; sx <= sxMax; sx++) {
            for (int sz = szMin; sz <= szMax; sz++) {
                long chunkKey = ChunkPos.asLong(sx, sz);
                if (!chunkSnapshot.containsKey(chunkKey)) continue;
                for (int sy = syMin; sy <= syMax; sy++) {
                    double minX = sx * 16.0, maxX = minX + 16.0;
                    double minY = sy * 16.0, maxY = minY + 16.0;
                    double minZ = sz * 16.0, maxZ = minZ + 16.0;
                    double fx = Math.max(Math.abs(ex - minX), Math.abs(ex - maxX));
                    double fy = Math.max(Math.abs(ey - minY), Math.abs(ey - maxY));
                    double fz = Math.max(Math.abs(ez - minZ), Math.abs(ez - maxZ));
                    double d = (fx * fx) / (hVoid * hVoid) + (fy * fy) / (vVoid * vVoid) + (fz * fz) / (hVoid * hVoid);
                    if (d <= 1.0) {
                        voidSections.add(SectionPos.asLong(sx, sy, sz));
                        outQueue.add(new BlastOp.VoidSection(sx, sy, sz));
                    }
                }
            }
        }
    }

    private void march() {
        int ex = epicenter.getX();
        int ey = epicenter.getY();
        int ez = epicenter.getZ();
        int hR = Math.max(1, hRadius - RIM_SKIP);
        int vR = Math.max(1, vRadius - RIM_SKIP);
        int hSpill = (int) Math.ceil(hRadius * SPILL_OUTER_SCALE);
        int vSpill = (int) Math.ceil(vRadius * SPILL_OUTER_SCALE);
        double invHSq = 1.0 / ((double) hRadius * hRadius);
        double invVSq = 1.0 / ((double) vRadius * vRadius);
        double rimSq = (hR / (double) hRadius) * (hR / (double) hRadius);
        double spillSq = SPILL_OUTER_SCALE * SPILL_OUTER_SCALE;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int bx = ex - hSpill; bx <= ex + hSpill; bx++) {
            double ndx = bx - ex;
            double xTerm = ndx * ndx * invHSq;
            if (xTerm > spillSq) continue;
            for (int bz = ez - hSpill; bz <= ez + hSpill; bz++) {
                double ndz = bz - ez;
                double xzTerm = xTerm + ndz * ndz * invHSq;
                if (xzTerm > spillSq) continue;
                for (int by = ey - vSpill; by <= ey + vSpill; by++) {
                    double ndy = by - ey;
                    double dSq = xzTerm + ndy * ndy * invVSq;
                    if (dSq > spillSq) continue;
                    if (dSq > rimSq) {
                        float t = (float) ((spillSq - dSq) / (spillSq - rimSq));
                        if (ThreadLocalRandom.current().nextFloat() > t * SPILL_CHANCE_MAX) continue;
                        cursor.set(bx, by, bz);
                        BlockState state = stateAt(cursor);
                        if (state == null || state.isAir()) continue;
                        outQueue.add(new BlastOp.SetBlock(cursor.immutable(), Blocks.AIR.defaultBlockState()));
                        continue;
                    }
                    if (dSq > EDGE_JITTER_INNER && dSq < EDGE_JITTER_OUTER) {
                        float t = (float) ((dSq - EDGE_JITTER_INNER) / (EDGE_JITTER_OUTER - EDGE_JITTER_INNER));
                        if (ThreadLocalRandom.current().nextFloat() < t * EDGE_PRESERVE_MAX) continue;
                    }
                    if (voidSections.contains(SectionPos.asLong(bx >> 4, by >> 4, bz >> 4))) continue;
                    cursor.set(bx, by, bz);
                    BlockState state = stateAt(cursor);
                    if (state == null) continue;
                    BlockPos posImm = cursor.immutable();
                    BlastOp op = BlockClassifier.classify(posImm, state, dSq, by - ey);
                    if (op == null) continue;
                    outQueue.add(op);
                    if (op instanceof BlastOp.SetBlock sb
                            && !sb.state().isAir()
                            && dSq >= 0.0625 && dSq < 0.16) {
                        BlockState above = stateAt(cursor.set(bx, by + 1, bz));
                        if (above != null && above.isAir()
                                && ThreadLocalRandom.current().nextFloat() < 0.05f) {
                            outQueue.add(new BlastOp.SetBlock(new BlockPos(bx, by + 1, bz), Blocks.FIRE.defaultBlockState()));
                        }
                    }
                }
            }
        }
    }
    private void emitRadiationDeposits() {
        double invHSq = 1.0 / ((double) hRadius * hRadius);
        int hSpill = (int) Math.ceil(hRadius * SPILL_OUTER_SCALE);
        int cxEpicenter = epicenter.getX() >> 4;
        int czEpicenter = epicenter.getZ() >> 4;
        int chunks = 0;
        for (int cx = cxEpicenter - hSpill; cx <= cxEpicenter + hSpill; cx++) {
            long dXCx = cx - cxEpicenter;
            double xTerm = dXCx * dXCx * invHSq;
            if (xTerm > 1.0) continue;
            for (int cz = czEpicenter - hSpill; cz <= czEpicenter + hSpill; cz++) {
                long dZCz = cz - czEpicenter;
                double dSq = xTerm + dZCz * dZCz * invHSq;
                if (dSq > 1.0) continue;
                if (!chunkSnapshot.containsKey(ChunkPos.asLong(cx, cz))) continue;
                double distanceFactor = clamp01(1.0 - dSq);
                if (distanceFactor <= 0.0) continue;
                outQueue.add(new BlastOp.RadiationDeposit(new ChunkPos(cx, cz), distanceFactor));
                chunks++;
            }
        }
        igentuman.nc.NuclearCraft.LOGGER.info("[Bomb] Sim fallout ops queued: {}", chunks);
    }

    private static double clamp01(double v) {
        return v < 0.0 ? 0.0 : (v > 1.0 ? 1.0 : v);
    }

    private BlockState stateAt(BlockPos pos) {
        long key = ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4);
        ChunkAccess chunk = chunkSnapshot.get(key);
        if (chunk == null) return null;
        if (pos.getY() < chunk.getMinBuildHeight() || pos.getY() >= chunk.getMaxBuildHeight()) return null;
        try {
            return chunk.getBlockState(pos);
        } catch (Throwable t) {
            return null;
        }
    }
}
