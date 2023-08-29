package igentuman.nc.content.materials;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static igentuman.nc.handler.config.CommonConfig.MATERIAL_PRODUCTS;


public class Materials extends MaterialsManager {

    public final static  String uranium = "uranium";
    public final static  String thorium = "thorium";
    public final static  String boron = "boron";
    public final static  String silver = "silver";
    public final static  String lead = "lead";
    public final static  String lead_platinum = "lead_platinum";
    public final static  String thallium = "thallium";
    public final static  String tin = "tin";
    public final static  String zinc = "zinc";
    public final static  String magnesium = "magnesium";
    public final static  String lithium = "lithium";
    public final static  String cobalt = "cobalt";
    public final static  String platinum = "platinum";
    public final static  String tough_alloy = "tough_alloy";
    public final static  String copper = "copper";
    public final static  String iron = "iron";
    public final static  String purpur = "purpur";
    public final static  String gold = "gold";
    public final static  String ferroboron = "ferroboron";
    public final static  String enderium = "enderium";
    public final static  String lapis = "lapis";
    public final static  String hard_carbon = "hard_carbon";
    public final static  String tin_silver = "tin_silver";
    public final static  String steel = "steel";
    public final static  String thermoconducting = "thermoconducting";
    public final static  String zircaloy = "zircaloy";
    public final static  String zirconium = "zirconium";
    public final static  String palladium = "palladium";
    public final static  String zirconium_molybdenum = "zirconium_molybdenum";
    public final static  String extreme = "extreme";
    public final static  String manganese = "manganese";
    public final static  String magnesium_diboride = "magnesium_diboride";
    public final static  String manganese_oxide = "manganese_oxide";
    public final static  String manganese_dioxide = "manganese_dioxide";
    public final static  String sic_sic_cmc = "sic_sic_cmc";
    public final static  String lithium_manganese_dioxide = "lithium_manganese_dioxide";
    public final static  String silicon_carbide = "silicon_carbide";
    public final static  String shibuichi = "shibuichi";
    public final static  String beryllium = "beryllium";
    public final static  String netherite = "netherite";
    public final static  String bronze = "bronze";
    public final static  String corium = "corium";
    public final static  String electrum = "electrum";
    public final static  String aluminum = "aluminum";
    public final static  String graphite = "graphite";
    public final static  String pyrolitic_carbon = "pyrolitic_carbon";
    public final static  String hsla_steel = "hsla_steel";
    public final static  String bismuth = "bismuth";
    public final static  String gadolinium = "gadolinium";
    public final static  String caesium_137 = "caesium_137";
    public final static  String europium_155 = "europium_155";
    public final static  String molybdenum = "molybdenum";
    public final static  String polonium = "polonium";
    public final static  String promethium_147 = "promethium_147";
    public final static  String protactinium_233 = "protactinium_233";
    public final static  String radium = "radium";
    public final static  String ruthenium_106 = "ruthenium_106";
    public final static  String strontium_90 = "strontium_90";
    public final static  String tbp = "tbp";
    public final static  String arsenic = "arsenic";
    public final static  String boron_nitride = "boron_nitride";
    public final static  String boron_arsenide = "boron_arsenide";
    public final static  String carobbiite = "carobbiite";
    public final static  String coal = "coal";
    public final static  String charcoal = "charcoal";
    public final static  String diamond = "diamond";
    public final static  String emerald = "emerald";
    public final static  String end_stone = "end_stone";
    public final static  String fluorite = "fluorite";
    public final static  String obsidian = "obsidian";
    public final static  String quartz = "quartz";
    public final static  String barium = "barium";
    public final static  String rhodochrosite = "rhodochrosite";
    public final static  String sulfur = "sulfur";
    public final static  String villiaumite = "villiaumite";
    public final static  String alugentum = "alugentum";
    public final static  String borax = "borax";

