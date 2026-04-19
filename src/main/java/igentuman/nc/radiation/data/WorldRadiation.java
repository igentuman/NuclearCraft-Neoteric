package igentuman.nc.radiation.data;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static igentuman.nc.NuclearCraft.currentTick;
import static igentuman.nc.handler.config.RadiationConfig.RADIATION_CONFIG;

public class WorldRadiation {

    private final double decaySpeed = ((double) RADIATION_CONFIG.DECAY_SPEED.get())/10000;
    public Map<Long, Long> chunkRadiation = new ConcurrentHashMap<>();
    public Map<Long, Long> updatedChunks = new ConcurrentHashMap<>();
    public Map<Long, Long> newChunks = new ConcurrentHashMap<>();
    public Level level;

    public WorldRadiation() {
    }

    public WorldRadiation(Map<Long, Long> radiation) {
        this.chunkRadiation = new ConcurrentHashMap<>(radiation);
    }

    public static WorldRadiation deserialize(CompoundTag radiation) {
        WorldRadiation worldRadiation = new WorldRadiation();
        worldRadiation.deserializeNBT(radiation);
        return worldRadiation;
    }

    public int chunkRadiation(int chunkX, int chunkZ) {
        int radiation = 0;
        long id = packChunkPos(chunkX, chunkZ);
        if (chunkRadiation.containsKey(id)) {
            radiation += unpackRadiation(chunkRadiation.get(id));
        }
        return radiation;
    }

    public int getChunkRadiation(int chunkX, int chunkZ) {
        long id = packChunkPos(chunkX, chunkZ);
        int radiation = naturalRadiation(chunkX, chunkZ);
        if (chunkRadiation.containsKey(id)) {
            radiation += unpackRadiation(chunkRadiation.get(id));
        }
        return radiation;
    }

    public void refresh(Level level)
    {
        this.level = level;
        chunkRadiation.putAll(newChunks);
        updatedChunks.clear();
        newChunks.clear();

        diffuseRadiation();

        Long[] ids = chunkRadiation.keySet().toArray(new Long[0]);
        for(long id: ids)
        {
            updateChunkRadiation(id);
        }
    }

