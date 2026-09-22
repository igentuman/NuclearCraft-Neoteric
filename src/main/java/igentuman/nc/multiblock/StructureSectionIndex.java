package igentuman.nc.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class StructureSectionIndex {

    private final Map<Long, Set<UUID>> ownedCandidates;
    private final Map<Long, Set<UUID>> watchedCandidates;

    public StructureSectionIndex() {
        this(Map.of(), Map.of());
    }

    public StructureSectionIndex(Map<Long, Set<UUID>> ownedCandidates, Map<Long, Set<UUID>> watchedCandidates) {
        this.ownedCandidates = mutableCopy(ownedCandidates);
        this.watchedCandidates = mutableCopy(watchedCandidates);
    }

    public Map<Long, Set<UUID>> ownedCandidates() {
        return immutableCopy(ownedCandidates);
    }

    public Map<Long, Set<UUID>> watchedCandidates() {
        return immutableCopy(watchedCandidates);
    }

    public Set<UUID> ownedCandidates(long section) {
        return immutableSet(ownedCandidates.get(section));
    }

    public Set<UUID> watchedCandidates(long section) {
        return immutableSet(watchedCandidates.get(section));
    }

    public Optional<UUID> overlappingOwner(StructureRecord candidate, Map<UUID, StructureRecord> structures) {
        Set<UUID> possible = new HashSet<>();
        for (long section : candidate.ownedSections()) {
            for (UUID id : ownedCandidates.getOrDefault(section, Set.of())) {
                StructureRecord existing = structures.get(id);
                if (existing != null && !id.equals(candidate.id()) && ownsSpace(existing)) possible.add(id);
            }
        }
        if (possible.isEmpty()) return Optional.empty();
        var cursor = candidate.footprint().cursor();
        while (cursor.hasNext()) {
            BlockPos pos = cursor.next().pos();
            for (UUID id : ownedCandidates.getOrDefault(sectionKey(pos), Set.of())) {
                StructureRecord existing = structures.get(id);
                if (!possible.contains(id) || existing == null) continue;
                if (existing.footprint().contains(pos)) return Optional.of(id);
            }
        }
        return Optional.empty();
    }

    private static boolean ownsSpace(StructureRecord record) {
        return record.state() != StructureLifecycleState.UNFORMED
                && record.state() != StructureLifecycleState.BREAKING
                && record.state() != StructureLifecycleState.DISPOSED;
    }

    public void index(StructureRecord record) {
        add(ownedCandidates, record.ownedSections(), record.id());
        add(watchedCandidates, record.ownedSections(), record.id());
    }

    public void unindex(StructureRecord record) {
        remove(ownedCandidates, record.ownedSections(), record.id());
        remove(watchedCandidates, record.ownedSections(), record.id());
    }

    public void unindex(UUID structureId) {
        removeEverywhere(ownedCandidates, structureId);
        removeEverywhere(watchedCandidates, structureId);
    }

    public Set<UUID> affected(BlockPos position, Map<UUID, StructureRecord> structures) {
        Set<UUID> candidates = watchedCandidates.get(sectionKey(position));
        if (candidates == null) return Set.of();
        Set<UUID> result = null;
        for (UUID id : candidates) {
            StructureRecord record = structures.get(id);
            if (record != null && record.footprint().contains(position)) {
                if (result == null) result = new HashSet<>();
                result.add(id);
            }
        }
        return result == null ? Set.of() : result;
    }

    public boolean hasTrackedSection(long section) {
        return watchedCandidates.containsKey(section);
    }

    public boolean contains(BlockPos position, Map<UUID, StructureRecord> structures) {
        Set<UUID> candidates = watchedCandidates.get(sectionKey(position));
        if (candidates == null) return false;
        for (UUID id : candidates) {
            StructureRecord record = structures.get(id);
            if (record != null && record.footprint().contains(position)) return true;
        }
        return false;
    }

    public void clear() {
        ownedCandidates.clear();
        watchedCandidates.clear();
    }

    public static long sectionKey(BlockPos pos) {
        return SectionPos.asLong(
                SectionPos.blockToSectionCoord(pos.getX()),
                SectionPos.blockToSectionCoord(pos.getY()),
                SectionPos.blockToSectionCoord(pos.getZ()));
    }

    private static Map<Long, Set<UUID>> mutableCopy(Map<Long, Set<UUID>> source) {
        Map<Long, Set<UUID>> copy = new HashMap<>();
        if (source != null) source.forEach((key, ids) -> copy.put(key, new HashSet<>(ids)));
        return copy;
    }

    private static Map<Long, Set<UUID>> immutableCopy(Map<Long, Set<UUID>> source) {
        Map<Long, Set<UUID>> copy = new HashMap<>();
        source.forEach((key, ids) -> copy.put(key, Set.copyOf(ids)));
        return Collections.unmodifiableMap(copy);
    }

    private static Set<UUID> immutableSet(Set<UUID> values) {
        return values == null ? Set.of() : Set.copyOf(values);
    }

    private static void add(Map<Long, Set<UUID>> index, Set<Long> sections, UUID id) {
        for (long section : sections) index.computeIfAbsent(section, ignored -> new HashSet<>()).add(id);
    }

    private static void remove(Map<Long, Set<UUID>> index, Set<Long> sections, UUID id) {
        for (long section : sections) {
            Set<UUID> candidates = index.get(section);
            if (candidates == null) continue;
            candidates.remove(id);
            if (candidates.isEmpty()) index.remove(section);
        }
    }

    private static void removeEverywhere(Map<Long, Set<UUID>> index, UUID id) {
        index.values().removeIf(ids -> {
            ids.remove(id);
            return ids.isEmpty();
        });
    }
}
