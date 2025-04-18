package igentuman.nc.block.kugelblitz;

import igentuman.nc.block.entity.kugelblitz.EXPLBE;
import igentuman.nc.block.entity.kugelblitz.EXPLProxyBE;
import igentuman.nc.container.EXPLContainer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static igentuman.nc.multiblock.kugelblitz.KugelblitzRegistration.EXPL_BE;
import static igentuman.nc.multiblock.kugelblitz.KugelblitzRegistration.EXPL_PROXY_BLOCK;

public class EXPLBlock extends DirectionalBlock implements EntityBlock {
    public static final BooleanProperty ACTIVE = BlockStateProperties.POWERED;

    public EXPLBlock(Properties pProperties) {
        super(pProperties.sound(SoundType.METAL).strength(8f));
        this.registerDefaultState(
                this.stateDefinition.any()
                        .setValue(FACING, Direction.UP)
                        .setValue(ACTIVE, false)
        );
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.FACING).add(ACTIVE);
    }

    @Override
    public VoxelShape getShape(BlockState blockstate, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return switch (blockstate.getValue(FACING)) {
            case UP -> Shapes.create(-1.00, 0.00, -1.00, 2.00, 4.00, 2.00);
            case DOWN -> Shapes.create(-1.00, -3.00, -1.00, 2.00, 1.00, 2.00);
            case NORTH -> Shapes.create(-1.00, -1.00, -3.00, 2.00, 2.00, 1.00);
            case SOUTH -> Shapes.create(-1.00, -1.00, 0.00, 2.00, 2.00, 4.00);
            case WEST -> Shapes.create(-3.00, -1.00, -1.00, 1.00, 2.00, 2.00);
            case EAST -> Shapes.create(0.00, -1.00, -1.00, 4.00, 2.00, 2.00);
        };
    }


    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return EXPL_BE.get().create(pPos, pState);
    }

    @Override
    public void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
        super.onPlace(pState, pLevel, pPos, pOldState, pIsMoving);
        placeProxyBlocks(pState, pLevel, pPos);
    }

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
        if(pState.getBlock() != pNewState.getBlock()) {
            removeProxyBlocks(pState, pLevel, pPos);
        }
    }

    public void removeProxyBlocks(BlockState pState, Level pLevel, BlockPos pPos) {
        //proxy block placement depends on facing
        Direction facing = pState.getValue(FACING);
        int minX = -1, maxX = 0, minY = -1, minZ = 2, maxY = 4, maxZ = 2;
        switch (facing) {
            case UP:
                minX = -1; minY = 0; minZ = -1; maxX = 2;  maxY = 4; maxZ = 2;
                break;
            case DOWN:
                minX = -1; minY = -3; minZ = -1; maxX = 2; maxY = 1; maxZ = 2;
                break;
            case NORTH:
                minX = -1; minY = -1; minZ = -3; maxX = 2; maxY = 2; maxZ = 1;
                break;
            case SOUTH:
                minX = -1; minY = -1; minZ = 0; maxX = 2; maxY = 2; maxZ = 4;
                break;
            case WEST:
                minX = -3; minY = -1; minZ = -1; maxX = 1; maxY = 2; maxZ = 2;
                break;
            case EAST:
                minX = 0; minY = -1; minZ = -1; maxX = 4; maxY = 2; maxZ = 2;
                break;
        }

        for (int x = minX; x < maxX; x++) {
            for (int z = minZ; z < maxZ; z++) {
                for (int y = minY; y < maxY; y++) {
                    BlockPos pos = pPos.offset(x, y, z);
                    if (pPos.equals(pos)) continue;
                    pLevel.removeBlock(pos, false);
                }
            }
        }
    }

    public void placeProxyBlocks(BlockState pState, Level pLevel, BlockPos pPos, EXPLBE core) {
        //proxy block placement depends on facing
        Direction facing = pState.getValue(FACING);
        int minX = -1, maxX = 0, minY = -1, minZ = 2, maxY = 4, maxZ = 2;
        switch (facing) {
            case UP:
                minX = -1; minY = 0; minZ = -1; maxX = 2;  maxY = 4; maxZ = 2;
                break;
            case DOWN:
                minX = -1; minY = -3; minZ = -1; maxX = 2; maxY = 1; maxZ = 2;
                break;
            case NORTH:
                minX = -1; minY = -1; minZ = -3; maxX = 2; maxY = 2; maxZ = 1;
                break;
            case SOUTH:
                minX = -1; minY = -1; minZ = 0; maxX = 2; maxY = 2; maxZ = 4;
                break;
            case WEST:
                minX = -3; minY = -1; minZ = -1; maxX = 1; maxY = 2; maxZ = 2;
                break;
            case EAST:
                minX = 0; minY = -1; minZ = -1; maxX = 4; maxY = 2; maxZ = 2;
                break;
        }

        for (int x = minX; x < maxX; x++) {
            for (int z = minZ; z < maxZ; z++) {
                for (int y = minY; y < maxY; y++) {
                    BlockPos pos = pPos.offset(x, y, z);
                    if (pPos.equals(pos)) continue;
                    pLevel.setBlock(pos, EXPL_PROXY_BLOCK.get().defaultBlockState(), 3);
                    EXPLProxyBE be = (EXPLProxyBE) pLevel.getBlockEntity(pos);
                    be.setCore(core);
                }
            }
        }
    }

    public void placeProxyBlocks(BlockState pState, Level pLevel, BlockPos pPos) {
        EXPLBE core = (EXPLBE) pLevel.getBlockEntity(pPos);
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
            BlockEntity be = level.getExistingBlockEntity(pos);
            if (be instanceof EXPLProxyBE)  {
                MenuProvider containerProvider = new MenuProvider() {
                    @Override
                    public Component getDisplayName() {
                        return Component.translatable("expl");
                    }

                    @Override
                    public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player playerEntity) {
                        return new EXPLContainer(windowId, pos, playerInventory);
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
                if (t instanceof EXPLBE tile) {
                    tile.tickClient();
                   // level.setBlockAndUpdate(pos, blockState.setValue(ACTIVE, tile.isActive));
                }
            };
        }
        return (lvl, pos, blockState, t)-> {
            if (t instanceof EXPLBE tile) {
                tile.tickServer();
            }
        };
    }

    @Override
    public void appendHoverText(ItemStack pStack, @javax.annotation.Nullable BlockGetter pLevel, List<Component> list, TooltipFlag pFlag) {
        if(asItem().toString().contains("empty") || this.asItem().equals(Items.AIR)) return;
        list.add(Component.translatable("tooltip.expl").withStyle(ChatFormatting.GREEN));
    }
}
