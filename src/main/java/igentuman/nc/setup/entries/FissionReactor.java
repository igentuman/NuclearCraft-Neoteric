package igentuman.nc.setup.entries;

import igentuman.nc.block_entity.fission.FissionPortBE;
import igentuman.nc.block_entity.fission.FissionReactorControllerBE;
import igentuman.nc.multiblock.MultiblockEntryBuilder;
import igentuman.nc.multiblock.ValidationScheduler;
import igentuman.nc.multiblock.fission.ActiveCoolant;
import igentuman.nc.multiblock.fission.FissionReactorCache;
import igentuman.nc.multiblock.fission.FissionReactorValidator;
import igentuman.nc.multiblock.fission.HeatSinkDef;
import igentuman.nc.registration.HeatSinkEntry;
import igentuman.nc.registration.ModEntry;
import igentuman.nc.setup.ModEntries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import static igentuman.nc.registration.ModEntryBuilder.addMultiblockBlock;
import static igentuman.nc.registration.ModEntryBuilder.addMultiblockController;
import static igentuman.nc.registration.ModEntryBuilder.addMultiblockPart;
import static igentuman.nc.setup.Registers.CREATIVE_MODE_TABS;

/** Declares fission reactor blocks, heat sinks, creative tab, and the multiblock definition. */
public class FissionReactor extends ModEntries {

    private static final Pattern COND_SUFFIX = Pattern.compile("[><=^\\-]\\d+$");
    private static boolean initialized = false;

