package igentuman.nc.compat.kubejs;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.core.IngredientSupplierKJS;
import dev.latvian.mods.kubejs.item.ItemStackJS;
import dev.latvian.mods.kubejs.item.ingredient.IngredientJS;
import dev.latvian.mods.kubejs.platform.IngredientPlatformHelper;
import dev.latvian.mods.kubejs.platform.RecipePlatformHelper;
import dev.latvian.mods.kubejs.recipe.InputReplacement;
import dev.latvian.mods.kubejs.recipe.RecipeExceptionJS;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.recipe.ReplacementMatch;
import dev.latvian.mods.rhino.mod.util.JsonSerializable;
import dev.latvian.mods.rhino.util.RemapForJS;
import igentuman.nc.content.particles.ParticleStack;
import igentuman.nc.content.particles.Particles;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InputParticle implements IngredientSupplierKJS, InputReplacement, JsonSerializable {
	public static final InputParticle EMPTY = new InputParticle(Ingredient.EMPTY, 0);
	public static final Map<String, InputParticle> PARSE_CACHE = new HashMap<>();

	public static InputParticle of(Ingredient ingredient, int count) {
		return count <= 0 || ingredient == Ingredient.EMPTY ? EMPTY : new InputParticle(ingredient, count);
	}
	
	private static Ingredient createIngredientFromParticle(ParticleStack stack) {
		// Since particles don't have direct item equivalents, we'll create a placeholder ingredient
		// This is a simplified approach - you might want to create actual particle items or use a different approach
		return Ingredient.of(Items.BARRIER); // Placeholder - replace with actual particle item if available
	}

	public static InputParticle of(Object o) {
		if (o instanceof InputParticle in) {
			return in;
		} else if (o instanceof ParticleStack stack) {
			return stack.isEmpty() ? EMPTY : of(createIngredientFromParticle(stack), stack.getAmount());
		} else if (o instanceof OutputParticle out) {
			return out.isEmpty() ? EMPTY : of(createIngredientFromParticle(out.item), out.getCount());
		} else if (o instanceof CharSequence) {
			var str = o.toString();

			if (str.isEmpty() || str.equals("air")) {
				return EMPTY;
			}

			var cached = PARSE_CACHE.get(str);

			if (cached != null) {
				return cached;
			}

			// parse "Nx ID"

			int x = str.indexOf('x');

			if (x > 0 && x < str.length() - 2 && str.charAt(x + 1) == ' ') {
				try {
					var ingredient = IngredientJS.of(str.substring(x + 2));

					if (ingredient == Ingredient.EMPTY) {
						return EMPTY;
					}

					int count = Integer.parseInt(str.substring(0, x));
					cached = of(IngredientJS.of(str.substring(x + 2)), count);
				} catch (Exception ignore) {
					throw new RecipeExceptionJS("Invalid particle input: " + str);
				}
			}

			if (cached == null) {
				cached = of(IngredientJS.of(str), 1);
			}

			PARSE_CACHE.put(str, cached);
			return cached;
		} else if (o instanceof JsonElement json) {
			return ofJson(json);
		} else if (o instanceof JsonObject json) {
			return ofJson(json);
		}

		return of(IngredientJS.of(o), 1);
	}

	static InputParticle ofJson(JsonElement json) {
		if (json == null || json.isJsonNull() || json.isJsonArray() && json.getAsJsonArray().isEmpty()) {
			return EMPTY;
		} else if (json.isJsonPrimitive()) {
			return of(json.getAsString());
		} else if (json.isJsonObject()) {
			var o = json.getAsJsonObject();
			var val = o.has("value");
			var count = o.has("amount") ? o.get("amount").getAsInt() : (o.has("count") ? o.get("count").getAsInt() : 1);

			// Check if this is a particle JSON format
			if (o.has("particle")) {
				var particle = Particles.getParticleFromName(o.get("particle").getAsString());
				if (particle != null) {
					long energy = o.has("meanEnergy") ? o.get("meanEnergy").getAsLong() : 0;
					double focus = o.has("focus") ? o.get("focus").getAsDouble() : 0.0;
					var particleStack = new ParticleStack(particle, count, energy, focus);
					return of(createIngredientFromParticle(particleStack), count);
				}
			} else if (o.has("type")) {
				try {
					return of(RecipePlatformHelper.get().getCustomIngredient(o), count);
				} catch (Exception ex) {
					throw new RecipeExceptionJS("Failed to parse custom ingredient (" + o.get("type") + ") from " + o + ": " + ex);
				}
			} else if (val || o.has("ingredient")) {
				return of(IngredientJS.ofJson(val ? o.get("value") : o.get("ingredient")), count);
			} else if (o.has("tag")) {
				return of(IngredientPlatformHelper.get().tag(o.get("tag").getAsString()), count);
			} else if (o.has("item")) {
				return of(Ingredient.of(ItemStackJS.of(o.get("item").getAsString())), count);
			}

			return EMPTY;
		} else {
			return of(Ingredient.fromJson(json), 1);
		}
	}

	public final Ingredient ingredient;
	public final int count;

	protected InputParticle(Ingredient ingredient, int count) {
		this.ingredient = ingredient;
		this.count = count;
	}

	@Override
	public Ingredient kjs$asIngredient() {
		return ingredient;
	}

	public InputParticle withCount(int count) {
		return count == this.count ? this : new InputParticle(ingredient, count);
	}

	public boolean isEmpty() {
		return this == EMPTY || ingredient.isEmpty() || count <= 0;
	}

	public boolean validForMatching() {
		return !isEmpty() && !ingredient.isEmpty();
	}

	public List<InputParticle> unwrap() {
		if (count > 1) {
			var list = new ArrayList<InputParticle>(count);
			var single = withCount(1);

			for (int i = 0; i < count; i++) {
				list.add(single);
			}

			return list;
		}

		return List.of(this);
	}

	@Override
	public String toString() {
		if (count > 1) {
			return count + "x " + ingredient;
		}

		return ingredient.toString();
	}

	// This method is intended to be used as a *sane default* for what input items might look like represented as JSON.
	// As the name implies, this is only intended to be used from KubeJS scripts, and should not be used for serialization purposes.
	@Override
	public JsonElement toJsonJS() {
		return toJsonJS(true);
	}

	@RemapForJS("toJson")
	public JsonElement toJsonJS(boolean alwaysNest) {
		if (!alwaysNest && count == 1) {
			return ingredient.toJson();
		} else {
			var o = new JsonObject();
			o.addProperty("count", count);
			o.add("ingredient", ingredient.toJson());
			return o;
		}
	}

	@Override
	public Object replaceInput(RecipeJS recipe, ReplacementMatch match, InputReplacement original) {
		if (original instanceof InputParticle o) {
			return withCount(o.count);
		}

		return this;
	}
}