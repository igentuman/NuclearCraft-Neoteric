package igentuman.nc.item;

import igentuman.nc.setup.registration.CreativeTabs;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

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
		this(props, CreativeTabs.NC_ITEMS);
	}

	public MultitoolItem(Properties props, ItemGroup group)
	{
		super(props.tab(group));
	}

	public MultitoolItem setBurnTime(int burnTime)
	{
		this.burnTime = burnTime;
		return this;
	}

	@Override
	public boolean doesSneakBypassUse(ItemStack stack, IWorldReader world, BlockPos pos, PlayerEntity player) {
		return true;
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



	@Override
	public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> list, ITooltipFlag flag)
	{
		list.add(new TranslationTextComponent("tooltip.nc.multitool.desc").withStyle(TextFormatting.YELLOW));
		list.add(new TranslationTextComponent("tooltip.nc.multitool.shift.desc").withStyle(TextFormatting.YELLOW));
	}
}
