package igentuman.nc.item;

import igentuman.nc.setup.registration.CreativeTabs;
import igentuman.nc.content.storage.BarrelBlocks;
import igentuman.nc.util.CapabilityUtils;
import igentuman.nc.util.TextUtils;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.item.ItemStack;
import net.minecraft.block.Block;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;

import javax.annotation.Nonnull;

public class BarrelBlockItem extends BlockItem
{
	public BarrelBlockItem(Block pBlock, Properties props)
	{
		this(pBlock, props, CreativeTabs.NC_BLOCKS);
	}

	public BarrelBlockItem(Block pBlock, Properties props, ItemGroup group)
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


	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, CompoundNBT nbt) {
		return new FluidHandlerItemStack(stack, getCapacity());
	}

	public int getCapacity() {
		return BarrelBlocks.all().get(code()).config().getCapacity();
	}

	public IFluidHandlerItem getFluid(ItemStack stack)
	{
		return CapabilityUtils.getPresentCapability(stack, CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY);
	}

	public String code()
	{
		return asItem().toString();
	}

/*	@Override
	public void appendHoverText(ItemStack stack, @javax.annotation.Nullable World world, List<TextComponent> list, TooltipFlag flag)
	{
		int storage = BarrelBlocks.all().get(code()).config().getCapacity();
		FluidStack fluid = getFluid(stack).getFluidInTank(0);
		if(fluid == null || fluid.isFluidEqual(FluidStack.EMPTY)) {
			list.add(new TranslationTextComponent("tooltip.nc.liquid_empty", formatLiquid(storage)).withStyle(TextFormatting.BLUE));
		} else {
			list.add(new TranslationTextComponent("tooltip.nc.liquid_stored", fluid.getDisplayName(), formatLiquid(fluid.getAmount()), formatLiquid(storage)).withStyle(TextFormatting.BLUE));
		}
		list.add(new TranslationTextComponent("tooltip.nc.use_multitool").withStyle(TextFormatting.YELLOW));
	}*/

	public String formatLiquid(int val)
	{
		return TextUtils.numberFormat(val/1000)+" B";
	}

}
