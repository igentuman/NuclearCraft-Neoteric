package igentuman.nc.setup.entries;

import igentuman.nc.block.fission.FissionDesignerBlock;
import igentuman.nc.block_entity.fission.FissionDesignerBE;
import igentuman.nc.container.FissionDesignerContainer;
import igentuman.nc.setup.ModEntries;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import static igentuman.nc.registration.ModEntryBuilder.add;

public class FissionDesigner extends ModEntries {

    private static boolean initialized = false;

    public static void fissionDesigner() {
        if (initialized) return;
        initialized = true;

        add("fission_reactor_designer")
                .block(FissionDesignerBlock::new, () -> BlockBehaviour.Properties.of()
                        .sound(SoundType.METAL).strength(4f).requiresCorrectToolForDrops())
                .blockEntity(FissionDesignerBE::new)
                .menu(FissionDesignerContainer::new)
                .build();
    }
}
