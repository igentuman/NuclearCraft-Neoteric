package igentuman.nc.multiblock.kugelblitz;

import igentuman.api.platform.NCLevels;
import igentuman.nc.block.kugelblitz.entity.BlackHoleBE;
import igentuman.nc.block.kugelblitz.entity.ChamberTerminalBE;
import igentuman.nc.block.kugelblitz.entity.PhotonConcentratorBE;
import igentuman.nc.multiblock.AbstractMultiblock;
import igentuman.nc.multiblock.MultiblockHandler;
import igentuman.nc.multiblock.ValidationResult;
import igentuman.nc.util.BlockPosInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static igentuman.nc.NuclearCraft.debugLog;
import static igentuman.nc.block.kugelblitz.entity.BlackHoleBE.MIN_MASS;
import static igentuman.nc.multiblock.kugelblitz.KugelblitzRegistration.CASING_BLOCKS;
import static igentuman.nc.multiblock.kugelblitz.KugelblitzRegistration.KUGELBLITZ_BLOCKS;
import static net.minecraft.world.level.block.Blocks.AIR;

public class KugelblitzMultiblock extends AbstractMultiblock {

    protected ChamberTerminalBE controllerBe;
    protected final HashSet<Block> validCornerBlocks;
    public BlackHoleBE blackHole;

    protected BlockPos centerBlockPos;
    protected int transformers = 0;
    protected int fluxRegulators = 0;
    protected int stabilizers = 0;
    public boolean initialized = false;

    public int maxHeight() {
        return 9;
    }
    public int minHeight() {
        return 9;
    }
    public int maxWidth() {
        return 9;
    }
    public int minWidth() {
        return 9;
    }
    public int maxDepth() {
        return 9;
    }
    public int minDepth() {
        return 9;
    }

    protected ChamberTerminalBE controllerBE() {
        if (controllerBe == null) {
            controllerBe = (ChamberTerminalBE) controller().controllerBE();
        }
        return controllerBe;
    }

    public BlockEntity getBlackHole() {
        if(getCenter() == null || getCenter().equals(BlockPos.ZERO)) return null;
        return NCLevels.getExistingBlockEntity(getLevel(), getCenter());
    }

    @Override
    protected Direction getControllerDirection() {
        return controllerBE().getFacing();
    }

    public KugelblitzMultiblock(ChamberTerminalBE be) {
        super(CASING_BLOCKS, null,
                null, new HashSet<>(List.of(KUGELBLITZ_BLOCKS.get("black_hole").get(), AIR)),
                new KugelblitzController(be));
        id = "chamber_"+be.getBlockPos().toShortString();
        MultiblockHandler.get(be.getLevel().dimension()).addMultiblock(this);
        validCornerBlocks = new HashSet<>(List.of(KUGELBLITZ_BLOCKS.get("neutronium_frame").get(), KUGELBLITZ_BLOCKS.get("chamber_terminal").get(), KUGELBLITZ_BLOCKS.get("chamber_port").get()));
    }

    @Override
    public void validate() {
        debugLog("=== Starting Kugelblitz Chamber validation at " + controllerPos.toShortString() + " ===");

        super.validate();
        
        if(initialized && (!outerValid || !innerValid)) {
            debugLog("Kugelblitz chamber became invalid - removing black hole");
            removeBlackHole();
        }
        
        if(validationResult.isValid) {
            debugLog("Components - Transformers: " + transformers + 
                    ", Flux regulators: " + fluxRegulators + 
                    ", Stabilizers: " + stabilizers);
        } else {
            debugLog("Kugelblitz chamber validation failed with result: " + validationResult);
        }
        
        initialized = true;
    }

    @Override
    public void setForRemoval() {

    }

    @Override
    public int resolveDepth()
    {
        if(isValidForOuter(getForwardPos(6))) {
            depth = 6;
        }
        if(isValidForOuter(getForwardPos(8))) {
            depth = 8;
        }
        return depth;
    }

    protected int getTopY(int forward, int up) {
        if (isValidForOuter(getForwardPos(forward).above(up))) {
            return controllerBE().getBlockPos().getY()+up;
        }
        return 0;
    }

