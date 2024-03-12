package igentuman.nc.block;

import igentuman.nc.NuclearCraft;
import igentuman.nc.block.entity.ContainerBE;
import igentuman.nc.container.StorageContainerContainer;
import igentuman.nc.network.toServer.StorageSideConfig;
import igentuman.nc.setup.registration.NCStorageBlocks;
import igentuman.nc.util.annotation.NothingNullByDefault;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.Direction;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.stats.Stats;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.inventory.Inventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;

import java.util.List;

import static igentuman.nc.setup.registration.NCItems.MULTITOOL;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.network.NetworkHooks;

@NothingNullByDefault
public class ContainerBlock extends Block {

    public ContainerBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public ActionResultType use(BlockState state, World level, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult result) {
        if (!level.isClientSide()) {
            ContainerBE be = (ContainerBE)level.getBlockEntity(pos);
            ItemStack handStack = player.getItemInHand(hand);
            if(handStack.getItem().equals(MULTITOOL.get())) {
                Direction dirToChange = result.getDirection();
                if(player.isShiftKeyDown()) {
                    dirToChange = dirToChange.getOpposite();
                }
                NuclearCraft.packetHandler().sendToServer(new StorageSideConfig(pos, dirToChange.ordinal()));
            } else {
                INamedContainerProvider containerProvider = new INamedContainerProvider() {
                    @Override
                    public ITextComponent getDisplayName() {
                        return new TranslationTextComponent("container.nc.storage");
                    }

                    @Override
                    public Container createMenu(int windowId, PlayerInventory playerInventory, PlayerEntity playerEntity) {
                        return new StorageContainerContainer(windowId, pos, playerInventory);
                    }
                };
                NetworkHooks.openGui((ServerPlayerEntity) player, containerProvider, be.getBlockPos());
            }
        }
        return ActionResultType.SUCCESS;
    }

    @Override
    public void onRemove(BlockState pState, World pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        if (pState.getBlock() != pNewState.getBlock()) {
            TileEntity blockEntity = pLevel.getBlockEntity(pPos);

        }
        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return NCStorageBlocks.STORAGE_BE.get(state.getBlock().asItem().toString()).get().create();
    }


    public String code()
    {
        return asItem().toString();
    }

    @Override
    public void setPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(world, pos, state, placer, stack);

        if (stack.hasTag()) {
            ContainerBE tileEntity = (ContainerBE) world.getBlockEntity(pos);
            CompoundNBT nbtData = stack.getTag();
            tileEntity.load(state, nbtData);
        }
    }

    @Override
    public void playerDestroy(World pLevel, PlayerEntity pPlayer, BlockPos pPos, BlockState pState, @javax.annotation.Nullable TileEntity pBlockEntity, ItemStack pTool) {
        pPlayer.awardStat(Stats.BLOCK_MINED.get(this));
        pPlayer.causeFoodExhaustion(0.005F);
        ContainerBE ContainerBE = (ContainerBE) pBlockEntity;
        CompoundNBT data = ContainerBE.getUpdateTag();

        ItemStack drop = new ItemStack(this);
        drop.setTag(data);
        if (!pLevel.isClientSide()) {
            ItemEntity itemEntity = new ItemEntity(pLevel, pPos.getX(), pPos.getY(), pPos.getZ(), drop);
            itemEntity.setDefaultPickUpDelay();
            pLevel.addFreshEntity(itemEntity);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @javax.annotation.Nullable IBlockReader world, List<ITextComponent> list, ITooltipFlag flag)
    {
        list.add(new TranslationTextComponent("tooltip.nc.use_multitool").withStyle(TextFormatting.YELLOW));
    }

}
