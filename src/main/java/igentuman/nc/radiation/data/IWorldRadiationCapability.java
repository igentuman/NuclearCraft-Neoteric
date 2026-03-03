package igentuman.nc.radiation.data;

import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;


public interface IWorldRadiationCapability extends INBTSerializable<CompoundTag> {

    int getChunkRadiation(int chunkX, int chunkZ);

    void updateChunkRadiation(long id);
}
