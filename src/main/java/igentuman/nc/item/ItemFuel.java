package igentuman.nc.item;

import igentuman.nc.api.content.FuelDef;
import igentuman.nc.registration.FissionFuelEntry;
import igentuman.nc.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

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
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        FuelDef def = def();
        if (variant.equals("_tr")) {
            tooltip.accept(TextUtils.applyFormat(__("tooltip.nuclearcraft.fuel.criticality", def.criticality), ChatFormatting.RED));
        } else {
            tooltip.accept(TextUtils.applyFormat(__("tooltip.nuclearcraft.fuel.forge_energy", TextUtils.formatEnergy(def.forgeEnergy)), ChatFormatting.BLUE));
        }
        tooltip.accept(TextUtils.applyFormat(__("tooltip.nuclearcraft.fuel.heat", TextUtils.numberFormat(def.heat)), ChatFormatting.GOLD));
        tooltip.accept(TextUtils.applyFormat(__("tooltip.nuclearcraft.fuel.depletion", TextUtils.formatTime(def.depletion)), ChatFormatting.GREEN));
    }
}
