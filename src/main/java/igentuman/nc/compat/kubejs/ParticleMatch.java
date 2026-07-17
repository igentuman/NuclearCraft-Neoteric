package igentuman.nc.compat.kubejs;

import dev.latvian.mods.kubejs.recipe.ReplacementMatch;
import igentuman.nc.content.particles.ParticleStack;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

public interface ParticleMatch extends ReplacementMatch {
	boolean contains(ParticleStack item);

	boolean contains(Ingredient in);

	default boolean contains(ItemLike itemLike) {
		// For particles, we can't directly convert ItemLike to ParticleStack
		// This is a placeholder implementation - you might want to create a mapping
		return false;
	}

	default boolean containsAny(ItemLike... itemLikes) {
		for (var item : itemLikes) {
			if (contains(item)) {
				return true;
			}
		}
		return false;
	}

	default boolean containsAny(Iterable<ItemLike> itemLikes) {
		for (var item : itemLikes) {
			if (contains(item)) {
				return true;
			}
		}
		return false;
	}
}
