package igentuman.nc.multiblock.kugelblitz;

import igentuman.nc.block.entity.fission.FissionControllerBE;
import igentuman.nc.block.entity.kugelblitz.ChamberTerminalBE;
import igentuman.nc.multiblock.AbstractNCMultiblock;
import igentuman.nc.multiblock.MultiblockHandler;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import java.util.List;
import static igentuman.nc.multiblock.kugelblitz.KugelblitzRegistration.CASING_BLOCKS;
import static igentuman.nc.multiblock.kugelblitz.KugelblitzRegistration.KUGELBLITZ_BLOCKS;
import static igentuman.nc.util.TagUtil.getBlocksByTagKey;
import static net.minecraft.world.level.block.Blocks.AIR;

public class KugelblitzMultiblock extends AbstractNCMultiblock {

    private ChamberTerminalBE<?> controllerBe;

    public int maxHeight() {
        return 8;
    }
    public int minHeight() {
        return 7;
    }
    public int maxWidth() {
        return 8;
    }
    public int minWidth() {
        return 7;
    }
    public int maxDepth() {
        return 8;
    }
    public int minDepth() {
        return 7;
    }

    private ChamberTerminalBE<?> controllerBE() {
        if (controllerBe == null) {
            controllerBe = (ChamberTerminalBE<?>) controller().controllerBE();
        }
        return controllerBe;
    }

    @Override
    protected Direction getFacing() {
        return controllerBE().getFacing();
    }

    protected KugelblitzMultiblock(List<Block> validOuterBlocks, List<Block> validInnerBlocks) {
        super(validOuterBlocks, validInnerBlocks);
    }

    public KugelblitzMultiblock(ChamberTerminalBE<?> be) {
        this(getBlocksByTagKey(CASING_BLOCKS.location().toString()), List.of(KUGELBLITZ_BLOCKS.get("black_hole").get(), AIR));
        id = "chamber_"+be.getBlockPos().toShortString();
        MultiblockHandler.addMultiblock(this);
        controller = new KugelblitzController(be);
    }

    @Override
    protected void invalidateStats() {

    }
}
