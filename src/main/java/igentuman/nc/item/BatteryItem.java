package igentuman.nc.item;

import igentuman.nc.handler.ItemEnergyHandler;
import igentuman.nc.util.CapabilityUtils;
import igentuman.nc.util.CustomEnergyStorage;
import igentuman.nc.util.TextUtils;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nonnull;

import static igentuman.nc.handler.config.CommonConfig.ENERGY_STORAGE;
import static net.minecraftforge.energy.CapabilityEnergy.ENERGY;

public class BatteryItem extends Item
{

	public BatteryItem(Properties props)
	{
		super(props);
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

/*	@Override
	public int getBarColor(ItemStack pStack)
	{
		return MathHelper.hsvToRgb(Math.max(0.0F, getBarWidth(pStack)/(float)MAX_BAR_WIDTH)/3.0F, 1.0F, 1.0F);
	}*/


	protected int getEnergyMaxStorage() {
		return ENERGY_STORAGE.getCapacityFor(toString());
	}

	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, CompoundNBT nbt) {
		return new ItemEnergyHandler(stack, getEnergyMaxStorage(), getEnergyMaxStorage(), getEnergyMaxStorage());
	}

	public CustomEnergyStorage getEnergy(ItemStack stack)
	{
		return (CustomEnergyStorage) CapabilityUtils.getPresentCapability(stack, ENERGY);
	}

/*
	@Override
	public int getBarWidth(ItemStack stack) {
		CustomEnergyStorage energyStorage = getEnergy(stack);
		float chargeRatio = (float) energyStorage.getEnergyStored() / (float) getEnergyMaxStorage();
		return (int) Math.min(13, 13*chargeRatio);
	}
*/


/*	@Override
	public void appendHoverText(ItemStack stack, @javax.annotation.Nullable World world, List<TextComponent> list, TooltipFlag flag)
	{
		list.add(new TranslationTextComponent("tooltip.nc.energy_stored", formatEnergy(getEnergy(stack).getEnergyStored()), formatEnergy(getEnergy(stack).getMaxEnergyStored())).withStyle(TextFormatting.BLUE));
	}*/

	public String formatEnergy(int energy)
	{
		if(energy >= 1000000000) {
			return TextUtils.numberFormat((double) energy /1000000000)+" GFE";
		}
		if(energy >= 1000000) {
			return TextUtils.numberFormat((double) energy /1000000)+" MFE";
		}
		if(energy >= 1000) {
			return TextUtils.numberFormat((double) energy /1000)+" kFE";
		}
		return TextUtils.numberFormat(energy)+" FE";
	}
}
