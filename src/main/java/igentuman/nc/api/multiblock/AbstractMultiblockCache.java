package igentuman.nc.api.multiblock;

import igentuman.nc.multiblock.MultiblockWorldInput;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractMultiblockCache implements IMultiblockCache {

    @FunctionalInterface
    public interface InputSource {
        Map<Long, MultiblockWorldInput.Section> fetch(Set<Long> sectionKeys);
    }

    public static final class UnloadedInputException extends RuntimeException {
        private final BlockPos position;

        public UnloadedInputException(BlockPos position) {
            super(null, null, false, false);
            this.position = position.immutable();
        }

        public BlockPos position() {
            return position;
        }
    }

    private final Map<Long, BlockState> blockStates = new ConcurrentHashMap<>();
    private final Map<Long, BlockEntity> blockEntities = new ConcurrentHashMap<>();
    private final Map<Long, MultiblockWorldInput.Section> sections = new ConcurrentHashMap<>();

    private BlockPos workingMin;
    private BlockPos workingMax;
    private final Set<Long> workingPorts = new HashSet<>();
    private final Set<Long> workingChecked = new HashSet<>();

    private volatile BlockPos publishedMin;
    private volatile BlockPos publishedMax;
    private volatile Set<Long> publishedPorts = Set.of();
    private volatile Set<Long> publishedChecked = Set.of();

    private volatile BlockPos controllerPos = BlockPos.ZERO;
    private volatile Direction facing = Direction.NORTH;
    private volatile InputSource input;

    public final void bind(BlockPos controllerPos, Direction facing) {
        this.controllerPos = controllerPos.immutable();
        this.facing = facing;
    }

    public final void bindInput(@Nullable InputSource source) {
        this.input = source;
    }

    public final BlockPos controllerPos() {
        return controllerPos;
    }

    public final Direction facing() {
        return facing;
    }

    public final BlockState getBlockState(BlockPos pos) {
        long key = pos.asLong();
        BlockState cached = blockStates.get(key);
        if (cached != null) return cached;
        BlockState state = section(pos).blockState(pos);
        blockStates.put(key, state);
        return state;
    }

    @Nullable
    public final BlockEntity getBlockEntity(BlockPos pos) {
        long key = pos.asLong();
        BlockEntity cached = blockEntities.get(key);
        if (cached != null) {
            if (!cached.isRemoved()) return cached;
            blockEntities.remove(key);
        }
        BlockEntity fresh = section(pos).blockEntity(pos);
        if (fresh == null || fresh.isRemoved()) return null;
        blockEntities.put(key, fresh);
        return fresh;
    }

    public final void prefetch(BlockPos from, BlockPos to) {
        int minSectionX = Math.min(from.getX(), to.getX()) >> 4;
        int maxSectionX = Math.max(from.getX(), to.getX()) >> 4;
        int minSectionY = Math.min(from.getY(), to.getY()) >> 4;
        int maxSectionY = Math.max(from.getY(), to.getY()) >> 4;
        int minSectionZ = Math.min(from.getZ(), to.getZ()) >> 4;
        int maxSectionZ = Math.max(from.getZ(), to.getZ()) >> 4;
        Set<Long> missing = new HashSet<>();
        for (int x = minSectionX; x <= maxSectionX; x++) {
            for (int y = minSectionY; y <= maxSectionY; y++) {
                for (int z = minSectionZ; z <= maxSectionZ; z++) {
                    long key = SectionPos.asLong(x, y, z);
                    if (!sections.containsKey(key)) missing.add(key);
                }
            }
        }
        if (!missing.isEmpty()) acquire(missing, from);
    }

    private MultiblockWorldInput.Section section(BlockPos pos) {
        long sectionKey = SectionPos.asLong(pos);
        MultiblockWorldInput.Section section = sections.get(sectionKey);
        if (section == null) {
            acquire(neighbourhood(sectionKey), pos);
            section = sections.get(sectionKey);
        }
        if (section == null || !section.loaded()) throw new UnloadedInputException(pos);
        return section;
    }

    private static Set<Long> neighbourhood(long sectionKey) {
        int x = SectionPos.x(sectionKey);
        int y = SectionPos.y(sectionKey);
        int z = SectionPos.z(sectionKey);
        return Set.of(sectionKey, SectionPos.asLong(x, y - 1, z), SectionPos.asLong(x, y + 1, z));
    }

    private void acquire(Set<Long> sectionKeys, BlockPos origin) {
        InputSource source = input;
        if (source == null) throw new UnloadedInputException(origin);
        Map<Long, MultiblockWorldInput.Section> resolved = source.fetch(sectionKeys);
        if (resolved.isEmpty()) throw new UnloadedInputException(origin);
        sections.putAll(resolved);
    }

    public final void dropSectionInputs() {
        sections.clear();
    }

    public final void refreshInputs() {
        sections.clear();
        blockStates.clear();
        blockEntities.clear();
    }

    @Override
    public final void invalidate(BlockPos pos) {
        long key = pos.asLong();
        blockStates.remove(key);
        blockEntities.remove(key);
        sections.remove(SectionPos.asLong(pos));
    }

    public final void invalidateChunk(int chunkX, int chunkZ) {
        blockStates.keySet().removeIf(key -> inChunk(key, chunkX, chunkZ));
        blockEntities.keySet().removeIf(key -> inChunk(key, chunkX, chunkZ));
        sections.keySet().removeIf(key -> SectionPos.x(key) == chunkX && SectionPos.z(key) == chunkZ);
    }

    private static boolean inChunk(long packed, int chunkX, int chunkZ) {
        BlockPos pos = BlockPos.of(packed);
        return (pos.getX() >> 4) == chunkX && (pos.getZ() >> 4) == chunkZ;
    }

    public final void resetValidationData() {
        workingMin = null;
        workingMax = null;
        workingPorts.clear();
        workingChecked.clear();
        resetWorkingData();
    }

    public final void publish() {
        publishedMin = workingMin;
        publishedMax = workingMax;
        publishedPorts = Set.copyOf(workingPorts);
        publishedChecked = Set.copyOf(workingChecked);
        publishData();
    }

    protected abstract void resetWorkingData();

    protected abstract void publishData();

    protected void saveData(CompoundTag tag) {
    }

    protected void loadData(CompoundTag tag) {
    }

    public final void setWorkingBounds(BlockPos min, BlockPos max) {
        workingMin = min.immutable();
        workingMax = max.immutable();
    }

    @Nullable
    public final BlockPos workingMin() {
        return workingMin;
    }

    @Nullable
    public final BlockPos workingMax() {
        return workingMax;
    }

    public final void addWorkingPort(BlockPos pos) {
        workingPorts.add(pos.asLong());
    }

    public final void addWorkingChecked(BlockPos pos) {
        workingChecked.add(pos.asLong());
    }

    public final Set<Long> workingPorts() {
        return workingPorts;
    }

    public final Set<Long> ports() {
        return publishedPorts;
    }

    public final Set<Long> checkedPositions() {
        return publishedChecked;
    }

    @Nullable
    public final BlockPos min() {
        return publishedMin;
    }

    @Nullable
    public final BlockPos max() {
        return publishedMax;
    }

    public final void restore(@Nullable BlockPos min, @Nullable BlockPos max, Set<Long> ports) {
        publishedMin = min == null ? null : min.immutable();
        publishedMax = max == null ? null : max.immutable();
        publishedPorts = Set.copyOf(ports);
    }

    @Nullable
    public final BlockState knownState(BlockPos pos) {
        return blockStates.get(pos.asLong());
    }

    public final boolean containsExpanded(BlockPos pos, int margin) {
        BlockPos min = publishedMin;
        BlockPos max = publishedMax;
        if (min == null || max == null) return true;
        return pos.getX() >= min.getX() - margin && pos.getX() <= max.getX() + margin
                && pos.getY() >= min.getY() - margin && pos.getY() <= max.getY() + margin
                && pos.getZ() >= min.getZ() - margin && pos.getZ() <= max.getZ() + margin;
    }

    public final boolean contains(BlockPos pos) {
        BlockPos min = publishedMin;
        BlockPos max = publishedMax;
        if (min == null || max == null) return publishedChecked.contains(pos.asLong());
        return pos.getX() >= min.getX() && pos.getX() <= max.getX()
                && pos.getY() >= min.getY() && pos.getY() <= max.getY()
                && pos.getZ() >= min.getZ() && pos.getZ() <= max.getZ();
    }

    @Override
    public final BlockState getBlockState(Level level, BlockPos pos) {
        return getBlockState(pos);
    }

    @Nullable
    @Override
    public final BlockEntity getBlockEntity(Level level, BlockPos pos) {
        return getBlockEntity(pos);
    }

    @Override
    public final Set<Long> getStructurePositions() {
        BlockPos min = publishedMin;
        BlockPos max = publishedMax;
        if (min == null || max == null) return publishedChecked;
        Set<Long> positions = new HashSet<>();
        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int y = min.getY(); y <= max.getY(); y++) {
                for (int z = min.getZ(); z <= max.getZ(); z++) {
                    positions.add(BlockPos.asLong(x, y, z));
                }
            }
        }
        return positions;
    }

    @Override
    public final boolean hasAABB() {
        return publishedMin != null && publishedMax != null;
    }

    @Override
    public final long aabbMinPacked() {
        BlockPos min = publishedMin;
        return min == null ? 0 : min.asLong();
    }

    @Override
    public final long aabbMaxPacked() {
        BlockPos max = publishedMax;
        return max == null ? 0 : max.asLong();
    }

    @Override
    public final void clear() {
        refreshInputs();
        resetValidationData();
        publishedMin = null;
        publishedMax = null;
        publishedPorts = Set.of();
        publishedChecked = Set.of();
    }

    @Override
    public final void saveNbt(CompoundTag tag, HolderLookup.Provider registries) {
        BlockPos min = publishedMin;
        BlockPos max = publishedMax;
        if (min != null && max != null) {
            tag.putLong("bounds_min", min.asLong());
            tag.putLong("bounds_max", max.asLong());
        }
        Set<Long> ports = publishedPorts;
        long[] packedPorts = new long[ports.size()];
        int index = 0;
        for (long port : ports) packedPorts[index++] = port;
        tag.putLongArray("ports", packedPorts);
        saveData(tag);
    }

    @Override
    public final void loadNbt(CompoundTag tag, HolderLookup.Provider registries) {
        if (tag.contains("bounds_min") && tag.contains("bounds_max")) {
            publishedMin = BlockPos.of(tag.getLong("bounds_min"));
            publishedMax = BlockPos.of(tag.getLong("bounds_max"));
        }
        Set<Long> ports = new HashSet<>();
        for (long port : tag.getLongArray("ports")) ports.add(port);
        publishedPorts = Set.copyOf(ports);
        loadData(tag);
    }
}
