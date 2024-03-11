package igentuman.nc.item;

import igentuman.nc.setup.registration.CreativeTabs;
import igentuman.nc.util.TextUtils;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.block.Block;

import javax.annotation.Nonnull;
import java.util.List;

public class ProcessorBlockItem extends BlockItem
{
	public ProcessorBlockItem(Block pBlock, Properties props)
	{
		this(pBlock, props, CreativeTabs.NC_BLOCKS);
	}

	public ProcessorBlockItem(Block pBlock, Properties props, ItemGroup group)
	{
		super(pBlock, new Properties().tab(group));
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

	public boolean canEquip(ItemStack stack, EquipmentSlotType armorType, Entity entity)
	{
		return false;
	}

	@Override
	public void appendHoverText(ItemStack stack, @javax.annotation.Nullable World world, List<ITextComponent> list, ITooltipFlag flag)
	{
		if(stack.hasTag() && stack.getTag().contains("energy")) {
			list.add(new TranslationTextComponent("tooltip.nc.content_saved").withStyle(TextFormatting.GRAY));
		}
		if(asItem().toString().contains("empty") || this.asItem().equals(Items.AIR)) return;
		list.add(TextUtils.applyFormat(new TranslationTextComponent("processor.description."+toString()), TextFormatting.AQUA));
	}
}
