package igentuman.nc.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;

import static igentuman.nc.NuclearCraft.rl;

/** Defines the mod's custom tool material tiers (tough alloy, thorium) and their stats/repair ingredients. */
public final class NCTiers {

    private NCTiers() {}

    public static final TagKey<Item> TOUGH_REPAIRS = TagKey.create(Registries.ITEM, rl("repairs/tough_alloy"));
    public static final TagKey<Item> THORIUM_REPAIRS = TagKey.create(Registries.ITEM, rl("repairs/thorium"));
    public static final TagKey<Item> QNP_REPAIRS = TagKey.create(Registries.ITEM, rl("repairs/qnp"));
    public static final TagKey<Item> HEV_REPAIRS = TagKey.create(Registries.ITEM, rl("repairs/hev"));

    public static final ToolMaterial TOUGH = new ToolMaterial(
            BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 10000, 12.0F, 10.0F, 22, TOUGH_REPAIRS);

    public static final ToolMaterial THORIUM = new ToolMaterial(
            BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 10000, 10.0F, 6.0F, 18, THORIUM_REPAIRS);

    public static final ToolMaterial QNP = new ToolMaterial(
            BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 50000, 20.0F, 14.0F, 25, QNP_REPAIRS);
}
