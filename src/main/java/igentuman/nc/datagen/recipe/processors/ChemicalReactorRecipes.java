package igentuman.nc.datagen.recipe.processors;

import igentuman.nc.content.fuel.FuelDef;
import igentuman.nc.registration.FissionFuelEntry;
import igentuman.nc.setup.ModEntries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import static igentuman.nc.datagen.recipe.ModRecipeProvider.*;

/** Generates chemical reactor recipes for gases, acids, molten alloys, and fissile fuel mixing. */
public class ChemicalReactorRecipes {

    public static void chemicalReactor(RecipeOutput out) {
        gasAndAcid(out);
        moltenAlloys(out);
        fuelMixing(out);
    }

    private static void fuelMixing(RecipeOutput out) {
        for (FissionFuelEntry fe : ModEntries.FISSION_FUEL.values()) {
            if (fe.group.equals("mixed") || fe.base().isSpecial()) continue;
            int[] iso = fe.base().isotopes;
            boolean he = fe.name.startsWith("he");
            int a1 = he ? MOLTEN_INGOT : MOLTEN_INGOT / 3;
            int a2 = he ? MOLTEN_INGOT * 2 : MOLTEN_INGOT * 3;
            for (String v : FuelDef.FLUID_VARIANTS) {
                Fluid f1 = isotopeFluid(fe.group + "/" + iso[0], v);
                Fluid f2 = isotopeFluid(fe.group + "/" + iso[1], v);
                chem(out, fe.idStem + v, f1, a1, f2, a2, new F[]{f(fe.fuelFluid(v), MOLTEN_INGOT)});
            }
        }
    }

