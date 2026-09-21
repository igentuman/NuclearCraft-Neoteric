package igentuman.nc.datagen.recipe.particle;

import igentuman.nc.api.particle.ParticleIngredient;
import igentuman.nc.api.particle.ParticleStack;
import igentuman.nc.recipe.FluidOutput;
import igentuman.nc.recipe.ItemOutput;
import igentuman.nc.recipe.particle.TargetChamberRecipe;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static igentuman.nc.NuclearCraft.MODID;
import static igentuman.nc.NuclearCraft.rl;
import static igentuman.nc.datagen.recipe.ModRecipeProvider.dust;
import static igentuman.nc.datagen.recipe.ModRecipeProvider.fluidOf;
import static igentuman.nc.datagen.recipe.ModRecipeProvider.gem;
import static igentuman.nc.datagen.recipe.ModRecipeProvider.ingot;
import static igentuman.nc.datagen.recipe.ModRecipeProvider.isotope;
import static igentuman.nc.datagen.recipe.ModRecipeProvider.materialTag;
import static igentuman.nc.datagen.recipe.ModRecipeProvider.part;
import static igentuman.nc.datagen.recipe.ModRecipeProvider.waste;

/**
 * Generates the full legacy target-chamber recipe set. Every recipe here resolves fully against
 * currently-registered materials/isotopes/fluids, including elemental mercury in both its ingot and fluid forms.
 */
public final class TargetChamberRecipes {

    public static final int EXPECTED_COUNT = 440;

    private static final long MOLE_AMOUNT = 1_000_000L;

    private TargetChamberRecipes() {
    }

