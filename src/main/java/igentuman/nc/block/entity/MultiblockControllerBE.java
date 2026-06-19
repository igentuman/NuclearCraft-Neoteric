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
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;

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
    public byte analogSignal = 0;
    @NBTField
    public BlockPos bottomLeft = BlockPos.ZERO;
    @NBTField
    public BlockPos topRight = BlockPos.ZERO;
    @NBTField
    public int topCasing = 0;
    @NBTField
    public int bottomCasing = 0;
    @NBTField
    public int leftCasing = 0;
    @NBTField
    public int rightCasing = 0;
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
    @NBTField
    public boolean enabledByController = false;
    @NBTField
    public boolean hasRedstoneSignal = false;
    @NBTField
    public int energyPerTick = 0;
    @NBTField
    public double efficiency = 0;
    @NBTField
    public boolean powered = false;
    protected boolean forceShutdown = false;
    protected Direction facing;
    public boolean externalControlled = false;
    public boolean  isControlledByComputer = false;


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

    @Override
    public void updateAnalogSignal() {

    }

    public Direction getFacing() {
        if (facing == null) {
            facing = getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        }
        return facing;
    }

    public void invalidateCache()
    {
        if(getLevel().isClientSide()) {
            return;
        }
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
        if(analyzeDelay > 0) {
            analyzeDelay--;
        }
        assert level != null;
        if(currentTick % 5 == 0) {
            MultiblockHandler.tickMultiblockAsync((ServerLevel) level, getMultiblock());
            if(multiblock != null && multiblock.isMarkedForRemoval()) {
                multiblock = null;
            }
        }
    }

    public void tickClient() {
        if((!isCasingValid || !isInternalValid) && !errorBlockPos.equals(BlockPos.ZERO)) {
            BlockOverlayHandler.addToOutline(new BlockPosInstance(errorBlockPos.getX(), errorBlockPos.getY(), errorBlockPos.getZ()));
        }
        BlockOverlayHandler.registerDebugController(this);
    }

    @Override
    public void setRemoved() {
        if(getLevel() != null && getLevel().isClientSide()) {
            BlockOverlayHandler.unregisterDebugController(this);
        }
        super.setRemoved();
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Info")) {
            CompoundTag infoTag = tag.getCompound("Info");
            if(infoTag.contains("erroredBlock")) {
                errorBlockPos = BlockPos.of(infoTag.getLong("erroredBlock"));
            } else {
                errorBlockPos = BlockPos.ZERO;
            }
            validationResult = ValidationResult.byId(infoTag.getInt("validationId"));
            if (infoTag.contains("upgrade_tier")) {
                upgrade_tier = infoTag.getInt("upgrade_tier");
                updateEnergyTier(upgrade_tier);
            }
        }
    }

    protected void handleValidation() {
        boolean wasFormed = isInternalValid && isCasingValid;
        getMultiblock().controller().setControllerBe(this);
        validationResult = getMultiblock().validationResult;
        if(
                errorBlockPos == null ||
                (!errorBlockPos.equals(getMultiblock().errorBlockPos) && getMultiblock().errorBlockPos != null))
        {
            errorBlockPos = getMultiblock().errorBlockPos;
            changed = true;
        }

        isInternalValid = getMultiblock().isInnerValid();
        isCasingValid = getMultiblock().isOuterValid();
        height = getMultiblock().height();
        width = getMultiblock().width();
        depth = getMultiblock().depth();
        topCasing = getMultiblock().topCasing;
        bottomCasing = getMultiblock().bottomCasing;
        leftCasing = getMultiblock().leftCasing;
        rightCasing = getMultiblock().rightCasing;
        if (getMultiblock().bottomLeft != null) {
            bottomLeft = new BlockPos(getMultiblock().bottomLeft);
        }
        if (getMultiblock().topRight != null) {
            topRight = new BlockPos(getMultiblock().topRight);
        }
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
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (tag.contains("Info")) {
            CompoundTag infoTag = tag.getCompound("Info");
            infoTag.putInt("validationId", validationResult.id);
            if(errorBlockPos == null) {
                errorBlockPos = BlockPos.ZERO;
            }
            infoTag.putLong("erroredBlock", errorBlockPos.asLong());
            infoTag.putInt("upgrade_tier", upgrade_tier);
            tag.remove("Info");
            tag.put("Info", infoTag);
        }
    }

    @Override
    public void loadClientData(CompoundTag tag) {
        super.loadClientData(tag);
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
    protected void saveClientData(CompoundTag tag) {
        super.saveClientData(tag);
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

    public void toggleMultiblock(boolean mode) {
        controllerEnabled = mode || getRedstoneSignal() > 0;
        enabledByController = mode;
    }

    public <T> LazyOptional<T> getPeripheral(Capability<T> cap, Direction side) {
        return LazyOptional.empty();
    }

    public void setRedstoneByPort(int redstoneSignal) {
        this.analogSignal = (byte) redstoneSignal;
        externalControlled  = true;
        toggleMultiblock(analogSignal > 0);
    }
}
