package igentuman.nc.radiation.data;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.HashMap;

import static igentuman.nc.handler.config.RadiationConfig.RADIATION_CONFIG;

public class WorldRadiation implements IWorldRadiationCapability {

    private final double decaySpeed = ((double) RADIATION_CONFIG.DECAY_SPEED.get())/10000;
    private final double spreadGate = RADIATION_CONFIG.SPREAD_GATE.get();
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
        int radiation = naturalRadiation(chunkX, chunkZ);
        if (chunkRadiation.containsKey(id)) {
            radiation += unpackX(chunkRadiation.get(id));
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
        int z = unpackZ(id);
        if (!level.getChunkSource().hasChunk(x, z)) {
            return;//do not recalculate unloaded chunks
        }
        int radiation = unpackX(chunkRadiation.get(id));
        int timestamp = unpackZ(chunkRadiation.get(id));
        int curTimestamp = (int) (getServerTime() / 20);
        int radiationChange = (int) ((curTimestamp - timestamp) * decaySpeed);
        //if(radiationChange == 0) return;
        radiation -= radiationChange;
        //if radiation is less than 100, remove it from the map
        if (radiation < 100) {
            chunkRadiation.remove(id);//still sending 0 radiation to client, so he will remove it from the map
            updatedChunks.put(id, pack(0, curTimestamp));
            return;
        }
        radiation = Math.min(radiation, 5000000);
        boolean toSpread = radiation > spreadGate;
        if(toSpread) {//if it spreads, then it looses
            radiation = (int)(0.99 * radiation);
        }
        long radiationData = pack(radiation, curTimestamp);
        chunkRadiation.replace(id, radiationData);
        updatedChunks.put(id, radiationData);
        //spread radiation around only if it is greater than 5 mRad
        if (toSpread) {
            spreadAround(x, z, radiation);
        }
    }

    //returns amount of actually added radiation in mRads
    public int addRadiation(Level level, double radiation, int x, int z)
    {
        this.level = level;
        long id = pack(x, z);
        int curTimestamp = (int) (getServerTime() / 20);
        int newRadiation = (int) (radiation*1000000);
        //if radiation is disabled we still run all radiation events, but not saving data
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


    private void spreadAround(int chunkX, int chunkZ, int radiation) {
        for(int i = -1; i <= 1; i++) {
            for(int j = -1; j <= 1; j++) {
                long id = pack(chunkX + i, chunkZ + j);
                if(updatedChunks.containsKey(id)) {
                    continue;
                }
                if(chunkRadiation.containsKey(id)) {
                    int curRadiation = unpackX(chunkRadiation.get(id));
                    int curTimestamp = unpackZ(chunkRadiation.get(id));
                    if(curRadiation > radiation*0.5) {
                        continue;
                    }
                    //if chunk already have radiation we use average value
                    int newRadiation = (int) ((curRadiation + radiation * RADIATION_CONFIG.SPREAD_MULTIPLIER.get()/5)/2);
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
