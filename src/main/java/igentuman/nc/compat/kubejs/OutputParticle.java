package igentuman.nc.compat.kubejs;

import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.item.InputItem;
import dev.latvian.mods.kubejs.item.ItemStackJS;
import dev.latvian.mods.kubejs.platform.IngredientPlatformHelper;
import dev.latvian.mods.kubejs.recipe.OutputReplacement;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.recipe.ReplacementMatch;
import dev.latvian.mods.kubejs.util.ConsoleJS;
import igentuman.nc.content.particles.ParticleStack;
import igentuman.nc.content.particles.Particles;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import org.jetbrains.annotations.Nullable;

public class OutputParticle implements OutputReplacement {
	public static final OutputParticle EMPTY = new OutputParticle(ParticleStack.EMPTY, Double.NaN, null);

	public static OutputParticle of(ParticleStack item, double chance) {
		return item.isEmpty() ? EMPTY : new OutputParticle(item, chance, null);
	}

	public static OutputParticle of(Object from) {
		if (from instanceof OutputParticle out) {
			return out;
		} else if (from instanceof ParticleStack stack) {
			return of(stack, Double.NaN);
		}

		var item = ParticleStack.EMPTY;
		
		// Try to convert from various input types
		if (from instanceof String str) {
			// Parse particle string format
			item = parseParticleFromString(str);
		} else if (from instanceof JsonObject json) {
			// Parse JSON particle format
			item = parseParticleFromJson(json);
		} else {
			// For now, return empty for unsupported types
			return EMPTY;
		}

		if (item.isEmpty()) {
			return EMPTY;
		}

		var chance = Double.NaN;
		IntProvider rolls = null;

		if (from instanceof JsonObject j) {
			if (j.has("chance")) {
				chance = j.get("chance").getAsDouble();
			}

			if (j.has("minRolls") && j.has("maxRolls")) {
				rolls = UniformInt.of(j.get("minRolls").getAsInt(), j.get("maxRolls").getAsInt());
			}
		}

		return new OutputParticle(item, chance, rolls);
	}
	
	private static ParticleStack parseParticleFromString(String str) {
		// Parse particle string format like "proton:100:1000:0.5" (particle:amount:energy:focus)
		String[] parts = str.split(":");
		if (parts.length >= 1) {
			var particle = Particles.getParticleFromName(parts[0]);
			if (particle != null) {
				int amount = parts.length > 1 ? Integer.parseInt(parts[1]) : 1;
				long energy = parts.length > 2 ? Long.parseLong(parts[2]) : 0;
				double focus = parts.length > 3 ? Double.parseDouble(parts[3]) : 0.0;
				return new ParticleStack(particle, amount, energy, focus);
			}
		}
		return ParticleStack.EMPTY;
	}
	
	private static ParticleStack parseParticleFromJson(JsonObject json) {
		// Parse JSON particle format: {"particle": "proton", "amount": 100, "meanEnergy": 1000, "focus": 0.5}
		if (json.has("particle")) {
			var particle = Particles.getParticleFromName(json.get("particle").getAsString());
			if (particle != null) {
				int amount = json.has("amount") ? json.get("amount").getAsInt() : 1;
				long energy = json.has("meanEnergy") ? json.get("meanEnergy").getAsLong() : 0;
				double focus = json.has("focus") ? json.get("focus").getAsDouble() : 0.0;
				return new ParticleStack(particle, amount, energy, focus);
			}
		}
		return ParticleStack.EMPTY;
	}

	public final ParticleStack item;
	public final double chance; // Use FloatProvider in future?
	public final IntProvider rolls;

	@Deprecated
	protected OutputParticle(ParticleStack item, double chance) {
		this(item, chance, null);
	}

	protected OutputParticle(ParticleStack item, double chance, @Nullable IntProvider rolls) {
		this.item = item;
		this.chance = chance;
		this.rolls = rolls;
	}

	public OutputParticle withCount(int count) {
		var newItem = item.copy();
		newItem.setAmount(count);
		return new OutputParticle(newItem, chance, rolls);
	}

	public OutputParticle withChance(double chance) {
		return new OutputParticle(item.copy(), chance, rolls);
	}

	public OutputParticle withRolls(IntProvider rolls) {
		return new OutputParticle(item.copy(), chance, rolls);
	}

	public OutputParticle withRolls(int min, int max) {
		return withRolls(UniformInt.of(min, max));
	}

	public boolean hasChance() {
		return !Double.isNaN(chance);
	}

	public double getChance() {
		return chance;
	}

	public int getCount() {
		return item.getAmount();
	}

	public CompoundTag getNbt() {
		// ParticleStack doesn't have NBT like ItemStack, return empty tag
		return new CompoundTag();
	}

	@Override
	public String toString() {
		if (item.getParticle() != null) {
			return item.getParticle().getName() + ":" + item.getAmount() + ":" + item.getMeanEnergy() + ":" + item.getFocus();
		}
		return "empty";
	}

	public boolean isEmpty() {
		return this == EMPTY;
	}

	@Override
	public Object replaceOutput(RecipeJS recipe, ReplacementMatch match, OutputReplacement original) {
		if (original instanceof OutputParticle o) {
			var replacement = new OutputParticle(item.copy(), o.chance, o.rolls);
			replacement.item.setAmount(o.getCount());
			return replacement;
		}

		return new OutputParticle(item.copy(), Double.NaN, null);
	}

	@Deprecated
	public InputParticle ignoreNBT() {
		var console = ConsoleJS.getCurrent(ConsoleJS.SERVER);
		console.warn("Particles don't have NBT data like items!");
		return InputParticle.of(createIngredientFromParticle(item), item.getAmount());
	}

	public InputParticle weakNBT() {
		// Particles don't have NBT, return equivalent InputParticle
		return InputParticle.of(createIngredientFromParticle(item), item.getAmount());
	}

	public InputParticle strongNBT() {
		// Particles don't have NBT, return equivalent InputParticle
		return InputParticle.of(createIngredientFromParticle(item), item.getAmount());
	}
	
	private static Ingredient createIngredientFromParticle(ParticleStack stack) {
		// Since particles don't have direct item equivalents, we'll create a placeholder ingredient
		return Ingredient.of(Items.BARRIER); // Placeholder - replace with actual particle item if available
	}
}
