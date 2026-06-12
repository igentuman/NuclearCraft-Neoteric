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
import static igentuman.nc.content.particles.ParticleSources.moleAmount;
import static igentuman.nc.content.particles.Particles.*;
import static igentuman.nc.multiblock.fission.FissionReactorRegistration.FISSION_BLOCKS;
import static igentuman.nc.setup.registration.FissionFuel.NC_WASTE;
import static igentuman.nc.setup.registration.NCItems.NC_PARTS;

public class TargetChamberRecipes extends AbstractRecipeProvider {

    private static final NcIngredient EMPTY = NcIngredient.stack(ItemStack.EMPTY);

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
        spallationMaterials.put(isotopeIngredient(iridium_192), waste("iridium"));
        spallationMaterials.put(ingotIngredient("osmium"), waste("osmium"));
        spallationMaterials.put(ingotIngredient("tungsten"), waste("tungsten"));
        spallationMaterials.put(ingotIngredient("hafnium"), waste("hafnium"));

        // Example recipe
        targetChamberItems(ingotIngredient("uranium"), particles(2000000, 0.1d, 100000, positron),
            List.of(particles(1000000, 0.05d, 50000, neutron)), ingotIngredient("thorium"), 50000000, 10);

        // ===== Proton reactions =====
        targetChamberItems(isotopeIngredient(boron11), p(400, proton),
            List.of(po(3, alpha)), EMPTY, 900, 0.2);
        targetChamberItems(ingotIngredient("beryllium"), p(1000, proton),
            List.of(po(alpha)), isotopeIngredient(lithium6), 7500, 0.625);
        targetChamberItems(ingotIngredient("aluminum"), p(1500, proton),
            List.of(po(photon)), ingotIngredient("silicon"), 2000, 0.02);
        targetChamberFluids(List.of(fluidIngredient("tritium", 1000)), p(2400, proton),
            List.of(po(helion), po(neutron)), List.of(), 3600, 0.5);
        targetChamberItems(isotopeIngredient(boron10), p(4000, proton),
            List.of(po(alpha)), isotopeIngredient(beryllium_7), 6000, 0.8);
        targetChamberFluids(List.of(fluidIngredient("fluorine", 1000)), p(4000, proton),
            List.of(po(alpha)), List.of(fluidIngredient("oxygen", 1000)), 11000, 0.5);
        targetChamberItems(ingotIngredient("copper"), p(4500, proton),
            List.of(po(photon)), ingotIngredient("zinc"), 5900, 0.04);
        targetChamberItems(ingotIngredient("cobalt"), p(4600, proton),
            List.of(po(photon)), ingotIngredient("nickel"), 5600, 0.2);
        targetChamberFluids(List.of(fluidIngredient("deuterium", 1000)), p(6000, proton),
            List.of(po(helion), po(photon)), List.of(), 10000, 0.02);
        targetChamberItems(ingotIngredient("osmium"), p(8300, proton),
            List.of(po(neutron)), isotopeIngredient(iridium_192), 10300, 0.16);
        targetChamberItems(ingotIngredient("manganese"), p(10000, proton),
            List.of(po(photon)), ingotIngredient("iron"), 19500, 0.02);
        targetChamberFluids(List.of(fluidIngredient("deuterium", 1000)), p(11000, proton),
            List.of(po(2, proton), po(neutron)), List.of(), 20500, 0.16);
        targetChamberItems(ingotIngredient("thorium"), p(11500, proton),
            List.of(po(2, neutron)), dustIngredient(protactinium_231), 16000, 0.625);
        targetChamberItems(isotopeIngredient(uranium238), p(12000, proton),
            List.of(po(2, neutron)), isotopeIngredient(neptunium237), 16500, 0.32);
        targetChamberMixed(ingotIngredient("gold"), null, p(12500, proton),
            List.of(po(2, neutron)), null, fluidIngredient("mercury", 144), 14000, 0.5);
        targetChamberItems(isotopeIngredient(plutonium242), p(12500, proton),
            List.of(po(2, neutron)), isotopeIngredient(americium241), 16500, 0.4);
        targetChamberItems(dustIngredient("bismuth"), p(14000, proton),
            List.of(po(photon)), dustIngredient("polonium"), 19000, 0.02);
        targetChamberItems(isotopeIngredient(boron11), p(15500, proton),
            List.of(po(photon)), dustIngredient("graphite"), 26000, 0.02);
        targetChamberItems(ingotIngredient("calcium"), p(16500, proton),
            List.of(po(2, proton)), ingotIngredient("potassium"), 25000, 1.0);
        targetChamberMixed(null, fluidIngredient("nitrogen", 1000), p(17000, proton),
            List.of(po(2, alpha)), isotopeIngredient(beryllium_7), null, 26000, 0.032);
        targetChamberMixed(null, fluidIngredient("oxygen", 1000), p(19000, proton),
            List.of(po(alpha), po(proton)), dustIngredient("graphite"), null, 25500, 0.25);
        targetChamberItems(ingotIngredient("silicon"), p(19000, proton),
            List.of(po(2, proton)), ingotIngredient("aluminum"), 28000, 1.0);
        targetChamberItems(isotopeIngredient(uranium238), p(19000, proton),
            List.of(po(3, neutron)), isotopeIngredient(neptunium236), 23000, 1.0);
        targetChamberItems(ingotIngredient("sodium"), p(20000, proton),
            List.of(po(proton), po(neutron)), isotopeIngredient(sodium_22), 28000, 0.5);
        targetChamberItems(dustIngredient("bismuth"), p(20000, proton),
            List.of(po(alpha)), ingotIngredient("lead"), 24000, 0.04);
        targetChamberItems(isotopeIngredient(uranium235), p(20500, proton),
            List.of(po(photon)), isotopeIngredient(neptunium236), 30000, 0.02);
        targetChamberItems(ingotIngredient("gold"), p(21000, proton),
            List.of(po(alpha)), ingotIngredient("platinum"), 25000, 0.064);
        targetChamberItems(isotopeIngredient(magnesium_26), p(23000, proton),
            List.of(po(alpha), po(neutron)), isotopeIngredient(sodium_22), 29000, 0.1);
        targetChamberItems(dustIngredient("graphite"), p(27000, proton),
            List.of(po(alpha), po(neutron), po(proton)), isotopeIngredient(beryllium_7), 35000, 0.08);
        targetChamberItems(isotopeIngredient(boron11), p(30000, proton),
            List.of(po(triton), po(2, neutron)), isotopeIngredient(beryllium_7), 33000, 0.05);
        targetChamberMixed(ingotIngredient("calcium"), null, p(30000, proton),
            List.of(po(3, proton)), null, fluidIngredient("argon", 1000), 43000, 0.20);
        targetChamberItems(ingotIngredient("silicon"), p(32000, proton),
            List.of(po(3, proton)), isotopeIngredient(magnesium_26), 50000, 0.2);
        targetChamberMixed(null, fluidIngredient("nitrogen", 1000), p(33000, proton),
            List.of(po(proton), po(deuteron)), dustIngredient("graphite"), null, 46000, 0.125);
        targetChamberItems(isotopeIngredient(magnesium_24), p(38000, proton),
            List.of(po(2, proton), po(neutron)), isotopeIngredient(sodium_22), 56000, 0.32);
        targetChamberItems(dustIngredient("graphite"), p(40000, proton),
            List.of(po(2, proton)), isotopeIngredient(boron11), 50000, 0.064);
        targetChamberFluids(List.of(fluidIngredient("oxygen", 1000)), p(40000, proton),
            List.of(po(proton), po(deuteron)), List.of(fluidIngredient("nitrogen", 1000)), 65000, 0.05);
        targetChamberItems(ingotIngredient("aluminum"), p(40000, proton),
            List.of(po(alpha), po(neutron), po(proton)), isotopeIngredient(sodium_22), 50000, 0.08);
        targetChamberItems(ingotIngredient("copper"), p(45000, proton),
            List.of(po(alpha)), ingotIngredient("nickel"), 56000, 0.625);
        targetChamberMixed(ingotIngredient("calcium"), null, p(51000, proton),
            List.of(po(helion), po(electron_neutrino), po(proton)), null, fluidIngredient("chlorine", 1000), 67000, 0.16);
        targetChamberItems(dustIngredient("graphite"), p(60000, proton),
            List.of(po(proton), po(deuteron)), isotopeIngredient(boron10), 85000, 0.08);
        targetChamberItems(dustIngredient("radium"), p(60000, proton),
            List.of(po(neutron)), waste("heavy"), 600000, 0.2);
        targetChamberItems(ingotIngredient("thorium"), p(60000, proton),
            List.of(po(neutron)), waste("heavy"), 600000, 1.0);
        targetChamberItems(isotopeIngredient(uranium233), p(60000, proton),
            List.of(po(neutron)), waste("heavy"), 600000, 1.0);
        targetChamberItems(isotopeIngredient(uranium234), p(60000, proton),
            List.of(po(2, neutron)), waste("heavy"), 600000, 1.0);
        targetChamberItems(isotopeIngredient(uranium235), p(60000, proton),
            List.of(po(4, neutron)), waste("heavy"), 600000, 1.0);
        targetChamberItems(isotopeIngredient(uranium238), p(60000, proton),
            List.of(po(2, neutron)), waste("heavy"), 600000, 1.0);
        targetChamberItems(isotopeIngredient(neptunium237), p(60000, proton),
            List.of(po(2, neutron)), waste("heavy"), 600000, 1.0);
        targetChamberItems(isotopeIngredient(plutonium239), p(60000, proton),
            List.of(po(4, neutron)), waste("heavy"), 600000, 1.0);
        targetChamberItems(isotopeIngredient(plutonium241), p(60000, proton),
            List.of(po(4, neutron)), waste("heavy"), 600000, 1.0);
        targetChamberItems(isotopeIngredient(plutonium242), p(60000, proton),
            List.of(po(4, neutron)), waste("heavy"), 600000, 1.0);
        targetChamberItems(isotopeIngredient(americium241), p(60000, proton),
            List.of(po(4, neutron)), waste("heavy"), 600000, 1.0);
        targetChamberItems(isotopeIngredient(americium243), p(60000, proton),
            List.of(po(2, neutron)), waste("heavy"), 600000, 1.0);
        targetChamberItems(isotopeIngredient(copernicium291), p(60000, proton),
            List.of(po(8, neutron)), waste("heavy"), 600000, 1.0);
        targetChamberMixed(null, fluidIngredient("oxygen", 1000), p(65000, proton),
            List.of(po(alpha), po(helion)), isotopeIngredient(boron10), null, 150000, 0.02);
        targetChamberItems(ingotIngredient("gold"), p(100000, proton),
            List.of(po(alpha), po(neutron), po(proton)), isotopeIngredient(iridium_192), 200000, 0.02);
        targetChamberItems(dustIngredient("bismuth"), p(100000, proton),
            List.of(po(neutron)), waste("light"), 600000, 0.4);
        targetChamberItems(dustIngredient("graphite"), p(150000, proton),
            List.of(po(proton), po(triton)), ingotIngredient("beryllium"), 1000000, 0.02);
        targetChamberItems(ingotIngredient("aluminum"), p(155000, proton),
            List.of(po(3, proton), po(2, neutron)), ingotIngredient("sodium"), 170000, 0.02);
        targetChamberItems(ingotIngredient("platinum"), p(200000, proton),
            List.of(po(neutron)), waste("light"), 600000, 0.02);
        targetChamberItems(ingotIngredient("gold"), p(200000, proton),
            List.of(po(neutron)), waste("light"), 600000, 0.16);
        targetChamberMixed(null, fluidIngredient("mercury", 144), p(200000, proton),
            List.of(po(neutron)), waste("light"), null, 600000, 0.02);
        targetChamberItems(ingotIngredient("mercury"), p(200000, proton),
            List.of(po(neutron)), waste("light"), 600000, 0.02);
        targetChamberItems(ingotIngredient("lead"), p(200000, proton),
            List.of(po(neutron)), waste("light"), 600000, 0.25);
        targetChamberItems(ingotIngredient("tungsten"), p(400000, proton),
            List.of(po(neutron)), waste("light"), 600000, 0.08);

