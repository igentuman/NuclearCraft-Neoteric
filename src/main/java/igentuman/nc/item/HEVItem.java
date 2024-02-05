package igentuman.nc.item;

import igentuman.nc.content.ArmorMaterials;
import igentuman.nc.handler.ItemEnergyHandler;
import igentuman.nc.util.CapabilityUtils;
import igentuman.nc.util.CustomEnergyStorage;
import igentuman.nc.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import java.util.List;

import static igentuman.nc.setup.registration.NCItems.*;
import static net.minecraftforge.energy.CapabilityEnergy.ENERGY;

public class HEVItem extends ArmorItem {
    public HEVItem(ArmorMaterials armorMaterials, EquipmentSlot equipmentSlot, Properties hazmatProps) {
        super(armorMaterials, equipmentSlot, hazmatProps);
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

    protected int getEnergyMaxStorage() {
        return 1000000;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        CustomEnergyStorage energyStorage = getEnergy(stack);
        float chargeRatio = (float) energyStorage.getEnergyStored() / (float) getEnergyMaxStorage();
        return (int) Math.min(13, 13*chargeRatio);
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt) {
        return new ItemEnergyHandler(stack, getEnergyMaxStorage(), 5000, getEnergyMaxStorage()/4);
    }

    @Override
    public void onArmorTick(ItemStack st, Level level, Player player) {
        if(charged(st)) {
            if(st.getItem().equals(HEV_CHEST.get())) {
                player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 1, 1, false, false));
            }
            if(st.getItem().equals(HEV_HELMET.get())) {
                player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 1, 1, false, false));
            }
            if(st.getItem().equals(HEV_PANTS.get())) {
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 1, 1, false, false));
            }
        }
    }

    private boolean charged(ItemStack st) {
        return getEnergy(st).getEnergyStored() > 0;
    }

    public CustomEnergyStorage getEnergy(ItemStack stack)
    {
        return (CustomEnergyStorage) CapabilityUtils.getPresentCapability(stack, ENERGY);
    }

    @Override
    public void appendHoverText(ItemStack stack, @javax.annotation.Nullable Level world, List<Component> list, TooltipFlag flag)
    {
        list.add(new TranslatableComponent("tooltip.nc.energy_stored", formatEnergy(getEnergy(stack).getEnergyStored()), formatEnergy(getEnergyMaxStorage())).withStyle(ChatFormatting.BLUE));
        list.add(new TranslatableComponent("tooltip.nc.hev.desc").withStyle(ChatFormatting.AQUA));
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
