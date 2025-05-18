package igentuman.nc.block.entity;

import igentuman.api.nc.multiblock.MultiblockAttachable;
import igentuman.nc.handler.event.client.BlockOverlayHandler;
import igentuman.nc.handler.sided.SidedContentHandler;
import igentuman.nc.handler.sided.capability.ItemCapabilityHandler;
import igentuman.nc.multiblock.AbstractMultiblock;
import igentuman.nc.multiblock.MultiblockHandler;
import igentuman.nc.multiblock.ValidationResult;
import igentuman.nc.util.CustomEnergyStorage;
import igentuman.nc.util.NCBlockPos;
import igentuman.nc.util.annotation.NBTField;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;

public class MultiblockControllerBE extends NuclearCraftBE implements MultiblockAttachable<AbstractMultiblock, MultiblockControllerBE> {

    @NBTField
    public int height = 1;
    @NBTField
    public int width = 1;
    @NBTField
    public int depth = 1;
    @NBTField
    public boolean isCasingValid = false;
    @NBTField
    public boolean isInternalValid = false;
    public boolean refreshCacheFlag = true;
    public byte validationRuns = 0;
    protected AbstractMultiblock multiblock;
    public BlockPos errorBlockPos = BlockPos.ZERO;
    public ValidationResult validationResult = ValidationResult.INCOMPLETE;
    public boolean controllerEnabled = false;

    public MultiblockControllerBE(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    @Override
    public void setMultiblock(AbstractMultiblock multiblock) {
        this.multiblock = multiblock;
    }

    @Override
    public MultiblockControllerBE controller() {
        return this;
    }

    @Override
    public AbstractMultiblock getMultiblock() {
        return multiblock;
    }

    @Override
    public boolean canInvalidateCache() {
        return true;
    }

    public void invalidateCache()
    {
        getMultiblock().hasToRefresh = true;
        isCasingValid = false;
        isInternalValid = false;
    }

    public int getDepth() {
        return depth;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    @Override
    public ItemCapabilityHandler getItemInventory()
    {
        return contentHandler().itemHandler;
    }

    public LazyOptional<IEnergyStorage> getEnergy() {
        return energy;
    }

    @Override
    public SidedContentHandler contentHandler() {
        return contentHandler;
    }

    @Override
    public CustomEnergyStorage energyStorage() {
        return energyStorage;
    }

    public void tickServer() {

    }

    public void tickClient() {
        if(!isCasingValid && !isInternalValid && errorBlockPos != null && errorBlockPos != BlockPos.ZERO) {
            BlockOverlayHandler.addToOutline(new NCBlockPos(errorBlockPos.getX(), errorBlockPos.getY(), errorBlockPos.getZ()));
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Info")) {
            CompoundTag infoTag = tag.getCompound("Info");
            if (!isCasingValid || !isInternalValid) {
                if(tag.contains("erroredBlock")) {
                    errorBlockPos = BlockPos.of(infoTag.getLong("erroredBlock"));
                } else {
                    errorBlockPos = BlockPos.ZERO;
                }
                validationResult = ValidationResult.byId(infoTag.getInt("validationId"));
            } else {
                validationResult = ValidationResult.VALID;
                errorBlockPos = BlockPos.ZERO;
            }
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if(getLevel().isClientSide()) {
            return;
        }
        if(getMultiblock() != null) {
            getMultiblock().onControllerRemoved();
        }
    }

    protected void handleValidation() {
        boolean wasFormed = isInternalValid && isCasingValid;
        validationResult = getMultiblock().validationResult;
        if(!errorBlockPos.equals(getMultiblock().errorBlockPos)) {
            errorBlockPos = getMultiblock().errorBlockPos;
            changed = true;
        }
        isInternalValid = getMultiblock().isInnerValid();
        isCasingValid = getMultiblock().isOuterValid();
        height = getMultiblock().height();
        width = getMultiblock().width();
        depth = getMultiblock().depth();
        trackChanges(wasFormed, getMultiblock().isFormed());
    }

    @Override
    public void setChanged() {
        MultiblockHandler.get(level.dimension()).addIgnoreToUpdate(getBlockPos());
        super.setChanged();
        wasUpdated = true;
        changed = true;
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (tag.contains("Info")) {
            CompoundTag infoTag = tag.getCompound("Info");
            infoTag.putInt("validationId", validationResult.id);
            if(errorBlockPos instanceof BlockPos) {
                infoTag.putLong("erroredBlock", errorBlockPos.asLong());
            } else {
                infoTag.putLong("erroredBlock", 0);
            }
            tag.remove("Info");
            tag.put("Info", infoTag);
        }
    }

    @Override
    public void loadClientData(CompoundTag tag) {
        super.loadClientData(tag);
        if (tag.contains("Info")) {
            CompoundTag infoTag = tag.getCompound("Info");
            if (!isCasingValid || !isInternalValid) {
                BlockPos tmp = BlockPos.of(infoTag.getLong("erroredBlock"));
                if(!tmp.equals(errorBlockPos) && level.isClientSide()) {
                    BlockOverlayHandler.removeFromOutline(NCBlockPos.copy(errorBlockPos), true);
                }
                errorBlockPos = tmp;
                validationResult = ValidationResult.byId(infoTag.getInt("validationId"));
            } else {
                validationResult = ValidationResult.VALID;
            }
        }
    }

    @Override
    protected void saveClientData(CompoundTag tag) {
        super.saveClientData(tag);
        if (tag.contains("Info")) {
            CompoundTag infoTag = tag.getCompound("Info");
            infoTag.putInt("validationId", validationResult.id);
            if(errorBlockPos instanceof BlockPos) {
                infoTag.putLong("erroredBlock", errorBlockPos.asLong());
            } else {
                infoTag.putLong("erroredBlock", 0);
            }
            tag.remove("Info");
            tag.put("Info", infoTag);
        }
    }
}
