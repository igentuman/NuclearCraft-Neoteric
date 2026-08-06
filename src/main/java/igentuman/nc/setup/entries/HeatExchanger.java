package igentuman.nc.setup.entries;

import igentuman.nc.block_entity.heat_exchanger.HeatExchangerColdPortBE;
import igentuman.nc.block_entity.heat_exchanger.HeatExchangerControllerBE;
import igentuman.nc.block_entity.heat_exchanger.HeatExchangerHotPortBE;
import igentuman.nc.config.Multiblocks;
import igentuman.nc.multiblock.MultiblockEntryBuilder;
import igentuman.nc.multiblock.heat_exchanger.HeatExchangerCache;
import igentuman.nc.multiblock.heat_exchanger.HeatExchangerValidator;
import igentuman.nc.registration.ModEntry;
import igentuman.nc.setup.ModEntries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.List;

import static igentuman.nc.registration.ModEntryBuilder.addMultiblockBlock;
import static igentuman.nc.registration.ModEntryBuilder.addMultiblockController;
import static igentuman.nc.registration.ModEntryBuilder.addMultiblockPart;
import static igentuman.nc.setup.Registers.CREATIVE_MODE_TABS;

public class HeatExchanger {

    private static boolean initialized = false;

    public static final List<String> TAB_BLOCKS = List.of(
            "heat_exchanger_controller",
            "heat_exchanger_casing",
            "heat_exchanger",
            "heat_exchanger_radiator",
            "heat_exchanger_hot_coolant_port",
            "heat_exchanger_cold_coolant_port");

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> HEAT_EXCHANGER = CREATIVE_MODE_TABS.register("heat_exchanger", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.nuclearcraft.heat_exchanger"))
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> ModEntries.get("heat_exchanger_controller").block().toStack())
            .displayItems((parameters, output) -> {
                for (String name : TAB_BLOCKS) {
                    ModEntry entry = ModEntries.get(name);
                    if (entry != null && entry.hasItem()) output.accept(entry.item());
                }
            }).build());

    public static void heatExchanger() {
        if (initialized) return;
        initialized = true;

        addMultiblockController("heat_exchanger_controller")
                .blockEntity(HeatExchangerControllerBE::new)
                .fluidCap(2, 2, 0)
                .withEnergyInput(Multiblocks.hxEnergyCapacity)
                .build();

        addMultiblockBlock("heat_exchanger_casing");
        addMultiblockBlock("heat_exchanger");
        addMultiblockBlock("heat_exchanger_radiator");
        addMultiblockPart("heat_exchanger_hot_coolant_port", HeatExchangerHotPortBE::new);
        addMultiblockPart("heat_exchanger_cold_coolant_port", HeatExchangerColdPortBE::new);

        int min = Multiblocks.hxMinSize;
        int max = Multiblocks.hxMaxSize;
        MultiblockEntryBuilder.name("heat_exchanger")
                .controller(ModEntries.get("heat_exchanger_controller"))
                .ports(ModEntries.get("heat_exchanger_hot_coolant_port"),
                        ModEntries.get("heat_exchanger_cold_coolant_port"))
                .casing(() -> ModEntries.get("heat_exchanger_casing").block().get(),
                        () -> ModEntries.get("heat_exchanger_radiator").block().get())
                .interior(() -> ModEntries.get("heat_exchanger").block().get())
                .sizeRange(min, max, min, max, min, max)
                .validator(HeatExchangerValidator::new)
                .cache(HeatExchangerCache::new)
                .build();
    }
}
