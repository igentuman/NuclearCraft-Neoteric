package igentuman.nc.item;

import igentuman.nc.setup.registration.CreativeTabs;
import igentuman.nc.util.CustomEnergyStorage;
import igentuman.nc.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.List;

public class BatteryItem extends Item
{
	private int burnTime = -1;
	private boolean isHidden = false;

	public BatteryItem()
	{
		this(new Properties());
	}

	protected final CustomEnergyStorage energyStorage = createEnergy();

	public LazyOptional<IEnergyStorage> getEnergy() {
		return energy;
	}

	protected final LazyOptional<IEnergyStorage> energy = LazyOptional.of(() -> energyStorage);

	public BatteryItem(Properties props)
	{
		this(props, CreativeTabs.NC_ITEMS);
	}

	public BatteryItem(Properties props, CreativeModeTab group)
	{
		super(new Item.Properties().tab(group).stacksTo(1));
	}

	public BatteryItem setBurnTime(int burnTime)
	{
		this.burnTime = burnTime;
		return this;
	}

	@Override
	public int getBurnTime(ItemStack itemStack, RecipeType<?> type)
	{
		return burnTime;
	}

	public boolean isHidden()
	{
		return isHidden;
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

	private CustomEnergyStorage createEnergy() {
		return new CustomEnergyStorage(getEnergyMaxStorage(), getEnergyMaxStorage(), getEnergyMaxStorage());
	}

	protected int getEnergyMaxStorage() {
		return 500000;
	}

	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt) {
		return new ICapabilityProvider() {
			@Override
			public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
				if (cap == ForgeCapabilities.ENERGY) {
					return energy.cast();
				}
				return LazyOptional.empty();
			}
		};
	}

	@Override
	public int getDamage(ItemStack stack)
	{
		float chargeRatio = (float) energyStorage.getEnergyStored() / (float) energyStorage.getMaxEnergyStored();
		return getMaxDamage() - (int) (getMaxDamage()*chargeRatio);
	}

	@Override
	public void appendHoverText(ItemStack stack, @javax.annotation.Nullable Level world, List<Component> list, TooltipFlag flag)
	{
		list.add(Component.translatable("tooltip.nc.energy_stored", formatEnergy(energyStorage.getEnergyStored()), formatEnergy(energyStorage.getMaxEnergyStored())).withStyle(ChatFormatting.BLUE));
	}

	public String formatEnergy(int energy)
	{
		return TextUtils.numberFormat(energy/1000)+" KFE";
	}
}
