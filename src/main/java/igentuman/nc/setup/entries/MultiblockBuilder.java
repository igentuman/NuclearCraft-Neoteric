package igentuman.nc.setup.entries;

import igentuman.nc.block.MultiblockBuilderBlock;
import igentuman.nc.block_entity.MultiblockBuilderBE;
import igentuman.nc.container.MultiblockBuilderContainer;
import igentuman.nc.setup.ModEntries;

import static igentuman.nc.registration.ModEntryBuilder.add;

public class MultiblockBuilder extends ModEntries {

    private static boolean initialized = false;

    public static void multiblockBuilder() {
        if (initialized) return;
        initialized = true;

        add("multiblock_builder")
                .block(() -> new MultiblockBuilderBlock())
                .blockEntity(MultiblockBuilderBE::new)
                .menu(MultiblockBuilderContainer::new)
                .build();
    }
}
