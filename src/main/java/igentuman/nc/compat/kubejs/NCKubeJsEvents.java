package igentuman.nc.compat.kubejs;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.event.*;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.recipe.RecipesEventJS;
import dev.latvian.mods.kubejs.script.ScriptType;
import igentuman.nc.NuclearCraft;
import igentuman.nc.block.kugelblitz.entity.BlackHoleBE;
import igentuman.nc.content.fuel.FuelDef;
import igentuman.nc.content.particles.ParticleSources;
import igentuman.nc.content.particles.ParticleStack;
import igentuman.nc.content.particles.Particles;
import igentuman.nc.setup.registration.FissionFuel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;


public class NCKubeJsEvents {
    public static final EventGroup GROUP = EventGroup.of("NCKJSEvents");
    public static final EventHandler PLAYER_ENTER_BLACKHOLE = GROUP.server("PlayerEnterBlackhole", () -> PlayerEnterBlackholeEventJS.class);
    public static final EventHandler REGISTER_FISSION_FUEL = GROUP.startup("RegisterFissionFuel", () -> RegisterFissionFuelEventJS.class);
    public static final EventHandler REGISTER_PARTICLE_SOURCE_ITEM = GROUP.startup("registerParticleSourceItem", () -> RegisterParticleSourceItemEventJS.class);
    public static final EventHandler REGISTER_PARTICLE_SOURCE_FLUID = GROUP.startup("registerParticleSourceFluid", () -> RegisterParticleSourceFluidEventJS.class);

    public static void onPlayerEnterBlackhole(BlackHoleBE.PlayerEnterBlackholeEvent event) {
        PlayerEnterBlackholeEventJS evenjs = new PlayerEnterBlackholeEventJS(event.getPlayer(), event.getBlackholePos(), event.getLevel());
        EventResult result = PLAYER_ENTER_BLACKHOLE.post(ScriptType.SERVER, evenjs);
        event.setCanceled(result.interruptDefault() || result.interruptFalse() || result.interruptTrue());
    }

    public static void onFissionFuelRegister(FissionFuel.RegisterFissionFuelEvent event) {
        RegisterFissionFuelEventJS eventJS = new RegisterFissionFuelEventJS(event);
        REGISTER_FISSION_FUEL.post(ScriptType.STARTUP, eventJS);
        System.out.println("Registered custom fission fuels via KubeJS");
    }

    public static void onParticleItemSourcesRegister(ParticleSources.RegisterParticleSourceItemEvent event) {
        RegisterParticleSourceItemEventJS eventJS = new RegisterParticleSourceItemEventJS(event);
        REGISTER_PARTICLE_SOURCE_ITEM.post(ScriptType.STARTUP, eventJS);
        System.out.println("Registered custom particle item sources via KubeJS");
    }

    public static void onParticleFluidSourcesRegister(ParticleSources.RegisterParticleSourceFluidEvent event) {
        RegisterParticleSourceFluidEventJS eventJS = new RegisterParticleSourceFluidEventJS(event);
        REGISTER_PARTICLE_SOURCE_FLUID.post(ScriptType.STARTUP, eventJS);
        System.out.println("Registered custom particle fluid sources via KubeJS");
    }
    
    /**
     * Automatically generate fission reactor recipes for custom fuels
     * This should be called during recipe loading
     */
    public static List<RecipeJS> generateCustomFuelRecipes(RecipesEventJS event) {
        List<RecipeJS> addedRecipes = new ArrayList<>();
        if(event == null) {
            NuclearCraft.LOGGER.warn("RecipesEventJS instance is null, cannot generate custom fuel recipes");
            return addedRecipes;
        }
        List<FuelDef> customFuels = FissionFuel.getCustomFuels();
        
        for (FuelDef fuelDef : customFuels) {
            String group = fuelDef.group;
            String name = fuelDef.name;
            
            // Generate recipe for base fuel
            addedRecipes.add(generateFuelRecipe(event, group, name, "", fuelDef));

            addedRecipes.add(generateFuelRecipe(event, group, name, "_ox", fuelDef));
            addedRecipes.add(generateFuelRecipe(event, group, name, "_ni", fuelDef));
            addedRecipes.add(generateFuelRecipe(event, group, name, "_za", fuelDef));
        }
        return addedRecipes;
    }
    
    private static RecipeJS generateFuelRecipe(RecipesEventJS event, String group, String name,
                                               String variant, FuelDef fuelDef) {
        NuclearCraft.LOGGER.warn("Generating recipe for fuel: " + group + " " + name + variant);
        String fuelItem = "nuclearcraft:fuel_" + group + "_" + name + variant;
        String depletedItem = "nuclearcraft:depleted_fuel_" + group + "_" + name + variant;
        String recipeId = "nuclearcraft:fission_reactor_controller/" + group + "_" + name + variant;
        
        // Create recipe JSON
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
        
        return event.custom(recipe).id(ResourceLocation.tryParse(recipeId));
    }

    public static class PlayerEnterBlackholeEventJS extends EventJS {
        public ServerPlayer getPlayer() {
            return player;
        }

        public BlockPos getBlackholePos() {
            return blackholePos;
        }

        public Level getLevel() {
            return level;
        }

        private final ServerPlayer player;
        private final BlockPos blackholePos;
        private final Level level;

        public PlayerEnterBlackholeEventJS(ServerPlayer player, BlockPos blackholePos, Level level) {
            this.player = player;
            this.blackholePos = blackholePos;
            this.level = level;
        }
    }
    
