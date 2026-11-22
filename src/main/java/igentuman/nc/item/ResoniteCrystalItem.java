package igentuman.nc.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;

import org.jetbrains.annotations.Nullable;
import java.util.Map;
import java.util.function.Consumer;

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
        return cd.copyTag().getBooleanOr(TAG_ANALYZED, false);
    }

    public static ShardRarity getShardRarity(ItemStack stack) {
        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        if (cd == null) return ShardRarity.COMMON;
        return ShardRarity.byOrdinal(cd.copyTag().getByteOr(TAG_RARITY, (byte) 0));
    }

    public static String getEffectId(ItemStack stack) {
        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        if (cd == null) return "";
        return cd.copyTag().getStringOr(TAG_EFFECT, "");
    }

    @Nullable
    public static Holder<MobEffect> getEffect(ItemStack stack) {
        if (!isAnalyzed(stack)) return null;
        String id = getEffectId(stack);
        if (id.isEmpty()) return null;
        Identifier rl = Identifier.tryParse(id);
        if (rl == null) return null;
        var key = ResourceKey.create(Registries.MOB_EFFECT, rl);
        return BuiltInRegistries.MOB_EFFECT.get(key).orElse(null);
    }

    public static int feOutput(ItemStack stack) {
        return isAnalyzed(stack) ? getShardRarity(stack).fePerTick : 0;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return isAnalyzed(stack) && getShardRarity(stack) == ShardRarity.LEGENDARY;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        if (!isAnalyzed(stack)) {
            tooltip.accept(__("tooltip.nc.resonite_crystal.raw").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            return;
        }
        ShardRarity r = getShardRarity(stack);
        tooltip.accept(__("tooltip.nc.resonite_crystal.rarity",
                __("tooltip.nc.resonite_rarity." + r.name().toLowerCase()))
                .withStyle(style -> style.withColor(TextColor.fromRgb(r.color))));

        Holder<MobEffect> eff = getEffect(stack);
        if (eff != null) {
            tooltip.accept(__("tooltip.nc.resonite_crystal.effect",
                    Component.translatable(eff.value().getDescriptionId()))
                    .withStyle(ChatFormatting.AQUA));
        }

        tooltip.accept(__("tooltip.nc.resonite_crystal.fe", formatEnergy(r.fePerTick))
                .withStyle(ChatFormatting.BLUE));

        String patron = PATRON_BY_EFFECT.get(getEffectId(stack));
        if (patron != null) {
            tooltip.accept(__("tooltip.nc.resonite_crystal.patron." + patron)
                    .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
        }
    }
}
