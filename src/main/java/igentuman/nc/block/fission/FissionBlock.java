package igentuman.nc.block.fission;

import igentuman.nc.block.entity.fission.FissionBE;
import igentuman.nc.multiblock.fission.FissionReactor;
import net.minecraft.block.SoundType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import org.antlr.v4.runtime.misc.NotNull;
import javax.annotation.Nullable;

public class FissionBlock extends Block {

    public FissionBlock(Properties pProperties) {
        super(pProperties.sound(SoundType.METAL));
    }

    @Override
    @Deprecated
    public boolean skipRendering(@NotNull BlockState state, @NotNull BlockState adjacentBlockState, @NotNull Direction side) {
        return adjacentBlockState.getBlock().equals(this) && asItem().toString().matches(".*glass|.*cell.*|.*slope.*");
    }

    private String blockEntityCode(BlockState state)
    {
        String code = state.getBlock().asItem().toString();
        if(code.matches(".*reactor_glass|.*reactor_casing.*")) {
            return "fission_casing";
        }
        if(code.matches("graphite.*|beryllium.*")) {
            return "fission_moderator";
        }
        if(code.contains("fuel_cell")) {
            return "fission_reactor_fuel_cell";
        }
        return code;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return state != null;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return FissionReactor.FISSION_BE.get(blockEntityCode(state)).get().create();
    }



    @Override
    public void onNeighborChange(BlockState state, IWorldReader level, BlockPos pos, BlockPos neighbor){
        ((FissionBE)level.getBlockEntity(pos)).onNeighborChange(state,  pos, neighbor);
    }
}