    public static void generate(RecipeOutput out) {
        proton(out, "aluminum", ingot("aluminum"), 1500, 2000, 50_000_000, gem("silicon"), 0.02, 11_600);
        proton(out, "copper", Items.COPPER_INGOT, 4500, 5900, 25_000_000, ingot("zinc"), 0.04, 7_710);
        proton(out, "manganese", ingot("manganese"), 10000, 19500, 50_000_000, Items.IRON_INGOT, 0.02, 10_200);

        targetItem(out, "proton_boron11_alpha", "proton", isotope("boron/11"), 400, 900, 0.2,
                List.of(stack(3, "alpha")), null, 8_680);
        targetItem(out, "proton_beryllium_lithium6", "proton", ingot("beryllium"), 1000, 7500, 0.625,
                List.of(stack("alpha")), isotope("lithium/6"), 2_130);
        targetFluid(out, "proton_tritium_helion", "proton", fluidOf("tritium"), 1000, 2400, 3600, 0.5,
                List.of(stack("helion"), stack("neutron")), null, 0);
        targetItem(out, "proton_boron10_beryllium7", "proton", isotope("boron/10"), 4000, 6000, 0.8,
                List.of(stack("alpha")), isotope("beryllium_7"), 1_150);
        target(out, "proton_fluorine_oxygen", "proton", null, fluidOf("fluorine"), 1000, 4000, 11000, 1.0, 0.5,
                List.of(stack("alpha")), null, fluidOf("oxygen"), 1000, 8_110);

        targetFluid(out, "proton_deuterium_helion_photon", "proton", fluidOf("deuterium"), 1000, 6000, 10000, 0.02,
                List.of(stack("helion"), stack("photon")), null, 0);
        targetItem(out, "proton_osmium_iridium192", "proton", ingot("osmium"), 8300, 10300, 0.16,
                List.of(stack("neutron")), isotope("iridium_192"), -1_830);
        targetFluid(out, "proton_deuterium_diproton_neutron", "proton", fluidOf("deuterium"), 1000, 11000, 20500, 0.16,
                List.of(stack(2, "proton"), stack("neutron")), null, 0);
        targetItem(out, "proton_thorium_protactinium231", "proton", ingot("thorium"), 11500, 16000, 0.625,
                List.of(stack(2, "neutron")), dust("protactinium_231"), -6_830);
        targetItem(out, "proton_uranium238_neptunium237", "proton", isotope("uranium/238"), 12000, 16500, 0.32,
                List.of(stack(2, "neutron")), isotope("neptunium/237"), -6_420);
        targetItem(out, "proton_plutonium242_americium241", "proton", isotope("plutonium/242"), 12500, 16500, 0.4,
                List.of(stack(2, "neutron")), isotope("americium/241"), -7_070);
        target(out, "proton_gold_mercury", "proton", Items.GOLD_INGOT, null, 0, 12500, 14000, 1.0, 0.5,
                List.of(stack(2, "neutron")), null, fluidOf("mercury"), 144, -8_170);
        targetItem(out, "proton_bismuth_polonium", "proton", dust("bismuth"), 14000, 19000, 0.02,
                List.of(stack("photon")), dust("polonium"), 4_980);
        targetItem(out, "proton_boron11_graphite", "proton", isotope("boron/11"), 15500, 26000, 0.02,
                List.of(stack("photon")), dust("graphite"), 16_000);
        targetItem(out, "proton_calcium_potassium", "proton", ingot("calcium"), 16500, 25000, 1.0,
                List.of(stack(2, "proton")), ingot("potassium"), -8_330);
        target(out, "proton_nitrogen_beryllium7", "proton", null, fluidOf("nitrogen"), 1000, 17000, 26000, 1.0, 0.032,
                List.of(stack(2, "alpha")), isotope("beryllium_7"), null, 0, -10_500);
        target(out, "proton_oxygen_graphite", "proton", null, fluidOf("oxygen"), 1000, 19000, 25500, 1.0, 0.25,
                List.of(stack("alpha"), stack("proton")), dust("graphite"), null, 0, -7_160);
        targetItem(out, "proton_silicon_aluminum", "proton", gem("silicon"), 19000, 28000, 1.0,
                List.of(stack(2, "proton")), ingot("aluminum"), -11_600);
        targetItem(out, "proton_uranium238_neptunium236", "proton", isotope("uranium/238"), 19000, 23000, 1.0,
                List.of(stack(3, "neutron")), isotope("neptunium/236"), -13_000);
        targetItem(out, "proton_sodium_sodium22", "proton", ingot("sodium"), 20000, 28000, 0.5,
                List.of(stack("proton"), stack("neutron")), isotope("sodium_22"), -12_400);
        targetItem(out, "proton_bismuth_lead", "proton", dust("bismuth"), 20000, 24000, 0.04,
                List.of(stack("alpha")), ingot("lead"), 10_400);
        targetItem(out, "proton_uranium235_neptunium236", "proton", isotope("uranium/235"), 20500, 30000, 0.02,
                List.of(stack("photon")), isotope("neptunium/236"), 4_830);
        targetItem(out, "proton_gold_platinum", "proton", Items.GOLD_INGOT, 21000, 25000, 0.064,
                List.of(stack("alpha")), ingot("platinum"), 8_490);
        targetItem(out, "proton_magnesium26_sodium22", "proton", isotope("magnesium_26"), 23000, 29000, 0.1,
                List.of(stack("alpha"), stack("neutron")), isotope("sodium_22"), -14_200);
        targetItem(out, "proton_graphite_beryllium7", "proton", dust("graphite"), 27000, 35000, 0.08,
                List.of(stack("alpha"), stack("neutron"), stack("proton")), isotope("beryllium_7"), -26_300);
        targetItem(out, "proton_boron11_beryllium7_triton", "proton", isotope("boron/11"), 30000, 33000, 0.05,
                List.of(stack("triton"), stack(2, "neutron")), isotope("beryllium_7"), -31_400);
        target(out, "proton_calcium_argon", "proton", ingot("calcium"), null, 0, 30000, 43000, 1.0, 0.20,
                List.of(stack(3, "proton")), null, fluidOf("argon"), 1000, -14_700);
        targetItem(out, "proton_silicon_magnesium26", "proton", gem("silicon"), 32000, 50000, 0.2,
                List.of(stack(3, "proton")), isotope("magnesium_26"), -19_900);
        target(out, "proton_nitrogen_graphite", "proton", null, fluidOf("nitrogen"), 1000, 33000, 46000, 1.0, 0.125,
                List.of(stack("proton"), stack("deuteron")), dust("graphite"), null, 0, -10_300);
        targetItem(out, "proton_magnesium24_sodium22", "proton", isotope("magnesium_24"), 38000, 56000, 0.32,
                List.of(stack(2, "proton"), stack("neutron")), isotope("sodium_22"), -24_100);
        targetItem(out, "proton_graphite_boron11", "proton", dust("graphite"), 40000, 50000, 0.064,
                List.of(stack(2, "proton")), isotope("boron/11"), -16_000);
        target(out, "proton_oxygen_nitrogen", "proton", null, fluidOf("oxygen"), 1000, 40000, 65000, 1.0, 0.05,
                List.of(stack("proton"), stack("deuteron")), null, fluidOf("nitrogen"), 1000, -20_700);
        targetItem(out, "proton_aluminum_sodium22", "proton", ingot("aluminum"), 40000, 50000, 0.08,
                List.of(stack("alpha"), stack("neutron"), stack("proton")), isotope("sodium_22"), -22_500);
        target(out, "proton_calcium_chlorine", "proton", ingot("calcium"), null, 0, 51000, 67000, 1.0, 0.16,
                List.of(stack("helion"), stack("electron_neutrino"), stack("proton")), null, fluidOf("chlorine"), 1000, -18_500);
        targetItem(out, "proton_graphite_boron10", "proton", dust("graphite"), 60000, 85000, 0.08,
                List.of(stack("proton"), stack("deuteron")), isotope("boron/10"), -25_200);
        target(out, "proton_oxygen_boron10", "proton", null, fluidOf("oxygen"), 1000, 65000, 150000, 1.0, 0.02,
                List.of(stack("alpha"), stack("helion")), isotope("boron/10"), null, 0, -26_900);
        targetItem(out, "proton_gold_iridium192", "proton", Items.GOLD_INGOT, 100000, 200000, 0.02,
                List.of(stack("alpha"), stack("neutron"), stack("proton")), isotope("iridium_192"), -6_800);
        targetItem(out, "proton_graphite_beryllium", "proton", dust("graphite"), 150000, 1000000, 0.02,
                List.of(stack("proton"), stack("triton")), ingot("beryllium"), -26_800);
        targetItem(out, "proton_aluminum_sodium", "proton", ingot("aluminum"), 155000, 170000, 0.02,
                List.of(stack(3, "proton"), stack(2, "neutron")), ingot("sodium"), -38_400);
        targetItem(out, "proton_cobalt_nickel", "proton", ingot("cobalt"), 4600, 5600, 0.2,
                List.of(stack("photon")), ingot("nickel"), 9_530);
        targetItem(out, "proton_copper_nickel", "proton", Items.COPPER_INGOT, 45000, 56000, 0.625,
                List.of(stack("alpha")), ingot("nickel"), 3_760);

        targetFluid(out, "neutron_helium3_proton_triton", "neutron", fluidOf("helium_3"), 1000, 0, 18000, 0.1,
                List.of(stack("proton"), stack("triton")), null, 764);
        targetItem(out, "neutron_beryllium7_proton_lithium7", "neutron", isotope("beryllium_7"), 0, 10000, 1.0,
                List.of(stack("proton")), isotope("lithium/7"), 1_640);
        targetItem(out, "neutron_boron10_photon_boron11", "neutron", isotope("boron/10"), 0, 1000, 0.02,
                List.of(stack("photon")), isotope("boron/11"), 11_500);
        target(out, "neutron_sodium22_proton_neon", "neutron", isotope("sodium_22"), null, 0, 0, 11000, 1.0, 0.25,
                List.of(stack("proton")), null, fluidOf("neon"), 1000, 3_630);
        targetItem(out, "neutron_cobalt_photon_cobalt60", "neutron", ingot("cobalt"), 0, 10000, 0.5,
                List.of(stack("photon")), isotope("cobalt_60"), 7_490);
        targetItem(out, "neutron_iridium192_photon_iridium", "neutron", isotope("iridium_192"), 0, 5000, 1.0,
                List.of(stack("photon")), ingot("iridium"), 7_770);
        targetItem(out, "neutron_uranium233_photon_uranium234", "neutron", isotope("uranium/233"), 0, 5000, 1.0,
                List.of(stack("photon")), isotope("uranium/234"), 6_840);
        targetItem(out, "neutron_uranium234_photon_uranium235", "neutron", isotope("uranium/234"), 0, 5000, 1.0,
                List.of(stack("photon")), isotope("uranium/235"), 5_300);
        targetItem(out, "neutron_neptunium236_photon_neptunium237", "neutron", isotope("neptunium/236"), 0, 14000, 1.0,
                List.of(stack("photon")), isotope("neptunium/237"), 6_580);
        targetItem(out, "neutron_plutonium238_photon_plutonium239", "neutron", isotope("plutonium/238"), 0, 5000, 1.0,
                List.of(stack("photon")), isotope("plutonium/239"), 5_650);
        targetItem(out, "neutron_plutonium241_photon_plutonium242", "neutron", isotope("plutonium/241"), 0, 5000, 1.0,
                List.of(stack("photon")), isotope("plutonium/242"), 6_310);
        targetItem(out, "neutron_americium241_photon_americium242", "neutron", isotope("americium/241"), 0, 5000, 1.0,
                List.of(stack("photon")), isotope("americium/242"), 5_540);
        targetItem(out, "neutron_americium242_photon_americium243", "neutron", isotope("americium/242"), 0, 5000, 1.0,
                List.of(stack("photon")), isotope("americium/243"), 6_370);
        targetItem(out, "neutron_sulfur_alpha_silicon", "neutron", dust("sulfur"), 2800, 4000, 0.2,
                List.of(stack("alpha")), gem("silicon"), 1_530);
        targetItem(out, "neutron_zinc_alpha_nickel", "neutron", ingot("zinc"), 5500, 13000, 0.25,
                List.of(stack("alpha")), ingot("nickel"), 3_860);
        targetItem(out, "neutron_lithium6_alpha_neutron_deuteron", "neutron", isotope("lithium/6"), 3000, 21000, 0.2,
                List.of(stack("alpha"), stack("neutron"), stack("deuteron")), null, -1_470);
        target(out, "neutron_chlorine_alpha_sulfur", "neutron", null, fluidOf("chlorine"), 1000, 3000, 13500, 1.0, 0.2,
                List.of(stack("alpha"), stack("electron_antineutrino"), stack("electron")), dust("sulfur"), null, 0, 2_650);
        target(out, "neutron_calcium_alpha_chlorine", "neutron", ingot("calcium"), null, 0, 3000, 15500, 1.0, 0.1,
                List.of(stack("alpha"), stack("electron_neutrino")), null, fluidOf("chlorine"), 1000, 2_050);
        targetItem(out, "neutron_beryllium_alpha_neutron", "neutron", ingot("beryllium"), 4000, 14000, 0.5,
                List.of(stack(2, "alpha"), stack(2, "neutron")), null, -1_570);
        targetItem(out, "neutron_lithium7_alpha_neutron_triton", "neutron", isotope("lithium/7"), 5000, 17000, 0.25,
                List.of(stack("alpha"), stack("neutron"), stack("triton")), null, -2_470);
        target(out, "neutron_nitrogen_triton_graphite", "neutron", null, fluidOf("nitrogen"), 1000, 6000, 16000, 1.0, 0.02,
                List.of(stack("triton")), dust("graphite"), null, 0, -4_010);
        targetItem(out, "neutron_plutonium242_neutron_plutonium241", "neutron", isotope("plutonium/242"), 8500, 13000, 1.0,
                List.of(stack(2, "neutron")), isotope("plutonium/241"), -6_310);
        targetItem(out, "neutron_iridium_neutron_iridium192", "neutron", ingot("iridium"), 9000, 19500, 1.0,
                List.of(stack(2, "neutron")), isotope("iridium_192"), -7_770);
        targetItem(out, "neutron_plutonium239_neutron_plutonium238", "neutron", isotope("plutonium/239"), 9000, 14000, 0.32,
                List.of(stack(2, "neutron")), isotope("plutonium/238"), -5_650);
        targetItem(out, "neutron_americium243_neutron_americium242", "neutron", isotope("americium/243"), 9000, 14500, 1.0,
                List.of(stack(2, "neutron")), isotope("americium/242"), -5_540);
        targetFluid(out, "neutron_tritium_deuteron_neutron", "neutron", fluidOf("tritium"), 1000, 10000, 18000, 0.04,
                List.of(stack("deuteron"), stack(2, "neutron")), null, -6_260);
        targetItem(out, "neutron_neptunium237_neutron_neptunium236", "neutron", isotope("neptunium/237"), 10000, 14000, 1.0,
                List.of(stack(2, "neutron")), isotope("neptunium/236"), -6_580);
        targetFluid(out, "neutron_deuterium_proton_neutron", "neutron", fluidOf("deuterium"), 1000, 11000, 54000, 0.16,
                List.of(stack("proton"), stack(2, "neutron")), null, -2_230);
        targetItem(out, "neutron_iron_alpha_chromium", "neutron", Items.IRON_INGOT, 11000, 22000, 0.1,
                List.of(stack("alpha")), ingot("chromium"), 324);
        targetItem(out, "neutron_copper_proton_nickel", "neutron", Items.COPPER_INGOT, 11000, 15000, 1.0,
                List.of(stack("proton"), stack("neutron")), ingot("nickel"), -6_120);
        targetItem(out, "neutron_chromium_alpha_titanium", "neutron", ingot("chromium"), 12000, 20000, 0.16,
                List.of(stack("alpha")), ingot("titanium"), -1_210);
        targetItem(out, "neutron_boron11_triton_beryllium", "neutron", isotope("boron/11"), 12500, 20000, 0.04,
                List.of(stack("triton")), ingot("beryllium"), -9_560);
        targetItem(out, "neutron_aluminum_proton_magnesium26", "neutron", ingot("aluminum"), 13000, 30000, 0.8,
                List.of(stack("proton"), stack("neutron")), isotope("magnesium_26"), -8_270);
        target(out, "neutron_magnesium24_alpha_neon", "neutron", isotope("magnesium_24"), null, 0, 14000, 32000, 1.0, 0.1,
                List.of(stack("alpha"), stack("neutron")), null, fluidOf("neon"), 1000, -9_320);
        targetItem(out, "neutron_calcium_proton_potassium", "neutron", ingot("calcium"), 14000, 24000, 1.0,
                List.of(stack("proton"), stack("neutron")), ingot("potassium"), -8_330);
        targetItem(out, "neutron_zinc_proton_copper", "neutron", ingot("zinc"), 14000, 24500, 1.0,
                List.of(stack("proton"), stack("neutron")), Items.COPPER_INGOT, -7_710);
        targetItem(out, "neutron_beryllium_triton_lithium7", "neutron", ingot("beryllium"), 15000, 26000, 0.064,
                List.of(stack("triton")), isotope("lithium/7"), -10_400);
        target(out, "neutron_potassium_alpha_chlorine", "neutron", ingot("potassium"), null, 0, 15000, 32000, 1.0, 0.1,
                List.of(stack("alpha"), stack("neutron")), null, fluidOf("chlorine"), 1000, -7_220);
        targetItem(out, "neutron_zirconium_alpha_strontium", "neutron", ingot("zirconium"), 15000, 20000, 0.04,
                List.of(stack("alpha")), ingot("strontium"), 1_760);
        targetItem(out, "neutron_boron11_neutron_boron10", "neutron", isotope("boron/11"), 17000, 29000, 0.05,
                List.of(stack(2, "neutron")), isotope("boron/10"), -11_500);
        targetItem(out, "neutron_sodium_neutron_sodium22", "neutron", ingot("sodium"), 17000, 28000, 0.25,
                List.of(stack(2, "neutron")), isotope("sodium_22"), -12_400);
        targetItem(out, "neutron_uranium235_neutron_uranium233", "neutron", isotope("uranium/235"), 17000, 23500, 0.625,
                List.of(stack(3, "neutron")), isotope("uranium/233"), -12_100);
        targetItem(out, "neutron_silicon_proton_aluminum", "neutron", gem("silicon"), 18000, 27000, 0.8,
                List.of(stack("proton"), stack("neutron")), ingot("aluminum"), -11_600);
        targetItem(out, "neutron_barium_proton_caesium137", "neutron", dust("barium"), 18200, 34000, 0.016,
                List.of(stack("proton")), dust("caesium_137"), -393);
        targetItem(out, "neutron_nickel_alpha_iron", "neutron", ingot("nickel"), 19000, 30000, 0.8,
                List.of(stack("alpha"), stack("neutron")), Items.IRON_INGOT, -6_400);
        targetItem(out, "neutron_platinum_alpha_osmium", "neutron", ingot("platinum"), 19000, 27000, 0.025,
                List.of(stack("alpha")), ingot("osmium"), 8_730);
        targetItem(out, "neutron_terbium_alpha_europium155", "neutron", dust("terbium"), 20800, 34000, 0.5,
                List.of(stack("alpha"), stack("neutron")), dust("europium_155"), -140);
        targetItem(out, "neutron_zirconium_deuteron_yttrium", "neutron", ingot("zirconium"), 21000, 36000, 0.625,
                List.of(stack("deuteron")), ingot("yttrium"), -6_130);
        target(out, "neutron_oxygen_alpha_beryllium", "neutron", null, fluidOf("oxygen"), 1000, 22000, 35000, 1.0, 0.02,
                List.of(stack(2, "alpha")), ingot("beryllium"), null, 0, -12_900);
        targetItem(out, "neutron_niobium_alpha_yttrium", "neutron", ingot("niobium"), 22000, 34000, 0.125,
                List.of(stack("alpha"), stack("neutron")), ingot("yttrium"), -1_930);
        targetItem(out, "neutron_uranium238_neutron_uranium235", "neutron", isotope("uranium/238"), 26000, 34000, 0.55,
                List.of(stack(4, "neutron")), isotope("uranium/235"), -17_800);
        targetItem(out, "neutron_manganese_triton_chromium", "neutron", ingot("manganese"), 29000, 46000, 0.625,
                List.of(stack("triton")), ingot("chromium"), -9_300);
        targetItem(out, "neutron_graphite_deuteron_boron11", "neutron", dust("graphite"), 29500, 60000, 0.2,
                List.of(stack("deuteron")), isotope("boron/11"), -13_700);
        targetItem(out, "neutron_cobalt_triton_iron", "neutron", ingot("cobalt"), 30000, 50000, 0.4,
                List.of(stack("triton")), Items.IRON_INGOT, -8_930);
        targetItem(out, "neutron_yttrium_deuteron_strontium", "neutron", ingot("yttrium"), 30000, 49000, 0.32,
                List.of(stack("deuteron")), ingot("strontium"), -4_840);
        target(out, "neutron_sodium_triton_neon", "neutron", ingot("sodium"), null, 0, 30000, 60000, 1.0, 0.1,
                List.of(stack("triton")), null, fluidOf("neon"), 1000, -10_700);
        targetItem(out, "neutron_gold_alpha_iridium", "neutron", Items.GOLD_INGOT, 30000, 45000, 0.05,
                List.of(stack("alpha"), stack("neutron")), ingot("iridium"), 967);
        targetItem(out, "neutron_magnesium24_deuteron_sodium22", "neutron", isotope("magnesium_24"), 35000, 60000, 0.25,
                List.of(stack("deuteron"), stack("neutron")), isotope("sodium_22"), -21_900);
        targetFluid(out, "neutron_helium_helion_neutron", "neutron", fluidOf("helium"), 1000, 40000, 60000, 0.02,
                List.of(stack("helion"), stack(2, "neutron")), null, -20_600);
        targetItem(out, "neutron_aluminum_helion_sodium22", "neutron", ingot("aluminum"), 44000, 80000, 0.05,
                List.of(stack("helion"), stack(3, "neutron")), isotope("sodium_22"), -43_100);
        targetItem(out, "neutron_bismuth_triton_lead", "neutron", dust("bismuth"), 70000, 150000, 0.2,
                List.of(stack("triton")), ingot("lead"), -2_680);
        targetItem(out, "neutron_graphite_helion_beryllium7", "neutron", dust("graphite"), 105000, 150000, 0.032,
                List.of(stack("helion"), stack(3, "neutron")), isotope("beryllium_7"), -46_800);

        targetFluid(out, "photon_deuterium_proton_neutron", "photon", fluidOf("deuterium"), 1000, 2500, 14000, 0.02,
                List.of(stack("proton"), stack("neutron")), null, -2_230);
        targetItem(out, "photon_lithium6_alpha_neutron_proton", "photon", isotope("lithium/6"), 5500, 17000, 0.02,
                List.of(stack("alpha"), stack("neutron"), stack("proton")), null, -3_700);
        targetFluid(out, "photon_helium3_diproton_neutron", "photon", fluidOf("helium_3"), 1000, 9000, 36000, 0.02,
                List.of(stack(2, "proton"), stack("neutron")), null, -7_720);
        targetItem(out, "photon_neptunium237_neutron_neptunium236", "photon", isotope("neptunium/237"), 9750, 14500, 1.0,
                List.of(stack("neutron")), isotope("neptunium/236"), -6_580);
        targetItem(out, "photon_plutonium239_neutron_plutonium238", "photon", isotope("plutonium/239"), 10000, 13500, 0.4,
                List.of(stack("neutron")), isotope("plutonium/238"), -5_650);
        targetItem(out, "photon_americium243_neutron_americium242", "photon", isotope("americium/243"), 10000, 12500, 1.0,
                List.of(stack("neutron")), isotope("americium/242"), -6_360);
        targetItem(out, "photon_tungsten_alpha_hafnium", "photon", ingot("tungsten"), 11000, 16500, 0.25,
                List.of(stack("alpha")), ingot("hafnium"), 1_660);
        targetItem(out, "photon_zirconium_proton_yttrium", "photon", ingot("zirconium"), 11500, 19000, 0.04,
                List.of(stack("proton")), ingot("yttrium"), -8_350);
        targetItem(out, "photon_iridium_neutron_iridium192", "photon", ingot("iridium"), 12000, 16000, 0.5,
                List.of(stack("neutron")), isotope("iridium_192"), -7_770);
        targetItem(out, "photon_bismuth_proton_lead", "photon", dust("bismuth"), 12000, 15000, 0.5,
                List.of(stack("proton"), stack("neutron")), ingot("lead"), -11_200);
        targetItem(out, "photon_niobium_alpha_yttrium", "photon", ingot("niobium"), 13000, 21000, 0.02,
                List.of(stack("alpha")), ingot("yttrium"), -1_930);
        targetItem(out, "photon_uranium235_neutron_uranium233", "photon", isotope("uranium/235"), 13500, 18000, 0.32,
                List.of(stack(2, "neutron")), isotope("uranium/233"), -12_100);
        targetItem(out, "photon_iron_proton_manganese", "photon", Items.IRON_INGOT, 14500, 21000, 0.125,
                List.of(stack("proton")), ingot("manganese"), -10_200);
        targetItem(out, "photon_yttrium_proton_strontium", "photon", ingot("yttrium"), 16000, 23000, 0.016,
                List.of(stack("proton")), ingot("strontium"), -7_070);
        targetItem(out, "photon_aluminum_proton_magnesium26", "photon", ingot("aluminum"), 17500, 23000, 0.032,
                List.of(stack("proton")), isotope("magnesium_26"), -8_270);
        targetItem(out, "photon_silicon_proton_aluminum", "photon", gem("silicon"), 18000, 23000, 0.08,
                List.of(stack("proton")), ingot("aluminum"), -11_600);
        targetItem(out, "photon_calcium_proton_potassium", "photon", ingot("calcium"), 18000, 22000, 0.05,
                List.of(stack("proton")), ingot("potassium"), -8_330);
        targetItem(out, "photon_lithium6_helion_triton", "photon", isotope("lithium/6"), 18500, 25500, 0.02,
                List.of(stack("helion"), stack("triton")), null, -15_800);
        targetItem(out, "photon_lithium7_neutron_lithium6", "photon", isotope("lithium/7"), 18500, 24000, 0.02,
                List.of(stack("neutron")), isotope("lithium/6"), -7_250);
        targetItem(out, "photon_copper_nickel", "photon", Items.COPPER_INGOT, 19500, 27000, 0.05,
                List.of(stack("proton"), stack("neutron")), ingot("nickel"), -16_700);
        target(out, "photon_nitrogen_proton_graphite", "photon", null, fluidOf("nitrogen"), 1000, 20000, 27000, 1.0, 0.02,
                List.of(stack("proton"), stack("neutron")), dust("graphite"), null, 0, -12_500);
        targetItem(out, "photon_magnesium26_neutron_magnesium24", "photon", isotope("magnesium_26"), 21000, 28000, 0.04,
                List.of(stack(2, "neutron")), isotope("magnesium_24"), -18_400);
        targetItem(out, "photon_boron11_alpha_neutron_deuteron", "photon", isotope("boron/11"), 24500, 30500, 0.02,
                List.of(stack(2, "alpha"), stack("neutron"), stack("deuteron")), null, -17_400);
        targetItem(out, "photon_beryllium_alpha_neutron", "photon", ingot("beryllium"), 26000, 46000, 0.02,
                List.of(stack(2, "alpha"), stack("neutron")), null, -1_570);
        target(out, "photon_oxygen_proton_nitrogen", "photon", null, fluidOf("oxygen"), 1000, 29000, 41000, 1.0, 0.02,
                List.of(stack("proton"), stack("neutron")), null, fluidOf("nitrogen"), 1000, -23_000);
        targetItem(out, "photon_graphite_alpha_beryllium7", "photon", dust("graphite"), 31500, 42500, 0.02,
                List.of(stack("alpha"), stack("neutron")), isotope("beryllium_7"), -26_300);
        targetItem(out, "photon_graphite_alpha_lithium6", "photon", dust("graphite"), 42500, 55000, 0.02,
                List.of(stack("alpha"), stack("neutron"), stack("proton")), isotope("lithium/6"), -31_900);

        targetItem(out, "electron_iron_alpha_chromium", "electron", Items.IRON_INGOT, 50000, 100000, 0.01,
                List.of(stack("alpha"), stack("electron")), ingot("chromium"), -7_610);
        targetItem(out, "electron_cobalt_alpha_manganese", "electron", ingot("cobalt"), 50000, 100000, 0.01,
                List.of(stack("alpha"), stack("electron")), ingot("manganese"), -6_940);
        targetItem(out, "electron_zinc_alpha_nickel", "electron", ingot("zinc"), 50000, 100000, 0.01,
                List.of(stack("alpha"), stack("electron")), ingot("nickel"), -3_960);
        targetItem(out, "electron_zirconium_proton_yttrium", "electron", ingot("zirconium"), 60000, 130000, 0.01,
                List.of(stack("proton"), stack("electron")), ingot("yttrium"), -8_350);

        targetFluid(out, "deuteron_tritium_alpha_neutron", "deuteron", fluidOf("tritium"), 1000, 50, 600, 0.5,
                List.of(stack("alpha"), stack("neutron")), null, 17_100);
        targetFluid(out, "deuteron_deuterium_triton_neutron", "deuteron", fluidOf("deuterium"), 1000, 500, 3000, 0.16,
                List.of(stack("triton"), stack("neutron")), null, 2_740);
        targetItem(out, "deuteron_lithium6_neutron_beryllium7", "deuteron", isotope("lithium/6"), 500, 3000, 0.16,
                List.of(stack("neutron")), isotope("beryllium_7"), 3_380);
        targetItem(out, "deuteron_beryllium_alpha_lithium7", "deuteron", ingot("beryllium"), 1500, 5000, 0.625,
                List.of(stack("alpha")), isotope("lithium/7"), 7_150);
        targetItem(out, "deuteron_boron11_neutron_graphite", "deuteron", isotope("boron/11"), 1500, 3000, 0.5,
                List.of(stack("neutron")), dust("graphite"), 13_700);
        targetItem(out, "deuteron_lithium7_alpha_neutron", "deuteron", isotope("lithium/7"), 3500, 9000, 1.0,
                List.of(stack(2, "alpha"), stack("neutron")), null, 15_100);
        target(out, "deuteron_oxygen_alpha_nitrogen", "deuteron", null, fluidOf("oxygen"), 1000, 3500, 10000, 1.0, 0.25,
                List.of(stack("alpha")), null, fluidOf("nitrogen"), 1000, 3_110);
        targetItem(out, "deuteron_graphite_alpha_boron10", "deuteron", dust("graphite"), 5000, 13000, 0.625,
                List.of(stack("alpha")), isotope("boron/10"), -1_340);
        targetItem(out, "deuteron_cobalt_proton_cobalt60", "deuteron", ingot("cobalt"), 5000, 10000, 0.4,
                List.of(stack("proton")), isotope("cobalt_60"), 5_270);
        targetItem(out, "deuteron_magnesium24_alpha_sodium22", "deuteron", isotope("magnesium_24"), 6000, 11000, 0.4,
                List.of(stack("alpha")), isotope("sodium_22"), 1_960);
        targetItem(out, "deuteron_bismuth_neutron_polonium", "deuteron", dust("bismuth"), 10000, 15000, 0.08,
                List.of(stack("neutron")), dust("polonium"), 2_760);
        targetItem(out, "deuteron_uranium233_proton_uranium234", "deuteron", isotope("uranium/233"), 10000, 16000, 0.08,
                List.of(stack("proton")), isotope("uranium/234"), 4_620);
        targetItem(out, "deuteron_plutonium241_neutron_americium241", "deuteron", isotope("plutonium/241"), 10500, 19500, 0.5,
                List.of(stack(2, "neutron")), isotope("americium/241"), -2_990);
        targetItem(out, "deuteron_uranium234_proton_uranium235", "deuteron", isotope("uranium/234"), 11000, 17000, 0.32,
                List.of(stack("proton")), isotope("uranium/235"), 3_070);
        targetItem(out, "deuteron_plutonium238_proton_plutonium239", "deuteron", isotope("plutonium/238"), 11000, 17000, 0.2,
                List.of(stack("proton")), isotope("plutonium/239"), 3_420);
        targetItem(out, "deuteron_uranium235_neutron_neptunium236", "deuteron", isotope("uranium/235"), 11500, 19500, 0.032,
                List.of(stack("neutron")), isotope("neptunium/236"), 2_610);
        targetItem(out, "deuteron_osmium_neutron_iridium192", "deuteron", ingot("osmium"), 12000, 14500, 1.0,
                List.of(stack(2, "neutron")), isotope("iridium_192"), -4_050);
        targetItem(out, "deuteron_plutonium242_neutron_americium242", "deuteron", isotope("plutonium/242"), 12000, 16000, 0.5,
                List.of(stack(2, "neutron")), isotope("americium/242"), -3_760);
        targetItem(out, "deuteron_americium243_neutron_curium243", "deuteron", isotope("americium/243"), 12000, 15000, 0.2,
                List.of(stack(2, "neutron")), isotope("curium/243"), -3_010);
        target(out, "deuteron_gold_mercury", "deuteron", Items.GOLD_INGOT, null, 0, 18000, 24000, 1.0, 0.5,
                List.of(stack(3, "neutron")), null, fluidOf("mercury"), 144, -10_400);
        targetItem(out, "deuteron_uranium238_neutron_neptunium236", "deuteron", isotope("uranium/238"), 24000, 30000, 1.0,
                List.of(stack(4, "neutron")), isotope("neptunium/236"), -15_200);
        targetItem(out, "deuteron_sodium_deuteron_sodium22", "deuteron", ingot("sodium"), 30000, 55000, 0.4,
                List.of(stack("deuteron"), stack("neutron")), isotope("sodium_22"), -12_400);
        targetItem(out, "deuteron_beryllium_deuteron_beryllium7", "deuteron", ingot("beryllium"), 34000, 53000, 0.064,
                List.of(stack("deuteron"), stack(2, "neutron")), isotope("beryllium_7"), -20_600);
        targetItem(out, "deuteron_beryllium_alpha_neutron_deuteron", "deuteron", ingot("beryllium"), 55000, 150000, 1.0,
                List.of(stack(2, "alpha"), stack("neutron"), stack("deuteron")), null, -1_570);
        targetItem(out, "deuteron_aluminum_alpha_triton_sodium22", "deuteron", ingot("aluminum"), 56000, 200000, 0.08,
                List.of(stack("alpha"), stack("triton")), isotope("sodium_22"), -16_300);
        targetItem(out, "deuteron_yttrium_alpha_strontium", "deuteron", ingot("yttrium"), 60000, 200000, 0.064,
                List.of(stack("alpha")), ingot("strontium"), 7_890);

        targetItem(out, "triton_beryllium_neutron_boron11", "triton", ingot("beryllium"), 1000, 7000, 1.0,
                List.of(stack("neutron")), isotope("boron/11"), 9_560);
        targetFluid(out, "triton_tritium_alpha_neutron", "triton", fluidOf("tritium"), 1000, 1200, 3000, 0.1,
                List.of(stack("alpha"), stack(2, "neutron")), null, 10_800);
        targetItem(out, "triton_graphite_alpha_boron11", "triton", dust("graphite"), 2500, 7500, 0.625,
                List.of(stack("alpha")), isotope("boron/11"), 3_860);
        targetItem(out, "triton_lithium6_neutron_beryllium7", "triton", isotope("lithium/6"), 8000, 12000, 0.064,
                List.of(stack(2, "neutron")), isotope("beryllium_7"), -2_520);

        targetItem(out, "helion_lithium6_deuteron_beryllium7", "helion", isotope("lithium/6"), 11000, 30000, 0.5,
                List.of(stack("deuteron")), isotope("beryllium_7"), 113);
        targetItem(out, "helion_cobalt_proton_cobalt60", "helion", ingot("cobalt"), 14000, 24000, 0.1,
                List.of(stack(2, "proton")), isotope("cobalt_60"), -226);
        targetItem(out, "helion_graphite_alpha_beryllium7", "helion", dust("graphite"), 22000, 31000, 0.125,
                List.of(stack(2, "alpha")), isotope("beryllium_7"), -5_690);
        targetItem(out, "helion_beryllium_alpha_neutron_beryllium7", "helion", ingot("beryllium"), 23000, 28000, 0.16,
                List.of(stack("alpha"), stack("neutron")), isotope("beryllium_7"), 14);
        targetItem(out, "helion_lead_neutron_polonium", "helion", ingot("lead"), 23000, 30000, 0.02,
                List.of(stack("neutron")), dust("polonium"), 1_060);
        targetItem(out, "helion_neptunium237_proton_plutonium238", "helion", isotope("neptunium/237"), 23500, 30000, 0.02,
                List.of(stack("proton"), stack("neutron")), isotope("plutonium/238"), -1_720);
        targetItem(out, "helion_lithium6_alpha_proton", "helion", isotope("lithium/6"), 30000, 150000, 0.625,
                List.of(stack(2, "alpha"), stack("proton")), null, 16_900);
        targetItem(out, "helion_boron10_alpha_beryllium7", "helion", isotope("boron/10"), 30000, 47000, 0.2,
                List.of(stack("alpha"), stack("neutron"), stack("proton")), isotope("beryllium_7"), -6_570);
        targetItem(out, "helion_bismuth_proton_polonium", "helion", dust("bismuth"), 30000, 43000, 0.2,
                List.of(stack("proton"), stack("neutron")), dust("polonium"), -2_730);
        targetItem(out, "helion_aluminum_alpha_sodium22", "helion", ingot("aluminum"), 90000, 165000, 0.16,
                List.of(stack(2, "alpha")), isotope("sodium_22"), -1_930);

        targetItem(out, "alpha_beryllium_neutron_graphite", "alpha", ingot("beryllium"), 4000, 6500, 1.0,
                List.of(stack("neutron")), dust("graphite"), 5_700);
        targetItem(out, "alpha_magnesium26_neutron_silicon", "alpha", isotope("magnesium_26"), 4000, 6000, 0.32,
                List.of(stack("neutron")), gem("silicon"), 35);
        targetItem(out, "alpha_sodium_proton_magnesium26", "alpha", ingot("sodium"), 4500, 6000, 0.16,
                List.of(stack("proton")), isotope("magnesium_26"), 1_820);
        target(out, "alpha_fluorine_neutron_sodium22", "alpha", null, fluidOf("fluorine"), 1000, 6000, 11000, 1.0, 0.25,
                List.of(stack("neutron")), isotope("sodium_22"), null, 0, -1_950);
        target(out, "alpha_fluorine_proton_neon", "alpha", null, fluidOf("fluorine"), 1000, 11000, 17500, 1.0, 0.16,
                List.of(stack("proton")), null, fluidOf("neon"), 1000, 1_670);
        targetItem(out, "alpha_lithium7_neutron_boron10", "alpha", isotope("lithium/7"), 6500, 7600, 0.5,
                List.of(stack("neutron")), isotope("boron/10"), -2_790);
        targetItem(out, "alpha_aluminum_positron_silicon", "alpha", ingot("aluminum"), 12000, 17500, 0.4,
                List.of(stack("positron"), stack("neutron"), stack("electron_neutrino")), gem("silicon"), 568);
        target(out, "alpha_nitrogen_proton_oxygen", "alpha", null, fluidOf("nitrogen"), 1000, 14000, 26000, 1.0, 0.1,
                List.of(stack("proton"), stack("neutron")), null, fluidOf("oxygen"), 1000, -5_330);
        targetItem(out, "alpha_copper_proton_zinc", "alpha", Items.COPPER_INGOT, 16000, 20000, 0.16,
                List.of(stack("proton")), ingot("zinc"), -1_540);
        target(out, "alpha_oxygen_positron_fluorine", "alpha", null, fluidOf("oxygen"), 1000, 18000, 25000, 1.0, 0.032,
                List.of(stack("positron"), stack("neutron"), stack("electron_neutrino")), null, fluidOf("fluorine"), 1000, -9_920);
        targetItem(out, "alpha_osmium_neutron_platinum", "alpha", ingot("osmium"), 19000, 27500, 0.025,
                List.of(stack("neutron")), ingot("platinum"), -8_730);
        targetItem(out, "alpha_uranium235_neutron_plutonium238", "alpha", isotope("uranium/235"), 21000, 32000, 0.02,
                List.of(stack("neutron")), isotope("plutonium/238"), -10_900);
        targetItem(out, "alpha_ytterbium_neutron_hafnium", "alpha", dust("ytterbium"), 21200, 26600, 1.0,
                List.of(stack(2, "neutron")), ingot("hafnium"), -14_800);
        targetItem(out, "alpha_lead_neutron_polonium", "alpha", ingot("lead"), 26000, 32000, 1.0,
                List.of(stack(2, "neutron")), dust("polonium"), -19_500);
        targetItem(out, "alpha_lithium6_photon_boron10", "alpha", isotope("lithium/6"), 27000, 45000, 0.02,
                List.of(stack("photon")), isotope("boron/10"), 4_460);
        targetItem(out, "alpha_uranium233_proton_neptunium236", "alpha", isotope("uranium/233"), 27000, 30000, 0.02,
                List.of(stack("proton")), isotope("neptunium/236"), -11_300);
        targetItem(out, "alpha_uranium238_neutron_plutonium239", "alpha", isotope("uranium/238"), 27000, 30000, 0.625,
                List.of(stack(3, "neutron")), isotope("plutonium/239"), -23_100);
        targetItem(out, "alpha_plutonium239_proton_americium242", "alpha", isotope("plutonium/239"), 28000, 30000, 0.02,
                List.of(stack("proton")), isotope("americium/242"), -11_700);
        target(out, "alpha_nitrogen_alpha_graphite", "alpha", null, fluidOf("nitrogen"), 1000, 30000, 56000, 1.0, 0.25,
                List.of(stack("alpha"), stack("neutron"), stack("proton")), dust("graphite"), null, 0, -12_500);
        targetItem(out, "alpha_uranium235_triton_neptunium236", "alpha", isotope("uranium/235"), 30000, 35000, 0.016,
                List.of(stack("triton")), isotope("neptunium/236"), -15_500);
        targetItem(out, "alpha_uranium238_neutron_plutonium238", "alpha", isotope("uranium/238"), 34500, 42000, 0.128,
                List.of(stack(4, "neutron")), isotope("plutonium/238"), -28_700);
        targetItem(out, "alpha_graphite_alpha_boron11", "alpha", dust("graphite"), 38000, 50000, 0.25,
                List.of(stack("alpha"), stack("proton")), isotope("boron/11"), -16_000);
        targetItem(out, "alpha_cobalt_proton_cobalt60", "alpha", ingot("cobalt"), 38000, 54000, 0.16,
                List.of(stack(2, "proton"), stack("neutron")), isotope("cobalt_60"), -20_800);
        targetItem(out, "alpha_graphite_alpha_boron10", "alpha", dust("graphite"), 53000, 69000, 0.16,
                List.of(stack("alpha"), stack("neutron"), stack("proton")), isotope("boron/10"), -27_400);
        targetItem(out, "alpha_beryllium_alpha_beryllium7", "alpha", ingot("beryllium"), 100000, 145000, 0.064,
                List.of(stack("alpha"), stack(2, "neutron")), isotope("beryllium_7"), -20_500);

        targetItem(out, "boron_ion_silicon_wafer_p_doped", "boron_ion", part("silicon_wafer"), 600, 1000, 2.0, 1.0,
                List.of(), part("silicon_p_doped"), 0);
        targetItem(out, "boron_ion_irradiation_chamber_piledriver", "boron_ion",
                part("fission_reactor_irradiation_chamber"), 1000, 2000, 3.0, 0.25,
                List.of(stack("proton"), stack("positron"), stack("electron")),
                part("fission_reactor_pile-driver_irradiation_chamber"), 0);
        targetItem(out, "boron_ion_lithium7_alpha_graphite", "boron_ion", isotope("lithium/7"), 6000, 12000, 0.2,
                List.of(stack("alpha"), stack(2, "neutron")), dust("graphite"), 5_010);

        targetItem(out, "ca48_berkelium248_alpha_copernicium291", "calcium_48_ion", isotope("berkelium/248"),
                40000, 50000, 2.0, 0.02,
                List.of(stack("alpha"), stack("neutron"), stack(3, "electron_neutrino")),
                isotope("copernicium/291"), -24_400);

        targetItem(out, "electron_antineutrino_iridium192_positron_osmium", "electron_antineutrino",
                isotope("iridium_192"), 0, 30000, 0.01, List.of(stack("positron")), ingot("osmium"), 25);
        targetItem(out, "electron_antineutrino_nickel_positron_iron", "electron_antineutrino",
                ingot("nickel"), 200, 10900, 0.01,
                List.of(stack(2, "positron"), stack("electron_neutrino")), Items.IRON_INGOT, -117);
        targetItem(out, "electron_antineutrino_americium242_positron_plutonium242", "electron_antineutrino",
                isotope("americium/242"), 300, 30000, 0.01, List.of(stack("positron")), isotope("plutonium/242"), -271);
        targetItem(out, "electron_antineutrino_curium243_positron_americium243", "electron_antineutrino",
                isotope("curium/243"), 1100, 30000, 0.01, List.of(stack("positron")), isotope("americium/243"), -1_010);
        targetItem(out, "electron_antineutrino_berkelium247_positron_curium247", "electron_antineutrino",
                isotope("berkelium/247"), 1100, 30000, 0.01, List.of(stack("positron")), isotope("curium/247"), -1_070);

        targetItem(out, "electron_neutrino_curium247_electron_berkelium247", "electron_neutrino",
                isotope("curium/247"), 0, 30000, 0.01, List.of(stack("electron")), isotope("berkelium/247"), 44);
        targetItem(out, "electron_neutrino_americium243_electron_curium243", "electron_neutrino",
                isotope("americium/243"), 100, 30000, 0.01, List.of(stack("electron")), isotope("curium/243"), -8);
        targetItem(out, "electron_neutrino_plutonium242_electron_americium242", "electron_neutrino",
                isotope("plutonium/242"), 800, 30000, 0.01, List.of(stack("electron")), isotope("americium/242"), -751);
        targetItem(out, "electron_neutrino_osmium_electron_iridium192", "electron_neutrino",
                ingot("osmium"), 1100, 30000, 0.01, List.of(stack("electron")), isotope("iridium_192"), -1_050);

        targetItem(out, "pion_minus_aluminum_proton_sodium22", "pion_minus", ingot("aluminum"), 150000, 250000, 0.025,
                List.of(stack("proton"), stack("neutron")), isotope("sodium_22"), 87_500);
        target(out, "pion_plus_argon_proton_chlorine", "pion_plus", null, fluidOf("argon"), 1000, 70000, 320000, 1.0, 0.04,
                List.of(stack(2, "proton"), stack("neutron")), null, fluidOf("chlorine"), 1000, 114_000);

        targetItem(out, "proton_radium_neutron_heavy", "proton", dust("radium"), 60000, 600000, 0.2,
                List.of(stack("neutron")), waste("heavy"), 0);
        targetItem(out, "proton_thorium_neutron_heavy", "proton", ingot("thorium"), 60000, 600000, 1.0,
                List.of(stack("neutron")), waste("heavy"), 0);
        targetItem(out, "proton_uranium233_neutron_heavy", "proton", isotope("uranium/233"), 60000, 600000, 1.0,
                List.of(stack("neutron")), waste("heavy"), 0);
        targetItem(out, "proton_uranium234_neutron_heavy", "proton", isotope("uranium/234"), 60000, 600000, 1.0,
                List.of(stack(2, "neutron")), waste("heavy"), 0);
        targetItem(out, "proton_uranium235_neutron_heavy", "proton", isotope("uranium/235"), 60000, 600000, 1.0,
                List.of(stack(4, "neutron")), waste("heavy"), 0);
        targetItem(out, "proton_uranium238_neutron_heavy", "proton", isotope("uranium/238"), 60000, 600000, 1.0,
                List.of(stack(2, "neutron")), waste("heavy"), 0);
        targetItem(out, "proton_neptunium237_neutron_heavy", "proton", isotope("neptunium/237"), 60000, 600000, 1.0,
                List.of(stack(2, "neutron")), waste("heavy"), 0);
        targetItem(out, "proton_plutonium239_neutron_heavy", "proton", isotope("plutonium/239"), 60000, 600000, 1.0,
                List.of(stack(4, "neutron")), waste("heavy"), 0);
        targetItem(out, "proton_plutonium241_neutron_heavy", "proton", isotope("plutonium/241"), 60000, 600000, 1.0,
                List.of(stack(4, "neutron")), waste("heavy"), 0);
        targetItem(out, "proton_plutonium242_neutron_heavy", "proton", isotope("plutonium/242"), 60000, 600000, 1.0,
                List.of(stack(4, "neutron")), waste("heavy"), 0);
        targetItem(out, "proton_americium241_neutron_heavy", "proton", isotope("americium/241"), 60000, 600000, 1.0,
                List.of(stack(4, "neutron")), waste("heavy"), 0);
        targetItem(out, "proton_americium243_neutron_heavy", "proton", isotope("americium/243"), 60000, 600000, 1.0,
                List.of(stack(2, "neutron")), waste("heavy"), 0);
        targetItem(out, "proton_copernicium291_neutron_heavy", "proton", isotope("copernicium/291"), 60000, 600000, 1.0,
                List.of(stack(8, "neutron")), waste("heavy"), 0);

        targetItem(out, "proton_bismuth_neutron_light", "proton", dust("bismuth"), 100000, 600000, 0.4,
                List.of(stack("neutron")), waste("light"), 0);
        targetItem(out, "proton_platinum_neutron_light", "proton", ingot("platinum"), 200000, 600000, 0.02,
                List.of(stack("neutron")), waste("light"), 0);
        targetItem(out, "proton_gold_neutron_light", "proton", Items.GOLD_INGOT, 200000, 600000, 0.16,
                List.of(stack("neutron")), waste("light"), 0);
        targetItem(out, "proton_mercury_neutron_light", "proton", ingot("mercury"), 200000, 600000, 0.02,
                List.of(stack("neutron")), waste("light"), 0);
        target(out, "proton_mercury_fluid_neutron_light", "proton", null, fluidOf("mercury"), 144, 200000, 600000,
                1.0, 0.02, List.of(stack("neutron")), waste("light"), null, 0, 0);
        targetItem(out, "proton_lead_neutron_light", "proton", ingot("lead"), 200000, 600000, 0.25,
                List.of(stack("neutron")), waste("light"), 0);
        targetItem(out, "proton_tungsten_neutron_light", "proton", ingot("tungsten"), 400000, 600000, 0.08,
                List.of(stack("neutron")), waste("light"), 0);

        targetItem(out, "neutron_copernicium291_neutron_heavy", "neutron", isotope("copernicium/291"), 60000, 1000000, 1.0,
                List.of(stack(8, "neutron")), waste("heavy"), 0);

        targetItem(out, "electron_uranium233_neutron_heavy", "electron", isotope("uranium/233"), 170000, 300000, 0.01,
                List.of(stack("neutron"), stack("electron")), waste("heavy"), 0);
        targetItem(out, "electron_uranium235_neutron_heavy", "electron", isotope("uranium/235"), 170000, 300000, 0.01,
                List.of(stack("neutron"), stack("electron")), waste("heavy"), 0);
        targetItem(out, "electron_plutonium239_neutron_heavy", "electron", isotope("plutonium/239"), 170000, 300000, 0.01,
                List.of(stack("neutron"), stack("electron")), waste("heavy"), 0);
        targetItem(out, "electron_neptunium237_neutron_heavy", "electron", isotope("neptunium/237"), 180000, 300000, 0.01,
                List.of(stack("neutron"), stack("electron")), waste("heavy"), 0);
        targetItem(out, "electron_uranium238_neutron_heavy", "electron", isotope("uranium/238"), 200000, 300000, 0.01,
                List.of(stack("neutron"), stack("electron")), waste("heavy"), 0);
        targetItem(out, "electron_americium242_neutron_heavy", "electron", isotope("americium/242"), 200000, 300000, 0.01,
                List.of(stack("neutron"), stack("electron")), waste("heavy"), 0);
        targetItem(out, "electron_thorium_neutron_heavy", "electron", ingot("thorium"), 220000, 300000, 0.01,
                List.of(stack("neutron"), stack("electron")), waste("heavy"), 0);

        spallationBatch(out, "pion_production", "proton", 600000, 5000000, 0.2,
                List.of(stack("pion_plus"), stack("pion_minus")), -279_000);
        target(out, "pion_production_mercury_fluid", "proton", null, fluidOf("mercury"), 144, 600000, 5000000,
                1.0, 0.2, List.of(stack("pion_plus"), stack("pion_minus")), waste("mercury"), null, 0, -279_000);
        spallationBatch(out, "antiproton_production", "proton", 5630000, 20000000, 0.2,
                List.of(stack("proton"), stack("antiproton")), -1_880_000);
        target(out, "antiproton_production_mercury_fluid", "proton", null, fluidOf("mercury"), 144, 5630000, 20000000,
                1.0, 0.2, List.of(stack("proton"), stack("antiproton")), waste("mercury"), null, 0, -1_880_000);
        spallationBatch(out, "antideuteron_production", "deuteron", 11300000, 20000000, 0.1,
                List.of(stack("deuteron"), stack("antideuteron")), -3_750_000);
        target(out, "antideuteron_production_mercury_fluid", "deuteron", null, fluidOf("mercury"), 144, 11300000, 20000000,
                1.0, 0.1, List.of(stack("deuteron"), stack("antideuteron")), waste("mercury"), null, 0, -3_750_000);
        spallationBatch(out, "antiproton_annihilation", "antiproton", 1, 10000000, 1.0,
                List.of(stack("pion_plus"), stack("pion_naught"), stack("pion_minus")), 1_460_000);
        target(out, "antiproton_annihilation_mercury_fluid", "antiproton", null, fluidOf("mercury"), 144, 1, 10000000,
                1.0, 1.0, List.of(stack("pion_plus"), stack("pion_naught"), stack("pion_minus")), waste("mercury"),
                null, 0, 1_460_000);
        spallationBatch(out, "antideuteron_annihilation", "antideuteron", 1, 10000000, 1.0,
                List.of(stack(4, "pion_plus"), stack(4, "pion_naught"), stack(4, "pion_minus")), 2_090_000);
        target(out, "antideuteron_annihilation_mercury_fluid", "antideuteron", null, fluidOf("mercury"), 144, 1, 10000000,
                1.0, 1.0, List.of(stack(4, "pion_plus"), stack(4, "pion_naught"), stack(4, "pion_minus")),
                waste("mercury"), null, 0, 2_090_000);
    }