        // Pion production via proton spallation
        for (Map.Entry<NcIngredient, NcIngredient> entry : spallationMaterials.entrySet()) {
            targetChamberItems(entry.getKey(), p(600000, proton),
                List.of(po(pion_plus), po(pion_minus)),
                entry.getValue(), 5000000, 0.2);
        }
        targetChamberMixed(null, fluidIngredient("mercury", 144), p(600000, proton),
            List.of(po(pion_plus), po(pion_minus)), waste("mercury"), null, 5000000, 0.2);

        // Antiproton production via proton
        for (Map.Entry<NcIngredient, NcIngredient> entry : spallationMaterials.entrySet()) {
            targetChamberItems(entry.getKey(), p(5630000, proton),
                List.of(po(proton), po(antiproton)),
                entry.getValue(), 20000000, 0.2);
        }
        targetChamberMixed(null, fluidIngredient("mercury", 144), p(5630000, proton),
            List.of(po(proton), po(antiproton)), waste("mercury"), null, 20000000, 0.2);

        // ===== Neutron reactions =====
        targetChamberFluids(List.of(fluidIngredient("helium_3", 1000)), p(0, neutron),
            List.of(po(proton), po(triton)), List.of(), 18000, 0.1);
        targetChamberItems(isotopeIngredient(beryllium_7), p(0, neutron),
            List.of(po(proton)), isotopeIngredient(lithium7), 10000, 1.0);
        targetChamberItems(isotopeIngredient(boron10), p(0, neutron),
            List.of(po(photon)), isotopeIngredient(boron11), 1000, 0.02);
        targetChamberMixed(isotopeIngredient(sodium_22), null, p(0, neutron),
            List.of(po(proton)), null, fluidIngredient("neon", 1000), 11000, 0.25);
        targetChamberItems(ingotIngredient("cobalt"), p(0, neutron),
            List.of(po(photon)), isotopeIngredient(cobalt_60), 10000, 0.5);
        targetChamberItems(isotopeIngredient(iridium_192), p(0, neutron),
            List.of(po(photon)), ingotIngredient("iridium"), 5000, 1.0);
        targetChamberItems(isotopeIngredient(uranium233), p(0, neutron),
            List.of(po(photon)), isotopeIngredient(uranium234), 5000, 1.0);
        targetChamberItems(isotopeIngredient(uranium234), p(0, neutron),
            List.of(po(photon)), isotopeIngredient(uranium235), 5000, 1.0);
        targetChamberItems(isotopeIngredient(neptunium236), p(0, neutron),
            List.of(po(photon)), isotopeIngredient(neptunium237), 14000, 1.0);
        targetChamberItems(isotopeIngredient(plutonium238), p(0, neutron),
            List.of(po(photon)), isotopeIngredient(plutonium239), 5000, 1.0);
        targetChamberItems(isotopeIngredient(plutonium241), p(0, neutron),
            List.of(po(photon)), isotopeIngredient(plutonium242), 5000, 1.0);
        targetChamberItems(isotopeIngredient(americium241), p(0, neutron),
            List.of(po(photon)), isotopeIngredient(americium242), 5000, 1.0);
        targetChamberItems(isotopeIngredient(americium242), p(0, neutron),
            List.of(po(photon)), isotopeIngredient(americium243), 5000, 1.0);
        targetChamberItems(dustIngredient("sulfur"), p(2800, neutron),
            List.of(po(alpha)), ingotIngredient("silicon"), 4000, 0.2);
        targetChamberItems(isotopeIngredient(lithium6), p(3000, neutron),
            List.of(po(alpha), po(neutron), po(deuteron)), EMPTY, 21000, 0.2);
        targetChamberMixed(null, fluidIngredient("chlorine", 1000), p(3000, neutron),
            List.of(po(alpha), po(electron_antineutrino), po(electron)), dustIngredient("sulfur"), null, 13500, 0.2);
        targetChamberMixed(ingotIngredient("calcium"), null, p(3000, neutron),
            List.of(po(alpha), po(electron_neutrino)), null, fluidIngredient("chlorine", 1000), 15500, 0.1);
        targetChamberItems(ingotIngredient("beryllium"), p(4000, neutron),
            List.of(po(2, alpha), po(2, neutron)), EMPTY, 14000, 0.5);
        targetChamberItems(isotopeIngredient(lithium7), p(5000, neutron),
            List.of(po(alpha), po(neutron), po(triton)), EMPTY, 17000, 0.25);
        targetChamberItems(ingotIngredient("zinc"), p(5500, neutron),
            List.of(po(alpha)), ingotIngredient("nickel"), 13000, 0.25);
        targetChamberMixed(null, fluidIngredient("nitrogen", 1000), p(6000, neutron),
            List.of(po(triton)), dustIngredient("graphite"), null, 16000, 0.02);
        targetChamberItems(isotopeIngredient(plutonium242), p(8500, neutron),
            List.of(po(2, neutron)), isotopeIngredient(plutonium241), 13000, 1.0);
        targetChamberItems(ingotIngredient("iridium"), p(9000, neutron),
            List.of(po(2, neutron)), isotopeIngredient(iridium_192), 19500, 1.0);
        targetChamberItems(isotopeIngredient(plutonium239), p(9000, neutron),
            List.of(po(2, neutron)), isotopeIngredient(plutonium238), 14000, 0.32);
        targetChamberItems(isotopeIngredient(americium243), p(9000, neutron),
            List.of(po(2, neutron)), isotopeIngredient(americium242), 14500, 1.0);
        targetChamberFluids(List.of(fluidIngredient("tritium", 1000)), p(10000, neutron),
            List.of(po(deuteron), po(2, neutron)), List.of(), 18000, 0.04);
        targetChamberItems(isotopeIngredient(neptunium237), p(10000, neutron),
            List.of(po(2, neutron)), isotopeIngredient(neptunium236), 14000, 1.0);
        targetChamberFluids(List.of(fluidIngredient("deuterium", 1000)), p(11000, neutron),
            List.of(po(proton), po(2, neutron)), List.of(), 54000, 0.16);
        targetChamberItems(ingotIngredient("iron"), p(11000, neutron),
            List.of(po(alpha)), ingotIngredient("chromium"), 22000, 0.1);
        targetChamberItems(ingotIngredient("copper"), p(11000, neutron),
            List.of(po(proton), po(neutron)), ingotIngredient("nickel"), 15000, 1.0);
        targetChamberItems(ingotIngredient("chromium"), p(12000, neutron),
            List.of(po(alpha)), ingotIngredient("titanium"), 20000, 0.16);
        targetChamberItems(isotopeIngredient(boron11), p(12500, neutron),
            List.of(po(triton)), ingotIngredient("beryllium"), 20000, 0.04);
        targetChamberItems(ingotIngredient("aluminum"), p(13000, neutron),
            List.of(po(proton), po(neutron)), isotopeIngredient(magnesium_26), 30000, 0.8);
        targetChamberMixed(isotopeIngredient(magnesium_24), null, p(14000, neutron),
            List.of(po(alpha), po(neutron)), null, fluidIngredient("neon", 1000), 32000, 0.1);
        targetChamberItems(ingotIngredient("calcium"), p(14000, neutron),
            List.of(po(proton), po(neutron)), ingotIngredient("potassium"), 24000, 1.0);
        targetChamberItems(ingotIngredient("zinc"), p(14000, neutron),
            List.of(po(proton), po(neutron)), ingotIngredient("copper"), 24500, 1.0);
        targetChamberItems(ingotIngredient("beryllium"), p(15000, neutron),
            List.of(po(triton)), isotopeIngredient(lithium7), 26000, 0.064);
        targetChamberMixed(ingotIngredient("potassium"), null, p(15000, neutron),
            List.of(po(alpha), po(neutron)), null, fluidIngredient("chlorine", 1000), 32000, 0.1);
        targetChamberItems(ingotIngredient("zirconium"), p(15000, neutron),
            List.of(po(alpha)), ingotIngredient("strontium"), 20000, 0.04);
        targetChamberItems(isotopeIngredient(boron11), p(17000, neutron),
            List.of(po(2, neutron)), isotopeIngredient(boron10), 29000, 0.05);
        targetChamberItems(ingotIngredient("sodium"), p(17000, neutron),
            List.of(po(2, neutron)), isotopeIngredient(sodium_22), 28000, 0.25);
        targetChamberItems(isotopeIngredient(uranium235), p(17000, neutron),
            List.of(po(3, neutron)), isotopeIngredient(uranium233), 23500, 0.625);
        targetChamberItems(ingotIngredient("silicon"), p(18000, neutron),
            List.of(po(proton), po(neutron)), ingotIngredient("aluminum"), 27000, 0.8);
        targetChamberItems(dustIngredient("barium"), p(18200, neutron),
            List.of(po(proton)), dustIngredient(caesium_137), 34000, 0.016);
        targetChamberItems(ingotIngredient("nickel"), p(19000, neutron),
            List.of(po(alpha), po(neutron)), ingotIngredient("iron"), 30000, 0.8);
        targetChamberItems(ingotIngredient("platinum"), p(19000, neutron),
            List.of(po(alpha)), ingotIngredient("osmium"), 27000, 0.025);
        targetChamberItems(dustIngredient("terbium"), p(20800, neutron),
            List.of(po(alpha), po(neutron)), dustIngredient(europium_155), 34000, 0.5);
        targetChamberItems(ingotIngredient("zirconium"), p(21000, neutron),
            List.of(po(deuteron)), ingotIngredient("yttrium"), 36000, 0.625);
        targetChamberMixed(null, fluidIngredient("oxygen", 1000), p(22000, neutron),
            List.of(po(2, alpha)), ingotIngredient("beryllium"), null, 35000, 0.02);
        targetChamberItems(ingotIngredient("niobium"), p(22000, neutron),
            List.of(po(alpha), po(neutron)), ingotIngredient("yttrium"), 34000, 0.125);
        targetChamberItems(isotopeIngredient(uranium238), p(26000, neutron),
            List.of(po(4, neutron)), isotopeIngredient(uranium235), 34000, 0.55);
        targetChamberItems(ingotIngredient("manganese"), p(29000, neutron),
            List.of(po(triton)), ingotIngredient("chromium"), 46000, 0.625);
        targetChamberItems(dustIngredient("graphite"), p(29500, neutron),
            List.of(po(deuteron)), isotopeIngredient(boron11), 60000, 0.2);
        targetChamberItems(ingotIngredient("cobalt"), p(30000, neutron),
            List.of(po(triton)), ingotIngredient("iron"), 50000, 0.4);
        targetChamberItems(ingotIngredient("yttrium"), p(30000, neutron),
            List.of(po(deuteron)), ingotIngredient("strontium"), 49000, 0.32);
        targetChamberMixed(ingotIngredient("sodium"), null, p(30000, neutron),
            List.of(po(triton)), null, fluidIngredient("neon", 1000), 60000, 0.1);
        targetChamberItems(ingotIngredient("gold"), p(30000, neutron),
            List.of(po(alpha), po(neutron)), ingotIngredient("iridium"), 45000, 0.05);
        targetChamberItems(isotopeIngredient(magnesium_24), p(35000, neutron),
            List.of(po(deuteron), po(neutron)), isotopeIngredient(sodium_22), 60000, 0.25);
        targetChamberFluids(List.of(fluidIngredient("helium", 1000)), p(40000, neutron),
            List.of(po(helion), po(2, neutron)), List.of(), 60000, 0.02);
        targetChamberItems(ingotIngredient("aluminum"), p(44000, neutron),
            List.of(po(helion), po(3, neutron)), isotopeIngredient(sodium_22), 80000, 0.05);
        targetChamberItems(isotopeIngredient(copernicium291), p(60000, neutron),
            List.of(po(8, neutron)), waste("heavy"), 1000000, 1.0);
        targetChamberItems(dustIngredient("bismuth"), p(70000, neutron),
            List.of(po(triton)), ingotIngredient("lead"), 150000, 0.2);
        targetChamberItems(dustIngredient("graphite"), p(105000, neutron),
            List.of(po(helion), po(3, neutron)), isotopeIngredient(beryllium_7), 150000, 0.032);