    public static class RegisterFissionFuelEventJS extends StartupEventJS {
        private final FissionFuel.RegisterFissionFuelEvent event;

        public RegisterFissionFuelEventJS(FissionFuel.RegisterFissionFuelEvent event) {
            this.event = event;
        }

        /**
         * Register a custom fission fuel with automatic recipe generation
         * @param group The fuel group (e.g., "tbu", "leu235")
         * @param name The fuel name/subtype (e.g., "oxide", "le")
         * @param forgeEnergy Energy produced per tick
         * @param heat Heat generated
         * @param criticality Criticality value
         * @param depletion Depletion rate
         * @param efficiency Efficiency value
         * @param isotope1 First isotope ID
         * @param isotope2 Second isotope ID
         */
        public void registerFuel(String group, String name, int forgeEnergy, double heat, 
                                int criticality, int depletion, int efficiency, 
                                int isotope1, int isotope2) {
            FuelDef fuelDef = new FuelDef(group, name, forgeEnergy, heat, criticality, depletion, efficiency);
            fuelDef.isotopes(isotope1, isotope2);
            event.addFuel(fuelDef);
        }
        
        /**
         * Register a custom fission fuel with double values and automatic recipe generation
         * @param group The fuel group (e.g., "tbu", "leu235")
         * @param name The fuel name/subtype (e.g., "oxide", "le")
         * @param forgeEnergy Energy produced per tick
         * @param heat Heat generated
         * @param criticality Criticality value
         * @param depletion Depletion rate
         * @param efficiency Efficiency value
         * @param isotope1 First isotope ID
         * @param isotope2 Second isotope ID
         */
        public void registerFuel(String group, String name, int forgeEnergy, double heat, 
                                double criticality, double depletion, double efficiency, 
                                int isotope1, int isotope2) {
            FuelDef fuelDef = new FuelDef(group, name, forgeEnergy, heat, criticality, depletion, efficiency);
            fuelDef.isotopes(isotope1, isotope2);
            event.addFuel(fuelDef);
        }
        
        /**
         * Register a custom fission fuel with custom recipe modifiers
         * @param group The fuel group (e.g., "tbu", "leu235")
         * @param name The fuel name/subtype (e.g., "oxide", "le")
         * @param forgeEnergy Energy produced per tick
         * @param heat Heat generated
         * @param criticality Criticality value
         * @param depletion Depletion rate
         * @param efficiency Efficiency value
         * @param isotope1 First isotope ID
         * @param isotope2 Second isotope ID
         * @param timeModifier Time modifier for recipe (default: 1.0)
         * @param powerModifier Power modifier for recipe (default: 1.0)
         * @param radiationModifier Radiation modifier for recipe (default: 1.0)
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
         * Register a custom fission fuel with double values and custom recipe modifiers
         * @param group The fuel group (e.g., "tbu", "leu235")
         * @param name The fuel name/subtype (e.g., "oxide", "le")
         * @param forgeEnergy Energy produced per tick
         * @param heat Heat generated
         * @param criticality Criticality value
         * @param depletion Depletion rate
         * @param efficiency Efficiency value
         * @param isotope1 First isotope ID
         * @param isotope2 Second isotope ID
         * @param timeModifier Time modifier for recipe (default: 1.0)
         * @param powerModifier Power modifier for recipe (default: 1.0)
         * @param radiationModifier Radiation modifier for recipe (default: 1.0)
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

    public static class RegisterParticleSourceItemEventJS extends StartupEventJS {
        private final ParticleSources.RegisterParticleSourceItemEvent event;

        public RegisterParticleSourceItemEventJS(ParticleSources.RegisterParticleSourceItemEvent event) {
            this.event = event;
        }

        public void add(String itemName, String particleName, int amount, long meanEnergy, double focus) {
            System.out.println("Adding particle source item via KubeJS: " + itemName + " " + particleName);
            event.addParticleSourceItem(itemName, new ParticleStack(Particles.getParticleFromName(particleName), amount, meanEnergy, focus));
        }

        public void add(String itemName, String particleName, int amount, long meanEnergy) {
            System.out.println("Adding particle source item via KubeJS: " + itemName + " " + particleName);
            event.addParticleSourceItem(itemName, new ParticleStack(Particles.getParticleFromName(particleName), amount, meanEnergy));
        }

        public void add(String itemName, String particleName, int amount) {
            System.out.println("Adding particle source item via KubeJS: " + itemName + " " + particleName);
            event.addParticleSourceItem(itemName, new ParticleStack(Particles.getParticleFromName(particleName), amount));
        }
    }

    public static class RegisterParticleSourceFluidEventJS extends StartupEventJS {
        private final ParticleSources.RegisterParticleSourceFluidEvent event;

        public RegisterParticleSourceFluidEventJS(ParticleSources.RegisterParticleSourceFluidEvent event) {
            this.event = event;
        }

        public void add(String fluidName, String particleName, int amount, long meanEnergy, double focus) {
            event.addParticleSourceFluid(fluidName, new ParticleStack(Particles.getParticleFromName(particleName), amount, meanEnergy, focus));
        }

        public void add(String fluidName, String particleName, int amount, long meanEnergy) {
            event.addParticleSourceFluid(fluidName, new ParticleStack(Particles.getParticleFromName(particleName), amount, meanEnergy));
        }

        public void add(String fluidName, String particleName, int amount) {
            event.addParticleSourceFluid(fluidName, new ParticleStack(Particles.getParticleFromName(particleName), amount));
        }
    }
}
