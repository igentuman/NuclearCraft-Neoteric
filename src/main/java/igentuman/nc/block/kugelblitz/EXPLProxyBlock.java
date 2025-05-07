package igentuman.nc.block.kugelblitz;

import igentuman.nc.block.entity.kugelblitz.EXPLBE;
import igentuman.nc.block.entity.kugelblitz.EXPLProxyBE;
import igentuman.nc.container.EXPLContainer;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;
import static igentuman.nc.multiblock.kugelblitz.KugelblitzRegistration.EXPL_PROXY_BE;
import static igentuman.nc.util.TextUtils.__;

public class EXPLProxyBlock extends Block implements EntityBlock {

    public EXPLProxyBlock(Properties pProperties) {
        super(pProperties.sound(SoundType.METAL).strength(8f));
        this.registerDefaultState(
                this.stateDefinition.any()
        );
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return EXPL_PROXY_BE.get().create(pPos, pState);
    }

    public String getCode()
    {
        return ForgeRegistries.BLOCKS.getKey(this).getPath();
    }

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        EXPLProxyBE be = (EXPLProxyBE) pLevel.getBlockEntity(pPos);
        be.destroyCore();
        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        if (!level.isClientSide()) {
            BlockEntity be = level.getBlockEntity(pos);
            EXPLProxyBE proxy = (EXPLProxyBE) be;
            if (proxy.getCoreBE() instanceof EXPLBE)  {
                MenuProvider containerProvider = new MenuProvider() {
                    @Override
                    public Component getDisplayName() {
                        return __("expl");
                    }

                    @Override
                    public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player playerEntity) {
                        return new EXPLContainer(windowId, proxy.getCorePos(), playerInventory);
                    }
                };
                NetworkHooks.openScreen((ServerPlayer) player, containerProvider, proxy.getCorePos());
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void playerDestroy(Level pLevel, Player pPlayer, BlockPos pPos, BlockState pState, @javax.annotation.Nullable BlockEntity pBlockEntity, ItemStack pTool) {
        pPlayer.awardStat(Stats.BLOCK_MINED.get(this));
        pPlayer.causeFoodExhaustion(0.005F);
    }

    @javax.annotation.Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return (lvl, pos, blockState, t) -> {
                if (t instanceof EXPLProxyBE tile) {
                    tile.tickClient();
                }
            };
        }
        return (lvl, pos, blockState, t)-> {
            if (t instanceof EXPLProxyBE tile) {
                tile.tickServer();
            }
        };
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState pState) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState pBlockState, Level pLevel, BlockPos pPos) {
        return pLevel.getBlockEntity(pPos) instanceof EXPLProxyBE be ? be.getAnalogSignal() : 0;
    }
}