        // ===== Photon reactions =====
        targetChamberFluids(List.of(fluidIngredient("deuterium", 1000)), p(2500, photon),
            List.of(po(proton), po(neutron)), List.of(), 14000, 0.02);
        targetChamberItems(isotopeIngredient(lithium6), p(5500, photon),
            List.of(po(alpha), po(neutron), po(proton)), EMPTY, 17000, 0.02);
        targetChamberFluids(List.of(fluidIngredient("helium_3", 1000)), p(9000, photon),
            List.of(po(2, proton), po(neutron)), List.of(), 36000, 0.02);
        targetChamberItems(isotopeIngredient(neptunium237), p(9750, photon),
            List.of(po(neutron)), isotopeIngredient(neptunium236), 14500, 1.0);
        targetChamberItems(isotopeIngredient(plutonium239), p(10000, photon),
            List.of(po(neutron)), isotopeIngredient(plutonium238), 13500, 0.4);
        targetChamberItems(isotopeIngredient(americium243), p(10000, photon),
            List.of(po(neutron)), isotopeIngredient(americium242), 12500, 1.0);
        targetChamberItems(ingotIngredient("tungsten"), p(11000, photon),
            List.of(po(alpha)), ingotIngredient("hafnium"), 16500, 0.25);
        targetChamberItems(ingotIngredient("zirconium"), p(11500, photon),
            List.of(po(proton)), ingotIngredient("yttrium"), 19000, 0.04);
        targetChamberItems(ingotIngredient("iridium"), p(12000, photon),
            List.of(po(neutron)), isotopeIngredient(iridium_192), 16000, 0.5);
        targetChamberItems(dustIngredient("bismuth"), p(12000, photon),
            List.of(po(proton), po(neutron)), ingotIngredient("lead"), 15000, 0.5);
        targetChamberItems(ingotIngredient("niobium"), p(13000, photon),
            List.of(po(alpha)), ingotIngredient("yttrium"), 21000, 0.02);
        targetChamberItems(isotopeIngredient(uranium235), p(13500, photon),
            List.of(po(2, neutron)), isotopeIngredient(uranium233), 18000, 0.32);
        targetChamberItems(ingotIngredient("iron"), p(14500, photon),
            List.of(po(proton)), ingotIngredient("manganese"), 21000, 0.125);
        targetChamberItems(ingotIngredient("yttrium"), p(16000, photon),
            List.of(po(proton)), ingotIngredient("strontium"), 23000, 0.016);
        targetChamberItems(ingotIngredient("aluminum"), p(17500, photon),
            List.of(po(proton)), isotopeIngredient(magnesium_26), 23000, 0.032);
        targetChamberItems(ingotIngredient("silicon"), p(18000, photon),
            List.of(po(proton)), ingotIngredient("aluminum"), 23000, 0.08);
        targetChamberItems(ingotIngredient("calcium"), p(18000, photon),
            List.of(po(proton)), ingotIngredient("potassium"), 22000, 0.05);
        targetChamberItems(isotopeIngredient(lithium6), p(18500, photon),
            List.of(po(helion), po(triton)), EMPTY, 25500, 0.02);
        targetChamberItems(isotopeIngredient(lithium7), p(18500, photon),
            List.of(po(neutron)), isotopeIngredient(lithium6), 24000, 0.02);
        targetChamberItems(ingotIngredient("copper"), p(19500, photon),
            List.of(po(proton), po(neutron)), ingotIngredient("nickel"), 27000, 0.05);
        targetChamberMixed(null, fluidIngredient("nitrogen", 1000), p(20000, photon),
            List.of(po(proton), po(neutron)), dustIngredient("graphite"), null, 27000, 0.02);
        targetChamberItems(isotopeIngredient(magnesium_26), p(21000, photon),
            List.of(po(2, neutron)), isotopeIngredient(magnesium_24), 28000, 0.04);
        targetChamberItems(isotopeIngredient(boron11), p(24500, photon),
            List.of(po(2, alpha), po(neutron), po(deuteron)), EMPTY, 30500, 0.02);
        targetChamberItems(ingotIngredient("beryllium"), p(26000, photon),
            List.of(po(2, alpha), po(neutron)), EMPTY, 46000, 0.02);
        targetChamberFluids(List.of(fluidIngredient("oxygen", 1000)), p(29000, photon),
            List.of(po(proton), po(neutron)), List.of(fluidIngredient("nitrogen", 1000)), 41000, 0.02);
        targetChamberItems(dustIngredient("graphite"), p(31500, photon),
            List.of(po(alpha), po(neutron)), isotopeIngredient(beryllium_7), 42500, 0.02);
        targetChamberItems(dustIngredient("graphite"), p(42500, photon),
            List.of(po(alpha), po(neutron), po(proton)), isotopeIngredient(lithium6), 55000, 0.02);

