package igentuman.nc.compat.kubejs;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.ReplacementMatch;
import dev.latvian.mods.kubejs.recipe.component.*;
import dev.latvian.mods.kubejs.util.TinyMap;
import com.google.gson.JsonObject;

import java.util.Map;

public interface ParticleComponents {
	RecipeComponent<InputParticle> INPUT = new RecipeComponent<>() {
		@Override
		public String componentType() {
			return "input_particle";
		}

		@Override
		public ComponentRole role() {
			return ComponentRole.INPUT;
		}

		@Override
		public Class<?> componentClass() {
			return InputParticle.class;
		}

		@Override
		public boolean hasPriority(RecipeJS recipe, Object from) {
			return recipe.inputItemHasPriority(from);
		}

		@Override
		public JsonElement write(RecipeJS recipe, InputParticle value) {
			return value.toJsonJS();
		}

		@Override
		public InputParticle read(RecipeJS recipe, Object from) {
			return InputParticle.of(from);
		}

		@Override
		public boolean isInput(RecipeJS recipe, InputParticle value, ReplacementMatch match) {
			return match instanceof ParticleMatch m && value.validForMatching() && m.contains(value.ingredient);
		}

		@Override
		public String checkEmpty(RecipeKey<InputParticle> key, InputParticle value) {
			if (value.isEmpty()) {
				return "Ingredient '" + key.name + "' can't be empty!";
			}

			return "";
		}

		@Override
		public RecipeComponent<TinyMap<Character, InputParticle>> asPatternKey() {
			// Particles don't have pattern keys like items, return null or create a custom one
			return null;
		}

		@Override
		public String toString() {
			return componentType();
		}
	};

	RecipeComponent<InputParticle[]> INPUT_ARRAY = INPUT.asArray();

	RecipeComponent<InputParticle[]> UNWRAPPED_INPUT_ARRAY = new RecipeComponentWithParent<>() {
		@Override
		public RecipeComponent<InputParticle[]> parentComponent() {
			return ParticleComponents.INPUT_ARRAY;
		}

		@Override
		public JsonElement write(RecipeJS recipe, InputParticle[] value) {
			var json = new JsonArray();

			for (var in : value) {
				for (var in1 : in.unwrap()) {
					json.add(ParticleComponents.INPUT.write(recipe, in1));
				}
			}

			return json;
		}

		@Override
		public String toString() {
			return parentComponent().toString();
		}
	};

	RecipeComponent<OutputParticle> OUTPUT = new RecipeComponent<>() {
		@Override
		public String componentType() {
			return "output_particle";
		}

		@Override
		public ComponentRole role() {
			return ComponentRole.OUTPUT;
		}

		@Override
		public Class<?> componentClass() {
			return OutputParticle.class;
		}

		@Override
		public boolean hasPriority(RecipeJS recipe, Object from) {
			return recipe.outputItemHasPriority(from);
		}

		@Override
		public JsonElement write(RecipeJS recipe, OutputParticle value) {
			return writeParticleToJson(value);
		}

		@Override
		public OutputParticle read(RecipeJS recipe, Object from) {
			return OutputParticle.of(from);
		}

		@Override
		public boolean isOutput(RecipeJS recipe, OutputParticle value, ReplacementMatch match) {
			return match instanceof ParticleMatch m && !value.isEmpty() && m.contains(value.item);
		}

		@Override
		public String checkEmpty(RecipeKey<OutputParticle> key, OutputParticle value) {
			if (value.isEmpty()) {
				return "ItemStack '" + key.name + "' can't be empty!";
			}

			return "";
		}

		@Override
		public String toString() {
			return componentType();
		}
	};

	RecipeComponent<OutputParticle[]> OUTPUT_ARRAY = OUTPUT.asArray();

	RecipeComponent<OutputParticle> OUTPUT_ID_WITH_COUNT = new RecipeComponentWithParent<>() {
		@Override
		public RecipeComponent<OutputParticle> parentComponent() {
			return OUTPUT;
		}

		@Override
		public void writeToJson(RecipeJS recipe, RecipeComponentValue<OutputParticle> cv, JsonObject json) {
			if (cv.value.item.getParticle() != null) {
				json.addProperty(cv.key.name, cv.value.item.getParticle().getName());
			}
			json.addProperty("count", cv.value.item.getAmount());
		}

		@Override
		public void readFromJson(RecipeJS recipe, RecipeComponentValue<OutputParticle> cv, JsonObject json) {
			RecipeComponentWithParent.super.readFromJson(recipe, cv, json);

			if (cv.value != null && json.has("count")) {
				cv.value.item.setAmount(json.get("count").getAsInt());
			}
		}

		@Override
		public void readFromMap(RecipeJS recipe, RecipeComponentValue<OutputParticle> cv, Map<?, ?> map) {
			RecipeComponentWithParent.super.readFromMap(recipe, cv, map);

			if (cv.value != null && map.containsKey("count")) {
				cv.value.item.setAmount(((Number) map.get("count")).intValue());
			}
		}

		@Override
		public String toString() {
			return parentComponent().toString();
		}
	};

	static JsonElement writeParticleToJson(OutputParticle value) {
		if (value.isEmpty()) {
			return null;
		}
		
		JsonObject json = new JsonObject();
		if (value.item.getParticle() != null) {
			json.addProperty("particle", value.item.getParticle().getName());
		}
		json.addProperty("amount", value.item.getAmount());
		json.addProperty("meanEnergy", value.item.getMeanEnergy());
		json.addProperty("focus", value.item.getFocus());
		
		// Note: chance and rolls are typically handled at recipe level, not particle level
		// but we can include them if needed for compatibility
		if (value.hasChance()) {
			json.addProperty("chance", value.getChance());
		}
		
		return json;
	}
}
