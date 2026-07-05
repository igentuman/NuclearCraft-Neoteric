package igentuman.nc.setup.entries;

import igentuman.nc.setup.ModEntries;

public class Materials extends ModEntries {
    public static void materials() {
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

        blockOnly("kumanderite");
        blockOnly("uranium238");
        blockOnly("plutonium238");
        blockOnly("americium241");
        blockOnly("californium250");
        blockOnly("supercold_ice");

        alloy("steel", 0xFF997B74);
        alloy("zirconium", 0xFFC6C77F);
        alloy("beryllium", 0xFFD7DFC6);
        alloy("bronze", 0xFFC78C47);
        alloy("electrum", 0xFFA0A15D);
        alloy("aluminum", 0xFFABEBCB);

        ingotPlateDustFluid("tough_alloy", 0xFF181322);
        ingotPlateDustFluid("palladium", 0xFF181322);
        ingotPlateDustFluid("hard_carbon", 0xFF164C5F);
        ingotPlateDustFluid("thermoconducting", 0xFF515C3C);
        ingotPlateDustFluid("extreme", 0xFF70293C);
        ingotPlateDustFluid("manganese", 0xFF7284CC);
        ingotPlateDustFluid("sic_sic_cmc", 0xFF7A766C);
        ingotPlateDustFluid("hsla_steel", 0xFF8174B0);

        ingotDustFluid("tin_silver", 0xFFD3D3E9);
        ingotDustFluid("zircaloy", 0xFFD8D8D8);
        ingotDustFluid("zirconium_molybdenum", 0xFFB3B7BC);
        ingotDustFluid("manganese_oxide", 0xFF7E9D7E);
        ingotDustFluid("manganese_dioxide", 0xFF28211E);
        ingotDustFluid("silicon_carbide", 0xFF716E63);
        ingotDustFluid("shibuichi", 0xFFB9B3AE);

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
        mat("baratol", 0xFFB8B0A0).ingot().dust().fluid(molten(800)).build();
        mat("tnt", 0xFFCC2A1F).fluid(molten(600)).build();
        mat("netherite", DEFAULT_COLOR).dust().plate().build();

        ingotOnly("osmiridium", DEFAULT_COLOR);
        ingotOnly("nichrome", DEFAULT_COLOR);
        ingotOnly("niobium_tin", DEFAULT_COLOR);
        ingotOnly("niobium_titanium", DEFAULT_COLOR);
        ingotOnly("stainless_steel", DEFAULT_COLOR);
        ingotOnly("super_alloy", DEFAULT_COLOR);
        ingotOnly("tungsten_carbide", DEFAULT_COLOR);
        ingotOnly("neutronium", 0xFF181322);

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

        dustGem("boron_nitride");
        dustGem("carobbiite");
        dustGem("fluorite");
        dustGem("rhodochrosite");
        dustGem("villiaumite");
        mat("boron_arsenide", 0xFFDEDE7A).dust().gem().fluid(molten(500)).build();

        mat("silicon", DEFAULT_COLOR).gem().build();

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
}
