package igentuman.nc.item;

import igentuman.nc.content.fuel.FuelDef;
import igentuman.nc.registration.FissionFuelEntry;
import igentuman.nc.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

import static igentuman.nc.util.TextUtils.__;

/** Fission fuel pellet item; resolves its stats live from the owning {@link FissionFuelEntry} for its tooltip. */
public class ItemFuel extends Item {

    private final FissionFuelEntry entry;
    public final String variant;

    public ItemFuel(Properties properties, FissionFuelEntry entry, String variant) {
        super(properties);
        this.entry = entry;
        this.variant = variant;
    }

    public FuelDef def() {
        return entry.variantDef(variant);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        FuelDef def = def();
        if (variant.equals("_tr")) {
            tooltip.add(TextUtils.applyFormat(__("tooltip.nuclearcraft.fuel.criticality", def.criticality), ChatFormatting.RED));
        } else {
            tooltip.add(TextUtils.applyFormat(__("tooltip.nuclearcraft.fuel.forge_energy", TextUtils.formatEnergy(def.forgeEnergy)), ChatFormatting.BLUE));
        }
        tooltip.add(TextUtils.applyFormat(__("tooltip.nuclearcraft.fuel.heat", TextUtils.numberFormat(def.heat)), ChatFormatting.GOLD));
        tooltip.add(TextUtils.applyFormat(__("tooltip.nuclearcraft.fuel.depletion", TextUtils.formatTime(def.depletion)), ChatFormatting.GREEN));
    }
}
