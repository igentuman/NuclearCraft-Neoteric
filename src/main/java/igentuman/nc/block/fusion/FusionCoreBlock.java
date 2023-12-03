package igentuman.nc.block.fusion;

import igentuman.nc.block.entity.fusion.FusionCoreBE;
import igentuman.nc.block.entity.fusion.FusionCoreProxyBE;
import igentuman.nc.block.entity.processor.NCProcessorBE;
import igentuman.nc.container.FissionControllerContainer;
import igentuman.nc.container.FusionCoreContainer;
import igentuman.nc.content.processors.Processors;
import igentuman.nc.setup.registration.NCProcessors;
import igentuman.nc.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
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
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING)
                .add(BlockStateProperties.POWERED);
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return Shapes.create(-1.00, 0.00, -1.00, 2.00, 3.00, 2.00);
    }


    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return FUSION_BE.get("fusion_core").get().create(pPos, pState);
    }

    @Override
    public void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
        super.onPlace(pState, pLevel, pPos, pOldState, pIsMoving);
        //placeProxyBlocks(pState, pLevel, pPos);
    }

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
        if(pState.getBlock() != pNewState.getBlock()) {
            removeProxyBlocks(pState, pLevel, pPos);
        }
    }

    public void removeProxyBlocks(BlockState pState, Level pLevel, BlockPos pPos) {
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

    public void placeProxyBlocks(BlockState pState, Level pLevel, BlockPos pPos, FusionCoreBE core) {
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

    public void placeProxyBlocks(BlockState pState, Level pLevel, BlockPos pPos) {
        FusionCoreBE core = (FusionCoreBE) pLevel.getBlockEntity(pPos);
        placeProxyBlocks(pState, pLevel, pPos, core);
    }

    @Override
    public void playerDestroy(Level pLevel, Player pPlayer, BlockPos pPos, BlockState pState, @javax.annotation.Nullable BlockEntity pBlockEntity, ItemStack pTool) {
        pPlayer.awardStat(Stats.BLOCK_MINED.get(this));
        pPlayer.causeFoodExhaustion(0.005F);

    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        if (!level.isClientSide()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof FusionCoreBE)  {
                MenuProvider containerProvider = new MenuProvider() {
                    @Override
                    public Component getDisplayName() {
                        return Component.translatable("fusion_core");
                    }

                    @Override
                    public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player playerEntity) {
                        return new FusionCoreContainer(windowId, pos, playerInventory);
                    }
                };
                NetworkHooks.openScreen((ServerPlayer) player, containerProvider, be.getBlockPos());
            }
        }
        return InteractionResult.SUCCESS;
    }

    @javax.annotation.Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return (lvl, pos, blockState, t) -> {
                if (t instanceof FusionCoreBE<?> tile) {
                    tile.tickClient();
                   // level.setBlockAndUpdate(pos, blockState.setValue(ACTIVE, tile.isActive));
                }
            };
        }
        return (lvl, pos, blockState, t)-> {
            if (t instanceof FusionCoreBE<?> tile) {
                tile.tickServer();
            }
        };
    }

    @Override
    public void appendHoverText(ItemStack pStack, @javax.annotation.Nullable BlockGetter pLevel, List<Component> list, TooltipFlag pFlag) {
        if(asItem().toString().contains("empty") || this.asItem().equals(Items.AIR)) return;
    }

}