        // ===== Electron reactions =====
        targetChamberItems(ingotIngredient("iron"), p(50000, electron),
            List.of(po(alpha), po(electron)), ingotIngredient("chromium"), 100000, 0.01);
        targetChamberItems(ingotIngredient("cobalt"), p(50000, electron),
            List.of(po(alpha), po(electron)), ingotIngredient("manganese"), 100000, 0.01);
        targetChamberItems(ingotIngredient("zinc"), p(50000, electron),
            List.of(po(alpha), po(electron)), ingotIngredient("nickel"), 100000, 0.01);
        targetChamberItems(ingotIngredient("zirconium"), p(60000, electron),
            List.of(po(proton), po(electron)), ingotIngredient("yttrium"), 130000, 0.01);
        targetChamberItems(isotopeIngredient(uranium233), p(170000, electron),
            List.of(po(neutron), po(electron)), waste("heavy"), 300000, 0.01);
        targetChamberItems(isotopeIngredient(uranium235), p(170000, electron),
            List.of(po(neutron), po(electron)), waste("heavy"), 300000, 0.01);
        targetChamberItems(isotopeIngredient(plutonium239), p(170000, electron),
            List.of(po(neutron), po(electron)), waste("heavy"), 300000, 0.01);
        targetChamberItems(isotopeIngredient(neptunium237), p(180000, electron),
            List.of(po(neutron), po(electron)), waste("heavy"), 300000, 0.01);
        targetChamberItems(isotopeIngredient(uranium238), p(200000, electron),
            List.of(po(neutron), po(electron)), waste("heavy"), 300000, 0.01);
        targetChamberItems(isotopeIngredient(americium242), p(200000, electron),
            List.of(po(neutron), po(electron)), waste("heavy"), 300000, 0.01);
        targetChamberItems(ingotIngredient("thorium"), p(220000, electron),
            List.of(po(neutron), po(electron)), waste("heavy"), 300000, 0.01);

