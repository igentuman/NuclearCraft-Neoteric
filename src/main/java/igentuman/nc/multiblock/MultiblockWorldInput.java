package igentuman.nc.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.LongConsumer;

public final class MultiblockWorldInput {

    private static final BlockState AIR = Blocks.AIR.defaultBlockState();
    private static final long WAIT_MILLIS = 5000L;

    public record Section(boolean loaded,
                          @Nullable PalettedContainer<BlockState> states,
                          Map<Long, BlockEntity> blockEntities) {

        public static final Section UNLOADED = new Section(false, null, Map.of());
        public static final Section EMPTY = new Section(true, null, Map.of());

        public BlockState blockState(BlockPos pos) {
            return states == null ? AIR : states.get(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15);
        }

        @Nullable
        public BlockEntity blockEntity(BlockPos pos) {
            return blockEntities.get(pos.asLong());
        }
    }

    private record Request(Set<Long> sections,
                           LongConsumer watcher,
                           CompletableFuture<Map<Long, Section>> result) {}

    private static final Map<ResourceKey<Level>, Queue<Request>> QUEUES = new ConcurrentHashMap<>();

    private MultiblockWorldInput() {}

    public static Map<Long, Section> fetch(ServerLevel level, Set<Long> sectionKeys, LongConsumer watcher) {
        if (sectionKeys.isEmpty()) return Map.of();
        Request request = new Request(Set.copyOf(sectionKeys), watcher, new CompletableFuture<>());
        QUEUES.computeIfAbsent(level.dimension(), ignored -> new ConcurrentLinkedQueue<>()).add(request);
        try {
            level.getServer().execute(() -> service(level));
        } catch (RejectedExecutionException | NullPointerException shuttingDown) {
            request.result().complete(Map.of());
        }
        try {
            return request.result().get(WAIT_MILLIS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            request.result().complete(Map.of());
            return Map.of();
        } catch (Exception unavailable) {
            request.result().complete(Map.of());
            return Map.of();
        }
    }

    public static void service(ServerLevel level) {
        Queue<Request> queue = QUEUES.get(level.dimension());
        if (queue == null) return;
        Request request;
        while ((request = queue.poll()) != null) {
            if (request.result().isDone()) continue;
            Map<Long, Section> resolved = new HashMap<>();
            for (long sectionKey : request.sections()) {
                request.watcher().accept(sectionKey);
                resolved.put(sectionKey, read(level, sectionKey));
            }
            request.result().complete(resolved);
        }
    }

    public static void discard(ResourceKey<Level> dimension) {
        Queue<Request> queue = QUEUES.remove(dimension);
        if (queue == null) return;
        Request request;
        while ((request = queue.poll()) != null) request.result().complete(Map.of());
    }

    public static void discardAll() {
        for (ResourceKey<Level> dimension : Set.copyOf(QUEUES.keySet())) discard(dimension);
    }

    private static Section read(ServerLevel level, long sectionKey) {
        int sectionX = SectionPos.x(sectionKey);
        int sectionY = SectionPos.y(sectionKey);
        int sectionZ = SectionPos.z(sectionKey);
        LevelChunk chunk = level.getChunkSource().getChunkNow(sectionX, sectionZ);
        if (chunk == null) return Section.UNLOADED;
        int index = chunk.getSectionIndexFromSectionY(sectionY);
        if (index < 0 || index >= chunk.getSections().length) return Section.EMPTY;
        LevelChunkSection section = chunk.getSection(index);
        PalettedContainer<BlockState> states = section.hasOnlyAir() ? null : section.getStates().copy();
        Map<Long, BlockEntity> blockEntities = Map.of();
        for (Map.Entry<BlockPos, BlockEntity> entry : chunk.getBlockEntities().entrySet()) {
            BlockPos pos = entry.getKey();
            if (SectionPos.blockToSectionCoord(pos.getY()) != sectionY) continue;
            BlockEntity blockEntity = entry.getValue();
            if (blockEntity.isRemoved()) continue;
            if (blockEntities.isEmpty()) blockEntities = new HashMap<>();
            blockEntities.put(pos.asLong(), blockEntity);
        }
        return new Section(true, states, blockEntities);
    }
}
