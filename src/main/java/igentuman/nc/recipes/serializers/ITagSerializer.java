package igentuman.nc.recipes.serializers;

import igentuman.nc.recipes.INCRecipe;
import net.minecraft.nbt.CompoundTag;

public interface ITagSerializer {
    CompoundTag toTag(INCRecipe recipe);
}
