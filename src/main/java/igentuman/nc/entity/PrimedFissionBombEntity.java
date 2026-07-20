package igentuman.nc.entity;

import com.mojang.authlib.GameProfile;
import igentuman.nc.NuclearCraft;
import igentuman.nc.entity.bomb.sim.BlastOp;
import igentuman.nc.entity.bomb.sim.BlastTask;
import igentuman.nc.entity.bomb.sim.BombFallout;
import igentuman.nc.entity.bomb.sim.BombSimulationExecutor;
import igentuman.nc.network.PacketBombDetonationStart;
import igentuman.nr.api.RadiationProfile;
import igentuman.nr.api.isotope.Isotope;
import igentuman.nr.api.isotope.IsotopeRegistry;
import igentuman.nr.api.isotope.IsotopeStack;
import igentuman.nr.radiation.source.LeftOverRadSource;
import igentuman.nr.radiation.source.WorldSourceRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ThreadedLevelLightEngine;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import static igentuman.nc.setup.entries.Bomb.TICKET_CONTROLLER;

public class PrimedFissionBombEntity extends Entity {

    public static final int PRELOAD_END = 20;
    public static final int FLASH_END = 24;
    public static final int FIREBALL_MIN_TICKS = 16;
    public static final int FIREBALL_MAX_END = 200;
    public static final int MUSHROOM_MIN_TICKS = 4;
    public static final int CLEANUP_TICKS = 20;

    public static final int SET_BLOCK_FLAGS = 2 | 16 | 32;

    public enum Phase { PRELOAD, FLASH, FIREBALL, MUSHROOM, CLEANUP, DONE }

