package igentuman.nc.item;

import igentuman.nc.util.capability.CustomEnergyStorage;
import igentuman.nc.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

import java.util.List;

import static igentuman.nc.setup.registration.NCItems.*;
import static igentuman.nc.util.TextUtils.__;

public class HEVItem extends ArmorItem {

    public HEVItem(Holder<ArmorMaterial> armorMaterial, ArmorItem.Type type, Properties hazmatProps) {
        super(armorMaterial, type, hazmatProps);
    }

    @Override
    public int getBarColor(ItemStack pStack)
    {
        return Mth.hsvToRgb(Math.max(0.0F, getBarWidth(pStack)/(float)MAX_BAR_WIDTH)/3.0F, 1.0F, 1.0F);
    }

    @Override
    public boolean isDamageable(ItemStack stack)
    {
        return false;
    }

    public int getEnergyMaxStorage() {
        return 1000000;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        CustomEnergyStorage energyStorage = getEnergy(stack);
        float chargeRatio = (float) energyStorage.getEnergyStored() / (float) getEnergyMaxStorage();
        return (int) Math.min(13, 13*chargeRatio);
    }

    @Override
    public void inventoryTick(ItemStack st, Level level, Entity entity, int slotId, boolean isSelected) {
        if (entity instanceof Player player && charged(st)) {
            if (st.is(HEV_CHEST.get()) && player.getItemBySlot(EquipmentSlot.CHEST).equals(st)) {
                player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 1, 1, false, false));
            }
            if (st.is(HEV_HELMET.get()) && player.getItemBySlot(EquipmentSlot.HEAD).equals(st)) {
                player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 1, 1, false, false));
            }
            if (st.is(HEV_PANTS.get()) && player.getItemBySlot(EquipmentSlot.LEGS).equals(st)) {
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 1, 1, false, false));
            }
        }
    }

    private boolean charged(ItemStack st) {
        return getEnergy(st).getEnergyStored() > 0;
    }

    public CustomEnergyStorage getEnergy(ItemStack stack)
    {
        IEnergyStorage storage = stack.getCapability(Capabilities.EnergyStorage.ITEM);
        return (CustomEnergyStorage) storage;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext pContext, List<Component> list, TooltipFlag flag)
    {
        list.add(__("tooltip.nc.energy_stored", formatEnergy(getEnergy(stack).getEnergyStored()), formatEnergy(getEnergyMaxStorage())).withStyle(ChatFormatting.BLUE));
        list.add(__("tooltip.nc.hev.desc").withStyle(ChatFormatting.AQUA));
    }

    public String formatEnergy(int energy)
    {
        return TextUtils.scaledFormat(energy)+" FE";
    }

    @Override
    public boolean isDamaged(ItemStack stack) {
        return false;
    }
}
