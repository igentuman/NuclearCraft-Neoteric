package igentuman.nc.util.insitu_leaching;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

public class WorldVeinsManager extends SavedData {

    public static final String NAME = "nc_world_veins";

    private WorldVeinOres worldVeinData;

    public WorldVeinOres getWorldVeinData(ServerLevel level) {
        worldVeinData.level = level;
        return worldVeinData;
    }

    public void setWorldVeinData(WorldVeinOres worldVeinData) {
        this.worldVeinData = worldVeinData;
        this.setDirty();
    }

    public WorldVeinsManager() {
        worldVeinData = new WorldVeinOres();
    }

    public static WorldVeinsManager get(Level level) {
        if (level.isClientSide) {
            throw new RuntimeException("Don't access this client-side!");
        }
        ServerLevel serverLevel = (ServerLevel) level;
        return serverLevel.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(WorldVeinsManager::new, WorldVeinsManager::load),
                NAME);
    }

    public static WorldVeinsManager load(CompoundTag tag, HolderLookup.Provider provider) {
        WorldVeinsManager manager = new WorldVeinsManager();
        if (tag.contains("depletion")) {
            manager.worldVeinData = WorldVeinOres.deserialize(tag);
        }
        return manager;
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag, HolderLookup.Provider provider) {
        tag.merge(worldVeinData.serializeNBT());
        return tag;
    }
}
