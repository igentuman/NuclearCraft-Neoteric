package igentuman.nc.compat.kubejs;

import com.google.gson.JsonElement;
import dev.latvian.mods.kubejs.core.RecipeManagerKJS;
import dev.latvian.mods.kubejs.event.EventGroupRegistry;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.RecipesKubeEvent;
import dev.latvian.mods.kubejs.recipe.component.ComponentRole;
import dev.latvian.mods.kubejs.recipe.component.NumberComponent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponentTypeRegistry;
import dev.latvian.mods.kubejs.recipe.component.SizedFluidIngredientComponent;
import dev.latvian.mods.kubejs.recipe.component.SizedIngredientComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchemaRegistry;
import igentuman.nc.NuclearCraft;
import igentuman.nc.recipes.NcRecipeType;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * NuclearCraft's KubeJS plugin. Registers a {@link RecipeSchema} for every recipe type in
 * {@link NcRecipeType#ALL_RECIPES} so that scripts can read, create, remove, and replace NC
 * recipes from JS. Also registers the custom particle components and a bridge for NC's
 * runtime-synthesized fuel recipes.
 *
 * <p>Schema matrix (see {@code docs/kubejs-port-plan.md} §7.1):
 * <ul>
 *   <li><b>A (timeModifier)</b> — generic, most recipes</li>
 *   <li><b>B (heatRequired)</b> — fission_boiling, turbine_controller</li>
 *   <li><b>C (coolingRate)</b> — fusion_coolant, accelerator_coolant</li>
 *   <li><b>D (timeModifier + temperature)</b> — fusion_core</li>
 *   <li><b>E (timeModifier + particles + crossSection + maxEnergy)</b> — target_chamber</li>
 * </ul>
 */
public class NuclearCraftKubeJSPlugin implements KubeJSPlugin {

	@Override
	public void registerEvents(EventGroupRegistry registry) {
		registry.register(NCKubeJsEvents.GROUP);
	}

	@Override
	public void registerRecipeComponents(RecipeComponentTypeRegistry registry) {
		registry.register(ParticleComponents.Input.TYPE);
		registry.register(ParticleComponents.Output.TYPE);
	}

	@Override
	public void registerRecipeSchemas(RecipeSchemaRegistry registry) {
		RecipeSchema schemaA = buildSchema("timeModifier", List.of());
		RecipeSchema schemaB = buildSchema("heatRequired", List.of());
		RecipeSchema schemaC = buildSchema("coolingRate", List.of());
		RecipeSchema schemaD = buildSchema("timeModifier", List.of(
				NumberComponent.DOUBLE
						.key("temperature", ComponentRole.OTHER).defaultOptional()
		));
		RecipeSchema schemaE = buildSchema("timeModifier", List.of(
				ParticleComponents.Input.TYPE.instance().asList()
						.key("inputParticles", ComponentRole.INPUT).defaultOptional(),
				ParticleComponents.Output.TYPE.instance().asList()
						.key("outputParticles", ComponentRole.OUTPUT).defaultOptional(),
				NumberComponent.DOUBLE
						.key("crossSection", ComponentRole.OTHER).defaultOptional(),
				NumberComponent.LONG
						.key("maxEnergy", ComponentRole.OTHER).defaultOptional()
		));

		for (String recipeType : NcRecipeType.ALL_RECIPES.keySet()) {
			RecipeSchema schema = switch (recipeType) {
				case "fission_boiling", "turbine_controller" -> schemaB;
				case "fusion_coolant", "accelerator_coolant" -> schemaC;
				case "fusion_core" -> schemaD;
				case "target_chamber" -> schemaE;
				default -> schemaA;
			};
			registry.register(
					ResourceLocation.fromNamespaceAndPath(NuclearCraft.MODID, recipeType),
					schema);
		}
	}

	@Override
	public void beforeRecipeLoading(RecipesKubeEvent event, RecipeManagerKJS manager,
	                                Map<ResourceLocation, JsonElement> recipeJsons) {
		NcRecipeType.invalidateCache();
		NCKubeJsEvents.generateCustomFuelRecipes(recipeJsons);
	}

	@Override
	public void afterInit() {
		NcRecipeType.invalidateCache();
	}

	/**
	 * Build a recipe schema with the common item/fluid/modifier keys, parameterized by the name
	 * of the "timing" field (which varies by recipe type — see §6.5-A of the port plan).
	 *
	 * @param timingKeyName JSON key name for the timing/rate modifier
	 *                      ({@code timeModifier}, {@code heatRequired}, or {@code coolingRate})
	 * @param extraKeys     Additional keys appended after the common set (recipe-type-specific)
	 */
	private static RecipeSchema buildSchema(String timingKeyName, List<RecipeKey<?>> extraKeys) {
		List<RecipeKey<?>> keys = new ArrayList<>();

		keys.add(SizedIngredientComponent.FLAT.instance().asList()
				.key("input", ComponentRole.INPUT).defaultOptional());
		keys.add(SizedIngredientComponent.FLAT.instance().asList()
				.key("output", ComponentRole.OUTPUT).defaultOptional());
		keys.add(SizedFluidIngredientComponent.FLAT.instance().asList()
				.key("inputFluids", ComponentRole.INPUT).defaultOptional());
		keys.add(SizedFluidIngredientComponent.FLAT.instance().asList()
				.key("outputFluids", ComponentRole.OUTPUT).defaultOptional());

		// Unbounded double ranges — real NC recipes have powerModifier values in the millions
		// (fusion cores), radiation values down to -10000 (gas scrubbers), etc. Any narrow
		// range here silently corrupts those recipes on round-trip via "fall back to default".
		keys.add(NumberComponent.DOUBLE
				.key(timingKeyName, ComponentRole.OTHER).defaultOptional());
		keys.add(NumberComponent.DOUBLE
				.key("powerModifier", ComponentRole.OTHER).defaultOptional());
		keys.add(NumberComponent.DOUBLE
				.key("radiation", ComponentRole.OTHER).defaultOptional());
		keys.add(NumberComponent.DOUBLE
				.key("rarityModifier", ComponentRole.OTHER).defaultOptional());

		keys.addAll(extraKeys);

		return new RecipeSchema(keys.toArray(new RecipeKey<?>[0]));
	}
}
