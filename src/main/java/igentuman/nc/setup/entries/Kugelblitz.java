package igentuman.nc.setup.entries;

import igentuman.nc.block.kugelblitz.BlackHoleBlock;
import igentuman.nc.block.kugelblitz.EXPLBlock;
import igentuman.nc.block.kugelblitz.EXPLProxyBlock;
import igentuman.nc.block.kugelblitz.KugelblitzEntityBlock;
import igentuman.nc.block_entity.kugelblitz.BlackHoleBE;
import igentuman.nc.block_entity.kugelblitz.ChamberPortBE;
import igentuman.nc.block_entity.kugelblitz.ChamberTerminalBE;
import igentuman.nc.block_entity.kugelblitz.EXPLBE;
import igentuman.nc.block_entity.kugelblitz.EXPLProxyBE;
import igentuman.nc.block_entity.kugelblitz.PhotonConcentratorBE;
import igentuman.nc.container.ChamberTerminalContainer;
import igentuman.nc.container.EXPLContainer;
import igentuman.nc.multiblock.MultiblockEntryBuilder;
import igentuman.nc.multiblock.kugelblitz.KugelblitzCache;
import igentuman.nc.multiblock.kugelblitz.KugelblitzValidator;
import igentuman.nc.recipe.kugelblitz.KugelblitzRecipes;
import igentuman.nc.registration.ModEntry;
import igentuman.nc.setup.ModEntries;
import igentuman.nc.util.SlotsLayout;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.List;

import static igentuman.nc.registration.ModEntryBuilder.add;
import static igentuman.nc.registration.ModEntryBuilder.addMultiblockBlock;
import static igentuman.nc.registration.ModEntryBuilder.addMultiblockController;
import static igentuman.nc.registration.ModEntryBuilder.addMultiblockPart;
import static igentuman.nc.setup.Registers.CREATIVE_MODE_TABS;

public class Kugelblitz extends ModEntries {

    private static boolean initialized = false;

    private static final List<String> TAB_BLOCKS = List.of(
            "chamber_terminal", "chamber_port", "neutronium_frame", "quantum_transformer",
            "quantum_flux_regulator", "event_horizon_stabilizer", "photon_concentrator", "expl");

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> KUGELBLITZ = CREATIVE_MODE_TABS.register("kugelblitz", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.nuclearcraft.kugelblitz"))
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> ModEntries.get("chamber_terminal").block().toStack())
            .displayItems((parameters, output) -> {
                for (String name : TAB_BLOCKS) {
                    ModEntry entry = ModEntries.get(name);
                    if (entry != null && entry.hasItem()) output.accept(entry.item());
                }
            }).build());

    public static void kugelblitz() {
        if (initialized) return;
        initialized = true;
        KugelblitzRecipes.init();

        addMultiblockController("chamber_terminal")
                .blockEntity(ChamberTerminalBE::new)
                .itemCap(1, 1)
                .fluidCap(1, 0, 0)
                .withEnergy(0, 100_000_000, 100_000_000)
                .withLayout(SlotsLayout.create().addInput(135, 80).addInput(189, 40).addOutput(189, 80))
                .menu(ChamberTerminalContainer::new)
                .build();

        addMultiblockPart("chamber_port", ChamberPortBE::new);

        addMultiblockBlock("neutronium_frame");
        addMultiblockBlock("quantum_transformer");
        addMultiblockBlock("quantum_flux_regulator", glassProps());
        addMultiblockBlock("event_horizon_stabilizer", glassProps());

        add("photon_concentrator")
                .block(name -> new KugelblitzEntityBlock(glassProps(), name, true))
                .blockEntity(PhotonConcentratorBE::new)
                .build();

        add("black_hole")
                .block(name -> new BlackHoleBlock(blackHoleProps(), name))
                .blockEntity(BlackHoleBE::new)
                .build();

        add("expl")
                .block(name -> new EXPLBlock(explProps(), name))
                .blockEntity(EXPLBE::new)
                .withEnergy(10_000_000, 0, 2_048_000_000)
                .menu(EXPLContainer::new)
                .build();

        add("expl_proxy")
                .block(name -> new EXPLProxyBlock(explProps(), name))
                .blockEntity(EXPLProxyBE::new)
                .withEnergy(0, 0, 0)
                .build();

        MultiblockEntryBuilder.name("kugelblitz_chamber")
                .controller(ModEntries.get("chamber_terminal"))
                .ports(ModEntries.get("chamber_port"))
                .casing(
                        () -> ModEntries.get("neutronium_frame").block().get(),
                        () -> ModEntries.get("quantum_transformer").block().get(),
                        () -> ModEntries.get("quantum_flux_regulator").block().get(),
                        () -> ModEntries.get("event_horizon_stabilizer").block().get(),
                        () -> ModEntries.get("photon_concentrator").block().get())
                .size(11, 11, 11)
                .validator(KugelblitzValidator::new)
                .cache(KugelblitzCache::new)
                .build();
    }

    private static BlockBehaviour.Properties glassProps() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL).strength(3.5f, 6.0f).sound(SoundType.GLASS)
                .requiresCorrectToolForDrops().noOcclusion();
    }

    private static BlockBehaviour.Properties blackHoleProps() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_BLACK).strength(4.0f).sound(SoundType.METAL)
                .requiresCorrectToolForDrops().noOcclusion().lightLevel(s -> 7);
    }

    private static BlockBehaviour.Properties explProps() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL).strength(3.5f, 6.0f).sound(SoundType.METAL)
                .requiresCorrectToolForDrops().noOcclusion();
    }
}
