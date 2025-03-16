package igentuman.nc.block.turbine;

import igentuman.nc.block.MultiblockBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

public class TurbineBlock extends MultiblockBlock {

    public TurbineBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public BlockState getStateForPlacement(@NotNull BlockPlaceContext context) {
        return this.defaultBlockState();
    }

    @Override
    @Deprecated
    public boolean skipRendering(@NotNull BlockState state, @NotNull BlockState adjacentBlockState, @NotNull Direction side) {
        return adjacentBlockState.getBlock().equals(this) && codeID().matches(".*glass.*|.*slope.*");
    }

    private String codeID()
    {
        return ForgeRegistries.BLOCKS.getKey(this).getPath();
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
        return asItem().toString().contains("glass");
    }
}
