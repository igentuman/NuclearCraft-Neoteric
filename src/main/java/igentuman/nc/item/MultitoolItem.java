package igentuman.nc.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static igentuman.nc.compat.mekanism.MekInteractions.handleMultitoolInteractionWithMek;
import static igentuman.nc.util.ModUtil.isMekanismLoaded;
import static igentuman.nc.util.TextUtils.__;

public class MultitoolItem extends Item
{
	private int burnTime = -1;
	private boolean isHidden = false;

	public MultitoolItem()
	{
		this(new Properties());
	}

	public MultitoolItem(Properties props)
	{
		super(props);
	}

	public MultitoolItem setBurnTime(int burnTime)
	{
		this.burnTime = burnTime;
		return this;
	}

	@Override
	public boolean doesSneakBypassUse(ItemStack stack, net.minecraft.world.level.LevelReader world, BlockPos pos, Player player)
	{
		return true;
	}

	@Override
	public int getBurnTime(ItemStack itemStack, RecipeType<?> type)
	{
		return burnTime;
	}

	public boolean isHidden()
	{
		return isHidden;
	}

	@Override
	public boolean isRepairable(@Nonnull ItemStack stack)
	{
		return false;
	}

	public boolean isIERepairable(@Nonnull ItemStack stack)
	{
		return super.isRepairable(stack);
	}

	@Override
	public boolean isBookEnchantable(ItemStack stack, ItemStack book)
	{
		return false;
	}

	public boolean canEquip(ItemStack stack, EquipmentSlot armorType, Entity entity)
	{
		return false;
	}

	@Override
	public int getBarColor(ItemStack pStack)
	{
		return Mth.hsvToRgb(Math.max(0.0F, getBarWidth(pStack)/(float)MAX_BAR_WIDTH)/3.0F, 1.0F, 1.0F);
	}

	@NotNull
	@Override
	public InteractionResult useOn(UseOnContext context) {
		Player player = context.getPlayer();
		Level world = context.getLevel();
		if(player == null || world.isClientSide()) {
			return super.useOn(context);
		}
		BlockPos pos = context.getClickedPos();
		Direction side = context.getClickedFace();
		BlockEntity be = world.getExistingBlockEntity(pos);
		if(isMekanismLoaded() && handleMultitoolInteractionWithMek(be, player, side)) {
			return InteractionResult.SUCCESS;
		}
		return super.useOn(context);
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> list, TooltipFlag flag)
	{
		list.add(__("tooltip.nc.multitool.desc").withStyle(ChatFormatting.YELLOW));
		list.add(__("tooltip.nc.multitool.shift.desc").withStyle(ChatFormatting.YELLOW));
	}
}
