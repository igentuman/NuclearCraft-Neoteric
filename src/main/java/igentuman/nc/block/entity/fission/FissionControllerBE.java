package igentuman.nc.block.entity.fission;

import igentuman.nc.util.ValidationResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import javax.annotation.Nullable;


public class FissionControllerBE extends FissionBE {
    public static String NAME = "fission_reactor_controller";
    public int heat;
    private Direction facing;

    public FissionControllerBE(BlockPos pPos, BlockState pBlockState) {
        super(pPos, pBlockState, NAME);
    }

    public ValidationResult validationResult;

    public boolean isCasingValid = false;
    public boolean isInternalValid = false;

    public int height = 1;
    public int width = 1;
    public int depth = 1;

    public int topCasing = 0;
    public int bottomCasing = 0;

    public int leftCasing = 0;
    public int rightCasing = 0;

    public int energy = 0;

    public void tickClient() {
    }

    public void tickServer() {
        validationResult = validateStructure();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }


    @Override
    public String getName() {
        return NAME;
    }

    public ValidationResult validateStructure()
    {
        ValidationResult casingValidation = validateCasing(getBlockPos(), getLevel());
        isCasingValid = casingValidation.isValid;
        if(!isCasingValid) {
            isInternalValid = false;
            return casingValidation;
        }
        ValidationResult internalValidation = validateInternal(getBlockPos(), getLevel());
       // isInternalValid = internalValidation.isValid;
        if(!internalValidation.isValid) {
            return internalValidation;
        }
        return new ValidationResult(true);
    }

    public ValidationResult validateInternal(BlockPos blockPos, Level level) {
        return new ValidationResult(true);
    }

    private void notifyPlayers(String messageKey, BlockPos errorBlock) {
        //getLevel().getNearestPlayer()
    }

    public ValidationResult validateCasing(BlockPos blockPos, Level level) {
        for(int y = 0; y < getHeight(); y++) {
            for(int x = 0; x < getWidth(); x++) {
                for (int z = 0; z < getDepth(); z++) {
                    if(y == 0 || x == 0 || z == 0 || y == getHeight()-1 || x == getWidth()-1 || z == getDepth()-1) {
                        if (!isFissionCasing(getSidePos(x - leftCasing).above(y - bottomCasing).relative(getFacing(), -z))) {
                            return new ValidationResult(
                                    false,
                                    "fission.casing.wrong.block",
                                    getSidePos(x - leftCasing).above(y - bottomCasing).relative(getFacing(), -z)
                            );
                        }
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
        if(height == 1 || !isCasingValid) {
            for(int i = 1; i<24-1; i++) {
                if(!isFissionCasing(getBlockPos().above(i))) {
                    topCasing = i-1;
                    height = i;
                    break;
                }
            }
            for(int i = 1; i<24-height-1; i++) {
                if(!isFissionCasing(getBlockPos().below(i))) {
                    bottomCasing = i-1;
                    height += i-1;
                    break;
                }
            }
        }
        return height;
    }

    public int getWidth()
    {
        if(width == 1 || !isCasingValid) {
            for(int i = 1; i<24-1; i++) {
                if(!isFissionCasing(getLeftPos(i))) {
                    leftCasing = i-1;
                    width = i;
                    break;

                }
            }
            for(int i = 1; i<24-width-1; i++) {
                if(!isFissionCasing(getRightPos(i))) {
                    rightCasing = i-1;
                    width += i-1;
                    break;
                }
            }
        }
        return width;
    }

    public int getDepth()
    {
        if(depth == 1 || !isCasingValid) {
            for(int i = 1; i<24-1; i++) {
                if(!isFissionCasing(getForwardPos(i).above(topCasing))) {
                    depth = i;
                    break;
                }
            }
        }
        return depth;
    }

    public BlockPos getForwardPos(int i) {
        return getBlockPos().relative(getFacing(), -i);
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
            case 0 -> getBlockPos().east(i);
            case 1 -> getBlockPos().north(i);
            case 2 -> getBlockPos().west(i);
            case 3 -> getBlockPos().south(i);
            default -> null;
        };
    }


    @Override
    public void load(CompoundTag tag) {
        if (tag.contains("Info")) {
            CompoundTag infoTag = tag.getCompound("Info");
            energy = infoTag.getInt("energy");
            height = infoTag.getInt("height");
            width = infoTag.getInt("width");
            depth = infoTag.getInt("depth");
            isCasingValid = infoTag.getBoolean("isCasingValid");

            if(!isCasingValid) {
                validationResult = new ValidationResult(
                        false,
                        infoTag.getString("validationKey"),
                        BlockPos.of(infoTag.getLong("erroredBlock"))
                );
            } else {
                validationResult = new ValidationResult(true);
            }
        }
        super.load(tag);
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        CompoundTag infoTag = new CompoundTag();
        infoTag.putInt("energy", energy);
        infoTag.putBoolean("isCasingValid", isCasingValid);
        infoTag.putString("validationKey", validationResult.messageKey);
        infoTag.putLong("erroredBlock", validationResult.errorBlock.asLong());
        infoTag.putInt("height", getHeight());
        infoTag.putInt("width", getWidth());
        infoTag.putInt("depth", getDepth());
        tag.put("Info", infoTag);
    }

    private Direction getFacing() {
        if(facing == null) {
            facing = getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        }
        return facing;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        if (tag != null) {
            loadClientData(tag);
        }
    }

    private void loadClientData(CompoundTag tag) {
        if (tag.contains("Info")) {
            CompoundTag infoTag = tag.getCompound("Info");
            energy = infoTag.getInt("energy");
            isCasingValid = infoTag.getBoolean("isCasingValid");
            height = infoTag.getInt("height");
            width = infoTag.getInt("width");
            depth = infoTag.getInt("depth");
            if(!isCasingValid) {
                validationResult = new ValidationResult(
                        false,
                        infoTag.getString("validationKey"),
                        BlockPos.of(infoTag.getLong("erroredBlock"))
                );
            } else {
                validationResult = new ValidationResult(true);
            }
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveClientData(tag);
        return tag;
    }

    private void saveClientData(CompoundTag tag) {
        CompoundTag infoTag = new CompoundTag();
        tag.put("Info", infoTag);
        infoTag.putInt("energy", energy);
        infoTag.putBoolean("isCasingValid", isCasingValid);
        infoTag.putInt("height", getHeight());
        infoTag.putInt("width", getWidth());
        infoTag.putInt("depth", getDepth());
        infoTag.putString("validationKey", validationResult.messageKey);
        infoTag.putLong("erroredBlock", validationResult.errorBlock.asLong());
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag tag = pkt.getTag();
        handleUpdateTag(tag);
    }

    public int getDepletionProgress() {
        return 0;
    }
}
