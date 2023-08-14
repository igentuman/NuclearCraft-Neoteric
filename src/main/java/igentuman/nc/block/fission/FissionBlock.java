package igentuman.nc.block.fission;

import igentuman.nc.block.entity.fission.FissionBE;
import igentuman.nc.block.entity.fission.FissionCasingBE;
import igentuman.nc.setup.multiblocks.FissionReactor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FissionBlock extends Block implements EntityBlock {

    public FissionBlock() {
        this(Properties.of(Material.METAL)
                .sound(SoundType.METAL)
                .strength(2.0f)
                .requiresCorrectToolForDrops());
    }
    public FissionBlock(Properties pProperties) {
        super(pProperties.sound(SoundType.METAL));
    }

    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        if(asItem().toString().matches(".*glass|.*cell.*")) {
            return Shapes.create(0.01, 0.01, 0.01, 0.99, 0.99, 0.99);
        }
        return Shapes.block();
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState();
    }

    @Override
    @Deprecated
    public boolean skipRendering(@NotNull BlockState state, @NotNull BlockState adjacentBlockState, @NotNull Direction side) {
        return adjacentBlockState.getBlock().equals(this) && asItem().toString().matches(".*glass|.*cell.*");
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING);
    }

    private String blockEntityCode()
    {
        if(asItem().toString().matches(".*reactor_glass|.*reactor_casing.*")) {
            return "fission_casing";
        }
        if(asItem().toString().matches("graphite.*|beryllium.*")) {
            return "fission_moderator";
        }
        if(asItem().toString().contains("fuel_cell")) {
            return "fission_reactor_fuel_cell";
        }
        return asItem().toString();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return FissionReactor.MULTIBLOCK_BE.get(blockEntityCode()).get().create(pPos, pState);
    }

    @javax.annotation.Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return (lvl, pos, blockState, t) -> {
                if (t instanceof FissionBE tile) {
                    tile.tickClient();
                }
            };
        }
        return (lvl, pos, blockState, t)-> {
            if (t instanceof FissionBE tile) {
                tile.tickServer();
            }
        };
    }

    public void onNeighborChange(BlockState state, LevelReader level, BlockPos pos, BlockPos neighbor){
        ((FissionBE)level.getBlockEntity(pos)).onNeighborChange(state,  pos, neighbor);
    }


}
