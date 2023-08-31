package igentuman.nc.item;

import igentuman.nc.setup.registration.CreativeTabs;
import igentuman.nc.setup.storage.ContainerBlocks;
import igentuman.nc.util.CapabilityUtils;
import igentuman.nc.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import java.util.List;

public class ContainerBlockItem extends BlockItem
{
	public ContainerBlockItem(Block pBlock, Properties props)
	{
		this(pBlock, props, CreativeTabs.NC_BLOCKS);
	}

	public ContainerBlockItem(Block pBlock, Properties props, CreativeModeTab group)
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

	public boolean canEquip(ItemStack stack, EquipmentSlot armorType, Entity entity)
	{
		return false;
	}


	public String code()
	{
		return asItem().toString();
	}

	@Override
	public void appendHoverText(ItemStack stack, @javax.annotation.Nullable Level world, List<Component> list, TooltipFlag flag)
	{
		list.add(Component.translatable("tooltip.nc.content_saved").withStyle(ChatFormatting.GRAY));

	}

}
