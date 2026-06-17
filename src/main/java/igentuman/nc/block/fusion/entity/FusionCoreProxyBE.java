package igentuman.nc.block.fusion.entity;

import igentuman.api.nc.multiblock.MultiblockAttachable;
import igentuman.nc.NuclearCraft;
import igentuman.nc.block.entity.NuclearCraftBE;
import igentuman.nc.multiblock.MultiblockHandler;
import igentuman.nc.multiblock.fusion.FusionReactorMultiblock;
import igentuman.nc.util.annotation.NBTField;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.List;
import java.util.Objects;

import static igentuman.nc.NuclearCraft.currentTick;
import static igentuman.nc.NuclearCraft.debugLog;
import static igentuman.nc.compat.gregtech.GTUtils.*;
import static igentuman.nc.compat.oc2.FusionReactorDevice.DEVICE_CAPABILITY;
import static igentuman.nc.multiblock.fusion.FusionReactorRegistration.FUSION_CORE_PROXY_BE;
import static igentuman.nc.util.ModUtil.*;

public class FusionCoreProxyBE extends NuclearCraftBE implements MultiblockAttachable<FusionReactorMultiblock, FusionCoreBE> {

    @NBTField
    protected BlockPos corePos;
    protected FusionCoreBE core;
    protected FusionReactorMultiblock multiblock;
    protected byte wasSignal = 0;
    protected long lastTickTime = 0;

    public FusionCoreProxyBE(BlockPos pPos, BlockState pBlockState) {
        super(FUSION_CORE_PROXY_BE.get(), pPos, pBlockState);
    }

    @Override
    public void setMultiblock(FusionReactorMultiblock multiblock) {
        this.multiblock = multiblock;
        markDirty();
    }

    public FusionReactorMultiblock getMultiblock() {
        return multiblock;
    }

    public void setController(FusionCoreBE controllerBE) {
        core = controllerBE;
        corePos = controllerBE.getBlockPos();
    }

    protected void validateCore()
    {
        if(core != null)
        {
            if(!level.isLoaded(core.getBlockPos())) {
                return;
            }
            core = (FusionCoreBE) level.getExistingBlockEntity(core.getBlockPos());
            if(core == null) {
                core = (FusionCoreBE) level.getBlockEntity(core.getBlockPos());
            }
            corePos = core.getBlockPos();
        } else {
            if(corePos == null) return;
            core = (FusionCoreBE) level.getExistingBlockEntity(corePos);
            if(core == null) {
                core = (FusionCoreBE) level.getBlockEntity(corePos);
            }
        }
    }

    public void tickServer()
    {
        //Disallow boosters like torcherino
        if(lastTickTime == currentTick) {
            return;
        }
        lastTickTime = currentTick;
        if(currentTick % 20 == 0) {
            validateCore();
        }
        if(!(core instanceof FusionCoreBE)) {
            level.removeBlock(worldPosition, false);
            return;
        }
        //if no recipe - tick only 5 times per second
        if(currentTick % 5 == 0 && !core.hasRecipe()) {
            return;
        }
        if(wasSignal != core.analogSignal || needToUpdate) {
            needToUpdate = false;
            wasSignal = core.analogSignal;
            MultiblockHandler.get(level.dimension()).addIgnoreToUpdate(getBlockPos());
            setChanged();
            level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
        }
    }

    public FusionCoreBE controller() {

        if (NuclearCraft.instance.isNcBeStopped
                || (!getLevel().isClientSide() && Objects.requireNonNull(getLevel()).getServer() != null && !getLevel().getServer().isRunning())
        ) return null;

        if(getLevel().isClientSide() && corePos != null) {
            return (FusionCoreBE) getLevel().getExistingBlockEntity(corePos);
        }
        if(core == null && corePos != null) {
            core = (FusionCoreBE) getLevel().getExistingBlockEntity(corePos);
        }
        return core;
    }

    @Override
    public void setRemoved()
    {
        if(canInvalidateCache() && !getLevel().isClientSide()) {
            if (controller() != null) controller().invalidateCache();
        }
        super.setRemoved();
    }

