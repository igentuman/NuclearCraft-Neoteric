package igentuman.nc.setup.entries;

import igentuman.nc.block.turbine.TurbineBladeBlock;
import igentuman.nc.block.turbine.TurbineCoilBlock;
import igentuman.nc.block.turbine.TurbineDummyBladeBlock;
import igentuman.nc.block.turbine.TurbineRotorBlock;
import igentuman.nc.block_entity.turbine.TurbineControllerBE;
import igentuman.nc.block_entity.turbine.TurbinePortBE;
import igentuman.nc.block_entity.turbine.TurbineRotorBE;
import igentuman.nc.config.Multiblocks;
import igentuman.nc.multiblock.MultiblockEntryBuilder;
import igentuman.nc.multiblock.ValidationScheduler;
import igentuman.nc.multiblock.turbine.BladeDef;
import igentuman.nc.multiblock.turbine.TurbineCache;
import igentuman.nc.multiblock.turbine.TurbineCoilDef;
import igentuman.nc.multiblock.turbine.TurbineValidator;
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
import java.util.regex.Pattern;

import static igentuman.nc.registration.ModEntryBuilder.add;
import static igentuman.nc.registration.ModEntryBuilder.addMultiblockBlock;
import static igentuman.nc.registration.ModEntryBuilder.addMultiblockController;
import static igentuman.nc.registration.ModEntryBuilder.addMultiblockPart;
import static igentuman.nc.setup.Registers.CREATIVE_MODE_TABS;

public class Turbine extends ModEntries {

    private static boolean initialized = false;

    private static final Pattern COND_SUFFIX = Pattern.compile("[><=^\\-]\\d+$");

    public static final List<String> BLADES = List.of(
            "turbine_basic_rotor_blade", "turbine_steel_rotor_blade",
            "turbine_extreme_rotor_blade", "turbine_sic_sic_cmc_rotor_blade");

    public static final List<String> COILS = List.of(
            "copper", "magnesium", "silver", "gold", "beryllium", "aluminum");

    private static final List<String> TAB_BLOCKS = buildTabList();

