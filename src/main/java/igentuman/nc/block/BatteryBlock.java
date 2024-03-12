package igentuman.nc.block;

import igentuman.nc.NuclearCraft;
import igentuman.nc.block.entity.energy.BatteryBE;
import igentuman.nc.block.entity.energy.NCEnergy;
import igentuman.nc.network.toServer.BatterySideConfig;
import igentuman.nc.setup.registration.NCEnergyBlocks;
import igentuman.nc.util.TextUtils;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.Direction;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.stats.Stats;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import javax.annotation.Nullable;

import java.util.List;
import java.util.UUID;

import static igentuman.nc.handler.config.CommonConfig.ENERGY_STORAGE;
import static igentuman.nc.setup.registration.NCItems.MULTITOOL;

public class BatteryBlock extends Block {
    public BatteryBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public ActionResultType use(BlockState state, World level, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult result) {
        if (!level.isClientSide()) {
            TileEntity be = level.getBlockEntity(pos);
            if (be instanceof BatteryBE)  {
                if(player.getItemInHand(hand).getItem().equals(MULTITOOL.get())) {
                    Direction dirToChange = result.getDirection();
                    if(player.isShiftKeyDown()) {
                       dirToChange = dirToChange.getOpposite();
                    }
                    NuclearCraft.packetHandler().sendToServer(new BatterySideConfig(pos, dirToChange.ordinal()));
                } else {
                    player.sendMessage(new TranslationTextComponent("tooltip.nc.energy_stored", formatEnergy(((BatteryBE)be).energyStorage.getEnergyStored()), formatEnergy(((BatteryBE)be).energyStorage.getMaxEnergyStored())).withStyle(TextFormatting.BLUE), player.getUUID());
                }
            }
        }
        return ActionResultType.SUCCESS;
    }

    @Override
    public void onRemove(BlockState pState, World pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        if (pState.getBlock() != pNewState.getBlock()) {
            TileEntity blockEntity = pLevel.getBlockEntity(pPos);
            if (blockEntity instanceof BatteryBE) {

            }
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
        return NCEnergyBlocks.ENERGY_BE.get(code()).get().create();
    }


    public String code()
    {
        return asItem().toString();
    }


    @Override
    public void setPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(world, pos, state, placer, stack);

        if (stack.hasTag()) {
            BatteryBE tileEntity = (BatteryBE) world.getBlockEntity(pos);
            CompoundNBT nbtData = stack.getTag();
            tileEntity.load(state, nbtData);
        }
    }

    @Override
    public void playerDestroy(World pLevel, PlayerEntity pPlayer, BlockPos pPos, BlockState pState, @javax.annotation.Nullable TileEntity pBlockEntity, ItemStack pTool) {
        pPlayer.awardStat(Stats.BLOCK_MINED.get(this));
        pPlayer.causeFoodExhaustion(0.005F);
        BatteryBE batteryBE = (BatteryBE) pBlockEntity;
        CompoundNBT data = batteryBE.getUpdateTag();

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
        int storage = ENERGY_STORAGE.getCapacityFor(asItem().toString());

        list.add(new TranslationTextComponent("tooltip.nc.energy_capacity", formatEnergy(storage)).withStyle(TextFormatting.BLUE));
        list.add(new TranslationTextComponent("tooltip.nc.use_multitool").withStyle(TextFormatting.YELLOW));

    }

    public String formatEnergy(int energy)
    {
        if(energy >= 1000000000) {
            return TextUtils.numberFormat(energy/1000000000)+" GFE";
        }
        if(energy >= 1000000) {
            return TextUtils.numberFormat(energy/1000000)+" MFE";
        }
        if(energy >= 1000) {
            return TextUtils.numberFormat(energy/1000)+" kFE";
        }
        return TextUtils.numberFormat(energy)+" FE";
    }

}