    private static void gasAndAcid(RecipeOutput out) {
        chem(out, "boron_arsenide",        fluidOf("arsenic"), 333, fluidOf("boron"), MOLTEN_INGOT / 2, new F[]{fluid("boron_arsenide", MOLTEN_INGOT * 2)}, 100, 60);
        chem(out, "ammonia_oxidation",     fluidOf("ammonia"), 350, fluidOf("oxygen"), 650, new F[]{fluid("nitric_oxide", 750), fluid("water", 250)}, 100, 30);
        chem(out, "no_oxidation",          fluidOf("nitric_oxide"), 500, fluidOf("oxygen"), 250, new F[]{fluid("nitrogen_dioxide", 750)}, 100, 30);
        chem(out, "nitric_acid",           fluidOf("nitrogen_dioxide"), 750, Fluids.WATER, 250, new F[]{fluid("nitric_acid", 1000)}, 300, 20);
        chem(out, "aqua_regia",            fluidOf("nitric_acid"), 250, fluidOf("hydrochloric_acid"), 750, new F[]{fluid("aqua_regia_acid", 1000)}, 300);
        chem(out, "hydrochloric_acid",     fluidOf("hydrogen_chloride"), 250, Fluids.WATER, 250, new F[]{fluid("hydrochloric_acid", 500)});
        chem(out, "diborane",              fluidOf("boron"), MOLTEN_INGOT, fluidOf("hydrogen"), 666, new F[]{fluid("diborane", 500)}, 100);
        chem(out, "boric_acid",            fluidOf("diborane"), 250, Fluids.WATER, 750, new F[]{fluid("boric_acid", 500), fluid("hydrogen", 500)}, 100);
        chem(out, "boron_nitride_solution", fluidOf("boric_acid"), 500, fluidOf("ammonia"), 500, new F[]{fluid("boron_nitride_solution", MOLTEN_INGOT / 2), fluid("water", 1000)}, 100, 25);
        chem(out, "ammonia_synthesis",     fluidOf("nitrogen"), 250, fluidOf("hydrogen"), 750, new F[]{fluid("ammonia", 750)}, 100, 25);
        chem(out, "water_synthesis",       fluidOf("hydrogen"), 500, fluidOf("liquid_oxygen"), 250, new F[]{fluid("water", 500)}, 100, 25);
        chem(out, "heavy_water",           fluidOf("deuterium"), 500, fluidOf("liquid_oxygen"), 250, new F[]{fluid("heavy_water", 500)}, 100, 25);
        chem(out, "lithium_fluoride",      fluidOf("lithium"), MOLTEN_INGOT * 2, fluidOf("fluorite_water"), 250, new F[]{fluid("lithium_fluoride", 360)}, 100, 25);
        chem(out, "beryllium_fluoride",    fluidOf("beryllium"), MOLTEN_INGOT * 2, fluidOf("fluorite_water"), 250, new F[]{fluid("beryllium_fluoride", 360)}, 100, 25);
        chem(out, "flibe_molten_salt",     fluidOf("lithium_fluoride"), MOLTEN_INGOT * 4, fluidOf("beryllium_fluoride"), MOLTEN_INGOT * 2, new F[]{fluid("flibe_molten_salt", MOLTEN_INGOT * 6)}, 100, 60);
        chem(out, "sulfur_dioxide",        fluidOf("sulfur"), MOLTEN_INGOT / 2, fluidOf("oxygen"), 500, new F[]{fluid("sulfur_dioxide", 500)}, 100, 25);
        chem(out, "sulfur_trioxide",       fluidOf("sulfur_dioxide"), 500, fluidOf("oxygen"), 250, new F[]{fluid("sulfur_trioxide", 500)}, 100, 25);
        chem(out, "sulfuric_acid",         fluidOf("sulfur_trioxide"), 250, Fluids.WATER, 250, new F[]{fluid("sulfuric_acid", 250)}, 100, 25);
        chem(out, "hydrofluoric_acid",     fluidOf("fluorite_water"), 500, fluidOf("sulfuric_acid"), 500, new F[]{fluid("hydrofluoric_acid", 1000), fluid("calcium_sulfate_solution", 50)}, 200, 25);
        chem(out, "sodium_hydroxide_solution", fluidOf("sodium_fluoride_solution"), MOLTEN_INGOT / 2, Fluids.WATER, 500, new F[]{fluid("sodium_hydroxide_solution", MOLTEN_INGOT / 2), fluid("hydrofluoric_acid", 500)}, 200, 25);
        chem(out, "potassium_hydroxide_solution", fluidOf("potassium_fluoride_solution"), MOLTEN_INGOT / 2, Fluids.WATER, 500, new F[]{fluid("potassium_hydroxide_solution", MOLTEN_INGOT / 2), fluid("hydrofluoric_acid", 500)}, 200, 25);
        chem(out, "borax_solution",        fluidOf("sodium_fluoride_solution"), MOLTEN_INGOT, fluidOf("boric_acid"), 2000, new F[]{fluid("borax_solution", MOLTEN_INGOT / 2), fluid("hydrofluoric_acid", 1500)}, 200, 25);
        chem(out, "oxygen_difluoride_hydrolysis", fluidOf("oxygen_difluoride"), 250, Fluids.WATER, 250, new F[]{fluid("liquid_oxygen", 250), fluid("hydrofluoric_acid", 250)}, 200, 25);
        chem(out, "oxygen_difluoride_sulfur", fluidOf("oxygen_difluoride"), 250, fluidOf("sulfur_dioxide"), 250, new F[]{fluid("sulfur_trioxide", 250), fluid("fluorite_water", 250)}, 200, 25);
        chem(out, "oxygen_difluoride",     fluidOf("oxygen"), 250, fluidOf("fluorite_water"), 500, new F[]{fluid("oxygen_difluoride", 250)}, 100, 25);
        chem(out, "manganese_reduction",   fluidOf("manganese_dioxide"), MOLTEN_INGOT / 2, fluidOf("carbon"), MOLTEN_INGOT, new F[]{fluid("manganese", MOLTEN_INGOT / 2), fluid("carbon_monoxide", 750)}, 100, 25);
        chem(out, "ethanol",               fluidOf("sugar"), MOLTEN_INGOT / 2, Fluids.WATER, 500, new F[]{fluid("ethanol", 2000), fluid("carbon_dioxide", 1000)}, 100, 25);
        chem(out, "carbon_shift",          fluidOf("carbon_dioxide"), 250, fluidOf("hydrogen"), 250, new F[]{fluid("carbon_monoxide", 250), fluid("water", 250)}, 100, 25);
        chem(out, "methanol",              fluidOf("carbon_monoxide"), 250, fluidOf("hydrogen"), 500, new F[]{fluid("methanol", 250)}, 100, 25);
        chem(out, "fluoromethane",         fluidOf("methanol"), 250, fluidOf("hydrofluoric_acid"), 250, new F[]{fluid("fluoromethane", 250), fluid("water", 250)}, 100, 25);
        chem(out, "ethene_sodium",         fluidOf("fluoromethane"), 250, fluidOf("sodium_hydroxide_solution"), MOLTEN_INGOT / 2, new F[]{fluid("ethene", 250), fluid("sodium_fluoride_solution", 250)}, 100, 25);
        chem(out, "ethene_potassium",      fluidOf("fluoromethane"), 250, fluidOf("potassium_hydroxide_solution"), MOLTEN_INGOT / 2, new F[]{fluid("ethene", 250), fluid("potassium_fluoride_solution", 250)}, 100);
        chem(out, "ethanol_sulfuric",      fluidOf("ethene"), 250, fluidOf("sulfuric_acid"), 250, new F[]{fluid("ethanol", 250), fluid("sulfur_trioxide", 250)}, 100);
        chem(out, "slurry_ice",            fluidOf("ice"), 350, fluidOf("ethanol"), 150, new F[]{fluid("slurry_ice", 500)});
        chem(out, "boron_arsenide_solution", fluidOf("boron_arsenide"), MOLTEN_INGOT * 2, Fluids.WATER, 100, new F[]{fluid("boron_arsenide_solution", 360)});
        chem(out, "hydrogen_chloride",     fluidOf("hydrogen"), 250, fluidOf("chlorine"), 150, new F[]{fluid("hydrogen_chloride", 250)}, 100, 25);
        chem(out, "hydrated_gelatin",      fluidOf("gelatin"), MOLTEN_INGOT / 2, Fluids.WATER, 250, new F[]{fluid("hydrated_gelatin", MOLTEN_INGOT * 2)}, 100, 25);
        chem(out, "marshmallow",           fluidOf("hydrated_gelatin"), MOLTEN_INGOT, fluidOf("sugar"), MOLTEN_INGOT / 2, new F[]{fluid("marshmallow", MOLTEN_INGOT)}, 200, 25);
        chem(out, "unsweetened_chocolate", fluidOf("chocolate_liquor"), MOLTEN_INGOT / 2, fluidOf("cocoa_butter"), MOLTEN_INGOT / 2, new F[]{fluid("unsweetened_chocolate", MOLTEN_INGOT)}, 100, 25);
        chem(out, "dark_chocolate",        fluidOf("unsweetened_chocolate"), MOLTEN_INGOT, fluidOf("sugar"), MOLTEN_INGOT / 2, new F[]{fluid("dark_chocolate", MOLTEN_INGOT)}, 100, 25);
        chem(out, "milk_chocolate",        fluidOf("dark_chocolate"), MOLTEN_INGOT, fluidOf("pasteurized_milk"), 250, new F[]{fluid("milk_chocolate", MOLTEN_INGOT * 2)}, 100, 25);
    }

