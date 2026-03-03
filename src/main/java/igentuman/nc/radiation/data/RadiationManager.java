package igentuman.nc.radiation.data;

import igentuman.nc.NuclearCraft;
import igentuman.nc.compat.mekanism.MekanismRadiation;
import igentuman.nc.network.toClient.PacketPlayerRadiationData;
import igentuman.nc.network.toClient.PacketWorldRadiationData;
import igentuman.nc.setup.registration.NCAttachments;
import igentuman.nc.util.ModUtil;
import net.minecraft.core.BlockPos;
import igentuman.api.platform.NCSerialization;
import net.minecraft.core.HolderLookup;
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
import java.util.LinkedList;
import java.util.List;

import static igentuman.nc.handler.config.RadiationConfig.RADIATION_CONFIG;
import static igentuman.nc.radiation.data.WorldRadiation.pack;

public class RadiationManager extends SavedData {

    private WorldRadiation worldRadiation;
    private int tickCounter = RADIATION_CONFIG.RADIATION_UPDATE_INTERVAL.get();
    private static final HashMap<ResourceKey<Level>, RadiationManager> instances = new HashMap<>();
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
        if(instances.containsKey(level.dimension())) {
            return instances.get(level.dimension());
        }
        DimensionDataStorage storage = ((ServerLevel)level).getDataStorage();
        instances.put(level.dimension(), storage.computeIfAbsent(
                new SavedData.Factory<>(RadiationManager::new, RadiationManager::new, null),
                "nc_world_radiation"));
        return instances.get(level.dimension());
    }

    public void tick(Level level) {
        if(!RADIATION_CONFIG.ENABLED.get()) return;
        level.players().forEach(player -> {
            long wasRadiation = 0;
            long playerRadiation = 0;
            if (player instanceof ServerPlayer serverPlayer) {
                PlayerRadiation playerRadiationCap = serverPlayer.getData(NCAttachments.PLAYER_RADIATION.get());
                wasRadiation = playerRadiationCap.getRadiation();
                playerRadiationCap.updateRadiation(level, player);
                playerRadiation = playerRadiationCap.getRadiation();

                NuclearCraft.packetHandler().sendTo(new PacketWorldRadiationData(worldRadiation.chunkRadiation), serverPlayer);
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

    public RadiationManager(CompoundTag tag, HolderLookup.Provider registries) {
        if(tag.contains("radiation")) {
            worldRadiation = WorldRadiation.deserialize(registries, tag);
        } else {
            worldRadiation = new WorldRadiation();
        }

    }

    @Override
    public @NotNull CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        return NCSerialization.serialize(worldRadiation, registries);
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
        worldRadiation.chunkRadiation.remove(pack(x,z));
    }

    public void addRadiation(Level level, double v, BlockPos worldPosition) {
        addRadiation(level, v, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ());
    }

    public void setChunkRadiation(BlockPos blockPos, int value) {
        worldRadiation.setChunkRadiation(blockPos, value);
        setDirty();
    }
}
