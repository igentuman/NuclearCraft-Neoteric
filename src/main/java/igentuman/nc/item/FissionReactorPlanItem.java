package igentuman.nc.item;

import igentuman.nc.util.TextUtils;
import igentuman.nc.screen.FissionReactorPlanScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

import static igentuman.nc.util.TextUtils.__;

public class FissionReactorPlanItem extends Item {

    public static final String DESIGN_KEY = "FissionDesign";
    public static final String FUEL_KEY = "Fuel";
    public static final String VARIANT_KEY = "Variant";
    public static final String NET_HEAT_KEY = "NetHeat";
    public static final String FE_GEN_KEY = "FeGen";

    public FissionReactorPlanItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (getDesign(stack).isEmpty()) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            FissionReactorPlanScreen.open(stack);
        }
        return InteractionResult.SUCCESS;
    }

    public static CompoundTag getDesign(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return tag.getCompoundOrEmpty(DESIGN_KEY);
    }

    public static void setDesign(ItemStack stack, CompoundTag design) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.put(DESIGN_KEY, design));
    }

    public static String fuelLabel(String key, String variant) {
        if (key == null || key.isEmpty()) return "";
        int slash = key.indexOf('/');
        String group = slash >= 0 ? key.substring(0, slash) : "";
        String name = slash >= 0 ? key.substring(slash + 1) : key;
        StringBuilder sb = new StringBuilder(name);
        if (variant != null && !variant.isEmpty()) {
            sb.append(" (").append(variant.startsWith("_") ? variant.substring(1) : variant).append(")");
        }
        if (!group.isEmpty()) {
            sb.append(" [").append(group).append("]");
        }
        return sb.toString();
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> list, TooltipFlag flag) {
        CompoundTag design = getDesign(stack);
        if (design.isEmpty()) {
            list.accept(__("tooltip.nc.fission_reactor_plan.blank").withStyle(ChatFormatting.DARK_GRAY));
            return;
        }
        int sx = design.getIntOr("sizeX", 0);
        int sy = design.getIntOr("sizeY", 0);
        int sz = design.getIntOr("sizeZ", 0);
        list.accept(__("tooltip.nc.fission_reactor_plan.size", sx, sy, sz).withStyle(ChatFormatting.AQUA));
        if (design.contains(FUEL_KEY)) {
            String label = fuelLabel(design.getStringOr(FUEL_KEY, ""), design.getStringOr(VARIANT_KEY, ""));
            if (!label.isEmpty()) {
                list.accept(__("tooltip.nc.fission_reactor_plan.fuel", label).withStyle(ChatFormatting.GOLD));
            }
        }
        if (design.contains(NET_HEAT_KEY)) {
            list.accept(__("tooltip.nc.fission_reactor_plan.net_heat",
                    TextUtils.numberFormat(design.getDoubleOr(NET_HEAT_KEY, 0.0))).withStyle(ChatFormatting.RED));
        }
        if (design.contains(FE_GEN_KEY)) {
            list.accept(__("tooltip.nc.fission_reactor_plan.fe_gen",
                    TextUtils.numberFormat(design.getIntOr(FE_GEN_KEY, 0))).withStyle(ChatFormatting.GREEN));
        }
        list.accept(__("tooltip.nc.fission_reactor_plan.view", Component.keybind("key.use"))
                .withStyle(ChatFormatting.GRAY));
    }
}