    private static void spallationBatch(RecipeOutput out, String groupPrefix, String inputSpecies,
                                         long minimumEnergyKeV, long maximumEnergyKeV, double crossSection,
                                         List<ParticleStack> particleOutputs, long releasedEnergyKeV) {
        for (Map.Entry<Item, Item> entry : spallationMaterials().entrySet()) {
            String suffix = BuiltInRegistries.ITEM.getKey(entry.getKey()).getPath();
            targetItem(out, groupPrefix + "_" + suffix, inputSpecies, entry.getKey(), minimumEnergyKeV,
                    maximumEnergyKeV, crossSection, particleOutputs, entry.getValue(), releasedEnergyKeV);
        }
    }

    private static Map<Item, Item> spallationMaterials() {
        Map<Item, Item> map = new LinkedHashMap<>();
        putSpallation(map, isotope("californium/252"), waste("californium"));
        putSpallation(map, isotope("californium/251"), waste("californium"));
        putSpallation(map, isotope("californium/250"), waste("californium"));
        putSpallation(map, isotope("californium/249"), waste("californium"));
        putSpallation(map, isotope("berkelium/248"), waste("berkelium"));
        putSpallation(map, isotope("berkelium/247"), waste("berkelium"));
        putSpallation(map, isotope("curium/247"), waste("curium"));
        putSpallation(map, isotope("curium/246"), waste("curium"));
        putSpallation(map, isotope("curium/245"), waste("curium"));
        putSpallation(map, isotope("curium/243"), waste("curium"));
        putSpallation(map, isotope("americium/243"), waste("americium"));
        putSpallation(map, isotope("americium/242"), waste("americium"));
        putSpallation(map, isotope("americium/241"), waste("americium"));
        putSpallation(map, isotope("plutonium/242"), waste("plutonium"));
        putSpallation(map, isotope("plutonium/241"), waste("plutonium"));
        putSpallation(map, isotope("plutonium/239"), waste("plutonium"));
        putSpallation(map, isotope("plutonium/238"), waste("plutonium"));
        putSpallation(map, isotope("neptunium/237"), waste("neptunium"));
        putSpallation(map, isotope("neptunium/236"), waste("neptunium"));
        putSpallation(map, isotope("uranium/238"), waste("uranium"));
        putSpallation(map, isotope("uranium/235"), waste("uranium"));
        putSpallation(map, isotope("uranium/234"), waste("uranium"));
        putSpallation(map, isotope("uranium/233"), waste("uranium"));
        putSpallation(map, dust("protactinium_231"), waste("protactinium"));
        putSpallation(map, dust("protactinium_233"), waste("protactinium"));
        putSpallation(map, ingot("thorium"), waste("thorium"));
        putSpallation(map, dust("radium"), waste("radium"));
        putSpallation(map, dust("polonium"), waste("polonium"));
        putSpallation(map, dust("bismuth"), waste("bismuth"));
        putSpallation(map, ingot("lead"), waste("lead"));
        putSpallation(map, ingot("mercury"), waste("mercury"));
        putSpallation(map, Items.GOLD_INGOT, waste("gold"));
        putSpallation(map, ingot("platinum"), waste("platinum"));
        putSpallation(map, ingot("iridium"), waste("iridium"));
        putSpallation(map, isotope("iridium_192"), waste("iridium"));
        putSpallation(map, ingot("osmium"), waste("osmium"));
        putSpallation(map, ingot("tungsten"), waste("tungsten"));
        putSpallation(map, ingot("hafnium"), waste("hafnium"));
        return map;
    }

