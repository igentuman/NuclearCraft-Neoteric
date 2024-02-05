package igentuman.nc.item;

import igentuman.nc.setup.registration.CreativeTabs;
import igentuman.nc.content.storage.BarrelBlocks;
import igentuman.nc.util.CapabilityUtils;
import igentuman.nc.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;

import javax.annotation.Nonnull;
import java.util.List;

public class BarrelBlockItem extends BlockItem
{
	public BarrelBlockItem(Block pBlock, Properties props)
	{
		this(pBlock, props, CreativeTabs.NC_BLOCKS);
	}

	public BarrelBlockItem(Block pBlock, Properties props, CreativeModeTab group)
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


	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt) {
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

	@Override
	public void appendHoverText(ItemStack stack, @javax.annotation.Nullable Level world, List<Component> list, TooltipFlag flag)
	{
		int storage = BarrelBlocks.all().get(code()).config().getCapacity();
		FluidStack fluid = getFluid(stack).getFluidInTank(0);
		if(fluid == null || fluid.isFluidEqual(FluidStack.EMPTY)) {
			list.add(new TranslatableComponent("tooltip.nc.liquid_empty", formatLiquid(storage)).withStyle(ChatFormatting.BLUE));
		} else {
			list.add(new TranslatableComponent("tooltip.nc.liquid_stored", fluid.getDisplayName(), formatLiquid(fluid.getAmount()), formatLiquid(storage)).withStyle(ChatFormatting.BLUE));
		}
		list.add(new TranslatableComponent("tooltip.nc.use_multitool").withStyle(ChatFormatting.YELLOW));
	}

	public String formatLiquid(int val)
	{
		return TextUtils.numberFormat(val/1000)+" B";
	}

}