        // ===== Deuteron reactions =====
        targetChamberFluids(List.of(fluidIngredient("tritium", 1000)), p(50, deuteron),
            List.of(po(alpha), po(neutron)), List.of(), 600, 0.5);
        targetChamberFluids(List.of(fluidIngredient("deuterium", 1000)), p(500, deuteron),
            List.of(po(triton), po(neutron)), List.of(), 3000, 0.16);
        targetChamberItems(isotopeIngredient(lithium6), p(500, deuteron),
            List.of(po(neutron)), isotopeIngredient(beryllium_7), 3000, 0.16);
        targetChamberItems(ingotIngredient("beryllium"), p(1500, deuteron),
            List.of(po(alpha)), isotopeIngredient(lithium7), 5000, 0.625);
        targetChamberItems(isotopeIngredient(boron11), p(1500, deuteron),
            List.of(po(neutron)), dustIngredient("graphite"), 3000, 0.5);
        targetChamberItems(isotopeIngredient(lithium7), p(3500, deuteron),
            List.of(po(2, alpha), po(neutron)), EMPTY, 9000, 1.0);
        targetChamberFluids(List.of(fluidIngredient("oxygen", 1000)), p(3500, deuteron),
            List.of(po(alpha)), List.of(fluidIngredient("nitrogen", 1000)), 10000, 0.25);
        targetChamberItems(dustIngredient("graphite"), p(5000, deuteron),
            List.of(po(alpha)), isotopeIngredient(boron10), 13000, 0.625);
        targetChamberItems(ingotIngredient("cobalt"), p(5000, deuteron),
            List.of(po(proton)), isotopeIngredient(cobalt_60), 10000, 0.4);
        targetChamberItems(isotopeIngredient(magnesium_24), p(6000, deuteron),
            List.of(po(alpha)), isotopeIngredient(sodium_22), 11000, 0.4);
        targetChamberItems(dustIngredient("bismuth"), p(10000, deuteron),
            List.of(po(neutron)), dustIngredient("polonium"), 15000, 0.08);
        targetChamberItems(isotopeIngredient(uranium233), p(10000, deuteron),
            List.of(po(proton)), isotopeIngredient(uranium234), 16000, 0.08);
        targetChamberItems(isotopeIngredient(plutonium241), p(10500, deuteron),
            List.of(po(2, neutron)), isotopeIngredient(americium241), 19500, 0.5);
        targetChamberItems(isotopeIngredient(uranium234), p(11000, deuteron),
            List.of(po(proton)), isotopeIngredient(uranium235), 17000, 0.32);
        targetChamberItems(isotopeIngredient(plutonium238), p(11000, deuteron),
            List.of(po(proton)), isotopeIngredient(plutonium239), 17000, 0.2);
        targetChamberItems(isotopeIngredient(uranium235), p(11500, deuteron),
            List.of(po(neutron)), isotopeIngredient(neptunium236), 19500, 0.032);
        targetChamberItems(ingotIngredient("osmium"), p(12000, deuteron),
            List.of(po(2, neutron)), isotopeIngredient(iridium_192), 14500, 1.0);
        targetChamberItems(isotopeIngredient(plutonium242), p(12000, deuteron),
            List.of(po(2, neutron)), isotopeIngredient(americium242), 16000, 0.5);
        targetChamberItems(isotopeIngredient(americium243), p(12000, deuteron),
            List.of(po(2, neutron)), isotopeIngredient(curium243), 15000, 0.2);
        targetChamberMixed(ingotIngredient("gold"), null, p(18000, deuteron),
            List.of(po(3, neutron)), null, fluidIngredient("mercury", 144), 24000, 0.5);
        targetChamberItems(isotopeIngredient(uranium238), p(24000, deuteron),
            List.of(po(4, neutron)), isotopeIngredient(neptunium236), 30000, 1.0);
        targetChamberItems(ingotIngredient("sodium"), p(30000, deuteron),
            List.of(po(deuteron), po(neutron)), isotopeIngredient(sodium_22), 55000, 0.4);
        targetChamberItems(ingotIngredient("beryllium"), p(34000, deuteron),
            List.of(po(deuteron), po(2, neutron)), isotopeIngredient(beryllium_7), 53000, 0.064);
        targetChamberItems(ingotIngredient("beryllium"), p(55000, deuteron),
            List.of(po(2, alpha), po(neutron), po(deuteron)), EMPTY, 150000, 1.0);
        targetChamberItems(ingotIngredient("aluminum"), p(56000, deuteron),
            List.of(po(alpha), po(triton)), isotopeIngredient(sodium_22), 200000, 0.08);
        targetChamberItems(ingotIngredient("yttrium"), p(60000, deuteron),
            List.of(po(alpha)), ingotIngredient("strontium"), 200000, 0.064);