    private static final EntityDataAccessor<Float> YIELD =
            SynchedEntityData.defineId(PrimedFissionBombEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> H_RADIUS =
            SynchedEntityData.defineId(PrimedFissionBombEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> V_RADIUS =
            SynchedEntityData.defineId(PrimedFissionBombEntity.class, EntityDataSerializers.INT);

    private float yield = 1.0f;
    private int hRadius = 128;
    private int vRadius = 64;
    private Phase phase = Phase.PRELOAD;
    private String placerUuid = "";

    private BlastTask task;
    private final Set<Long> forcedChunks = new HashSet<>();
    private final Set<Long> chunksToResend = new HashSet<>();
    private final Set<Long> dirtyFastChunks = new HashSet<>();
    private final Set<Long> dirtyFastSections = new HashSet<>();
    private final Map<Long, Boolean> protectedChunks = new HashMap<>();
    private final List<Long> writtenPositions = new ArrayList<>();
    private final ConcurrentLinkedQueue<Long> pendingResend = new ConcurrentLinkedQueue<>();
    private final AtomicInteger relightPending = new AtomicInteger(0);
    private long detonationStartNanos = 0L;
    private int opsAppliedTotal = 0;
    private boolean simDoneLogged = false;
    private int phaseStartTick = 0;
    private boolean cleanupDone = false;
    private Player cachedFakePlayer = null;

    public PrimedFissionBombEntity(EntityType<? extends PrimedFissionBombEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public void configure(float yield, int hRadius, int vRadius) {
        this.yield = yield;
        this.hRadius = hRadius;
        this.vRadius = vRadius;
        this.entityData.set(YIELD, yield);
        this.entityData.set(H_RADIUS, hRadius);
        this.entityData.set(V_RADIUS, vRadius);
    }

    public void setPlacerUuid(String uuid) {
        this.placerUuid = uuid;
    }

    public String getPlacerUuid() {
        return placerUuid;
    }

    public Phase phase() { return phase; }
    public float yield() { return yield; }
    public int hRadius() { return hRadius; }
    public int vRadius() { return vRadius; }

    public void preForceEpicenter() {
        if (!(level() instanceof ServerLevel server)) return;
        BlockPos epicenter = blockPosition();
        int cx = epicenter.getX() >> 4;
        int cz = epicenter.getZ() >> 4;
        forceChunk(server, cx, cz, true, true);
        forcedChunks.add(ChunkPos.asLong(cx, cz));
    }

    private void forceChunk(ServerLevel server, int cx, int cz, boolean add, boolean ticking) {
        try {
            TICKET_CONTROLLER.forceChunk(server, this, cx, cz, add, ticking);
        } catch (IllegalArgumentException e) {
            NuclearCraft.LOGGER.warn("[Bomb] ticket controller not registered; skipping forceChunk {},{}", cx, cz);
        }
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
            phaseStartTick = tickCount;
            onPhaseEnter(phase);
        }
        if (task != null && !simDoneLogged && task.done.get()) {
            simDoneLogged = true;
            NuclearCraft.LOGGER.info("[Bomb] Sim thread done at tick={} phase={}", tickCount, phase);
        }
        if (phase == Phase.FIREBALL || phase == Phase.MUSHROOM) {
            drainOps(igentuman.nc.config.Bomb.OPS_PER_TICK.get());
        }
        if (level() instanceof ServerLevel sv) {
            drainPendingResend(sv, igentuman.nc.config.Bomb.CHUNK_RESENDS_PER_TICK.get());
        }
        if (phase == Phase.DONE && cleanupDone && pendingResend.isEmpty() && relightPending.get() <= 0) {
            long totalMs = (System.nanoTime() - detonationStartNanos) / 1_000_000L;
            NuclearCraft.LOGGER.info("[Bomb] Discard (relight+resend complete) at t+{}ms (tick={}) opsTotal={}",
                    totalMs, tickCount, opsAppliedTotal);
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

    private void onPhaseEnter(Phase p) {
        switch (p) {
            case FLASH -> {
                broadcastDetonationStart();
                applyEntityDamage();
                registerEpicenterRadiationSource();
            }
            case CLEANUP -> {
                drainOps(Integer.MAX_VALUE);
                if (level() instanceof ServerLevel server) {
                    flushFastDirtyChunks(server);
                    flushChunkResends(server, Integer.MAX_VALUE);
                }
                cleanupDone = true;
            }
            default -> { }
        }
    }

    private void registerEpicenterRadiationSource() {
        if (!igentuman.nc.config.Bomb.RADIATION_ENABLED.get()) return;
        if (!(level() instanceof ServerLevel server)) return;
        long now = server.getGameTime();
        double yieldScale = Math.max(0.0, igentuman.nc.config.Bomb.RADIATION_SOURCE_YIELD_SCALE.get());
        RadiationProfile leftover = RadiationProfile.empty();
        for (var e : BombFallout.sourceAtoms().entrySet()) {
            Isotope iso = IsotopeRegistry.get(e.getKey());
            if (iso == null) continue;
            leftover.put(new IsotopeStack(iso, e.getValue() * yield * yieldScale, now));
        }
        if (leftover.isEmpty()) return;
        try {
            WorldSourceRegistry.get(server).register(
                    new LeftOverRadSource(server, blockPosition(), leftover, now, true));
            NuclearCraft.LOGGER.info("[Bomb] Epicenter LeftOverRadSource at {} yield={} yieldScale={}",
                    blockPosition(), yield, yieldScale);
        } catch (Throwable t) {
            NuclearCraft.LOGGER.warn("[Bomb] Failed to register LeftOverRadSource: {}", t.toString());
        }
    }

    private void startSimulation() {
        if (!(level() instanceof ServerLevel server)) return;
        detonationStartNanos = System.nanoTime();
        igentuman.nc.entity.bomb.sim.BlockClassifier.initializeRecipesMap(server);
        BlockPos epicenter = blockPosition();
        int cxMin = (epicenter.getX() - hRadius) >> 4;
        int cxMax = (epicenter.getX() + hRadius) >> 4;
        int czMin = (epicenter.getZ() - hRadius) >> 4;
        int czMax = (epicenter.getZ() + hRadius) >> 4;
        Map<Long, ChunkAccess> snapshot = new HashMap<>();
        for (int cx = cxMin; cx <= cxMax; cx++) {
            for (int cz = czMin; cz <= czMax; cz++) {
                forceChunk(server, cx, cz, true, false);
                forcedChunks.add(ChunkPos.asLong(cx, cz));
                ChunkAccess chunk = server.getChunk(cx, cz, ChunkStatus.FULL, true);
                snapshot.put(ChunkPos.asLong(cx, cz), chunk);
            }
        }
        task = new BlastTask(epicenter, hRadius, vRadius, yield, snapshot);
        BombSimulationExecutor.getExecutor().submit(task);
        NuclearCraft.LOGGER.info("[Bomb] Detonation start: pos={} yield={} hR={} vR={} chunks={}",
                epicenter, yield, hRadius, vRadius, snapshot.size());
    }

    private void drainOps(int max) {
        if (task == null || !(level() instanceof ServerLevel server)) return;
        int applied = 0;
        BlastOp op;
        while (applied < max && (op = task.outQueue.poll()) != null) {
            applyOp(server, op);
            applied++;
        }
        opsAppliedTotal += applied;
        flushChunkResends(server, igentuman.nc.config.Bomb.CHUNK_RESENDS_PER_TICK.get());
    }

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
            if (oldState.is(Blocks.BEDROCK)) return;
            Player player = getPlacerPlayer(server);
            if (player != null) {
                int cx = sb.pos().getX() >> 4;
                int cz = sb.pos().getZ() >> 4;
                long chunkKey = ChunkPos.asLong(cx, cz);
                if (protectedChunks.containsKey(chunkKey)) {
                    if (protectedChunks.get(chunkKey)) return;
                } else if (!oldState.isAir()) {
                    BlockEvent.BreakEvent breakEvent = new BlockEvent.BreakEvent(server, sb.pos(), oldState, player);
                    if (NeoForge.EVENT_BUS.post(breakEvent).isCanceled()) {
                        protectedChunks.put(chunkKey, true);
                        return;
                    }
                    protectedChunks.put(chunkKey, false);
                }
            }
            if (igentuman.nc.config.Bomb.FAST_BLOCK_WRITES.get()) {
                applySetBlockFast(server, sb.pos(), sb.state());
            } else {
                server.setBlock(sb.pos(), sb.state(), SET_BLOCK_FLAGS);
            }
        } else if (op instanceof BlastOp.VoidSection vs) {
            voidSection(server, vs.sx(), vs.sy(), vs.sz());
        } else if (op instanceof BlastOp.RadiationDeposit rd) {
            applyRadiationDeposit(server, rd);
        }
    }

    private void applyRadiationDeposit(ServerLevel server, BlastOp.RadiationDeposit rd) {
        if (!igentuman.nc.config.Bomb.RADIATION_ENABLED.get()) return;
        double densityMul = Math.max(0.0, igentuman.nc.config.Bomb.RADIATION_FALLOUT_AREAL_DENSITY.get());
        double scale = yield * rd.distanceFactor() * densityMul;
        if (scale <= 0.0) return;
        long now = server.getGameTime();
        RadiationProfile soil = RadiationProfile.empty();
        for (var e : BombFallout.fallbackAtoms().entrySet()) {
            Isotope iso = IsotopeRegistry.get(e.getKey());
            if (iso == null) continue;
            double atoms = e.getValue() * scale;
            if (atoms <= 0.0) continue;
            soil.put(new IsotopeStack(iso, atoms, now));
        }
        if (soil.isEmpty()) return;
        try {
            WorldSourceRegistry.get(server).setChunkRadiation(
                    rd.chunk(),
                    RadiationProfile.empty(),
                    RadiationProfile.empty(),
                    soil);
        } catch (Throwable t) {
            NuclearCraft.LOGGER.warn("[Bomb] Failed to setChunkRadiation for {}: {}", rd.chunk(), t.toString());
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
                    EnumSet.of(
                            Heightmap.Types.MOTION_BLOCKING,
                            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                            Heightmap.Types.OCEAN_FLOOR,
                            Heightmap.Types.WORLD_SURFACE));
            le.setLightEnabled(chunk.getPos(), true);
            chunk.setLightCorrect(false);
            relightPending.incrementAndGet();
            le.lightChunk(chunk, false).thenAccept(c ->
                    pendingResend.add(ChunkPos.asLong(c.getPos().x, c.getPos().z)));
        }
        dirtyFastChunks.clear();
    }

    private void scheduleFluidAndFallingTicks(ServerLevel server) {
        if (writtenPositions.isEmpty()) return;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (long packed : writtenPositions) {
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
                } else if (ns.getBlock() instanceof FallingBlock
                        && !server.getBlockTicks().hasScheduledTick(cursor, ns.getBlock())) {
                    server.scheduleTick(cursor.immutable(), ns.getBlock(), 2);
                }
            }
        }
        writtenPositions.clear();
    }

    private void voidSection(ServerLevel server, int sx, int sy, int sz) {
        LevelChunk chunk = server.getChunkSource().getChunkNow(sx, sz);
        if (chunk == null) return;
        int idx = chunk.getSectionIndexFromSectionY(sy);
        LevelChunkSection[] sections = chunk.getSections();
        if (idx < 0 || idx >= sections.length) return;

        Player player = getPlacerPlayer(server);
        if (player != null) {
            long chunkKey = ChunkPos.asLong(sx, sz);
            boolean isProtected;
            if (protectedChunks.containsKey(chunkKey)) {
                isProtected = protectedChunks.get(chunkKey);
            } else {
                int yMin = sy << 4;
                BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();
                boolean foundBlockToCheck = false;
                boolean checkResult = false;
                for (int x = 0; x < 16 && !foundBlockToCheck; x++) {
                    for (int y = 0; y < 16 && !foundBlockToCheck; y++) {
                        for (int z = 0; z < 16; z++) {
                            mpos.set((sx << 4) + x, yMin + y, (sz << 4) + z);
                            BlockState state = chunk.getBlockState(mpos);
                            if (!state.isAir()) {
                                foundBlockToCheck = true;
                                BlockEvent.BreakEvent breakEvent =
                                        new BlockEvent.BreakEvent(server, mpos.immutable(), state, player);
                                checkResult = NeoForge.EVENT_BUS.post(breakEvent).isCanceled();
                                break;
                            }
                        }
                    }
                }
                isProtected = foundBlockToCheck && checkResult;
                if (foundBlockToCheck) protectedChunks.put(chunkKey, isProtected);
            }
            if (isProtected) return;
        }

        int yMin = sy << 4;
        BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();
        boolean hasBedrock = false;
        LevelChunkSection sec = sections[idx];
        if (sec != null) {
            outer:
            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        mpos.set((sx << 4) + x, yMin + y, (sz << 4) + z);
                        if (chunk.getBlockState(mpos).is(Blocks.BEDROCK)) {
                            hasBedrock = true;
                            break outer;
                        }
                    }
                }
            }
        }

        if (hasBedrock && sec != null) {
            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        mpos.set((sx << 4) + x, yMin + y, (sz << 4) + z);
                        BlockState state = chunk.getBlockState(mpos);
                        if (!state.isAir() && !state.is(Blocks.BEDROCK)) {
                            sec.setBlockState(x, y, z, Blocks.AIR.defaultBlockState(), false);
                        }
                    }
                }
            }
        } else {
            Registry<Biome> biomeReg = server.registryAccess().registryOrThrow(Registries.BIOME);
            sections[idx] = new LevelChunkSection(biomeReg);
        }

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
            forceChunk(server, cx, cz, false, false);
            forceChunk(server, cx, cz, false, true);
        }
        forcedChunks.clear();
    }

    private void broadcastDetonationStart() {
        if (!(level() instanceof ServerLevel server)) return;
        PacketBombDetonationStart pkt = new PacketBombDetonationStart(getId(), blockPosition(), yield);
        PacketDistributor.sendToPlayersInDimension(server, pkt);
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
                le.igniteForSeconds(30);
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

    @Override
    public void remove(RemovalReason reason) {
        releaseForcedChunks();
        super.remove(reason);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(YIELD, 1.0f);
        builder.define(H_RADIUS, 128);
        builder.define(V_RADIUS, 64);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        yield = tag.getFloat("yield");
        hRadius = tag.getInt("hRadius");
        vRadius = tag.getInt("vRadius");
        placerUuid = tag.getString("placerUuid");
        String phaseName = tag.getString("phase");
        if (!phaseName.isEmpty()) {
            try {
                phase = Phase.valueOf(phaseName);
            } catch (IllegalArgumentException ignored) {
                phase = Phase.PRELOAD;
            }
        }
        entityData.set(YIELD, yield);
        entityData.set(H_RADIUS, hRadius);
        entityData.set(V_RADIUS, vRadius);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putFloat("yield", yield);
        tag.putInt("hRadius", hRadius);
        tag.putInt("vRadius", vRadius);
        tag.putString("placerUuid", placerUuid);
        tag.putString("phase", phase.name());
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean fireImmune() {
        return true;
    }
}
