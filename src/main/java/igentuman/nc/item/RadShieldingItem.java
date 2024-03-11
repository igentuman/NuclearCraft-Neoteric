package igentuman.nc.item;

import igentuman.nc.setup.registration.CreativeTabs;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class RadShieldingItem extends Item
{
	private int burnTime = -1;
	private boolean isHidden = false;

	private int radShieldingLevel = 0;

	public RadShieldingItem()
	{
		this(new Properties(), 0);
	}

	public RadShieldingItem(Properties props, int shieldLevel)
	{
		this(props, CreativeTabs.NC_ITEMS, shieldLevel);
	}

	public RadShieldingItem(Properties props, ItemGroup group, int radShieldingLevel)
	{
		super(props.tab(group));
		this.radShieldingLevel = radShieldingLevel;
	}

	public int getRadiationShieldingLevel()
	{
		return radShieldingLevel;
	}

	public RadShieldingItem setBurnTime(int burnTime)
	{
		this.burnTime = burnTime;
		return this;
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
		list.add(new TranslationTextComponent("tooltip.nc.shielding.desc").withStyle(TextFormatting.GRAY));
	}
}