    public void setCore(FusionCoreBE core) {
        FusionCoreBE wasCore = this.core;
        this.core = core;
        corePos = core.getBlockPos();
        if(wasCore != core) {
            MultiblockHandler.get(getLevel().dimension()).addIgnoreToUpdate(getBlockPos());
            setChanged();
            level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    public void destroyCore() {
        if(corePos != null) {
            BlockState st = level.getBlockState(corePos);
            if(st.equals(Blocks.AIR.defaultBlockState())) return;

            ItemStack core = new ItemStack(st.getBlock().asItem());
            core.getOrCreateTag().putInt("upgrade_tier", getCoreBE().upgrade_tier);

            level.removeBlock(corePos, false);
            Block.popResource(level, corePos, core);
        }
    }

    public FusionCoreBE getCoreBE() {
        return core;
    }

    public BlockPos getCorePos() {
        return corePos;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if(controller() == null) return super.getCapability(cap, side);
        if(side == null || side.getAxis().isHorizontal()) {
            return LazyOptional.empty();
        }
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return LazyOptional.empty();
        }
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            return controller().getCapability(cap, side);
        }
        if(isGtLoaded()) {
            if (cap == com.gregtechceu.gtceu.api.capability.forge.GTCapability.CAPABILITY_ENERGY_CONTAINER) {
                if (isGTEUCapEnabled()) {
                    return getGTEnergy(controller(), side).cast();
                }
            }
        }
        if (cap == ForgeCapabilities.ENERGY) {
            if(isGtLoaded() && isOnlyGTCEUCapEnabled()) {
                return LazyOptional.empty();
            }
            return controller().getCapability(cap, side);
        }
        if(isCcLoaded()) {
            if(cap == dan200.computercraft.shared.Capabilities.CAPABILITY_PERIPHERAL) {
                return controller().getPeripheral(cap, side);
            }
        }

        if(isOC2Loaded()) {
            if(cap == DEVICE_CAPABILITY) {
                return controller().getOCDevice(cap, side);
            }
        }

        if(isMekanismLoaded()) {
            if(cap == mekanism.common.capabilities.Capabilities.GAS_HANDLER) {
                if(controller().contentHandler().hasFluidCapability(side)) {
                    return LazyOptional.of(() -> controller().contentHandler().gasConverter(side));
                }
                return LazyOptional.empty();
            }
            if(cap == mekanism.common.capabilities.Capabilities.SLURRY_HANDLER) {
                if(controller().contentHandler().hasFluidCapability(side)) {
                    return LazyOptional.of(() -> controller().contentHandler().getSlurryConverter(side));
                }
                return LazyOptional.empty();
            }
        }
        return super.getCapability(cap, side);
    }

    @Override
    public boolean canInvalidateCache()
    {
        return false;
    }

    @Override
    public void updateAnalogSignal() {

    }

    public void sendOutEnergy() {
        int required = getCoreBE().rfAmplifiersPower + getCoreBE().magnetsPower;
        for(Direction side: List.of(Direction.UP, Direction.DOWN)) {
            if(getCoreBE().energyStorage().getEnergyStored() > required) {
                BlockEntity be = getLevel().getExistingBlockEntity(getBlockPos().relative(side));
                if(!(be instanceof BlockEntity) || (be instanceof FusionCoreProxyBE) || (be instanceof FusionCoreBE)) {
                    continue;
                }
                int wasEnergy = getCoreBE().energyStorage().getEnergyStored();

                if((isGtLoaded() && isGTEUCapEnabled())) {
                    transferEU(controller(), be, controller().energyStorage(), side);
                }
                if(isGtLoaded() && isOnlyGTCEUCapEnabled()) {
                    return;
                }

                IEnergyStorage r = be.getCapability(ForgeCapabilities.ENERGY, side.getOpposite()).orElse(null);
                if(r == null || getCoreBE() == null) continue;
                int extracted = wasEnergy - getCoreBE().energyStorage().getEnergyStored();
                if(extracted >= controller().energyStorage().getMaxExtract()) {
                    return;
                }
                int canExtract = Math.min(controller().energyStorage().getMaxExtract() - extracted,  controller().energyStorage().getEnergyStored());
                if(r.canReceive()) {
                    int available = getCoreBE().energyStorage().getEnergyStored()-required-canExtract;
                    if(available < 0) return;
                    int recieved = 0;
                    try {
                        recieved = r.receiveEnergy(Math.min(available, getCoreBE().energyStorage().getMaxExtract()), false);
                    } catch (Exception e) {
                        debugLog("Entity failed to receive FE at: " + be.getBlockPos().toShortString());
                        return;
                    }
                    getCoreBE().energyStorage().consumeEnergy(recieved);
                    controller().setChanged();
                }
            }
        }
    }

    public void forceTickServer(FusionCoreBE core) {
        this.core = core;
        core.inputRedstoneSignal =  Math.max(getLevel().getBestNeighborSignal(getBlockPos()), core.inputRedstoneSignal);
        core.rfAmplificationRatio = (int)((double)core.inputRedstoneSignal / 0.15D);
    }

    public void toggleRedstoneMode() {
        getCoreBE().toggleRedstoneMode();
    }

    public int getAnalogSignal() {
        return getCoreBE() == null ? 0 : getCoreBE().analogSignal;
    }
}
