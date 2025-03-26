package igentuman.nc.multiblock.kugelblitz;

import igentuman.nc.block.entity.fission.FissionControllerBE;
import igentuman.nc.block.entity.kugelblitz.ChamberTerminalBE;
import igentuman.nc.multiblock.AbstractNCMultiblock;
import igentuman.nc.multiblock.MultiblockHandler;
import igentuman.nc.multiblock.ValidationResult;
import igentuman.nc.util.NCBlockPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import java.util.List;
import static igentuman.nc.multiblock.kugelblitz.KugelblitzRegistration.CASING_BLOCKS;
import static igentuman.nc.multiblock.kugelblitz.KugelblitzRegistration.KUGELBLITZ_BLOCKS;
import static igentuman.nc.util.TagUtil.getBlocksByTagKey;
import static net.minecraft.world.level.block.Blocks.AIR;

public class KugelblitzMultiblock extends AbstractNCMultiblock {

    private ChamberTerminalBE controllerBe;

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

    private ChamberTerminalBE controllerBE() {
        if (controllerBe == null) {
            controllerBe = (ChamberTerminalBE) controller().controllerBE();
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

    public KugelblitzMultiblock(ChamberTerminalBE be) {
        this(getBlocksByTagKey(CASING_BLOCKS.location().toString()), List.of(KUGELBLITZ_BLOCKS.get("black_hole").get(), AIR));
        id = "chamber_"+be.getBlockPos().toShortString();
        MultiblockHandler.addMultiblock(this);
        controller = new KugelblitzController(be);
    }

    @Override
    public void validateOuter()
    {
        super.validateOuter();
        validatePhotonConcentrators();
    }

    private void validatePhotonConcentrators() {
        BlockPos center = getCenter();
        BlockPos left = center.offset(-3, 0, 0);
        BlockPos right = center.offset(3, 0, 0);
        BlockPos top = center.offset(0, 0, -3);
        BlockPos bottom = center.offset(0, 0, 3);
        if (getLevel().getBlockState(left).getBlock() != KUGELBLITZ_BLOCKS.get("photon_concentrator").get() ||
                getLevel().getBlockState(right).getBlock() != KUGELBLITZ_BLOCKS.get("photon_concentrator").get() ||
                getLevel().getBlockState(top).getBlock() != KUGELBLITZ_BLOCKS.get("photon_concentrator").get() ||
                getLevel().getBlockState(bottom).getBlock() != KUGELBLITZ_BLOCKS.get("photon_concentrator").get()) {
            validationResult = ValidationResult.PHOTON_CONCENTRATOR;
        }
    }

    @Override
    public void invalidateStats() {

    }

    public BlockPos getCenter() {
        BlockPos pos = new BlockPos(getTopRightBlock());
        return switch (getFacing()) {
            case NORTH -> pos.offset(+3, -3, +3);
            case SOUTH -> pos.offset(-3, -3, -3);
            case WEST -> pos.offset(+3, -3, -3);
            case EAST -> pos.offset(-3, -3, +3);
            default -> pos;
        };
    }
}
