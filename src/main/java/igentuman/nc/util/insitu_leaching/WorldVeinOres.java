package igentuman.nc.util.insitu_leaching;

import igentuman.nc.recipes.type.OreVeinRecipe;
import igentuman.api.platform.NCSerialization;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.HashMap;


public class WorldVeinOres implements IWorldVeinCapability {

    public ServerLevel level;
    public HashMap<Long, Integer> blocksInVein = new HashMap<>();

    public WorldVeinOres() {
    }

    public WorldVeinOres(Level level) {
        this.level = (ServerLevel) level;
    }

    public static WorldVeinOres deserialize(HolderLookup.Provider provider, CompoundTag veins) {
        WorldVeinOres worldVeins = new WorldVeinOres();
        NCSerialization.deserialize(worldVeins, provider, veins);
        return worldVeins;
    }

    @Override
    public OreVeinRecipe getVeinForChunk(int chunkX, int chunkZ) {
        return OreVeinProvider.get(level).getVeinForChunk(chunkX, chunkZ);
    }

    @Override
    public boolean chunkContainsVein(int chunkX, int chunkZ) {
        return OreVeinProvider.get(level).chunkContainsVein(chunkX, chunkZ);
    }

    @Override
    public int getBlocksLeft(int chunkX, int chunkZ) {
        long id = ChunkPos.asLong(chunkX, chunkZ);
        if (blocksInVein.containsKey(id)) {
            return blocksInVein.get(id);
        }
        return OreVeinProvider.get(level).getVeinSize(chunkX, chunkZ);
    }

    @Override
    public void mineBlock(int chunkX, int chunkZ) {
        if(!chunkContainsVein(chunkX, chunkZ)) {
            return;
        }
        long id = ChunkPos.asLong(chunkX, chunkZ);
        if (blocksInVein.containsKey(id)) {
            blocksInVein.replace(id, blocksInVein.get(id)-1);
        } else {
            blocksInVein.put(id, OreVeinProvider.get(level).getVeinSize(chunkX, chunkZ)-1);
        }
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        CompoundTag veinsTag = new CompoundTag();
        for(long key : blocksInVein.keySet()) {
            veinsTag.putInt(String.valueOf(key), blocksInVein.get(key));
        }
        tag.put("depletion", veinsTag);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        CompoundTag veinsTag = nbt.getCompound("depletion");
        for(String key : veinsTag.getAllKeys()) {
            blocksInVein.put(Long.parseLong(key), veinsTag.getInt(key));
        }
    }

    public ItemStack gatherRandomOre(int x, int z) {
        if (getBlocksLeft(x, z) == 0) return ItemStack.EMPTY;
        OreVeinRecipe vein = getVeinForChunk(x, z);
        if(vein == null) {
            return ItemStack.EMPTY;
        }
        ItemStack ore = vein.getRandomOre(level, x, z, getBlocksLeft(x, z));
        if (!ore.isEmpty()) {
            mineBlock(x, z);
            WorldVeinsManager.get(level).setDirty();
        }
        return ore;
    }
}
