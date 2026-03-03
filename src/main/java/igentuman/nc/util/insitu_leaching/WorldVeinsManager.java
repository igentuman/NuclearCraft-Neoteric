package igentuman.nc.util.insitu_leaching;

import igentuman.api.platform.NCSerialization;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public class WorldVeinsManager extends SavedData {

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
    @Nonnull
    public static WorldVeinsManager get(Level level) {
        if (level.isClientSide) {
            throw new RuntimeException("Don't access this client-side!");
        }
        DimensionDataStorage storage = ((ServerLevel) level).getDataStorage();
        return storage.computeIfAbsent(
                new SavedData.Factory<>(WorldVeinsManager::new, WorldVeinsManager::new, null),
                "nc_world_veins");
    }

    public WorldVeinsManager(CompoundTag tag, HolderLookup.Provider registries) {
        if(tag.contains("depletion")) {
            worldVeinData = WorldVeinOres.deserialize(registries, tag);
        } else {
            worldVeinData = new WorldVeinOres();
        }
    }

    @Override
    public @NotNull CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        return NCSerialization.serialize(worldVeinData, registries);
    }
}
