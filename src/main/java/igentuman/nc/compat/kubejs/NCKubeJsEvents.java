package igentuman.nc.compat.kubejs;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;
import dev.latvian.mods.kubejs.event.EventResult;
import dev.latvian.mods.kubejs.event.KubeEvent;
import dev.latvian.mods.kubejs.event.KubeStartupEvent;
import dev.latvian.mods.kubejs.script.ScriptType;
import igentuman.nc.NuclearCraft;
import igentuman.nc.block.kugelblitz.entity.BlackHoleBE;
import igentuman.nc.content.fuel.FuelDef;
import igentuman.nc.setup.registration.FissionFuel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Map;

/**
 * KubeJS event bridges for NuclearCraft.
 *
 * <p>This class declares a custom {@link EventGroup} ({@code NCKJSEvents}) that scripts can
 * subscribe to via e.g. {@code NCKJSEvents.playerEnterBlackhole(event => ...)}. The static
 * methods {@link #onPlayerEnterBlackhole} and {@link #onFissionFuelRegister} are called from
 * NC's own Java code (guarded by {@code ModList.isLoaded("kubejs")}) to forward NeoForge-bus
 * events into the KubeJS script environment.
 *
 * <p>{@link #generateCustomFuelRecipes} is invoked from
 * {@link NuclearCraftKubeJSPlugin#beforeRecipeLoading} and writes synthesized fission-fuel
 * recipes directly into the JSON recipe map that KubeJS hands to the vanilla recipe loader.
 */
public final class NCKubeJsEvents {

	public static final EventGroup GROUP = EventGroup.of("NCKJSEvents");

	public static final EventHandler PLAYER_ENTER_BLACKHOLE = GROUP.server(
			"playerEnterBlackhole", () -> PlayerEnterBlackholeEventJS.class);

	public static final EventHandler REGISTER_FISSION_FUEL = GROUP.startup(
			"registerFissionFuel", () -> RegisterFissionFuelEventJS.class);

	private NCKubeJsEvents() {}

	/** Forward a {@link BlackHoleBE.PlayerEnterBlackholeEvent} from NC's NeoForge bus into KubeJS scripts. */
	public static void onPlayerEnterBlackhole(BlackHoleBE.PlayerEnterBlackholeEvent event) {
		var eventJS = new PlayerEnterBlackholeEventJS(event.getPlayer(), event.getBlackholePos(), event.getLevel());
		EventResult result = PLAYER_ENTER_BLACKHOLE.post(ScriptType.SERVER, eventJS);
		// Any script-side interrupt (cancel / success / exit) suppresses the default kill —
		// the script is claiming responsibility for the outcome.
		if (result.interruptFalse() || result.interruptTrue() || result.interruptDefault()) {
			event.setCanceled(true);
		}
	}

	/** Forward a {@link FissionFuel.RegisterFissionFuelEvent} into KubeJS startup scripts. */
	public static void onFissionFuelRegister(FissionFuel.RegisterFissionFuelEvent event) {
		var eventJS = new RegisterFissionFuelEventJS(event);
		REGISTER_FISSION_FUEL.post(ScriptType.STARTUP, eventJS);
	}

	/**
	 * Synthesize fission reactor recipes for every fuel definition added via the
	 * {@link RegisterFissionFuelEventJS} JS API, and inject them into the recipe JSON map
	 * handed to KubeJS's {@code beforeRecipeLoading} hook.
	 */
	public static void generateCustomFuelRecipes(Map<ResourceLocation, JsonElement> recipeJsons) {
		List<FuelDef> customFuels = FissionFuel.getCustomFuels();
		if (customFuels.isEmpty()) {
			return;
		}

		NuclearCraft.LOGGER.info("Injecting {} custom fission fuel recipe groups from KubeJS", customFuels.size());

		for (FuelDef fuelDef : customFuels) {
			addFuelRecipe(recipeJsons, fuelDef, "");
			addFuelRecipe(recipeJsons, fuelDef, "_ox");
			addFuelRecipe(recipeJsons, fuelDef, "_ni");
			addFuelRecipe(recipeJsons, fuelDef, "_za");
		}
	}