        // Antideuteron production via deuteron
        for (Map.Entry<NcIngredient, NcIngredient> entry : spallationMaterials.entrySet()) {
            targetChamberItems(entry.getKey(), p(11300000, deuteron),
                List.of(po(deuteron), po(antideuteron)),
                entry.getValue(), 20000000, 0.1);
        }
        targetChamberMixed(null, fluidIngredient("mercury", 144), p(11300000, deuteron),
            List.of(po(deuteron), po(antideuteron)), waste("mercury"), null, 20000000, 0.1);

        // ===== Triton reactions =====
        targetChamberItems(ingotIngredient("beryllium"), p(1000, triton),
            List.of(po(neutron)), isotopeIngredient(boron11), 7000, 1.0);
        targetChamberFluids(List.of(fluidIngredient("tritium", 1000)), p(1200, triton),
            List.of(po(alpha), po(2, neutron)), List.of(), 3000, 0.1);
        targetChamberItems(dustIngredient("graphite"), p(2500, triton),
            List.of(po(alpha)), isotopeIngredient(boron11), 7500, 0.625);
        targetChamberItems(isotopeIngredient(lithium6), p(8000, triton),
            List.of(po(2, neutron)), isotopeIngredient(beryllium_7), 12000, 0.064);

        // ===== Helion reactions =====
        targetChamberItems(isotopeIngredient(lithium6), p(11000, helion),
            List.of(po(deuteron)), isotopeIngredient(beryllium_7), 30000, 0.5);
        targetChamberItems(ingotIngredient("cobalt"), p(14000, helion),
            List.of(po(2, proton)), isotopeIngredient(cobalt_60), 24000, 0.1);
        targetChamberItems(dustIngredient("graphite"), p(22000, helion),
            List.of(po(2, alpha)), isotopeIngredient(beryllium_7), 31000, 0.125);
        targetChamberItems(ingotIngredient("beryllium"), p(23000, helion),
            List.of(po(alpha), po(neutron)), isotopeIngredient(beryllium_7), 28000, 0.16);
        targetChamberItems(ingotIngredient("lead"), p(23000, helion),
            List.of(po(neutron)), dustIngredient("polonium"), 30000, 0.02);
        targetChamberItems(isotopeIngredient(neptunium237), p(23500, helion),
            List.of(po(proton), po(neutron)), isotopeIngredient(plutonium238), 30000, 0.02);
        targetChamberItems(isotopeIngredient(lithium6), p(30000, helion),
            List.of(po(2, alpha), po(proton)), EMPTY, 150000, 0.625);
        targetChamberItems(isotopeIngredient(boron10), p(30000, helion),
            List.of(po(alpha), po(neutron), po(proton)), isotopeIngredient(beryllium_7), 47000, 0.2);
        targetChamberItems(dustIngredient("bismuth"), p(30000, helion),
            List.of(po(proton), po(neutron)), dustIngredient("polonium"), 43000, 0.2);
        targetChamberItems(ingotIngredient("aluminum"), p(90000, helion),
            List.of(po(2, alpha)), isotopeIngredient(sodium_22), 165000, 0.16);

