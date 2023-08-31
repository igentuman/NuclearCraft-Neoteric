package igentuman.nc.util.insitu_leaching;

import igentuman.nc.handler.OreVeinProvider;
import igentuman.nc.recipes.type.OreVeinRecipe;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;

import java.util.HashMap;


public class WorldVeinOres implements IWorldVeinCapability {

    public ServerLevel level;
    public HashMap<Long, Integer> chunkVeins = new HashMap<>();

    public WorldVeinOres() {
    }

    public static WorldVeinOres deserialize(CompoundTag veins) {
        WorldVeinOres worldVeins = new WorldVeinOres();
        worldVeins.deserializeNBT(veins);
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
        if (chunkVeins.containsKey(id)) {
            return chunkVeins.get(id);
        }
        return OreVeinProvider.get(level).getVeinSize(chunkX, chunkZ);
    }

    @Override
    public void mineBlock(int chunkX, int chunkZ) {
        if(!chunkContainsVein(chunkX, chunkZ)) {
            return;
        }
        long id = ChunkPos.asLong(chunkX, chunkZ);
        if (chunkVeins.containsKey(id)) {
            chunkVeins.put(id, chunkVeins.get(id)-1);
        } else {
            chunkVeins.put(id, OreVeinProvider.get(level).getVeinSize(chunkX, chunkZ)-1);
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        CompoundTag veinsTag = new CompoundTag();
        for(long key : chunkVeins.keySet()) {
            veinsTag.putInt(String.valueOf(key), chunkVeins.get(key));
        }
        tag.put("depletion", veinsTag);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        CompoundTag veinsTag = nbt.getCompound("depletion");
        for(String key : veinsTag.getAllKeys()) {
            chunkVeins.put(Long.parseLong(key), veinsTag.getInt(key));
        }
    }

    public ItemStack gatherRandomOre(int x, int z) {
        OreVeinRecipe vein = getVeinForChunk(x, z);
        if(vein == null) {
            return ItemStack.EMPTY;
        }
        return vein.getRandomOre(level, x, z, getBlocksLeft(x, z));
    }
}
