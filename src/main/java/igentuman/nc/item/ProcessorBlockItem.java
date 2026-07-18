package igentuman.nc.item;

import igentuman.nc.setup.registration.CreativeTabs;
import igentuman.nc.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nonnull;
import java.util.List;

import static igentuman.nc.block.entity.NuclearCraftBE.isGTEUCapEnabled;
import static igentuman.nc.handler.config.CommonConfig.GTCEU_CONFIG;
import static igentuman.nc.util.ModUtil.isGtLoaded;
import static igentuman.nc.util.TextUtils.__;

public class ProcessorBlockItem extends BlockItem
{
	public ProcessorBlockItem(Block pBlock, Properties props)
	{
		this(pBlock, props, CreativeTabs.NC_BLOCKS_TAB);
	}

	public ProcessorBlockItem(Block pBlock, Properties props, CreativeModeTab group)
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

	public boolean canEquip(ItemStack stack, EquipmentSlot armorType, Entity entity)
	{
		return false;
	}

	@Override
	public void appendHoverText(ItemStack stack, @javax.annotation.Nullable Level world, List<Component> list, TooltipFlag flag)
	{
		if(stack.hasTag() && stack.getTag().contains("energy")) {
			list.add(__("tooltip.nc.content_saved").withStyle(ChatFormatting.GRAY));
		}
		if(asItem().toString().contains("empty") || this.asItem().equals(Items.AIR)) return;
		if(isGtLoaded() && isGTEUCapEnabled()) {
			list.add(__("tooltip.nc.energy_base_eu_tier", GTCEU_CONFIG.PROCESSOR_ENERGY_TIER.get()).withStyle(ChatFormatting.GOLD));
		}
		list.add(TextUtils.applyFormat(__("processor.description."+toString()), ChatFormatting.AQUA));
	}
}