    public final static String yellowcake = "yellowcake";
    public final static  String neodymium = "neodymium";
    public final static  String c_mn_blend = "c_mn_blend";
    public final static  String calcium_sulfate = "calcium_sulfate";
    public final static  String crystal_binder = "crystal_binder";
    public final static  String dimensional_blend = "dimensional_blend";
    public final static  String energetic_blend = "energetic_blend";
    public final static  String irradiated_borax = "irradiated_borax";
    public final static  String potassium_fluoride = "potassium_fluoride";
    public final static  String potassium_hydroxide = "potassium_hydroxide";
    public final static  String sodium_fluoride = "sodium_fluoride";
    public final static  String sodium_hydroxide = "sodium_hydroxide";
    public final static  String carbon_manganese = "carbon_manganese";
    public final static String silicon = "silicon";
    public final static String americium241 = "americium/241";
    public final static String americium242 = "americium/242";
    public final static String americium243 = "americium/243";
    public final static String berkelium247 = "berkelium/247";
    public final static String berkelium248 = "berkelium/248";
    public final static String boron10 = "boron/10";
    public final static String boron11 = "boron/11";
    public final static String californium249 = "californium/249";
    public final static String californium250 = "californium/250";
    public final static String californium251 = "californium/251";
    public final static String californium252 = "californium/252";
    public final static String curium243 = "curium/243";
    public final static String curium245 = "curium/245";
    public final static String curium246 = "curium/246";
    public final static String curium247 = "curium/247";
    public final static String lithium6 = "lithium/6";
    public final static String lithium7 = "lithium/7";
    public final static String neptunium236 = "neptunium/236";
    public final static String neptunium237 = "neptunium/237";
    public final static String plutonium238 = "plutonium/238";
    public final static String plutonium239 = "plutonium/239";
    public final static String plutonium241 = "plutonium/241";
    public final static String plutonium242 = "plutonium/242";
    public final static String thorium230 = "thorium/230";
    public final static String thorium232 = "thorium/232";
    public final static String uranium233 = "uranium/233";
    public final static String uranium235 = "uranium/235";
    public final static String uranium238 = "uranium/238";
    public final static String supercold_ice = "supercold_ice";

    public static HashMap<String, Double> isotopes = new HashMap<>();
    public final static String potassium_iodide = "potassium_iodide";
    public final static String iodine = "iodine";

