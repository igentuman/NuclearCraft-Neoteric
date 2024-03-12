package igentuman.nc.block.fusion;

import igentuman.nc.block.entity.fusion.FusionCoreBE;
import igentuman.nc.block.entity.fusion.FusionCoreProxyBE;
import igentuman.nc.container.FusionCoreContainer;
import net.minecraft.block.SoundType;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Items;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.stats.Stats;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraftforge.fml.network.NetworkHooks;
import javax.annotation.Nullable;

import java.util.List;

import static igentuman.nc.multiblock.fusion.FusionReactor.FUSION_BE;
import static igentuman.nc.multiblock.fusion.FusionReactor.FUSION_CORE_PROXY;

public class FusionCoreBlock extends FusionBlock {
    public static final BooleanProperty ACTIVE = BlockStateProperties.POWERED;

    public FusionCoreBlock(Properties pProperties) {
        super(pProperties.sound(SoundType.METAL));
        this.registerDefaultState(
                this.stateDefinition.any()
                        .setValue(ACTIVE, false)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.POWERED);
    }


    @Override
    public VoxelShape getShape(BlockState pState, IBlockReader world, BlockPos pPos, ISelectionContext pContext) {
        return VoxelShapes.create(new AxisAlignedBB(-1.00, 0.00, -1.00, 2.00, 3.00, 2.00));
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState pState, IBlockReader world) {
        return FUSION_BE.get("fusion_core").get().create();
    }

    @Override
    public void onPlace(BlockState pState, World pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
        super.onPlace(pState, pLevel, pPos, pOldState, pIsMoving);
        placeProxyBlocks(pState, pLevel, pPos);
    }

    @Override
    public void onRemove(BlockState pState, World pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
        if(pState.getBlock() != pNewState.getBlock()) {
            removeProxyBlocks(pState, pLevel, pPos);
        }
    }

    public void removeProxyBlocks(BlockState pState, World pLevel, BlockPos pPos) {
        for(int x = -1; x < 2; x++) {
            for (int z = -1; z < 2; z++) {
                for(int y = 0; y < 3; y++) {
                    BlockPos pos = pPos.offset(x, y, z);
                    if(pPos.equals(pos)) continue;
                    pLevel.removeBlock(pos, false);
                }
            }
        }
    }

    public void placeProxyBlocks(BlockState pState, World pLevel, BlockPos pPos, FusionCoreBE core) {
        for(int x = -1; x < 2; x++) {
            for (int z = -1; z < 2; z++) {
                for(int y = 0; y < 3; y++) {
                    BlockPos pos = pPos.offset(x, y, z);
                    if(pPos.equals(pos)) continue;
                    pLevel.setBlock(pos, FUSION_CORE_PROXY.get().defaultBlockState(), 3);
                    FusionCoreProxyBE be = (FusionCoreProxyBE) pLevel.getBlockEntity(pos);
                    be.setCore(core);
                }
            }
        }
    }

    public void placeProxyBlocks(BlockState pState, World pLevel, BlockPos pPos) {
        FusionCoreBE core = (FusionCoreBE) pLevel.getBlockEntity(pPos);
        placeProxyBlocks(pState, pLevel, pPos, core);
    }

    @Override
    public void playerDestroy(World pLevel, PlayerEntity pPlayer, BlockPos pPos, BlockState pState, @javax.annotation.Nullable TileEntity pBlockEntity, ItemStack pTool) {
        pPlayer.awardStat(Stats.BLOCK_MINED.get(this));
        pPlayer.causeFoodExhaustion(0.005F);

    }

    @Override
    public ActionResultType use(BlockState state, World level, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult rayTraceResult) {
        if (!level.isClientSide()) {
            TileEntity be = level.getBlockEntity(pos);
            if (be instanceof FusionCoreBE)  {
                INamedContainerProvider containerProvider = new INamedContainerProvider() {
                    @Override
                    public ITextComponent getDisplayName() {
                        return null;
                    }

                    @Override
                    public Container createMenu(int windowId, PlayerInventory playerInventory, PlayerEntity playerEntity) {
                        return new FusionCoreContainer(windowId, pos, playerInventory);
                    }
                };
                NetworkHooks.openGui((ServerPlayerEntity) player, containerProvider, be.getBlockPos());
            }
        }
        return ActionResultType.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack pStack, @javax.annotation.Nullable IBlockReader pLevel, List<ITextComponent> list, ITooltipFlag pFlag) {
        if(asItem().toString().contains("empty") || this.asItem().equals(Items.AIR)) return;
    }

}
