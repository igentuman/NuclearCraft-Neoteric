package igentuman.nc.item;

import igentuman.nc.setup.registration.CreativeTabs;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class NCBaseItem extends Item
{
	private int burnTime = -1;
	private boolean isHidden = false;

	public NCBaseItem()
	{
		this(new Properties());
	}

	public NCBaseItem(Properties props)
	{
		this(props, CreativeTabs.NC_ITEMS);
	}

	public NCBaseItem(Properties props, ItemGroup group)
	{
		super(props.tab(group));
	}

	public NCBaseItem setBurnTime(int burnTime)
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

}
