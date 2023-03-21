package igentuman.nc.item;
import igentuman.nc.setup.fuel.FuelDef;
import igentuman.nc.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class ItemFuel extends Item {

    public FuelDef def;
    public int heat = 0;

    public int heat_boiling = 0;

    public Integer criticality = 0;

    public Integer depletion = 0;

    public Integer efficiency = 0;

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
        heat = def.heat;
        heat_boiling = def.getHeatBoiling();
        criticality = def.criticality;
        depletion = def.depletion;
        efficiency = def.efficiency;
        return this;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> list, TooltipFlag flag)
    {
        list.add(TextUtils.applyFormat(Component.translatable("fuel.heat.descr", heat), ChatFormatting.GOLD));
        list.add(TextUtils.applyFormat(Component.translatable("fuel.heat_boiling.descr", heat_boiling), ChatFormatting.YELLOW));
        list.add(TextUtils.applyFormat(Component.translatable("fuel.criticality.descr", criticality), ChatFormatting.RED));
        list.add(TextUtils.applyFormat(Component.translatable("fuel.depletion.descr", depletion), ChatFormatting.GREEN));
        list.add(TextUtils.applyFormat(Component.translatable("fuel.efficiency.descr", efficiency), ChatFormatting.DARK_PURPLE));
    }
}
