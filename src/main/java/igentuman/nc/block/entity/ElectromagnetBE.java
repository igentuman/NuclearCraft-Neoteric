package igentuman.nc.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import static igentuman.nc.setup.registration.NCBlocks.NC_BE;

public class ElectromagnetBE extends BlockEntity {
    protected String name;

    public ElectromagnetBE(BlockPos blockPos, BlockState blockState) {
        this(NC_BE.get(getName(blockState)).get(), blockPos, blockState);
    }

    public static String getName(BlockState pBlockState) {
        return pBlockState.getBlock().asItem().toString();
    }


    public ElectromagnetBE(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
        name = getName(pBlockState);
    }
}
