package igentuman.nc.setup;

import igentuman.nc.registration.FluidDefinition;
import igentuman.nc.registration.IsotopeEntry;
import igentuman.nc.registration.ModEntry;
import igentuman.nc.registration.ModEntryBuilder;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import java.util.HashMap;

import static igentuman.nc.registration.ModEntryBuilder.add;
import static igentuman.nc.registration.ModEntryBuilder.addIsotope;
import static igentuman.nc.registration.ModEntryBuilder.addMetalOreMaterial;

public class ModEntries {
    public static final HashMap<String, ModEntry> ENTRIES = new HashMap<>();
    public static final HashMap<String, IsotopeEntry> ISOTOPES = new HashMap<>();
    public static BlockBehaviour.Properties COMMON_BLOCK_PROPS = BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.5f).requiresCorrectToolForDrops();

    private static final int DEFAULT_COLOR = 0xFFFFFFFF;

    public static void init() {
        materials();
        isotopes();
        fluids();
    }

    private static void materials() {
        // Full ore materials: ore + raw + ingot + nugget + plate + dust + storage block + molten fluid + worldgen.
        oreMetal("uranium", 0xFF476447);
        oreMetal("thorium", 0xFF2E2E2E);
        oreMetal("boron", 0xFF999999);
        oreMetal("silver", 0xFFA49CA6);
        oreMetal("lead", 0xFF778887);
        oreMetal("tin", 0xFFDFDFE8);
        oreMetal("zinc", 0xFFAFB0A6);
        oreMetal("magnesium", 0xFFF0D0ED);
        oreMetal("lithium", DEFAULT_COLOR);
        oreMetal("cobalt", 0xFF5A6691);
        oreMetal("platinum", 0xFF6C939B);

        // Storage-block-only materials (includes isotope storage blocks).
        blockOnly("kumanderite");
        blockOnly("uranium238");
        blockOnly("plutonium238");
        blockOnly("americium241");
        blockOnly("californium250");
        blockOnly("supercold_ice");

        // Full alloys: ingot + dust + nugget + plate + storage block + molten fluid.
        alloy("steel", 0xFF997B74);
        alloy("zirconium", 0xFFC6C77F);
        alloy("beryllium", 0xFFD7DFC6);
        alloy("bronze", 0xFFC78C47);
        alloy("electrum", 0xFFA0A15D);
        alloy("aluminum", 0xFFABEBCB);

        // ingot + plate + dust + molten fluid.
        ingotPlateDustFluid("tough_alloy", 0xFF181322);
        ingotPlateDustFluid("palladium", 0xFF181322);
        ingotPlateDustFluid("hard_carbon", 0xFF164C5F);
        ingotPlateDustFluid("thermoconducting", 0xFF515C3C);
        ingotPlateDustFluid("extreme", 0xFF70293C);
        ingotPlateDustFluid("manganese", 0xFF7284CC);
        ingotPlateDustFluid("sic_sic_cmc", 0xFF7A766C);
        ingotPlateDustFluid("hsla_steel", 0xFF8174B0);

        // ingot + dust + molten fluid.
        ingotDustFluid("tin_silver", 0xFFD3D3E9);
        ingotDustFluid("zircaloy", 0xFFD8D8D8);
        ingotDustFluid("zirconium_molybdenum", 0xFFB3B7BC);
        ingotDustFluid("manganese_oxide", 0xFF7E9D7E);
        ingotDustFluid("manganese_dioxide", 0xFF28211E);
        ingotDustFluid("silicon_carbide", 0xFF716E63);
        ingotDustFluid("shibuichi", 0xFFB9B3AE);

        // Misc alloy/metal combinations.
        mat("copper", 0xFFA86F32).dust().plate().fluid().build();
        mat("iron", 0xFF7D0707).dust().plate().fluid().build();
        mat("purpur", 0xFF7E0399).dust().fluid().build();
        mat("gold", 0xFFD4CD08).dust().fluid().build();
        mat("enderium", 0xFF00856C).dust().fluid().build();
        mat("lapis", 0xFF04128F).dust().fluid().build();
        mat("carbon_manganese", 0xFF7E0399).dust().fluid().ingot().build();
        mat("lead_platinum", 0xFF04118F).dust().fluid().ingot().build();
        mat("ferroboron", 0xFF543636).dust().ingot().fluid().plate().build();
        mat("magnesium_diboride", 0xFF26303D).ingot().fluid().build();
        mat("lithium_manganese_dioxide", 0xFF696969).ingot().dust().fluid().plate().build();
        mat("graphite", 0xFF292929).ingot().dust().storageBlock().plate().build();
        mat("pyrolitic_carbon", 0xFF292929).ingot().dust().build();
        mat("corium", 0xFF7C7C6F).storageBlock().fluid(molten(3000)).build();
        mat("baratol", 0xFFB8B0A0).ingot().dust().fluid(molten(800)).build();
        mat("tnt", 0xFFCC2A1F).fluid(molten(600)).build();
        mat("netherite", DEFAULT_COLOR).dust().plate().build();

        // ingot-only.
        ingotOnly("osmiridium", DEFAULT_COLOR);
        ingotOnly("nichrome", DEFAULT_COLOR);
        ingotOnly("niobium_tin", DEFAULT_COLOR);
        ingotOnly("niobium_titanium", DEFAULT_COLOR);
        ingotOnly("stainless_steel", DEFAULT_COLOR);
        ingotOnly("super_alloy", DEFAULT_COLOR);
        ingotOnly("tungsten_carbide", DEFAULT_COLOR);
        ingotOnly("neutronium", 0xFF181322);

        // dust + ingot.
        dustIngot("calcium");
        dustIngot("chromium");
        dustIngot("hafnium");
        dustIngot("iridium");
        dustIngot("niobium");
        dustIngot("osmium");
        dustIngot("potassium");
        dustIngot("sodium");
        dustIngot("strontium");
        dustIngot("titanium");
        dustIngot("tungsten");
        dustIngot("yttrium");

        // dust + molten fluid (radioactive dusts, salts and similar).
        dustFluid("ruthenium_106", 0xFF8174B0, 600);
        dustFluid("strontium_90", 0xFF8174B0, 600);
        dustFluid("promethium_147", 0xFF8174B0, 600);
        dustFluid("molybdenum", 0xFF8174B0, 600);
        dustFluid("caesium_137", 0xFF8174B0, 600);
        dustFluid("europium_155", 0xFF8174B0, 600);
        dustFluid("arsenic", 0xFFB4DE7A, 500);
        dustFluid("obsidian", 0xFFDEDE7A, 600);
        dustFluid("sulfur", 0xFFDEDE7A, 700);
        dustFluid("potassium_hydroxide", 0xFFDEDE7A, 500);
        dustFluid("sodium_hydroxide", 0xFFDEDE7A, 500);
        dustFluid("polonium", 0xFF15755B, 800);
        dustFluid("potassium_iodide", 0xFFFFFAFA, 100);
        dustFluid("barium_nitrate", 0xFFE6E6CC, 600);

        // dust + gem (and one dust + gem + fluid).
        dustGem("boron_nitride");
        dustGem("carobbiite");
        dustGem("fluorite");
        dustGem("rhodochrosite");
        dustGem("villiaumite");
        mat("boron_arsenide", 0xFFDEDE7A).dust().gem().fluid(molten(500)).build();

        // gem-only.
        mat("silicon", DEFAULT_COLOR).gem().build();

        // dust-only.
        dustOnly("bscco");
        dustOnly("erbium");
        dustOnly("ytterbium");
        dustOnly("germanium");
        dustOnly("terbium");
        dustOnly("samarium");
        dustOnly("neodymium");
        dustOnly("iodine");
        dustOnly("barium");
        dustOnly("bismuth");
        dustOnly("thallium");
        dustOnly("gadolinium");
        dustOnly("protactinium_231");
        dustOnly("protactinium_233");
        dustOnly("radium");
        dustOnly("tbp");
        dustOnly("coal");
        dustOnly("charcoal");
        dustOnly("diamond");
        dustOnly("emerald");
        dustOnly("end_stone");
        dustOnly("quartz");
        dustOnly("alugentum");
        dustOnly("borax");
        dustOnly("c_mn_blend");
        dustOnly("calcium_sulfate");
        dustOnly("crystal_binder");
        dustOnly("dimensional_blend");
        dustOnly("energetic_blend");
        dustOnly("irradiated_borax");
        dustOnly("potassium_fluoride");
        dustOnly("sodium_fluoride");
        dustOnly("yellowcake");
    }

    // ---------------------------------------------------------------------
    // Isotopes
    //
    // New entry type (see IsotopeEntry). Each isotope registers item-form variants (base + _za/_ox/_ni;
    // special families register the base form only) and carries the per-item radiation level. The
    // radiation system is wired in later; half-life / decay-product metadata fields exist on
    // IsotopeEntry but are left unset because the original mod tracks no such data.
    // ---------------------------------------------------------------------
    private static void isotopes() {
        addIsotope("americium/241", 0.02);
        addIsotope("copernicium/291", 0.32);
        addIsotope("americium/242", 0.07);
        addIsotope("americium/243", 0.0013);
        addIsotope("berkelium/247", 0.0075);
        addIsotope("berkelium/248", 0.003);
        addIsotope("boron/10", 0.001);
        addIsotope("boron/11", 0.007);
        addIsotope("californium/249", 0.002);
        addIsotope("californium/250", 0.076);
        addIsotope("californium/251", 0.005);
        addIsotope("californium/252", 0.38);
        addIsotope("curium/243", 0.034);
        addIsotope("curium/245", 0.0012);
        addIsotope("curium/246", 0.0021);
        addIsotope("curium/247", 0.0004);
        addIsotope("lithium/6", 0.002);
        addIsotope("lithium/7", 0.002);
        addIsotope("neptunium/236", 0.006);
        addIsotope("neptunium/237", 0.00047);
        addIsotope("plutonium/238", 0.011);
        addIsotope("plutonium/239", 0.05);
        addIsotope("plutonium/241", 0.071);
        addIsotope("plutonium/242", 0.002);
        addIsotope("thorium/230", 0.0009);
        addIsotope("thorium/232", 0.002);
        addIsotope("uranium/233", 0.003);
        addIsotope("uranium/234", 0.013);
        addIsotope("uranium/235", 0.02);
        addIsotope("uranium/238", 0.0002);
        addIsotope("xenorium/298", 1.2);
        addIsotope("quantite", 0.3);
        addIsotope("beryllium_7", 0.1);
        addIsotope("calcium_48", 0.1);
        addIsotope("cobalt_60", 0.1);
        addIsotope("iridium_192", 0.1);
        addIsotope("magnesium_24", 0.1);
        addIsotope("magnesium_26", 0.1);
        addIsotope("sodium_22", 0.1);
    }

    // ---------------------------------------------------------------------
    // Standalone fluids: acids and gases
    //
    // Registered as fluid-only material entries so they reuse the existing fluid registration,
    // datagen and creative-tab wiring. The explicit bare name (setName) keeps the original registry
    // ids (e.g. nitric_acid, steam) for recipe/tag compatibility. The color carries the original
    // ARGB tint. Acids: density 400, viscosity 1000. Gases: density -1000, viscosity 0.
    // ---------------------------------------------------------------------
    private static void fluids() {
        acid("hydrofluoric_acid", 0xCCFFEE99);
        acid("hydrochloric_acid", 0xBBEEEEFF);
        acid("boric_acid", 0xCCA0EFFF);
        acid("sulfuric_acid", 0xCCF8FFD3);
        acid("nitric_acid", 0xCC4F9EFF);
        acid("aqua_regia_acid", 0xCCFFBB99);

        gas("quantite_energy", 0x369CD192, 2372);
        gas("steam", 0xCC929292, 373);
        gas("high_pressure_steam", 0xCCBDBDBD, 383);
        gas("exhaust_steam", 0xCC7E7E7E, 292);
        gas("low_pressure_steam", 0xCCA8A8A8, 272);
        gas("low_quality_steam", 0xCC828282, 272);
        gas("argon", 0xCCFF75DD, 87);
        gas("neon", 0xCCFF9F7A, 27);
        gas("chlorine", 0xCCFFFF8F, 239);
        gas("nitric_oxide", 0xCCC9EEFF, 121);
        gas("nitrogen_dioxide", 0xCC782A10, 294);
        gas("hydrogen", 0xCCA0EFFF, 239);
        gas("helium", 0xCCC57B81, 293);
        gas("hot_helium", 0xAAC57B81, 693);
        gas("helium_3", 0xCCCBBB67, 293);
        gas("tritium", 0xCC5DBBD6, 20);
        gas("deuterium", 0xCC9E6FEF, 239);
        gas("oxygen", 0xCC7E8CC8, 293);
        gas("nitrogen", 0xCC7CC37B, 293);
        gas("fluorine", 0xCCD3C75D, 293);
        gas("carbon", 0xCC5C635A, 400);
        gas("carbon_dioxide", 0xCC5C635A, 394);
        gas("carbon_monoxide", 0xCC4C5649, 381);
        gas("ethene", 0xCCFFE4A3, 169);
        gas("fluoromethane", 0xCC424C05, 194);
        gas("ammonia", 0xCC7AC3A0, 240);
        gas("oxygen_difluoride", 0xCCEA1B01, 128);
        gas("diborane", 0xCC5DBBD6, 180);
        gas("sulfur_dioxide", 0xCCC3BC7A, 400);
        gas("sulfur_trioxide", 0xCCD3AE5D, 400);
        gas("radon", 0xFFFFFFFF, 260);
    }

    // ---- material builder helpers ----

    private static ModEntryBuilder mat(String name, int color) {
        return add(name).material(color);
    }

    private static void oreMetal(String name, int color) {
        addMetalOreMaterial(name, color).build();
    }

    private static void blockOnly(String name) {
        mat(name, DEFAULT_COLOR).storageBlock().build();
    }

    private static void alloy(String name, int color) {
        mat(name, color).ingot().dust().nugget().plate().storageBlock().fluid().build();
    }

    private static void ingotPlateDustFluid(String name, int color) {
        mat(name, color).ingot().plate().dust().fluid().build();
    }

    private static void ingotDustFluid(String name, int color) {
        mat(name, color).ingot().dust().fluid().build();
    }

    private static void ingotOnly(String name, int color) {
        mat(name, color).ingot().build();
    }

    private static void dustIngot(String name) {
        mat(name, DEFAULT_COLOR).dust().ingot().build();
    }

    private static void dustOnly(String name) {
        mat(name, DEFAULT_COLOR).dust().build();
    }

    private static void dustGem(String name) {
        mat(name, DEFAULT_COLOR).dust().gem().build();
    }

    private static void dustFluid(String name, int color, int temperature) {
        mat(name, color).dust().fluid(molten(temperature)).build();
    }

    private static void acid(String name, int color) {
        mat(name, color).fluid(FluidDefinition.acid().setName(name)).build();
    }

    private static void gas(String name, int color, int temperature) {
        mat(name, color).fluid(FluidDefinition.gas(temperature).setName(name)).build();
    }

    /** Molten-metal fluid with a non-default temperature. */
    private static FluidDefinition molten(int temperature) {
        return FluidDefinition.metal().setTemperature(temperature);
    }

    public static ModEntry get(String name) {
        return ENTRIES.getOrDefault(name, null);
    }

    public static boolean isEnabled(String name) {
        ModEntry entry = get(name);
        return entry == null || entry.isEnabled();
    }
}