    @Override
    public void validateOuter()
    {
        fluxRegulators = 0;
        stabilizers = 0;
        transformers = 0;
        errorBlockPos = BlockPos.ZERO;
        bottomLeft = null;
        topRight = null;
        controllers.clear();
        resolveWidth();
        resolveHeight();
        resolveDepth();
        outerValid = false;
        if(height() != 1 && width() != 5) {
            validationResult = ValidationResult.INCOMPLETE;
            return;
        }
        //Finding top center block

        //absolute top position
        int topY = getTopY(2, 9);
        topY = topY != 0 ? topY : getTopY(2, 8);
        topY = topY != 0 ? topY : getTopY(2, 1);
        topY = topY != 0 ? topY : getTopY(2, 2);

        //check if opposite block exists
        if (topY == 0 || !isValidForOuter(getForwardPos(2).below(10-(topY-controllerBE().getBlockPos().getY())))) {
            return;
        }

        int left = 0;
        //detect topRight block
        for (int i = 0; i < 6; i++) {
            if (!isValidForOuter(getLeftPos(i))) {
                break;
            }
            left = i;
        }
        int forward = depth() == 8 ? -4 : -3;
        BlockPos l = getLeftPos(left-2).relative(getControllerDirection(), forward);
        BlockPos topCenter = new BlockPos(l.getX(), topY, l.getZ());
        if(!(NCLevels.getExistingBlockEntity(getLevel(), topCenter) instanceof PhotonConcentratorBE pcBE)) {
            validationResult = ValidationResult.PHOTON_CONCENTRATOR;
            return;
        } else {
            pcBE.setMultiblock(this);
        }
        //Initial set of blocks
        List<BlockState> topWall = getWallBlocks(Direction.Axis.Y, topCenter);
        if(topWall.size() != 25) {
            validationResult = ValidationResult.INCOMPLETE;
            return;
        }

        //check if walls are identical
        if(
                !isWallValid(topWall, getWallBlocks(Direction.Axis.Y, topCenter.below(10)))
                || !isWallValid(topWall, getWallBlocks(Direction.Axis.X, topCenter.below(5).west(5)))
                || !isWallValid(topWall, getWallBlocks(Direction.Axis.X, topCenter.below(5).east(5)))
                || !isWallValid(topWall, getWallBlocks(Direction.Axis.Z, topCenter.below(5).north(5)))
                || !isWallValid(topWall, getWallBlocks(Direction.Axis.Z, topCenter.below(5).south(5)))
        ) {
            validationResult = ValidationResult.ASYMETRIC_WALLS;
            return;
        }

        //validate frames around walls
        if(
                !isFrameValid(Direction.Axis.Y, topCenter.below(1))
                || !isFrameValid(Direction.Axis.Y, topCenter.below(9))
                || !isFrameValid(Direction.Axis.X, topCenter.below(5).west(4))
                || !isFrameValid(Direction.Axis.X, topCenter.below(5).east(4))
                || !isFrameValid(Direction.Axis.Z, topCenter.below(5).north(4))
                || !isFrameValid(Direction.Axis.Z, topCenter.below(5).south(4))
        ) {
            validationResult = ValidationResult.INCOMPLETE;
            return;
        }

        if(
                !isCornersValid(topCenter.below(2))
                || !isCornersValid(topCenter.below(8))
        ) {
            validationResult = ValidationResult.INCOMPLETE;
            return;
        }

        if (controllers.size() > 1) {
            validationResult = ValidationResult.TOO_MANY_CONTROLLERS;
            return;
        }
        centerBlockPos = topCenter.below(5);
        bottomLeft = BlockPosInstance.of(topCenter.offset(-3, -3, -3));
        topRight = BlockPosInstance.of(topCenter.offset(3, 0, 3));
        validationResult = ValidationResult.VALID;
        outerValid = true;
    }

