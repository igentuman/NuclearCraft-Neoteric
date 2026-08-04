package igentuman.nc.item;

import igentuman.nc.util.ItemDataUtils;
import igentuman.nc.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static igentuman.nc.util.TextUtils.__;

public class FissionReactorPlanItem extends Item {

    public static final String DESIGN_KEY = "FissionDesign";
    public static final String FUEL_KEY = "Fuel";
    public static final String NET_HEAT_KEY = "NetHeat";
    public static final String FE_GEN_KEY = "FeGen";

    public FissionReactorPlanItem(Properties props) {
        super(props);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> list, TooltipFlag flag) {
        CompoundTag design = ItemDataUtils.getCompound(stack, DESIGN_KEY);
        if (design.isEmpty()) {
            list.add(__("tooltip.nc.fission_reactor_plan.blank").withStyle(ChatFormatting.DARK_GRAY));
            return;
        }
        int sx = design.getInt("sizeX");
        int sy = design.getInt("sizeY");
        int sz = design.getInt("sizeZ");
        list.add(__("tooltip.nc.fission_reactor_plan.size", sx, sy, sz).withStyle(ChatFormatting.AQUA));
        List<String> fuelKey = fuelKeyFromTag(design);
        if (fuelKey != null) {
            list.add(__("tooltip.nc.fission_reactor_plan.fuel", fuelLabel(fuelKey)).withStyle(ChatFormatting.GOLD));
        }
        if (design.contains(NET_HEAT_KEY)) {
            list.add(__("tooltip.nc.fission_reactor_plan.net_heat",
                    TextUtils.numberFormat(design.getDouble(NET_HEAT_KEY))).withStyle(ChatFormatting.RED));
        }
        if (design.contains(FE_GEN_KEY)) {
            int fe = design.getInt(FE_GEN_KEY);
            list.add(__(TextUtils.energyGenLine(), TextUtils.numberFormat(TextUtils.energy2Display(fe)))
                    .withStyle(ChatFormatting.GREEN));
        }
    }

    public static ListTag fuelKeyToTag(List<String> key) {
        ListTag tag = new ListTag();
        if (key == null) {
            return tag;
        }
        for (String s : key) {
            tag.add(StringTag.valueOf(s));
        }
        return tag;
    }

    public static List<String> fuelKeyFromTag(CompoundTag design) {
        if (!design.contains(FUEL_KEY, Tag.TAG_LIST)) {
            return null;
        }
        ListTag list = design.getList(FUEL_KEY, Tag.TAG_STRING);
        List<String> key = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            key.add(list.getString(i));
        }
        return key;
    }

    public static String fuelLabel(List<String> key) {
        StringBuilder sb = new StringBuilder();
        if (key.size() > 2) {
            sb.append(key.get(2));
        }
        if (key.size() > 3 && !key.get(3).isEmpty()) {
            sb.append(" (").append(key.get(3)).append(")");
        }
        if (key.size() > 1) {
            sb.append(" [").append(key.get(1)).append("]");
        }
        return sb.toString();
    }
}
