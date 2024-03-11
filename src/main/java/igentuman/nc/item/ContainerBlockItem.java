package igentuman.nc.item;

import igentuman.nc.setup.registration.CreativeTabs;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.block.Block;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ContainerBlockItem extends BlockItem
{
	public ContainerBlockItem(Block pBlock, Item.Properties props)
	{
		this(pBlock, props, CreativeTabs.NC_BLOCKS);
	}

	public ContainerBlockItem(Block pBlock, Properties props, ItemGroup group)
	{
		super(pBlock, new Properties().tab(group).stacksTo(1));
	}


	@Override
	public boolean isRepairable(@Nonnull ItemStack stack)
	{
		return false;
	}


	@Override
	public boolean isBookEnchantable(ItemStack stack, ItemStack book)
	{
		return false;
	}


	public String code()
	{
		return asItem().toString();
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> list, ITooltipFlag flag)
	{
		list.add(new TranslationTextComponent("tooltip.nc.content_saved").withStyle(TextFormatting.GRAY));
		list.add(new TranslationTextComponent("tooltip.nc.use_multitool").withStyle(TextFormatting.YELLOW));
	}

}
