package igentuman.nc.compat.kubejs;

import com.mojang.serialization.Codec;
import dev.latvian.mods.kubejs.recipe.RecipeScriptContext;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentType;
import dev.latvian.mods.kubejs.recipe.filter.RecipeMatchContext;
import dev.latvian.mods.kubejs.recipe.match.ReplacementMatchInfo;
import dev.latvian.mods.rhino.type.TypeInfo;
import igentuman.nc.NuclearCraft;
import net.minecraft.resources.ResourceLocation;

/**
 * KubeJS recipe components for NC's target chamber particle inputs and outputs.
 *
 * <p>Particles have no {@link net.minecraft.world.item.crafting.Ingredient} equivalent, so these
 * components are custom — they round-trip the four-field particle JSON shape
 * ({@code particle}/{@code amount}/{@code meanEnergy}/{@code focus}) via a direct codec on
 * {@link InputParticle} and {@link OutputParticle}. Matching participates in KubeJS's replacement
 * machinery via {@link ParticleMatch}.
 *
 * <p>The two {@link RecipeComponentType} handles are registered by
 * {@link NuclearCraftKubeJSPlugin#registerRecipeComponents}.
 */
public final class ParticleComponents {

	private ParticleComponents() {}

	/** Input-side particle component record. Immutable; the type reference is injected at registration time. */
	public record Input(RecipeComponentType<?> type) implements RecipeComponent<InputParticle> {

		public static final RecipeComponentType<InputParticle> TYPE = RecipeComponentType.unit(
				ResourceLocation.fromNamespaceAndPath(NuclearCraft.MODID, "input_particle"),
				Input::new);

		@Override
		public Codec<InputParticle> codec() {
			return InputParticle.CODEC;
		}

		@Override
		public TypeInfo typeInfo() {
			return TypeInfo.of(InputParticle.class);
		}

		@Override
		public InputParticle wrap(RecipeScriptContext cx, Object from) {
			return InputParticle.of(from);
		}

		@Override
		public boolean matches(RecipeMatchContext cx, InputParticle value, ReplacementMatchInfo match) {
			return match.match() instanceof ParticleMatch m
					&& value.validForMatching()
					&& m.contains(value.ingredient);
		}

		@Override
		public boolean isEmpty(InputParticle value) {
			return value.isEmpty();
		}

		@Override
		public boolean allowEmpty() {
			// Target chamber recipes may omit input particles entirely.
			return true;
		}

		@Override
		public String toString() {
			return type.toString();
		}
	}

	/** Output-side particle component record. */
	public record Output(RecipeComponentType<?> type) implements RecipeComponent<OutputParticle> {

		public static final RecipeComponentType<OutputParticle> TYPE = RecipeComponentType.unit(
				ResourceLocation.fromNamespaceAndPath(NuclearCraft.MODID, "output_particle"),
				Output::new);

		@Override
		public Codec<OutputParticle> codec() {
			return OutputParticle.CODEC;
		}

		@Override
		public TypeInfo typeInfo() {
			return TypeInfo.of(OutputParticle.class);
		}

		@Override
		public OutputParticle wrap(RecipeScriptContext cx, Object from) {
			return OutputParticle.of(from);
		}

		@Override
		public boolean matches(RecipeMatchContext cx, OutputParticle value, ReplacementMatchInfo match) {
			return match.match() instanceof ParticleMatch m
					&& !value.isEmpty()
					&& m.contains(value.item);
		}

		@Override
		public boolean isEmpty(OutputParticle value) {
			return value.isEmpty();
		}

		@Override
		public boolean allowEmpty() {
			return true;
		}

		@Override
		public String toString() {
			return type.toString();
		}
	}
}
