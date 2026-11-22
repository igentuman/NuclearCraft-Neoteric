package igentuman.nc.item;

import igentuman.nc.config.Common;
import igentuman.nc.setup.Registers;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.util.TextUtils.__;
import static igentuman.nc.util.TextUtils.formatEnergy;

/** Powered armor with independent FE and quantite charges and effects while worn. */
public class HEVItem extends Item {

    public static final int MAX_QE_CHARGE = 10_000;
    private static final int MAX_BAR_WIDTH = 13;
    private static final Identifier ARMOR_BONUS_ID = rl("hev_full_set_bonus");
    private static final Identifier FLIGHT_ID = rl("hev_flight");

    private final EquipmentSlot armorSlot;

    public HEVItem(ArmorMaterial material, ArmorType type, Properties properties) {
        super(properties.humanoidArmor(material, type));
        this.armorSlot = type.getSlot();
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

    public EnergyHandler getEnergy(ItemStack stack) {
        return stack.isEmpty() ? null : ItemAccess.forStack(stack).getCapability(Capabilities.Energy.ITEM);
    }

    private boolean isFeCharged(ItemStack stack) {
        EnergyHandler energy = getEnergy(stack);
        return energy != null && energy.getAmountAsInt() > 0;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        EnergyHandler energy = getEnergy(stack);
        float ratio = energy == null ? 0 : (float) energy.getAmountAsInt() / (float) Common.HEV_ENERGY_STORAGE.get();
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
        for (EquipmentSlot slot : List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)) {
            ItemStack armor = player.getItemBySlot(slot);
            if (!(armor.getItem() instanceof HEVItem) || !isQeCharged(armor)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        if (!(entity instanceof Player player) || slot != armorSlot) return;
        if (player.getItemBySlot(armorSlot) != stack) return;

        boolean isHelmet = slot == EquipmentSlot.HEAD;
        boolean isChest = slot == EquipmentSlot.CHEST;
        boolean isLegs = slot == EquipmentSlot.LEGS;
        boolean isBoots = slot == EquipmentSlot.FEET;

        if (isFeCharged(stack)) {
            if (isChest) player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 1, 1, false, false));
            if (isHelmet) player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 1, 1, false, false));
            if (isLegs) player.addEffect(new MobEffectInstance(MobEffects.SPEED, 1, 1, false, false));
        }

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
            player.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, 1, 1, false, false));
        }

        if (isChest) {
            // Grants flight through NeoForge's attribute, so creative/spectator flight is left alone;
            // ServerPlayer stops flying on its own once no source grants it.
            AttributeInstance flightAttr = player.getAttribute(NeoForgeMod.CREATIVE_FLIGHT);
            if (flightAttr != null) {
                boolean hasFlight = flightAttr.hasModifier(FLIGHT_ID);
                if (qeCharged && !hasFlight) {
                    flightAttr.addTransientModifier(new AttributeModifier(FLIGHT_ID, 1.0, AttributeModifier.Operation.ADD_VALUE));
                } else if (!qeCharged && hasFlight) {
                    flightAttr.removeModifier(FLIGHT_ID);
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
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        EnergyHandler energy = getEnergy(stack);
        int stored = energy == null ? 0 : energy.getAmountAsInt();
        builder.accept(__("tooltip.nuclearcraft.energy_stored",
                formatEnergy(stored), formatEnergy(Common.HEV_ENERGY_STORAGE.get())).withStyle(ChatFormatting.BLUE));
        builder.accept(__("tooltip.nuclearcraft.hev.qe_charge", getQeCharge(stack), MAX_QE_CHARGE).withStyle(ChatFormatting.GREEN));
        builder.accept(__("tooltip.nuclearcraft.hev.desc").withStyle(ChatFormatting.AQUA));
    }
}
