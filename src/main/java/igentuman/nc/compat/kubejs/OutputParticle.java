package igentuman.nc.compat.kubejs;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.kubejs.script.ConsoleJS;
import igentuman.nc.content.particles.ParticleStack;
import igentuman.nc.content.particles.Particles;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;

/**
 * KubeJS output-side representation of a particle for NC's target chamber recipes.
 *
 * <p>Like {@link InputParticle}, particles have no real item equivalent; this class only carries
 * particle metadata (type, amount, energy, focus) and an optional chance/rolls modifier.
 */
public class OutputParticle {
	public static final OutputParticle EMPTY = new OutputParticle(ParticleStack.EMPTY, Double.NaN, null);

	/**
	 * JSON codec for round-tripping particle output data through KubeJS list components.
	 * Shape: {@code {"particle": "proton", "amount": 100, "meanEnergy": 1000000, "focus": 0.5}}.
	 * NC's {@code ParticleStack.fromJSON} reads only these four fields — {@link #chance} and {@link #rolls}
	 * are JS-side-only modifiers and do not round-trip to NC.
	 */
	public static final Codec<OutputParticle> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.fieldOf("particle").forGetter(op -> op.item.getParticle() != null ? op.item.getParticle().getName() : ""),
			Codec.INT.fieldOf("amount").forGetter(op -> op.item.getAmount()),
			Codec.LONG.fieldOf("meanEnergy").forGetter(op -> op.item.getMeanEnergy()),
			Codec.DOUBLE.fieldOf("focus").forGetter(op -> op.item.getFocus())
	).apply(instance, (name, count, meanEnergy, focus) -> {
		var particle = Particles.getParticleFromName(name);
		if (particle == null || count <= 0) return EMPTY;
		return new OutputParticle(new ParticleStack(particle, count, meanEnergy, focus), Double.NaN, null);
	}));

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

		if (from instanceof String str) {
			item = parseParticleFromString(str);
		} else if (from instanceof JsonObject json) {
			item = parseParticleFromJson(json);
		} else {
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
		// "proton:100:1000:0.5" → (particle:amount:energy:focus)
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
		// {"particle": "proton", "amount": 100, "meanEnergy": 1000, "focus": 0.5}
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

	@Deprecated
	public InputParticle ignoreNBT() {
		ConsoleJS.SERVER.warn("Particles don't have NBT data like items!");
		return InputParticle.of(placeholder(), item.getAmount());
	}

	public InputParticle weakNBT() {
		return InputParticle.of(placeholder(), item.getAmount());
	}

	public InputParticle strongNBT() {
		return InputParticle.of(placeholder(), item.getAmount());
	}

	private static Ingredient placeholder() {
		return Ingredient.of(Items.BARRIER);
	}
}
