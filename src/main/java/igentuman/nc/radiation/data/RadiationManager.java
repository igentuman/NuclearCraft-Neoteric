package igentuman.nc.radiation.data;

import igentuman.nc.NuclearCraft;
import igentuman.nc.compat.mekanism.MekanismRadiation;
import igentuman.nc.network.toClient.PacketPlayerRadiationData;
import igentuman.nc.network.toClient.PacketWorldRadiationData;
import igentuman.nc.util.ModUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.LinkedList;
import java.util.List;

import static igentuman.nc.handler.config.RadiationConfig.RADIATION_CONFIG;
import static igentuman.nc.radiation.data.WorldRadiation.packChunkPos;

public class RadiationManager extends SavedData {

    private WorldRadiation worldRadiation;
    private int tickCounter = RADIATION_CONFIG.RADIATION_UPDATE_INTERVAL.get();
    private static final Map<ResourceKey<Level>, RadiationManager> instances = new ConcurrentHashMap<>();
    public static void clear(Level level) {
        get(level).worldRadiation.chunkRadiation.clear();
        get(level).worldRadiation.updatedChunks.clear();
        get(level).worldRadiation.newChunks.clear();
    }

    public static void clearAll() {
        instances.clear();
    }

    public WorldRadiation getWorldRadiation() {
        return this.worldRadiation;
    }

    public void setWorldRadiation(WorldRadiation worldRadiation) {
        this.worldRadiation = worldRadiation;
        this.setDirty();
    }

    public RadiationManager() {
        worldRadiation = new WorldRadiation();
    }

    @Nonnull
    public static RadiationManager get(Level level) {
        if (level.isClientSide) {
            throw new RuntimeException("Don't access this client-side!");
        }
        if(instances.containsKey(level.dimension())) {
            return instances.get(level.dimension());
        }
        DimensionDataStorage storage = ((ServerLevel)level).getDataStorage();
        instances.put(level.dimension(), storage.computeIfAbsent(RadiationManager::new, RadiationManager::new, "nc_world_radiation"));
        return instances.get(level.dimension());
    }

    public void tick(Level level) {
        if(!RADIATION_CONFIG.ENABLED.get()) return;
        level.players().forEach(player -> {
            long playerRadiation = 0;
            if (player instanceof ServerPlayer serverPlayer) {
                PlayerRadiation playerRadiationCap = serverPlayer.getCapability(PlayerRadiationProvider.PLAYER_RADIATION).orElse(null);
                if(playerRadiationCap != null) {
                    playerRadiationCap.updateRadiation(level, player);
                    playerRadiation = playerRadiationCap.getRadiation();
                }

                // Sync nearby chunks
                HashMap<Long, Long> nearbyRadiation = new HashMap<>();
                int syncRadius = 8;
                int px = player.chunkPosition().x;
                int pz = player.chunkPosition().z;

                for (int x = px - syncRadius; x <= px + syncRadius; x++) {
                    for (int z = pz - syncRadius; z <= pz + syncRadius; z++) {
                        long id = WorldRadiation.packChunkPos(x, z);
                        if (worldRadiation.chunkRadiation.containsKey(id)) {
                            nearbyRadiation.put(id, worldRadiation.chunkRadiation.get(id));
                        }
                    }
                }
                // Also sync recently updated chunks even if they are slightly further away, 
                // but only if they belong to this world.
                for (long id : worldRadiation.updatedChunks.keySet()) {
                    if (!nearbyRadiation.containsKey(id)) {
                        int cx = WorldRadiation.unpackX(id);
                        int cz = WorldRadiation.unpackZ(id);
                        if (Math.abs(cx - px) <= 16 && Math.abs(cz - pz) <= 16) {
                            nearbyRadiation.put(id, worldRadiation.updatedChunks.get(id));
                        }
                    }
                }

                if (!nearbyRadiation.isEmpty()) {
                    NuclearCraft.packetHandler().sendTo(new PacketWorldRadiationData(nearbyRadiation), serverPlayer);
                }
                NuclearCraft.packetHandler().sendTo(new PacketPlayerRadiationData(playerRadiation), serverPlayer);
            }

        });
        tickCounter--;
        if (tickCounter == RADIATION_CONFIG.RADIATION_UPDATE_INTERVAL.get()/2) {
            worldRadiation.refresh(level);
            return;
        }
        if (tickCounter == 0) {
            tickCounter = RADIATION_CONFIG.RADIATION_UPDATE_INTERVAL.get();
            if(worldRadiation.updatedChunks.isEmpty()) {
                return;
            }

            setDirty();
        }
    }

    public RadiationManager(CompoundTag tag) {
        if(tag.contains("radiation")) {
            worldRadiation = WorldRadiation.deserialize(tag);
        } else {
            worldRadiation = new WorldRadiation();
        }

    }

    @Override
    public @NotNull CompoundTag save(CompoundTag tag) {
        return worldRadiation.serializeNBT();
    }
    protected int[] ignoredPos;
    public void addRadiation(Level level, double value, int x, int y, int z) {
        if(!RADIATION_CONFIG.ENABLED.get()) return;
        if(ignoredPos != null && ignoredPos[0] == x && ignoredPos[1] == y && ignoredPos[2] == z) {
            ignoredPos = null;
            return;
        }
        LevelChunk chunk = level.getChunkAt(new BlockPos(x, y, z));
        int appliedRadiation = worldRadiation.addRadiation(level, value, chunk.getPos().x, chunk.getPos().z);
        if(ModUtil.isMekanismLoaded() && RADIATION_CONFIG.MEKANISM_RADIATION_INTEGRATION.get()) {
            ignoredPos = new int[]{x, y, z};
            MekanismRadiation.radiate(x, y, z, appliedRadiation/5000, level);
        }
    }

    public void clearChunk(int x, int z) {
        worldRadiation.chunkRadiation.remove(packChunkPos(x,z));
    }

    public void addRadiation(Level level, double v, BlockPos worldPosition) {
        addRadiation(level, v, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ());
    }

    public void setChunkRadiation(BlockPos blockPos, int value) {
        worldRadiation.setChunkRadiation(blockPos, value);
        setDirty();
    }
}
