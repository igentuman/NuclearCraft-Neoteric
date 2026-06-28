package igentuman.nc.item;

import igentuman.nc.content.fuel.FuelDef;
import igentuman.nc.registration.FuelEntry;
import igentuman.nc.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

import static igentuman.nc.util.TextUtils.__;

/**
 * A fission fuel pellet item. Resolves its parameters live from the owning {@link FuelEntry}
 * so that KubeJS parameter overrides are reflected in the tooltip. Radiation is intentionally
 * not modeled here (it lives in the separate NuclearRadiation mod).
 */
public class ItemFuel extends Item {

    private final FuelEntry entry;
    public final String variant;

    public ItemFuel(Properties properties, FuelEntry entry, String variant) {
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
        tooltip.add(TextUtils.applyFormat(__("tooltip.nuclearcraft.fuel.depletion", def.depletion), ChatFormatting.GREEN));
        tooltip.add(TextUtils.applyFormat(__("tooltip.nuclearcraft.fuel.efficiency", def.efficiency), ChatFormatting.AQUA));
    }
}
