package igentuman.nc.block.target_chamber.entity;

import igentuman.nc.NuclearCraft;
import igentuman.nc.block.entity.MultiblockPortBE;
import igentuman.nc.block.accelerator.entity.AcceleratorBeamPortBE;
import igentuman.nc.block.entity.ParticleChamberControllerBE;
import igentuman.nc.content.particles.ParticleStack;
import igentuman.nc.handler.sided.capability.FluidCapabilityHandler;
import igentuman.nc.multiblock.AbstractMultiblock;
import igentuman.nc.multiblock.MultiblockHandler;
import igentuman.nc.multiblock.particle_chamber.ParticleChamberMultiblock;
import igentuman.nc.util.Equations;
import igentuman.nc.util.PortMode;
import igentuman.nc.util.annotation.NBTField;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

import static igentuman.nc.NuclearCraft.currentTick;
import static igentuman.nc.compat.oc2.FusionReactorDevice.DEVICE_CAPABILITY;
import static igentuman.nc.content.particles.CapabilityParticleStackHandler.PARTICLE_HANDLER_CAPABILITY;
import static igentuman.nc.multiblock.accelerator.AcceleratorRegistration.ACCELERATOR_BLOCKS;
import static igentuman.nc.multiblock.particle_chamber.ParticleChamberRegistration.TARGET_CHAMBER_BE;
import static igentuman.nc.util.ModUtil.isCcLoaded;
import static igentuman.nc.util.ModUtil.isOC2Loaded;
import static igentuman.nc.util.PortMode.PORT_MODE;

public class TargetChamberBeamPortBE extends MultiblockPortBE {

    public static String NAME = "target_chamber_beam_port";

    @NBTField
    public ParticleStack clientParticle;
    protected ParticleChamberMultiblock multiblock;
    public ParticleChamberControllerBE controller;

    public TargetChamberBeamPortBE(BlockPos pPos, BlockState pBlockState) {
        super(TARGET_CHAMBER_BE.get(NAME).get(), pPos, pBlockState);
    }

    @Override
    public void setMultiblock(AbstractMultiblock multiblock) {
        this.multiblock = (ParticleChamberMultiblock) multiblock;
        markDirty();
    }

    @Override
    public ParticleChamberMultiblock getMultiblock() {
        return multiblock;
    }

    @Override
    public void tickServer() {
        if(NuclearCraft.instance.isNcBeStopped || isRemoved()) return;
        if (lastTickTime == currentTick) {
            return;
        }
        lastTickTime = currentTick;
        boolean updated = updateController();
        if(!isConnectedToController()) return;
        updateIfNeeded(updated);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if(!isConnectedToController()) return LazyOptional.empty();
        if (cap == PARTICLE_HANDLER_CAPABILITY) {
            return controller().getCapability(cap, side);
        }
        return LazyOptional.empty();
    }

    @Override
    public ParticleChamberControllerBE controller() {
        return (ParticleChamberControllerBE) super.controller();
    }

    public void extractParticle(ParticleStack particleStack) {
        Direction facing = getFacing();
        BlockPos currentPos = worldPosition.relative(facing);
        int maxDistance = 16;

        for (int distance = 0; distance < maxDistance; distance++) {

            assert level != null;
            BlockState blockState = level.getBlockState(currentPos);
            if (blockState.is(ACCELERATOR_BLOCKS.get("particle_beam").get())) {
                currentPos = currentPos.relative(facing);
                continue;
            }

            if (level.getBlockEntity(currentPos) instanceof AcceleratorBeamPortBE targetPort) {
                if (targetPort.getFacing() == facing.getOpposite() && targetPort.getBlockState().getValue(PORT_MODE) == PortMode.Mode.INPUT) {
                    if (targetPort.controller() != null) {
                        particleStack.addFocus(-Equations.focusLoss(distance-1, particleStack));
                        targetPort.controller().getCapability(PARTICLE_HANDLER_CAPABILITY, facing.getOpposite())
                                .ifPresent(handler -> {
                                    handler.reciveParticle(facing.getOpposite(), particleStack);
                                });
                    }
                }
                break;
            } else if (level.getBlockEntity(currentPos) instanceof TargetChamberBeamPortBE targetPort) {
                if (targetPort.getFacing() == facing.getOpposite() && targetPort.getBlockState().getValue(PORT_MODE) == PortMode.Mode.INPUT) {
                    if (targetPort.controller() != null) {
                        particleStack.addFocus(-Equations.focusLoss(distance-1, particleStack));
                        targetPort.getCapability(PARTICLE_HANDLER_CAPABILITY, facing.getOpposite())
                                .ifPresent(handler -> {
                                    handler.reciveParticle(facing.getOpposite(), particleStack);
                                });
                    }
                }
                break;
            } else {
                break;
            }
        }

        clientParticle = particleStack.copy();
        markDirty();
    }

    public boolean isOutput() {
        return blockState.getValue(PORT_MODE) == PortMode.Mode.OUTPUT;
    }

    public boolean isInput() {
        return blockState.getValue(PORT_MODE) == PortMode.Mode.INPUT;
    }
}
