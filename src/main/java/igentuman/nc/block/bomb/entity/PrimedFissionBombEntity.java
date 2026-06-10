package igentuman.nc.block.bomb.entity;

import com.mojang.authlib.GameProfile;
import igentuman.nc.NuclearCraft;
import igentuman.nc.radiation.data.RadiationManager;
import igentuman.nc.block.bomb.sim.BlastOp;
import igentuman.nc.block.bomb.sim.BlastTask;
import igentuman.nc.block.bomb.sim.BombSimulationExecutor;
import igentuman.nc.entity.EntityBlockProjectile;
import igentuman.nc.handler.config.CommonConfig;
import igentuman.nc.network.toClient.PacketBombDetonationStart;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ThreadedLevelLightEngine;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.common.world.ForgeChunkManager;
import net.minecraftforge.event.level.BlockEvent;

import java.util.*;

import static igentuman.nc.setup.registration.Entities.BLOCK_PROJECTILE;

public class PrimedFissionBombEntity extends Entity {

    public static final int PRELOAD_END = 20;
    public static final int FLASH_END = 24;
    public static final int FIREBALL_MIN_TICKS = 16;
    public static final int FIREBALL_MAX_END = 200;
    public static final int MUSHROOM_MIN_TICKS = 4;
    public static final int CLEANUP_TICKS = 20;

    public static final int SET_BLOCK_FLAGS = 2 | 16 | 32;

    public enum Phase { PRELOAD, FLASH, FIREBALL, MUSHROOM, CLEANUP, DONE }

    private float yield = 1.0f;
    private String placerUuid = "";

    public void setPlacerUuid(String uuid) {
        this.placerUuid = uuid;
    }

    public String getPlacerUuid() {
        return this.placerUuid;
    }
    private int hRadius = 128;
    private int vRadius = 64;
    private Phase phase = Phase.PRELOAD;
    private BlastTask task;
    private final Set<Long> forcedChunks = new HashSet<>();
    private final Set<Long> chunksToResend = new HashSet<>();
    private final Set<Long> dirtyFastChunks = new HashSet<>();
    private final Set<Long> dirtyFastSections = new HashSet<>();
    private final Map<Long, Boolean> protectedChunks = new HashMap<>();
    private long detonationStartNanos = 0L;
    private long phaseStartNanos = 0L;
    private int opsApplied = 0;
    private int opsAppliedTotal = 0;
    private boolean simDoneLogged = false;
    private long phaseDrainNanos = 0L;
    private long phaseDrainMaxNanos = 0L;
    private int phaseDrainTicks = 0;
    private int phaseStartTick = 0;
    private final LongArrayList writtenPositions = new LongArrayList();
    private final java.util.concurrent.ConcurrentLinkedQueue<Long> pendingResend = new java.util.concurrent.ConcurrentLinkedQueue<>();
    private final java.util.concurrent.atomic.AtomicInteger relightPending = new java.util.concurrent.atomic.AtomicInteger(0);
    private boolean cleanupDone = false;

