package igentuman.nc.block.entity;

import igentuman.api.nc.multiblock.MultiblockAttachable;
import igentuman.nc.handler.event.client.BlockOverlayHandler;
import igentuman.nc.handler.sided.SidedContentHandler;
import igentuman.nc.handler.sided.capability.ItemCapabilityHandler;
import igentuman.nc.multiblock.AbstractMultiblock;
import igentuman.nc.multiblock.MultiblockHandler;
import igentuman.nc.multiblock.ValidationResult;
import igentuman.nc.util.capability.CustomEnergyStorage;
import igentuman.nc.util.BlockPosInstance;
import igentuman.nc.util.annotation.NBTField;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Objects;

import static igentuman.nc.NuclearCraft.currentTick;

public class MultiblockControllerBE extends NuclearCraftBE implements MultiblockAttachable<AbstractMultiblock, MultiblockControllerBE> {

    @NBTField
    public int height = 1;
    @NBTField
    public int upgrade_tier = 0;
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
    @NBTField
    public int analyzeDelay = 0;
    @NBTField
    public BlockPos bottomLeft = BlockPos.ZERO;
    @NBTField
    public BlockPos topRight = BlockPos.ZERO;
    protected AbstractMultiblock multiblock;
    public BlockPos errorBlockPos = BlockPos.ZERO;
    public ValidationResult validationResult = ValidationResult.INCOMPLETE;
    public boolean controllerEnabled = false;
    @NBTField
    private boolean displayDetailedDataFlag = false;
    protected long lastTickTime = 0;
    @NBTField
    public long validationsCounter = 0;
    @NBTField
    public long validationTime = 0;
    @NBTField
    public long multiblockTicksCounter = 0;
    public MultiblockControllerBE(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    public int getBaseGTEnergyTier() {
        return 0;
    }

    public int getRedstoneSignal() {
        return Objects.requireNonNull(getLevel()).getBestNeighborSignal(worldPosition);
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
        if(getLevel().isClientSide()) {
            return;
        }
        // Only flag the multiblock for re-validation.
        // Don't reset isCasingValid/isInternalValid here — let handleValidation()
        // update them when the async re-validation actually completes.
        // Resetting eagerly causes the client to briefly see "incomplete".
        getMultiblock().hasToRefresh = true;
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


    @Override
    public SidedContentHandler contentHandler() {
        return contentHandler;
    }

    @Override
    public CustomEnergyStorage energyStorage() {
        return energyStorage;
    }

    public void tickServer() {
        if(analyzeDelay > 0) {
            analyzeDelay--;
        }
        assert level != null;
        if(level.getGameTime() % 5 == 0) {
            MultiblockHandler.tickMultiblock((ServerLevel) level, getMultiblock());
            if(multiblock != null && multiblock.isMarkedForRemoval()) {
                multiblock = null;
            }
        }
    }

    public void tickClient() {
        if((!isCasingValid || !isInternalValid) && !errorBlockPos.equals(BlockPos.ZERO)) {
            BlockOverlayHandler.addToOutline(new BlockPosInstance(errorBlockPos.getX(), errorBlockPos.getY(), errorBlockPos.getZ()));
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Info")) {
            CompoundTag infoTag = tag.getCompound("Info");
            if(infoTag.contains("erroredBlock")) {
                errorBlockPos = BlockPos.of(infoTag.getLong("erroredBlock"));
            } else {
                errorBlockPos = BlockPos.ZERO;
            }
            validationResult = ValidationResult.byId(infoTag.getInt("validationId"));
        }
    }

    protected void handleValidation() {
        getMultiblock().controller().setControllerBe(this);
        boolean wasFormed = isInternalValid && isCasingValid;
        validationResult = getMultiblock().validationResult;
        if(errorBlockPos == null || !errorBlockPos.equals(getMultiblock().errorBlockPos)) {
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
        if(level != null) {
            MultiblockHandler.get(level.dimension()).addIgnoreToUpdate(getBlockPos());
            level.blockEntityChanged(getBlockPos());
        }
        wasUpdated = true;
        changed = true;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (tag.contains("Info")) {
            CompoundTag infoTag = tag.getCompound("Info");
            infoTag.putInt("validationId", validationResult.id);
            if(errorBlockPos == null) {
                errorBlockPos = BlockPos.ZERO;
            }
            infoTag.putLong("erroredBlock", errorBlockPos.asLong());
            tag.remove("Info");
            tag.put("Info", infoTag);
        }
    }

    @Override
    public void loadClientData(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadClientData(tag, registries);
        if (tag.contains("Info")) {
            CompoundTag infoTag = tag.getCompound("Info");
            BlockPos tmp = BlockPos.ZERO;
            if(infoTag.contains("erroredBlock")) {
                tmp = BlockPos.of(infoTag.getLong("erroredBlock"));
            }
            if(!tmp.equals(errorBlockPos) && level.isClientSide()) {
                BlockOverlayHandler.removeFromOutline(BlockPosInstance.copy(errorBlockPos), true);
            }
            errorBlockPos = tmp;
            validationResult = ValidationResult.byId(infoTag.getInt("validationId"));
        }
    }

    @Override
    protected void saveClientData(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveClientData(tag, registries);
        if (tag.contains("Info")) {
            CompoundTag infoTag = tag.getCompound("Info");
            infoTag.putInt("validationId", validationResult.id);
            if(errorBlockPos == null) {
                errorBlockPos = BlockPos.ZERO;
            }
            infoTag.putLong("erroredBlock", errorBlockPos.asLong());
            tag.remove("Info");
            tag.put("Info", infoTag);
        }
    }

    public void runAnalyze() {
        if (analyzeDelay > 0) {
            return;
        }
        analyzeDelay = 100;
        if (getMultiblock() != null) {
            getMultiblock().wipeCache();
            getMultiblock().setForRemoval();
            displayDetailedDataFlag = true;
        }
    }

    public HashMap<String, String> getAnalyzeReport() {
        return new HashMap<>();
    }

    public void updateEnergyTier(int upgradeTier) {
        if(energyStorage().getGTOutputAmperage() > 0) {
            energyStorage().setOutputEnergyTier(getBaseGTEnergyTier() + upgradeTier);
        }
        if(energyStorage().getGTInputAmperage() > 0) {
            energyStorage().setInputEnergyTier(getBaseGTEnergyTier() + upgradeTier);
        }

        setChanged();
    }
}
