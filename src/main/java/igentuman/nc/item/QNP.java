package igentuman.nc.item;

import igentuman.nc.setup.registration.CreativeTabs;
import igentuman.nc.util.CustomEnergyStorage;
import igentuman.nc.util.TextUtils;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.energy.IEnergyContainer;
import mekanism.common.util.StorageUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.List;

public class QNP extends PickaxeItem
{
	protected final CustomEnergyStorage energyStorage = createEnergy();

	public QNP(Tier pTier, int pAttackDamageModifier, float pAttackSpeedModifier, Properties pProperties) {
		super(pTier, pAttackDamageModifier, pAttackSpeedModifier, pProperties);
	}

	public LazyOptional<IEnergyStorage> getEnergy() {
		return energy;
	}

	protected final LazyOptional<IEnergyStorage> energy = LazyOptional.of(() -> energyStorage);

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
		return 1500000;
	}

	@Override
	public int getDamage(ItemStack stack)
	{
		float chargeRatio = (float) energyStorage.getEnergyStored() / (float) energyStorage.getMaxEnergyStored();
		return getMaxDamage() - (int) (getMaxDamage()*chargeRatio);
	}

	@Override
	public boolean isDamaged(ItemStack stack) {
		return false;
	}

	@Override
	public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
		return true;
	}
	@Override
	public void setDamage(ItemStack stack, int damage)
	{

	}

	@Override
	public boolean isBarVisible(ItemStack pStack) {
		return true;
	}

	public boolean mineBlock(@NotNull ItemStack stack, @NotNull Level world, @NotNull BlockState state, @NotNull BlockPos pos, @NotNull LivingEntity entityliving) {
		energyStorage.extractEnergy(100, false);
		return true;
	}

	@Override
	public float getDestroySpeed(@NotNull ItemStack stack, @NotNull BlockState state) {
		if(energyStorage.getEnergyStored() > 100) return getTier().getSpeed();
		return 0.1F;
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
	public void appendHoverText(ItemStack stack, @javax.annotation.Nullable Level world, List<Component> list, TooltipFlag flag)
	{
		list.add(Component.translatable("tooltip.nc.energy_stored", formatEnergy(energyStorage.getEnergyStored()), formatEnergy(energyStorage.getMaxEnergyStored())).withStyle(ChatFormatting.BLUE));
	}

	public String formatEnergy(int energy)
	{
		return TextUtils.numberFormat(energy/1000)+" KFE";
	}
}