    private static List<String> buildTabList() {
        List<String> l = new ArrayList<>();
        l.add("turbine_controller");
        l.add("turbine_casing");
        l.add("turbine_glass");
        l.add("turbine_port");
        l.add("turbine_bearing");
        l.add("turbine_rotor_shaft");
        l.addAll(BLADES);
        for (String c : COILS) l.add("turbine_" + c + "_coil");
        return l;
    }

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TURBINE = CREATIVE_MODE_TABS.register("turbine", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.nuclearcraft.turbine"))
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> ModEntries.get("turbine_controller").block().toStack())
            .displayItems((parameters, output) -> {
                for (String name : TAB_BLOCKS) {
                    ModEntry entry = ModEntries.get(name);
                    if (entry != null && entry.hasItem()) output.accept(entry.item());
                }
            }).build());

    public static void turbine() {
        if (initialized) return;
        initialized = true;

        addMultiblockController("turbine_controller")
                .blockEntity(TurbineControllerBE::new)
                .fluidCap(1, 1, 0)
                .withEnergyOutput(100_000_000)
                .build();

        addMultiblockBlock("turbine_casing");
        addMultiblockBlock("turbine_glass", glassProps());
        addMultiblockPart("turbine_port", TurbinePortBE::new);
        addMultiblockBlock("turbine_bearing");

        add("turbine_rotor_shaft")
                .block(name -> new TurbineRotorBlock(rotorProps(), name))
                .blockEntity(TurbineRotorBE::new)
                .build();

        for (String blade : BLADES) {
            add(blade).block(name -> new TurbineBladeBlock(bladeProps())).build();
        }

        add("dummy_turbine_blade").block(name -> new TurbineDummyBladeBlock(bladeProps())).build();

        register(new TurbineCoilDef("copper").efficiency(110).rules("turbine_gold_coil"));
        register(new TurbineCoilDef("magnesium").efficiency(86).rules("turbine_bearing"));
        register(new TurbineCoilDef("silver").efficiency(112).rules("turbine_magnesium_coil|turbine_gold_coil"));
        register(new TurbineCoilDef("gold").efficiency(104).rules("turbine_beryllium_coil"));
        register(new TurbineCoilDef("beryllium").efficiency(90).rules("turbine_magnesium_coil"));
        register(new TurbineCoilDef("aluminum").efficiency(98)
                .rules("turbine_gold_coil|turbine_magnesium_coil|turbine_beryllium_coil|turbine_copper_coil"));

        for (String coil : COILS) {
            addMultiblockBlock("turbine_" + coil + "_coil",
                    () -> new TurbineCoilBlock(coilProps(), ModEntries.TURBINE_COILS.get(coil)));
        }

        ModEntries.COIL_SCHEDULE = computeSchedule();

        ModEntries.TURBINE_BLADES.put("turbine_basic_rotor_blade", new BladeDef("basic", 95, 120));
        ModEntries.TURBINE_BLADES.put("turbine_steel_rotor_blade", new BladeDef("steel", 100, 140));
        ModEntries.TURBINE_BLADES.put("turbine_extreme_rotor_blade", new BladeDef("extreme", 110, 160));
        ModEntries.TURBINE_BLADES.put("turbine_sic_sic_cmc_rotor_blade", new BladeDef("sic_sic_cmc", 125, 180));

        int min = Multiblocks.turbineMinSize;
        int max = Multiblocks.turbineMaxSize;
        MultiblockEntryBuilder.name("turbine")
                .controller(ModEntries.get("turbine_controller"))
                .ports(ModEntries.get("turbine_port"))
                .casing(
                        () -> ModEntries.get("turbine_casing").block().get(),
                        () -> ModEntries.get("turbine_glass").block().get(),
                        () -> ModEntries.get("turbine_bearing").block().get())
                .interior(() -> ModEntries.get("turbine_rotor_shaft").block().get())
                .sizeRange(min, max, min, max, min, max)
                .validator(TurbineValidator::new)
                .cache(TurbineCache::new)
                .build();
    }

    public static void register(TurbineCoilDef def) {
        ModEntries.TURBINE_COILS.putIfAbsent(def.name, def);
    }

    private static List<String> computeSchedule() {
        ValidationScheduler scheduler = new ValidationScheduler();
        for (TurbineCoilDef def : ModEntries.TURBINE_COILS.values()) {
            scheduler.addNode(def.name);
            for (String rule : def.rules) {
                for (String dep : extractCoilDeps(rule)) {
                    if (ModEntries.TURBINE_COILS.containsKey(dep) && !dep.equals(def.name)) {
                        scheduler.graphAddEdge(def.name, dep);
                    }
                }
            }
        }
        return scheduler.getSchedule();
    }

    private static List<String> extractCoilDeps(String rule) {
        List<String> deps = new ArrayList<>();
        for (String part : rule.split("\\|")) {
            String stripped = COND_SUFFIX.matcher(part.trim()).replaceAll("");
            if (stripped.startsWith("#") || stripped.contains(":")) continue;
            if (stripped.startsWith("turbine_") && stripped.endsWith("_coil")) {
                deps.add(stripped.substring("turbine_".length(), stripped.length() - "_coil".length()));
            }
        }
        return deps;
    }

    private static BlockBehaviour.Properties glassProps() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL).strength(3.5f, 6.0f).sound(SoundType.GLASS)
                .requiresCorrectToolForDrops().noOcclusion();
    }

    private static BlockBehaviour.Properties rotorProps() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL).strength(3.5f, 6.0f).sound(SoundType.METAL)
                .requiresCorrectToolForDrops().noOcclusion();
    }

    private static BlockBehaviour.Properties bladeProps() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL).strength(3.5f, 6.0f).sound(SoundType.METAL)
                .requiresCorrectToolForDrops().noOcclusion();
    }

    private static BlockBehaviour.Properties coilProps() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL).strength(3.5f, 6.0f).sound(SoundType.METAL)
                .requiresCorrectToolForDrops();
    }
}
