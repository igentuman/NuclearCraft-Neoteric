package igentuman.nc.item;

import igentuman.nc.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;

import java.util.List;

import static igentuman.nc.util.TextUtils.__;

/** Item that stores a fission reactor design (grid, fuel, simulated stats) in its NBT and shows a summary tooltip. */
public class FissionReactorPlanItem extends Item {

    public static final String DESIGN_KEY = "FissionDesign";
    public static final String FUEL_KEY = "Fuel";
    public static final String VARIANT_KEY = "Variant";
    public static final String NET_HEAT_KEY = "NetHeat";
    public static final String FE_GEN_KEY = "FeGen";

    public FissionReactorPlanItem(Properties props) {
        super(props);
    }

    public static CompoundTag getDesign(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return tag.getCompound(DESIGN_KEY);
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
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> list, TooltipFlag flag) {
        CompoundTag design = getDesign(stack);
        if (design.isEmpty()) {
            list.add(__("tooltip.nc.fission_reactor_plan.blank").withStyle(ChatFormatting.DARK_GRAY));
            return;
        }
        int sx = design.getInt("sizeX");
        int sy = design.getInt("sizeY");
        int sz = design.getInt("sizeZ");
        list.add(__("tooltip.nc.fission_reactor_plan.size", sx, sy, sz).withStyle(ChatFormatting.AQUA));
        if (design.contains(FUEL_KEY)) {
            String label = fuelLabel(design.getString(FUEL_KEY), design.getString(VARIANT_KEY));
            if (!label.isEmpty()) {
                list.add(__("tooltip.nc.fission_reactor_plan.fuel", label).withStyle(ChatFormatting.GOLD));
            }
        }
        if (design.contains(NET_HEAT_KEY)) {
            list.add(__("tooltip.nc.fission_reactor_plan.net_heat",
                    TextUtils.numberFormat(design.getDouble(NET_HEAT_KEY))).withStyle(ChatFormatting.RED));
        }
        if (design.contains(FE_GEN_KEY)) {
            list.add(__("tooltip.nc.fission_reactor_plan.fe_gen",
                    TextUtils.numberFormat(design.getInt(FE_GEN_KEY))).withStyle(ChatFormatting.GREEN));
        }
    }
}