    public PrimedFissionBombEntity(EntityType<? extends PrimedFissionBombEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public void configure(float yield, int hRadius, int vRadius) {
        this.yield = yield;
        this.hRadius = hRadius;
        this.vRadius = vRadius;
    }

    public void preForceEpicenter() {
        if (!(level() instanceof ServerLevel server)) return;
        BlockPos epicenter = blockPosition();
        int cx = epicenter.getX() >> 4;
        int cz = epicenter.getZ() >> 4;
        ForgeChunkManager.forceChunk(server, NuclearCraft.MODID, this.getUUID(), cx, cz, true, true);
        forcedChunks.add(ChunkPos.asLong(cx, cz));
    }

    public Phase phase() { return phase; }
    public float yield() { return yield; }
    public int hRadius() { return hRadius; }
    public int vRadius() { return vRadius; }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) return;
        if (task == null && phase == Phase.PRELOAD) {
            startSimulation();
        }
        Phase prev = phase;
        phase = computePhase();
        if (phase != prev) {
            onPhaseExit(prev);
            phaseStartTick = tickCount;
            onPhaseEnter(phase);
        }
        if (task != null && !simDoneLogged && task.done.get()) {
            simDoneLogged = true;
            NuclearCraft.LOGGER.info("[Bomb] Sim thread done at t+{}ms (tick={}, phase={})",
                    (System.nanoTime() - detonationStartNanos) / 1_000_000L, tickCount, phase);
        }
        if (phase == Phase.FIREBALL || phase == Phase.MUSHROOM) {
            drainOps(igentuman.nc.handler.config.CommonConfig.BOMB_CONFIG.OPS_PER_TICK.get());
        }
        if (level() instanceof ServerLevel sv) {
            drainPendingResend(sv, igentuman.nc.handler.config.CommonConfig.BOMB_CONFIG.CHUNK_RESENDS_PER_TICK.get());
        }
        if (phase == Phase.DONE && cleanupDone && pendingResend.isEmpty() && relightPending.get() <= 0) {
            long totalMs = (System.nanoTime() - detonationStartNanos) / 1_000_000L;
            NuclearCraft.LOGGER.info("[Bomb] Discard (relight+resend complete) at t+{}ms (tick={})", totalMs, tickCount);
            releaseForcedChunks();
            discard();
        }
    }

    private void drainPendingResend(ServerLevel server, int max) {
        if (pendingResend.isEmpty()) return;
        int sent = 0;
        Long key;
        while (sent < max && (key = pendingResend.poll()) != null) {
            int cx = ChunkPos.getX(key);
            int cz = ChunkPos.getZ(key);
            ChunkPos cpos = new ChunkPos(cx, cz);
            LevelChunk chunk = server.getChunkSource().getChunkNow(cx, cz);
            if (chunk != null) {
                ClientboundLevelChunkWithLightPacket pkt = new ClientboundLevelChunkWithLightPacket(
                        chunk, server.getChunkSource().getLightEngine(), null, null);
                for (ServerPlayer p : server.getChunkSource().chunkMap.getPlayers(cpos, false)) {
                    p.connection.send(pkt);
                }
            }
            relightPending.decrementAndGet();
            sent++;
        }
    }

    private Phase computePhase() {
        if (tickCount < PRELOAD_END) return Phase.PRELOAD;
        if (tickCount < FLASH_END) return Phase.FLASH;
        boolean queueDrained = task != null && task.done.get() && task.outQueue.isEmpty();
        int sincePhase = tickCount - phaseStartTick;
        switch (phase) {
            case PRELOAD, FLASH -> {
                return Phase.FIREBALL;
            }
            case FIREBALL -> {
                if (tickCount >= FIREBALL_MAX_END) return Phase.MUSHROOM;
                if (queueDrained && sincePhase >= FIREBALL_MIN_TICKS) return Phase.MUSHROOM;
                return Phase.FIREBALL;
            }
            case MUSHROOM -> {
                if (sincePhase >= MUSHROOM_MIN_TICKS) return Phase.CLEANUP;
                return Phase.MUSHROOM;
            }
            case CLEANUP -> {
                if (sincePhase >= CLEANUP_TICKS) return Phase.DONE;
                return Phase.CLEANUP;
            }
            default -> {
                return Phase.DONE;
            }
        }
    }

    private void onPhaseExit(Phase p) {
        long now = System.nanoTime();
        long phaseMs = (now - phaseStartNanos) / 1_000_000L;
        long totalMs = (now - detonationStartNanos) / 1_000_000L;
        long drainSumMs = phaseDrainNanos / 1_000_000L;
        long drainMaxMs = phaseDrainMaxNanos / 1_000_000L;
        long drainAvgUs = phaseDrainTicks > 0 ? (phaseDrainNanos / 1000L) / phaseDrainTicks : 0L;
        NuclearCraft.LOGGER.info("[Bomb] Phase {} done: {}ms (t+{}ms, opsApplied={}, drainTicks={}, drainSum={}ms, drainMax={}ms, drainAvg={}us)",
                p, phaseMs, totalMs, opsApplied, phaseDrainTicks, drainSumMs, drainMaxMs, drainAvgUs);
        opsAppliedTotal += opsApplied;
        opsApplied = 0;
        phaseDrainNanos = 0L;
        phaseDrainMaxNanos = 0L;
        phaseDrainTicks = 0;
        phaseStartNanos = now;
    }

    private void onPhaseEnter(Phase p) {
        switch (p) {
            case FLASH -> {
                broadcastDetonationStart();
                applyEntityDamage();
            }
            case CLEANUP -> {
                drainOps(Integer.MAX_VALUE);
                if (level() instanceof ServerLevel server) {
                    long t0 = System.nanoTime();
                    flushFastDirtyChunks(server);
                    flushChunkResends(server, Integer.MAX_VALUE);
                    NuclearCraft.LOGGER.info("[Bomb] Cleanup flush: heightmap+fluids+relight-scheduled={}ms (pendingChunks={})",
                            (System.nanoTime() - t0) / 1_000_000L, relightPending.get());
                }
                cleanupDone = true;
            }
            case FIREBALL, MUSHROOM -> { }
            case DONE -> {
                long totalMs = (System.nanoTime() - detonationStartNanos) / 1_000_000L;
                NuclearCraft.LOGGER.info("[Bomb] Detonation complete: total={}ms ticks={} opsAppliedTotal={}",
                        totalMs, tickCount, opsAppliedTotal);
            }
        }
    }

    private void startSimulation() {
        if (!(level() instanceof ServerLevel server)) return;
        detonationStartNanos = System.nanoTime();
        phaseStartNanos = detonationStartNanos;
        long snapshotStart = detonationStartNanos;
        BlockPos epicenter = blockPosition();
        int cxMin = (epicenter.getX() - hRadius) >> 4;
        int cxMax = (epicenter.getX() + hRadius) >> 4;
        int czMin = (epicenter.getZ() - hRadius) >> 4;
        int czMax = (epicenter.getZ() + hRadius) >> 4;
        Map<Long, ChunkAccess> snapshot = new HashMap<>();
        for (int cx = cxMin; cx <= cxMax; cx++) {
            for (int cz = czMin; cz <= czMax; cz++) {
                ForgeChunkManager.forceChunk(server, NuclearCraft.MODID, this.getUUID(), cx, cz, true, true);
                forcedChunks.add(ChunkPos.asLong(cx, cz));
                ChunkAccess chunk = server.getChunk(cx, cz, ChunkStatus.FULL, true);
                snapshot.put(ChunkPos.asLong(cx, cz), chunk);
            }
        }
        long snapshotMs = (System.nanoTime() - snapshotStart) / 1_000_000L;
        int chunkCount = snapshot.size();
        task = new BlastTask(epicenter, hRadius, vRadius, yield, snapshot);
        BombSimulationExecutor.getExecutor().submit(task);
        NuclearCraft.LOGGER.info("[Bomb] Detonation start: pos={} yield={} hR={} vR={} chunks={} snapshot={}ms",
                epicenter, yield, hRadius, vRadius, chunkCount, snapshotMs);
    }

    private void drainOps(int max) {
        if (task == null || !(level() instanceof ServerLevel server)) return;
        long t0 = System.nanoTime();
        int applied = 0;
        BlastOp op;
        while (applied < max && (op = task.outQueue.poll()) != null) {
            applyOp(server, op);
            applied++;
        }
        long drainNs = System.nanoTime() - t0;
        if (applied > 0) {
            phaseDrainNanos += drainNs;
            if (drainNs > phaseDrainMaxNanos) phaseDrainMaxNanos = drainNs;
            phaseDrainTicks++;
        }
        opsApplied += applied;
        flushChunkResends(server, CommonConfig.BOMB_CONFIG.CHUNK_RESENDS_PER_TICK.get());
    }

    private Player cachedFakePlayer = null;

    private Player getPlacerPlayer(ServerLevel server) {
        if (placerUuid == null || placerUuid.isEmpty()) return null;
        try {
            UUID uuid = UUID.fromString(placerUuid);
            Player p = server.getPlayerByUUID(uuid);
            if (p != null) return p;
            if (cachedFakePlayer != null) return cachedFakePlayer;
            cachedFakePlayer = FakePlayerFactory.get(server, new GameProfile(uuid, "[NC]BOMB"));
            return cachedFakePlayer;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private void applyOp(ServerLevel server, BlastOp op) {
        if (op instanceof BlastOp.SetBlock sb) {
            BlockState oldState = server.getBlockState(sb.pos());
            Player player = getPlacerPlayer(server);
            if (player != null) {
                int cx = sb.pos().getX() >> 4;
                int cz = sb.pos().getZ() >> 4;
                long chunkKey = ChunkPos.asLong(cx, cz);
                if (protectedChunks.containsKey(chunkKey)) {
                    if (protectedChunks.get(chunkKey)) {
                        return;
                    }
                } else {
                    if (!oldState.isAir()) {
                        BlockEvent.BreakEvent breakEvent = new BlockEvent.BreakEvent(server, sb.pos(), oldState, player);
                        if (MinecraftForge.EVENT_BUS.post(breakEvent)) {
                            protectedChunks.put(chunkKey, true);
                            return;
                        }
                        protectedChunks.put(chunkKey, false);
                    }
                }
            }
            if (CommonConfig.BOMB_CONFIG.FAST_BLOCK_WRITES.get()) {
                applySetBlockFast(server, sb.pos(), sb.state());
            } else {
                server.setBlock(sb.pos(), sb.state(), SET_BLOCK_FLAGS);
            }
            if (oldState != null && !oldState.isAir() && sb.state().isAir()) {
                BlockPos epicenter = blockPosition();
                double dx = sb.pos().getX() - epicenter.getX();
                double dy = sb.pos().getY() - epicenter.getY();
                double dz = sb.pos().getZ() - epicenter.getZ();
                double invHSq = 1.0 / ((double) hRadius * hRadius);
                double invVSq = 1.0 / ((double) vRadius * vRadius);
                double dSq = (dx * dx + dz * dz) * invHSq + (dy * dy) * invVSq;

                //TODO implement debris
                /*if (dSq >= 0.7 && dSq <= 1.0) {
                    if (server.random.nextFloat() < 0.05f) {
                        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
                        if (dist > 0.0) {
                            Vec3 dir = new Vec3(dx, Math.max(dy, 0) + 0.6, dz).normalize();
                            EntityBlockProjectile debris = new EntityBlockProjectile(BLOCK_PROJECTILE.get(), server);
                            double sx = sb.pos().getX() + 0.5 + dir.x * 1.5;
                            double sy = sb.pos().getY() + 0.5 + dir.y * 1.5;
                            double sz = sb.pos().getZ() + 0.5 + dir.z * 1.5;
                            debris.setPos(sx, sy, sz);
                            float velocity = 2.2f + server.random.nextFloat() * 0.6f;
                            float inaccuracy = 2.0f;
                            debris.shoot(dir.x, dir.y, dir.z, velocity, inaccuracy);
                            server.addFreshEntity(debris);
                        }
                    }
                }*/
            }
        } else if (op instanceof BlastOp.VoidSection vs) {
            voidSection(server, vs.sx(), vs.sy(), vs.sz());
        } else if (op instanceof BlastOp.RadiationDeposit rd) {
            RadiationManager.get(server).addRadiation(server, rd.amount(), rd.chunk().x * 16 + 8, blockPosition().getY(), rd.chunk().z * 16 + 8);
        }
    }

    private void applySetBlockFast(ServerLevel server, BlockPos pos, BlockState state) {
        int cx = pos.getX() >> 4;
        int cz = pos.getZ() >> 4;
        LevelChunk chunk = server.getChunkSource().getChunkNow(cx, cz);
        if (chunk == null) return;
        int idx = chunk.getSectionIndex(pos.getY());
        LevelChunkSection[] sections = chunk.getSections();
        if (idx < 0 || idx >= sections.length) return;
        LevelChunkSection sec = sections[idx];
        int lx = pos.getX() & 15;
        int ly = pos.getY() & 15;
        int lz = pos.getZ() & 15;
        BlockState old = sec.setBlockState(lx, ly, lz, state, false);
        if (old.hasBlockEntity()) {
            chunk.removeBlockEntity(pos);
        }
        chunk.setUnsaved(true);
        long chunkKey = ChunkPos.asLong(cx, cz);
        dirtyFastChunks.add(chunkKey);
        dirtyFastSections.add(SectionPos.asLong(cx, pos.getY() >> 4, cz));
        chunksToResend.add(chunkKey);
        writtenPositions.add(pos.asLong());
    }

    private void flushFastDirtyChunks(ServerLevel server) {
        if (dirtyFastChunks.isEmpty() && writtenPositions.isEmpty()) return;
        ThreadedLevelLightEngine le =
                (ThreadedLevelLightEngine) server.getChunkSource().getLightEngine();

        scheduleFluidAndFallingTicks(server);

        for (long secKey : dirtyFastSections) {
            int sx = SectionPos.x(secKey);
            int sy = SectionPos.y(secKey);
            int sz = SectionPos.z(secKey);
            le.updateSectionStatus(SectionPos.of(sx, sy, sz), false);
        }
        dirtyFastSections.clear();

        for (long key : dirtyFastChunks) {
            int cx = ChunkPos.getX(key);
            int cz = ChunkPos.getZ(key);
            LevelChunk chunk = server.getChunkSource().getChunkNow(cx, cz);
            if (chunk == null) continue;
            Heightmap.primeHeightmaps(
                    chunk,
                    java.util.EnumSet.of(
                            Heightmap.Types.MOTION_BLOCKING,
                            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                            Heightmap.Types.OCEAN_FLOOR,
                            Heightmap.Types.WORLD_SURFACE));
            le.setLightEnabled(chunk.getPos(), true);
            chunk.setLightCorrect(false);
            relightPending.incrementAndGet();
            le.lightChunk(chunk, false).thenAccept(c -> {
                pendingResend.add(ChunkPos.asLong(c.getPos().x, c.getPos().z));
            });
        }
        dirtyFastChunks.clear();
    }

    private void scheduleFluidAndFallingTicks(ServerLevel server) {
        if (writtenPositions.isEmpty()) return;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        int scheduled = 0;
        for (int i = 0; i < writtenPositions.size(); i++) {
            long packed = writtenPositions.getLong(i);
            int x = BlockPos.getX(packed);
            int y = BlockPos.getY(packed);
            int z = BlockPos.getZ(packed);
            for (Direction d : Direction.values()) {
                cursor.set(x + d.getStepX(), y + d.getStepY(), z + d.getStepZ());
                LevelChunk nchunk =
                        server.getChunkSource().getChunkNow(cursor.getX() >> 4, cursor.getZ() >> 4);
                if (nchunk == null) continue;
                BlockState ns = nchunk.getBlockState(cursor);
                FluidState fs = ns.getFluidState();
                if (!fs.isEmpty() && !server.getFluidTicks().hasScheduledTick(cursor, fs.getType())) {
                    server.scheduleTick(cursor.immutable(), fs.getType(), fs.getType().getTickDelay(server));
                    scheduled++;
                } else if (ns.getBlock() instanceof FallingBlock) {
                    if (!server.getBlockTicks().hasScheduledTick(cursor, ns.getBlock())) {
                        server.scheduleTick(cursor.immutable(), ns.getBlock(), 2);
                        scheduled++;
                    }
                }
            }
        }
        NuclearCraft.LOGGER.info("[Bomb] Fluid/falling ticks scheduled: {} (from {} writes)",
                scheduled, writtenPositions.size());
        writtenPositions.clear();
    }

    private void voidSection(ServerLevel server, int sx, int sy, int sz) {
        LevelChunk chunk = server.getChunkSource().getChunkNow(sx, sz);
        if (chunk == null) return;
        int idx = chunk.getSectionIndexFromSectionY(sy);
        LevelChunkSection[] sections = chunk.getSections();
        if (idx < 0 || idx >= sections.length) return;

        net.minecraft.world.entity.player.Player player = getPlacerPlayer(server);
        if (player != null) {
            long chunkKey = ChunkPos.asLong(sx, sz);
            boolean isProtected = false;
            if (protectedChunks.containsKey(chunkKey)) {
                isProtected = protectedChunks.get(chunkKey);
            } else {
                int yMin = sy << 4;
                BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();
                boolean foundBlockToCheck = false;
                boolean checkResult = false;

                for (int x = 0; x < 16; x++) {
                    for (int y = 0; y < 16; y++) {
                        for (int z = 0; z < 16; z++) {
                            mpos.set((sx << 4) + x, yMin + y, (sz << 4) + z);
                            BlockState state = chunk.getBlockState(mpos);
                            if (!state.isAir()) {
                                foundBlockToCheck = true;
                                net.minecraftforge.event.level.BlockEvent.BreakEvent breakEvent =
                                        new net.minecraftforge.event.level.BlockEvent.BreakEvent(server, mpos.immutable(), state, player);
                                checkResult = net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(breakEvent);
                                break;
                            }
                        }
                        if (foundBlockToCheck) break;
                    }
                    if (foundBlockToCheck) break;
                }

                if (foundBlockToCheck) {
                    isProtected = checkResult;
                    protectedChunks.put(chunkKey, isProtected);
                } else {
                    isProtected = false;
                }
            }

            if (isProtected) {
                return;
            }

            Registry<Biome> biomeReg = server.registryAccess().registryOrThrow(Registries.BIOME);
            sections[idx] = new LevelChunkSection(biomeReg);
            int yMin = sy << 4;
            int yMax = yMin + 16;
            List<BlockPos> toRemove = new ArrayList<>();
            for (BlockPos pos : chunk.getBlockEntities().keySet()) {
                if (pos.getY() >= yMin && pos.getY() < yMax) toRemove.add(pos);
            }
            for (BlockPos pos : toRemove) chunk.removeBlockEntity(pos);
            chunk.setUnsaved(true);
            server.getChunkSource().getLightEngine().updateSectionStatus(SectionPos.of(sx, sy, sz), true);
            long key = ChunkPos.asLong(sx, sz);
            chunksToResend.add(key);
            dirtyFastChunks.add(key);
            return;
        }

        Registry<Biome> biomeReg = server.registryAccess().registryOrThrow(Registries.BIOME);
        sections[idx] = new LevelChunkSection(biomeReg);

        int yMin = sy << 4;
        int yMax = yMin + 16;
        List<BlockPos> toRemove = new ArrayList<>();
        for (BlockPos pos : chunk.getBlockEntities().keySet()) {
            if (pos.getY() >= yMin && pos.getY() < yMax) toRemove.add(pos);
        }
        for (BlockPos pos : toRemove) chunk.removeBlockEntity(pos);

        chunk.setUnsaved(true);
        server.getChunkSource().getLightEngine().updateSectionStatus(SectionPos.of(sx, sy, sz), true);
        long key = ChunkPos.asLong(sx, sz);
        chunksToResend.add(key);
        dirtyFastChunks.add(key);
    }

    private void flushChunkResends(ServerLevel server, int max) {
        if (chunksToResend.isEmpty()) return;
        Iterator<Long> it = chunksToResend.iterator();
        int sent = 0;
        while (it.hasNext() && sent < max) {
            long key = it.next();
            int cx = ChunkPos.getX(key);
            int cz = ChunkPos.getZ(key);
            ChunkPos cpos = new ChunkPos(cx, cz);
            LevelChunk chunk = server.getChunkSource().getChunkNow(cx, cz);
            if (chunk != null) {
                ClientboundLevelChunkWithLightPacket pkt = new ClientboundLevelChunkWithLightPacket(
                        chunk, server.getChunkSource().getLightEngine(), null, null);
                for (ServerPlayer p : server.getChunkSource().chunkMap.getPlayers(cpos, false)) {
                    p.connection.send(pkt);
                }
            }
            it.remove();
            sent++;
        }
    }

    private void releaseForcedChunks() {
        if (!(level() instanceof ServerLevel server)) return;
        for (long key : forcedChunks) {
            int cx = ChunkPos.getX(key);
            int cz = ChunkPos.getZ(key);
            ForgeChunkManager.forceChunk(server, NuclearCraft.MODID, this.getUUID(), cx, cz, false, true);
        }
        forcedChunks.clear();
    }

    @Override
    public void remove(RemovalReason reason) {
        releaseForcedChunks();
        super.remove(reason);
    }

    private void applyEntityDamage() {
        if (!(level() instanceof ServerLevel server)) return;
        BlockPos ep = blockPosition();
        double ex = ep.getX() + 0.5;
        double ey = ep.getY() + 0.5;
        double ez = ep.getZ() + 0.5;
        int hR = hRadius + 100;
        int vR = vRadius + 100;
        AABB box = new AABB(
                ex - hR, ey - vR, ez - hR,
                ex + hR, ey + vR, ez + hR);
        double invHSq = 1.0 / ((double) hR * hR);
        double invVSq = 1.0 / ((double) vR * vR);
        List<LivingEntity> targets = server.getEntitiesOfClass(LivingEntity.class, box);
        int hit = 0;
        for (LivingEntity le : targets) {
            double dx = le.getX() - ex;
            double dy = le.getY() - ey;
            double dz = le.getZ() - ez;
            double t = (dx * dx + dz * dz) * invHSq + (dy * dy) * invVSq;
            if (t >= 1.0) continue;
            int walls = countWallsOnRay(server, ex, ey, ez, le);
            float atten = (float) Math.pow(0.85, walls);
            float dmg = (float) (5000.0 * (1.0 - t)) * atten;
            if (dmg <= 0f) continue;
            le.hurt(server.damageSources().explosion(this, null), dmg);
            if (le.isAlive()) {
                le.setSecondsOnFire(30);
            }
            hit++;
        }
        NuclearCraft.LOGGER.info("[Bomb] Entity damage: scanned={} hit={}", targets.size(), hit);
    }

    private int countWallsOnRay(ServerLevel server, double sx, double sy, double sz, LivingEntity target) {
        Vec3 end = target.getEyePosition();
        double dx = end.x - sx;
        double dy = end.y - sy;
        double dz = end.z - sz;
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (dist < 1.0) return 0;
        int steps = (int) Math.ceil(dist * 2.0);
        double inv = 1.0 / steps;
        BlockPos.MutableBlockPos cur = new BlockPos.MutableBlockPos();
        long startKey = BlockPos.asLong((int) Math.floor(sx), (int) Math.floor(sy), (int) Math.floor(sz));
        long endKey = BlockPos.asLong((int) Math.floor(end.x), (int) Math.floor(end.y), (int) Math.floor(end.z));
        long lastKey = Long.MIN_VALUE;
        int count = 0;
        for (int i = 1; i < steps; i++) {
            double f = i * inv;
            int x = (int) Math.floor(sx + dx * f);
            int y = (int) Math.floor(sy + dy * f);
            int z = (int) Math.floor(sz + dz * f);
            long key = BlockPos.asLong(x, y, z);
            if (key == lastKey || key == startKey || key == endKey) continue;
            lastKey = key;
            cur.set(x, y, z);
            BlockState s = server.getBlockState(cur);
            if (s.isAir()) continue;
            if (s.getCollisionShape(server, cur).isEmpty()) continue;
            count++;
        }
        return count;
    }

    private void broadcastDetonationStart() {
        if (!(level() instanceof ServerLevel server)) return;
        PacketBombDetonationStart pkt = new PacketBombDetonationStart(getId(), blockPosition(), yield);
        NuclearCraft.packetHandler().sendToDimension(pkt, server.dimension());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        yield = tag.getFloat("yield");
        hRadius = tag.getInt("hRadius");
        vRadius = tag.getInt("vRadius");
        if (tag.contains("phase")) phase = Phase.valueOf(tag.getString("phase"));
        if (tag.contains("placerUuid")) placerUuid = tag.getString("placerUuid");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putFloat("yield", yield);
        tag.putInt("hRadius", hRadius);
        tag.putInt("vRadius", vRadius);
        tag.putString("phase", phase.name());
        tag.putString("placerUuid", placerUuid);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distanceSq) {
        return distanceSq < 25_000_000.0D;
    }

    @Override
    public boolean shouldRender(double x, double y, double z) {
        return true;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }
}