    private static final List<String> REACTOR_BLOCKS = List.of(
            "fission_reactor_controller",
            "fission_reactor_casing",
            "fission_reactor_glass",
            "fission_reactor_port",
            "fission_reactor_solid_fuel_cell",
            "fission_reactor_irradiation_chamber",
            "fission_reactor_pile-driver_irradiation_chamber",
            "msr_controller",
            "msr_fuel_cell",
            "msr_port",
            "fission_reactor_designer",
            "multiblock_builder",
            "fission_reactor_plan"
    );

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> FISSION_REACTOR = CREATIVE_MODE_TABS.register("fission_reactor", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.nuclearcraft.fission_reactor"))
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> ModEntries.get("fission_reactor_controller").block().toStack())
            .displayItems((parameters, output) -> {
                for (String name : REACTOR_BLOCKS) {
                    ModEntry entry = ModEntries.get(name);
                    if (entry != null && entry.hasItem()) output.accept(entry.item());
                }
                for (HeatSinkEntry hs : ModEntries.HEAT_SINKS.values()) {
                    if (hs.isEnabled()) output.accept(hs.block().get().asItem());
                }
            }).build());

    public static void fissionReactor() {
        if (initialized) return;
        initialized = true;

        addMultiblockController("fission_reactor_controller")
                .blockEntity(FissionReactorControllerBE::new)
                .itemCap(1, 1)
                .fluidCap(1 + ActiveCoolant.COUNT, 1, 0)
                .withEnergyOutput(100_000_000)
                .build();
        addMultiblockBlock("fission_reactor_casing");
        addMultiblockBlock("fission_reactor_glass", BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL).strength(3.5f, 6.0f).sound(SoundType.GLASS).requiresCorrectToolForDrops().noOcclusion());
        addMultiblockPart("fission_reactor_port", FissionPortBE::new);
        addMultiblockBlock("fission_reactor_solid_fuel_cell", BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL).strength(3.5f, 6.0f).requiresCorrectToolForDrops().noOcclusion());
        addMultiblockBlock("fission_reactor_irradiation_chamber");
        addMultiblockBlock("fission_reactor_pile-driver_irradiation_chamber");

        register(new HeatSinkDef("empty").heat(0));
        register(new HeatSinkDef("empty_active").heat(0));
        register(new HeatSinkDef("water").heat(60).rules("fission_reactor_solid_fuel_cell|#nuclearcraft:moderators"));
        register(new HeatSinkDef("redstone").heat(90).rules("fission_reactor_solid_fuel_cell"));
        register(new HeatSinkDef("quartz").heat(90).rules("#nuclearcraft:moderators"));
        register(new HeatSinkDef("gold").heat(120).rules("water_heat_sink", "redstone_heat_sink"));
        register(new HeatSinkDef("glowstone").heat(130).rules("#nuclearcraft:moderators>2"));
        register(new HeatSinkDef("lapis").heat(120).rules("fission_reactor_solid_fuel_cell", "#nuclearcraft:fission_reactor_casing"));
        register(new HeatSinkDef("diamond").heat(150).rules("water_heat_sink", "quartz_heat_sink"));
        register(new HeatSinkDef("emerald").heat(160).rules("fission_reactor_solid_fuel_cell", "#nuclearcraft:moderators"));
        register(new HeatSinkDef("copper").heat(80).rules("glowstone_heat_sink"));
        register(new HeatSinkDef("tin").heat(120).rules("lapis_heat_sink-2"));
        register(new HeatSinkDef("lead").heat(60).rules("iron_heat_sink"));
        register(new HeatSinkDef("iron").heat(80).rules("gold_heat_sink"));
        register(new HeatSinkDef("obsidian").heat(40).rules("glowstone_heat_sink-2"));
        register(new HeatSinkDef("prismarine").heat(115).rules("water_heat_sink"));
        register(new HeatSinkDef("magnesium").heat(110).rules("#nuclearcraft:fission_reactor_casing", "#nuclearcraft:moderators"));
        register(new HeatSinkDef("lithium").heat(130).rules("lead_heat_sink-2"));
        register(new HeatSinkDef("manganese").heat(150).rules("fission_reactor_solid_fuel_cell>2"));
        register(new HeatSinkDef("aluminum").heat(175).rules("quartz_heat_sink", "lapis_heat_sink"));
        register(new HeatSinkDef("silver").heat(170).rules("glowstone_heat_sink>2", "tin_heat_sink|fission_reactor_irradiation_chamber"));
        register(new HeatSinkDef("boron").heat(160).rules("quartz_heat_sink", "#nuclearcraft:fission_reactor_casing|#nuclearcraft:moderators"));
        register(new HeatSinkDef("arsenic").heat(135).rules("#nuclearcraft:moderators>3"));
        register(new HeatSinkDef("platinum").heat(180).rules("fission_reactor_irradiation_chamber", "#nuclearcraft:moderators"));
        register(new HeatSinkDef("cobalt").heat(75).rules("fission_reactor_irradiation_chamber"));
        register(new HeatSinkDef("fluorite").heat(160).rules("gold_heat_sink", "prismarine_heat_sink"));
        register(new HeatSinkDef("villiaumite").heat(180).rules("redstone_heat_sink", "end_stone_heat_sink"));
        register(new HeatSinkDef("carobbiite").heat(140).rules("end_stone_heat_sink", "fission_reactor_irradiation_chamber"));
        register(new HeatSinkDef("netherite").heat(150).rules("obsidian_heat_sink", "fission_reactor_irradiation_chamber"));
        register(new HeatSinkDef("nether_brick").heat(70).rules("obsidian_heat_sink"));
        register(new HeatSinkDef("slime").heat(145).rules("water_heat_sink", "lead_heat_sink"));
        register(new HeatSinkDef("end_stone").heat(40).rules("enderium_heat_sink"));
        register(new HeatSinkDef("purpur").heat(95).rules("#nuclearcraft:fission_reactor_casing", "iron_heat_sink"));
        register(new HeatSinkDef("enderium").heat(120).rules("#nuclearcraft:fission_reactor_casing^3"));
        register(new HeatSinkDef("cryotheum").heat(160).rules("fission_reactor_solid_fuel_cell>2"));
        register(new HeatSinkDef("liquid_nitrogen").heat(185).rules("copper_heat_sink", "purpur_heat_sink"));
        register(new HeatSinkDef("liquid_helium").heat(140).rules("redstone_heat_sink", "#nuclearcraft:fission_reactor_casing"));
        register(new HeatSinkDef("active_water").heat(250).rules("fission_reactor_solid_fuel_cell|#nuclearcraft:moderators"));
        register(new HeatSinkDef("active_redstone").heat(270).rules("fission_reactor_solid_fuel_cell"));
        register(new HeatSinkDef("active_cryotheum").heat(480).rules("fission_reactor_solid_fuel_cell>2"));
        register(new HeatSinkDef("active_enderium").heat(360).rules("#nuclearcraft:fission_reactor_casing^3"));
        register(new HeatSinkDef("active_liquid_nitrogen").heat(555).rules("copper_heat_sink", "purpur_heat_sink"));
        register(new HeatSinkDef("active_liquid_helium").heat(420).rules("redstone_heat_sink", "#nuclearcraft:fission_reactor_casing"));

        ModEntries.HS_SCHEDULE = computeSchedule();

        MultiblockEntryBuilder.name("fission_reactor")
                .controller(ModEntries.get("fission_reactor_controller"))
                .ports(ModEntries.get("fission_reactor_port"))
                .casing(
                        () -> ModEntries.get("fission_reactor_casing").block().get(),
                        () -> ModEntries.get("fission_reactor_glass").block().get())
                .interior(() -> ModEntries.get("fission_reactor_solid_fuel_cell").block().get())
                .sizeRange(3, 26, 3, 26, 3, 26)
                .validator(FissionReactorValidator::new)
                .cache(FissionReactorCache::new)
                .build();
    }

    public static HeatSinkEntry register(HeatSinkDef def) {
        return HeatSinkEntry.register(def);
    }

    public static void remove(String name) {
        HeatSinkEntry entry = ModEntries.HEAT_SINKS.get(name);
        if (entry != null) entry.disable();
    }

    public static void override(String name, Consumer<HeatSinkDef> mutator) {
        HeatSinkEntry entry = ModEntries.HEAT_SINKS.get(name);
        if (entry != null) entry.override(mutator);
    }

    private static List<String> computeSchedule() {
        ValidationScheduler scheduler = new ValidationScheduler();
        for (HeatSinkEntry entry : ModEntries.HEAT_SINKS.values()) {
            scheduler.addNode(entry.name);
            for (String rule : entry.def().rules) {
                for (String dep : extractHeatSinkDeps(rule)) {
                    if (ModEntries.HEAT_SINKS.containsKey(dep) && !dep.equals(entry.name)) {
                        scheduler.graphAddEdge(entry.name, dep);
                    }
                }
            }
        }
        return scheduler.getSchedule();
    }

    private static List<String> extractHeatSinkDeps(String rule) {
        List<String> deps = new ArrayList<>();
        for (String part : rule.split("\\|")) {
            String stripped = COND_SUFFIX.matcher(part.trim()).replaceAll("");
            if (stripped.startsWith("#") || stripped.contains(":")) continue;
            if (stripped.endsWith("_heat_sink")) {
                deps.add(stripped.substring(0, stripped.length() - "_heat_sink".length()));
            }
        }
        return deps;
    }
}
