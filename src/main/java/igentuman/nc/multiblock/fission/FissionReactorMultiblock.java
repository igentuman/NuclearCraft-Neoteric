package igentuman.nc.multiblock.fission;

import igentuman.nc.block.entity.fission.FissionBE;
import igentuman.nc.block.entity.fission.FissionControllerBE;
import igentuman.nc.block.entity.fission.FissionFuelCellBE;
import igentuman.nc.block.entity.fission.FissionHeatSinkBE;
import igentuman.nc.multiblock.AbstractNCMultiblock;
import igentuman.nc.setup.multiblocks.*;
import igentuman.nc.multiblock.ValidationResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

import static igentuman.nc.util.TagUtil.getBlocksByTagKey;

public class FissionReactorMultiblock extends AbstractNCMultiblock {

    protected static final List<Block> validOuterBlocks = getBlocksByTagKey(FissionBlocks.CASING_BLOCKS.location().toString());
    protected static final List<Block> validInnerBlocks = getBlocksByTagKey(FissionBlocks.INNER_REACTOR_BLOCKS.location().toString());
    protected static final List<Block> moderatorBlocks = getBlocksByTagKey(FissionBlocks.MODERATORS_BLOCKS.location().toString());
    protected static final List<Block> heatSinkBlocks = getBlocksByTagKey(FissionBlocks.HEAT_SINK_BLOCKS.location().toString());
    public HashMap<BlockPos, FissionHeatSinkBE> activeHeatSinks = new HashMap<>();

    private List<BlockPos> moderators = new ArrayList<>();
    public List<BlockPos> heatSinks = new ArrayList<>();
    public List<BlockPos> fuelCells = new ArrayList<>();
    private double heatSinkCooling = 0;

    public FissionReactorMultiblock(FissionControllerBE fissionControllerBE) {
        controller = new FissionReactorController(fissionControllerBE);
    }

    public Map<BlockPos, FissionHeatSinkBE> activeHeatSinks() {
        if(activeHeatSinks.isEmpty()) {
            for(BlockPos hpos: heatSinks) {
                BlockEntity be = getLevel().getBlockEntity(hpos);
                if(be instanceof FissionHeatSinkBE) {
                    FissionHeatSinkBE hs = (FissionHeatSinkBE) be;
                    if(hs.isValid(true)) {
                        activeHeatSinks.put(hpos, hs);
                    }
                }
            }
        }
        ((FissionControllerBE)controller().controllerBE()).activeHeatSinksCount = activeHeatSinks.size();
        return activeHeatSinks;
    }

    @Override
    public List<Block> validOuterBlocks() { return validOuterBlocks;  }

    @Override
    public List<Block> validInnerBlocks() { return validInnerBlocks; }

    public BlockPos getBottomLeftBlock() {
        return getSidePos(leftCasing).above(bottomCasing).relative(getFacing(), -depth + 1);
    }

    public BlockPos getTopRightBlock() {
        return getSidePos(width - rightCasing - 1).above(height - topCasing - 1).relative(getFacing(), -1);
    }

    @Override
    public void validateOuter() {
        for(int y = 0; y < resolveHeight(); y++) {
            for(int x = 0; x < resolveWidth(); x++) {
                for (int z = 0; z < resolveDepth(); z++) {
                    if(width < 3 || height < 3 || depth < 3)
                    {
                        validationResult = ValidationResult.TOO_SMALL;
                        return;
                    }
                    if(y == 0 || x == 0 || z == 0 || y == resolveHeight()-1 || x == resolveWidth()-1 || z == resolveDepth()-1) {
                        if (!isValidForOuter(getSidePos(x - leftCasing).above(y - bottomCasing).relative(getFacing(), -z))) {
                            validationResult = ValidationResult.WRONG_OUTER;
                            ((FissionControllerBE)controller().controllerBE()).errorBlockPos =  getSidePos(x - leftCasing).above(y - bottomCasing).relative(getFacing(), -z);
                            return;
                        }
                        attachMultiblock(getSidePos(x - leftCasing).above(y - bottomCasing).relative(getFacing(), -z));
                        allBlocks.add(getSidePos(x - leftCasing).above(y - bottomCasing).relative(getFacing(), -z));
                    }
                }
            }
        }
        validationResult = ValidationResult.VALID;
    }

    private void attachMultiblock(BlockPos pos) {
        BlockEntity be = getLevel().getBlockEntity(pos);
        if(be instanceof FissionBE) {
            ((FissionBE) be).setMultiblock(this);
        }
    }

    public static boolean isModerator(BlockPos pos, Level world) {
        return  moderatorBlocks.contains(Objects.requireNonNull(world).getBlockState(pos).getBlock());
    }

    protected boolean isHeatSink(BlockPos pos) {
        return heatSinkBlocks.contains(getLevel().getBlockState(pos).getBlock());
    }

    protected boolean isFuelCell(BlockPos pos) {
        return FissionReactor.MULTI_BLOCKS.get("fission_reactor_solid_fuel_cell").get().equals(getLevel().getBlockState(pos).getBlock());
    }