    public void validateInner() {
        if (!outerValid) {
            clearStats();
            return;
        }
        if (centerBlockPos == null || centerBlockPos.equals(BlockPos.ZERO)) {
            validationResult = ValidationResult.INCOMPLETE;
            return;
        }

        final int radius = 4;

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    // Calculate squared distance from center
                    double distSquared = x*x + y*y + z*z;

                    // If within sphere (radius² = 16)
                    if (distSquared <= radius * radius) {
                        BlockPos pos = centerBlockPos.offset(x, y, z);

                        if (!isValidForInner(pos)) {
                            validationResult = ValidationResult.WRONG_INNER;
                            errorBlockPos = new BlockPos(pos);
                            return;
                        }

                        processInnerBlock(pos);
                    }
                }
            }
        }
        controllerBE().controllerEnabled = isFormed();
        controllerBE().fluxRegulators = fluxRegulators();
        controllerBE().transformers = transformers();
        controllerBE().stabilizers = stabilizers();
        controllerBE().blackholePos = getCenter();
        controllerBE().setChanged();
        validationResult =  ValidationResult.VALID;
        errorBlockPos = BlockPos.ZERO;
        innerValid = true;
    }

    @Override
    public HashSet<Block> validCornerBlocks() {
        return validCornerBlocks;
    }

    private boolean isCornersValid(BlockPos center) {
        if(!isValidForOuter(center.offset(3, 0, 3))
                || !isValidForOuter(center.offset(-3, 0, 3))
                || !isValidForOuter(center.offset(3, 0, -3))
                || !isValidForOuter(center.offset(-3, 0, -3))
        ) {
            return false;
        }
        processOuterBlock(center.offset(3, 0, 3));
        processOuterBlock(center.offset(-3, 0, 3));
        processOuterBlock(center.offset(3, 0, -3));
        processOuterBlock(center.offset(-3, 0, -3));
        return true;
    }

    private boolean isFrameValid(Direction.Axis axis, BlockPos center) {
        for (int i = -3; i <= 3; i++) {
            for (int j = -3; j <= 3; j++) {
                if (Math.abs(i) != 3 && Math.abs(j) != 3) continue;
                if ((i == -3 && j == -3) || (i == -3 && j == 3) ||
                        (i == 3 && j == -3) || (i == 3 && j == 3)) continue;
                BlockPos posToCheck;
                if (axis == Direction.Axis.Y) {
                    posToCheck = center.offset(i, 0, j);
                } else if (axis == Direction.Axis.X) {
                    posToCheck = center.offset(0, i, j);
                } else {
                    posToCheck = center.offset(i, j, 0);
                }

                if (!isValidCorner(posToCheck)) {
                    errorBlockPos = new BlockPos(posToCheck);
                    return false;
                }
                processOuterBlock(posToCheck);
            }
        }

        return true;
    }

    //Compare walls to be identical
    private boolean isWallValid(List<BlockState> initial, List<BlockState> toVerify) {
        if (initial.size() != toVerify.size()) {
            return false;
        }
        for (int i = 0; i < initial.size(); i++) {
            if (!initial.get(i).is(toVerify.get(i).getBlock())) {
                return false;
            }
        }
        return true;
    }

    private List<BlockState> getWallBlocks(Direction.Axis axis, BlockPos center) {
        List<BlockState> blocks = new ArrayList<>();
        for (int i = -2; i <= 2; i++) {
            for (int j = -2; j <= 2; j++) {
                BlockPos newPos;
                if (axis == Direction.Axis.Y) {
                    newPos = center.offset(i, 0, j);
                } else if (axis == Direction.Axis.X) {
                    newPos = center.offset(0, i, j);
                } else {
                    newPos = center.offset(i, j, 0);
                }
                BlockState bs = getBlockState(newPos);
                if (bs.isAir() || !isValidForOuter(newPos)) {
                    errorBlockPos = new BlockPos(newPos);
                    return blocks;
                }
                if (bs.is(KUGELBLITZ_BLOCKS.get("quantum_transformer").get())) {
                    transformers++;
                }
                if (bs.is(KUGELBLITZ_BLOCKS.get("quantum_flux_regulator").get())) {
                    fluxRegulators++;
                }
                if (bs.is(KUGELBLITZ_BLOCKS.get("event_horizon_stabilizer").get())) {
                    stabilizers++;
                }
                blocks.add(bs);
                processOuterBlock(newPos);
            }
        }

        return blocks;
    }

    @Override
    public void tick(Level level) {
        super.tick(level);
        //multiblock is not formed
        if(centerBlockPos == null) return;
        for(Direction dir: Direction.values()) {
            if(getBlockEntity(getCenter().relative(dir, 5)) instanceof PhotonConcentratorBE pcBE) {
                pcBE.setMultiblock(this);
            }
        }
    }



    @Override
    public void clearStats() {
        controllerBE().controllerEnabled = false;
        controllerBE().fluxRegulators = 0;
        controllerBE().transformers = 0;
        controllerBE().stabilizers = 0;
        controllerBE().setChanged();
    }

    public BlockPos getCenter() {
        return centerBlockPos;
    }

    public int fluxRegulators() {
        return fluxRegulators;
    }

    public int stabilizers() {
        return stabilizers;
    }

    public int transformers() {
        return transformers;
    }

    public void removeBlackHole() {
        if(blackHole != null) {
            getLevel().setBlock(blackHole.getBlockPos(), AIR.defaultBlockState(), 3);
            blackHole = null;
            return;
        }
        getLevel().setBlock(getCenter(), AIR.defaultBlockState(), 3);
        blackHole = null;
    }

    @Override
    public void onControllerRemoved() {
        removeBlackHole();
        super.onControllerRemoved();
    }

    public void gotLaserBurst() {
        controllerBE().gotLaserBurst = true;
    }
}
