package igentuman.nc.block.entity;

import igentuman.nc.util.annotation.NBTField;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.BlockState;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class RecipeBE extends NuclearCraftBE {
    public RecipeBE(TileEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }
    
}
