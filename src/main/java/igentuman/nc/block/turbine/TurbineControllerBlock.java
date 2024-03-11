package igentuman.nc.block.turbine;

import igentuman.nc.block.entity.turbine.TurbineControllerBE;
import igentuman.nc.container.TurbineControllerContainer;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.Direction;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.inventory.Inventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraftforge.fml.network.NetworkHooks;
import org.antlr.v4.runtime.misc.NotNull;;
import javax.annotation.Nullable;

import static igentuman.nc.multiblock.turbine.TurbineRegistration.TURBINE_BE;

public class TurbineControllerBlock extends HorizontalBlock {
    public static final DirectionProperty HORIZONTAL_FACING = FACING;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final String NAME = "turbine_controller";
    public TurbineControllerBlock() {
        this(Properties.of(Material.METAL)
                .sound(SoundType.METAL)
                .strength(2.0f)
                .requiresCorrectToolForDrops());
    }
    public TurbineControllerBlock(Properties pProperties) {
        super(pProperties.sound(SoundType.METAL));
/*        this.registerDefaultState(
                this.stateDefinition.any()
                        .setValue(HORIZONTAL_FACING, Direction.NORTH)
                        .setValue(POWERED, false)
        );*/
    }
/*    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }*/

/*    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING)
                .add(BlockStateProperties.POWERED);
    }*/
/*

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pPos, @NotNull BlockState pState) {
        return TURBINE_BE.get(NAME).get().create(pPos, pState);
    }

    @Override
    public InteractionResult use(@NotNull BlockState state, World level, @NotNull BlockPos pos, @NotNull PlayerEntity player, InteractionHand hand, BlockHitResult result) {

        if (!level.isClientSide()) {
            TileEntity be = level.getBlockEntity(pos);

            if (be instanceof TurbineControllerBE<?>)  {
                MenuProvider containerProvider = new MenuProvider() {
                    @Override
                    public TextComponent getDisplayName() {
                        return new TranslationTextComponent(NAME);
                    }

                    @Override
                    public ContainerScreen createMenu(int windowId, @NotNull Inventory playerInventory, @NotNull PlayerEntity playerEntity) {
                            return new TurbineControllerContainer(windowId, pos, playerInventory);
                    }
                };
                NetworkHooks.openGui((ServerPlayer) player, containerProvider, be.getBlockPos());
            }
        }
        return InteractionResult.SUCCESS;
    }
*/

}
