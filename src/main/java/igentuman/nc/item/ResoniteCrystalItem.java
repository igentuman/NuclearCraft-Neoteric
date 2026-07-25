package igentuman.nc.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;

import java.util.List;

import static igentuman.nc.util.TextUtils.__;
import static igentuman.nc.util.TextUtils.formatEnergy;

public class ResoniteCrystalItem extends Item {

    public static final String TAG_ANALYZED = "analyzed";
    public static final String TAG_RARITY = "rarity";
    public static final String TAG_EFFECT = "effect";

    public ResoniteCrystalItem(Properties props) {
        super(props);
    }

    public static boolean isAnalyzed(ItemStack stack) {
        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        if (cd == null) return false;
        return cd.copyTag().getBoolean(TAG_ANALYZED);
    }

    public static ShardRarity getShardRarity(ItemStack stack) {
        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        if (cd == null) return ShardRarity.COMMON;
        return ShardRarity.byOrdinal(cd.copyTag().getByte(TAG_RARITY));
    }

    public static String getEffectId(ItemStack stack) {
        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        if (cd == null) return "";
        return cd.copyTag().getString(TAG_EFFECT);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return isAnalyzed(stack) && getShardRarity(stack) == ShardRarity.LEGENDARY;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (!isAnalyzed(stack)) {
            tooltip.add(__("tooltip.nc.resonite_crystal.raw").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            return;
        }
        ShardRarity r = getShardRarity(stack);
        tooltip.add(__("tooltip.nc.resonite_crystal.rarity",
                __("tooltip.nc.resonite_rarity." + r.name().toLowerCase()))
                .withStyle(style -> style.withColor(TextColor.fromRgb(r.color))));

        String effectIdStr = getEffectId(stack);
        if (!effectIdStr.isEmpty()) {
            ResourceLocation rl = ResourceLocation.tryParse(effectIdStr);
            if (rl != null) {
                var key = ResourceKey.create(Registries.MOB_EFFECT, rl);
                BuiltInRegistries.MOB_EFFECT.getHolder(key).ifPresent(holder -> {
                    MobEffect effect = holder.value();
                    tooltip.add(__("tooltip.nc.resonite_crystal.effect",
                            Component.translatable(effect.getDescriptionId()))
                            .withStyle(ChatFormatting.AQUA));
                });
            }
        }

        tooltip.add(__("tooltip.nc.resonite_crystal.fe", formatEnergy(r.fePerTick))
                .withStyle(ChatFormatting.BLUE));
    }
}