    public static HashMap<String, NCMaterial> all()
    {
        if(all == null) {
            all = new HashMap<>();
            //ores and all basic stuff by default
            all.put(uranium, NCMaterial.ore(uranium).color(0xFF476447));
            all.put(thorium, NCMaterial.ore(thorium).color(0xFF2E2E2E));
            all.put(boron, NCMaterial.ore(boron).color(0xFF999999));
            all.put(silver, NCMaterial.ore(silver).color(0xFFA49CA6));
            all.put(lead, NCMaterial.ore(lead).ores(true, false, false, false).color(0xFF778887));
            all.put(tin, NCMaterial.ore(tin).ores(true, false, false, false).color(0xFFDFDFE8));
            all.put(zinc, NCMaterial.ore(zinc).ores(true, false, false, false).color(0xFFAFB0A6));
            all.put(magnesium, NCMaterial.ore(magnesium).color(0xFFF0D0ED));
            all.put(lithium, NCMaterial.ore(lithium));
            all.put(cobalt, NCMaterial.ore(cobalt).color(0xFF5A6691));
            all.put(platinum, NCMaterial.ore(platinum).ores(false, true, false, false).color(0xFF6C939B));

            all.put("uranium238", NCMaterial.get("uranium238").define("block"));
            all.put("plutonium238", NCMaterial.get("plutonium238").define("block"));
            all.put("americium241", NCMaterial.get("americium241").define("block"));
            all.put("californium250", NCMaterial.get("californium250").define("block"));

            //ingots, nuggets, dusts...
            all.put(netherite, NCMaterial.get(netherite).define("dust", "plate"));
            all.put(supercold_ice, NCMaterial.get(supercold_ice).define("block"));
            all.put(tough_alloy, NCMaterial.get(tough_alloy).define("ingot", "plate", "dust", "fluid").color(0xFF181322));
            all.put(palladium, NCMaterial.get(palladium).define("ingot", "plate", "dust", "fluid").color(0xFF181322));
            all.put(copper, NCMaterial.get(copper).define("dust", "plate", "fluid").color(0xFFA86F32));
            all.put(iron, NCMaterial.get(iron).define("dust", "plate", "fluid").color(0xFF7D0707));
            all.put(purpur, NCMaterial.get(purpur).define("dust", "fluid").color(0xFF7E0399));
            all.put(carbon_manganese, NCMaterial.get(carbon_manganese).define("dust", "fluid", "ingot").color(0xFF7E0399));
            all.put(gold, NCMaterial.get(gold).define("dust", "fluid").color(0xFFD4CD08));
            all.put(ferroboron, NCMaterial.get(ferroboron).define("dust","ingot", "fluid", "plate").color(0xFF543636));
            all.put(enderium, NCMaterial.get(enderium).define("dust", "fluid").color(0xFF00856C));
            all.put(lapis, NCMaterial.get(lapis).define("dust", "fluid").color(0xFF04128F));
            all.put(lead_platinum, NCMaterial.get(lead_platinum).define("dust", "fluid", "ingot").color(0xFF04118F));
            all.put(hard_carbon, NCMaterial.get(hard_carbon).define("ingot", "plate", "dust", "fluid").color(0xFF164C5F));
            all.put(tin_silver, NCMaterial.get(tin_silver).define("ingot", "dust", "fluid").color(0xFFD3D3E9));
            all.put(steel, NCMaterial.alloy(steel).color(0xFF997B74));
            all.put(thermoconducting, NCMaterial.alloy(thermoconducting).define("ingot", "plate", "dust", "fluid").color(0xFF515C3C));
            all.put(zircaloy, NCMaterial.alloy(zircaloy).define("ingot", "dust", "fluid").color(0xFFD8D8D8));
            all.put(zirconium, NCMaterial.alloy(zirconium).color(0xFFC6C77F));
            all.put(zirconium_molybdenum, NCMaterial.alloy(zirconium_molybdenum).define("ingot", "dust", "fluid").color(0xFFB3B7BC));
            all.put(extreme, NCMaterial.alloy(extreme).define("ingot", "plate", "dust", "fluid").color(0xFF70293C));
            all.put(manganese, NCMaterial.alloy(manganese).define("ingot", "plate", "dust", "fluid").color(0xFF7284CC));
            all.put(magnesium_diboride, NCMaterial.alloy(magnesium_diboride).define("ingot", "fluid").color(0xFF26303D));
            all.put(manganese_oxide, NCMaterial.alloy(manganese_oxide).define("ingot", "dust", "fluid").color(0xFF7E9D7E));
            all.put(manganese_dioxide, NCMaterial.alloy(manganese_dioxide).define("ingot", "dust", "fluid").color(0xFF28211E));
            all.put(sic_sic_cmc, NCMaterial.alloy(sic_sic_cmc).define("ingot", "plate", "dust", "fluid").color(0xFF7A766C));
            all.put(lithium_manganese_dioxide, NCMaterial.alloy(lithium_manganese_dioxide).define("ingot", "dust", "fluid", "plate").color(0xFF696969));
            all.put(silicon_carbide, NCMaterial.alloy(silicon_carbide).define("ingot", "dust", "fluid").color(0xFF716E63));
            all.put(shibuichi, NCMaterial.alloy(shibuichi).define("ingot", "dust", "fluid").color(0xFFB9B3AE));
            all.put(beryllium, NCMaterial.alloy(beryllium).color(0xFFD7DFC6));
            all.put(bronze, NCMaterial.alloy(bronze).color(0xFFC78C47));
            all.put(corium, NCMaterial.alloy(corium).define("fluid").fluid(true, 3000).color(0xFF7C7C6F));
            all.put(electrum, NCMaterial.alloy(electrum).color(0xFFA0A15D));
            all.put(aluminum, NCMaterial.alloy(aluminum).color(0xFFABEBCB));
            all.put(graphite, NCMaterial.get(graphite).define("ingot", "dust", "block", "plate").color(0xFF292929));
            all.put(pyrolitic_carbon, NCMaterial.get(pyrolitic_carbon).define("ingot", "dust").color(0xFF292929));
            all.put(hsla_steel, NCMaterial.alloy(hsla_steel).define("ingot", "plate", "dust", "fluid").color(0xFF8174B0));
            all.put(ruthenium_106, NCMaterial.dust(ruthenium_106).fluid(true, 600).color(0xFF8174B0));
            all.put(strontium_90, NCMaterial.dust(strontium_90).fluid(true, 600).color(0xFF8174B0));
            all.put(promethium_147, NCMaterial.dust(promethium_147).fluid(true, 600).color(0xFF8174B0));
            all.put(molybdenum, NCMaterial.dust(molybdenum).fluid(true, 600).color(0xFF8174B0));
            all.put(caesium_137, NCMaterial.dust(caesium_137).fluid(true, 600).color(0xFF8174B0));
            all.put(arsenic, NCMaterial.dust(arsenic).fluid(true, 500).color(0xFFDEDE7A));
            all.put(boron_arsenide, NCMaterial.dust(boron_arsenide).with("gem").fluid(true, 500).color(0xFFDEDE7A));
            all.put(obsidian, NCMaterial.dust(obsidian).fluid(true, 600).color(0xFFDEDE7A));
            all.put(sulfur, NCMaterial.dust(sulfur).fluid(true, 700).color(0xFFDEDE7A));
            all.put(potassium_hydroxide, NCMaterial.dust(potassium_hydroxide).fluid(true, 500).color(0xFFDEDE7A));
            all.put(sodium_hydroxide, NCMaterial.dust(sodium_hydroxide).fluid(true, 500).color(0xFFDEDE7A));
            all.put(europium_155, NCMaterial.dust(europium_155).fluid(true, 600).color(0xFF8174B0));

            //dusts
            all.put(neodymium, NCMaterial.dust(neodymium));
            all.put(iodine, NCMaterial.dust(iodine));
            all.put(potassium_iodide, NCMaterial.dust(potassium_iodide).fluid(true, 100).color(0xFFFFFAFA));
            all.put(barium, NCMaterial.dust(barium));
            all.put(bismuth, NCMaterial.dust(bismuth));
            all.put(thallium, NCMaterial.dust(thallium));
            all.put(gadolinium, NCMaterial.dust(gadolinium));
            all.put(polonium, NCMaterial.dust(polonium));
            all.put(protactinium_233, NCMaterial.dust(protactinium_233));
            all.put(radium, NCMaterial.dust(radium));
            all.put(tbp, NCMaterial.dust(tbp));
            all.put(boron_nitride, NCMaterial.dust(boron_nitride).with("gem"));
            all.put(silicon, NCMaterial.gem(silicon));
            all.put(carobbiite, NCMaterial.dust(carobbiite).with("gem"));
            all.put(coal, NCMaterial.dust(coal));
            all.put(charcoal, NCMaterial.dust(charcoal));
            all.put(diamond, NCMaterial.dust(diamond));
            all.put(emerald, NCMaterial.dust(emerald));
            all.put(end_stone, NCMaterial.dust(end_stone));
            all.put(fluorite, NCMaterial.dust(fluorite).with("gem"));
            all.put(quartz, NCMaterial.dust(quartz));
            all.put(rhodochrosite, NCMaterial.dust(rhodochrosite).with("gem"));
            all.put(villiaumite, NCMaterial.dust(villiaumite).with("gem"));
            all.put(alugentum, NCMaterial.dust(alugentum));
            all.put(borax, NCMaterial.dust(borax));
            all.put(c_mn_blend, NCMaterial.dust(c_mn_blend));
            all.put(calcium_sulfate, NCMaterial.dust(calcium_sulfate));
            all.put(crystal_binder, NCMaterial.dust(crystal_binder));
            all.put(dimensional_blend, NCMaterial.dust(dimensional_blend));
            all.put(energetic_blend, NCMaterial.dust(energetic_blend));
            all.put(irradiated_borax, NCMaterial.dust(irradiated_borax));
            all.put(potassium_fluoride, NCMaterial.dust(potassium_fluoride));
            all.put(sodium_fluoride, NCMaterial.dust(sodium_fluoride));
            all.put(yellowcake, NCMaterial.dust(yellowcake));

        }
        return all;
    }

