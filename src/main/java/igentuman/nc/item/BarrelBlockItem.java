package igentuman.nc.item;

import igentuman.nc.content.storage.BarrelBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

import javax.annotation.Nonnull;
import java.util.List;

import static igentuman.nc.util.TextUtils.__;
import static igentuman.nc.util.TextUtils.formatLiquid;

public class BarrelBlockItem extends BlockItem
{

	public BarrelBlockItem(Block pBlock, Properties props)
	{
		super(pBlock, new Properties().stacksTo(1));
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


	public int getCapacity() {
		return BarrelBlocks.all().get(code()).config().getCapacity();
	}

	public IFluidHandlerItem getFluid(ItemStack stack)
	{
		return stack.getCapability(Capabilities.FluidHandler.ITEM);
	}

	public String code()
	{
		return asItem().toString();
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext pContext, List<Component> list, TooltipFlag flag)
	{
		int storage = BarrelBlocks.all().get(code()).config().getCapacity() * 1000;
		FluidStack fluid = getFluid(stack).getFluidInTank(0);
		if(fluid == null || fluid.isFluidEqual(FluidStack.EMPTY)) {
			list.add(__("tooltip.nc.liquid_capacity", formatLiquid(storage)).withStyle(ChatFormatting.BLUE));
		} else {
			list.add(__("tooltip.nc.liquid_stored", fluid.getDisplayName(), formatLiquid(fluid.getAmount()), formatLiquid(storage)).withStyle(ChatFormatting.BLUE));
		}
		list.add(__("tooltip.nc.use_multitool").withStyle(ChatFormatting.YELLOW));
	}
}