	private static void addFuelRecipe(Map<ResourceLocation, JsonElement> recipeJsons, FuelDef fuelDef, String variant) {
		String group = fuelDef.group;
		String name = fuelDef.name;
		String fuelItem = "nuclearcraft:fuel_" + group + "_" + name + variant;
		String depletedItem = "nuclearcraft:depleted_fuel_" + group + "_" + name + variant;
		ResourceLocation recipeId = ResourceLocation.fromNamespaceAndPath(
				NuclearCraft.MODID, "fission_reactor_controller/" + group + "_" + name + variant);

		JsonObject recipe = new JsonObject();
		recipe.addProperty("type", "nuclearcraft:fission_reactor_controller");

		JsonArray input = new JsonArray();
		JsonObject inputItem = new JsonObject();
		inputItem.addProperty("item", fuelItem);
		input.add(inputItem);
		recipe.add("input", input);

		JsonArray output = new JsonArray();
		JsonObject outputItem = new JsonObject();
		outputItem.addProperty("item", depletedItem);
		output.add(outputItem);
		recipe.add("output", output);

		recipe.addProperty("timeModifier", fuelDef.timeModifier);
		recipe.addProperty("powerModifier", fuelDef.powerModifier);
		recipe.addProperty("radiation", fuelDef.radiationModifier);

		recipeJsons.put(recipeId, recipe);
	}

	// ---- KubeEvent implementations ----

	public static class PlayerEnterBlackholeEventJS implements KubeEvent {
		private final ServerPlayer player;
		private final BlockPos blackholePos;
		private final Level level;

		public PlayerEnterBlackholeEventJS(ServerPlayer player, BlockPos blackholePos, Level level) {
			this.player = player;
			this.blackholePos = blackholePos;
			this.level = level;
		}

		public ServerPlayer getPlayer() {
			return player;
		}

		public BlockPos getBlackholePos() {
			return blackholePos;
		}

		public Level getLevel() {
			return level;
		}
	}

	public static class RegisterFissionFuelEventJS implements KubeStartupEvent {
		private final FissionFuel.RegisterFissionFuelEvent event;

		public RegisterFissionFuelEventJS(FissionFuel.RegisterFissionFuelEvent event) {
			this.event = event;
		}

		/**
		 * Register a custom fission fuel with integer properties and default recipe modifiers.
		 */
		public void registerFuel(String group, String name, int forgeEnergy, double heat,
		                         int criticality, int depletion, int efficiency,
		                         int isotope1, int isotope2) {
			FuelDef fuelDef = new FuelDef(group, name, forgeEnergy, heat, criticality, depletion, efficiency);
			fuelDef.isotopes(isotope1, isotope2);
			event.addFuel(fuelDef);
		}

		/**
		 * Register a custom fission fuel with double properties and default recipe modifiers.
		 */
		public void registerFuel(String group, String name, int forgeEnergy, double heat,
		                         double criticality, double depletion, double efficiency,
		                         int isotope1, int isotope2) {
			FuelDef fuelDef = new FuelDef(group, name, forgeEnergy, heat, criticality, depletion, efficiency);
			fuelDef.isotopes(isotope1, isotope2);
			event.addFuel(fuelDef);
		}

		/**
		 * Register a custom fission fuel with integer properties and explicit recipe modifiers.
		 */
		public void registerFuel(String group, String name, int forgeEnergy, double heat,
		                         int criticality, int depletion, int efficiency,
		                         int isotope1, int isotope2,
		                         double timeModifier, double powerModifier, double radiationModifier) {
			FuelDef fuelDef = new FuelDef(group, name, forgeEnergy, heat, criticality, depletion, efficiency);
			fuelDef.isotopes(isotope1, isotope2);
			fuelDef.recipeModifiers(timeModifier, powerModifier, radiationModifier);
			event.addFuel(fuelDef);
		}

		/**
		 * Register a custom fission fuel with double properties and explicit recipe modifiers.
		 */
		public void registerFuel(String group, String name, int forgeEnergy, double heat,
		                         double criticality, double depletion, double efficiency,
		                         int isotope1, int isotope2,
		                         double timeModifier, double powerModifier, double radiationModifier) {
			FuelDef fuelDef = new FuelDef(group, name, forgeEnergy, heat, criticality, depletion, efficiency);
			fuelDef.isotopes(isotope1, isotope2);
			fuelDef.recipeModifiers(timeModifier, powerModifier, radiationModifier);
			event.addFuel(fuelDef);
		}
	}
}