    @Override
    public void validateInner() {
        invalidateStats();
        for(int y = 1; y < resolveHeight()-1; y++) {
            for(int x = 1; x < resolveWidth()-1; x++) {
                for (int z = 1; z < resolveDepth()-1; z++) {
                    BlockPos toCheck = getSidePos(x - leftCasing).above(y - bottomCasing).relative(getFacing(), -z);
                    if (isValidForOuter(toCheck)) {
                        validationResult = ValidationResult.WRONG_INNER;
                        ((FissionControllerBE)controller().controllerBE()).errorBlockPos = getSidePos(x - leftCasing).above(y - bottomCasing).relative(getFacing(), -z);
                        return;
                    }
                    if(isFuelCell(toCheck)) {
                        BlockEntity be = getLevel().getBlockEntity(toCheck);
                        if(be instanceof FissionFuelCellBE) {
                            fuelCells.add(toCheck);
                            ((FissionControllerBE)controller().controllerBE()).activeModeratorsAttachmentsCount += ((FissionFuelCellBE) be).getAttachedModeratorsCount();
                        }
                    }
                    if(isModerator(toCheck, getLevel())) {
                        moderators.add(toCheck);
                    }
                    if(isHeatSink(toCheck)) {
                        heatSinks.add(toCheck);
                    }
                    allBlocks.add(toCheck);
                }
            }
        }
        activeHeatSinks();
        ((FissionControllerBE)controller().controllerBE()).fuelCellsCount = fuelCells.size();
        ((FissionControllerBE)controller().controllerBE()).updateEnergyStorage();
        ((FissionControllerBE)controller().controllerBE()).activeModeratorsCount = moderators.size();

        validationResult =  ValidationResult.VALID;
    }

    public boolean isValidForOuter(BlockPos pos)
    {
        if(getLevel() == null) return false;
        try {
            return  validOuterBlocks().contains(getLevel().getBlockState(pos).getBlock());
        } catch (NullPointerException ignored) { }
        return false;
    }

    public int resolveHeight()
    {
        if(height < minHeight() || !outerValid) {
            for(int i = 1; i<maxHeight()-1; i++) {
                if(!isValidForOuter(controllerPos().above(i))) {
                    topCasing = i-1;
                    height = i;
                    break;
                }
            }
            for(int i = 1; i<maxHeight()-height-1; i++) {
                if(!isValidForOuter(controllerPos().below(i))) {
                    bottomCasing = i-1;
                    height += i-1;
                    break;
                }
            }
        }
        return height;
    }

    public int resolveWidth()
    {
        if(width < minWidth() || !outerValid) {
            for(int i = 1; i<maxWidth()-1; i++) {
                if(!isValidForOuter(getLeftPos(i))) {
                    leftCasing = i-1;
                    width = i;
                    break;

                }
            }
            for(int i = 1; i<maxWidth()-width-1; i++) {
                if(!isValidForOuter(getRightPos(i))) {
                    rightCasing = i-1;
                    width += i-1;
                    break;
                }
            }
        }
        return width;
    }

    public int resolveDepth()
    {
        if(depth < minDepth() || !outerValid) {
            for(int i = 1; i<maxDepth()-1; i++) {
                if(!isValidForOuter(getForwardPos(i).above(topCasing))) {
                    depth = i;
                    break;
                }
            }
        }
        return depth;
    }

    public void invalidateStats()
    {
        ((FissionControllerBE)controller().controllerBE()).activeModeratorsAttachmentsCount = 0;
        ((FissionControllerBE)controller().controllerBE()).activeModeratorsCount = 0;
        ((FissionControllerBE)controller().controllerBE()).heatSinkCooling = 0;
        ((FissionControllerBE)controller().controllerBE()).fuelCellsCount = 0;
        moderators.clear();
        fuelCells.clear();
        heatSinks.clear();
        activeHeatSinks.clear();
    }


    public BlockPos getForwardPos(int i) {
        return controllerPos().relative(getFacing(), -i);
    }

    public BlockPos getLeftPos(int i)
    {
        return getSidePos(-i);
    }

    public BlockPos getRightPos(int i)
    {
        return getSidePos(i);
    }

    public BlockPos getSidePos(int i) {
        return switch (getFacing().ordinal()) {
            case 3 -> controllerPos().east(i);
            case 5 -> controllerPos().north(i);
            case 2 -> controllerPos().west(i);
            case 4 -> controllerPos().south(i);
            default -> null;
        };
    }

    private Direction getFacing() {
        return ((FissionControllerBE)controller().controllerBE()).getFacing();
    }

    public double getHeatSinkCooling() {
        if(refreshInnerCacheFlag || heatSinkCooling == 0) {
            heatSinkCooling = 0;
            for (FissionHeatSinkBE hs : activeHeatSinks().values()) {
                heatSinkCooling += hs.getHeat();
            }
        }
        return heatSinkCooling;
    }

    public void onNeighborChange(BlockState state, BlockPos pos, BlockPos neighbor) {
        //neighbor inside box
        if(neighbor.equals(controllerPos()) || !allBlocks.contains(neighbor)) return;
        refreshInnerCacheFlag = true;
        refreshOuterCacheFlag = true;
        isFormed = false;
    }
}
