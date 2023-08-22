package igentuman.nc.item;

import igentuman.nc.setup.registration.CreativeTabs;
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class RadShieldingItem extends Item
{
	private int burnTime = -1;
	private boolean isHidden = false;

	private int radShieldingLevel = 0;

	public RadShieldingItem()
	{
		this(new Properties(), 0);
	}

	public RadShieldingItem(Properties props, int shieldLevel)
	{
		this(props, CreativeTabs.NC_ITEMS, shieldLevel);
	}

	public RadShieldingItem(Properties props, CreativeModeTab group, int radShieldingLevel)
	{
		super(props.tab(group));
		this.radShieldingLevel = radShieldingLevel;
	}

	public int getRadiationShieldingLevel()
	{
		return radShieldingLevel;
	}

	public RadShieldingItem setBurnTime(int burnTime)
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

	public boolean isIERepairable(@Nonnull ItemStack stack)
	{
		return super.isRepairable(stack);
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

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> list, TooltipFlag flag)
	{
		list.add(Component.translatable("tooltip.nc.shielding.desc").withStyle(net.minecraft.ChatFormatting.GRAY));
	}
}
