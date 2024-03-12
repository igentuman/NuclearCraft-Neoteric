package igentuman.nc.radiation.data;

import igentuman.nc.NuclearCraft;
import igentuman.nc.compat.mekanism.MekanismRadiation;
import igentuman.nc.network.toClient.PacketRadiationData;
import igentuman.nc.util.ModUtil;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;

import net.minecraft.world.World;

import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.antlr.v4.runtime.misc.NotNull;

import javax.annotation.Nonnull;

import static igentuman.nc.handler.config.RadiationConfig.RADIATION_CONFIG;
import static igentuman.nc.radiation.data.WorldRadiation.pack;
import static igentuman.nc.radiation.data.WorldRadiationProvider.WORLD_RADIATION;

public class RadiationManager extends WorldSavedData {

    private WorldRadiation worldRadiation;
    private int tickCounter = RADIATION_CONFIG.RADIATION_UPDATE_INTERVAL.get();

    public static void clear(World level) {
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
        super("nc_world_radiation");
        worldRadiation = new WorldRadiation();
    }
    @Nonnull
    public static RadiationManager get(World level) {
        if (level.isClientSide) {
            throw new RuntimeException("Don't access this client-side!");
        }
        DimensionSavedDataManager savedData = ServerLifecycleHooks.getCurrentServer().overworld().getDataStorage();
        return savedData.computeIfAbsent(RadiationManager::new, "nc_world_radiation");
    }

    public void tick(World level) {
        level.players().forEach(player -> {
            if (player instanceof ServerPlayerEntity) {
                int playerChunkX = ((ServerPlayerEntity) player).xChunk;
                int playerChunkZ = ((ServerPlayerEntity) player).zChunk;
                long id = pack(playerChunkX, playerChunkZ);
                PlayerRadiation playerRadiationCap = player.getCapability(PlayerRadiationProvider.PLAYER_RADIATION).orElse(null);
                int playerRadiation = 0;
                if(playerRadiationCap != null) {
                    playerRadiationCap.updateRadiation(level, player);
                    playerRadiation = playerRadiationCap.getRadiation();
                }

                if(worldRadiation.chunkRadiation.get(id) != null) {
                    NuclearCraft.packetHandler().sendTo(new PacketRadiationData(id, worldRadiation.chunkRadiation.get(id), playerRadiation), (ServerPlayerEntity) player);
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

    public RadiationManager(CompoundNBT tag) {
        super("nc_world_radiation");
        if(tag.contains("radiation")) {
            worldRadiation = WorldRadiation.deserialize(tag);
        } else {
            worldRadiation = new WorldRadiation();
        }

    }

    @Override
    public void load(CompoundNBT tag) {
        if(tag.contains("radiation")) {
            worldRadiation = WorldRadiation.deserialize(tag);
        } else {
            worldRadiation = new WorldRadiation();
        }
    }

    @Override
    public @NotNull CompoundNBT save(CompoundNBT tag) {
        return worldRadiation.serializeNBT();
    }
    protected int[] ignoredPos;
    public void addRadiation(World level, double value, int x, int y, int z) {
        if(ignoredPos != null && ignoredPos[0] == x && ignoredPos[1] == y && ignoredPos[2] == z) {
            ignoredPos = null;
            return;
        }
        Chunk chunk = level.getChunkAt(new BlockPos(x, y, z));
        int appliedRadiation = worldRadiation.addRadiation(level, value, chunk.getPos().x, chunk.getPos().z);
        if(ModUtil.isMekanismLoadeed() && RADIATION_CONFIG.MEKANISM_RADIATION_INTEGRATION.get()) {
            ignoredPos = new int[]{x, y, z};
            MekanismRadiation.radiate(x, y, z, appliedRadiation, level);
        }
    }
}
