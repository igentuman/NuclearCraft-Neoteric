package igentuman.nc.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

import static igentuman.nc.util.TextUtils.__;
import static igentuman.nc.util.TextUtils.formatEnergy;

public class ResoniteCrystalItem extends Item {

    public static final String TAG_ANALYZED = "analyzed";
    public static final String TAG_RARITY = "rarity";
    public static final String TAG_EFFECT = "effect";

    private static final Map<String, String> PATRON_BY_EFFECT = Map.ofEntries(
            Map.entry("minecraft:invisibility", "noteclip"),
            Map.entry("minecraft:health_boost", "marcin212"),
            Map.entry("nuclearcraft:max_health_boost", "marcin212"),
            Map.entry("minecraft:strength", "personbelowrocks"),
            Map.entry("nuclearcraft:radiation_resistance", "tomdodd4598"),
            Map.entry("minecraft:luck", "ethantabler"),
            Map.entry("minecraft:absorption", "endleon201"),
            Map.entry("minecraft:speed", "sancho_lucky"),
            Map.entry("minecraft:jump_boost", "cerusvi"),
            Map.entry("minecraft:resistance", "tocix9730"));

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

    @Nullable
    public static Holder<MobEffect> getEffect(ItemStack stack) {
        if (!isAnalyzed(stack)) return null;
        String id = getEffectId(stack);
        if (id.isEmpty()) return null;
        ResourceLocation rl = ResourceLocation.tryParse(id);
        if (rl == null) return null;
        var key = ResourceKey.create(Registries.MOB_EFFECT, rl);
        return BuiltInRegistries.MOB_EFFECT.getHolder(key).orElse(null);
    }

    public static int feOutput(ItemStack stack) {
        return isAnalyzed(stack) ? getShardRarity(stack).fePerTick : 0;
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

        Holder<MobEffect> eff = getEffect(stack);
        if (eff != null) {
            tooltip.add(__("tooltip.nc.resonite_crystal.effect",
                    Component.translatable(eff.value().getDescriptionId()))
                    .withStyle(ChatFormatting.AQUA));
        }

        tooltip.add(__("tooltip.nc.resonite_crystal.fe", formatEnergy(r.fePerTick))
                .withStyle(ChatFormatting.BLUE));

        String patron = PATRON_BY_EFFECT.get(getEffectId(stack));
        if (patron != null) {
            tooltip.add(__("tooltip.nc.resonite_crystal.patron." + patron)
                    .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
        }
    }
}