        // ===== Alpha reactions =====
        targetChamberItems(ingotIngredient("beryllium"), p(4000, alpha),
            List.of(po(neutron)), dustIngredient("graphite"), 6500, 1.0);
        targetChamberItems(isotopeIngredient(magnesium_26), p(4000, alpha),
            List.of(po(neutron)), ingotIngredient("silicon"), 6000, 0.32);
        targetChamberItems(ingotIngredient("sodium"), p(4500, alpha),
            List.of(po(proton)), isotopeIngredient(magnesium_26), 6000, 0.16);
        targetChamberMixed(null, fluidIngredient("fluorine", 1000), p(6000, alpha),
            List.of(po(neutron)), isotopeIngredient(sodium_22), null, 11000, 0.25);
        targetChamberFluids(List.of(fluidIngredient("fluorine", 1000)), p(11000, alpha),
            List.of(po(proton)), List.of(fluidIngredient("neon", 1000)), 17500, 0.16);
        targetChamberItems(isotopeIngredient(lithium7), p(6500, alpha),
            List.of(po(neutron)), isotopeIngredient(boron10), 7600, 0.5);
        targetChamberItems(ingotIngredient("aluminum"), p(12000, alpha),
            List.of(po(positron), po(neutron), po(electron_neutrino)), ingotIngredient("silicon"), 17500, 0.4);
        targetChamberFluids(List.of(fluidIngredient("nitrogen", 1000)), p(14000, alpha),
            List.of(po(proton), po(neutron)), List.of(fluidIngredient("oxygen", 1000)), 26000, 0.1);
        targetChamberItems(ingotIngredient("copper"), p(16000, alpha),
            List.of(po(proton)), ingotIngredient("zinc"), 20000, 0.16);
        targetChamberFluids(List.of(fluidIngredient("oxygen", 1000)), p(18000, alpha),
            List.of(po(positron), po(neutron), po(electron_neutrino)), List.of(fluidIngredient("fluorine", 1000)), 25000, 0.032);
        targetChamberItems(ingotIngredient("osmium"), p(19000, alpha),
            List.of(po(neutron)), ingotIngredient("platinum"), 27500, 0.025);
        targetChamberItems(isotopeIngredient(uranium235), p(21000, alpha),
            List.of(po(neutron)), isotopeIngredient(plutonium238), 32000, 0.02);
        targetChamberItems(dustIngredient("ytterbium"), p(21200, alpha),
            List.of(po(2, neutron)), ingotIngredient("hafnium"), 26600, 1.0);
        targetChamberItems(ingotIngredient("lead"), p(26000, alpha),
            List.of(po(2, neutron)), dustIngredient("polonium"), 32000, 1.0);
        targetChamberItems(isotopeIngredient(lithium6), p(27000, alpha),
            List.of(po(photon)), isotopeIngredient(boron10), 45000, 0.02);
        targetChamberItems(isotopeIngredient(uranium233), p(27000, alpha),
            List.of(po(proton)), isotopeIngredient(neptunium236), 30000, 0.02);
        targetChamberItems(isotopeIngredient(uranium238), p(27000, alpha),
            List.of(po(3, neutron)), isotopeIngredient(plutonium239), 30000, 0.625);
        targetChamberItems(isotopeIngredient(plutonium239), p(28000, alpha),
            List.of(po(proton)), isotopeIngredient(americium242), 30000, 0.02);
        targetChamberMixed(null, fluidIngredient("nitrogen", 1000), p(30000, alpha),
            List.of(po(alpha), po(neutron), po(proton)), dustIngredient("graphite"), null, 56000, 0.25);
        targetChamberItems(isotopeIngredient(uranium235), p(30000, alpha),
            List.of(po(triton)), isotopeIngredient(neptunium236), 35000, 0.016);
        targetChamberItems(isotopeIngredient(uranium238), p(34500, alpha),
            List.of(po(4, neutron)), isotopeIngredient(plutonium238), 42000, 0.128);
        targetChamberItems(dustIngredient("graphite"), p(38000, alpha),
            List.of(po(alpha), po(proton)), isotopeIngredient(boron11), 50000, 0.25);
        targetChamberItems(ingotIngredient("cobalt"), p(38000, alpha),
            List.of(po(2, proton), po(neutron)), isotopeIngredient(cobalt_60), 54000, 0.16);
        targetChamberItems(dustIngredient("graphite"), p(53000, alpha),
            List.of(po(alpha), po(neutron), po(proton)), isotopeIngredient(boron10), 69000, 0.16);
        targetChamberItems(ingotIngredient("beryllium"), p(100000, alpha),
            List.of(po(alpha), po(2, neutron)), isotopeIngredient(beryllium_7), 145000, 0.064);

        // ===== Boron Ion reactions =====
        targetChamberItems(ingredient(NC_PARTS.get("silicon_wafer").get()), particles(600, 2.0, 1, boron_ion),
            List.of(), ingredient(NC_PARTS.get("silicon_p_doped").get()), 1000, 1.0);
        targetChamberItems(ingredient(FISSION_BLOCKS.get("fission_reactor_irradiation_chamber").get()), particles(1000, 3.0, 1, boron_ion),
            List.of(po(proton), po(positron), po(electron)),
            ingredient(FISSION_BLOCKS.get("fission_reactor_pile-driver_irradiation_chamber").get()), 2000, 0.25);
        targetChamberItems(isotopeIngredient(lithium7), particles(6000, 1.0, 1, boron_ion),
            List.of(po(alpha), po(2, neutron)), dustIngredient("graphite"), 12000, 0.2);

