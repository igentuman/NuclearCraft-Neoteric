package igentuman.nc.setup.entries;

import igentuman.nc.block.fusion.ElectromagnetSlopeBlock;
import igentuman.nc.block_entity.fusion.FusionCoreProxyBE;
import igentuman.nc.block_entity.fusion.FusionPortBE;
import igentuman.nc.block_entity.fusion.FusionReactorControllerBE;
import igentuman.nc.multiblock.MultiblockEntryBuilder;
import igentuman.nc.multiblock.fusion.FusionReactorCache;
import igentuman.nc.multiblock.fusion.FusionReactorValidator;
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

import static igentuman.nc.registration.ModEntryBuilder.add;
import static igentuman.nc.registration.ModEntryBuilder.addMultiblockBlock;
import static igentuman.nc.registration.ModEntryBuilder.addMultiblockController;
import static igentuman.nc.registration.ModEntryBuilder.addMultiblockPart;
import static igentuman.nc.setup.Registers.CREATIVE_MODE_TABS;

public class FusionReactor extends ModEntries {

    private static boolean initialized = false;

    public static final List<String> MAGNET_TIERS = List.of(
            "basic", "magnesium_diboride", "niobium_tin", "niobium_titanium", "bscco");

    private static final int ENERGY_BUFFER = 2_048_000_000;

    private static final List<String> TAB_BLOCKS = buildTabList();

    private static List<String> buildTabList() {
        List<String> l = new ArrayList<>();
        l.add("fusion_reactor_core");
        l.add("fusion_reactor_casing");
        l.add("fusion_reactor_glass");
        l.add("fusion_reactor_port");
        l.add("fusion_reactor_connector");
        for (String t : MAGNET_TIERS) l.add(t + "_electromagnet");
        for (String t : MAGNET_TIERS) l.add(t + "_electromagnet_slope");
        for (String t : MAGNET_TIERS) l.add(t + "_rf_amplifier");
        return l;
    }

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> FUSION_REACTOR = CREATIVE_MODE_TABS.register("fusion_reactor", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.nuclearcraft.fusion_reactor"))
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> ModEntries.get("fusion_reactor_core").block().toStack())
            .displayItems((parameters, output) -> {
                for (String name : TAB_BLOCKS) {
                    ModEntry entry = ModEntries.get(name);
                    if (entry != null && entry.hasItem()) output.accept(entry.item());
                }
            }).build());

    public static void fusionReactor() {
        if (initialized) return;
        initialized = true;

        addMultiblockController("fusion_reactor_core")
                .blockEntity(FusionReactorControllerBE::new)
                .fluidCap(3, 5, 0)
                .withEnergy(1_000_000, 100_000_000, ENERGY_BUFFER)
                .build();

        addMultiblockBlock("fusion_reactor_casing");
        addMultiblockBlock("fusion_reactor_glass", glassProps());
        addMultiblockPart("fusion_reactor_port", FusionPortBE::new);
        addMultiblockBlock("fusion_reactor_connector");
        addMultiblockPart("fusion_reactor_core_proxy", coreProxyProps(), FusionCoreProxyBE::new);

        for (String t : MAGNET_TIERS) {
            addMultiblockBlock(t + "_electromagnet");
            add(t + "_electromagnet_slope").block(name -> new ElectromagnetSlopeBlock(slopeProps())).build();
            addMultiblockBlock(t + "_rf_amplifier");
        }

        MultiblockEntryBuilder.name("fusion_reactor")
                .controller(ModEntries.get("fusion_reactor_core"))
                .ports(ModEntries.get("fusion_reactor_port"), ModEntries.get("fusion_reactor_core_proxy"))
                .casing(
                        () -> ModEntries.get("fusion_reactor_casing").block().get(),
                        () -> ModEntries.get("fusion_reactor_glass").block().get(),
                        () -> ModEntries.get("fusion_reactor_connector").block().get())
                .sizeRange(3, 67, 3, 3, 3, 67)
                .validator(FusionReactorValidator::new)
                .cache(FusionReactorCache::new)
                .build();
    }

    private static BlockBehaviour.Properties glassProps() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL).strength(3.5f, 6.0f).sound(SoundType.GLASS)
                .requiresCorrectToolForDrops().noOcclusion();
    }

    private static BlockBehaviour.Properties slopeProps() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL).strength(3.5f, 6.0f)
                .requiresCorrectToolForDrops().noOcclusion();
    }

    private static BlockBehaviour.Properties coreProxyProps() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL).strength(3.5f, 6.0f)
                .requiresCorrectToolForDrops().noOcclusion();
    }
}
