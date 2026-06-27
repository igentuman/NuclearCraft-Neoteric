package igentuman.nc.item;

import igentuman.nc.handler.CrystalEnergyProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

import static igentuman.nc.util.TextUtils.__;
import static igentuman.nc.util.TextUtils.formatEnergy;

public class ResoniteCrystalItem extends Item {

    public static final String TAG_ANALYZED = "analyzed";
    public static final String TAG_RARITY = "rarity";
    public static final String TAG_EFFECT = "effect";

    /** Easter egg: an analyzed effect tied to a Patreon patron gets a flavor line crediting them. */
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
        return stack.hasTag() && stack.getTag().getBoolean(TAG_ANALYZED);
    }

    public static ShardRarity rarity(ItemStack stack) {
        return ShardRarity.byOrdinal(stack.hasTag() ? stack.getTag().getByte(TAG_RARITY) : 0);
    }

    @Nullable
    public static MobEffect effect(ItemStack stack) {
        if (!isAnalyzed(stack)) {
            return null;
        }
        String id = stack.getTag().getString(TAG_EFFECT);
        if (id.isEmpty()) {
            return null;
        }
        return ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(id));
    }

    /** FE/t this crystal supplies: zero while raw, the rolled rarity's output once analyzed. */
    public static int feOutput(ItemStack stack) {
        return isAnalyzed(stack) ? rarity(stack).fePerTick : 0;
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new CrystalEnergyProvider(stack);
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        return isAnalyzed(stack) ? rarity(stack).vanilla : Rarity.COMMON;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return isAnalyzed(stack) && rarity(stack) == ShardRarity.LEGENDARY;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (!isAnalyzed(stack)) {
            tooltip.add(__("tooltip.nc.resonite_crystal.raw").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            return;
        }
        ShardRarity r = rarity(stack);
        tooltip.add(__("tooltip.nc.resonite_crystal.rarity", __("tooltip.nc.resonite_rarity." + r.name().toLowerCase()))
                .withStyle(style -> style.withColor(TextColor.fromRgb(r.color))));
        MobEffect eff = effect(stack);
        if (eff != null) {
            tooltip.add(__("tooltip.nc.resonite_crystal.effect", Component.translatable(eff.getDescriptionId()))
                    .withStyle(ChatFormatting.AQUA));
        }
        tooltip.add(__("tooltip.nc.resonite_crystal.fe", formatEnergy(r.fePerTick)).withStyle(ChatFormatting.BLUE));
        String patron = PATRON_BY_EFFECT.get(stack.getTag().getString(TAG_EFFECT));
        if (patron != null) {
            tooltip.add(__("tooltip.nc.resonite_crystal.patron." + patron)
                    .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
        }
    }
}
