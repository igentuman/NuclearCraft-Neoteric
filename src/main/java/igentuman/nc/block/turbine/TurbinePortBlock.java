package igentuman.nc.block.turbine;

import igentuman.nc.block.entity.turbine.TurbinePortBE;
import igentuman.nc.container.TurbinePortContainer;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFaceBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;
import org.antlr.v4.runtime.misc.NotNull;

;


public class TurbinePortBlock extends HorizontalFaceBlock {
    public static final DirectionProperty HORIZONTAL_FACING = FACING;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public TurbinePortBlock() {
        this(Properties.of(Material.METAL)
                .sound(SoundType.METAL)
                .strength(2.0f)
                .requiresCorrectToolForDrops());
    }
    public TurbinePortBlock(Properties pProperties) {
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
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING)
                .add(BlockStateProperties.POWERED);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pPos, @NotNull BlockState pState) {
        return TURBINE_BE.get("turbine_port").get().create(pPos, pState);
    }*/

/*    @Override
    public ActionResultType use(BlockState state, World level, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult result) {

        if (!level.isClientSide()) {
            TileEntity be = level.getBlockEntity(pos);

            if (be instanceof TurbinePortBE)  {
                MenuProvider containerProvider = new MenuProvider() {
                    @Override
                    public TextComponent getDisplayName() {
                        return new TranslationTextComponent("turbine_port");
                    }

                    @Override
                    public ContainerScreen createMenu(int windowId, @NotNull Inventory playerInventory, @NotNull PlayerEntity playerEntity) {
                            return new TurbinePortContainer(windowId, pos, playerInventory);
                    }
                };
                NetworkHooks.openGui((ServerPlayer) player, containerProvider, be.getBlockPos());
            }
        }
        return InteractionResult.SUCCESS;
    }*/


}
