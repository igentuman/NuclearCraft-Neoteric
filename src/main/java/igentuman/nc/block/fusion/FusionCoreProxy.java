package igentuman.nc.block.fusion;

import igentuman.nc.block.entity.fusion.FusionBE;
import igentuman.nc.block.entity.fusion.FusionCoreBE;
import igentuman.nc.block.entity.fusion.FusionCoreProxyBE;
import igentuman.nc.container.FusionCoreContainer;
import net.minecraft.block.SoundType;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.stats.Stats;
import net.minecraft.inventory.Inventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.block.BlockState;
import net.minecraftforge.fml.network.NetworkHooks;
import javax.annotation.Nullable;

import java.util.Properties;


public class FusionCoreProxy extends FusionBlock {

    public FusionCoreProxy(Properties pProperties) {
        super(pProperties.sound(SoundType.METAL));
/*        this.registerDefaultState(
                this.stateDefinition.any()
        );*/
    }

    public String getCode()
    {
        return Registry.BLOCK.getKey(this).getPath();
    }

    @Override
    public void onRemove(BlockState pState, World pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        FusionCoreProxyBE be = (FusionCoreProxyBE) pLevel.getBlockEntity(pPos);
        be.destroyCore();
        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);

    }

    @Override
    public ActionResultType use(BlockState state, World level, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult result) {
        if (!level.isClientSide()) {
            TileEntity be = level.getBlockEntity(pos);
            FusionCoreProxyBE proxy = (FusionCoreProxyBE) be;

            if (proxy.getCoreBE() instanceof FusionCoreBE)  {
                INamedContainerProvider containerProvider = new INamedContainerProvider() {
                    @Override
                    public ITextComponent getDisplayName() {
                        return new TranslationTextComponent("fusion_core");
                    }

                    @Override
                    public Container createMenu(int windowId, PlayerInventory playerInventory, PlayerEntity playerEntity) {
                        return new FusionCoreContainer(windowId, proxy.getCorePos(), playerInventory);
                    }
                };
                NetworkHooks.openGui((ServerPlayerEntity) player, containerProvider, proxy.getCorePos());
            }
        }
        return ActionResultType.SUCCESS;
    }

    @Override
    public void playerDestroy(World pLevel, PlayerEntity pPlayer, BlockPos pPos, BlockState pState, @javax.annotation.Nullable TileEntity pBlockEntity, ItemStack pTool) {
        pPlayer.awardStat(Stats.BLOCK_MINED.get(this));
        pPlayer.causeFoodExhaustion(0.005F);
    }
}
