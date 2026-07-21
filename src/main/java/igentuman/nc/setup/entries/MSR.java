package igentuman.nc.setup.entries;

import igentuman.nc.block_entity.fission.MsrControllerBE;
import igentuman.nc.config.Multiblocks;
import igentuman.nc.container.MsrControllerContainer;
import igentuman.nc.multiblock.MultiblockEntryBuilder;
import igentuman.nc.multiblock.fission.MoltenSaltReactorValidator;
import igentuman.nc.multiblock.fission.MsrCache;
import igentuman.nc.setup.ModEntries;
import igentuman.nc.util.SlotsLayout;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import static igentuman.nc.registration.ModEntryBuilder.addMultiblockBlock;
import static igentuman.nc.registration.ModEntryBuilder.addMultiblockController;
import static igentuman.nc.registration.ModEntryBuilder.addMultiblockPart;

public class MSR {

    private static boolean initialized = false;

    public static void msr() {
        if (initialized) return;
        initialized = true;

        addMultiblockController("msr_controller")
                .blockEntity(MsrControllerBE::new)
                .itemCap(1, 1)
                .fluidCap(1, 1, 0)
                .menu(MsrControllerContainer::new)
                .withLayout(SlotsLayout.create()
                        .addInput(106, 21)
                        .addInput(128, 22)
                        .addOutput(106, 51)
                        .addOutput(150, 22))
                .build();

        addMultiblockBlock("msr_fuel_cell", BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL).strength(3.5f, 6.0f).sound(SoundType.METAL)
                .requiresCorrectToolForDrops().noOcclusion());

        addMultiblockPart("msr_port");

        int min = Multiblocks.msrMinSize;
        int max = Multiblocks.msrMaxSize;
        MultiblockEntryBuilder.name("molten_salt_reactor")
                .controller(ModEntries.get("msr_controller"))
                .ports(ModEntries.get("msr_port"))
                .casing(
                        () -> ModEntries.get("fission_reactor_casing").block().get(),
                        () -> ModEntries.get("fission_reactor_glass").block().get())
                .interior(() -> ModEntries.get("msr_fuel_cell").block().get())
                .sizeRange(min, max, min, max, min, max)
                .validator(MoltenSaltReactorValidator::new)
                .cache(MsrCache::new)
                .build();
    }
}
