package igentuman.nc.recipes;

import net.minecraft.nbt.CompoundTag;

public interface ITagSerializer {
    CompoundTag toTag(INCRecipe recipe);
}
