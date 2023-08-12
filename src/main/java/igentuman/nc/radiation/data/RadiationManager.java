package igentuman.nc.radiation.data;

import igentuman.nc.NuclearCraft;
import igentuman.nc.handler.config.CommonConfig;
import igentuman.nc.network.toClient.PacketRadiationData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import javax.annotation.Nonnull;

import static igentuman.nc.handler.config.CommonConfig.RADIATION_CONFIG;

public class RadiationManager extends SavedData {

    private WorldRadiation radiation;
    private int tickCounter = RADIATION_CONFIG.RADIATION_UPDATE_INTERVAL.get();
    public WorldRadiation getRadiation() {
        return this.radiation;
    }

    public void setRadiation(WorldRadiation radiation) {
        this.radiation = radiation;
        this.setDirty();
    }
    public RadiationManager() {
        radiation = new WorldRadiation();
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
        tickCounter--;
        if (tickCounter == RADIATION_CONFIG.RADIATION_UPDATE_INTERVAL.get()/2) {
            radiation.refresh(level);
            return;
        }
        if (tickCounter == 0) {
            tickCounter = RADIATION_CONFIG.RADIATION_UPDATE_INTERVAL.get();
            if(radiation.updatedChunks.isEmpty()) {
                return;
            }
            level.players().forEach(player -> {
                if (player instanceof ServerPlayer serverPlayer) {
                    NuclearCraft.packetHandler().sendTo(new PacketRadiationData(radiation.updatedChunks), serverPlayer);
                }
            });
            setDirty();
        }
    }

    public RadiationManager(CompoundTag tag) {
        if(tag.contains("radiation")) {
            radiation = WorldRadiation.deserialize(tag);
        } else {
            radiation = new WorldRadiation();
        }

    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        return radiation.serializeNBT();
    }

    public void addRadiation(Level level, double value, int x, int z) {
        radiation.addRadiation(level, value, x, z);
    }
}
