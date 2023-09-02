package igentuman.nc.radiation.data;

import igentuman.nc.NuclearCraft;
import igentuman.nc.compat.mekanism.MekanismRadiation;
import igentuman.nc.network.toClient.PacketRadiationData;
import igentuman.nc.util.ModUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

import static igentuman.nc.handler.config.CommonConfig.RADIATION_CONFIG;
import static igentuman.nc.radiation.data.WorldRadiation.pack;

public class RadiationManager extends SavedData {

    private WorldRadiation worldRadiation;
    private int tickCounter = RADIATION_CONFIG.RADIATION_UPDATE_INTERVAL.get();

    public static void clear(Level level) {
        get(level).worldRadiation.chunkRadiation.clear();
        get(level).worldRadiation.updatedChunks.clear();
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
        DimensionDataStorage storage = ((ServerLevel)level).getDataStorage();
        return storage.computeIfAbsent(RadiationManager::new, RadiationManager::new, "nc_world_radiation");
    }

    public void tick(Level level) {
        level.players().forEach(player -> {
            if (player instanceof ServerPlayer serverPlayer) {
                int playerChunkX = player.chunkPosition().x;
                int playerChunkZ = player.chunkPosition().z;
                long id = pack(playerChunkX, playerChunkZ);
                PlayerRadiation playerRadiationCap = serverPlayer.getCapability(PlayerRadiationProvider.PLAYER_RADIATION).orElse(null);
                int playerRadiation = 0;
                if(playerRadiationCap != null) {
                    playerRadiationCap.updateRadiation(level, player);
                    playerRadiation = playerRadiationCap.getRadiation();
                }

                if(worldRadiation.chunkRadiation.get(id) != null) {
                    NuclearCraft.packetHandler().sendTo(new PacketRadiationData(id, worldRadiation.chunkRadiation.get(id), playerRadiation), serverPlayer);
                }
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
        if(ignoredPos != null && ignoredPos[0] == x && ignoredPos[1] == y && ignoredPos[2] == z) {
            ignoredPos = null;
            return;
        }
        LevelChunk chunk = level.getChunkAt(new BlockPos(x, y, z));
        int appliedRadiation = worldRadiation.addRadiation(level, value, chunk.getPos().x, chunk.getPos().z);
        if(ModUtil.isMekanismLoadeed() && RADIATION_CONFIG.MEKANISM_RADIATION_INTEGRATION.get()) {
            ignoredPos = new int[]{x, y, z};
            MekanismRadiation.radiate(x, y, z, appliedRadiation, level);
        }
    }
}
