package igentuman.nc.item;
import igentuman.nc.content.fuel.FuelDef;
import igentuman.nc.util.TextUtils;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import javax.annotation.Nullable;

import java.util.List;

import static igentuman.nc.handler.event.client.InputEvents.DESCRIPTIONS_SHOW;

public class ItemFuel extends Item {

    public FuelDef def;
    public double heat = 0;
    public int forge_energy = 0;

    public double heat_boiling = 0;

    public int criticality = 0;

    public int depletion = 0;

    public int efficiency = 0;

    public ItemFuel(Properties pProperties, FuelDef def) {
        this(pProperties);
        setDefinition(def);
    }

    public ItemFuel(Properties pProperties) {
        super(pProperties);
    }

    public ItemFuel setDefinition(FuelDef definition)
    {
        def = definition;
        heat = def.getHeatFEMode();
        heat_boiling = def.getHeatBoilingMode();
        criticality = def.criticality;
        depletion = def.depletion;
        efficiency = def.efficiency;
        forge_energy = def.forge_energy;
        return this;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> list, ITooltipFlag flag)
    {
        if(!DESCRIPTIONS_SHOW) {
            list.add(TextUtils.applyFormat(new TranslationTextComponent("tooltip.toggle_description_keys"), TextFormatting.GRAY));
        } else {
            list.add(TextUtils.applyFormat(new TranslationTextComponent("fuel.heat.descr", TextUtils.numberFormat(heat)), TextFormatting.GOLD));
            //list.add(TextUtils.applyFormat(new TranslationTextComponent("fuel.heat_boiling.descr", TextUtils.numberFormat(heat_boiling)), TextFormatting.YELLOW));
            list.add(TextUtils.applyFormat(new TranslationTextComponent("fuel.forge_energy.descr", forge_energy), TextFormatting.BLUE));
            //list.add(TextUtils.applyFormat(new TranslationTextComponent("fuel.criticality.descr", criticality), TextFormatting.RED));
            list.add(TextUtils.applyFormat(new TranslationTextComponent("fuel.depletion.descr", depletion), TextFormatting.GREEN));
            //list.add(TextUtils.applyFormat(new TranslationTextComponent("fuel.efficiency.descr", efficiency), TextFormatting.DARK_PURPLE));
            list.add(TextUtils.applyFormat(new TranslationTextComponent("fuel.description"), TextFormatting.AQUA));
        }
    }
}
