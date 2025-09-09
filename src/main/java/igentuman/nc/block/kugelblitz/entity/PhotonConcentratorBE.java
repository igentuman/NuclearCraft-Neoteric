package igentuman.nc.block.kugelblitz.entity;

import igentuman.api.nc.multiblock.MultiblockAttachable;
import igentuman.nc.NuclearCraft;
import igentuman.nc.block.entity.NuclearCraftBE;
import igentuman.nc.multiblock.AbstractMultiblock;
import igentuman.nc.multiblock.MultiblockHandler;
import igentuman.nc.multiblock.kugelblitz.KugelblitzMultiblock;
import igentuman.nc.multiblock.kugelblitz.KugelblitzRegistration;
import igentuman.nc.util.annotation.NBTField;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import static igentuman.nc.NuclearCraft.debugLog;

public class PhotonConcentratorBE extends NuclearCraftBE implements MultiblockAttachable {

    public static String NAME = "photon_concentrator";

    @NBTField
    public BlockPos controllerPos;
    protected KugelblitzMultiblock multiblock;
    public ChamberTerminalBE controller;

    public PhotonConcentratorBE(BlockPos pPos, BlockState pBlockState) {
        super(KugelblitzRegistration.KUGELBLITZ_BE.get(NAME).get(), pPos, pBlockState);
    }

    @Override
    public void setMultiblock(AbstractMultiblock multiblock) {
        this.multiblock = (KugelblitzMultiblock) multiblock;
    }

    @Override
    public KugelblitzMultiblock getMultiblock() {
        return multiblock;
    }

    @Override
    public boolean canInvalidateCache() {
        return true;
    }

    @Override
    public ChamberTerminalBE controller() {
        if(NuclearCraft.instance.isNcBeStopped || (!getLevel().isClientSide() && getLevel().getServer() != null && !getLevel().getServer().isRunning())) return null;
        if(getLevel().isClientSide && controllerPos != null) {
            return (ChamberTerminalBE) getLevel().getExistingBlockEntity(controllerPos);
        }
        if (controller != null && !controller.isRemoved()) {
            return controller;
        }
        try {
            controller = (ChamberTerminalBE) getMultiblock().controller().controllerBE();
            if(controllerPos != controller.getBlockPos()) {
                controllerPos = controller.getBlockPos();
                setChanged();
            }
        } catch (NullPointerException e) {
            if(controllerPos != null) {
                controller = (ChamberTerminalBE) getLevel().getExistingBlockEntity(controllerPos);
            }
        }
        if(controller == null) {
            AbstractMultiblock mb = MultiblockHandler.get(level.dimension()).getMultiblockByPos(worldPosition);
            if(mb instanceof KugelblitzMultiblock kmb) {
                controller = (ChamberTerminalBE) kmb.controller().controllerBE();
                if(controller != null && controllerPos != controller.getBlockPos()) {
                    controllerPos = controller.getBlockPos();
                    setChanged();
                }
            }
        }
        return controller;
    }

    public void gotEnergy(Direction facing) {
        if(!getLevel().isClientSide()) {
            if(controller() == null) {
                debugLog("No controller found for PhotonConcentrator at "+worldPosition.toShortString());
                return;
            }
            controller().gotEnergy(facing);
        }
    }
}