        // ===== Ca-48 reactions =====
        targetChamberItems(isotopeIngredient(berkelium248), particles(40000, 2.0, 1, calcium_48_ion),
            List.of(po(alpha), po(neutron), po(3, electron_neutrino)),
            isotopeIngredient(copernicium291), 50000, 0.02);

        // ===== Electron antineutrino reactions =====
        targetChamberItems(isotopeIngredient(iridium_192), p(0, electron_antineutrino),
            List.of(po(positron)), ingotIngredient("osmium"), 30000, 0.01);
        targetChamberItems(ingotIngredient("nickel"), p(200, electron_antineutrino),
            List.of(po(2, positron), po(electron_neutrino)), ingotIngredient("iron"), 10900, 0.01);
        targetChamberItems(isotopeIngredient(americium242), p(300, electron_antineutrino),
            List.of(po(positron)), isotopeIngredient(plutonium242), 30000, 0.01);
        targetChamberItems(isotopeIngredient(curium243), p(1100, electron_antineutrino),
            List.of(po(positron)), isotopeIngredient(americium243), 30000, 0.01);
        targetChamberItems(isotopeIngredient(berkelium247), p(1100, electron_antineutrino),
            List.of(po(positron)), isotopeIngredient(curium247), 30000, 0.01);

        // ===== Electron neutrino reactions =====
        targetChamberItems(isotopeIngredient(curium247), p(0, electron_neutrino),
            List.of(po(electron)), isotopeIngredient(berkelium247), 30000, 0.01);
        targetChamberItems(isotopeIngredient(americium243), p(100, electron_neutrino),
            List.of(po(electron)), isotopeIngredient(curium243), 30000, 0.01);
        targetChamberItems(isotopeIngredient(plutonium242), p(800, electron_neutrino),
            List.of(po(electron)), isotopeIngredient(americium242), 30000, 0.01);
        targetChamberItems(ingotIngredient("osmium"), p(1100, electron_neutrino),
            List.of(po(electron)), isotopeIngredient(iridium_192), 30000, 0.01);

        // ===== Pion reactions =====
        targetChamberItems(ingotIngredient("aluminum"), p(150000, pion_minus),
            List.of(po(proton), po(neutron)), isotopeIngredient(sodium_22), 250000, 0.025);
        targetChamberFluids(List.of(fluidIngredient("argon", 1000)), p(70000, pion_plus),
            List.of(po(2, proton), po(neutron)), List.of(fluidIngredient("chlorine", 1000)), 320000, 0.04);

        // ===== Antiproton annihilation =====
        for (Map.Entry<NcIngredient, NcIngredient> entry : spallationMaterials.entrySet()) {
            targetChamberItems(entry.getKey(), p(1, antiproton),
                List.of(po(pion_plus), po(pion_naught), po(pion_minus)),
                entry.getValue(), 10000000, 1.0);
        }
        targetChamberMixed(null, fluidIngredient("mercury", 144), p(1, antiproton),
            List.of(po(pion_plus), po(pion_naught), po(pion_minus)),
            waste("mercury"), null, 10000000, 1.0);

        // ===== Antideuteron annihilation =====
        for (Map.Entry<NcIngredient, NcIngredient> entry : spallationMaterials.entrySet()) {
            targetChamberItems(entry.getKey(), p(1, antideuteron),
                List.of(po(4, pion_plus), po(4, pion_naught), po(4, pion_minus)),
                entry.getValue(), 10000000, 1.0);
        }
        targetChamberMixed(null, fluidIngredient("mercury", 144), p(1, antideuteron),
            List.of(po(4, pion_plus), po(4, pion_naught), po(4, pion_minus)),
            waste("mercury"), null, 10000000, 1.0);
    }

    private static NcIngredient waste(String name) {
        return NcIngredient.of(NC_WASTE.get(name).get());
    }

    private static ParticleStack particles(int energy, double focus, int amount, Particle particle) {
        return new ParticleStack(particle, amount, energy, focus);
    }

    // Input particle helper: focus 1.0, amount 1.
    private static ParticleStack p(int energy, Particle particle) {
        return new ParticleStack(particle, 1, energy, 1.0);
    }

    // Output particle helper: amount 1.
    private static ParticleStack po(Particle particle) {
        return new ParticleStack(particle, 1, 1, 1.0);
    }

    // Output particle helper: explicit amount.
    private static ParticleStack po(int amount, Particle particle) {
        return new ParticleStack(particle, amount, 1, 1.0);
    }

    private static void targetChamberItems(NcIngredient inputItems, ParticleStack inputParticle, List<ParticleStack> outputParticles, NcIngredient outputItems, long maxEnergy, double crossSection) {
        int recpieAmount = (int) (moleAmount / crossSection);
        inputParticle.setAmount(inputParticle.getAmount() * recpieAmount);
        targetChamber(List.of(), List.of(inputItems), List.of(inputParticle), outputParticles, List.of(), List.of(outputItems), maxEnergy, crossSection);
    }

    private static void targetChamberFluids(List<FluidStackIngredient> inputFluids, ParticleStack inputParticle, List<ParticleStack> outputParticles, List<FluidStackIngredient> outputFluids, long maxEnergy, double crossSection) {
        int recpieAmount = (int) (moleAmount / crossSection);
        inputParticle.setAmount(inputParticle.getAmount() * recpieAmount);
        targetChamber(inputFluids, List.of(), List.of(inputParticle), outputParticles, outputFluids, List.of(), maxEnergy, crossSection);
    }

    private static void targetChamberMixed(NcIngredient inItem, FluidStackIngredient inFluid, ParticleStack inputParticle, List<ParticleStack> outputParticles, NcIngredient outItem, FluidStackIngredient outFluid, long maxEnergy, double crossSection) {
        int recpieAmount = (int) (moleAmount / crossSection);
        inputParticle.setAmount(inputParticle.getAmount() * recpieAmount);
        targetChamber(
            inFluid == null ? List.of() : List.of(inFluid),
            inItem == null ? List.of() : List.of(inItem),
            List.of(inputParticle),
            outputParticles,
            outFluid == null ? List.of() : List.of(outFluid),
            outItem == null ? List.of() : List.of(outItem),
            maxEnergy, crossSection);
    }
}
