package igentuman.nc.radiation.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import java.util.HashMap;

import static igentuman.nc.handler.config.CommonConfig.RADIATION_CONFIG;

public class WorldRadiation implements IWorldRadiationCapability {

    private final double decaySpeed = RADIATION_CONFIG.DECAY_SPEED.get();
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

    @Override
    public int getChunkRadiation(int chunkX, int chunkZ) {
        long id = pack(chunkX, chunkZ);
        if (chunkRadiation.containsKey(id)) {
            int radiation = unpackX(chunkRadiation.get(id));
            return radiation;
        }
        return naturalRadiation(chunkX, chunkZ);
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
    public void updateChunkRadiation(int x, int z)
    {
        long id = pack(x, z);
        updateChunkRadiation(id);
    }

    @Override
    public void updateChunkRadiation(long id) {
        if (updatedChunks.containsKey(id) || !chunkRadiation.containsKey(id)) {
            return;
        }
        int x = unpackX(id);
        int z = unpackY(id);
        if (!level.getChunkSource().hasChunk(x, z)) {
            return;//do not recalculate unloaded chunks
        }
        int radiation = unpackX(chunkRadiation.get(id));
        int timestamp = unpackY(chunkRadiation.get(id));
        int curTimestamp = (int) (getServerTime() / 20);
        radiation -= (int) ((curTimestamp - timestamp) * decaySpeed);
        //if radiation is less than 10, remove it from the map
        if (radiation < 10) {
            chunkRadiation.remove(id);//still sending 0 radiation to client so he will remove it from the map
            updatedChunks.put(id, pack(0, curTimestamp));
            return;
        }
        long radiationData = pack(radiation, curTimestamp);
        chunkRadiation.replace(id, radiationData);
        updatedChunks.put(id, radiationData);
        //spread radiation around only if it is greater than 5 mRad
        if (radiation > RADIATION_CONFIG.SPREAD_MULTIPLIER.get()) {
            spreadAround(x, z, radiation);
        }
    }

    public void addRadiation(Level level, double radiation, int x, int z)
    {
        this.level = level;
        long id = pack(x, z);
        int curTimestamp = (int) (getServerTime() / 20);
        int newRadiation = (int) (radiation*1000000);

        if(chunkRadiation.containsKey(id)) {
            int curRadiation = unpackX(chunkRadiation.get(id));
            newRadiation = curRadiation + newRadiation;
            chunkRadiation.replace(id, pack(newRadiation, curTimestamp));
        }

        if(newChunks.containsKey(id)) {
            newChunks.replace(id, pack(newRadiation, curTimestamp));
        } else {
            newChunks.put(id, pack(newRadiation, curTimestamp));
        }
    }

    private long getServerTime() {
        return level.getGameTime();
    }


    private void spreadAround(int chunkX, int chunkZ, int radiation) {
        for(int i = -1; i <= 1; i++) {
            for(int j = -1; j <= 1; j++) {
                long id = pack(chunkX + i, chunkZ + j);
                if(updatedChunks.containsKey(id)) {
                    continue;
                }
                if(chunkRadiation.containsKey(id)) {
                    int curRadiation = unpackX(chunkRadiation.get(id));
                    int curTimestamp = unpackY(chunkRadiation.get(id));
                    if(curRadiation > radiation*RADIATION_CONFIG.SPREAD_MULTIPLIER.get()) {
                        continue;
                    }
                    //if chunk already have radiation we use average value
                    int newRadiation = (int) ((curRadiation + radiation * RADIATION_CONFIG.SPREAD_MULTIPLIER.get())/2);
                    chunkRadiation.replace(id, pack(newRadiation, curTimestamp));
                }
                else {
                    int curTimestamp = (int) (getServerTime() / 20);
                    chunkRadiation.put(id, pack((int) (radiation * RADIATION_CONFIG.SPREAD_MULTIPLIER.get()), curTimestamp));
                }
                if(updatedChunks.containsKey(id)) {
                    updatedChunks.replace(id, chunkRadiation.get(id));
                } else {
                    updatedChunks.put(id, chunkRadiation.get(id));
                }
            }
        }
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

    public static long pack(int x, int y) {
        return ((long) x << 32) | (y & 0xFFFFFFFFL);
    }

    public static int unpackX(long packed) {
        return (int) (packed >> 32);
    }

    public static int unpackY(long packed) {
        return (int) (packed & 0xFFFFFFFFL);
    }

    public int naturalRadiation(int chunkX, int chunkZ) {
        return 0;
    }
}
