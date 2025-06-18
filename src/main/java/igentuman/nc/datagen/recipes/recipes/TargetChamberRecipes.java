package igentuman.nc.datagen.recipes.recipes;

import igentuman.nc.content.particles.Particle;
import igentuman.nc.content.particles.ParticleStack;
import igentuman.nc.recipes.ingredient.FluidStackIngredient;
import igentuman.nc.recipes.ingredient.NcIngredient;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static igentuman.nc.content.materials.Materials.*;
import static igentuman.nc.content.particles.Particles.*;
import static igentuman.nc.setup.registration.FissionFuel.NC_WASTE;
import static igentuman.nc.setup.registration.NCItems.NC_PARTS;

public class TargetChamberRecipes extends AbstractRecipeProvider {

    public static void generate(Consumer<FinishedRecipe> consumer) {
        TargetChamberRecipes.consumer = consumer;
        ID = "target_chamber";

        Map<NcIngredient, NcIngredient> spallationMaterials = new HashMap<>();
        spallationMaterials.put(isotopeStack(californium252), waste("californium"));
        spallationMaterials.put(isotopeStack(californium251), waste("californium"));
        spallationMaterials.put(isotopeStack(californium250), waste("californium"));
        spallationMaterials.put(isotopeStack(californium249), waste("californium"));
        spallationMaterials.put(isotopeStack(berkelium248), waste("berkelium"));
        spallationMaterials.put(isotopeStack(berkelium247), waste("berkelium"));
        spallationMaterials.put(isotopeStack(curium247), waste("curium"));
        spallationMaterials.put(isotopeStack(curium246), waste("curium"));
        spallationMaterials.put(isotopeStack(curium245), waste("curium"));
        spallationMaterials.put(isotopeStack(curium243), waste("curium"));
        spallationMaterials.put(isotopeStack(americium243), waste("americium"));
        spallationMaterials.put(isotopeStack(americium242), waste("americium"));
        spallationMaterials.put(isotopeStack(americium241), waste("americium"));
        spallationMaterials.put(isotopeStack(plutonium242), waste("plutonium"));
        spallationMaterials.put(isotopeStack(plutonium241), waste("plutonium"));
        spallationMaterials.put(isotopeStack(plutonium239), waste("plutonium"));
        spallationMaterials.put(isotopeStack(plutonium238), waste("plutonium"));
        spallationMaterials.put(isotopeStack(neptunium237), waste("neptunium"));
        spallationMaterials.put(isotopeStack(neptunium236), waste("neptunium"));
        spallationMaterials.put(isotopeStack(uranium238), waste("uranium"));
        spallationMaterials.put(isotopeStack(uranium235), waste("uranium"));
        spallationMaterials.put(isotopeStack(uranium234), waste("uranium"));
        spallationMaterials.put(isotopeStack(uranium233), waste("uranium"));
        spallationMaterials.put(dustIngredient(protactinium_231), waste("protactinium"));
        spallationMaterials.put(dustIngredient(protactinium_233), waste("protactinium"));
        spallationMaterials.put(ingotIngredient(thorium), waste("thorium"));
        spallationMaterials.put(dustIngredient("radium"), waste("radium"));
        spallationMaterials.put(dustIngredient("polonium"), waste("polonium"));
        spallationMaterials.put(dustIngredient("bismuth"), waste("bismuth"));
        spallationMaterials.put(ingotIngredient("lead"), waste("lead"));
        spallationMaterials.put(ingotIngredient("mercury"), waste("mercury"));
        spallationMaterials.put(ingotIngredient("gold"), waste("gold"));
        spallationMaterials.put(ingotIngredient("platinum"), waste("platinum"));
        spallationMaterials.put(ingotIngredient("iridium"), waste("iridium"));
        spallationMaterials.put(ingotIngredient("iridium192"), waste("iridium"));
        spallationMaterials.put(ingotIngredient("osmium"), waste("osmium"));
        spallationMaterials.put(ingotIngredient("tungsten"), waste("tungsten"));
        spallationMaterials.put(ingotIngredient("hafnium"), waste("hafnium"));

        // Example recipe from original file
        targetChamberItems(ingotIngredient("uranium"), particles(2000000, 0.1d, 100000, positron),
            List.of(particles(1000000, 0.05d, 50000, neutron)), ingotIngredient("thorium"), 50000000, 10);

        // Proton reactions
        targetChamberItems(isotopeIngredient(boron11), particles(400, 1.0, 1, proton),
            List.of(particles(1, 1.0, 3, alpha)), NcIngredient.stack(ItemStack.EMPTY), 900, 0.2);

        targetChamberItems(ingotIngredient("beryllium"), particles(1000, 1.0, 1, proton),
            List.of(particles(1, 1.0, 1, alpha)), ingotIngredient(lithium6), 7500, 0.625);

        targetChamberItems(ingotIngredient("aluminum"), particles(1500, 1.0, 1, proton),
            List.of(particles(1, 1.0, 1, photon)), ingotIngredient("silicon"), 2000, 0.02);

        targetChamberFluids(List.of(fluidIngredient("tritium", 1000)), particles(2400, 1.0, 1, proton),
            List.of(particles(1, 1.0, 1, helion), particles(1, 1.0, 1, neutron)), List.of(), 3600, 0.5);

        targetChamberItems(isotopeIngredient(boron10), particles(4000, 1.0, 1, proton),
            List.of(particles(1, 1.0, 1, alpha)), isotopeIngredient(beryllium_7), 6000, 0.8);

        targetChamberFluids(List.of(fluidIngredient("fluorine", 1000)), particles(4000, 1.0, 1, proton),
            List.of(particles(1, 1.0, 1, alpha)), List.of(fluidIngredient("oxygen", 1000)), 11000, 0.5);

        targetChamberItems(ingotIngredient("copper"), particles(4500, 1.0, 1, proton),
            List.of(particles(1, 1.0, 1, photon)), ingotIngredient("zinc"), 5900, 0.04);

        targetChamberItems(ingotIngredient("cobalt"), particles(4600, 1.0, 1, proton),
            List.of(particles(1, 1.0, 1, photon)), ingotIngredient("nickel"), 5600, 0.2);

        targetChamberFluids(List.of(fluidIngredient("deuterium", 1000)), particles(6000, 1.0, 1, proton),
            List.of(particles(1, 1.0, 1, helion), particles(1, 1.0, 1, photon)), List.of(), 10000, 0.02);

        targetChamberItems(ingotIngredient("osmium"), particles(8300, 1.0, 1, proton),
            List.of(particles(1, 1.0, 1, neutron)), ingotIngredient("iridium192"), 10300, 0.16);

        targetChamberItems(ingotIngredient("manganese"), particles(10000, 1.0, 1, proton),
            List.of(particles(1, 1.0, 1, photon)), ingotIngredient("iron"), 19500, 0.02);

        targetChamberFluids(List.of(fluidIngredient("deuterium", 1000)), particles(11000, 1.0, 1, proton),
            List.of(particles(2, 1.0, 1, proton), particles(1, 1.0, 1, neutron)), List.of(), 20500, 0.16);

        targetChamberItems(ingotIngredient("thorium"), particles(11500, 1.0, 1, proton),
            List.of(particles(2, 1.0, 1, neutron)), dustIngredient(protactinium_231), 16000, 0.625);

        targetChamberItems(ingotIngredient("uranium238"), particles(12000, 1.0, 1, proton),
            List.of(particles(2, 1.0, 1, neutron)), isotopeIngredient(neptunium237), 16500, 0.32);

        // Neutron reactions
        targetChamberFluids(List.of(fluidIngredient("helium_3", 1000)), particles(0, 1.0, 1, neutron),
            List.of(particles(1, 1.0, 1, proton), particles(1, 1.0, 1, triton)), List.of(), 18000, 0.1);

        targetChamberItems(isotopeIngredient(beryllium_7), particles(0, 1.0, 1, neutron),
            List.of(particles(1, 1.0, 1, proton)), isotopeIngredient(lithium7), 10000, 1.0);

        targetChamberItems(isotopeIngredient(boron10), particles(0, 1.0, 1, neutron),
            List.of(particles(1, 1.0, 1, photon)), isotopeIngredient(boron11), 1000, 0.02);

        // Add more neutron reactions...

        // Photon reactions
        targetChamberFluids(List.of(fluidIngredient("deuterium", 1000)), particles(2500, 1.0, 1, photon),
            List.of(particles(1, 1.0, 1, proton), particles(1, 1.0, 1, neutron)), List.of(), 14000, 0.02);

        targetChamberItems(isotopeIngredient(lithium6), particles(5500, 1.0, 1, photon),
            List.of(particles(1, 1.0, 1, alpha), particles(1, 1.0, 1, neutron), particles(1, 1.0, 1, proton)), NcIngredient.stack(ItemStack.EMPTY), 17000, 0.02);

        // Add more photon reactions...

        // Electron reactions
        targetChamberItems(ingotIngredient("iron"), particles(50000, 1.0, 1, electron),
            List.of(particles(1, 1.0, 1, alpha), particles(1, 1.0, 1, electron)), ingotIngredient("chromium"), 100000, 0.01);

        // Add more electron reactions...

        // Deuteron reactions
        targetChamberFluids(List.of(fluidIngredient("tritium", 1000)), particles(50, 1.0, 1, deuteron),
            List.of(particles(1, 1.0, 1, alpha), particles(1, 1.0, 1, neutron)), List.of(), 600, 0.5);

        // Add more deuteron reactions...

        // Triton reactions
        targetChamberItems(ingotIngredient("beryllium"), particles(1000, 1.0, 1, triton),
            List.of(particles(1, 1.0, 1, neutron)), isotopeIngredient(boron11), 7000, 1.0);

        // Add more triton reactions...

        // Helion reactions
        targetChamberItems(isotopeIngredient(lithium6), particles(11000, 1.0, 1, helion),
            List.of(particles(1, 1.0, 1, deuteron)), isotopeIngredient(beryllium_7), 30000, 0.5);

        // Add more helion reactions...

        // Alpha reactions
        targetChamberItems(ingotIngredient("beryllium"), particles(4000, 1.0, 1, alpha),
            List.of(particles(1, 1.0, 1, neutron)), dustIngredient("graphite"), 6500, 1.0);

        // Add more alpha reactions...

        // Boron Ion reactions
        targetChamberItems(ingredient(NC_PARTS.get("silicon_wafer").get()), particles(600, 2.0, 1, boron_ion),
            List.of(), ingredient(NC_PARTS.get("silicon_p_doped").get()), 1000, 1.0);

        // Ca-48 reactions
        targetChamberItems(isotopeIngredient(berkelium248), particles(40000, 2.0, 1, calcium_48_ion),
            List.of(particles(1, 1.0, 1, alpha), particles(1, 1.0, 1, neutron), particles(2, 1.0, 1, electron_neutrino)),
                isotopeIngredient(copernicium291), 50000, 0.02);

        // Electron antineutrino reactions
        targetChamberItems(isotopeIngredient(iridium_192), particles(0, 1.0, 1, electron_antineutrino),
            List.of(particles(1, 1.0, 1, positron)), ingotIngredient("osmium"), 10000, 0.01);

        // Electron neutrino reactions
        targetChamberItems(isotopeIngredient(curium247), particles(500, 1.0, 1, electron_neutrino),
            List.of(particles(1, 1.0, 1, electron)), isotopeIngredient(berkelium247), 10500, 0.01);

        // Pion reactions
        targetChamberItems(ingotIngredient("aluminum"), particles(150000, 1.0, 1, pion_minus),
            List.of(particles(1, 1.0, 1, proton), particles(1, 1.0, 1, neutron)), isotopeIngredient(sodium_22), 250000, 0.025);
            
        // Antiproton reactions
        for (Map.Entry<NcIngredient, NcIngredient> entry : spallationMaterials.entrySet()) {
            targetChamberItems(entry.getKey(), particles(1, 1.0, 1, antiproton),
                List.of(particles(1, 1.0, 1, pion_plus), particles(1, 1.0, 1, pion_naught), particles(1, 1.0, 1, pion_minus)), 
                entry.getValue(), 10000000, 1.0);
        }
        
        // Antideuteron reactions
        for (Map.Entry<NcIngredient, NcIngredient> entry : spallationMaterials.entrySet()) {
            targetChamberItems(entry.getKey(), particles(1, 1.0, 1, antideuteron),
                List.of(particles(4, 1.0, 1, pion_plus), particles(4, 1.0, 1, pion_naught), particles(4, 1.0, 1, pion_minus)), 
                entry.getValue(), 10000000, 1.0);
        }
    }

    private static NcIngredient waste(String name) {
        return NcIngredient.of(NC_WASTE.get(name).get());
    }

    private static ParticleStack particles(int energy, double focus, int amount, Particle particles) {
        return new ParticleStack(particles, amount, energy, focus);
    }

    private static void targetChamberItems(NcIngredient inputItems, ParticleStack inputParticle, List<ParticleStack> outputParticles, NcIngredient outputItems, long maxEnergy, double crossSection) {
        targetChamber(List.of(), List.of(inputItems), List.of(inputParticle), outputParticles, List.of(), List.of(outputItems), maxEnergy, crossSection);
    }

    private static void targetChamberFluids(List<FluidStackIngredient> inputFluids, ParticleStack inputParticle, List<ParticleStack> outputParticles, List<FluidStackIngredient> outputFluids, long maxEnergy, double crossSection) {
        targetChamber(inputFluids, List.of(), List.of(inputParticle), outputParticles, outputFluids, List.of(), maxEnergy, crossSection);
    }
}
