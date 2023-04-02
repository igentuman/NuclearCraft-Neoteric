package igentuman.nc.block.entity.fission;

import igentuman.nc.setup.registration.NCBlocks;
import igentuman.nc.util.ValidationResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;


public class FissionController extends FissionBE {
    public static String NAME = "fission_controller";
    private Direction facing;

    public FissionController(BlockPos pPos, BlockState pBlockState) {
        super(NCBlocks.MULTIBLOCK_BE.get(NAME).get(), pPos, pBlockState);
    }

    public boolean isCasingValid = false;
    public boolean isInternalValid = false;

    public int height = 0;
    public int width = 0;
    public int depth = 0;

    public int topCasing = 0;
    public int bottomCasing = 0;

    public int leftCasing = 0;
    public int rightCasing = 0;

    public int backCasing = 0;




    public void tickClient() {
    }

    public void tickServer() {

        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }

    @Override
    public String getName() {
        return NAME;
    }

    public void validateStructure()
    {
        ValidationResult casingValidation = validateCasing(getBlockPos(), getLevel());
        isCasingValid = casingValidation.isValid;
        if(!isCasingValid) {
            isInternalValid = false;
            return;
        }
        ValidationResult internalValidation = validateInternal(getBlockPos(), getLevel());
        isInternalValid = internalValidation.isValid;
        if(!internalValidation.isValid) {
            return;
        }
    }

    public ValidationResult validateInternal(BlockPos blockPos, Level level) {
        return new ValidationResult(true);
    }

    private void notifyPlayers(String messageKey, BlockPos errorBlock) {
        //getLevel().getNearestPlayer()
    }

    public ValidationResult validateCasing(BlockPos blockPos, Level level) {
        for(int y = 0; y < getHeight(); y++) {
            int xStep = 1;
            if(y > 0 && y < getHeight()) {
                xStep = getWidth()-1;
            }
            for(int x = 0; x < getWidth(); x+=xStep) {
                int zStep = 1;
                if(x > 0 && x < getWidth()) {
                    zStep = getDepth()-1;
                }
                for (int z = 0; z < getDepth(); z+=zStep) {
                    if(!isFissionCasing(getBlockPos().offset(x, y-bottomCasing, z))) {
                        return new ValidationResult(false, "fission.cassing.wrong.block", getBlockPos().offset(x, y-bottomCasing, z));
                    }
                }
            }
        }
        return new ValidationResult(true);
    }

    public boolean isFissionCasing(BlockPos pos)
    {
       return getLevel().getBlockEntity(pos) instanceof FissionBE;
    }

    public int getHeight()
    {
        if(height == 0) {
            for(int i = 1; i<24-1; i++) {
                if(!isFissionCasing(getBlockPos().above(i))) {
                    topCasing = i;
                    height = i;
                }
            }
            for(int i = 1; i<24-height-1; i++) {
                if(!isFissionCasing(getBlockPos().below(i))) {
                    bottomCasing = i;
                    height += i;
                }
            }
        }
        return height;
    }

    public int getWidth()
    {
        if(width == 0) {
            for(int i = 1; i<24-1; i++) {
                if(!isFissionCasing(getLeftPos(i))) {
                    leftCasing = i;
                    width = i;
                }
            }
            for(int i = 1; i<24-width-1; i++) {
                if(!isFissionCasing(getRightPos(i))) {
                    rightCasing = i;
                    width += i;
                }
            }
        }
        return width;
    }

    public int getDepth()
    {
        if(depth == 0) {
            for(int i = 1; i<24-1; i++) {
                if(!isFissionCasing(getForwardPos(i).above(topCasing))) {
                    depth = i;
                }
            }
        }
        return depth;
    }

    public BlockPos getForwardPos(int i) {
        return getBlockPos().relative(getFacing(), i);
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
            case 0 -> getBlockPos().north(i);
            case 1 -> getBlockPos().east(i);
            case 2 -> getBlockPos().south(i);
            case 3 -> getBlockPos().west(i);
            default -> null;
        };
    }

    private Direction getFacing() {
        if(facing == null) {
            facing = getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        }
        return facing;
    }
}
