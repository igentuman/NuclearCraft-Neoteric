package igentuman.nc.radiation.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.INBTSerializable;


public interface IWorldRadiationCapability extends INBTSerializable<CompoundTag> {

    int getChunkRadiation(int chunkX, int chunkZ);

    void updateChunkRadiation(int chunkX, int chunkZ);

    void updateChunkRadiation(long id);
}