    private static void moltenAlloys(RecipeOutput out) {
        chem(out, "nichrome_molten",       fluidOf("iron"), MOLTEN_INGOT, fluidOf("chromium"), MOLTEN_INGOT * 4, new F[]{fluid("nichrome", MOLTEN_INGOT * 5)}, 100, 25);
        chem(out, "osmiridium_molten",     fluidOf("osmium"), MOLTEN_INGOT * 3, fluidOf("iridium"), MOLTEN_INGOT, new F[]{fluid("osmiridium", MOLTEN_INGOT * 4)}, 100, 25);
        chem(out, "sic_sic_cmc_molten",    fluidOf("carbon_manganese"), MOLTEN_INGOT, fluidOf("titanium"), MOLTEN_INGOT * 11, new F[]{fluid("sic_sic_cmc", MOLTEN_INGOT * 12)}, 100, 25);
        chem(out, "niobium_titanium_molten", fluidOf("niobium"), MOLTEN_INGOT, fluidOf("titanium"), MOLTEN_INGOT, new F[]{fluid("niobium_titanium", MOLTEN_INGOT)}, 100, 25);
        chem(out, "niobium_tin_molten",    fluidOf("niobium"), MOLTEN_INGOT * 2, fluidOf("tin"), MOLTEN_INGOT, new F[]{fluid("niobium_tin", MOLTEN_INGOT * 3)}, 100, 25);
        chem(out, "electrum_molten",       fluidOf("gold"), MOLTEN_INGOT, fluidOf("silver"), MOLTEN_INGOT, new F[]{fluid("electrum", MOLTEN_INGOT * 2)}, 100, 25);
        chem(out, "stainless_steel_molten", fluidOf("steel"), MOLTEN_INGOT, fluidOf("chromium"), MOLTEN_INGOT, new F[]{fluid("stainless_steel", MOLTEN_INGOT * 2)}, 100, 25);
        chem(out, "thermoconducting_molten", fluidOf("extreme"), MOLTEN_INGOT, fluidOf("boron_arsenide"), MOLTEN_INGOT, new F[]{fluid("thermoconducting", MOLTEN_INGOT * 2)}, 100, 25);
        chem(out, "steel_molten",          fluidOf("coal"), MOLTEN_INGOT * 3, fluidOf("iron"), MOLTEN_INGOT, new F[]{fluid("steel", MOLTEN_INGOT)}, 100, 25);
        chem(out, "bronze_molten",         fluidOf("copper"), MOLTEN_INGOT * 3, fluidOf("tin"), MOLTEN_INGOT, new F[]{fluid("bronze", MOLTEN_INGOT * 4)}, 100, 25);
        chem(out, "ferroboron_molten",     fluidOf("boron"), MOLTEN_INGOT, fluidOf("steel"), MOLTEN_INGOT, new F[]{fluid("ferroboron", MOLTEN_INGOT * 2)}, 100, 25);
        chem(out, "hard_carbon_molten",    fluidOf("graphite"), MOLTEN_INGOT, fluidOf("diamond"), MOLTEN_INGOT, new F[]{fluid("hard_carbon", MOLTEN_INGOT * 2)}, 100, 25);
        chem(out, "tough_alloy_molten",    fluidOf("ferroboron"), MOLTEN_INGOT, fluidOf("lithium"), MOLTEN_INGOT, new F[]{fluid("tough_alloy", MOLTEN_INGOT * 2)}, 100, 25);
        chem(out, "magnesium_diboride_molten", fluidOf("magnesium"), MOLTEN_INGOT, fluidOf("boron"), MOLTEN_INGOT * 2, new F[]{fluid("magnesium_diboride", MOLTEN_INGOT * 3)}, 100, 25);
        chem(out, "lithium_manganese_dioxide_molten", fluidOf("lithium"), MOLTEN_INGOT, fluidOf("manganese_dioxide"), MOLTEN_INGOT, new F[]{fluid("lithium_manganese_dioxide", MOLTEN_INGOT * 2)}, 100, 25);
        chem(out, "shibuichi_molten",      fluidOf("copper"), MOLTEN_INGOT * 3, fluidOf("silver"), MOLTEN_INGOT, new F[]{fluid("shibuichi", MOLTEN_INGOT * 4)}, 100, 25);
        chem(out, "tin_silver_molten",     fluidOf("tin"), MOLTEN_INGOT * 3, fluidOf("silver"), MOLTEN_INGOT, new F[]{fluid("tin_silver", MOLTEN_INGOT * 4)}, 100, 25);
        chem(out, "lead_platinum_molten",  fluidOf("lead"), MOLTEN_INGOT * 3, fluidOf("platinum"), MOLTEN_INGOT, new F[]{fluid("lead_platinum", MOLTEN_INGOT * 4)}, 100, 25);
        chem(out, "extreme_molten",        fluidOf("tough_alloy"), MOLTEN_INGOT, fluidOf("hard_carbon"), MOLTEN_INGOT, new F[]{fluid("extreme", MOLTEN_INGOT * 2)}, 100, 25);
        chem(out, "silicon_carbide_molten", fluidOf("silicon"), MOLTEN_INGOT, fluidOf("graphite"), MOLTEN_INGOT, new F[]{fluid("silicon_carbide", MOLTEN_INGOT * 2)}, 100, 25);
        chem(out, "carbon_manganese_molten", fluidOf("manganese"), MOLTEN_INGOT, fluidOf("graphite"), MOLTEN_INGOT, new F[]{fluid("carbon_manganese", MOLTEN_INGOT * 2)}, 300, 75);
        chem(out, "zircaloy_molten",       fluidOf("zirconium"), MOLTEN_INGOT * 7, fluidOf("tin"), MOLTEN_INGOT, new F[]{fluid("zircaloy", MOLTEN_INGOT * 8)}, 100, 25);
        chem(out, "hsla_steel_molten",     fluidOf("iron"), MOLTEN_INGOT * 15, fluidOf("carbon_manganese"), MOLTEN_INGOT, new F[]{fluid("hsla_steel", MOLTEN_INGOT * 16)}, 300, 75);
        chem(out, "zirconium_molybdenum_molten", fluidOf("molybdenum"), MOLTEN_INGOT * 15, fluidOf("zirconium"), MOLTEN_INGOT, new F[]{fluid("zirconium_molybdenum", MOLTEN_INGOT * 16)}, 300, 75);
    }
}
