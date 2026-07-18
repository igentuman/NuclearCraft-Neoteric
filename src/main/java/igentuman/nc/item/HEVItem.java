package igentuman.nc.item;

import igentuman.nc.content.ArmorMaterials;
import igentuman.nc.handler.ItemEnergyHandler;
import igentuman.nc.util.capability.CapabilityUtils;
import igentuman.nc.util.capability.CustomEnergyStorage;
import igentuman.nc.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

import static igentuman.nc.setup.registration.NCItems.*;
import static igentuman.nc.util.TextUtils.__;

public class HEVItem extends ArmorItem {

    public static final String TAG_QE_CHARGE = "hev_qe_charge";
    public static final int MAX_QE_CHARGE = 10_000;

    private static final UUID ARMOR_BONUS_UUID = UUID.fromString("4d76dc73-b7c5-4e95-b8e4-9f5af13be05c");

    public HEVItem(ArmorMaterials armorMaterials, EquipmentSlot slot, Properties hazmatProps) {
        super(armorMaterials, slot, hazmatProps);
    }

    public static int getQECharge(ItemStack stack) {
        if (!stack.hasTag() || !stack.getTag().contains(TAG_QE_CHARGE)) {
            return 0;
        }
        return stack.getTag().getInt(TAG_QE_CHARGE);
    }

    public static void setQECharge(ItemStack stack, int qe) {
        stack.getOrCreateTag().putInt(TAG_QE_CHARGE, Mth.clamp(qe, 0, MAX_QE_CHARGE));
    }

    public static void drainQE(ItemStack stack, int amount) {
        setQECharge(stack, getQECharge(stack) - amount);
    }

    public static boolean isQECharged(ItemStack stack) {
        return getQECharge(stack) > 0;
    }

    @Override
    public int getBarColor(ItemStack pStack) {
        return Mth.hsvToRgb(Math.max(0.0F, getBarWidth(pStack) / (float) MAX_BAR_WIDTH) / 3.0F, 1.0F, 1.0F);
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return false;
    }

    protected int getEnergyMaxStorage() {
        return 1000000;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        CustomEnergyStorage energyStorage = getEnergy(stack);
        float chargeRatio = (float) energyStorage.getEnergyStored() / (float) getEnergyMaxStorage();
        return (int) Math.min(13, 13 * chargeRatio);
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt) {
        return new ItemEnergyHandler(stack, getEnergyMaxStorage(), 5000, getEnergyMaxStorage() / 4);
    }

    private static boolean allPiecesQECharged(Player player) {
        for (ItemStack armor : player.getArmorSlots()) {
            if (!(armor.getItem() instanceof HEVItem) || !isQECharged(armor)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, net.minecraft.world.entity.Entity entity, int slotIndex, boolean selectedSlot) {
        if (!(entity instanceof Player player)) return;
        long gameTime = level.getGameTime();
        boolean isHelmet = stack.is(HEV_HELMET.get()) && player.getItemBySlot(EquipmentSlot.HEAD).equals(stack);
        boolean isChest = stack.is(HEV_CHEST.get()) && player.getItemBySlot(EquipmentSlot.CHEST).equals(stack);
        boolean isLegs = stack.is(HEV_PANTS.get()) && player.getItemBySlot(EquipmentSlot.LEGS).equals(stack);
        boolean isBoots = stack.is(HEV_BOOTS.get()) && player.getItemBySlot(EquipmentSlot.FEET).equals(stack);

        if (fECharged(stack)) {
            if (isChest) {
                player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 1, 1, false, false));
            }
            if (isHelmet) {
                player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 1, 1, false, false));
            }
            if (isLegs) {
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 1, 1, false, false));
            }
        }

        if (level.isClientSide) return;

        if (!isHelmet && !isChest && !isLegs && !isBoots) return;

        if (gameTime % 20 == 0 && isQECharged(stack)) {
            drainQE(stack, 1);
        }

        boolean qeCharged = isQECharged(stack);

        if (isHelmet && qeCharged) {
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 220, 0, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 1, 0, false, false));
        }

        if (isBoots && qeCharged) {
            player.addEffect(new MobEffectInstance(MobEffects.JUMP, 1, 1, false, false));
        }

        if (isChest) {
            boolean shouldFly = qeCharged;
            if (player.getAbilities().mayfly != shouldFly) {
                player.getAbilities().mayfly = shouldFly;
                if (!shouldFly && player.getAbilities().flying) {
                    player.getAbilities().flying = false;
                }
                if (player instanceof ServerPlayer sp) {
                    sp.onUpdateAbilities();
                }
            }
            if (qeCharged && player.getAbilities().flying && gameTime % 20 == 0) {
                drainQE(stack, 1);
            }

            boolean allQECharged = allPiecesQECharged(player);

            AttributeInstance armorAttr = player.getAttribute(Attributes.ARMOR);
            if (armorAttr != null) {
                boolean hasBonus = armorAttr.getModifier(ARMOR_BONUS_UUID) != null;
                if (allQECharged && !hasBonus) {
                    armorAttr.addTransientModifier(new AttributeModifier(ARMOR_BONUS_UUID, "HEV Full Set Bonus", 1.0, AttributeModifier.Operation.ADDITION));
                } else if (!allQECharged && hasBonus) {
                    armorAttr.removeModifier(ARMOR_BONUS_UUID);
                }
            }

            if (allQECharged) {
                stack.getOrCreateTag().putInt("rad_shielding", 1);
            } else {
                stack.getOrCreateTag().remove("rad_shielding");
            }
        }
    }

    private boolean fECharged(ItemStack st) {
        return getEnergy(st).getEnergyStored() > 0;
    }

    public CustomEnergyStorage getEnergy(ItemStack stack) {
        return (CustomEnergyStorage) CapabilityUtils.getPresentCapability(stack, ForgeCapabilities.ENERGY);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> list, TooltipFlag flag) {
        list.add(__("tooltip.nc.energy_stored", formatEnergy(getEnergy(stack).getEnergyStored()), formatEnergy(getEnergyMaxStorage())).withStyle(ChatFormatting.BLUE));
        list.add(__("tooltip.nc.q36_charge", getQECharge(stack), MAX_QE_CHARGE).withStyle(ChatFormatting.GREEN));
        list.add(__("tooltip.nc.hev.desc").withStyle(ChatFormatting.AQUA));
    }

    public String formatEnergy(int energy) {
        return TextUtils.scaledFormat(energy) + " FE";
    }

    @Override
    public boolean isDamaged(ItemStack stack) {
        return false;
    }
}
