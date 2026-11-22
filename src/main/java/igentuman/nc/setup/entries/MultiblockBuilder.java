package igentuman.nc.setup.entries;

import igentuman.nc.block.MultiblockBuilderBlock;
import igentuman.nc.block_entity.MultiblockBuilderBE;
import igentuman.nc.container.MultiblockBuilderContainer;
import igentuman.nc.setup.ModEntries;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import static igentuman.nc.registration.ModEntryBuilder.add;

public class MultiblockBuilder extends ModEntries {

    private static boolean initialized = false;

    public static void multiblockBuilder() {
        if (initialized) return;
        initialized = true;

        add("multiblock_builder")
                .block(MultiblockBuilderBlock::new, () -> BlockBehaviour.Properties.of()
                        .sound(SoundType.METAL).strength(4f).requiresCorrectToolForDrops())
                .blockEntity(MultiblockBuilderBE::new)
                .menu(MultiblockBuilderContainer::new)
                .build();
    }
}