    private static void putSpallation(Map<Item, Item> map, Item key, Item value) {
        if (key != null && value != null) {
            map.put(key, value);
        }
    }

    private static void proton(RecipeOutput out, String name, Item targetItem, long meanEnergyKeV,
                                long maximumEnergyKeV, long requiredWork, Item productItem, double crossSection,
                                long releasedEnergyKeV) {
        if (targetItem == null || productItem == null) return;
        ParticleIngredient particleInput = new ParticleIngredient(Set.of(rl("proton")), requiredWork,
                meanEnergyKeV, maximumEnergyKeV, 1.0);
        TargetChamberRecipe recipe = new TargetChamberRecipe(
                List.of(materialIngredient(targetItem)), List.of(), particleInput,
                List.of(ItemOutput.of(productItem, 1)), List.of(),
                List.of(new ParticleStack(rl("photon"), 1, 1, 1.0)),
                crossSection, releasedEnergyKeV, 0);
        out.accept(ResourceLocation.fromNamespaceAndPath(MODID, "target_chamber/proton_" + name), recipe, null);
    }

    private static void targetItem(RecipeOutput out, String id, String inputSpecies, Item targetItem,
                                    long minimumEnergyKeV, long maximumEnergyKeV, double crossSection,
                                    List<ParticleStack> particleOutputs, Item productItem, long releasedEnergyKeV) {
        target(out, id, inputSpecies, targetItem, null, 0, minimumEnergyKeV, maximumEnergyKeV, 1.0, crossSection,
                particleOutputs, productItem, null, 0, releasedEnergyKeV);
    }