    public static List<String> isotopes()
    {
        if(!isotopes.isEmpty()) return new ArrayList<>(isotopes.keySet());
        isotopes.put(americium241, 0.002);
        isotopes.put(americium242, 0.007);
        isotopes.put(americium243, 0.00013);
        isotopes.put(berkelium247, 0.00075);
        isotopes.put(berkelium248, 0.003);
        isotopes.put(boron10, 0.001);
        isotopes.put(boron11, 0.0007);
        isotopes.put(californium249, 0.002);
        isotopes.put(californium250, 0.076);
        isotopes.put(californium251, 0.001);
        isotopes.put(californium252, 0.38);
        isotopes.put(curium243, 0.034);
        isotopes.put(curium245, 0.00012);
        isotopes.put(curium246, 0.00021);
        isotopes.put(curium247, 0.0004);
        isotopes.put(lithium6, 0.0002);
        isotopes.put(lithium7, 0.0002);
        isotopes.put(neptunium236, 0.00006);
        isotopes.put(neptunium237, 0.00047);
        isotopes.put(plutonium238, 0.011);
        isotopes.put(plutonium239, 0.0005);
        isotopes.put(plutonium241, 0.071);
        isotopes.put(plutonium242, 0.00002);
        isotopes.put(thorium230, 0.0001);
        isotopes.put(thorium232, 0.0002);
        isotopes.put(uranium233, 0.0003);
        isotopes.put(uranium235, 0.002);
        isotopes.put(uranium238, 0.000002);
        return isotopes();
    }

    public static List<String> slurries() {
        return MATERIAL_PRODUCTS.SLURRIES.get();
    }
}
