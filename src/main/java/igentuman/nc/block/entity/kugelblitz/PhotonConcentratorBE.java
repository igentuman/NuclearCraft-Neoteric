package igentuman.nc.block.entity.kugelblitz;

import igentuman.api.nc.multiblock.MultiblockAttachable;
import igentuman.nc.NuclearCraft;
import igentuman.nc.block.entity.NuclearCraftBE;
import igentuman.nc.handler.sided.capability.FluidCapabilityHandler;
import igentuman.nc.multiblock.AbstractNCMultiblock;
import igentuman.nc.multiblock.MultiblockHandler;
import igentuman.nc.multiblock.kugelblitz.KugelblitzMultiblock;
import igentuman.nc.multiblock.kugelblitz.KugelblitzRegistration;
import igentuman.nc.util.annotation.NBTField;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static igentuman.nc.util.ModUtil.isCcLoaded;

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
    public void setMultiblock(AbstractNCMultiblock multiblock) {
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
        return controller;
    }
}
