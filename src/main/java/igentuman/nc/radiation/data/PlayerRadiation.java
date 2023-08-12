package igentuman.nc.radiation.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import java.util.HashMap;

import static igentuman.nc.handler.config.CommonConfig.RADIATION_CONFIG;

public class PlayerRadiation implements IPlayerRadiationCapability {

    private final double decaySpeed = RADIATION_CONFIG.DECAY_SPEED_FOR_PLAYER.get();

    public Level level;
    private int radiation = 0;
    private int timestamp = 0;

    public PlayerRadiation() {
    }

    public static PlayerRadiation deserialize(CompoundTag radiation) {
        PlayerRadiation playerRadiation = new PlayerRadiation();
        playerRadiation.deserializeNBT(radiation);
        return playerRadiation;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("radiation", radiation);
        tag.putInt("timestamp", timestamp);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        CompoundTag radiationTag = nbt.getCompound("radiation");
        radiation = radiationTag.getInt("radiation");
        timestamp = radiationTag.getInt("timestamp");
    }

    public void copyFrom(PlayerRadiation source) {
        radiation = source.radiation;
        timestamp = source.timestamp;
    }

    public void updateRadiation(Level level, Entity player) {
        this.level = level;
        WorldRadiation worldRadiation = RadiationManager.get(level).getWorldRadiation();
        int chunkRadiation = worldRadiation.getChunkRadiation(player.chunkPosition().x, player.chunkPosition().z);
        if(chunkRadiation > radiation) {
            radiation = (chunkRadiation + radiation)/2;
        }
    }

    @Override
    public int getRadiation() {
        return radiation;
    }

    @Override
    public void setRadiation(int radiation) {
        this.radiation = radiation;
    }

    @Override
    public int getTimestamp() {
        return timestamp;
    }

    @Override
    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }
}
