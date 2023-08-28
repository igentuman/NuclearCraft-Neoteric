package igentuman.nc.item;

import igentuman.nc.handler.ItemEnergyHandler;
import igentuman.nc.setup.registration.CreativeTabs;
import igentuman.nc.util.CapabilityUtils;
import igentuman.nc.util.CustomEnergyStorage;
import igentuman.nc.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nonnull;
import java.util.List;

import static igentuman.nc.handler.config.CommonConfig.ENERGY_STORAGE;

public class BatteryBlockItem extends BlockItem
{
	public BatteryBlockItem(Block pBlock, Properties props)
	{
		this(pBlock, props, CreativeTabs.NC_BLOCKS);
	}

	public BatteryBlockItem(Block pBlock, Properties props, CreativeModeTab group)
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
	public int getBarColor(ItemStack pStack)
	{
		return Mth.hsvToRgb(Math.max(0.0F, getBarWidth(pStack)/(float)MAX_BAR_WIDTH)/3.0F, 1.0F, 1.0F);
	}


	protected int getEnergyMaxStorage() {
		return ENERGY_STORAGE.getCapacityFor(toString());
	}

	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt) {
		return new ItemEnergyHandler(stack, getEnergyMaxStorage(), getEnergyMaxStorage(), getEnergyMaxStorage());
	}

	public CustomEnergyStorage getEnergy(ItemStack stack)
	{
		return (CustomEnergyStorage) CapabilityUtils.getPresentCapability(stack, ForgeCapabilities.ENERGY);
	}

	@Override
	public int getBarWidth(ItemStack stack) {
		CustomEnergyStorage energyStorage = getEnergy(stack);
		float chargeRatio = (float) energyStorage.getEnergyStored() / (float) getEnergyMaxStorage();
		return (int) Math.min(13, 13*chargeRatio);
	}


	@Override
	public void appendHoverText(ItemStack stack, @javax.annotation.Nullable Level world, List<Component> list, TooltipFlag flag)
	{
		list.add(Component.translatable("tooltip.nc.energy_stored", formatEnergy(getEnergy(stack).getEnergyStored()), formatEnergy(getEnergy(stack).getMaxEnergyStored())).withStyle(ChatFormatting.BLUE));
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
