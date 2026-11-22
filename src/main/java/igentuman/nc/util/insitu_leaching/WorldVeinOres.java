package igentuman.nc.util.insitu_leaching;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import igentuman.nc.recipe.OreVeinRecipe;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/** Tracks per-chunk vein depletion as a map of chunk pos long to remaining blocks, persisted via WorldVeinsManager. */
public class WorldVeinOres {

    private static final Codec<Long> CHUNK_KEY_CODEC = Codec.STRING.comapFlatMap(key -> {
        try {
            return DataResult.success(Long.parseLong(key));
        } catch (NumberFormatException error) {
            return DataResult.error(() -> "Invalid vein chunk key: " + key);
        }
    }, Object::toString);
    public static final Codec<WorldVeinOres> CODEC = Codec.unboundedMap(CHUNK_KEY_CODEC, Codec.intRange(0, Integer.MAX_VALUE))
            .xmap(entries -> {
                WorldVeinOres ores = new WorldVeinOres();
                ores.blocksInVein.putAll(entries);
                return ores;
            }, ores -> ores.blocksInVein).fieldOf("depletion").codec();

    public ServerLevel level;
    private final Map<Long, Integer> blocksInVein = new HashMap<>();
    private final Map<Long, DepletionJournal> journals = new HashMap<>();

    public WorldVeinOres() {
    }

    public WorldVeinOres(ServerLevel level) {
        this.level = level;
    }

    public OreVeinRecipe getVeinForChunk(int chunkX, int chunkZ) {
        return OreVeinProvider.get(level).getVeinForChunk(chunkX, chunkZ);
    }

    public boolean chunkContainsVein(int chunkX, int chunkZ) {
        return OreVeinProvider.get(level).chunkContainsVein(chunkX, chunkZ);
    }

    public int getBlocksLeft(int chunkX, int chunkZ) {
        long id = ChunkPos.pack(chunkX, chunkZ);
        if (blocksInVein.containsKey(id)) {
            return blocksInVein.get(id);
        }
        return OreVeinProvider.get(level).getVeinSize(chunkX, chunkZ);
    }

    /** Reserves a random ore and its vein depletion in the caller's inventory transaction. */
    public ItemStack gatherRandomOre(int x, int z, TransactionContext transaction) {
        if (level == null) return ItemStack.EMPTY;
        int remaining = getBlocksLeft(x, z);
        if (remaining <= 0) return ItemStack.EMPTY;
        OreVeinRecipe vein = getVeinForChunk(x, z);
        if (vein == null) return ItemStack.EMPTY;
        ItemStack ore = vein.getRandomOre(level, x, z, remaining);
        if (!ore.isEmpty()) {
            long id = ChunkPos.pack(x, z);
            journals.computeIfAbsent(id, DepletionJournal::new).updateSnapshots(transaction);
            blocksInVein.put(id, remaining - 1);
        }
        return ore;
    }

    /** Snapshots only the affected chunk, retaining an absent entry when a reservation aborts. */
    private final class DepletionJournal extends SnapshotJournal<@Nullable Integer> {
        private final long chunk;

        private DepletionJournal(long chunk) { this.chunk = chunk; }

        @Override
        protected @Nullable Integer createSnapshot() { return blocksInVein.get(chunk); }

        @Override
        protected void revertToSnapshot(@Nullable Integer snapshot) {
            if (snapshot == null) blocksInVein.remove(chunk);
            else blocksInVein.put(chunk, snapshot);
        }

        @Override
        protected void onRootCommit(@Nullable Integer original) {
            if (!Objects.equals(original, blocksInVein.get(chunk))) WorldVeinsManager.get(level).setDirty();
        }
    }
}
