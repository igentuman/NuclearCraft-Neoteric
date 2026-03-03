package igentuman.nc.util.insitu_leaching;

import igentuman.nc.recipes.type.OreVeinRecipe;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;


public interface IWorldVeinCapability extends INBTSerializable<CompoundTag> {

    OreVeinRecipe getVeinForChunk(int chunkX, int chunkZ);

    boolean chunkContainsVein(int chunkX, int chunkZ);

    int getBlocksLeft(int chunkX, int chunkZ);

    void mineBlock(int chunkX, int chunkZ);
}
