package igentuman.nc.block.fusion;

import igentuman.nc.block.entity.fusion.FusionBE;
import igentuman.nc.block.entity.fusion.FusionCoreBE;
import igentuman.nc.block.entity.fusion.FusionCoreProxyBE;
import igentuman.nc.container.FusionCoreContainer;
import igentuman.nc.multiblock.fusion.FusionReactor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import static igentuman.nc.multiblock.fusion.FusionReactor.FUSION_CORE_PROXY_BE;

public class FusionCoreProxy extends FusionBlock {

    public FusionCoreProxy(Properties pProperties) {
        super(pProperties.sound(SoundType.METAL));
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
        return FUSION_CORE_PROXY_BE.get().create(pPos, pState);
    }

    public String getCode()
    {
        return Registry.BLOCK.getKey(this).getPath();
    }

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        FusionCoreProxyBE be = (FusionCoreProxyBE) pLevel.getBlockEntity(pPos);
        be.destroyCore();
        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);

    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        if (!level.isClientSide()) {
            BlockEntity be = level.getBlockEntity(pos);
            FusionCoreProxyBE proxy = (FusionCoreProxyBE) be;

            if (proxy.getCoreBE() instanceof FusionCoreBE)  {
                MenuProvider containerProvider = new MenuProvider() {
                    @Override
                    public Component getDisplayName() {
                        return Component.translatable("fusion_core");
                    }

                    @Override
                    public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player playerEntity) {
                        return new FusionCoreContainer(windowId, proxy.getCorePos(), playerInventory);
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
                if (t instanceof FusionBE tile) {
                    tile.tickClient();
                }
            };
        }
        return (lvl, pos, blockState, t)-> {
            if (t instanceof FusionBE tile) {
                tile.tickServer();
            }
        };
    }
}
