package igentuman.nc.util.insitu_leaching;

import igentuman.nc.recipe.OreVeinRecipe;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;

import java.util.HashMap;
import java.util.Map;

/** Tracks per-chunk vein depletion as a map of chunk pos long to remaining blocks, persisted via WorldVeinsManager. */
public class WorldVeinOres {

    public ServerLevel level;
    public final Map<Long, Integer> blocksInVein = new HashMap<>();

    public WorldVeinOres() {
    }

    public WorldVeinOres(ServerLevel level) {
        this.level = level;
    }

    public static WorldVeinOres deserialize(CompoundTag tag) {
        WorldVeinOres worldVeins = new WorldVeinOres();
        worldVeins.deserializeNBT(tag);
        return worldVeins;
    }

    public OreVeinRecipe getVeinForChunk(int chunkX, int chunkZ) {
        return OreVeinProvider.get(level).getVeinForChunk(chunkX, chunkZ);
    }

    public boolean chunkContainsVein(int chunkX, int chunkZ) {
        return OreVeinProvider.get(level).chunkContainsVein(chunkX, chunkZ);
    }

    public int getBlocksLeft(int chunkX, int chunkZ) {
        long id = ChunkPos.asLong(chunkX, chunkZ);
        if (blocksInVein.containsKey(id)) {
            return blocksInVein.get(id);
        }
        return OreVeinProvider.get(level).getVeinSize(chunkX, chunkZ);
    }

    public void mineBlock(int chunkX, int chunkZ) {
        if (!chunkContainsVein(chunkX, chunkZ)) {
            return;
        }
        long id = ChunkPos.asLong(chunkX, chunkZ);
        if (blocksInVein.containsKey(id)) {
            blocksInVein.replace(id, blocksInVein.get(id) - 1);
        } else {
            blocksInVein.put(id, OreVeinProvider.get(level).getVeinSize(chunkX, chunkZ) - 1);
        }
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        CompoundTag veinsTag = new CompoundTag();
        for (long key : blocksInVein.keySet()) {
            veinsTag.putInt(String.valueOf(key), blocksInVein.get(key));
        }
        tag.put("depletion", veinsTag);
        return tag;
    }

    public void deserializeNBT(CompoundTag nbt) {
        CompoundTag veinsTag = nbt.getCompound("depletion");
        for (String key : veinsTag.getAllKeys()) {
            blocksInVein.put(Long.parseLong(key), veinsTag.getInt(key));
        }
    }

    /** Gathers a random ore from the chunk's virtual vein, decrementing the depletion counter. */
    public ItemStack gatherRandomOre(int x, int z) {
        if (level == null) return ItemStack.EMPTY;
        if (getBlocksLeft(x, z) <= 0) return ItemStack.EMPTY;
        OreVeinRecipe vein = getVeinForChunk(x, z);
        if (vein == null) return ItemStack.EMPTY;
        ItemStack ore = vein.getRandomOre(level, x, z, getBlocksLeft(x, z));
        if (!ore.isEmpty()) {
            mineBlock(x, z);
            WorldVeinsManager.get(level).setDirty();
        }
        return ore;
    }
}
