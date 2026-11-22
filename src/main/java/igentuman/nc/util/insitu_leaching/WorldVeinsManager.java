package igentuman.nc.util.insitu_leaching;

import com.mojang.serialization.Codec;
import igentuman.nc.util.persistence.CurrentSavedData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/** Persists vein depletion independently in each dimension. */
public class WorldVeinsManager extends SavedData {

    public static final String NAME = "nc_world_veins";
    public static final Codec<WorldVeinsManager> CODEC = CurrentSavedData.strict(WorldVeinOres.CODEC.xmap(data -> {
        WorldVeinsManager manager = new WorldVeinsManager();
        manager.worldVeinData = data;
        return manager;
    }, manager -> manager.worldVeinData));
    public static final SavedDataType<WorldVeinsManager> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath("nuclearcraft", NAME), WorldVeinsManager::new, CODEC);

    private WorldVeinOres worldVeinData;

    public WorldVeinOres getWorldVeinData(ServerLevel level) {
        worldVeinData.level = level;
        return worldVeinData;
    }

    public WorldVeinsManager() {
        worldVeinData = new WorldVeinOres();
    }

    public static WorldVeinsManager get(Level level) {
        if (level.isClientSide()) {
            throw new RuntimeException("Don't access this client-side!");
        }
        ServerLevel serverLevel = (ServerLevel) level;
        return CurrentSavedData.get(serverLevel, TYPE);
    }
}