    private static void targetItem(RecipeOutput out, String id, String inputSpecies, Item targetItem,
                                    long minimumEnergyKeV, long maximumEnergyKeV, double minimumFocus,
                                    double crossSection, List<ParticleStack> particleOutputs, Item productItem,
                                    long releasedEnergyKeV) {
        target(out, id, inputSpecies, targetItem, null, 0, minimumEnergyKeV, maximumEnergyKeV, minimumFocus,
                crossSection, particleOutputs, productItem, null, 0, releasedEnergyKeV);
    }

    private static void targetFluid(RecipeOutput out, String id, String inputSpecies, Fluid targetFluid,
                                     int targetFluidAmount, long minimumEnergyKeV, long maximumEnergyKeV,
                                     double crossSection, List<ParticleStack> particleOutputs, Item productItem,
                                     long releasedEnergyKeV) {
        target(out, id, inputSpecies, null, targetFluid, targetFluidAmount, minimumEnergyKeV, maximumEnergyKeV, 1.0,
                crossSection, particleOutputs, productItem, null, 0, releasedEnergyKeV);
    }

    private static void target(RecipeOutput out, String id, String inputSpecies, Item targetItem, Fluid targetFluid,
                                int targetFluidAmount, long minimumEnergyKeV, long maximumEnergyKeV,
                                double minimumFocus, double crossSection, List<ParticleStack> particleOutputs,
                                Item productItem, Fluid productFluid, int productFluidAmount,
                                long releasedEnergyKeV) {
        if (targetItem == null && targetFluid == null) return;
        List<SizedIngredient> itemInputs = targetItem == null ? List.of() : List.of(materialIngredient(targetItem));
        List<SizedFluidIngredient> fluidInputs = targetFluid == null ? List.of()
                : List.of(SizedFluidIngredient.of(targetFluid, targetFluidAmount));
        List<ItemOutput> itemOutputs = productItem == null ? List.of() : List.of(ItemOutput.of(productItem, 1));
        List<FluidOutput> fluidOutputs = productFluid == null ? List.of()
                : List.of(FluidOutput.of(productFluid, productFluidAmount));
        long requiredWork = Math.round(MOLE_AMOUNT / crossSection);
        ParticleIngredient particleInput = new ParticleIngredient(Set.of(rl(inputSpecies)), requiredWork,
                minimumEnergyKeV, maximumEnergyKeV, minimumFocus);
        TargetChamberRecipe recipe = new TargetChamberRecipe(itemInputs, fluidInputs, particleInput,
                itemOutputs, fluidOutputs, particleOutputs, crossSection, releasedEnergyKeV, 0);
        out.accept(ResourceLocation.fromNamespaceAndPath(MODID, "target_chamber/" + id), recipe, null);
    }

    private static SizedIngredient materialIngredient(Item item) {
        TagKey<Item> tag = materialTag(item);
        return tag == null ? SizedIngredient.of(item, 1) : SizedIngredient.of(tag, 1);
    }

    private static ParticleStack stack(String species) {
        return stack(1, species);
    }

    private static ParticleStack stack(int amount, String species) {
        return new ParticleStack(rl(species), amount, 0, 1.0);
    }
}
