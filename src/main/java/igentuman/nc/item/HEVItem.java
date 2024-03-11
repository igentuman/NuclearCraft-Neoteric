package igentuman.nc.item;

import igentuman.nc.handler.ItemEnergyHandler;
import igentuman.nc.util.CapabilityUtils;
import igentuman.nc.util.CustomEnergyStorage;
import igentuman.nc.util.TextUtils;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nullable;
import java.util.List;

import static igentuman.nc.setup.registration.NCItems.*;
import static net.minecraftforge.energy.CapabilityEnergy.ENERGY;

public class HEVItem extends ArmorItem {
    public HEVItem(IArmorMaterial armorMaterials, EquipmentSlotType equipmentSlot, Properties hazmatProps) {
        super(armorMaterials, equipmentSlot, hazmatProps);
    }

/*    @Override
    public int getBarColor(ItemStack pStack)
    {
        return MathHelper.hsvToRgb(Math.max(0.0F, getBarWidth(pStack)/(float)MAX_BAR_WIDTH)/3.0F, 1.0F, 1.0F);
    }*/

    @Override
    public boolean isDamageable(ItemStack stack)
    {
        return false;
    }

    protected int getEnergyMaxStorage() {
        return 1000000;
    }

/*    @Override
    public int getBarWidth(ItemStack stack) {
        CustomEnergyStorage energyStorage = getEnergy(stack);
        float chargeRatio = (float) energyStorage.getEnergyStored() / (float) getEnergyMaxStorage();
        return (int) Math.min(13, 13*chargeRatio);
    }*/

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, CompoundNBT nbt) {
        return new ItemEnergyHandler(stack, getEnergyMaxStorage(), 5000, getEnergyMaxStorage()/4);
    }

    @Override
    public void onArmorTick(ItemStack st, World level, PlayerEntity player) {
        if(charged(st)) {
            if(st.getItem().equals(HEV_CHEST.get())) {
                player.addEffect(new EffectInstance(Effects.ABSORPTION, 1, 1, false, false));
            }
            if(st.getItem().equals(HEV_HELMET.get())) {
                player.addEffect(new EffectInstance(Effects.WATER_BREATHING, 1, 1, false, false));
            }
            if(st.getItem().equals(HEV_PANTS.get())) {
                player.addEffect(new EffectInstance(Effects.MOVEMENT_SPEED, 1, 1, false, false));
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
    public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> list, ITooltipFlag flag)
    {
        list.add(new TranslationTextComponent("tooltip.nc.energy_stored", formatEnergy(getEnergy(stack).getEnergyStored()), formatEnergy(getEnergyMaxStorage())).withStyle(TextFormatting.BLUE));
        list.add(new TranslationTextComponent("tooltip.nc.hev.desc").withStyle(TextFormatting.AQUA));
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
