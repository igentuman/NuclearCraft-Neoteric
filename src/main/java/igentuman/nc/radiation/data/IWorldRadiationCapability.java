package igentuman.nc.radiation.data;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;
import net.minecraftforge.common.util.INBTSerializable;


public interface IWorldRadiationCapability extends INBTSerializable<CompoundNBT> {

    int getChunkRadiation(int chunkX, int chunkZ);

    void updateChunkRadiation(int chunkX, int chunkZ);

    void updateChunkRadiation(long id);
}