    private void diffuseRadiation() {
        if (chunkRadiation.isEmpty()) return;

        HashMap<Long, Integer> bleedMap = new HashMap<>();
        double bleedRate = 0.25; // 25% bleeds to neighbors

        for (long id : chunkRadiation.keySet()) {
            long packedData = chunkRadiation.get(id);
            int radiation = unpackRadiation(packedData);
            if (radiation < 1000) continue; // Too small to diffuse

            int bleedAmount = (int) (radiation * bleedRate);
            int perNeighbor = bleedAmount / 8;
            if (perNeighbor <= 0) continue;

            int x = unpackX(id);
            int z = unpackZ(id);

            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dz == 0) continue;
                    long neighborId = packChunkPos(x + dx, z + dz);
                    bleedMap.put(neighborId, bleedMap.getOrDefault(neighborId, 0) + perNeighbor);
                }
            }

            // Deduct bleed amount from source
            int newRadiation = radiation - bleedAmount;
            chunkRadiation.replace(id, packData(newRadiation, unpackTimestamp(packedData)));
            updatedChunks.put(id, chunkRadiation.get(id));
        }

        // Apply bleedMap to chunkRadiation
        int curTimestamp = (int) (getServerTime() / 20);
        for (long id : bleedMap.keySet()) {
            int bleed = bleedMap.get(id);
            if (chunkRadiation.containsKey(id)) {
                long packedData = chunkRadiation.get(id);
                int radiation = unpackRadiation(packedData);
                chunkRadiation.replace(id, packData(radiation + bleed, unpackTimestamp(packedData)));
            } else {
                chunkRadiation.put(id, packData(bleed, curTimestamp));
            }
            updatedChunks.put(id, chunkRadiation.get(id));
        }
    }

    public void updateChunkRadiation(long id) {
        if (updatedChunks.containsKey(id) || !chunkRadiation.containsKey(id)) {
            return;
        }
        int x = unpackX(id);
        int z = unpackZ(id);
        if (!level.getChunkSource().hasChunk(x, z)) {
            return;//do not recalculate unloaded chunks
        }
        long packedData = chunkRadiation.get(id);
        int radiation = unpackRadiation(packedData);
        int timestamp = unpackTimestamp(packedData);
        int curTimestamp = (int) (getServerTime() / 20);
        
        double secondsPassed = curTimestamp - timestamp;
        if (secondsPassed > 0) {
            int radiationChange = (int) (secondsPassed * decaySpeed);
            if (radiationChange > 0) {
                radiation -= radiationChange;
                timestamp = curTimestamp;
            }
        }

        if (radiation < 100) {
            chunkRadiation.remove(id);
            updatedChunks.put(id, packData(0, curTimestamp));
            return;
        }
        radiation = Math.min(radiation, Integer.MAX_VALUE);
        long radiationData = packData(radiation, timestamp);
        chunkRadiation.replace(id, radiationData);
        updatedChunks.put(id, radiationData);
    }

    //returns amount of actually added radiation in mRads
    public int addRadiation(Level level, double radiation, int x, int z)
    {
        this.level = level;
        long id = packChunkPos(x, z);
        int curTimestamp = (int) (getServerTime() / 20);
        int newRadiationAmount = (int) Math.min(radiation * 1000000.0, Integer.MAX_VALUE);
        if(!RADIATION_CONFIG.ENABLED.get()) return 0;
        
        int curRadiation = 0;
        if(chunkRadiation.containsKey(id)) {
            long packedData = chunkRadiation.get(id);
            curRadiation = unpackRadiation(packedData);
            double secondsPassed = curTimestamp - unpackTimestamp(packedData);
            if (secondsPassed > 0) {
                curRadiation -= (int) (secondsPassed * decaySpeed);
            }
        }
        
        int totalRadiation = (int) Math.max(0, Math.min((long)curRadiation + newRadiationAmount, Integer.MAX_VALUE));
        long radiationData = packData(totalRadiation, curTimestamp);
        
        chunkRadiation.put(id, radiationData);
        if(newChunks.containsKey(id)) {
            newChunks.replace(id, radiationData);
        } else {
            newChunks.put(id, radiationData);
        }
        return totalRadiation;
    }

    private long getServerTime() {
        return level.getGameTime();
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        CompoundTag radiationTag = new CompoundTag();
        for(long key : chunkRadiation.keySet()) {
            radiationTag.putLong(String.valueOf(key), chunkRadiation.get(key));
        }
        tag.put("radiation", radiationTag);
        return tag;
    }

    public void deserializeNBT(CompoundTag nbt) {
        CompoundTag radiationTag = nbt.getCompound("radiation");
        for(String key : radiationTag.getAllKeys()) {
            chunkRadiation.put(Long.parseLong(key), radiationTag.getLong(key));
        }
    }

    public static long packChunkPos(int x, int z) {
        return ChunkPos.asLong(x, z);
    }

    public static int unpackX(long packed) {
        return ChunkPos.getX(packed);
    }

    public static int unpackZ(long packed) {
        return ChunkPos.getZ(packed);
    }

    public static long packData(int radiation, int timestamp) {
        return ((long)radiation << 32) | (timestamp & 0xFFFFFFFFL);
    }

    public static int unpackRadiation(long packed) {
        return (int)(packed >> 32);
    }

    public static int unpackTimestamp(long packed) {
        return (int)packed;
    }

    public int naturalRadiation(int chunkX, int chunkZ) {
        int radiation = RADIATION_CONFIG.NATURAL_RADIATION.get();
        if(level == null) {
            return radiation;
        }
        Holder<Biome> biome = level.getBiomeManager().getNoiseBiomeAtPosition(chunkX*16, 0, chunkZ*16);

        String biomeId = biome.unwrapKey().get().location().toString();
        radiation += RADIATION_CONFIG.biomeRadiation(biomeId);
        radiation += RADIATION_CONFIG.dimensionRadiation(level.dimension().location().toString());
        return radiation;
    }

    public void setChunkRadiation(BlockPos blockPos, int value) {
        long id = packChunkPos(blockPos.getX() >> 4, blockPos.getZ() >> 4);
        int curTimestamp = (int) (getServerTime() / 20);
        int newRadiation = Math.min(value*1000, Integer.MAX_VALUE);
        chunkRadiation.put(id, packData(newRadiation, curTimestamp));
        updatedChunks.put(id, packData(newRadiation, curTimestamp));
    }
}
