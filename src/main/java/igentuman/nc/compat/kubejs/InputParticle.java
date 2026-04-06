package igentuman.nc.compat.kubejs;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.kubejs.core.IngredientSupplierKJS;
import dev.latvian.mods.kubejs.error.KubeRuntimeException;
import dev.latvian.mods.kubejs.util.JsonSerializable;
import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.util.RemapForJS;
import igentuman.nc.content.particles.ParticleStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * KubeJS input-side representation of a particle ingredient for NC's target chamber recipes.
 *
 * <p>Particles have no true {@link Ingredient} equivalent — the {@link #ingredient} field is always
 * a {@link Items#BARRIER} placeholder, kept only so this type can plug into KubeJS's ingredient-supplier
 * machinery. All real matching lives on {@link ParticleMatch}.
 */
public class InputParticle implements IngredientSupplierKJS, JsonSerializable {
	public static final InputParticle EMPTY = new InputParticle(Ingredient.EMPTY, 0, "", 0L, 0.0);
	public static final Map<String, InputParticle> PARSE_CACHE = new HashMap<>();

	/**
	 * JSON codec for round-tripping particle input data through KubeJS list components.
	 * Shape: {@code {"particle": "proton", "amount": 100, "meanEnergy": 1000000, "focus": 0.5}}.
	 * All four fields are required — matches {@code ParticleStack.fromJSON} semantics (no defaults).
	 */
	public static final Codec<InputParticle> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.fieldOf("particle").forGetter(ip -> ip.particleName),
			Codec.INT.fieldOf("amount").forGetter(ip -> ip.count),
			Codec.LONG.fieldOf("meanEnergy").forGetter(ip -> ip.meanEnergy),
			Codec.DOUBLE.fieldOf("focus").forGetter(ip -> ip.focus)
	).apply(instance, (name, count, meanEnergy, focus) ->
			count <= 0 ? EMPTY : new InputParticle(placeholder(), count, name, meanEnergy, focus)));

	public static InputParticle of(Ingredient ingredient, int count) {
		return count <= 0 || ingredient == Ingredient.EMPTY ? EMPTY : new InputParticle(ingredient, count, "", 0L, 0.0);
	}

	public static InputParticle of(String particleName, int count) {
		return count <= 0 ? EMPTY : new InputParticle(placeholder(), count, particleName, 0L, 0.0);
	}

	public static InputParticle of(String particleName, int count, long meanEnergy, double focus) {
		return count <= 0 ? EMPTY : new InputParticle(placeholder(), count, particleName, meanEnergy, focus);
	}

	/** Placeholder ingredient for particles — they have no real item representation. */
	static Ingredient placeholder() {
		return Ingredient.of(Items.BARRIER);
	}

	public static InputParticle of(Object o) {
		if (o instanceof InputParticle in) {
			return in;
		} else if (o instanceof ParticleStack stack) {
			if (stack.isEmpty()) return EMPTY;
			var name = stack.getParticle() != null ? stack.getParticle().getName() : "";
			return of(name, stack.getAmount(), stack.getMeanEnergy(), stack.getFocus());
		} else if (o instanceof OutputParticle out) {
			if (out.isEmpty()) return EMPTY;
			var ps = out.item;
			var name = ps.getParticle() != null ? ps.getParticle().getName() : "";
			return of(name, out.getCount(), ps.getMeanEnergy(), ps.getFocus());
		} else if (o instanceof CharSequence) {
			var str = o.toString();

			if (str.isEmpty() || str.equals("air")) {
				return EMPTY;
			}

			var cached = PARSE_CACHE.get(str);

			if (cached != null) {
				return cached;
			}

			// parse "Nx particleName"
			int x = str.indexOf('x');

			if (x > 0 && x < str.length() - 2 && str.charAt(x + 1) == ' ') {
				try {
					int count = Integer.parseInt(str.substring(0, x));
					var name = str.substring(x + 2).trim();
					cached = of(name, count);
				} catch (Exception ignore) {
					throw new KubeRuntimeException("Invalid particle input: " + str);
				}
			}

			if (cached == null) {
				cached = of(str, 1);
			}

			PARSE_CACHE.put(str, cached);
			return cached;
		} else if (o instanceof JsonElement json) {
			return ofJson(json);
		} else if (o instanceof JsonObject json) {
			return ofJson(json);
		}

		return of(placeholder(), 1);
	}

	static InputParticle ofJson(JsonElement json) {
		if (json == null || json.isJsonNull() || json.isJsonArray() && json.getAsJsonArray().isEmpty()) {
			return EMPTY;
		} else if (json.isJsonPrimitive()) {
			return of(json.getAsString());
		} else if (json.isJsonObject()) {
			var o = json.getAsJsonObject();

			if (o.has("particle")) {
				var count = o.has("amount") ? o.get("amount").getAsInt() : (o.has("count") ? o.get("count").getAsInt() : 1);
				var meanEnergy = o.has("meanEnergy") ? o.get("meanEnergy").getAsLong() : 0L;
				var focus = o.has("focus") ? o.get("focus").getAsDouble() : 0.0;
				return of(o.get("particle").getAsString(), count, meanEnergy, focus);
			}

			return EMPTY;
		}

		return EMPTY;
	}

	public final Ingredient ingredient;
	public final int count;
	public final String particleName;
	public final long meanEnergy;
	public final double focus;

	protected InputParticle(Ingredient ingredient, int count, String particleName, long meanEnergy, double focus) {
		this.ingredient = ingredient;
		this.count = count;
		this.particleName = particleName == null ? "" : particleName;
		this.meanEnergy = meanEnergy;
		this.focus = focus;
	}

	@Override
	public Ingredient kjs$asIngredient() {
		return ingredient;
	}

	public InputParticle withCount(int count) {
		return count == this.count ? this : new InputParticle(ingredient, count, particleName, meanEnergy, focus);
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

	@Override
	public JsonElement toJson(Context cx) {
		return toJsonJS(true);
	}

	@RemapForJS("toJson")
	public JsonElement toJsonJS(boolean alwaysNest) {
		if (!alwaysNest && count == 1) {
			return Ingredient.CODEC.encodeStart(com.mojang.serialization.JsonOps.INSTANCE, ingredient).getOrThrow();
		} else {
			var o = new JsonObject();
			o.addProperty("count", count);
			o.add("ingredient", Ingredient.CODEC.encodeStart(com.mojang.serialization.JsonOps.INSTANCE, ingredient).getOrThrow());
			return o;
		}
	}
}
