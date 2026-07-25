package igentuman.nc.item;

import igentuman.nc.config.Common;
import igentuman.nc.setup.Registers;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

import java.util.List;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.util.TextUtils.__;
import static igentuman.nc.util.TextUtils.formatEnergy;

public class HEVItem extends ArmorItem {

    public static final int MAX_QE_CHARGE = 10_000;
    private static final int MAX_BAR_WIDTH = 13;
    private static final ResourceLocation ARMOR_BONUS_ID = rl("hev_full_set_bonus");

    public HEVItem(Holder<ArmorMaterial> material, ArmorItem.Type type, Properties properties) {
        super(material, type, properties);
    }

    public static int getQeCharge(ItemStack stack) {
        return stack.getOrDefault(Registers.HEV_QE_CHARGE.get(), 0);
    }

    public static void setQeCharge(ItemStack stack, int qe) {
        stack.set(Registers.HEV_QE_CHARGE.get(), Mth.clamp(qe, 0, MAX_QE_CHARGE));
    }

    public static boolean isQeCharged(ItemStack stack) {
        return getQeCharge(stack) > 0;
    }

    public IEnergyStorage getEnergy(ItemStack stack) {
        return stack.getCapability(Capabilities.EnergyStorage.ITEM);
    }

    private boolean isFeCharged(ItemStack stack) {
        IEnergyStorage energy = getEnergy(stack);
        return energy != null && energy.getEnergyStored() > 0;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        IEnergyStorage energy = getEnergy(stack);
        float ratio = energy == null ? 0 : (float) energy.getEnergyStored() / (float) Common.HEV_ENERGY_STORAGE.get();
        return (int) Math.min(MAX_BAR_WIDTH, MAX_BAR_WIDTH * ratio);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return Mth.hsvToRgb(Math.max(0.0F, getBarWidth(stack) / (float) MAX_BAR_WIDTH) / 3.0F, 1.0F, 1.0F);
    }

    @Override
    public boolean isDamaged(ItemStack stack) {
        return false;
    }

    private static boolean allPiecesQeCharged(Player player) {
        for (ItemStack armor : player.getArmorSlots()) {
            if (!(armor.getItem() instanceof HEVItem) || !isQeCharged(armor)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!(entity instanceof Player player)) return;
        EquipmentSlot slot = getEquipmentSlot();
        if (!player.getItemBySlot(slot).equals(stack)) return;

        boolean isHelmet = slot == EquipmentSlot.HEAD;
        boolean isChest = slot == EquipmentSlot.CHEST;
        boolean isLegs = slot == EquipmentSlot.LEGS;
        boolean isBoots = slot == EquipmentSlot.FEET;

        if (isFeCharged(stack)) {
            if (isChest) player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 1, 1, false, false));
            if (isHelmet) player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 1, 1, false, false));
            if (isLegs) player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 1, 1, false, false));
        }

        if (level.isClientSide) return;

        long gameTime = level.getGameTime();
        if (gameTime % 20 == 0 && isQeCharged(stack)) {
            setQeCharge(stack, getQeCharge(stack) - 1);
        }

        boolean qeCharged = isQeCharged(stack);

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
                setQeCharge(stack, getQeCharge(stack) - 1);
            }

            AttributeInstance armorAttr = player.getAttribute(Attributes.ARMOR);
            if (armorAttr != null) {
                boolean allQeCharged = allPiecesQeCharged(player);
                boolean hasBonus = armorAttr.hasModifier(ARMOR_BONUS_ID);
                if (allQeCharged && !hasBonus) {
                    armorAttr.addTransientModifier(new AttributeModifier(ARMOR_BONUS_ID, 1.0, AttributeModifier.Operation.ADD_VALUE));
                } else if (!allQeCharged && hasBonus) {
                    armorAttr.removeModifier(ARMOR_BONUS_ID);
                }
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> list, TooltipFlag flag) {
        IEnergyStorage energy = getEnergy(stack);
        int stored = energy == null ? 0 : energy.getEnergyStored();
        list.add(__("tooltip.nuclearcraft.energy_stored",
                formatEnergy(stored), formatEnergy(Common.HEV_ENERGY_STORAGE.get())).withStyle(ChatFormatting.BLUE));
        list.add(__("tooltip.nuclearcraft.hev.qe_charge", getQeCharge(stack), MAX_QE_CHARGE).withStyle(ChatFormatting.GREEN));
        list.add(__("tooltip.nuclearcraft.hev.desc").withStyle(ChatFormatting.AQUA));
    }
}
