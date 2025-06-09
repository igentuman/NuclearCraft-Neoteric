package igentuman.nc.radiation.data;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.HashMap;

import static igentuman.nc.handler.config.RadiationConfig.RADIATION_CONFIG;

public class WorldRadiation implements IWorldRadiationCapability {

    private final double decaySpeed = ((double) RADIATION_CONFIG.DECAY_SPEED.get())/10000;
    public HashMap<Long, Long> chunkRadiation = new HashMap<>();
    public HashMap<Long, Long> updatedChunks = new HashMap<>();
    public HashMap<Long, Long> newChunks = new HashMap<>();
    public Level level;

    public WorldRadiation() {
    }

    public static WorldRadiation deserialize(CompoundTag radiation) {
        WorldRadiation worldRadiation = new WorldRadiation();
        worldRadiation.deserializeNBT(radiation);
        return worldRadiation;
    }

    public int chunkRadiation(int chunkX, int chunkZ) {
        int radiation = 0;
        long id = pack(chunkX, chunkZ);
        if (chunkRadiation.containsKey(id)) {
            radiation += unpackX(chunkRadiation.get(id));
        }
        return radiation;
    }

    @Override
    public int getChunkRadiation(int chunkX, int chunkZ) {
        long id = pack(chunkX, chunkZ);
        int radiation = naturalRadiation(chunkX, chunkZ);
        if (chunkRadiation.containsKey(id)) {
            radiation += unpackX(chunkRadiation.get(id));
        }

        radiation += iterateNearbyChunks(chunkX, chunkZ);

        return radiation;
    }

    private int iterateNearbyChunks(int chunkX, int chunkZ) {
        int radius = 7;
        int radiation = 0;
        for (int x = chunkX - radius; x <= chunkX + radius; x++) {
            for (int z = chunkZ - radius; z <= chunkZ + radius; z++) {
                if (x == chunkX && z == chunkZ) {
                    continue;
                }
                long id = pack(x, z);
                int chunkRadiationValue = 0;
                if (chunkRadiation.containsKey(id)) {
                    chunkRadiationValue = unpackX(chunkRadiation.get(id));
                }
                double distance = Math.log(Math.pow(chunkX - x, 2) + Math.pow(chunkZ - z, 2) + 2D);
                double multiplier = 1.0 / Math.max(1.0, distance);
                radiation += (int)((chunkRadiationValue + (double) naturalRadiation(x, z) / 5) * multiplier);
            }
        }
        return radiation;
    }

    public void refresh(Level level)
    {
        this.level = level;
        chunkRadiation.putAll(newChunks);
        updatedChunks.clear();
        newChunks.clear();
        Long[] ids = chunkRadiation.keySet().toArray(new Long[0]);
        for(long id: ids)
        {
            updateChunkRadiation(id);
        }
    }

    @Override
    public void updateChunkRadiation(long id) {
        if (updatedChunks.containsKey(id) || !chunkRadiation.containsKey(id)) {
            return;
        }
        int x = unpackX(id);
        int z = unpackZ(id);
        if (!level.getChunkSource().hasChunk(x, z)) {
            return;//do not recalculate unloaded chunks
        }
        int radiation = unpackX(chunkRadiation.get(id));
        int timestamp = unpackZ(chunkRadiation.get(id));
        int curTimestamp = (int) (getServerTime() / 20);
        int radiationChange = (int) ((curTimestamp - timestamp) * decaySpeed);
        radiation -= radiationChange;
        if (radiation < 100) {
            chunkRadiation.remove(id);
            updatedChunks.put(id, pack(0, curTimestamp));
            return;
        }
        radiation = Math.min(radiation, 5000000);
        long radiationData = pack(radiation, curTimestamp);
        chunkRadiation.replace(id, radiationData);
        updatedChunks.put(id, radiationData);
    }

    //returns amount of actually added radiation in mRads
    public int addRadiation(Level level, double radiation, int x, int z)
    {
        this.level = level;
        long id = pack(x, z);
        int curTimestamp = (int) (getServerTime() / 20);
        int newRadiation = (int) (radiation*1000000);
        if(!RADIATION_CONFIG.ENABLED.get()) return 0;
        if(chunkRadiation.containsKey(id)) {
            int curRadiation = unpackX(chunkRadiation.get(id));
            if(curRadiation > newRadiation) {
                newRadiation = newRadiation/10;
            }
            newRadiation = curRadiation + newRadiation;
            chunkRadiation.replace(id, pack(newRadiation, curTimestamp));
        }

        if(newChunks.containsKey(id)) {
            newChunks.replace(id, pack(newRadiation, curTimestamp));
        } else {
            newChunks.put(id, pack(newRadiation, curTimestamp));
        }
        return newRadiation;
    }

    private long getServerTime() {
        return level.getGameTime();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        CompoundTag radiationTag = new CompoundTag();
        for(long key : chunkRadiation.keySet()) {
            radiationTag.putLong(String.valueOf(key), chunkRadiation.get(key));
        }
        tag.put("radiation", radiationTag);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        CompoundTag radiationTag = nbt.getCompound("radiation");
        for(String key : radiationTag.getAllKeys()) {
            chunkRadiation.put(Long.parseLong(key), radiationTag.getLong(key));
        }
    }

    public static long pack(int x, int z) {
        return ChunkPos.asLong(x, z);
    }

    public static int unpackX(long packed) {
        return ChunkPos.getX(packed);
    }

    public static int unpackZ(long packed) {
        return ChunkPos.getZ(packed);
    }

    public int naturalRadiation(int chunkX, int chunkZ) {
        int radiation = RADIATION_CONFIG.NATURAL_RADIATION.get();
        if(level == null) {
            return radiation;
        }
        String biomeId = level.getBiome(new BlockPos(chunkX*16, 0, chunkZ*16))
                .unwrapKey().get().location().toString();
        radiation += RADIATION_CONFIG.biomeRadiation(biomeId);
        radiation += RADIATION_CONFIG.dimensionRadiation(level.dimension().location().toString());
        return radiation;
    }
}
