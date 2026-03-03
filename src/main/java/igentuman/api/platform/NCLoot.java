package igentuman.api.platform;

import net.minecraft.advancements.critereon.EnchantmentPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.entries.DynamicLoot;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.CopyComponentsFunction;
import net.minecraft.world.level.storage.loot.functions.EnchantedCountIncreaseFunction;
import net.minecraft.world.level.storage.loot.functions.SetContainerContents;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

/**
 * Platform wrapper for loot table API changes in NeoForge 1.21.1.
 * - CopyNbtFunction → CopyComponentsFunction
 * - SetContainerContents(BlockEntityType) → SetContainerContents.setContents(...)
 * - Enchantment predicates now holder-based (Enchantments fields are ResourceKey<Enchantment>)
 * - LootingEnchantFunction → EnchantedCountIncreaseFunction (holder-based)
 */
public final class NCLoot {
    private NCLoot() {}

    /**
     * Replaces CopyNbtFunction.copyData(ContextNbtProvider.BLOCK_ENTITY).
     * In 1.21.1 NBT is stored as DataComponents, so we use CopyComponentsFunction.
     */
    public static CopyComponentsFunction.Builder copyBlockEntityData() {
        return CopyComponentsFunction.copyComponents(CopyComponentsFunction.Source.BLOCK_ENTITY);
    }

    /**
     * Replaces SetContainerContents.setContents(BlockEntityType<?>).
     * In 1.21.1 container contents use the component-based system.
     */
    public static SetContainerContents.Builder setContainerContents() {
        return SetContainerContents.setContents(net.minecraft.world.level.storage.loot.ContainerComponentManipulators.CONTAINER);
    }

    /**
     * Builds a silk touch condition for loot tables using holder-based enchantment lookup.
     */
    public static LootItemCondition.Builder hasSilkTouch(HolderLookup.Provider registries) {
        return MatchTool.toolMatches(ItemPredicate.Builder.item()
                .withSubPredicate(
                        net.minecraft.advancements.critereon.ItemSubPredicates.ENCHANTMENTS,
                        net.minecraft.advancements.critereon.ItemEnchantmentsPredicate.enchantments(
                                java.util.List.of(new EnchantmentPredicate(
                                        registries.holderOrThrow(Enchantments.SILK_TOUCH),
                                        MinMaxBounds.Ints.atLeast(1))))));
    }

    /**
     * Builds a fortune bonus count for loot tables using holder-based enchantment lookup.
     */
    public static ApplyBonusCount.Builder fortuneBonus(HolderLookup.Provider registries, int bonusMultiplier) {
        return ApplyBonusCount.addUniformBonusCount(
                registries.holderOrThrow(Enchantments.FORTUNE), bonusMultiplier);
    }

    /**
     * Replaces LootingEnchantFunction.lootingMultiplier(NumberProvider).
     * In 1.21.1, LootingEnchantFunction was replaced by EnchantedCountIncreaseFunction
     * which requires a HolderLookup.Provider for holder-based enchantment lookup.
     */
    public static EnchantedCountIncreaseFunction.Builder lootingMultiplier(
            HolderLookup.Provider registries, NumberProvider count) {
        return EnchantedCountIncreaseFunction.lootingMultiplier(registries, count);
    }
}
