package igentuman.nc.item;

import igentuman.nc.handler.ItemEnergyHandler;
import igentuman.nc.setup.registration.CreativeTabs;
import igentuman.nc.util.CapabilityUtils;
import igentuman.nc.util.CustomEnergyStorage;
import igentuman.nc.util.TextUtils;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static igentuman.nc.handler.config.CommonConfig.ENERGY_STORAGE;
import static net.minecraftforge.energy.CapabilityEnergy.ENERGY;

public class BatteryBlockItem extends BlockItem
{
	public BatteryBlockItem(Block pBlock, Properties props)
	{
		this(pBlock, props, CreativeTabs.NC_BLOCKS);
	}

	public BatteryBlockItem(Block pBlock, Properties props, ItemGroup group)
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

	public boolean canEquip(ItemStack stack, EquipmentSlotType armorType, Entity entity)
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
	}*/


	@Override
	public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> list, ITooltipFlag flag)
	{
		list.add(new TranslationTextComponent("tooltip.nc.energy_stored", formatEnergy(getEnergy(stack).getEnergyStored()), formatEnergy(getEnergy(stack).getMaxEnergyStored())).withStyle(TextFormatting.BLUE));
		list.add(new TranslationTextComponent("tooltip.nc.use_multitool").withStyle(TextFormatting.YELLOW));
	}

	public String formatEnergy(int energy)
	{
		if(energy >= 1000000000) {
			return TextUtils.numberFormat(energy/1000000000)+" GFE";
		}
		if(energy >= 1000000) {
			return TextUtils.numberFormat(energy/1000000)+" MFE";
		}
		if(energy >= 1000) {
			return TextUtils.numberFormat(energy/1000)+" kFE";
		}
		return TextUtils.numberFormat(energy)+" FE";
	}
}
