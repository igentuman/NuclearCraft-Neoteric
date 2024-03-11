package igentuman.nc.util.insitu_leaching;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.WorldSavedData;
import org.antlr.v4.runtime.misc.NotNull;;

import javax.annotation.Nonnull;

public class WorldVeinsManager extends WorldSavedData {

    private WorldVeinOres worldVeinData;
    public WorldVeinOres getWorldVeinData(World level) {
        worldVeinData.level = level;
        return worldVeinData;
    }

    public void setWorldVeinData(WorldVeinOres worldVeinData) {
        this.worldVeinData = worldVeinData;
        this.setDirty();
    }

    public WorldVeinsManager() {
        super("nc_world_veins");
        worldVeinData = new WorldVeinOres();
    }
    @Nonnull
    public static WorldVeinsManager get(World level) {
        if (level.isClientSide) {
            throw new RuntimeException("Don't access this client-side!");
        }
        DimensionSavedDataManager storage = ((ServerWorld) level).getDataStorage();
        return null;
    //    return storage.computeIfAbsent(WorldVeinsManager::new, WorldVeinsManager::new, "nc_world_veins");
    }

    public WorldVeinsManager(CompoundNBT tag) {
        super("nc_world_veins");
        if(tag.contains("depletion")) {
            worldVeinData = WorldVeinOres.deserialize(tag);
        } else {
            worldVeinData = new WorldVeinOres();
        }
    }

    @Override
    public void load(CompoundNBT compoundNBT) {

    }

    @Override
    public @NotNull CompoundNBT save(CompoundNBT tag) {
        return worldVeinData.serializeNBT();
    }
}
